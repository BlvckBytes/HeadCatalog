package me.blvckbytes.headcatalog.command;

import me.blvckbytes.bukkitboilerplate.PlayerCommand;
import me.blvckbytes.headcatalog.config.HeadCatalogCommandSection;
import me.blvckbytes.headcatalog.gui.InventoryRegistry;
import me.blvckbytes.headcatalog.gui.SingleChoiceParameter;
import me.blvckbytes.headcatalog.gui.SingleChoiceUI;
import me.blvckbytes.headcatalog.heads.IHeadManager;
import org.bukkit.entity.Player;

public class HeadCatalogCommand extends PlayerCommand {

  private final IHeadManager headManager;
  private final InventoryRegistry inventoryRegistry;

  public HeadCatalogCommand(
    HeadCatalogCommandSection commandSection,
    IHeadManager headManager,
    InventoryRegistry inventoryRegistry
  ) {
    super(commandSection);
    this.headManager = headManager;
    this.inventoryRegistry = inventoryRegistry;
  }

  @Override
  protected void onPlayerExecution(Player player, String s, String[] strings) {
    inventoryRegistry.createInventory(SingleChoiceUI.class, new SingleChoiceParameter(player)).show();
//    Collection<Head> heads = this.headManager.getHeadsUnmodifiable();
//
//    if (heads == null) {
//      player.sendMessage("§cHeads aren't ready yet");
//      return;
//    }
//
//    int i = 0;
//    for (Head head : heads) {
//      player.getInventory().addItem(head.item);
//      if (++i == 10)
//        break;
//    }
//
//    player.sendMessage("§aHeads handed out");
  }
}
