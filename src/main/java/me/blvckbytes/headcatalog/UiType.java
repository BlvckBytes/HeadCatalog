package me.blvckbytes.headcatalog;

import org.bukkit.Bukkit;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.Nullable;

public enum UiType {
  CHEST_9X1(9),
  CHEST_9X2(9 * 2),
  CHEST_9X3(9 * 3),
  CHEST_9X4(9 * 4),
  CHEST_9X5(9 * 5),
  CHEST_9X6(9 * 6),
  DISPENSER(9),
  DROPPER(9),
  FURNACE(3),
  WORKBENCH(10),
  CRAFTING(5),
  ENCHANTING(2),
  BREWING(5),
  MERCHANT(3),
  ANVIL(3),
  SMITHING(4),
  BEACON(1),
  HOPPER(5),
  BLAST_FURNACE(3),
  LECTERN(1),
  SMOKER(3),
  LOOM(4),
  CARTOGRAPHY(3),
  GRINDSTONE(3),
  STONECUTTER(2),
  COMPOSTER(1),
  CHISELED_BOOKSHELF(6),
  JUKEBOX(1),
  ;
  
  public final int size;
  public final @Nullable InventoryType type;
  
  UiType(int size) {
    this.size = size;
    this.type = typeFromConstantNameOrNull(name());
  }

  public @Nullable Inventory makeInventory() {
    switch (this) {
      case CHEST_9X1:
      case CHEST_9X2:
      case CHEST_9X3:
      case CHEST_9X4:
      case CHEST_9X5:
      case CHEST_9X6:
        return Bukkit.createInventory(null, size);
    }

    if (type == null)
      return null;

    return Bukkit.createInventory(null, type);
  }

  private static @Nullable InventoryType typeFromConstantNameOrNull(String constantName) {
    try {
      return InventoryType.valueOf(constantName);
    } catch (Exception e) {
      return null;
    }
  }
}
