package me.blvckbytes.headcatalog.gui.config;

import me.blvckbytes.bukkitevaluable.IItemBuildable;

public class SingleChoiceUISection extends PageableUISection implements ISingleChoiceParameterProvider {

  private IItemBuildable search;

  @Override
  public IItemBuildable getSearch() {
    return search;
  }
}
