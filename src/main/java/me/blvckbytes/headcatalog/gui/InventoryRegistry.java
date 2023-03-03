package me.blvckbytes.headcatalog.gui;

import me.blvckbytes.autowirer.IAutoWirer;
import me.blvckbytes.autowirer.ICleanable;
import me.blvckbytes.autowirer.IInitializable;
import me.blvckbytes.bbreflect.packets.communicator.IItemNameCommunicator;
import me.blvckbytes.headcatalog.gui.config.AUIParameter;
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

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;

public class InventoryRegistry implements IInitializable, ICleanable, Listener {

  // FIXME: Whoops, totally forgot to unregister no longer used instances

  private final Map<Inventory, AInventoryUI<?, ?>> inventories;
  private final IAutoWirer autoWirer;
  private final IItemNameCommunicator itemNameCommunicator;
  private final Plugin plugin;
  private @Nullable BukkitTask tickerTask;

  public InventoryRegistry(Plugin plugin, IAutoWirer autoWirer, IItemNameCommunicator itemNameCommunicator) {
    this.inventories = new HashMap<>();
    this.autoWirer = autoWirer;
    this.itemNameCommunicator = itemNameCommunicator;
    this.plugin = plugin;
  }

  @SuppressWarnings("unchecked")
  public <T extends AInventoryUI<?, ?>> T createInventory(Class<T> inventoryType, AUIParameter parameter) {
    Constructor<?>[] constructors = inventoryType.getConstructors();

    if (constructors.length != 1)
      throw new IllegalStateException("Inventories need to have exactly one public constructor");

    Constructor<?> constructor = inventoryType.getConstructors()[0];
    Class<?>[] argumentTypes = constructor.getParameterTypes();
    Object[] argumentValues = new Object[argumentTypes.length];

    for (int i = 0; i < argumentTypes.length; i++) {
      Class<?> argumentType = argumentTypes[i];

      if (AUIParameter.class.isAssignableFrom(argumentType)) {
        argumentValues[i] = parameter;
        continue;
      }

      Object parameterValue = autoWirer.findInstance(argumentType);

      if (parameterValue == null)
        throw new IllegalStateException("Constructor requested unknown type: " + argumentType);

      argumentValues[i] = parameterValue;
    }

    try {
      T inventoryInstance = (T) constructor.newInstance(argumentValues);
      inventories.put(inventoryInstance.getInventory(), inventoryInstance);
      return inventoryInstance;
    } catch (Exception e) {
      throw new IllegalStateException(e);
    }
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
}
