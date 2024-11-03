package me.blvckbytes.headcatalog;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.IntFunction;

public class FakeUiRegistry extends PacketAdapter implements Listener {

  public static final ItemStack ITEM_AIR = new ItemStack(Material.AIR);

  private final Map<UUID, FakeUiSession> sessionByPlayerId;

  public final ProtocolManager protocolManager;
  private final Plugin plugin;

  public FakeUiRegistry(ProtocolManager protocolManager, Plugin plugin) {
    super(
      plugin,
      ListenerPriority.HIGHEST,
      PacketType.Play.Server.OPEN_WINDOW,
      PacketType.Play.Server.WINDOW_ITEMS,
      PacketType.Play.Server.SET_SLOT,
      PacketType.Play.Client.WINDOW_CLICK,
      PacketType.Play.Client.ITEM_NAME
    );

    this.sessionByPlayerId = new HashMap<>();
    this.protocolManager = protocolManager;
    this.plugin = plugin;
  }

  @Override
  public void onPacketReceiving(PacketEvent event) {
    var player = event.getPlayer();

    FakeUiSession session;

    if (player == null || (session = sessionByPlayerId.get(player.getUniqueId())) == null)
      return;

    if (event.getPacketType() == PacketType.Play.Client.ITEM_NAME) {
      var name = event.getPacket().getStrings().read(0);
      session.setAnvilText(name);
      event.setCancelled(true);
    }
  }

  @Override
  public void onPacketSending(PacketEvent event) {
    var player = event.getPlayer();

    FakeUiSession session;

    if (player == null || (session = sessionByPlayerId.get(player.getUniqueId())) == null)
      return;

    if (event.getPacketType() == PacketType.Play.Server.OPEN_WINDOW) {
      var packet = event.getPacket();

      if (session.titleComponentSupplier != null)
        patchWindowTitle(packet, session.titleComponentSupplier.get());

      session.windowId = packet.getIntegers().read(0);
      session.windowOpenPacket = packet;
      return;
    }

    if (event.getPacketType() == PacketType.Play.Server.WINDOW_ITEMS) {
      event.setCancelled(true);
      return;
    }

    if (event.getPacketType() == PacketType.Play.Server.SET_SLOT)
      event.setCancelled(true);
  }

  @EventHandler
  public void onClick(InventoryClickEvent event) {
    if (!(event.getWhoClicked() instanceof Player player))
      return;

    FakeUiSession session;

    if ((session = sessionByPlayerId.get(player.getUniqueId())) == null)
      return;

    session.onClick(event.getRawSlot(), event.getClick());
    event.setCancelled(true);
  }

  @EventHandler
  public void onClose(InventoryCloseEvent event) {
    if (!(event.getPlayer() instanceof Player player))
      return;

    if (sessionByPlayerId.remove(player.getUniqueId()) == null)
      return;

    var playerInventory = player.getInventory();

    runNextTick(() -> updatePlayerInventory(player, slot -> {
      var item = playerInventory.getItem(slot);
      return item == null ? ITEM_AIR : item;
    }));
  }

  public FakeUiSession createAndRegister(UiType uiType, Player player, FakeUiEventHandler clickHandler) {
    var session = new FakeUiSession(this, player, uiType, clickHandler);
    sessionByPlayerId.put(player.getUniqueId(), session);
    return session;
  }

  private void patchWindowTitle(PacketContainer windowOpenPacket, @Nullable Object titleComponent) {
    if (windowOpenPacket.getType() != PacketType.Play.Server.OPEN_WINDOW)
      throw new IllegalStateException("Can only patch the window-title on an OPEN_WINDOW packet!");

    if (titleComponent == null) {
      // TODO: Set to empty component
      return;
    }

    windowOpenPacket.getChatComponents().write(0, WrappedChatComponent.fromHandle(titleComponent));
  }

  public void sendTitle(FakeUiSession session) {
    if (session.titleComponentSupplier == null || session.windowOpenPacket == null)
      return;

    patchWindowTitle(session.windowOpenPacket, session.titleComponentSupplier.get());
    protocolManager.sendServerPacket(session.player, session.windowOpenPacket);
  }

  public void clearCursor(FakeUiSession session) {
    var packet = protocolManager.createPacket(PacketType.Play.Server.SET_SLOT);

    packet.getIntegers()
      .write(0, -1)
      // 1: state-id
      // TODO: This is version-dependent!
      .write(2, -1);

    packet.getItemModifier().write(0, ITEM_AIR);

    protocolManager.sendServerPacket(session.player, packet, false);
  }

  public void sendTopFakeSlot(FakeUiSession session, int slot) {
    var packet = protocolManager.createPacket(PacketType.Play.Server.SET_SLOT);
    var slotContent = session.getTopFakeItem(slot);

    packet.getIntegers()
      .write(0, session.windowId)
      // 1: state-id
      // TODO: This is version-dependent!
      .write(2, slot);

    packet.getItemModifier().write(0, slotContent);

    protocolManager.sendServerPacket(session.player, packet, false);
  }

  public void sendBottomFakeSlot(FakeUiSession session, int slot) {
    var packet = protocolManager.createPacket(PacketType.Play.Server.SET_SLOT);
    var slotContent = session.getBottomFakeItem(slot);

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

    protocolManager.sendServerPacket(session.player, packet, false);
  }

  public void sendTopFakeContents(FakeUiSession session) {
    var packet = protocolManager.createPacket(PacketType.Play.Server.WINDOW_ITEMS);

    packet.getIntegers().write(0, session.windowId);
    packet.getItemListModifier().write(0, session.getTopFakeItems());

    protocolManager.sendServerPacket(session.player, packet, false);
  }

  public void runNextTick(Runnable runnable) {
    Bukkit.getScheduler().runTask(plugin, runnable);
  }

  public void sendBottomFakeContents(FakeUiSession session) {
    updatePlayerInventory(session.player, session::getBottomFakeItem);
  }

  private void updatePlayerInventory(Player player, IntFunction<ItemStack> itemResolver) {
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

    for (var bottomSlot = 0; bottomSlot < 4 * 9; ++bottomSlot)
      windowItems.add(itemResolver.apply(bottomSlot));

    windowItems.add(playerInventory.getItemInOffHand());

    packet.getIntegers().write(0, 0); // windowId 0 = own inventory
    packet.getItemListModifier().write(0, windowItems);

    protocolManager.sendServerPacket(player, packet, false);
  }
}
