package me.blvckbytes.headcatalog;

import me.blvckbytes.headcatalog.gui.EAnimationType;
import me.blvckbytes.headcatalog.gui.IReadonlyInventory;
import me.blvckbytes.headcatalog.gui.InventoryAnimator;
import me.blvckbytes.utilitytypes.Tuple;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;

import static org.junit.jupiter.api.Assertions.*;

public class InventoryAnimatorTests {

  // NOTE: Oh my, I'm really not feeling this right now... postponing it a bit.

  @Test
  public void shouldAnimateSlidingRight() {
    testAnimation(EAnimationType.SLIDE_RIGHT, this::createSlideRightExpectations);
  }

  @Test
  public void shouldAnimateSlidingLeft() {
    testAnimation(EAnimationType.SLIDE_LEFT, this::createSlideLeftExpectations);
  }

  @Test
  public void shouldAnimateSlidingUp() {
    testAnimation(EAnimationType.SLIDE_UP, this::createSlideUpExpectations);
  }

  @Test
  public void shouldAnimateSlidingDown() {
    testAnimation(EAnimationType.SLIDE_DOWN, this::createSlideDownExpectations);
  }

  private void testAnimation(EAnimationType animationType, BiFunction<IReadonlyInventory, IReadonlyInventory, Tuple<Integer, Map<Integer, Map<Integer, ItemStack>>>> expectationsFunction) {
    VirtualInventory inventoryA = new VirtualInventory(), inventoryB = new VirtualInventory();
    Tuple<Integer, Map<Integer, Map<Integer, ItemStack>>> expectations = expectationsFunction.apply(inventoryA, inventoryB);

    Map<Integer, ItemStack> setterBuffer = new HashMap<>();

    InventoryAnimator animator = new InventoryAnimator(setterBuffer::put);

    animator.saveLayout(inventoryA);
    animator.animateTo(animationType, generateFullSlotMask(inventoryB), inventoryB);

    int tickIndex = 0;
    while (animator.tick()) {
      Map<Integer, ItemStack> expectedSetCalls = expectations.b.get(tickIndex);

      assertNotNull(expectedSetCalls, "Didn't expect tick " + tickIndex);

      int size = expectedSetCalls.size();

      assertEquals(size, setterBuffer.size(), "Number of setter calls for tick " + tickIndex + " mismatched");

      for (Map.Entry<Integer, ItemStack> expectedSetCall : expectedSetCalls.entrySet()) {
        int slot = expectedSetCall.getKey();
        ItemStack setItem = setterBuffer.get(slot);
        ItemStack expectedSetItem = expectedSetCall.getValue();

        assertNotNull(setItem, "Missing set call to " + slot + " for tick " + tickIndex);

        assertSame(setItem, expectedSetCall.getValue(), () -> {
          Integer setItemSlot;
          char setItemInv;

          if ((setItemSlot = inventoryA.getSlotOf(setItem)) != null) {
            setItemInv = 'a';
          } else if ((setItemSlot = inventoryB.getSlotOf(setItem)) != null) {
            setItemInv = 'b';
          } else
            throw new IllegalStateException("Could not locate slot of setItem");

          Integer expectedSetItemSlot;
          char expectedSetItemInv;

          if ((expectedSetItemSlot = inventoryA.getSlotOf(expectedSetItem)) != null) {
            expectedSetItemInv = 'a';
          } else if ((expectedSetItemSlot = inventoryB.getSlotOf(expectedSetItem)) != null) {
            expectedSetItemInv = 'b';
          } else
            throw new IllegalStateException("Could not locate slot of expectedSetItem");

          return "Expected item (" + expectedSetItemSlot + ", " + expectedSetItemInv + ") but got (" + setItemSlot + ", " + setItemInv + ")";
        });
      }

      ++tickIndex;
      setterBuffer.clear();
    }

    assertEquals(expectations.a, tickIndex, "Didn't tick as often as expected");
  }

  //=========================================================================//
  //                             Expectation Data                            //
  //=========================================================================//

  private Tuple<Integer, Map<Integer, Map<Integer, ItemStack>>> createSlideRightExpectations(IReadonlyInventory a, IReadonlyInventory b) {
    Map<Integer, Map<Integer, ItemStack>> expectedSetCallsByTick = new HashMap<>();

    // First frame - all slots from A
    Map<Integer, ItemStack> expectedSetCallsT0 = new HashMap<>();
    for (int i = 0; i < a.getSize(); i++)
      expectedSetCallsT0.put(i, a.getItem(i));
    expectedSetCallsByTick.put(0, expectedSetCallsT0);

    int numberOfRows = a.getSize() / 9;

    // Iterate columns, left to right
    for (int i = 0; i < 9; i++) {
    }

    // Last frame - all slots from B
    Map<Integer, ItemStack> expectedSetCallsT8 = new HashMap<>();
    for (int i = 0; i < b.getSize(); i++)
      expectedSetCallsT8.put(i, b.getItem(i));
    expectedSetCallsByTick.put(8, expectedSetCallsT8);

    return new Tuple<>(9, expectedSetCallsByTick);
  }

  private Tuple<Integer, Map<Integer, Map<Integer, ItemStack>>> createSlideLeftExpectations(IReadonlyInventory a, IReadonlyInventory b) {
    Map<Integer, Map<Integer, ItemStack>> expectedSetCallsByTick = new HashMap<>();

    // TODO: Populate map...

    return new Tuple<>(9, expectedSetCallsByTick);
  }

  private Tuple<Integer, Map<Integer, Map<Integer, ItemStack>>> createSlideUpExpectations(IReadonlyInventory a, IReadonlyInventory b) {
    Map<Integer, Map<Integer, ItemStack>> expectedSetCallsByTick = new HashMap<>();

    // TODO: Populate map...

    return new Tuple<>(9, expectedSetCallsByTick);
  }

  private Tuple<Integer, Map<Integer, Map<Integer, ItemStack>>> createSlideDownExpectations(IReadonlyInventory a, IReadonlyInventory b) {
    Map<Integer, Map<Integer, ItemStack>> expectedSetCallsByTick = new HashMap<>();

    // TODO: Populate map...

    return new Tuple<>(9, expectedSetCallsByTick);
  }

  //=========================================================================//
  //                                 Mocks                                   //
  //=========================================================================//

  private static class VirtualInventory implements IReadonlyInventory {

    private static final int NUMBER_OF_SLOTS = 5 * 9;

    private final ItemStack[] slots;

    public VirtualInventory() {
      this.slots = new ItemStack[NUMBER_OF_SLOTS];
      for (int i = 0; i < NUMBER_OF_SLOTS; i++)
        slots[i] = new ItemStack(Material.AIR);
    }

    @Override
    public int getSize() {
      return NUMBER_OF_SLOTS;
    }

    public @Nullable Integer getSlotOf(@Nullable ItemStack item) {
      for (int i = 0; i < NUMBER_OF_SLOTS; i++) {
        if (slots[i] == item)
          return i;
      }
      return null;
    }

    @Override
    public ItemStack getItem(int slot) {
      return slots[slot];
    }
  }

  private List<Integer> generateFullSlotMask(IReadonlyInventory inventory) {
    List<Integer> result = new ArrayList<>();

    int invSize = inventory.getSize();
    for (int i = 0; i < invSize; i++)
      result.add(i);

    return result;
  }
}
