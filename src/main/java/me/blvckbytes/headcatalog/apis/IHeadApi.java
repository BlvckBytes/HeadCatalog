package me.blvckbytes.headcatalog.apis;

import me.blvckbytes.bbconfigmapper.IEvaluable;
import me.blvckbytes.bukkitevaluable.BukkitEvaluable;

import java.util.List;

public interface IHeadApi {

  List<String> getUrls();

  BukkitEvaluable getDataType();

  IEvaluable getArrayExtractor();

  IEvaluable getItemMapper();

}
