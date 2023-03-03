package me.blvckbytes.headcatalog.gui.config;

import me.blvckbytes.bbconfigmapper.IEvaluable;
import me.blvckbytes.bbconfigmapper.ScalarType;
import me.blvckbytes.bbconfigmapper.sections.IConfigSection;
import me.blvckbytes.bukkitevaluable.IItemBuildable;
import me.blvckbytes.gpeee.interpreter.IEvaluationEnvironment;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class PageableUISection extends BaseUILayoutSection implements IConfigSection, IPageableParameterProvider {

  private IItemBuildable previousPage;
  private IItemBuildable currentPage;
  private IItemBuildable nextPage;

  private @Nullable IEvaluable paginationSlots;

  @Override
  public IItemBuildable getPreviousPage() {
    return previousPage;
  }

  @Override
  public IItemBuildable getCurrentPage() {
    return currentPage;
  }

  @Override
  public IItemBuildable getNextPage() {
    return nextPage;
  }

  @Override
  public List<Integer> getPaginationSlots(IEvaluationEnvironment environment) {
    if (paginationSlots == null)
      return new ArrayList<>();
    return paginationSlots.asList(ScalarType.INT, environment);
  }
}
