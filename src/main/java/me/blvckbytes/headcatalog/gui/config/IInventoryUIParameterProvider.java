package me.blvckbytes.headcatalog.gui.config;

import me.blvckbytes.bbconfigmapper.IEvaluable;
import me.blvckbytes.bukkitevaluable.IItemBuildable;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Set;

public interface IInventoryUIParameterProvider {

  IEvaluable getTitle();

  int getNumberOfRows();

  @Nullable IItemBuildable getFill();

  @Nullable IItemBuildable getBorder();

  Map<String, Set<Long>> getSlotContents();

}
