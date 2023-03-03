package me.blvckbytes.headcatalog.gui.config;

import me.blvckbytes.bukkitevaluable.IItemBuildable;

public class AnvilSearchUISection extends PageableUISection implements IAnvilSearchParameterProvider {

  private IItemBuildable filter;
  private IItemBuildable back;
  private IItemBuildable searchItem;

  @Override
  public IItemBuildable getFilter() {
    return this.filter;
  }

  @Override
  public IItemBuildable getSearchItem() {
    return this.searchItem;
  }

  @Override
  public IItemBuildable getBack() {
    return this.back;
  }
}
