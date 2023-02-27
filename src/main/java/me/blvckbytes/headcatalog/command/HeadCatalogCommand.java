package me.blvckbytes.headcatalog.command;

import me.blvckbytes.bukkitboilerplate.PlayerCommand;
import me.blvckbytes.headcatalog.config.HeadCatalogCommandSection;
import me.blvckbytes.headcatalog.heads.Head;
import me.blvckbytes.headcatalog.heads.IHeadManager;
import org.bukkit.entity.Player;

import java.util.Collection;

public class HeadCatalogCommand extends PlayerCommand {

  private final IHeadManager headManager;

  public HeadCatalogCommand(
    HeadCatalogCommandSection commandSection,
    IHeadManager headManager
  ) {
    super(commandSection);
    this.headManager = headManager;
  }

  @Override
  protected void onPlayerExecution(Player player, String s, String[] strings) {
    Collection<Head> heads = this.headManager.getHeadsUnmodifiable();

    if (heads == null) {
      player.sendMessage("§cHeads aren't ready yet");
      return;
    }

    int i = 0;
    for (Head head : heads) {
      player.getInventory().addItem(head.item);
      if (++i == 10)
        break;
    }

    player.sendMessage("§aHeads handed out");
  }
}
