package me.blvckbytes.headcatalog.gui.config;

import me.blvckbytes.bbconfigmapper.IEvaluable;
import me.blvckbytes.bbconfigmapper.ScalarType;
import me.blvckbytes.bbconfigmapper.sections.IConfigSection;
import me.blvckbytes.bukkitevaluable.IItemBuildable;
import me.blvckbytes.gpeee.GPEEE;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class PageableUISection extends UIBaseLayoutSection implements IConfigSection, IPageableParameterProvider {

  private IItemBuildable previousPage;
  private IItemBuildable currentPage;
  private IItemBuildable nextPage;

  private @Nullable IEvaluable paginationSlots;

  private List<Integer> evaluatedPaginationSlots;

  @Override
  public void afterParsing(List<Field> fields) throws Exception {
    super.afterParsing(fields);

    if (paginationSlots == null) {
      this.evaluatedPaginationSlots = new ArrayList<>();
      return;
    }

    this.evaluatedPaginationSlots = paginationSlots.asList(ScalarType.INT, GPEEE.EMPTY_ENVIRONMENT);
  }

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
  public List<Integer> getPaginationSlots() {
    return this.evaluatedPaginationSlots;
  }
}
