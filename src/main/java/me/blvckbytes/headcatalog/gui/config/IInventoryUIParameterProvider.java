package me.blvckbytes.headcatalog.gui.config;

import me.blvckbytes.bbconfigmapper.IEvaluable;
import me.blvckbytes.gpeee.interpreter.IEvaluationEnvironment;

import java.util.Map;
import java.util.Set;

public interface IInventoryUIParameterProvider {

  IEvaluable getTitle();

  int getNumberOfRows();

  boolean isAnimating();

  int getAnimationPeriod();

  Map<String, Set<Integer>> getSlotContents(IEvaluationEnvironment environment);

}
