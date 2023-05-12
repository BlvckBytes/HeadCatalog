package me.blvckbytes.headcatalog.config;

import me.blvckbytes.bbconfigmapper.sections.IConfigSection;
import me.blvckbytes.bukkitevaluable.BukkitEvaluable;

public class MessagesSection implements IConfigSection {

  private BukkitEvaluable headsNotReadyYet;
  private BukkitEvaluable inventoryFull;
  private BukkitEvaluable requestedHead;

  public BukkitEvaluable getHeadsNotReadyYet() {
    return this.headsNotReadyYet;
  }

  public BukkitEvaluable getInventoryFull() {
    return this.inventoryFull;
  }

  public BukkitEvaluable getRequestedHead() {
    return this.requestedHead;
  }
}
