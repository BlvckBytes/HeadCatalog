package me.blvckbytes.headcatalog.gui.config;

import me.blvckbytes.bbconfigmapper.sections.IConfigSection;
import me.blvckbytes.bukkitevaluable.IItemBuildable;

import java.util.List;

public class PageableUISection extends UIBaseLayoutSection implements IConfigSection, IPageableParameterProvider {

  private IItemBuildable previousPage;
  private IItemBuildable currentPage;
  private IItemBuildable nextPage;
  private List<Integer> paginationSlots;

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
    return paginationSlots;
  }
}
