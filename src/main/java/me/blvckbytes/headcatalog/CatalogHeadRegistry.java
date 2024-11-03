package me.blvckbytes.headcatalog;

import com.comphenix.protocol.ProtocolManager;
import com.tcoded.folialib.impl.PlatformScheduler;
import me.blvckbytes.bukkitevaluable.ConfigKeeper;
import me.blvckbytes.headcatalog.config.MainSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class CatalogHeadRegistry implements Listener {

  private final List<CatalogHead> catalogHeads;
  private final List<String> normalizedCategories;

  private final FakeUiRegistry fakeUiRegistry;
  private final ProtocolManager protocolManager;
  private final PlatformScheduler platformScheduler;
  private final ConfigKeeper<MainSection> config;
  private final Logger logger;

  public CatalogHeadRegistry(
    FakeUiRegistry fakeUiRegistry,
    ProtocolManager protocolManager,
    PlatformScheduler platformScheduler,
    ConfigKeeper<MainSection> config,
    Logger logger
  ) {
    this.fakeUiRegistry = fakeUiRegistry;
    this.protocolManager = protocolManager;
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

    var userInterface = new HeadCatalogUi(protocolManager, platformScheduler, logger, filteredHeads, player, config);
    fakeUiRegistry.registerFakeUi(userInterface);
    userInterface.openInventory();

    return HeadCatalogOpenResult.ofCount(filteredHeads.size());
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
}
