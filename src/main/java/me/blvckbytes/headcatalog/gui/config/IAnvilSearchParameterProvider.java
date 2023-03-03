package me.blvckbytes.headcatalog.gui.config;

import me.blvckbytes.bukkitevaluable.IItemBuildable;

public interface IAnvilSearchParameterProvider extends IPageableParameterProvider {

  IItemBuildable getFilter();

  IItemBuildable getSearchItem();

  IItemBuildable getBack();

}
