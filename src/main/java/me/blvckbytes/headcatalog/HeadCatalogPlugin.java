package me.blvckbytes.headcatalog;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.tcoded.folialib.FoliaLib;
import com.tcoded.folialib.impl.PlatformScheduler;
import me.blvckbytes.bukkitevaluable.ConfigKeeper;
import me.blvckbytes.bukkitevaluable.ConfigManager;
import me.blvckbytes.headcatalog.config.MainSection;
import me.blvckbytes.headcatalog.index.HeadIndex;
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
import java.util.logging.Logger;

public class HeadCatalogPlugin extends JavaPlugin implements CommandExecutor, Listener {

  private static final long HEAD_FETCH_PERIOD_S = 60 * 60 * 12;

  private ProtocolManager protocolManager;
  private FakeUiRegistry fakeUiRegistry;
  private PlatformScheduler platformScheduler;
  private Logger logger;
  private HeadIndex headIndex;
  private ConfigKeeper<MainSection> config;

  @Override
  public void onEnable() {
    logger = getLogger();

    try {
      this.protocolManager = ProtocolLibrary.getProtocolManager();

      var configManager = new ConfigManager(this, "config");
      config = new ConfigKeeper<>(configManager, "config.yml", MainSection.class);

      var foliaLib = new FoliaLib(this);
      platformScheduler = foliaLib.getScheduler();

      var headRegistry = new HeadRegistry(platformScheduler, logger);
      platformScheduler.runTimer(task -> headRegistry.load(), 0, HEAD_FETCH_PERIOD_S * 20);

      this.fakeUiRegistry = new FakeUiRegistry(platformScheduler, this);
      Bukkit.getServer().getPluginManager().registerEvents(fakeUiRegistry, this);
      protocolManager.addPacketListener(fakeUiRegistry);

      headIndex = new HeadIndex(headRegistry, logger);
      Bukkit.getServer().getPluginManager().registerEvents(headIndex, this);

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

    var userInterface = new HeadCatalogUi(protocolManager, platformScheduler, logger, headIndex, player, config);
    fakeUiRegistry.registerFakeUi(userInterface);
    userInterface.openInventory();

    player.sendMessage("§aOpening catalog");
    return true;
  }
}