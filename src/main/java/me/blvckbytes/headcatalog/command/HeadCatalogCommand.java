package me.blvckbytes.headcatalog.command;

import me.blvckbytes.autowirer.ICleanable;
import me.blvckbytes.autowirer.IInitializable;
import me.blvckbytes.bukkitboilerplate.IInventory;
import me.blvckbytes.bukkitboilerplate.InventoryUtil;
import me.blvckbytes.bukkitcommands.PlayerCommand;
import me.blvckbytes.bukkitevaluable.IItemBuildable;
import me.blvckbytes.bukkitevaluable.section.PermissionsSection;
import me.blvckbytes.bukkitinventoryui.IInventoryRegistry;
import me.blvckbytes.bukkitinventoryui.anvilsearch.AnvilSearchParameter;
import me.blvckbytes.bukkitinventoryui.anvilsearch.AnvilSearchUI;
import me.blvckbytes.bukkitinventoryui.anvilsearch.IAnvilSearchParameterProvider;
import me.blvckbytes.bukkitinventoryui.base.DataBoundUISlot;
import me.blvckbytes.bukkitinventoryui.base.IInventoryUI;
import me.blvckbytes.bukkitinventoryui.base.UIInteraction;
import me.blvckbytes.gpeee.interpreter.EvaluationEnvironmentBuilder;
import me.blvckbytes.gpeee.interpreter.IEvaluationEnvironment;
import me.blvckbytes.headcatalog.apis.EDeltaMode;
import me.blvckbytes.headcatalog.EPermissionNode;
import me.blvckbytes.headcatalog.apis.HeadModel;
import me.blvckbytes.headcatalog.apis.IHeadApisManager;
import me.blvckbytes.headcatalog.config.HeadCatalogCommandSection;
import me.blvckbytes.headcatalog.config.MessagesSection;
import me.blvckbytes.headcatalog.economy.EconomyAdapter;
import me.blvckbytes.headcatalog.economy.IEconomyAdapter;
import me.blvckbytes.headcatalog.ui.edit.HeadEditUI;
import me.blvckbytes.headcatalog.ui.edit.HeadEditUIParameter;
import me.blvckbytes.headcatalog.ui.edit.IHeadEditUIParameterProvider;
import me.blvckbytes.utilitytypes.Tuple;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class HeadCatalogCommand extends PlayerCommand implements IInitializable, ICleanable, Listener {

  private final IInventoryRegistry inventoryRegistry;
  private final IHeadApisManager apisManager;
  private final MessagesSection messagesSection;
  private final PermissionsSection permissionsSection;
  private final InventoryUtil inventoryUtil;
  private final IEconomyAdapter economyAdapter;

  private final IItemBuildable representativeItem;
  private final IItemBuildable representativeItemAdmin;

  private final List<DataBoundUISlot<Head>> headSlots;
  private final List<DataBoundUISlot<Head>> headSlotsAdmin;

  private final IAnvilSearchParameterProvider anvilSearchProvider;
  private final IHeadEditUIParameterProvider headEditUIParameterProvider;

  private final Map<Player, AnvilSearchUI<Head>> searchUIByPlayer;

  public HeadCatalogCommand(
    HeadCatalogCommandSection commandSection,
    IHeadApisManager apisManager,
    IRepresentativeProvider representativeProvider,
    MessagesSection messagesSection,
    PermissionsSection permissionsSection,
    IInventoryRegistry inventoryRegistry,
    IAnvilSearchParameterProvider anvilSearchProvider,
    IHeadEditUIParameterProvider headEditUIParameterProvider,
    InventoryUtil inventoryUtil,
    EconomyAdapter economyAdapter,
    Logger logger
  ) {
    super(commandSection, logger);
    this.inventoryRegistry = inventoryRegistry;
    this.messagesSection = messagesSection;
    this.permissionsSection = permissionsSection;
    this.apisManager = apisManager;

    this.representativeItem = representativeProvider.getRepresentative().asItem();
    this.representativeItemAdmin = this.representativeItem.patch(representativeProvider.getRepresentativeAdmin());

    this.anvilSearchProvider = anvilSearchProvider;
    this.headEditUIParameterProvider = headEditUIParameterProvider;

    this.inventoryUtil = inventoryUtil;
    this.economyAdapter = economyAdapter;

    this.searchUIByPlayer = new HashMap<>();

    this.headSlots = new ArrayList<>();
    this.headSlotsAdmin = new ArrayList<>();
  }

  @Override
  protected List<String> onTabComplete(CommandSender sender, String label, String[] args) {
    return EMPTY_STRING_LIST;
  }

  @Override
  protected void onPlayerInvocation(Player player, String alias, String[] args) {
    ECommandAction action = enumParameterOrElse(args, 0, ECommandAction.class, null);

    if (action == ECommandAction.ADD) {
      // TODO: /hc add <skin_url> <name>
      player.sendMessage("§cComing soon!");
      return;
    }

    if (!permissionsSection.hasPermission(player, EPermissionNode.OPEN)) {
      permissionsSection.sendMissingMessage(player, EPermissionNode.OPEN);
      return;
    }

    AnvilSearchUI<Head> ui = createOrGetSearchUI(player);
    boolean hadSlotListDelta;

    if (action == ECommandAction.ADMIN)
      hadSlotListDelta = ui.setSlots(this.headSlotsAdmin);
    else
      hadSlotListDelta = ui.setSlots(this.headSlots);

    ui.show();

    if (hadSlotListDelta)
      ui.invokeFilterFunctionAndUpdatePageSlots();
  }

  private AnvilSearchUI<Head> createOrGetSearchUI(Player player) {
    AnvilSearchUI<Head> searchUI = searchUIByPlayer.get(player);

    if (searchUI == null) {
      AnvilSearchParameter<Head> anvilSearchParameter = new AnvilSearchParameter<>(
        anvilSearchProvider, player, this.headSlots, HeadModelSearchFilter.HEAD_EVERYWHERE, null
      );

      searchUI = new AnvilSearchUI<>(anvilSearchParameter, inventoryRegistry);
      this.searchUIByPlayer.put(player, searchUI);
    }

    return searchUI;
  }

  private void handleHeadRequest(Player player, Head head, IInventoryUI ui) {
    if (!permissionsSection.hasPermission(player, EPermissionNode.REQUEST)) {
      permissionsSection.sendMissingMessage(player, EPermissionNode.REQUEST);
      return;
    }

    IInventory<?> receiver = inventoryUtil.fromBukkit(player.getInventory());
    Tuple<Integer, Runnable> result = this.inventoryUtil.prepareAddingToInventory(receiver, head.item, InventoryUtil.EMPTY_SLOT_MASK, true);

    // Item wouldn't fit
    if (result.a > 0) {
      player.sendMessage(this.messagesSection.getInventoryFull().stringify());
      return;
    }

    double price = head.model.price;
    boolean hasPriceBypass = permissionsSection.hasPermission(player, EPermissionNode.PRICE_BYPASS);

    // Only try to withdraw if the player doesn't bypass the price
    // And an economy system is available
    if (!hasPriceBypass && economyAdapter.isAvailable()) {
      double balance = economyAdapter.getBalance(player);

      if (balance < price) {
        IEvaluationEnvironment environment = new EvaluationEnvironmentBuilder()
          .withStaticVariable("balance", balance)
          .build(head.environment);

        player.sendMessage(this.messagesSection.getMissingBalance().stringify(environment));
        return;
      }

      String errorMessage;
      if ((errorMessage = economyAdapter.withdrawAmount(player, price)) != null) {
        IEvaluationEnvironment environment = new EvaluationEnvironmentBuilder()
          .withStaticVariable("error_message", errorMessage)
          .build(head.environment);

        player.sendMessage(this.messagesSection.getEconomyError().stringify(environment));
        return;
      }
    }

    // Dispatch adding the item
    result.b.run();

    if (hasPriceBypass)
      player.sendMessage(this.messagesSection.getRequestedHeadPriceBypassed().stringify(head.environment));
    else
      player.sendMessage(this.messagesSection.getRequestedHead().stringify(head.environment));

    ui.close();
  }

  private void handleHeadDelete(Player player, Head head) {
    // TODO: Implement
  }

  private void handleHeadEdit(Player player, Head head) {
    new HeadEditUI(new HeadEditUIParameter(headEditUIParameterProvider, player, head), inventoryRegistry).show();
  }

  private void onHeadClick(UIInteraction interaction, boolean admin, Head head) {
    Player player = interaction.ui.getViewer();

    if (interaction.clickType.isLeftClick()) {
      handleHeadRequest(player, head, interaction.ui);
      return;
    }

    if (!admin)
      return;

    if (interaction.clickType.isRightClick()) {
      if (interaction.clickType.isShiftClick()) {
        handleHeadDelete(player, head);
        return;
      }

      handleHeadEdit(player, head);
    }
  }

  private IEvaluationEnvironment getHeadItemEnvironment(HeadModel headModel) {
    return new EvaluationEnvironmentBuilder()
      .withStaticVariable("head", headModel)
      .build();
  }

  private void onHeadDelta(Collection<HeadModel> heads, EDeltaMode mode) {
    if (mode == EDeltaMode.REMOVED) {
      this.headSlots.removeIf(slot -> heads.contains(slot.data.model));
      this.headSlotsAdmin.removeIf(slot -> heads.contains(slot.data.model));
    }

    else {
      for (HeadModel headModel : heads) {
        IEvaluationEnvironment environment = getHeadItemEnvironment(headModel);

        ItemStack item = this.representativeItem.build(environment);
        ItemStack itemAdmin = this.representativeItemAdmin.build(environment);

        Head head = new Head(headModel, environment, item, itemAdmin);

        this.headSlots.add(new DataBoundUISlot<>(() -> head.item, interaction -> {
          onHeadClick(interaction, false, head);
          return null;
        }, head));

        this.headSlotsAdmin.add(new DataBoundUISlot<>(() -> head.itemAdmin, interaction -> {
          onHeadClick(interaction, true, head);
          return null;
        }, head));
      }

      logger.log(Level.INFO, "Mapped " + heads.size() + " head models to representative items");
    }

    for (AnvilSearchUI<Head> ui : this.searchUIByPlayer.values())
      ui.invokeFilterFunctionAndUpdatePageSlots();
  }

  @Override
  public void cleanup() {
    this.apisManager.unregisterDeltaCallback(this::onHeadDelta);
    this.searchUIByPlayer.clear();
  }

  @Override
  public void initialize() {
    this.apisManager.registerDeltaCallback(this::onHeadDelta);
  }

  @EventHandler
  public void onQuit(PlayerQuitEvent event) {
    this.searchUIByPlayer.remove(event.getPlayer());
  }
}
