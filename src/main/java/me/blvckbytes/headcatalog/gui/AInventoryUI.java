package me.blvckbytes.headcatalog.gui;

import me.blvckbytes.bbreflect.packets.communicator.IFakeSlotCommunicator;
import me.blvckbytes.bukkitevaluable.IItemBuildable;
import me.blvckbytes.gpeee.interpreter.EvaluationEnvironmentBuilder;
import me.blvckbytes.gpeee.interpreter.IEvaluationEnvironment;
import me.blvckbytes.headcatalog.gui.config.AUIParameter;
import me.blvckbytes.headcatalog.gui.config.IInventoryUIParameterProvider;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Supplier;

public abstract class AInventoryUI<T extends IInventoryUIParameterProvider, U extends AUIParameter> implements IReadonlyInventory {

  protected final Inventory inventory;
  protected final InventoryAnimator animator;
  protected final T parameterProvider;
  protected final U parameter;
  protected final Map<String, Set<Integer>> slotContents;
  protected final IEvaluationEnvironment inventoryEnvironment;
  protected final IFakeSlotCommunicator fakeSlotCommunicator;

  private final Map<Integer, UISlot> slots;
  protected final Map<Integer, ItemStack> fakeSlotItemCache;

  public AInventoryUI(IFakeSlotCommunicator fakeSlotCommunicator, T parameterProvider, U parameter) {
    this.slots = new HashMap<>();
    this.fakeSlotCommunicator = fakeSlotCommunicator;
    this.parameter = parameter;
    this.parameterProvider = parameterProvider;
    this.inventory = createInventory();
    this.fakeSlotItemCache = new HashMap<>();
    this.animator = new InventoryAnimator(this::setItem);
    this.inventoryEnvironment = getInventoryEnvironment();
    this.slotContents = parameterProvider.getSlotContents(this.inventoryEnvironment);
  }

  private IEvaluationEnvironment getInventoryEnvironment() {
    return new EvaluationEnvironmentBuilder()
      .withStaticVariable("inventory_size", this.inventory.getSize())
      .withStaticVariable("viewer_name", this.parameter.viewer.getName())
      .build();
  }

  private void setItem(int slot, ItemStack item) {
    if (slot < 0)
      return;

    int inventorySize = this.inventory.getSize();
    if (
      slot >= inventorySize ||
      this.inventory.getType() == InventoryType.ANVIL
    ) {
      this.fakeSlotItemCache.put(slot, item);
      this.fakeSlotCommunicator.setFakeSlot(parameter.viewer, slot, true, fakeSlotItemCache.get(slot));
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
    Set<Integer> slots = slotContents.get(name);

    if (slots == null)
      return;

    for (int slot : slots)
      drawSlot(slot);
  }

  protected void drawSlot(int slot) {
    UISlot targetSlot = this.slots.get(slot);

    if (targetSlot == null) {
      setItem(slot, null);
      return;
    }

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
    this.parameter.viewer.openInventory(this.inventory);
    this.decorate();
    this.drawAll();
  }

  public void close() {
    parameter.viewer.closeInventory();
    updatePlayerInventory();
  }

  protected void decorate() {
    for (Map.Entry<String, IItemBuildable> customItemEntry : this.parameterProvider.getCustomItems().entrySet()) {
      String customItemName = customItemEntry.getKey();
      Set<Integer> customItemSlots = slotContents.get(customItemName);

      if (customItemSlots == null)
        continue;

      ItemStack customItem = customItemEntry.getValue().build(this.inventoryEnvironment);
      UISlot customItemSlot = new UISlot(() -> customItem);

      for (int slot : customItemSlots)
        setSlot(slot, customItemSlot);
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
    return this.parameter.viewer;
  }

  protected void handleClose() {
    updatePlayerInventory();
  }

  private void updatePlayerInventory() {
    if (fakeSlotItemCache.size() == 0)
      return;

    // FIXME: Revise this...

    Player viewer = parameter.viewer;
    Inventory viewerInventory = viewer.getInventory();
    int playerInventorySize = viewerInventory.getSize();
    int inventorySize = inventory.getSize();

    for (Integer fakeSlot : fakeSlotItemCache.keySet()) {
      int inventorySlot = fakeSlot - inventorySize + 9;

      if (inventorySlot < 9 || inventorySlot >= playerInventorySize + 9)
        continue;

      ItemStack realItem;
      if (inventorySlot >= 36)
        realItem = viewerInventory.getItem(inventorySlot - 36);
      else
        realItem = viewerInventory.getItem(inventorySlot);

      fakeSlotCommunicator.setFakeSlot(parameter.viewer, inventorySlot, false, realItem);
    }
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

    // Re-send fake items on interaction, as they could disappear otherwise (seldom,
    // but still). Happens if the server only clears the cursor but doesn't re-send the slot
    // Fake slots also always need to be cancelled
    ItemStack fakeItem = fakeSlotItemCache.get(slot);
    if (fakeItem != null) {
      interaction.cancel.run();
      fakeSlotCommunicator.setFakeSlot(parameter.viewer, slot, true, fakeItem);
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
