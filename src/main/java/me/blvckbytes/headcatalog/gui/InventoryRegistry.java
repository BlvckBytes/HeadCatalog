package me.blvckbytes.headcatalog.gui;

import me.blvckbytes.autowirer.ICleanable;
import me.blvckbytes.autowirer.IInitializable;
import me.blvckbytes.bbreflect.packets.communicator.IFakeSlotCommunicator;
import me.blvckbytes.bbreflect.packets.communicator.IItemNameCommunicator;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public class InventoryRegistry implements IInventoryRegistry, IInitializable, ICleanable, Listener {

  private final Map<Inventory, AInventoryUI<?, ?>> inventories;
  private final IItemNameCommunicator itemNameCommunicator;
  private final IFakeSlotCommunicator fakeSlotCommunicator;
  private final Plugin plugin;
  private @Nullable BukkitTask tickerTask;

  public InventoryRegistry(Plugin plugin, IFakeSlotCommunicator fakeSlotCommunicator, IItemNameCommunicator itemNameCommunicator) {
    this.inventories = new HashMap<>();
    this.fakeSlotCommunicator = fakeSlotCommunicator;
    this.itemNameCommunicator = itemNameCommunicator;
    this.plugin = plugin;
  }

  @Override
  public void cleanup() {
    this.itemNameCommunicator.unregisterReceiver(this::onAnvilItemRename);

    for (AInventoryUI<?, ?> inventory : inventories.values())
      inventory.close();

    if (this.tickerTask != null) {
      this.tickerTask.cancel();
      this.tickerTask = null;
    }
  }

  @EventHandler
  public void onClose(InventoryCloseEvent event) {
    AInventoryUI<?, ?> inventoryUI = inventories.get(event.getInventory());

    if (inventoryUI == null)
      return;

    inventoryUI.handleClose();
  }

  @EventHandler
  public void onDrag(InventoryDragEvent event) {
    Inventory topInventory = event.getView().getTopInventory();
    AInventoryUI<?, ?> inventoryUI = inventories.get(topInventory);

    if (inventoryUI == null)
      return;

    for (int slot : event.getRawSlots()) {
      inventoryUI.handleInteraction(UIInteraction.fromDragEvent(inventoryUI, event, slot));

      if (event.isCancelled())
        break;
    }
  }

  @EventHandler
  public void onClick(InventoryClickEvent event) {
    Inventory clickedInventory = event.getClickedInventory();

    if (clickedInventory == null)
      return;

    Inventory topInventory = event.getView().getTopInventory();
    AInventoryUI<?, ?> inventoryUI = inventories.get(topInventory);

    if (inventoryUI == null)
      return;

    inventoryUI.handleInteraction(UIInteraction.fromClickEvent(inventoryUI, event));
  }

  private void onAnvilItemRename(Player player, String name) {
    Inventory topInventory = player.getOpenInventory().getTopInventory();
    AInventoryUI<?, ?> inventoryUI = inventories.get(topInventory);

    if (inventoryUI == null)
      return;

    inventoryUI.handleItemRename(name);
  }

  @Override
  public void initialize() {
    this.itemNameCommunicator.registerReceiver(this::onAnvilItemRename);

    tickerTask = Bukkit.getScheduler().runTaskTimer(plugin, new Runnable() {

      long time = 0;

      @Override
      public void run() {
        for (AInventoryUI<?, ?> inventory : inventories.values())
          inventory.handleTick(time);
        ++time;
      }
    }, 0L, 0L);
  }

  @Override
  public void register(AInventoryUI<?, ?> ui) {
    this.inventories.put(ui.getInventory(), ui);
  }

  @Override
  public void unregister(AInventoryUI<?, ?> ui) {
    this.inventories.remove(ui.getInventory());
  }

  @Override
  public IFakeSlotCommunicator getFakeSlotCommunicator() {
    return this.fakeSlotCommunicator;
  }
}