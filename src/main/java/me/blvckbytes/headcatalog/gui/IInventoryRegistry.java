package me.blvckbytes.headcatalog.gui;

import me.blvckbytes.bbreflect.packets.communicator.IFakeSlotCommunicator;

public interface IInventoryRegistry {

  void register(AInventoryUI<?, ?> ui);

  void unregister(AInventoryUI<?, ?> ui);

  IFakeSlotCommunicator getFakeSlotCommunicator();

}
