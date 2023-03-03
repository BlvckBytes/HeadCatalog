package me.blvckbytes.headcatalog.gui.config;

import me.blvckbytes.bukkitevaluable.IItemBuildable;

public class AnvilSearchUISection extends PageableUISection implements IAnvilSearchParameterProvider {

  private IItemBuildable filter;

  @Override
  public IItemBuildable getFilter() {
    return this.filter;
  }
}
