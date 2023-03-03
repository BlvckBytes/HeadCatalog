package me.blvckbytes.headcatalog.gui;

import org.bukkit.inventory.ItemStack;

public interface IReadonlyInventory {

  int getSize();

  ItemStack getItem(int slot);

}
