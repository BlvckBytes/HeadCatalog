package me.blvckbytes.headcatalog.gui;

import me.blvckbytes.headcatalog.gui.config.AUIParameter;
import me.blvckbytes.headcatalog.gui.config.IAnvilSearchParameterProvider;
import org.bukkit.entity.Player;

public class AnvilSearchParameter<T> extends AUIParameter<IAnvilSearchParameterProvider> {

  public final ISearchFilterEnum<?, T> searchFilter;
  public final FAnvilItemsFilter<T> filterFunction;

  public AnvilSearchParameter(
    IAnvilSearchParameterProvider provider,
    Player viewer,
    FAnvilItemsFilter<T> filterFunction,
    ISearchFilterEnum<?, T> searchFilter
  ) {
    super(provider, viewer);
    this.searchFilter = searchFilter;
    this.filterFunction = filterFunction;
  }
}
