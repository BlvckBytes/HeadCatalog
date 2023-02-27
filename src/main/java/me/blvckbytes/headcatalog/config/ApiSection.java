package me.blvckbytes.headcatalog.config;

import me.blvckbytes.bbconfigmapper.IEvaluable;
import me.blvckbytes.bbconfigmapper.sections.CSAlways;
import me.blvckbytes.bbconfigmapper.sections.IConfigSection;
import me.blvckbytes.bukkitevaluable.BukkitEvaluable;
import me.blvckbytes.headcatalog.apis.IHeadApi;

import java.util.List;

public class ApiSection implements IConfigSection, IHeadApi {

  @CSAlways
  private List<String> urls;

  private BukkitEvaluable dataType;
  private IEvaluable arrayExtractor;
  private IEvaluable itemMapper;

  @Override
  public List<String> getUrls() {
    return this.urls;
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
