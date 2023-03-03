package me.blvckbytes.headcatalog.gui;

import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

public class DataBoundUISlot<T> extends UISlot {

  public final T data;

  public DataBoundUISlot(Supplier<ItemStack> itemSupplier, @Nullable IInteractionHandler interactionHandler, T data) {
    super(itemSupplier, interactionHandler);
    this.data = data;
  }
}
