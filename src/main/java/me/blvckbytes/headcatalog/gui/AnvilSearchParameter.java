package me.blvckbytes.headcatalog.gui;

import me.blvckbytes.headcatalog.gui.config.AUIParameter;
import org.bukkit.entity.Player;

public class AnvilSearchParameter extends AUIParameter {

  public final ISearchFilterEnum<?> searchFilter;

  public AnvilSearchParameter(Player viewer, ISearchFilterEnum<?> searchFilter) {
    super(viewer);
    this.searchFilter = searchFilter;
  }
}
