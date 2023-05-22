package me.blvckbytes.headcatalog.ui.edit;

import me.blvckbytes.bukkitevaluable.IItemBuildable;
import me.blvckbytes.bukkitinventoryui.base.IInventoryUIParameterProvider;

public interface IHeadEditUIParameterProvider extends IInventoryUIParameterProvider {

  IItemBuildable getBlockToggle();

  IItemBuildable getCategories();

  IItemBuildable getTags();

  IItemBuildable getName();

  IItemBuildable getPrice();

}
