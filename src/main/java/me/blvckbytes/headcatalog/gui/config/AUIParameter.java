package me.blvckbytes.headcatalog.gui.config;

import org.bukkit.entity.Player;

public abstract class AUIParameter {

  public final Player viewer;

  public AUIParameter(Player viewer) {
    this.viewer = viewer;
  }
}
