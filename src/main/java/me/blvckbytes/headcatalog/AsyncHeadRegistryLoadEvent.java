package me.blvckbytes.headcatalog;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import java.util.Collection;

public class AsyncHeadRegistryLoadEvent extends Event {

  private static final HandlerList handlers = new HandlerList();

  public final Collection<Head> heads;
  public final Collection<String> normalizedCategories;

  public AsyncHeadRegistryLoadEvent(Collection<Head> heads, Collection<String> normalizedCategories) {
    super(true);

    this.heads = heads;
    this.normalizedCategories = normalizedCategories;
  }

  @Override
  public HandlerList getHandlers() {
    return handlers;
  }

  public static HandlerList getHandlerList() {
    return handlers;
  }
}
