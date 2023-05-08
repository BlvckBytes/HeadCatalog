package me.blvckbytes.headcatalog.config;

import me.blvckbytes.bbconfigmapper.sections.IConfigSection;
import me.blvckbytes.bukkitevaluable.BukkitEvaluable;

public class MessagesSection implements IConfigSection {

  private BukkitEvaluable prefix;
  private BukkitEvaluable headsNotReadyYet;

  public BukkitEvaluable getPrefix() {
    return this.prefix;
  }

  public BukkitEvaluable getHeadsNotReadyYet() {
    return this.headsNotReadyYet;
  }
}
