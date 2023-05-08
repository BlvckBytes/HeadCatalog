package me.blvckbytes.headcatalog.ui;

import me.blvckbytes.bukkitinventoryui.anvilsearch.ISearchFilterEnum;
import me.blvckbytes.headcatalog.heads.Head;
import me.blvckbytes.utilitytypes.FUnsafeFunction;

import java.util.*;
import java.util.function.Function;

public enum HeadModelSearchFilter implements ISearchFilterEnum<HeadModelSearchFilter, Head> {

  HEAD_EVERYWHERE(head -> new HashSet<String>() {{
    addWordsFromString(this, head.model.name);
    addWordsFromString(this, head.model.categoriesString);
    addWordsFromString(this, head.model.tagsString);
  }}),

  HEAD_NAME(head -> new HashSet<String>() {{
    addWordsFromString(this, head.model.name);
  }}),

  HEAD_CATEGORIES(head -> new HashSet<String>() {{
    addWordsFromString(this, head.model.categoriesString);
  }}),

  HEAD_TAGS(head -> new HashSet<String>() {{
    addWordsFromString(this, head.model.tagsString);
  }}),
  ;

  private static void addWordsFromString(Set<String> set, String string) {
    // Account for provider errors
    if (string == null)
      return;

    for (String word : string.split(" "))
      set.add(word.toLowerCase(Locale.ROOT));
  }

  private static final HeadModelSearchFilter[] values = values();

  private final Function<Head, String[]> texts;
  private final Map<Head, String[]> cache;

  HeadModelSearchFilter(FUnsafeFunction<Head, Set<String>, Exception> texts) {
    this.cache = new HashMap<>();
    this.texts = o -> {
      try {
        String[] result;

        if ((result = cache.get(o)) != null)
          return result;

        Set<String> resultSet = texts.apply(o);
        result = new String[resultSet.size()];
        resultSet.toArray(result);

        cache.put(o, result);
        return result;
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
  public Function<Head, String[]> getWords() {
    return this.texts;
  }
}
