package me.blvckbytes.headcatalog.gui;

import me.blvckbytes.bukkitevaluable.IItemBuildable;
import me.blvckbytes.headcatalog.gui.config.IInventoryUIParameterProvider;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Supplier;

public abstract class AInventoryUI<T extends IInventoryUIParameterProvider> {

  private final Inventory inventory;
  protected final T parameterProvider;
  protected final Player viewer;
  private final Map<Integer, UISlot> slots;

  public AInventoryUI(T parameterProvider, Player viewer) {
    this.slots = new HashMap<>();
    this.viewer = viewer;
    this.parameterProvider = parameterProvider;
    this.inventory = createInventory();
  }

  private void setItem(int slot, Supplier<ItemStack> item) {
    if (slot < 0 || slot >= this.inventory.getSize())
      return;
    this.inventory.setItem(slot, item.get());
  }

  protected void drawAll() {
    for (Map.Entry<Integer, UISlot> slotEntry : this.slots.entrySet())
      setItem(slotEntry.getKey(), slotEntry.getValue().itemSupplier);
  }

  protected void drawNamedSlot(String name) {
    Set<Long> slots = parameterProvider.getSlotContents().get(name);

    if (slots == null)
      return;

    for (long slot : slots)
      drawSlot((int) slot);
  }

  protected void drawSlot(int slot) {
    UISlot targetSlot = this.slots.get(slot);

    if (targetSlot == null)
      return;

    setItem(slot, targetSlot.itemSupplier);
  }

  protected void setSlots(UISlot value, Collection<? extends Number> slots) {
    for (Number slot : slots)
      this.slots.put(slot.intValue(), value);
  }

  protected void setSlot(int slot, @Nullable UISlot value) {
    if (value == null) {
      this.slots.remove(slot);
      return;
    }

    this.slots.put(slot, value);
  }

  public void show() {
    this.decorate();
    this.drawAll();
    this.viewer.openInventory(this.inventory);
  }

  public void close() {
    while (inventory.getViewers().size() > 0)
      inventory.getViewers().remove(0).closeInventory();
  }

  protected void decorate() {
    IItemBuildable borderBuildable = parameterProvider.getBorder();
    IItemBuildable fillBuildable = parameterProvider.getFill();
    int invSize = inventory.getSize();

    if (fillBuildable != null) {
      UISlot slotValue = new UISlot(fillBuildable::build);
      for (int i = 0; i < invSize; i++)
        setSlot(i, slotValue);
    }

    // Borders are only supported on row-based inventories
    if (borderBuildable != null && invSize % 9 == 0) {
      UISlot slotValue = new UISlot(borderBuildable::build);
      int numRows = invSize / 9;

      for (int i = 0; i < invSize; i++) {
        if (
          // First row
          i < 9 ||
          // First slot in row
          i % 9 == 0 ||
          // Last slot in row
          (i + 1) % 9 == 0 ||
          // Last row
          i >= (numRows - 1) * 9
        ) {
          setSlot(i, slotValue);
        }
      }
    }
  }

  protected abstract Inventory createInventory();

  public Inventory getInventory() {
    return this.inventory;
  }

  public Player getViewer() {
    return this.viewer;
  }

  protected abstract void handleClose();

  protected abstract boolean canInteractWithOwnInventory();

  private boolean isAllowedToInteractWithEmptySlot(UIInteraction interaction) {
    if (interaction.wasTopInventory)
      return false;

    if (!canInteractWithOwnInventory())
      return false;

    return interaction.action != InventoryAction.MOVE_TO_OTHER_INVENTORY;
  }

  public void handleInteraction(UIInteraction interaction) {
    int slot = interaction.slot;

    if (!interaction.wasTopInventory)
      slot += inventory.getSize();

    UISlot targetSlot = slots.get(slot);

    if (targetSlot == null || targetSlot.interactionHandler == null) {
      if (!isAllowedToInteractWithEmptySlot(interaction))
        interaction.cancel.run();

      return;
    }

    EnumSet<EClickResultFlag> resultFlags = targetSlot.interactionHandler.handle(interaction);

    if (resultFlags == null) {
      interaction.cancel.run();
      return;
    }

    for (EClickResultFlag resultFlag : resultFlags) {
      if (resultFlag.isCancelling(interaction)) {
        interaction.cancel.run();
        return;
      }
    }
  }
}
