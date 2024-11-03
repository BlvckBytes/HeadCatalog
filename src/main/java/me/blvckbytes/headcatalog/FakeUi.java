package me.blvckbytes.headcatalog;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import com.tcoded.folialib.impl.PlatformScheduler;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.function.IntFunction;
import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class FakeUi {

  public static final ItemStack ITEM_AIR = new ItemStack(Material.AIR);

  public final Player player;

  private int windowId;
  private @Nullable PacketContainer windowOpenPacket;

  private final ProtocolManager protocolManager;
  protected final PlatformScheduler platformScheduler;
  protected final Logger logger;
  private final UiType uiType;

  private final ItemStack[] topFakeSlots;
  private final ItemStack[] bottomFakeSlots;

  public FakeUi(
    ProtocolManager protocolManager,
    PlatformScheduler platformScheduler,
    Logger logger,
    Player player,
    UiType uiType
  ) {
    this.protocolManager = protocolManager;
    this.platformScheduler = platformScheduler;
    this.logger = logger;
    this.player = player;
    this.uiType = uiType;

    this.topFakeSlots = new ItemStack[uiType.size];
    this.bottomFakeSlots = new ItemStack[9 * 4];

    Arrays.fill(this.topFakeSlots, ITEM_AIR);
    Arrays.fill(this.bottomFakeSlots, ITEM_AIR);
  }

  public void setWindowOpenPacket(@NotNull PacketContainer packet) {
    this.windowId = packet.getIntegers().read(0);
    this.windowOpenPacket = packet;
    platformScheduler.runNextTick(task -> initializeUiContents());
  }

  public abstract void initializeUiContents();

  public abstract Object createTitleComponent() throws Throwable;

  public void sendTitle() {
    if (windowOpenPacket == null)
      return;

    patchWindowTitle(windowOpenPacket);
    protocolManager.sendServerPacket(player, windowOpenPacket);
  }

  public void patchWindowTitle(PacketContainer windowOpenPacket) {
    try {
      if (windowOpenPacket.getType() != PacketType.Play.Server.OPEN_WINDOW)
        throw new IllegalStateException("Can only patch the window-title on an OPEN_WINDOW packet!");

      windowOpenPacket.getChatComponents().write(0, WrappedChatComponent.fromHandle(createTitleComponent()));
    } catch (Throwable e) {
      logger.log(Level.SEVERE, "An error occurred while trying to patch the window-title packet", e);
    }
  }

  public void setTopFakeItem(ItemStack item, int slot) {
    if (slot < 0 || slot >= topFakeSlots.length)
      return;

    topFakeSlots[slot] = item == null ? ITEM_AIR : item;
  }

  public void sendTopFakeSlot(int slot) {
    try {
      var packet = protocolManager.createPacket(PacketType.Play.Server.SET_SLOT);
      var slotContent = getTopFakeItem(slot);

      packet.getIntegers()
        .write(0, windowId)
        // 1: state-id
        // TODO: This is version-dependent!
        .write(2, slot);

      packet.getItemModifier().write(0, slotContent);

      protocolManager.sendServerPacket(player, packet, false);
    } catch (Exception e) {
      logger.log(Level.SEVERE, "An error occurred while trying to send a top fake-slot", e);
    }
  }

  public void sendTopFakeContents() {
    try {
      var packet = protocolManager.createPacket(PacketType.Play.Server.WINDOW_ITEMS);

      packet.getIntegers().write(0, windowId);
      packet.getItemListModifier().write(0, Arrays.asList(this.topFakeSlots));

      protocolManager.sendServerPacket(player, packet, false);
    } catch (Exception e) {
      logger.log(Level.SEVERE, "An error occurred while trying to send top fake-contents", e);
    }
  }

  public @NotNull ItemStack getTopFakeItem(int slot) {
    if (slot < 0 || slot >= topFakeSlots.length)
      return ITEM_AIR;

    return topFakeSlots[slot];
  }

  public void setBottomFakeItem(ItemStack item, int slot) {
    if (slot < 0 || slot >= bottomFakeSlots.length)
      return;

    bottomFakeSlots[slot] = item == null ? ITEM_AIR : item;
  }

  public void sendBottomFakeSlot(int slot) {
    try {
      var packet = protocolManager.createPacket(PacketType.Play.Server.SET_SLOT);
      var slotContent = getBottomFakeItem(slot);

      // crafting + armor offsets
      int mappedSlot = slot + 9;

      // wrapping around for the hotbar
      if (mappedSlot >= 9 * 4)
        mappedSlot -= 9 * 4;

      packet.getIntegers()
        .write(0, -2) // Own inventory window-id
        // 1: state-id
        // TODO: This is version-dependent!
        .write(2, mappedSlot);

      packet.getItemModifier().write(0, slotContent);

      protocolManager.sendServerPacket(player, packet, false);
    } catch (Exception e) {
      logger.log(Level.SEVERE, "An error occurred while trying to send a bottom fake-slot", e);
    }
  }

  public void sendBottomFakeContents() {
    updatePlayerInventory(this::getBottomFakeItem);
  }

  public void updatePlayerInventory(IntFunction<ItemStack> itemResolver) {
    try {
      var packet = protocolManager.createPacket(PacketType.Play.Server.WINDOW_ITEMS);
      var windowItems = new ArrayList<ItemStack>(4 * 9 + 10);

      // Crafting-Result, 2x2 Crafting-Slots
      for (var i = 0; i < 5; ++i)
        windowItems.add(ITEM_AIR);

      var playerInventory = player.getInventory();

      // Always in fixed order, starting from the boots and going up to the helmet.
      var armorContents = playerInventory.getArmorContents();

      for (var i = 3; i >= 0; --i) {
        var armorContent = armorContents[i];

        if (armorContent == null) {
          windowItems.add(ITEM_AIR);
          continue;
        }

        windowItems.add(armorContent);
      }

      for (var bottomSlot = 0; bottomSlot < 4 * 9; ++bottomSlot) {
        var item = itemResolver.apply(bottomSlot);

        if (item == null) {
          windowItems.add(ITEM_AIR);
          continue;
        }

        windowItems.add(item);
      }

      windowItems.add(playerInventory.getItemInOffHand());

      packet.getIntegers().write(0, 0); // windowId 0 = own inventory
      packet.getItemListModifier().write(0, windowItems);

      protocolManager.sendServerPacket(player, packet, false);
    } catch (Exception e) {
      logger.log(Level.SEVERE, "An error occurred while trying to update the player-inventory", e);
    }
  }

  public @NotNull ItemStack getBottomFakeItem(int slot) {
    if (slot < 0 || slot >= bottomFakeSlots.length)
      return ITEM_AIR;

    return bottomFakeSlots[slot];
  }

  public void clearCursor() {
    try {
      var packet = protocolManager.createPacket(PacketType.Play.Server.SET_SLOT);

      packet.getIntegers()
        .write(0, -1)
        // 1: state-id
        // TODO: This is version-dependent!
        .write(2, -1);

      packet.getItemModifier().write(0, ITEM_AIR);

      protocolManager.sendServerPacket(player, packet, false);
    } catch (Exception e) {
      logger.log(Level.SEVERE, "An error occurred while trying to clear the cursor", e);
    }
  }

  public abstract void handleClick(int slot, boolean isTop, ClickType clickType);

  public void onRawClick(int rawSlot, ClickType clickType) {
    if (rawSlot < 0)
      return;

    clearCursor();

    boolean didRedrawTop = false;
    boolean didRedrawBottom = false;

    // TODO: Analyze based on type (fast-path) and similarity, which items could stack to the cursor
    //       If they're none, send single slot; if they're less than 5, send individual slots, else send all
    //       Also check whether the other (top/bottom) inventory is affected at all

    if (clickType == ClickType.DOUBLE_CLICK) {
      sendTopFakeContents();
      didRedrawTop = true;

      sendBottomFakeContents();
      didRedrawBottom = true;
    }

    if (rawSlot < uiType.size) {
      handleClick(rawSlot, true, clickType);

      if (!didRedrawTop)
        sendTopFakeSlot(rawSlot);

      return;
    }

    var bottomRelativeSlot = rawSlot - uiType.size;

    handleClick(bottomRelativeSlot, false, clickType);

    if (!didRedrawBottom)
      sendBottomFakeSlot(bottomRelativeSlot);
  }

  public void openInventory() {
    try {
      var inventory = uiType.makeInventory();

      if (inventory == null)
        throw new IllegalStateException("Unsupported UI-type: " + uiType.makeInventory());

      player.openInventory(inventory);
    } catch (Exception e) {
      logger.log(Level.SEVERE, "An error occurred while trying to open an inventory of type " + uiType.name(), e);
    }
  }
}
