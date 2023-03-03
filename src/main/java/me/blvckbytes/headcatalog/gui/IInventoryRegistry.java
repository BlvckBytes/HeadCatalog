package me.blvckbytes.headcatalog.gui;

import me.blvckbytes.bbreflect.packets.communicator.IFakeSlotCommunicator;
import me.blvckbytes.utilitytypes.EIterationDecision;

import java.util.function.Function;

public interface IInventoryRegistry {

  void register(AInventoryUI<?, ?> ui);

  void unregister(AInventoryUI<?, ?> ui);

  boolean isRegistered(AInventoryUI<?, ?> ui);

  IFakeSlotCommunicator getFakeSlotCommunicator();

  <T extends AInventoryUI<?, ?>> void forEachRegisteredOfType(Class<T> type, Function<T, EIterationDecision> consumer);

}
