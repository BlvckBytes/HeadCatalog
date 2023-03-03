package me.blvckbytes.headcatalog.heads;

import me.blvckbytes.headcatalog.apis.HeadModel;
import org.bukkit.inventory.ItemStack;

public class Head {

  public final HeadModel model;
  public final ItemStack item;

  public Head(HeadModel model, ItemStack item) {
    this.model = model;
    this.item = item;
  }
}
