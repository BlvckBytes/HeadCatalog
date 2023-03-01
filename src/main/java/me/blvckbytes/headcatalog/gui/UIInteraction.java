package me.blvckbytes.headcatalog.gui;

import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;

public class UIInteraction {

  public final int slot;
  public final boolean wasTopInventory;
  public final Runnable cancel;
  public final InventoryAction action;
  public final ClickType clickType;

  public UIInteraction(int slot, boolean wasTopInventory, Runnable cancel, InventoryAction action, ClickType clickType) {
    this.slot = slot;
    this.wasTopInventory = wasTopInventory;
    this.cancel = cancel;
    this.action = action;
    this.clickType = clickType;
  }

  public static UIInteraction fromDragEvent(InventoryDragEvent event, int slot) {
    Inventory topInventory = event.getView().getTopInventory();
    int topInventorySize = topInventory.getSize();
    boolean wasTopInventory = slot < topInventorySize;

    return new UIInteraction(
      slot,
      wasTopInventory,
      () -> event.setCancelled(true),
      InventoryAction.PLACE_SOME,
      ClickType.DROP
    );
  }

  public static UIInteraction fromClickEvent(InventoryClickEvent event) {
    return new UIInteraction(
      event.getRawSlot(),
      event.getClickedInventory() == event.getView().getTopInventory(),
      () -> event.setCancelled(true),
      event.getAction(), event.getClick()
    );
  }

  @Override
  public String toString() {
    return "UIInteraction{" +
      "slot=" + slot +
      ", wasTopInventory=" + wasTopInventory +
      ", action=" + action +
      ", clickType=" + clickType +
    '}';
  }
}
