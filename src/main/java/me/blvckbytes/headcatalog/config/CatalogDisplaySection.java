package me.blvckbytes.headcatalog.config;

import me.blvckbytes.bbconfigmapper.sections.AConfigSection;
import me.blvckbytes.bbconfigmapper.sections.CSAlways;
import me.blvckbytes.bukkitevaluable.BukkitEvaluable;
import me.blvckbytes.gpeee.interpreter.EvaluationEnvironmentBuilder;

@CSAlways
public class CatalogDisplaySection extends AConfigSection {

  public BukkitEvaluable title;
  public CatalogDisplayItemsSection items;

  public CatalogDisplaySection(EvaluationEnvironmentBuilder baseEnvironment) {
    super(baseEnvironment);
  }
}
