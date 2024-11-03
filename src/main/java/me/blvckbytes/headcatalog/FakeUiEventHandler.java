package me.blvckbytes.headcatalog;

import org.bukkit.event.inventory.ClickType;

public interface FakeUiEventHandler {

  /**
   * @return True if the clicked-at slot has to be redrawn internally, for it not to vanish; if the
   * handler takes action by redrawing or changing a fake-slot on their own, this has to be set to false
   */
  boolean handleClick(FakeUiSession session, int slot, boolean isTop, ClickType clickType);

  void handleAnvilText(FakeUiSession session, String text);

}
