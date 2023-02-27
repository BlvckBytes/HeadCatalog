package me.blvckbytes.headcatalog.source;

import java.util.List;

public interface IHeadApisProvider {

  List<? extends IHeadApi> getApis();

  long getUpdatePeriodSeconds();

}
