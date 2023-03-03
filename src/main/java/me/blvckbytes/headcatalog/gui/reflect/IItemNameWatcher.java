package me.blvckbytes.headcatalog.gui.reflect;

public interface IItemNameWatcher {

  void registerReceiver(FItemNameReceiver receiver);

  void unregisterReceiver(FItemNameReceiver receiver);

}
