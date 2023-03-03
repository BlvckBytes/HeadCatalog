package me.blvckbytes.headcatalog.command;

import me.blvckbytes.bukkitboilerplate.PlayerCommand;
import me.blvckbytes.headcatalog.config.HeadCatalogCommandSection;
import me.blvckbytes.headcatalog.gui.*;
import me.blvckbytes.headcatalog.heads.Head;
import me.blvckbytes.headcatalog.heads.IHeadManager;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class HeadCatalogCommand extends PlayerCommand {

  private final InventoryRegistry inventoryRegistry;

  private List<UISlot> headSlots;

  public HeadCatalogCommand(
    HeadCatalogCommandSection commandSection,
    IHeadManager headManager,
    InventoryRegistry inventoryRegistry
  ) {
    super(commandSection);
    this.inventoryRegistry = inventoryRegistry;

    headManager.registerUpdateCallback(this::mapHeadsToUISlots);
  }

  private void mapHeadsToUISlots(Collection<Head> heads) {
    this.headSlots = new ArrayList<>();

    for (Head head : heads) {
      headSlots.add(new UISlot(() -> head.item, e -> {
        e.ui.getViewer().getInventory().addItem(head.item);
        return null;
      }));
    }
  }

  @Override
  protected void onPlayerExecution(Player player, String s, String[] strings) {
//    if (this.headSlots == null) {
//      player.sendMessage("§cHeads aren't ready yet");
//      return;
//    }
//
//    SingleChoiceUI singleChoiceUI = inventoryRegistry.createInventory(SingleChoiceUI.class, new SingleChoiceParameter(player));
//    singleChoiceUI.show();
//    singleChoiceUI.setPageableSlots(this.headSlots);

    AnvilSearchUI ui = inventoryRegistry.createInventory(AnvilSearchUI.class, new AnvilSearchParameter(player, HeadModelSearchFilter.HEAD_EVERYWHERE));
//    SingleChoiceUI ui = inventoryRegistry.createInventory(SingleChoiceUI.class, new SingleChoiceParameter(player));
    ui.show();
  }
}
