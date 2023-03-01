package me.blvckbytes.headcatalog.gui;

import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

public class UISlot {

  public final Supplier<ItemStack> itemSupplier;
  public final @Nullable IInteractionHandler interactionHandler;

  public UISlot(Supplier<ItemStack> itemSupplier) {
    this(itemSupplier, null);
  }

  public UISlot(Supplier<ItemStack> itemSupplier, @Nullable IInteractionHandler interactionHandler) {
    this.interactionHandler = interactionHandler;
    this.itemSupplier = itemSupplier;
  }
}
