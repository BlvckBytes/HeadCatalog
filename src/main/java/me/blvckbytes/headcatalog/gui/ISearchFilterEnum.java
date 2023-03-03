package me.blvckbytes.headcatalog.gui;

import java.util.function.Function;

public interface ISearchFilterEnum<T extends Enum<?>, M> {

  String name();

  int ordinal();

  ISearchFilterEnum<T, M>[] listValues();

  /**
   * Get a list of texts to search through when provided a model's instance
   */
  Function<M, String[]> getTexts();

  /**
   * Get the next enum value in the enum's ordinal sequence
   * and wrap around if performed on the last value
   * @return Next enum value
   */
  default ISearchFilterEnum<T, M> nextValue() {
    ISearchFilterEnum<T, M>[] values = listValues();
    return values[(ordinal() + 1) % values.length];
  }
}
