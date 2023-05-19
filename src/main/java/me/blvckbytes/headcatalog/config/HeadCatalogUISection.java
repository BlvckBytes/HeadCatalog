package me.blvckbytes.headcatalog.config;

import me.blvckbytes.bukkitevaluable.section.ItemStackSection;
import me.blvckbytes.bukkitinventoryui.anvilsearch.AnvilSearchUISection;
import me.blvckbytes.headcatalog.command.IRepresentativeProvider;

public class HeadCatalogUISection extends AnvilSearchUISection implements IRepresentativeProvider {

  private ItemStackSection representative;

  @Override
  public ItemStackSection getRepresentative() {
    return this.representative;
  }
}
