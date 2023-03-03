package me.blvckbytes.headcatalog.gui;

import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.BiConsumer;

public class InventoryAnimator {

  private final @Nullable ItemStack fillerItem;
  private final BiConsumer<Integer, ItemStack> setter;

  private ItemStack @Nullable [] fromLayout, toLayout;
  private @Nullable EAnimationType animationType;
  private List<Integer> mask;
  private int numberOfFrames;
  private int numberOfRows;
  private int currentFrame;

  public InventoryAnimator(@Nullable ItemStack fillerItem, BiConsumer<Integer, ItemStack> setter) {
    this.setter = setter;
    this.fillerItem = fillerItem;
  }

  public void animateTo(EAnimationType animationType, List<Integer> mask, IReadonlyInventory inventory) {
    int inventorySize = inventory.getSize();

    if (inventorySize % 9 != 0)
      return;

    if (this.toLayout == null || this.toLayout.length >= inventory.getSize())
      this.toLayout = new ItemStack[inventorySize];

    for (int i = 0; i < inventorySize; i++)
      this.toLayout[i] = inventory.getItem(i);

    this.animationType = animationType;
    this.mask = mask;
    this.numberOfRows = inventorySize / 9;
    this.numberOfFrames = getNumberOfFrames(animationType);
    this.currentFrame = 0;

    drawCurrentFrame();
  }

  public void fastForward() {
    this.currentFrame = this.numberOfFrames - 1;
    drawCurrentFrame();
    this.animationType = null;
  }

  public void saveLayout(IReadonlyInventory inventory) {
    int inventorySize = inventory.getSize();
    if (this.fromLayout == null || this.fromLayout.length >= inventory.getSize())
      this.fromLayout = new ItemStack[inventorySize];

    for (int i = 0; i < inventorySize; i++)
      this.fromLayout[i] = inventory.getItem(i);
  }

  public boolean tick() {
    if (this.animationType == null)
      return false;

    if (++currentFrame < numberOfFrames) {
      drawCurrentFrame();
      return true;
    }

    this.animationType = null;
    return false;
  }

  private void drawCurrentFrame() {
    if (this.animationType == null || this.fromLayout == null || this.toLayout == null)
      return;

    switch (this.animationType) {
      // Drawing columns
      case SLIDE_LEFT:
      case SLIDE_RIGHT:
      {
        for (int drawCol = 0; drawCol < 9; drawCol++) {
          ItemStack[] origin;
          int readCol;

          if (this.animationType == EAnimationType.SLIDE_LEFT) {
            if (drawCol < (numberOfFrames - currentFrame - 1)) {
              origin = fromLayout;
              readCol = drawCol + currentFrame + 1;
            } else {
              origin = toLayout;
              readCol = drawCol - (8 - currentFrame);
            }
          }

          else {
            if (drawCol > currentFrame) {
              origin = fromLayout;
              readCol = drawCol - currentFrame - 1;
            }
            else {
              origin = toLayout;
              readCol = 8 - currentFrame + drawCol;
            }
          }

          for (int i = 0; i < numberOfRows * 9; i += 9) {
            int destinationSlot = drawCol + i;
            int sourceSlot = readCol + i;

            if (mask == null || (mask.contains(destinationSlot) && mask.contains(sourceSlot)))
              this.setter.accept(destinationSlot, getItem(origin, sourceSlot));
          }
        }
        break;
      }

      // Drawing rows
      case SLIDE_DOWN:
      case SLIDE_UP:
      {
        for (int drawRow = 0; drawRow < numberOfRows; drawRow++) {
          ItemStack[] origin;
          int readRow;

          if (this.animationType == EAnimationType.SLIDE_DOWN) {
            if (drawRow > currentFrame) {
              origin = fromLayout;
              readRow = drawRow - (currentFrame + 1);
            } else {
              origin = toLayout;
              readRow = drawRow + (numberOfRows - currentFrame - 1);
            }
          }

          else {
            if (drawRow < (numberOfFrames - currentFrame - 1)) {
              origin = fromLayout;
              readRow = drawRow + (currentFrame + 1);
            } else {
              origin = toLayout;
              readRow = drawRow - (numberOfRows - currentFrame - 1);
            }
          }

          for (int i = 0; i < 9; i++) {
            int destinationSlot = drawRow * 9 + i;
            int sourceSlot = readRow * 9 + i;

            if (mask == null || (mask.contains(destinationSlot) && mask.contains(sourceSlot)))
              this.setter.accept(destinationSlot, getItem(origin, sourceSlot));
          }
        }
        break;
      }
    }
  }

  private ItemStack getItem(ItemStack[] contents, int slot) {
    if (slot >= contents.length)
      return this.fillerItem;
    return contents[slot];
  }

  private int getNumberOfFrames(EAnimationType animationType) {
    switch (animationType) {
      // Bottom and top will both take as many frames as there are rows
      case SLIDE_UP:
      case SLIDE_DOWN:
        return this.numberOfRows;

      // Left and right take as many frames as there are horizontal slots
      case SLIDE_RIGHT:
      case SLIDE_LEFT:
        return 9;

      default:
        return 0;
    }
  }
}
