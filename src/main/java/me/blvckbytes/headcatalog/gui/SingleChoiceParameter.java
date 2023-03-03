package me.blvckbytes.headcatalog.gui;

import me.blvckbytes.headcatalog.gui.config.AUIParameter;
import me.blvckbytes.headcatalog.gui.config.IAnvilSearchParameterProvider;
import me.blvckbytes.headcatalog.gui.config.ISingleChoiceParameterProvider;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Consumer;

public class SingleChoiceParameter<T> extends AUIParameter<ISingleChoiceParameterProvider> {

  public final ISearchFilterEnum<?, T> filterEnum;
  public final FAnvilItemsFilter<T> filterFunction;
  private final IAnvilSearchParameterProvider anvilSearchProvider;

  public SingleChoiceParameter(
    ISingleChoiceParameterProvider provider,
    Player viewer,
    IAnvilSearchParameterProvider anvilSearchProvider,
    ISearchFilterEnum<?, T> filterEnum,
    FAnvilItemsFilter<T> filterFunction
  ) {
    super(provider, viewer);
    this.filterEnum = filterEnum;
    this.filterFunction = filterFunction;
    this.anvilSearchProvider = anvilSearchProvider;
  }

  public AnvilSearchParameter<T> makeAnvilSearchParameter(@Nullable Consumer<AnvilSearchUI<T>> backHandler) {
    return new AnvilSearchParameter<>(anvilSearchProvider, viewer, filterFunction, filterEnum, backHandler);
  }
}
