package me.blvckbytes.headcatalog;

import com.comphenix.protocol.ProtocolManager;
import com.tcoded.folialib.impl.PlatformScheduler;
import me.blvckbytes.bukkitevaluable.ConfigKeeper;
import me.blvckbytes.gpeee.GPEEE;
import me.blvckbytes.gpeee.interpreter.EvaluationEnvironmentBuilder;
import me.blvckbytes.gpeee.interpreter.IEvaluationEnvironment;
import me.blvckbytes.headcatalog.config.MainSection;
import me.blvckbytes.syllables_matcher.Syllables;
import me.blvckbytes.syllables_matcher.SyllablesMatcher;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.logging.Logger;

public class HeadCatalogUi extends FakeAnvilUi {

  public static final int PREVIOUS_PAGE_SLOT = 29;
  public static final int NEXT_PAGE_SLOT = 33;
  private static final int PAGE_SIZE = 9 * 3;

  private final IEvaluationEnvironment paginationEnvironment;
  private final IEvaluationEnvironment searchEnvironment;
  private final AsyncTaskQueue taskQueue;

  private final List<CatalogHead> unfilteredHeads;
  private final List<CatalogHead> filteredHeads;
  private final ConfigKeeper<MainSection> config;

  private int currentPage;
  private int numberOfPages;

  public HeadCatalogUi(
    ProtocolManager protocolManager,
    PlatformScheduler platformScheduler,
    Logger logger,
    List<CatalogHead> heads,
    Player player,
    ConfigKeeper<MainSection> config
  ) {
    super(protocolManager, logger, platformScheduler, player);

    this.config = config;
    this.unfilteredHeads = heads;
    this.filteredHeads = new ArrayList<>(heads);
    this.taskQueue = new AsyncTaskQueue(platformScheduler);
    this.numberOfPages = (heads.size() + PAGE_SIZE - 1) / PAGE_SIZE;

    this.paginationEnvironment = new EvaluationEnvironmentBuilder()
      .withLiveVariable("current_page", () -> this.currentPage + 1)
      .withLiveVariable("number_pages", () -> this.numberOfPages)
      .build();

    this.searchEnvironment = new EvaluationEnvironmentBuilder()
      .withLiveVariable("search_query", () -> {
        // Otherwise, the anvil-input will show the client-name of the item
        if (this.anvilText.isEmpty())
          return " ";

        return this.anvilText;
      })
      .build(paginationEnvironment);
  }

  @Override
  protected void onDebouncedAnvilText() {
    updateFilteredHeads();
    sendTitle();
    drawPaginationItems();
  }

  private void updateFilteredHeads() {
    this.filteredHeads.clear();
    this.currentPage = 0;

    var querySyllables = Objects.requireNonNull(Syllables.forString(false, anvilText, ' ').syllables());

    if (querySyllables.size() == 0) {
      this.filteredHeads.addAll(unfilteredHeads);
      return;
    }

    var start = System.nanoTime();
    var matcher = new SyllablesMatcher();

    matcher.setQuery(querySyllables);

    for (var head : unfilteredHeads) {
      matcher.resetQueryMatches();
      matcher.setTarget(head.nameSyllables);
      matcher.match();

      if (!matcher.hasUnmatchedQuerySyllables())
        filteredHeads.add(head);
    }

    var end = System.nanoTime();
    logger.info("Filter took " + (end - start) / 1000.0 / 1000.0);
  }

  private void nextPage() {
    taskQueue.enqueue(() -> {
      if (this.currentPage >= numberOfPages - 1)
        return;

      ++this.currentPage;
      sendTitle();
      initializeUiContents();
    });
  }

  private void lastPage() {
    taskQueue.enqueue(() -> {
      if (this.currentPage >= numberOfPages - 1)
        return;

      this.currentPage = this.numberOfPages - 1;
      sendTitle();
      initializeUiContents();
    });
  }

  private void previousPage() {
    taskQueue.enqueue(() -> {
      if (currentPage == 0)
        return;

      --this.currentPage;
      sendTitle();
      initializeUiContents();
    });
  }

  private void firstPage() {
    taskQueue.enqueue(() -> {
      if (currentPage == 0)
        return;

      this.currentPage = 0;
      sendTitle();
      initializeUiContents();
    });
  }

  private void drawPaginationItems() {
    this.numberOfPages = (filteredHeads.size() + PAGE_SIZE - 1) / PAGE_SIZE;

    int bottomSlot;

    for (bottomSlot = 0; bottomSlot < PAGE_SIZE; ++bottomSlot) {
      var headsIndex = currentPage * PAGE_SIZE + bottomSlot;

      if (headsIndex >= filteredHeads.size()) {
        setBottomFakeItem(null, bottomSlot);
        continue;
      }

      var head = filteredHeads.get(headsIndex);

      var headItem = config.rootSection.catalogDisplay.items.representative.build(
        head.getHeadEnvironmentBuilder()
          .build()
      );

      setBottomFakeItem(headItem, bottomSlot);
    }

    var fillerItem = config.rootSection.catalogDisplay.items.filler.build(GPEEE.EMPTY_ENVIRONMENT);

    for (var i = 0; i < 9; ++i)
      setBottomFakeItem(fillerItem, bottomSlot++);

    setBottomFakeItem(
      config.rootSection.catalogDisplay.items.previousPage.build(paginationEnvironment),
      PREVIOUS_PAGE_SLOT
    );

    setBottomFakeItem(
      config.rootSection.catalogDisplay.items.nextPage.build(paginationEnvironment),
      NEXT_PAGE_SLOT
    );

    sendBottomFakeContents();
  }

  @Override
  public void handleClick(int slot, boolean isTop, ClickType clickType) {
    super.handleClick(slot, isTop, clickType);

    if (isTop)
      return;

    if (slot == PREVIOUS_PAGE_SLOT) {
      if (clickType.isRightClick())
        firstPage();
      else
        previousPage();
    }

    else if (slot == NEXT_PAGE_SLOT) {
      if (clickType.isRightClick())
        lastPage();
      else
        nextPage();
    }
  }

  @Override
  public ItemStack buildAnvilInputItem() {
    return config.rootSection.catalogDisplay.items.anvilInputItem.build(searchEnvironment);
  }

  @Override
  public ItemStack buildAnvilSecondItem() {
    return config.rootSection.catalogDisplay.items.filler.build(GPEEE.EMPTY_ENVIRONMENT);
  }

  @Override
  public ItemStack buildAnvilResultItem() {
    return config.rootSection.catalogDisplay.items.anvilResultItem.build(searchEnvironment);
  }

  @Override
  public void initializeUiContents() {
    super.initializeUiContents();
    drawPaginationItems();
  }

  @Override
  public Object createTitleComponent() throws Throwable {
    return config.rootSection.catalogDisplay.title.asChatComponent(paginationEnvironment);
  }
}
