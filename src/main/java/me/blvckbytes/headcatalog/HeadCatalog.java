package me.blvckbytes.headcatalog;

import me.blvckbytes.autowirer.AutoWirer;
import me.blvckbytes.bbreflect.CommandRegisterer;
import me.blvckbytes.bbreflect.IReflectionHelper;
import me.blvckbytes.bbreflect.ReflectionHelper;
import me.blvckbytes.bbreflect.packets.PacketInterceptorRegistry;
import me.blvckbytes.bbreflect.packets.communicator.CustomPayloadCommunicator;
import me.blvckbytes.bbreflect.packets.communicator.FakeSlotCommunicator;
import me.blvckbytes.bbreflect.packets.communicator.ItemNameCommunicator;
import me.blvckbytes.bbreflect.packets.communicator.WindowOpenCommunicator;
import me.blvckbytes.bbreflect.version.ServerVersion;
import me.blvckbytes.bukkitboilerplate.InventoryUtil;
import me.blvckbytes.bukkitboilerplate.PluginFileHandler;
import me.blvckbytes.bukkitevaluable.ConfigManager;
import me.blvckbytes.bukkitevaluable.IConfigManager;
import me.blvckbytes.bukkitevaluable.IConfigPathsProvider;
import me.blvckbytes.bukkitevaluable.section.PermissionsSection;
import me.blvckbytes.bukkitinventoryui.InventoryRegistry;
import me.blvckbytes.headcatalog.config.*;
import me.blvckbytes.headcatalog.economy.EconomyAdapter;
import me.blvckbytes.headcatalog.apis.HeadApisManager;
import me.blvckbytes.headcatalog.command.HeadCatalogCommand;
import me.blvckbytes.headcatalog.persistence.JsonFilePersistence;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Level;
import java.util.logging.Logger;

public class HeadCatalog extends JavaPlugin implements IConfigPathsProvider {

  private AutoWirer wirer;
  private Logger logger;

  @Override
  public void onEnable() {
    long beginStamp = System.nanoTime();

    logger = getLogger();
    logger.setLevel(Level.INFO);

    wirer = new AutoWirer()
      .addExistingSingleton(this)
      .addExistingSingleton(logger)
      .addSingleton(IReflectionHelper.class, dependencies -> {
        ServerVersion version = ServerVersion.current();
        IReflectionHelper helper = new ReflectionHelper(version);
        logger.log(Level.INFO, "Detected server version " + helper.getVersion());
        return helper;
      }, null)
      .addSingleton(EconomyAdapter.class)
      .addSingleton(InventoryUtil.class)
      .addSingleton(HeadCatalogCommand.class)
      .addSingleton(ConfigManager.class)
      .addSingleton(ConfigManager.class)
      .addSingleton(CommandRegisterer.class)
      .addSingleton(HeadApisManager.class)
      .addSingleton(PluginFileHandler.class)
      .addSingleton(JsonFilePersistence.class)
      .addSingleton(InventoryRegistry.class)
      .addSingleton(FakeSlotCommunicator.class)
      .addSingleton(PacketInterceptorRegistry.class)
      .addSingleton(ItemNameCommunicator.class)
      .addSingleton(WindowOpenCommunicator.class)
      .addSingleton(CustomPayloadCommunicator.class)
      .addSingleton(MessagesSection.class, dependencies -> {
        IConfigManager configManager = (IConfigManager) dependencies[0];
        return configManager.getMapper("config.yml").mapSection("messages", MessagesSection.class);
      }, null, IConfigManager.class)
      .addSingleton(HeadCatalogCommandSection.class, dependencies -> {
        IConfigManager configManager = (IConfigManager) dependencies[0];
        return configManager.getMapper("config.yml").mapSection("command", HeadCatalogCommandSection.class);
      }, null, IConfigManager.class)
      .addSingleton(SourceSection.class, dependencies -> {
        IConfigManager configManager = (IConfigManager) dependencies[0];
        return configManager.getMapper("config.yml").mapSection("source", SourceSection.class);
      }, null, IConfigManager.class)
      .addSingleton(HeadCatalogUISection.class, dependencies -> {
        IConfigManager configManager = (IConfigManager) dependencies[0];
        return configManager.getMapper("config.yml").mapSection("headCatalogUI", HeadCatalogUISection.class);
      }, null, IConfigManager.class)
      .addSingleton(PermissionsSection.class, dependencies -> {
        IConfigManager configManager = (IConfigManager) dependencies[0];
        return configManager.getMapper("config.yml").mapSection("permissions", PermissionsSection.class);
      }, null, IConfigManager.class)
      .addInstantiationListener(Listener.class, (listener, dependencies) -> {
        Bukkit.getPluginManager().registerEvents(listener, this);
      })
      .addInstantiationListener(Command.class, (command, dependencies) -> {
        ((CommandRegisterer) dependencies[0]).register(command);
      }, CommandRegisterer.class)
      .onException(e -> {
        this.logger.log(Level.SEVERE, e, () -> "An error occurred while setting up the plugin:");
        Bukkit.getServer().getPluginManager().disablePlugin(this);
      })
      .wire(wirer -> {
        this.logger.log(Level.INFO, "Successfully loaded " + wirer.getInstancesCount() + " classes (" + ((System.nanoTime() - beginStamp) / 1000 / 1000) + "ms)");
      });
  }

  @Override
  public void onDisable() {
    try {
      if (wirer != null)
        wirer.cleanup();
    } catch (Exception e) {
      this.logger.log(Level.SEVERE, e, () -> "An error occurred while cleaning up");
    }
  }

  @Override
  public String[] getConfigPaths() {
    return new String[] { "config.yml" };
  }
}
