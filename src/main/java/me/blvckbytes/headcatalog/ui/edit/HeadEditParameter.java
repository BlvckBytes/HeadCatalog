package me.blvckbytes.headcatalog.ui.edit;

import me.blvckbytes.bukkitinventoryui.anvilsearch.IAnvilSearchParameterProvider;
import me.blvckbytes.bukkitinventoryui.base.AUIParameter;
import me.blvckbytes.headcatalog.command.Head;
import org.bukkit.entity.Player;

public class HeadEditParameter extends AUIParameter<IHeadEditUIParameterProvider> {

  public final Head target;
  public final IAnvilSearchParameterProvider anvilSearchProvider;

  public HeadEditParameter(
    IHeadEditUIParameterProvider provider,
    Player viewer,
    Head target,
    IAnvilSearchParameterProvider anvilSearchProvider
  ) {
    super(provider, viewer);
    this.target = target;
    this.anvilSearchProvider = anvilSearchProvider;
  }
}
