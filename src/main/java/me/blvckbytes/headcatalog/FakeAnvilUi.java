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

  protected final PlatformScheduler platformScheduler;

  protected String anvilText;
  private @Nullable WrappedTask previousInputDebounceTask;

  public FakeAnvilUi(
    ProtocolManager protocolManager,
    Logger logger,
    PlatformScheduler platformScheduler,
    Player player
  ) {
    super(protocolManager, logger, player, UiType.ANVIL);

    this.platformScheduler = platformScheduler;
    this.anvilText = " ";
  }

  protected abstract ItemStack buildAnvilInputItem();
  protected abstract ItemStack buildAnvilSecondItem();
  protected abstract ItemStack buildAnvilResultItem();
  protected abstract void onDebouncedAnvilText();

  public void setAnvilText(String text) {
    // Once the anvil's input-box changes, the client un-renders this slot.
    // While it'll be set back after the debounced text re-renders the UI, this operation
    // is cheap enough to make it look better by having the item be put back immediately.
    setTopFakeItem(buildAnvilResultItem(), ANVIL_RESULT_ITEM_SLOT);
    sendTopFakeSlot(ANVIL_RESULT_ITEM_SLOT);

    if (text.strip().equals(this.anvilText.strip()))
      return;

    this.anvilText = text;

    if (previousInputDebounceTask != null)
      previousInputDebounceTask.cancel();

    previousInputDebounceTask = platformScheduler.runLaterAsync(this::onDebouncedAnvilText, INPUT_DEBOUNCE_T);
  }

  @Override
  protected void afterWindowOpenPacketSent() {
    drawAnvilItems();
  }

  private void drawAnvilItems() {
    setTopFakeItem(buildAnvilInputItem(), ANVIL_INPUT_ITEM_SLOT);
    setTopFakeItem(buildAnvilSecondItem(), ANVIL_SECOND_ITEM_SLOT);
    setTopFakeItem(buildAnvilResultItem(), ANVIL_RESULT_ITEM_SLOT);
    sendTopFakeContents();
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
