package me.blvckbytes.headcatalog.config;

import me.blvckbytes.bukkitevaluable.section.ItemStackSection;
import me.blvckbytes.bukkitinventoryui.anvilsearch.AnvilSearchUISection;
import me.blvckbytes.headcatalog.command.IRepresentativeProvider;

public class HeadCatalogUISection extends AnvilSearchUISection implements IRepresentativeProvider {

  private ItemStackSection representative;
  private ItemStackSection representativeAdmin;

  @Override
  public ItemStackSection getRepresentative() {
    return this.representative;
  }

  @Override
  public ItemStackSection getRepresentativeAdmin() {
    return this.representativeAdmin;
  }
}
