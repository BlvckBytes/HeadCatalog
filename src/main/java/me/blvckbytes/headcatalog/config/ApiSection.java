package me.blvckbytes.headcatalog.config;

import me.blvckbytes.bbconfigmapper.IEvaluable;
import me.blvckbytes.bbconfigmapper.sections.IConfigSection;
import me.blvckbytes.bukkitevaluable.BukkitEvaluable;
import me.blvckbytes.headcatalog.apis.IHeadApi;

public class ApiSection implements IConfigSection, IHeadApi {

  private String url;
  private BukkitEvaluable dataType;
  private IEvaluable arrayExtractor;
  private IEvaluable itemMapper;

  @Override
  public String getUrl() {
    return this.url;
  }

  @Override
  public BukkitEvaluable getDataType() {
    return this.dataType;
  }

  @Override
  public IEvaluable getArrayExtractor() {
    return this.arrayExtractor;
  }

  @Override
  public IEvaluable getItemMapper() {
    return this.itemMapper;
  }
}
