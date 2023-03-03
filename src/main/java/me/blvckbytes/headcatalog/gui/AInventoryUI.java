package me.blvckbytes.headcatalog.gui;

import me.blvckbytes.bukkitevaluable.IItemBuildable;
import me.blvckbytes.headcatalog.gui.config.IInventoryUIParameterProvider;
import me.blvckbytes.headcatalog.gui.reflect.IFakeSlotCommunicator;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Supplier;

public abstract class AInventoryUI<T extends IInventoryUIParameterProvider> implements IReadonlyInventory {

  protected final Inventory inventory;
  protected final InventoryAnimator animator;
  protected final T parameterProvider;
  protected final Player viewer;
  private final Map<Integer, UISlot> slots;
  private final Map<Integer, ItemStack> fakeSlotItemCache;
  private final IFakeSlotCommunicator fakeSlotCommunicator;

  public AInventoryUI(IFakeSlotCommunicator fakeSlotCommunicator, T parameterProvider, Player viewer) {
    this.slots = new HashMap<>();
    this.fakeSlotCommunicator = fakeSlotCommunicator;
    this.viewer = viewer;
    this.parameterProvider = parameterProvider;
    this.inventory = createInventory();
    this.fakeSlotItemCache = new HashMap<>();
    this.animator = new InventoryAnimator(getFillerItem(), this::setItem);
  }

  private ItemStack getFillerItem() {
    IItemBuildable fillBuildable = parameterProvider.getFill();
    if (fillBuildable != null)
      return fillBuildable.build();

    IItemBuildable borderBuildable = parameterProvider.getFill();
    if (borderBuildable != null)
      return borderBuildable.build();

    return new ItemStack(Material.AIR);
  }

  private void setItem(int slot, ItemStack item) {
    if (slot < 0)
      return;

    int inventorySize = this.inventory.getSize();
    if (
      slot >= inventorySize ||
      this.inventory.getType() == InventoryType.ANVIL
    ) {
      this.fakeSlotCommunicator.setFakeSlot(slot, true, item, viewer, 0);
      this.fakeSlotItemCache.put(slot, item);
      return;
    }

    this.inventory.setItem(slot, item);
  }


  private void setSuppliedItem(int slot, Supplier<ItemStack> item) {
    this.setItem(slot, item.get());
  }

  protected void drawAll() {
    for (Map.Entry<Integer, UISlot> slotEntry : this.slots.entrySet())
      setSuppliedItem(slotEntry.getKey(), slotEntry.getValue().itemSupplier);
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

    setSuppliedItem(slot, targetSlot.itemSupplier);
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
    // Open the inventory before decorating, so that the fake slot
    // communicator takes effect (has a target window ID), if applicable
    this.viewer.openInventory(this.inventory);
    this.decorate();
    this.drawAll();
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

  @Override
  public int getSize() {
    return this.inventory.getSize();
  }

  @Override
  public ItemStack getItem(int slot) {
    if (slot < 0)
      return null;

    int inventorySize = this.inventory.getSize();
    if (
      slot >= inventorySize ||
      this.inventory.getType() == InventoryType.ANVIL
    )
      return this.fakeSlotItemCache.get(slot);

    return this.inventory.getItem(slot);
  }

  public Player getViewer() {
    return this.viewer;
  }

  protected void handleClose() {
    viewer.updateInventory();
  }

  protected abstract boolean canInteractWithOwnInventory();

  public void handleTick(long time) {}

  private boolean isAllowedToInteractWithEmptySlot(UIInteraction interaction) {
    if (interaction.wasTopInventory)
      return false;

    if (!canInteractWithOwnInventory())
      return false;

    return interaction.action != InventoryAction.MOVE_TO_OTHER_INVENTORY;
  }

  public void handleItemRename(String name) {}

  public void handleInteraction(UIInteraction interaction) {
    int slot = interaction.slot;

    // Re-send fake items on interaction, as they could disappear otherwise
    // Fake slots also always need to be cancelled
    for (Map.Entry<Integer, ItemStack> fakeItemEntry : fakeSlotItemCache.entrySet()) {
      int fakeSlot = fakeItemEntry.getKey();

      if (slot == fakeSlot)
        interaction.cancel.run();

      this.fakeSlotCommunicator.setFakeSlot(fakeItemEntry.getKey(), true, fakeItemEntry.getValue(), viewer, 1);
    }

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
