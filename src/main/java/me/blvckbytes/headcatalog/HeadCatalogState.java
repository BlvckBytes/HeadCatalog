package me.blvckbytes.headcatalog;

import com.tcoded.folialib.impl.PlatformScheduler;
import com.tcoded.folialib.wrapper.task.WrappedTask;
import me.blvckbytes.bukkitevaluable.ConfigKeeper;
import me.blvckbytes.gpeee.GPEEE;
import me.blvckbytes.gpeee.interpreter.EvaluationEnvironmentBuilder;
import me.blvckbytes.gpeee.interpreter.IEvaluationEnvironment;
import me.blvckbytes.headcatalog.config.MainSection;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class HeadCatalogState {

  // Debouncing input keystrokes, as to not needlessly filter and render when typing quickly
  private static final long INPUT_DEBOUNCE_T = 2;

  // Debouncing rendering the input-item into the anvil, as to avoid feedback-loops
  private static final long INPUT_ITEM_DEBOUNCE_T = 20;

  public static final int PREVIOUS_PAGE_SLOT = 29;
  public static final int NEXT_PAGE_SLOT = 33;
  private static final int PAGE_SIZE = 9 * 3;

  private final FakeUiSession userInterface;
  private final ConfigKeeper<MainSection> config;
  private final List<CatalogHead> heads;

  private final PlatformScheduler platformScheduler;
  private final AsyncTaskQueue taskQueue;

  public final IEvaluationEnvironment paginationEnvironment;
  public final IEvaluationEnvironment searchEnvironment;

  private int currentPage;
  private int numberOfPages;
  private String searchQuery;
  private boolean isSearchQueryValid;
  private @Nullable WrappedTask previousInputDebounceTask;
  private @Nullable WrappedTask previousInputItemDebounceTask;

  private ItemStack fillerItem;

  public HeadCatalogState(
    FakeUiSession userInterface,
    PlatformScheduler platformScheduler,
    ConfigKeeper<MainSection> config,
    List<CatalogHead> heads
  ) {
    this.userInterface = userInterface;
    this.config = config;
    this.heads = heads;
    this.searchQuery = "";

    this.platformScheduler = platformScheduler;
    this.taskQueue = new AsyncTaskQueue(platformScheduler);

    this.numberOfPages = (heads.size() + (PAGE_SIZE - 1)) / PAGE_SIZE;

    this.onConfigUpdate();

    this.paginationEnvironment = new EvaluationEnvironmentBuilder()
      .withLiveVariable("current_page", () -> this.currentPage + 1)
      .withLiveVariable("number_pages", () -> this.numberOfPages)
      .build();

    this.searchEnvironment = new EvaluationEnvironmentBuilder()
      .withLiveVariable("search_query", () -> {
        // Otherwise, the anvil-input will show the client-name of the item
        if (this.searchQuery.isEmpty())
          return " ";

        return this.searchQuery;
      })
      .withLiveVariable("is_valid", () -> this.isSearchQueryValid)
      .build(paginationEnvironment);
  }

  public void onConfigUpdate() {
    this.fillerItem = config.rootSection.catalogDisplay.items.filler.build(GPEEE.EMPTY_ENVIRONMENT);
  }

  public void setSearchText(String text) {
    this.searchQuery = text.strip();
    this.isSearchQueryValid = !this.searchQuery.isBlank();

    if (previousInputDebounceTask != null)
      previousInputDebounceTask.cancel();

    previousInputDebounceTask = platformScheduler.runLater(() -> {
      taskQueue.enqueue(() -> {
        // TODO: Filter based on search-query
        this.drawAnvilSecondAndOutputItems(true);
      });
    }, INPUT_DEBOUNCE_T);

    if (previousInputItemDebounceTask != null)
      previousInputItemDebounceTask.cancel();

    previousInputItemDebounceTask = platformScheduler.runLaterAsync(this::drawAnvilInputItemAndOthers, INPUT_ITEM_DEBOUNCE_T);
  }

  public void nextPage() {
    taskQueue.enqueue(() -> {
      if (this.currentPage >= numberOfPages - 1)
        return;

      ++this.currentPage;
      userInterface.sendTitle();
      this.drawPaginationItems();
      this.drawAnvilInputItemAndOthers();
    });
  }

  public void lastPage() {
    taskQueue.enqueue(() -> {
      if (this.currentPage >= numberOfPages - 1)
        return;

      this.currentPage = this.numberOfPages - 1;
      userInterface.sendTitle();
      this.drawPaginationItems();
      this.drawAnvilInputItemAndOthers();
    });
  }

  public void previousPage() {
    taskQueue.enqueue(() -> {
      if (currentPage == 0)
        return;

      --this.currentPage;
      userInterface.sendTitle();
      this.drawPaginationItems();
      this.drawAnvilInputItemAndOthers();
    });
  }

  public void firstPage() {
    taskQueue.enqueue(() -> {
      if (currentPage == 0)
        return;

      this.currentPage = 0;
      userInterface.sendTitle();
      this.drawPaginationItems();
      this.drawAnvilInputItemAndOthers();
    });
  }

  public void onShow() {
    taskQueue.enqueue(() -> {
      this.drawAnvilInputItemAndOthers();
      this.drawPaginationItems();
    });
  }

  private void drawAnvilInputItemAndOthers() {
    userInterface.setTopFakeItem(
      config.rootSection.catalogDisplay.items.anvilInputItem.build(searchEnvironment),
      0
    );

    // Draw all, as a single slot-update will cause the other slots to go empty again
    drawAnvilSecondAndOutputItems(false);
    userInterface.sendTopFakeContents();
  }

  private void drawAnvilSecondAndOutputItems(boolean draw) {
    // Only draw 1 and 2, as to not render 0, which may still be debouncing

    userInterface.setTopFakeItem(fillerItem, 1);

    if (draw)
      userInterface.sendTopFakeSlot(1);

    var resultItem = config.rootSection.catalogDisplay.items.anvilResultItem.build(searchEnvironment);

    userInterface.setTopFakeItem(resultItem, 2);

    if (draw)
      userInterface.sendTopFakeSlot(2);
  }

  private void drawPaginationItems() {
    int bottomSlot;

    for (bottomSlot = 0; bottomSlot < PAGE_SIZE; ++bottomSlot) {
      var headsIndex = currentPage * PAGE_SIZE + bottomSlot;

      if (headsIndex >= heads.size()) {
        userInterface.setBottomFakeItem(null, bottomSlot);
        continue;
      }

      var head = heads.get(headsIndex);

      var headItem = config.rootSection.catalogDisplay.items.representative.build(
        head.getHeadEnvironmentBuilder()
          .build()
      );

      userInterface.setBottomFakeItem(headItem, bottomSlot);
    }

    for (var i = 0; i < 9; ++i)
      userInterface.setBottomFakeItem(fillerItem, bottomSlot++);

    userInterface.setBottomFakeItem(
      config.rootSection.catalogDisplay.items.previousPage.build(paginationEnvironment),
      PREVIOUS_PAGE_SLOT
    );

    userInterface.setBottomFakeItem(
      config.rootSection.catalogDisplay.items.nextPage.build(paginationEnvironment),
      NEXT_PAGE_SLOT
    );

    userInterface.sendBottomFakeContents();
  }
}
