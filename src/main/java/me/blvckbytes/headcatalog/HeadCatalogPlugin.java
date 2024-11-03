package me.blvckbytes.headcatalog;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.tcoded.folialib.FoliaLib;
import me.blvckbytes.bukkitevaluable.ConfigKeeper;
import me.blvckbytes.bukkitevaluable.ConfigManager;
import me.blvckbytes.headcatalog.config.MainSection;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.logging.Level;

public class HeadCatalogPlugin extends JavaPlugin implements CommandExecutor, Listener {

  private static final long HEAD_FETCH_PERIOD_S = 60 * 60 * 12;

  private ProtocolManager protocolManager;
  private FakeUiRegistry fakeUiRegistry;
  private CatalogHeadRegistry catalogHeadRegistry;

  @Override
  public void onEnable() {
    var logger = getLogger();

    try {
      this.protocolManager = ProtocolLibrary.getProtocolManager();

      var configManager = new ConfigManager(this, "config");
      var config = new ConfigKeeper<>(configManager, "config.yml", MainSection.class);

      var foliaLib = new FoliaLib(this);
      var platformScheduler = foliaLib.getScheduler();

      var headRegistry = new HeadRegistry(platformScheduler, logger);
      platformScheduler.runTimer(task -> headRegistry.load(), 0, HEAD_FETCH_PERIOD_S * 20);

      this.fakeUiRegistry = new FakeUiRegistry(platformScheduler, this);
      Bukkit.getServer().getPluginManager().registerEvents(fakeUiRegistry, this);
      protocolManager.addPacketListener(fakeUiRegistry);

      catalogHeadRegistry = new CatalogHeadRegistry(fakeUiRegistry, protocolManager, platformScheduler, config, logger);
      Bukkit.getServer().getPluginManager().registerEvents(catalogHeadRegistry, this);

      Objects.requireNonNull(getCommand("test")).setExecutor(this);
      getServer().getPluginManager().registerEvents(this, this);
    } catch (Exception e) {
      logger.log(Level.SEVERE, "Could not initialize plugin", e);
      Bukkit.getPluginManager().disablePlugin(this);
    }
  }

  @Override
  public void onDisable() {
    if (fakeUiRegistry != null) {
      fakeUiRegistry.onShutdown();
      protocolManager.removePacketListener(fakeUiRegistry);
    }
  }

  @Override
  public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
    if (!(sender instanceof Player player)) {
      sender.sendMessage("§cOnly players can use this command");
      return true;
    }

    var displayedHeadCount = catalogHeadRegistry.createForAndOpen(player);

    if (displayedHeadCount.emptyType() != null) {
      player.sendMessage(switch (displayedHeadCount.emptyType()) {
        case HEAD_CATALOG_EMPTY -> "§cHead-Catalog is empty!";
        case NO_ACCESS_TO_ANY_HEADS -> "§cYou don't have access to any existing heads!";
      });

      return true;
    }

    player.sendMessage("§aOpening catalog with " + displayedHeadCount.displayedHeadsCount() + " items");
    return true;
  }
}