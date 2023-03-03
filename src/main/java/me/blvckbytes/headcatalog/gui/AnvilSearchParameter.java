package me.blvckbytes.headcatalog.gui;

import me.blvckbytes.headcatalog.gui.config.AUIParameter;
import org.bukkit.entity.Player;

public class AnvilSearchParameter<T> extends AUIParameter {

  public final ISearchFilterEnum<?, T> searchFilter;
  public final FAnvilItemsFilter<T> filterFunction;

  public AnvilSearchParameter(Player viewer, FAnvilItemsFilter<T> filterFunction, ISearchFilterEnum<?, T> searchFilter) {
    super(viewer);
    this.searchFilter = searchFilter;
    this.filterFunction = filterFunction;
  }
}
