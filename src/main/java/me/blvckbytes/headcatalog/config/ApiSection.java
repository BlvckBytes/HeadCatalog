package me.blvckbytes.headcatalog.config;

import me.blvckbytes.bbconfigmapper.IEvaluable;
import me.blvckbytes.bbconfigmapper.StringUtils;
import me.blvckbytes.bbconfigmapper.sections.CSAlways;
import me.blvckbytes.bbconfigmapper.sections.IConfigSection;
import me.blvckbytes.bukkitevaluable.BukkitEvaluable;
import me.blvckbytes.headcatalog.apis.IHeadApi;

import java.lang.reflect.Field;
import java.util.List;

public class ApiSection implements IConfigSection, IHeadApi {

  private static final String DEFAULT_USER_AGENT = "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.11 (KHTML, like Gecko) Chrome/23.0.1271.95 Safari/537.11";

  @CSAlways
  private List<String> urls;

  private String userAgent;

  private BukkitEvaluable dataType;
  private IEvaluable arrayExtractor;
  private IEvaluable itemMapper;

  @Override
  public void afterParsing(List<Field> fields) throws Exception {
    if (this.userAgent == null || StringUtils.isBlank(this.userAgent))
      this.userAgent = DEFAULT_USER_AGENT;
  }

  @Override
  public String getUserAgent() {
    return this.userAgent;
  }

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
