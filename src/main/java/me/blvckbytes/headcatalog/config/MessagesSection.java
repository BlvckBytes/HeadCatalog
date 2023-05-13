package me.blvckbytes.headcatalog.config;

import me.blvckbytes.bbconfigmapper.sections.IConfigSection;
import me.blvckbytes.bukkitevaluable.BukkitEvaluable;

public class MessagesSection implements IConfigSection {

  private BukkitEvaluable headsNotReadyYet;
  private BukkitEvaluable inventoryFull;
  private BukkitEvaluable requestedHead;
  private BukkitEvaluable missingBalance;
  private BukkitEvaluable economyError;
  private BukkitEvaluable requestedHeadPriceBypassed;

  public BukkitEvaluable getHeadsNotReadyYet() {
    return this.headsNotReadyYet;
  }

  public BukkitEvaluable getInventoryFull() {
    return this.inventoryFull;
  }

  public BukkitEvaluable getRequestedHead() {
    return this.requestedHead;
  }

  public BukkitEvaluable getMissingBalance() {
    return this.missingBalance;
  }

  public BukkitEvaluable getEconomyError() {
    return this.economyError;
  }

  public BukkitEvaluable getRequestedHeadPriceBypassed() {
    return this.requestedHeadPriceBypassed;
  }
}
