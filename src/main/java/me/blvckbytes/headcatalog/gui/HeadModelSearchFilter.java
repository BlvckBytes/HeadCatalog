package me.blvckbytes.headcatalog.gui;

import me.blvckbytes.headcatalog.apis.HeadModel;
import me.blvckbytes.utilitytypes.FUnsafeFunction;

import java.util.function.Function;

public enum HeadModelSearchFilter implements ISearchFilterEnum<HeadModelSearchFilter> {

  HEAD_EVERYWHERE(model -> new String[] {
    model.name, model.categoriesString, model.tagsString
  }),

  HEAD_NAME(model -> new String[] {
    model.name
  }),

  HEAD_CATEGORIES(model -> new String[] {
    model.categoriesString
  }),

  HEAD_TAGS(model -> new String[] {
    model.tagsString
  }),
  ;

  private static final HeadModelSearchFilter[] values = values();

  private final Function<Object, String[]> texts;

  HeadModelSearchFilter(FUnsafeFunction<HeadModel, String[], Exception> texts) {
    this.texts = o -> {
      try {
        return texts.apply((HeadModel) o);
      } catch (Exception e) {
        e.printStackTrace();
        return new String[0];
      }
    };
  }

  @Override
  public ISearchFilterEnum<HeadModelSearchFilter>[] listValues() {
    return values;
  }

  @Override
  public Function<Object, String[]> getTexts() {
    return this.texts;
  }
}
