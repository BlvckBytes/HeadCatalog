package me.blvckbytes.headcatalog.ui;

import me.blvckbytes.bukkitinventoryui.anvilsearch.ISearchFilterEnum;
import me.blvckbytes.headcatalog.heads.Head;
import me.blvckbytes.utilitytypes.FUnsafeFunction;

import java.util.function.Function;

public enum HeadModelSearchFilter implements ISearchFilterEnum<HeadModelSearchFilter, Head> {

  HEAD_EVERYWHERE(head -> new String[] {
    head.model.name, head.model.categoriesString, head.model.tagsString
  }),

  HEAD_NAME(head -> new String[] {
    head.model.name
  }),

  HEAD_CATEGORIES(head -> new String[] {
    head.model.categoriesString
  }),

  HEAD_TAGS(head -> new String[] {
    head.model.tagsString
  }),
  ;

  private static final HeadModelSearchFilter[] values = values();

  private final Function<Head, String[]> texts;

  HeadModelSearchFilter(FUnsafeFunction<Head, String[], Exception> texts) {
    this.texts = o -> {
      try {
        return texts.apply(o);
      } catch (Exception e) {
        e.printStackTrace();
        return new String[0];
      }
    };
  }

  @Override
  public ISearchFilterEnum<HeadModelSearchFilter, Head>[] listValues() {
    return values;
  }

  @Override
  public Function<Head, String[]> getTexts() {
    return this.texts;
  }
}
