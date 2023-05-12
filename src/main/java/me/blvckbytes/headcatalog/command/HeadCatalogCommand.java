package me.blvckbytes.headcatalog.command;

import me.blvckbytes.autowirer.ICleanable;
import me.blvckbytes.autowirer.IInitializable;
import me.blvckbytes.bukkitboilerplate.IInventory;
import me.blvckbytes.bukkitboilerplate.InventoryUtil;
import me.blvckbytes.bukkitcommands.PlayerCommand;
import me.blvckbytes.bukkitevaluable.section.PermissionsSection;
import me.blvckbytes.bukkitinventoryui.IInventoryRegistry;
import me.blvckbytes.bukkitinventoryui.anvilsearch.IAnvilSearchParameterProvider;
import me.blvckbytes.bukkitinventoryui.base.DataBoundUISlot;
import me.blvckbytes.bukkitinventoryui.base.IInventoryUI;
import me.blvckbytes.bukkitinventoryui.singlechoice.ISingleChoiceParameterProvider;
import me.blvckbytes.bukkitinventoryui.singlechoice.SingleChoiceParameter;
import me.blvckbytes.bukkitinventoryui.singlechoice.SingleChoiceUI;
import me.blvckbytes.headcatalog.EPermissionNode;
import me.blvckbytes.headcatalog.config.HeadCatalogCommandSection;
import me.blvckbytes.headcatalog.config.MessagesSection;
import me.blvckbytes.headcatalog.ui.*;
import me.blvckbytes.headcatalog.heads.Head;
import me.blvckbytes.headcatalog.heads.IHeadManager;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.*;
import java.util.logging.Logger;

public class HeadCatalogCommand extends PlayerCommand implements IInitializable, ICleanable, Listener {

  private final IInventoryRegistry inventoryRegistry;
  private final IHeadManager headManager;
  private final MessagesSection messagesSection;
  private final PermissionsSection permissionsSection;
  private final InventoryUtil inventoryUtil;

  private List<DataBoundUISlot<Head>> headSlots;
  private final IAnvilSearchParameterProvider anvilSearchProvider;
  private final ISingleChoiceParameterProvider singleChoiceProvider;

  private final Map<Player, SingleChoiceUI<Head>> headUiByPlayer;

  public HeadCatalogCommand(
    HeadCatalogCommandSection commandSection,
    IHeadManager headManager,
    MessagesSection messagesSection,
    PermissionsSection permissionsSection,
    IInventoryRegistry inventoryRegistry,
    IAnvilSearchParameterProvider anvilSearchProvider,
    ISingleChoiceParameterProvider singleChoiceProvider,
    InventoryUtil inventoryUtil,
    Logger logger
  ) {
    super(commandSection, logger);
    this.inventoryRegistry = inventoryRegistry;
    this.messagesSection = messagesSection;
    this.permissionsSection = permissionsSection;
    this.headManager = headManager;
    this.anvilSearchProvider = anvilSearchProvider;
    this.singleChoiceProvider = singleChoiceProvider;
    this.inventoryUtil = inventoryUtil;
    this.headUiByPlayer = new HashMap<>();
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

    if (this.headSlots == null) {
      player.sendMessage(this.messagesSection.getHeadsNotReadyYet().stringify());
      return;
    }

    createOrGetHeadUI(player).show();
  }

  private SingleChoiceUI<Head> createOrGetHeadUI(Player player) {
    SingleChoiceUI<Head> singleChoiceUI = headUiByPlayer.get(player);

    if (singleChoiceUI == null) {
      SingleChoiceParameter<Head> singleChoiceParameter = new SingleChoiceParameter<>(
        singleChoiceProvider, player,
        anvilSearchProvider, HeadModelSearchFilter.HEAD_EVERYWHERE, this.headSlots
      );

      singleChoiceUI = new SingleChoiceUI<>(singleChoiceParameter, inventoryRegistry);
      singleChoiceUI.setPageableSlots(this.headSlots);
      this.headUiByPlayer.put(player, singleChoiceUI);
    }

    return singleChoiceUI;
  }

  private void onHeadClick(IInventoryUI ui, Head head) {
    Player player = ui.getViewer();

    if (!permissionsSection.hasPermission(player, EPermissionNode.REQUEST)) {
      permissionsSection.sendMissingMessage(player, EPermissionNode.REQUEST);
      return;
    }

    // TODO: Implement withdrawing the price of this head

    IInventory<?> receiver = inventoryUtil.fromBukkit(player.getInventory());
    int remaining = this.inventoryUtil.addToInventory(receiver, head.item, InventoryUtil.EMPTY_SLOT_MASK, true);

    if (remaining > 0) {
      player.sendMessage(this.messagesSection.getInventoryFull().stringify());
      return;
    }

    player.sendMessage(this.messagesSection.getRequestedHead().stringify(head.environment));
    ui.close();
  }

  private void updateHeads(Collection<Head> heads) {
    List<DataBoundUISlot<Head>> result = new ArrayList<>();
    for (Head head : heads) {
      result.add(new DataBoundUISlot<>(() -> head.item, interaction -> {
        onHeadClick(interaction.ui, head);
        return null;
      }, head));
    }

    this.headSlots = result;
    for (SingleChoiceUI<Head> ui : this.headUiByPlayer.values())
      ui.setPageableSlots(this.headSlots);
  }

  @Override
  public void cleanup() {
    this.headManager.unregisterUpdateCallback(this::updateHeads);
  }

  @Override
  public void initialize() {
    this.headManager.registerUpdateCallback(this::updateHeads);
  }

  @EventHandler
  public void onQuit(PlayerQuitEvent event) {
    this.headUiByPlayer.remove(event.getPlayer());
  }
}
