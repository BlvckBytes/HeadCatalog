package me.blvckbytes.headcatalog.config;

import me.blvckbytes.bbconfigmapper.sections.CSAlways;
import me.blvckbytes.bbconfigmapper.sections.IConfigSection;
import me.blvckbytes.headcatalog.source.IHeadApi;
import me.blvckbytes.headcatalog.source.IHeadSourceProvider;

import java.util.List;

public class SourceSection implements IConfigSection, IHeadSourceProvider {

  @CSAlways
  private List<ApiSection> apis;

  private long updatePeriodSeconds;

  @Override
  public List<? extends IHeadApi> getApis() {
    return this.apis;
  }

  @Override
  public long getUpdatePeriodSeconds() {
    return this.updatePeriodSeconds;
  }
}
