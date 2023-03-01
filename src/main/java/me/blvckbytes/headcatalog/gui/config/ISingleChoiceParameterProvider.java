package me.blvckbytes.headcatalog.gui.config;

import me.blvckbytes.bukkitevaluable.IItemBuildable;

public interface ISingleChoiceParameterProvider extends IInventoryUIParameterProvider, IPageableParameterProvider {

  IItemBuildable getSearch();

}
