package me.blvckbytes.headcatalog.config;

import me.blvckbytes.bbconfigmapper.sections.CSAlways;
import me.blvckbytes.bbconfigmapper.sections.CSInlined;
import me.blvckbytes.bbconfigmapper.sections.IConfigSection;
import me.blvckbytes.headcatalog.apis.IHeadApi;
import me.blvckbytes.headcatalog.apis.IHeadApisProvider;

import java.util.List;

public class ApisSection implements IConfigSection, IHeadApisProvider {

  @CSAlways
  @CSInlined
  private List<ApiSection> apis;

  @Override
  public List<? extends IHeadApi> getApis() {
    return apis;
  }
}
