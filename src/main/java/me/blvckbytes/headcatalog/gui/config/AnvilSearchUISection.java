package me.blvckbytes.headcatalog.gui.config;

import me.blvckbytes.bukkitevaluable.IItemBuildable;

import java.lang.reflect.Field;
import java.util.List;

public class AnvilSearchUISection extends PageableUISection implements IAnvilSearchParameterProvider {

  private IItemBuildable filter;
  private IItemBuildable back;
  private IItemBuildable searchItem;

  private int searchDebounceTicks;

  @Override
  public void afterParsing(List<Field> fields) throws Exception {
    super.afterParsing(fields);
    this.searchDebounceTicks = Math.max(0, searchDebounceTicks);
  }

  @Override
  public IItemBuildable getFilter() {
    return this.filter;
  }

  @Override
  public IItemBuildable getSearchItem() {
    return this.searchItem;
  }

  @Override
  public IItemBuildable getBack() {
    return this.back;
  }

  @Override
  public int getSearchDebounceTicks() {
    return searchDebounceTicks;
  }
}
