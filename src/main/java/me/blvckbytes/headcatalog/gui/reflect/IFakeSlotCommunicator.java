package me.blvckbytes.headcatalog.gui.reflect;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public interface IFakeSlotCommunicator {

  void setFakeSlot(int slotId, boolean top, ItemStack item, Player player, int delayTicks);

}
