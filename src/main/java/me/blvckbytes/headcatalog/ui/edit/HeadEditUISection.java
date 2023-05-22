package me.blvckbytes.headcatalog.ui.edit;

import me.blvckbytes.bukkitevaluable.IItemBuildable;
import me.blvckbytes.bukkitinventoryui.base.BaseUILayoutSection;

public class HeadEditUISection extends BaseUILayoutSection implements IHeadEditUIParameterProvider {

  private IItemBuildable blockToggle;
  private IItemBuildable categories;
  private IItemBuildable tags;
  private IItemBuildable name;
  private IItemBuildable price;

  @Override
  public IItemBuildable getBlockToggle() {
    return this.blockToggle;
  }

  @Override
  public IItemBuildable getCategories() {
    return this.categories;
  }

  @Override
  public IItemBuildable getTags() {
    return this.tags;
  }

  @Override
  public IItemBuildable getName() {
    return this.name;
  }

  @Override
  public IItemBuildable getPrice() {
    return this.price;
  }
}
