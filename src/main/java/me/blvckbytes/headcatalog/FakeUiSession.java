package me.blvckbytes.headcatalog;

import com.comphenix.protocol.events.PacketContainer;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;

public class FakeUiSession {

  public int windowId;

  public final Player player;
  public @Nullable Object parameter;
  public @Nullable PacketContainer windowOpenPacket;
  public @Nullable Supplier<@Nullable Object> titleComponentSupplier;

  private final FakeUiRegistry registry;
  private final UiType uiType;
  private final FakeUiEventHandler eventHandler;

  private final ItemStack[] topFakeSlots;
  private final ItemStack[] bottomFakeSlots;

  public FakeUiSession(
    FakeUiRegistry registry,
    Player player,
    UiType uiType,
    FakeUiEventHandler eventHandler
  ) {
    this.registry = registry;
    this.player = player;
    this.uiType = uiType;
    this.eventHandler = eventHandler;
    this.windowId = Integer.MAX_VALUE;

    this.topFakeSlots = new ItemStack[uiType.size];
    this.bottomFakeSlots = new ItemStack[9 * 4];

    Arrays.fill(this.topFakeSlots, FakeUiRegistry.ITEM_AIR);
    Arrays.fill(this.bottomFakeSlots, FakeUiRegistry.ITEM_AIR);
  }

  public void sendTitle() {
    registry.sendTitle(this);
  }

  public void setTopFakeItem(ItemStack item, int slot) {
    if (slot < 0 || slot >= topFakeSlots.length)
      return;

    topFakeSlots[slot] = item == null ? FakeUiRegistry.ITEM_AIR : item;
  }

  public void sendTopFakeSlot(int slot) {
    registry.sendTopFakeSlot(this, slot);
  }

  public void sendTopFakeContents() {
    registry.sendTopFakeContents(this);
  }

  public @NotNull ItemStack getTopFakeItem(int slot) {
    if (slot < 0 || slot >= topFakeSlots.length)
      return FakeUiRegistry.ITEM_AIR;

    return topFakeSlots[slot];
  }

  public List<@NotNull ItemStack> getTopFakeItems() {
    return Arrays.asList(this.topFakeSlots);
  }

  public void setBottomFakeItem(ItemStack item, int slot) {
    if (slot < 0 || slot >= bottomFakeSlots.length)
      return;

    bottomFakeSlots[slot] = item == null ? FakeUiRegistry.ITEM_AIR : item;
  }

  public void sendBottomFakeSlot(int slot) {
    registry.sendBottomFakeSlot(this, slot);
  }

  public void sendBottomFakeContents() {
    registry.sendBottomFakeContents(this);
  }

  public @NotNull ItemStack getBottomFakeItem(int slot) {
    if (slot < 0 || slot >= bottomFakeSlots.length)
      return FakeUiRegistry.ITEM_AIR;

    return bottomFakeSlots[slot];
  }

  public void setAnvilText(String text) {
    eventHandler.handleAnvilText(this, text);
  }

  public void onClick(int rawSlot, ClickType clickType) {
    if (rawSlot < 0)
      return;

    registry.clearCursor(this);

    boolean didRedrawTop = false;
    boolean didRedrawBottom = false;

    // TODO: Analyze based on type (fast-path) and similarity, which items could stack to the cursor
    //       If they're none, send single slot; if they're less than 5, send individual slots, else send all
    //       Also check whether the other (top/bottom) inventory is affected at all

    if (clickType == ClickType.DOUBLE_CLICK) {
      registry.sendTopFakeContents(this);
      didRedrawTop = true;

      registry.sendBottomFakeContents(this);
      didRedrawBottom = true;
    }

    if (rawSlot < uiType.size) {
      if (eventHandler.handleClick(this, rawSlot, true, clickType)) {
        if (!didRedrawTop)
          registry.sendTopFakeSlot(this, rawSlot);
      }

      return;
    }

    var bottomRelativeSlot = rawSlot - uiType.size;

    if (eventHandler.handleClick(this, bottomRelativeSlot, false, clickType)) {
      if (!didRedrawBottom)
        registry.sendBottomFakeSlot(this, bottomRelativeSlot);
    }
  }

  public void openInventory() {
    var inventory = uiType.makeInventory();

    if (inventory == null)
      throw new IllegalStateException("Unsupported UI-type: " + uiType.makeInventory());

    player.openInventory(inventory);
  }
}
