package me.blvckbytes.headcatalog.gui.reflect;

import org.bukkit.entity.Player;

@FunctionalInterface
public interface FItemNameReceiver {

  void receive(Player player, String name);

}
