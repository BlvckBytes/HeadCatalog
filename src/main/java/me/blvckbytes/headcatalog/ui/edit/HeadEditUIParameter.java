package me.blvckbytes.headcatalog.ui.edit;

import me.blvckbytes.bukkitinventoryui.base.AUIParameter;
import me.blvckbytes.headcatalog.command.Head;
import org.bukkit.entity.Player;

public class HeadEditUIParameter extends AUIParameter<IHeadEditUIParameterProvider> {

  public final Head target;

  public HeadEditUIParameter(
    IHeadEditUIParameterProvider provider,
    Player viewer,
    Head target
  ) {
    super(provider, viewer);
    this.target = target;
  }
}
