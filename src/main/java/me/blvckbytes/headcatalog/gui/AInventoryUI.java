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

  private final Map<Integer, UISlot> slots;
  private final Map<Integer, ItemStack> fakeSlotItemCache;
  private final IFakeSlotCommunicator fakeSlotCommunicator;

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
      this.fakeSlotCommunicator.setFakeSlot(slot, true, item, this.parameter.viewer, 0);
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
    Set<Integer> slots = slotContents.get(name);

    if (slots == null)
      return;

    for (int slot : slots)
      drawSlot(slot);
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
    this.parameter.viewer.openInventory(this.inventory);
    this.decorate();
    this.drawAll();
  }

  public void close() {
    while (inventory.getViewers().size() > 0)
      inventory.getViewers().remove(0).closeInventory();
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
    this.parameter.viewer.updateInventory();
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

      this.fakeSlotCommunicator.setFakeSlot(fakeItemEntry.getKey(), true, fakeItemEntry.getValue(), this.parameter.viewer, 1);
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
