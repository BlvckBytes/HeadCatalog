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
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Set;

public class ItemNameWatcher implements IItemNameWatcher, IInitializable, ICleanable {

  private final ClassHandle C_PI_ITEM_NAME;
  private final FieldHandle C_PI_ITEM_NAME__NAME;
  private final Set<FItemNameReceiver> receivers;
  private final IPacketInterceptorRegistry packetInterceptor;

  public ItemNameWatcher(IReflectionHelper reflectionHelper, IPacketInterceptorRegistry packetInterceptor) throws Exception {
    this.packetInterceptor = packetInterceptor;
    this.receivers = new HashSet<>();

    C_PI_ITEM_NAME = reflectionHelper.getClass(RClass.PACKET_I_ITEM_NAME);

    C_PI_ITEM_NAME__NAME = C_PI_ITEM_NAME.locateField()
      .withType(String.class)
      .required();
  }

  @Override
  public void registerReceiver(FItemNameReceiver receiver) {
    this.receivers.add(receiver);
  }

  @Override
  public void unregisterReceiver(FItemNameReceiver receiver) {
    this.receivers.remove(receiver);
  }

  private @Nullable Object interceptIncoming(@Nullable String playerName, Object packet, Object channel) throws Exception {
    if (playerName == null)
      return packet;

    if (C_PI_ITEM_NAME.isInstance(packet)) {
      Player player = Bukkit.getPlayer(playerName);

      if (player == null)
        return packet;

      String name = (String) C_PI_ITEM_NAME__NAME.get(packet);
      for (FItemNameReceiver receiver : receivers)
        receiver.receive(player, name);
    }

    return packet;
  }

  @Override
  public void cleanup() {
    this.packetInterceptor.unregisterInboundPacketInterceptor(this::interceptIncoming);
  }

  @Override
  public void initialize() {
    this.packetInterceptor.registerInboundPacketInterceptor(this::interceptIncoming, EPriority.LOWEST);
  }
}
