package me.blvckbytes.headcatalog.economy;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

public interface IEconomyAdapter {

  /**
   * Returns whether an economy system could be located on the server
   */
  boolean isAvailable();

  /**
   * Get the current balance of the player's account
   * @param player Player to check the account of
   */
  double getBalance(Player player);

  /**
   * Tries to withdraw a given amount from the player's account
   * @param player Player to withdraw from
   * @param amount Amount to withdraw
   * @return null on success, the error-message if the withdrawal failed
   */
  @Nullable String withdrawAmount(Player player, double amount);

}
