package me.blvckbytes.headcatalog.config;

import me.blvckbytes.bbconfigmapper.sections.AConfigSection;
import me.blvckbytes.bbconfigmapper.sections.CSAlways;
import me.blvckbytes.gpeee.interpreter.EvaluationEnvironmentBuilder;

@CSAlways
public class MainSection extends AConfigSection {

  public CatalogDisplaySection catalogDisplay;

  public MainSection(EvaluationEnvironmentBuilder baseEnvironment) {
    super(baseEnvironment);
  }
}
