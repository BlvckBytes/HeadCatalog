package me.blvckbytes.headcatalog.config;

import me.blvckbytes.bbconfigmapper.sections.CSAlways;
import me.blvckbytes.bbconfigmapper.sections.IConfigSection;
import me.blvckbytes.headcatalog.apis.IHeadApi;
import me.blvckbytes.headcatalog.apis.IHeadApisProvider;

import java.util.List;

public class SourceSection implements IConfigSection, IHeadApisProvider {

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
