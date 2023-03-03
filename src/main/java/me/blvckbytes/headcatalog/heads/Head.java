package me.blvckbytes.headcatalog.heads;

import me.blvckbytes.headcatalog.apis.HeadModel;
import me.blvckbytes.headcatalog.gui.IItemSupplier;
import org.bukkit.inventory.ItemStack;

public class Head implements IItemSupplier {

  public final HeadModel model;
  public final ItemStack item;

  public Head(HeadModel model, ItemStack item) {
    this.model = model;
    this.item = item;
  }

  @Override
  public ItemStack getItem() {
    return item;
  }
}
