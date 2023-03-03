package me.blvckbytes.headcatalog.gui.config;

import org.bukkit.entity.Player;

public abstract class AUIParameter<Provider extends IInventoryUIParameterProvider> {

  public final Player viewer;
  public final Provider provider;

  public AUIParameter(Provider provider, Player viewer) {
    this.viewer = viewer;
    this.provider = provider;
  }
}
