package me.blvckbytes.headcatalog;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import com.tcoded.folialib.impl.PlatformScheduler;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.plugin.Plugin;

import java.util.*;

public class FakeUiRegistry extends PacketAdapter implements Listener {

  private final Map<UUID, FakeUi> userInterfaceByPlayerId;
  private final PlatformScheduler platformScheduler;

  public FakeUiRegistry(PlatformScheduler platformScheduler, Plugin plugin) {
    super(
      plugin,
      ListenerPriority.HIGHEST,
      PacketType.Play.Server.OPEN_WINDOW,
      PacketType.Play.Server.WINDOW_ITEMS,
      PacketType.Play.Server.SET_SLOT,
      PacketType.Play.Client.WINDOW_CLICK,
      PacketType.Play.Client.ITEM_NAME
    );

    this.userInterfaceByPlayerId = new HashMap<>();
    this.platformScheduler = platformScheduler;
    this.plugin = plugin;
  }

  @Override
  public void onPacketReceiving(PacketEvent event) {
    var player = event.getPlayer();

    FakeUi userInterface;

    if (player == null || (userInterface = userInterfaceByPlayerId.get(player.getUniqueId())) == null)
      return;

    if (event.getPacketType() == PacketType.Play.Client.ITEM_NAME) {
      if (userInterface instanceof FakeAnvilUi anvilInterface)
        anvilInterface.setAnvilText(event.getPacket().getStrings().read(0));

      event.setCancelled(true);
    }
  }

  @Override
  public void onPacketSending(PacketEvent event) {
    var player = event.getPlayer();

    FakeUi session;

    if (player == null || (session = userInterfaceByPlayerId.get(player.getUniqueId())) == null)
      return;

    if (event.getPacketType() == PacketType.Play.Server.OPEN_WINDOW) {
      var packet = event.getPacket();

      session.patchWindowTitle(packet);
      session.beforeWindowOpenPacketSent(packet);

      event.getNetworkMarker().addPostListener(session.getOrCreateOpenWindowPostListener(plugin));
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

    FakeUi session;

    if ((session = userInterfaceByPlayerId.get(player.getUniqueId())) == null)
      return;

    session.onRawClick(event.getRawSlot(), event.getClick());
    event.setCancelled(true);
  }

  @EventHandler
  public void onClose(InventoryCloseEvent event) {
    if (!(event.getPlayer() instanceof Player player))
      return;

    FakeUi userInterface;

    if ((userInterface = userInterfaceByPlayerId.remove(player.getUniqueId())) == null)
      return;

    platformScheduler.runNextTick(task -> userInterface.updatePlayerInventory(player.getInventory()::getItem));
  }

  public void onShutdown() {
    for (var userInterfaceIterator = userInterfaceByPlayerId.values().iterator(); userInterfaceIterator.hasNext();) {
      var userInterface = userInterfaceIterator.next();

      // Remove before taking any action, as to not cause the close-event's scheduler to execute,
      // because scheduling after the plugin is being disabled will result in an error
      userInterfaceIterator.remove();

      userInterface.player.closeInventory();
      userInterface.updatePlayerInventory(userInterface.player.getInventory()::getItem);
    }
  }

  public void registerFakeUi(FakeUi userInterface) {
    userInterfaceByPlayerId.put(userInterface.player.getUniqueId(), userInterface);
  }
}
