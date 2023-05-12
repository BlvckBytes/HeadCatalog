package me.blvckbytes.headcatalog.config;

import me.blvckbytes.bbconfigmapper.sections.IConfigSection;
import me.blvckbytes.bukkitevaluable.BukkitEvaluable;

public class MessagesSection implements IConfigSection {

  private BukkitEvaluable headsNotReadyYet;

  public BukkitEvaluable getHeadsNotReadyYet() {
    return this.headsNotReadyYet;
  }
}
