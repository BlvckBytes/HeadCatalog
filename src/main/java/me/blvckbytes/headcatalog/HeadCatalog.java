package me.blvckbytes.headcatalog;

import me.blvckbytes.autowirer.AutoWirer;
import me.blvckbytes.bbreflect.CommandRegisterer;
import me.blvckbytes.bbreflect.IReflectionHelper;
import me.blvckbytes.bbreflect.ReflectionHelperFactory;
import me.blvckbytes.bbreflect.packets.EInterceptorFeature;
import me.blvckbytes.bbreflect.packets.IInterceptorFeatureProvider;
import me.blvckbytes.bbreflect.packets.PacketInterceptorRegistry;
import me.blvckbytes.bbreflect.packets.communicator.CustomPayloadCommunicator;
import me.blvckbytes.bbreflect.packets.communicator.FakeSlotCommunicator;
import me.blvckbytes.bbreflect.packets.communicator.ItemNameCommunicator;
import me.blvckbytes.bbreflect.packets.communicator.WindowOpenCommunicator;
import me.blvckbytes.bukkitboilerplate.PluginFileHandler;
import me.blvckbytes.bukkitevaluable.ConfigManager;
import me.blvckbytes.bukkitevaluable.IConfigManager;
import me.blvckbytes.bukkitevaluable.IConfigPathsProvider;
import me.blvckbytes.bukkitinventoryui.InventoryRegistry;
import me.blvckbytes.bukkitinventoryui.anvilsearch.AnvilSearchUISection;
import me.blvckbytes.bukkitinventoryui.singlechoice.SingleChoiceUISection;
import me.blvckbytes.headcatalog.config.GuiSection;
import me.blvckbytes.headcatalog.heads.HeadManager;
import me.blvckbytes.headcatalog.apis.HeadApisManager;
import me.blvckbytes.headcatalog.command.HeadCatalogCommand;
import me.blvckbytes.headcatalog.config.SourceSection;
import me.blvckbytes.headcatalog.config.HeadCatalogCommandSection;
import me.blvckbytes.headcatalog.persistence.JsonFilePersistence;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.EnumSet;
import java.util.logging.Level;
import java.util.logging.Logger;

public class HeadCatalog extends JavaPlugin implements IConfigPathsProvider, IInterceptorFeatureProvider {

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
        IReflectionHelper helper = new ReflectionHelperFactory(logger, this).makeHelper();
        logger.log(Level.INFO, "Detected server version " + helper.getVersion());
        return helper;
      }, IReflectionHelper::cleanupInterception)
      .addSingleton(HeadCatalogCommand.class)
      .addSingleton(ConfigManager.class)
      .addSingleton(ConfigManager.class)
      .addSingleton(CommandRegisterer.class)
      .addSingleton(HeadApisManager.class)
      .addSingleton(HeadManager.class)
      .addSingleton(PluginFileHandler.class)
      .addSingleton(JsonFilePersistence.class)
      .addSingleton(InventoryRegistry.class)
      .addSingleton(FakeSlotCommunicator.class)
      .addSingleton(PacketInterceptorRegistry.class)
      .addSingleton(ItemNameCommunicator.class)
      .addSingleton(WindowOpenCommunicator.class)
      .addSingleton(CustomPayloadCommunicator.class)
      .addSingleton(HeadCatalogCommandSection.class, dependencies -> {
        IConfigManager configManager = (IConfigManager) dependencies[0];
        return configManager.getMapper("config.yml").mapSection("command", HeadCatalogCommandSection.class);
      }, null, IConfigManager.class)
      .addSingleton(GuiSection.class, dependencies -> {
        IConfigManager configManager = (IConfigManager) dependencies[0];
        return configManager.getMapper("config.yml").mapSection("gui", GuiSection.class);
      }, null, IConfigManager.class)
      .addSingleton(SourceSection.class, dependencies -> {
        IConfigManager configManager = (IConfigManager) dependencies[0];
        return configManager.getMapper("config.yml").mapSection("source", SourceSection.class);
      }, null, IConfigManager.class)
      .addSingleton(SingleChoiceUISection.class, dependencies -> {
        IConfigManager configManager = (IConfigManager) dependencies[0];
        return configManager.getMapper("config.yml").mapSection("singleChoiceUI", SingleChoiceUISection.class);
      }, null, IConfigManager.class)
      .addSingleton(AnvilSearchUISection.class, dependencies -> {
        IConfigManager configManager = (IConfigManager) dependencies[0];
        return configManager.getMapper("config.yml").mapSection("anvilSearchUI", AnvilSearchUISection.class);
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

  @Override
  public EnumSet<EInterceptorFeature> getInterceptorFeatures() {
    return EnumSet.of(EInterceptorFeature.PACKET_INTERCEPTION, EInterceptorFeature.BYTES_INTERCEPTION);
  }
}
