package me.blvckbytes.headcatalog.source;

import java.util.List;

public interface IHeadSourceProvider {

  List<? extends IHeadApi> getApis();

  long getUpdatePeriodSeconds();

}
