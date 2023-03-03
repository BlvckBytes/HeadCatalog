package me.blvckbytes.headcatalog.gui;

import java.util.List;

public interface FAnvilItemsFilter<T> {

  List<T> applyFilter(ISearchFilterEnum<?, T> searchFilter, String text);

}
