package me.blvckbytes.headcatalog.gui.config;

import me.blvckbytes.bukkitevaluable.IItemBuildable;

import java.util.List;
import java.util.Set;

public interface IPageableParameterProvider extends IInventoryUIParameterProvider {

  IItemBuildable getPreviousPage();

  IItemBuildable getCurrentPage();

  IItemBuildable getNextPage();

  List<Integer> getPaginationSlots();

}
