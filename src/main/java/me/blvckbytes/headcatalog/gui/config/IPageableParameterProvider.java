package me.blvckbytes.headcatalog.gui.config;

import me.blvckbytes.bukkitevaluable.IItemBuildable;
import me.blvckbytes.gpeee.interpreter.IEvaluationEnvironment;

import java.util.List;

public interface IPageableParameterProvider extends IInventoryUIParameterProvider {

  IItemBuildable getPreviousPage();

  IItemBuildable getCurrentPage();

  IItemBuildable getNextPage();

  List<Integer> getPaginationSlots(IEvaluationEnvironment environment);

}
