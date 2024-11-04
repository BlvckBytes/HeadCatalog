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

  private final List<Head> catalogHeads;
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
    // TODO: Actually build the index

    this.catalogHeads.clear();
    this.catalogHeads.addAll(event.registry.heads);

    this.normalizedCategories.clear();
    this.normalizedCategories.addAll(event.registry.normalizedCategories);
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

  private List<Head> filterHeadsByPermission(Player player) {
    var result = new ArrayList<Head>();
    var permissionResultByNumericCategory = new Boolean[normalizedCategories.size()];

    for (var head : catalogHeads) {
      Boolean hasPermission;

      if ((hasPermission = permissionResultByNumericCategory[head.numericCategory]) == null) {
        hasPermission = player.hasPermission("headcatalog.category." + head.normalizedCategory);
        permissionResultByNumericCategory[head.numericCategory] = hasPermission;
      }

      if (hasPermission)
        result.add(head);
    }

    return result;
  }
}
