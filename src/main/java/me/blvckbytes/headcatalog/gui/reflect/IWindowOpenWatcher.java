package me.blvckbytes.headcatalog.gui.reflect;

import org.bukkit.entity.Player;

public interface IWindowOpenWatcher {

  /**
   * Currently open window ID of the top inventory
   * @return Window ID if a window is open, -1 otherwise
   */
  int getCurrentTopInventoryWindowId(Player player);

}
