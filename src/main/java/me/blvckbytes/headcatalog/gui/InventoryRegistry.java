package me.blvckbytes.headcatalog.gui;

import me.blvckbytes.autowirer.IAutoWirer;
import me.blvckbytes.autowirer.ICleanable;
import me.blvckbytes.headcatalog.gui.config.AUIParameter;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;

public class InventoryRegistry implements ICleanable, Listener {

  private final Map<Inventory, AInventoryUI<?>> inventories;
  private final IAutoWirer autoWirer;

  public InventoryRegistry(IAutoWirer autoWirer) {
    this.inventories = new HashMap<>();
    this.autoWirer = autoWirer;
  }

  @SuppressWarnings("unchecked")
  public <T extends AInventoryUI<?>> T createInventory(Class<T> inventoryType, AUIParameter parameter) {
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
    for (AInventoryUI<?> inventory : inventories.values())
      inventory.close();
    inventories.clear();
  }

  @EventHandler
  public void onClose(InventoryCloseEvent event) {
    AInventoryUI<?> inventoryUI = inventories.get(event.getInventory());

    if (inventoryUI == null)
      return;

    inventoryUI.handleClose();
  }

  @EventHandler
  public void onDrag(InventoryDragEvent event) {
    Inventory topInventory = event.getView().getTopInventory();
    AInventoryUI<?> inventoryUI = inventories.get(topInventory);

    if (inventoryUI == null)
      return;

    for (int slot : event.getRawSlots()) {
      inventoryUI.handleInteraction(UIInteraction.fromDragEvent(event, slot));

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
    AInventoryUI<?> inventoryUI = inventories.get(topInventory);

    if (inventoryUI == null)
      return;

    inventoryUI.handleInteraction(UIInteraction.fromClickEvent(event));
  }
}
