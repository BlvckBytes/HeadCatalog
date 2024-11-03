package me.blvckbytes.headcatalog;

import com.comphenix.protocol.ProtocolManager;
import com.tcoded.folialib.impl.PlatformScheduler;
import com.tcoded.folialib.wrapper.task.WrappedTask;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.logging.Logger;

public abstract class FakeAnvilUi extends FakeUi {

  private static final int ANVIL_INPUT_ITEM_SLOT  = 0;
  private static final int ANVIL_SECOND_ITEM_SLOT = 1;
  private static final int ANVIL_RESULT_ITEM_SLOT = 2;

  // Debouncing input keystrokes, as to not needlessly filter and render when typing quickly
  private static final long INPUT_DEBOUNCE_T = 5;

  // Debouncing rendering the input-item into the anvil, as to avoid feedback-loops
  private static final long INPUT_ITEM_DEBOUNCE_T = 20;

  protected String anvilText;
  private @Nullable WrappedTask previousInputDebounceTask;
  private @Nullable WrappedTask previousInputItemDebounceTask;

  public FakeAnvilUi(
    ProtocolManager protocolManager,
    Logger logger,
    PlatformScheduler platformScheduler,
    Player player
  ) {
    super(protocolManager, platformScheduler, logger, player, UiType.ANVIL);

    this.anvilText = " ";
  }

  protected abstract ItemStack buildAnvilInputItem();
  protected abstract ItemStack buildAnvilSecondItem();
  protected abstract ItemStack buildAnvilResultItem();
  protected abstract void onDebouncedAnvilText();

  public void setAnvilText(String text) {
    if (text.equals(this.anvilText))
      return;

    this.anvilText = text;

    if (previousInputDebounceTask != null)
      previousInputDebounceTask.cancel();

    previousInputDebounceTask = platformScheduler.runLaterAsync(() -> {
      this.onDebouncedAnvilText();
      this.drawAnvilSecondAndOutputItems(true);
    }, INPUT_DEBOUNCE_T);

    if (previousInputItemDebounceTask != null)
      previousInputItemDebounceTask.cancel();

    previousInputItemDebounceTask = platformScheduler.runLaterAsync(this::drawAnvilInputItemAndOthers, INPUT_ITEM_DEBOUNCE_T);
  }

  @Override
  public void initializeUiContents() {
    drawAnvilInputItemAndOthers();
  }

  protected void drawAnvilInputItemAndOthers() {
    setTopFakeItem(buildAnvilInputItem(), ANVIL_INPUT_ITEM_SLOT);

    // Draw all, as a single slot-update will cause the other slots to go empty again
    drawAnvilSecondAndOutputItems(false);
    sendTopFakeContents();
  }

  private void drawAnvilSecondAndOutputItems(boolean draw) {
    // Only draw 1 and 2, as to not render 0, which may still be debouncing

    setTopFakeItem(buildAnvilSecondItem(), ANVIL_SECOND_ITEM_SLOT);

    if (draw)
      sendTopFakeSlot(ANVIL_SECOND_ITEM_SLOT);

    setTopFakeItem(buildAnvilResultItem(), ANVIL_RESULT_ITEM_SLOT);

    if (draw)
      sendTopFakeSlot(2);
  }

  @Override
  public void handleClick(int slot, boolean isTop, ClickType clickType) {
    if (!isTop)
      return;

    // Clicking one of the first two slots results in the third being cleared
    if (slot == ANVIL_INPUT_ITEM_SLOT || slot == ANVIL_SECOND_ITEM_SLOT)
      platformScheduler.runNextTick(task -> sendTopFakeSlot(ANVIL_RESULT_ITEM_SLOT));
  }
}
