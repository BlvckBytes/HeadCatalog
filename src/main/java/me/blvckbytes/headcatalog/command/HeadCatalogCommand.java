package me.blvckbytes.headcatalog;

import me.blvckbytes.bukkitboilerplate.PlayerCommand;
import me.blvckbytes.headcatalog.config.HeadCatalogCommandSection;
import org.bukkit.entity.Player;

public class HeadCatalogCommand extends PlayerCommand {

  public HeadCatalogCommand(HeadCatalogCommandSection commandSection) {
    super(commandSection);
  }

  @Override
  protected void onPlayerExecution(Player player, String s, String[] strings) {
    player.sendMessage("§aHello, world! :)");
  }
}
