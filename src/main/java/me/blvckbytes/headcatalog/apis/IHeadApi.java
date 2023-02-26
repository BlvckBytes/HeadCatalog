package me.blvckbytes.headcatalog.apis;

import me.blvckbytes.bbconfigmapper.IEvaluable;
import me.blvckbytes.bukkitevaluable.BukkitEvaluable;

public interface IHeadApi {

  String getUrl();

  BukkitEvaluable getDataType();

  IEvaluable getArrayExtractor();

  IEvaluable getItemMapper();

}
