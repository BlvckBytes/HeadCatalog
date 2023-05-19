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

  private final List<DataBoundUISlot<Head>> headSlots;
  private final IAnvilSearchParameterProvider anvilSearchProvider;

  private final Map<Player, AnvilSearchUI<Head>> searchUIByPlayer;

  public HeadCatalogCommand(
    HeadCatalogCommandSection commandSection,
    IHeadApisManager apisManager,
    IRepresentativeProvider representativeProvider,
    MessagesSection messagesSection,
    PermissionsSection permissionsSection,
    IInventoryRegistry inventoryRegistry,
    IAnvilSearchParameterProvider anvilSearchProvider,
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
    this.anvilSearchProvider = anvilSearchProvider;
    this.inventoryUtil = inventoryUtil;
    this.economyAdapter = economyAdapter;

    this.searchUIByPlayer = new HashMap<>();
    this.headSlots = new ArrayList<>();
  }

  @Override
  protected List<String> onTabComplete(CommandSender commandSender, String s, String[] strings) {
    return EMPTY_STRING_LIST;
  }

  @Override
  protected void onPlayerInvocation(Player player, String alias, String[] args) {
    if (!permissionsSection.hasPermission(player, EPermissionNode.OPEN)) {
      permissionsSection.sendMissingMessage(player, EPermissionNode.OPEN);
      return;
    }

    createOrGetSearchUI(player).show();
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

  private void onHeadClick(IInventoryUI ui, Head head) {
    Player player = ui.getViewer();

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

  private IEvaluationEnvironment getHeadItemEnvironment(HeadModel headModel) {
    return new EvaluationEnvironmentBuilder()
      .withStaticVariable("head", headModel)
      .build();
  }

  private void onHeadDelta(Collection<HeadModel> heads, EDeltaMode mode) {
    if (mode == EDeltaMode.REMOVED) {
      this.headSlots.removeIf(slot -> heads.contains(slot.data.model));
    }

    else {
      for (HeadModel headModel : heads) {
        IEvaluationEnvironment environment = getHeadItemEnvironment(headModel);
        ItemStack item = this.representativeItem.build(environment);
        Head head = new Head(headModel, environment, item);

        this.headSlots.add(new DataBoundUISlot<>(() -> head.item, interaction -> {
          onHeadClick(interaction.ui, head);
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
