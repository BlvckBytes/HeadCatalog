package me.blvckbytes.headcatalog.economy;

import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.jetbrains.annotations.Nullable;

import java.util.logging.Level;
import java.util.logging.Logger;

public class EconomyAdapter implements IEconomyAdapter {

  private final @Nullable Economy economy;

  public EconomyAdapter(Logger logger) {
    this.economy = findEconomyProvider();

    if (this.economy != null)
      logger.log(Level.INFO, "Found economy provider: " + economy.getName());
    else
      logger.log(Level.INFO, "Could not find an economy provider!");
  }

  @Override
  public boolean isAvailable() {
    return this.economy != null && this.economy.isEnabled();
  }

  @Override
  public double getBalance(Player player) {
    if (!this.isAvailable())
      return 0;

    assert economy != null;
    return this.economy.getBalance(player);
  }

  @Override
  public @Nullable String withdrawAmount(Player player, double amount) {
    if (!this.isAvailable())
      return "No economy system available";

    assert economy != null;
    EconomyResponse response = this.economy.withdrawPlayer(player, amount);

    if (response.type == EconomyResponse.ResponseType.SUCCESS)
      return null;

    return response.errorMessage;
  }

  private @Nullable Economy findEconomyProvider() {
    Server server = Bukkit.getServer();

    if (server.getPluginManager().getPlugin("Vault") == null)
      return null;

    RegisteredServiceProvider<Economy> provider = server.getServicesManager().getRegistration(Economy.class);

    if (provider == null)
      return null;

    return provider.getProvider();
  }
}
