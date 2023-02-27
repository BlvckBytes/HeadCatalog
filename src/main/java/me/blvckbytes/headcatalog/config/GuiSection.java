package me.blvckbytes.headcatalog.config;

import me.blvckbytes.bbconfigmapper.sections.IConfigSection;
import me.blvckbytes.bukkitevaluable.section.ItemStackSection;
import me.blvckbytes.headcatalog.heads.IRepresentativeProvider;

public class GuiSection implements IConfigSection, IRepresentativeProvider {

  private ItemStackSection representative;

  @Override
  public ItemStackSection getRepresentative() {
    return this.representative;
  }
}
