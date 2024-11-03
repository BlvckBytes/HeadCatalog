package me.blvckbytes.headcatalog;

import com.tcoded.folialib.impl.PlatformScheduler;
import me.blvckbytes.bukkitevaluable.BukkitEvaluable;
import me.blvckbytes.bukkitevaluable.ConfigKeeper;
import me.blvckbytes.gpeee.interpreter.IEvaluationEnvironment;
import me.blvckbytes.headcatalog.config.MainSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class HeadCatalogUiRegistry implements FakeUiEventHandler, Listener {

  private final List<CatalogHead> catalogHeads;
  private final List<String> normalizedCategories;

  private final FakeUiRegistry fakeUiRegistry;
  private final PlatformScheduler platformScheduler;
  private final ConfigKeeper<MainSection> config;
  private final Logger logger;

  public HeadCatalogUiRegistry(
    FakeUiRegistry fakeUiRegistry,
    PlatformScheduler platformScheduler,
    ConfigKeeper<MainSection> config,
    Logger logger
  ) {
    this.fakeUiRegistry = fakeUiRegistry;
    this.platformScheduler = platformScheduler;
    this.config = config;
    this.logger = logger;

    this.catalogHeads = new ArrayList<>();
    this.normalizedCategories = new ArrayList<>();
  }

  @EventHandler
  public void onHeadRegistryLoad(AsyncHeadRegistryLoadEvent event) {
    logger.info("Indexing " + event.heads.size() + " heads for search");

    catalogHeads.clear();
    normalizedCategories.clear();

    for (var head : event.heads)
      catalogHeads.add(new CatalogHead(head));

    normalizedCategories.addAll(event.normalizedCategories);

    logger.info("Done!");
  }

  public HeadCatalogOpenResult createForAndOpen(Player player) {
    if (catalogHeads.isEmpty())
      return HeadCatalogOpenResult.empty(EmptyType.HEAD_CATALOG_EMPTY);

    var filteredHeads = filterHeadsByPermission(player);

    if (filteredHeads.isEmpty())
      return HeadCatalogOpenResult.empty(EmptyType.NO_ACCESS_TO_ANY_HEADS);

    var session = fakeUiRegistry.createAndRegister(UiType.ANVIL, player, this);
    var parameter = new HeadCatalogState(session, platformScheduler, config, filteredHeads);

    session.titleComponentSupplier = () -> createTitle(config.rootSection.catalogDisplay.title, parameter.paginationEnvironment);
    session.parameter = parameter;

    session.openInventory();
    parameter.onShow();

    return HeadCatalogOpenResult.ofCount(filteredHeads.size());
  }

  public @Nullable Object createTitle(BukkitEvaluable title, IEvaluationEnvironment environment) {
    try {
      return title.applicator.asChatComponent(title, environment);
    } catch (Throwable error) {
      logger.log(Level.SEVERE, "Could not create title-component", error);
      return null;
    }
  }

  private List<CatalogHead> filterHeadsByPermission(Player player) {
    var result = new ArrayList<CatalogHead>();
    var permissionResultByNumericCategory = new Boolean[normalizedCategories.size()];

    for (var head : catalogHeads) {
      var numericCategory = head.handle.numericCategory();
      Boolean hasPermission;

      if ((hasPermission = permissionResultByNumericCategory[numericCategory]) == null) {
        hasPermission = player.hasPermission("headcatalog.category." + head.handle.normalizedCategory());
        permissionResultByNumericCategory[numericCategory] = hasPermission;
      }

      if (hasPermission)
        result.add(head);
    }

    return result;
  }

  @Override
  public boolean handleClick(FakeUiSession session, int slot, boolean isTop, ClickType clickType) {
    if (!(session.parameter instanceof HeadCatalogState state))
      return true;

    if (slot == HeadCatalogState.PREVIOUS_PAGE_SLOT) {
      if (clickType.isRightClick())
        state.firstPage();
      else
        state.previousPage();
    }

    else if (slot == HeadCatalogState.NEXT_PAGE_SLOT) {
      if (clickType.isRightClick())
        state.lastPage();
      else
        state.nextPage();
    }

    return true;
  }

  @Override
  public void handleAnvilText(FakeUiSession session, String text) {
    if (session.parameter instanceof HeadCatalogState state)
      state.setSearchText(text);
  }
}
