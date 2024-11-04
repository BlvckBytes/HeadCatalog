package me.blvckbytes.headcatalog;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class AsyncHeadRegistryLoadEvent extends Event {

  private static final HandlerList handlers = new HandlerList();

  public final HeadRegistry registry;

  public AsyncHeadRegistryLoadEvent(HeadRegistry registry) {
    super(true);

    this.registry = registry;
  }

  @Override
  public HandlerList getHandlers() {
    return handlers;
  }

  public static HandlerList getHandlerList() {
    return handlers;
  }
}
