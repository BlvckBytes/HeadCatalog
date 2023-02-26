package me.blvckbytes.headcatalog;

import me.blvckbytes.autowirer.AutoWirer;
import me.blvckbytes.bbreflect.CommandRegisterer;
import me.blvckbytes.bbreflect.IReflectionHelper;
import me.blvckbytes.bbreflect.ReflectionHelperFactory;
import me.blvckbytes.bukkitboilerplate.ConsoleSenderLogger;
import me.blvckbytes.bukkitboilerplate.ELogLevel;
import me.blvckbytes.bukkitboilerplate.ILogger;
import me.blvckbytes.bukkitboilerplate.PluginFileHandler;
import me.blvckbytes.bukkitevaluable.ConfigManager;
import me.blvckbytes.bukkitevaluable.GPEEELogRedirect;
import me.blvckbytes.bukkitevaluable.IConfigManager;
import me.blvckbytes.bukkitevaluable.IConfigPathsProvider;
import me.blvckbytes.headcatalog.apis.ApisManager;
import me.blvckbytes.headcatalog.command.HeadCatalogCommand;
import me.blvckbytes.headcatalog.config.ApisSection;
import me.blvckbytes.headcatalog.config.HeadCatalogCommandSection;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

public class HeadCatalog extends JavaPlugin implements IConfigPathsProvider {

  private AutoWirer wirer;
  private ILogger logger;

  @Override
  public void onEnable() {
    long beginStamp = System.nanoTime();

    logger = new ConsoleSenderLogger(this);

    wirer = new AutoWirer()
      .addExistingSingleton(this)
      .addExistingSingleton(logger)
      .addSingleton(IReflectionHelper.class, dependencies -> {
        IReflectionHelper helper = new ReflectionHelperFactory(getClassLoader()).makeHelper();
        logger.log(ELogLevel.INFO, "Detected server version " + helper.getVersion());
        return helper;
      }, IReflectionHelper::cleanupInterception)
      .addSingleton(HeadCatalogCommand.class)
      .addSingleton(ConfigManager.class)
      .addSingleton(GPEEELogRedirect.class)
      .addSingleton(ConfigManager.class)
      .addSingleton(CommandRegisterer.class)
      .addSingleton(ApisManager.class)
      .addSingleton(PluginFileHandler.class)
      .addSingleton(HeadCatalogCommandSection.class, dependencies -> {
        IConfigManager configManager = (IConfigManager) dependencies[0];
        return configManager.getMapper("config.yml").mapSection("command", HeadCatalogCommandSection.class);
      }, null, IConfigManager.class)
      .addSingleton(ApisSection.class, dependencies -> {
        IConfigManager configManager = (IConfigManager) dependencies[0];
        return configManager.getMapper("config.yml").mapSection("apis", ApisSection.class);
      }, null, IConfigManager.class)
      .addInstantiationListener(Listener.class, (listener, dependencies) -> {
        Bukkit.getPluginManager().registerEvents(listener, this);
      })
      .addInstantiationListener(Command.class, (command, dependencies) -> {
        ((CommandRegisterer) dependencies[0]).register(command);
      }, CommandRegisterer.class)
      .onException(e -> {
        this.logger.log(ELogLevel.ERROR, "An error occurred while setting up the plugin:");
        this.logger.logError(e);
        Bukkit.getServer().getPluginManager().disablePlugin(this);
      })
      .wire(wirer -> {
        this.logger.log(ELogLevel.INFO, "Successfully loaded " + wirer.getInstancesCount() + " classes (" + ((System.nanoTime() - beginStamp) / 1000 / 1000) + "ms)");
      });
  }

  @Override
  public void onDisable() {
    try {
      if (wirer != null)
        wirer.cleanup();
    } catch (Exception e) {
      this.logger.logError(e);
    }
  }

  @Override
  public String[] getConfigPaths() {
    return new String[] { "config.yml" };
  }
}
