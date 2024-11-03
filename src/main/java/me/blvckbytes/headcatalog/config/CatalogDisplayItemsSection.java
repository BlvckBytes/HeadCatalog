package me.blvckbytes.headcatalog.config;

import me.blvckbytes.bbconfigmapper.sections.AConfigSection;
import me.blvckbytes.bukkitevaluable.IItemBuildable;
import me.blvckbytes.gpeee.interpreter.EvaluationEnvironmentBuilder;

public class CatalogDisplayItemsSection extends AConfigSection {

  public IItemBuildable representative;
  public IItemBuildable previousPage;
  public IItemBuildable nextPage;
  public IItemBuildable anvilInputItem;
  public IItemBuildable anvilResultItem;
  public IItemBuildable filler;

  public CatalogDisplayItemsSection(EvaluationEnvironmentBuilder baseEnvironment) {
    super(baseEnvironment);
  }
}
