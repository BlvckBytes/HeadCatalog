package me.blvckbytes.headcatalog.gui.reflect;

import me.blvckbytes.autowirer.ICleanable;
import me.blvckbytes.autowirer.IInitializable;
import me.blvckbytes.bbreflect.IReflectionHelper;
import me.blvckbytes.bbreflect.RClass;
import me.blvckbytes.bbreflect.handle.ClassHandle;
import me.blvckbytes.bbreflect.handle.FieldHandle;
import me.blvckbytes.bbreflect.packets.EPriority;
import me.blvckbytes.bbreflect.packets.IPacketInterceptorRegistry;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public class WindowOpenWatcher implements IWindowOpenWatcher, Listener, IInitializable, ICleanable {

  private final Map<Player, Integer> topInventoryWindowIdByPlayer;
  private final ClassHandle C_PI_CLOSE_WINDOW, C_PO_OPEN_WINDOW;
  private final FieldHandle C_PI_CLOSE_WINDOW__WINDOW_ID, C_PO_OPEN_WINDOW__WINDOW_ID;
  private final IPacketInterceptorRegistry packetInterceptor;

  public WindowOpenWatcher(IReflectionHelper reflectionHelper, IPacketInterceptorRegistry packetInterceptor) throws Exception {
    this.packetInterceptor = packetInterceptor;
    this.topInventoryWindowIdByPlayer = new HashMap<>();

    C_PI_CLOSE_WINDOW = reflectionHelper.getClass(RClass.PACKET_I_CLOSE_WINDOW);
    C_PO_OPEN_WINDOW = reflectionHelper.getClass(RClass.PACKET_O_OPEN_WINDOW);

    C_PI_CLOSE_WINDOW__WINDOW_ID = C_PI_CLOSE_WINDOW.locateField()
      .withType(int.class)
      .required();

    C_PO_OPEN_WINDOW__WINDOW_ID = C_PO_OPEN_WINDOW.locateField()
      .withType(int.class)
      .required();
  }

  @Override
  public int getCurrentTopInventoryWindowId(Player player) {
    Integer windowId = this.topInventoryWindowIdByPlayer.get(player);

    if (windowId == null)
      return -1;

    return windowId;
  }

  private @Nullable Object interceptIncoming(@Nullable String playerName, Object packet, Object channel) throws Exception {
    if (playerName == null)
      return packet;

    if (C_PI_CLOSE_WINDOW.isInstance(packet)) {
      int windowId = (int) C_PI_CLOSE_WINDOW__WINDOW_ID.get(packet);
      Player player = Bukkit.getPlayer(playerName);

      if (player == null)
        return packet;

      Integer currentWindowId = topInventoryWindowIdByPlayer.get(player);

      if (currentWindowId == null)
        return packet;

      if (currentWindowId == windowId)
        topInventoryWindowIdByPlayer.remove(player);
    }

    return packet;
  }

  private @Nullable Object interceptOutgoing(@Nullable String playerName, Object packet, Object channel) throws Exception {
    if (playerName == null)
      return packet;

    if (C_PO_OPEN_WINDOW.isInstance(packet)) {
      int windowId = (int) C_PO_OPEN_WINDOW__WINDOW_ID.get(packet);
      Player player = Bukkit.getPlayer(playerName);

      if (player == null)
        return packet;

      topInventoryWindowIdByPlayer.put(player, windowId);
    }

    return packet;
  }

  @EventHandler
  public void onQuit(PlayerQuitEvent event) {
    this.topInventoryWindowIdByPlayer.remove(event.getPlayer());
  }

  @Override
  public void cleanup() {
    this.packetInterceptor.unregisterInboundPacketInterceptor(this::interceptIncoming);
    this.packetInterceptor.unregisterOutboundPacketInterceptor(this::interceptOutgoing);
  }

  @Override
  public void initialize() {
    this.packetInterceptor.registerInboundPacketInterceptor(this::interceptIncoming, EPriority.LOWEST);
    this.packetInterceptor.registerOutboundPacketInterceptor(this::interceptOutgoing, EPriority.LOWEST);
  }
}
