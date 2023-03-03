package me.blvckbytes.headcatalog.gui;

import me.blvckbytes.headcatalog.gui.config.AUIParameter;
import me.blvckbytes.headcatalog.gui.config.IAnvilSearchParameterProvider;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

public class AnvilSearchParameter<T> extends AUIParameter<IAnvilSearchParameterProvider> {

  public final ISearchFilterEnum<?, T> filterEnum;
  public final FAnvilItemsFilter<T> filterFunction;
  public final @Nullable Consumer<AnvilSearchUI<T>> backHandler;

  public AnvilSearchParameter(
    IAnvilSearchParameterProvider provider,
    Player viewer,
    FAnvilItemsFilter<T> filterFunction,
    ISearchFilterEnum<?, T> filterEnum,
    @Nullable Consumer<AnvilSearchUI<T>> backHandler
  ) {
    super(provider, viewer);
    this.filterEnum = filterEnum;
    this.filterFunction = filterFunction;
    this.backHandler = backHandler;
  }
}
