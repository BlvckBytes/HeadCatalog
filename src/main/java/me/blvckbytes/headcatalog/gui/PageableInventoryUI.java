package me.blvckbytes.headcatalog.gui;

import me.blvckbytes.bbreflect.packets.communicator.IFakeSlotCommunicator;
import me.blvckbytes.gpeee.interpreter.EvaluationEnvironmentBuilder;
import me.blvckbytes.gpeee.interpreter.IEvaluationEnvironment;
import me.blvckbytes.headcatalog.gui.config.IPageableParameterProvider;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public abstract class PageableInventoryUI<T extends IPageableParameterProvider> extends AInventoryUI<T> {

  private static final String
    KEY_PREVIOUS_PAGE = "previousPage",
    KEY_CURRENT_PAGE = "currentPage",
    KEY_NEXT_PAGE = "nextPage";

  private final List<Integer> paginationSlotIndices;
  private final int pageSize;
  private final long animationPeriod;

  private List<UISlot> pageableSlots;
  private int numberOfPageables;
  private boolean isFirstPageRender;

  private int currentPage;
  private int numberOfPages;

  public PageableInventoryUI(IFakeSlotCommunicator fakeSlotCommunicator, T pageableParameters, Player viewer) {
    super(fakeSlotCommunicator, pageableParameters, viewer);
    this.pageableSlots = new ArrayList<>();
    this.paginationSlotIndices = parameterProvider.getPaginationSlots(inventoryEnvironment);
    this.animationPeriod = parameterProvider.getAnimationPeriod();
    this.pageSize = this.paginationSlotIndices.size();
    this.isFirstPageRender = true;
  }

  @Override
  protected void decorate() {
    super.decorate();

    IEvaluationEnvironment paginationEnvironment = getPaginationEnvironment();

    for (Map.Entry<String, Set<Integer>> contentEntry : slotContents.entrySet()) {
      Set<Integer> slots = contentEntry.getValue();
      UISlot slotContent = null;

      switch (contentEntry.getKey()) {
        case KEY_PREVIOUS_PAGE:
          slotContent = new UISlot(() -> parameterProvider.getPreviousPage().build(paginationEnvironment), this::handlePreviousPageClick);
          break;

        case KEY_NEXT_PAGE:
          slotContent = new UISlot(() -> parameterProvider.getNextPage().build(paginationEnvironment), this::handleNextPageClick);
          break;

        case KEY_CURRENT_PAGE:
          slotContent = new UISlot(() -> parameterProvider.getCurrentPage().build(paginationEnvironment));
          break;
      }

      if (slotContent == null)
        continue;

      setSlots(slotContent, slots);
    }
  }

  public void setPageableSlots(Collection<UISlot> items) {
    this.pageableSlots = new ArrayList<>(items);
    this.numberOfPageables = this.pageableSlots.size();

    if (this.pageSize == 0) {
      this.numberOfPages = 0;
      setCurrentPage(0, null);
      return;
    }

    int oldNumberOfPages = this.numberOfPages;
    this.numberOfPages = (int) Math.ceil(this.numberOfPageables / (float) this.pageSize);

    // Try to keep the current page, if possible, only reset if it would be out of bounds
    if (this.numberOfPages < oldNumberOfPages)
      setCurrentPage(0, null);

    else
      this.drawPagination(null);
  }

  private void drawCurrentPage() {
    for (int i = 0; i < pageSize; i++) {
      int slot = paginationSlotIndices.get(i);
      int pageableSlotsIndex = this.currentPage * this.pageSize + i;

      UISlot slotValue;

      if (pageableSlotsIndex >= this.numberOfPageables)
        slotValue = null;
      else
        slotValue = pageableSlots.get(pageableSlotsIndex);

      setSlot(slot, slotValue);
      drawSlot(slot);
    }
  }

  private void drawPagination(@Nullable EAnimationType animationType) {
    animator.saveLayout(this);

    this.drawCurrentPage();
    this.drawNamedSlot(KEY_PREVIOUS_PAGE);
    this.drawNamedSlot(KEY_CURRENT_PAGE);
    this.drawNamedSlot(KEY_NEXT_PAGE);

    if (!isFirstPageRender && animationType != null && parameterProvider.isAnimating())
      animator.animateTo(animationType, paginationSlotIndices, this);

    isFirstPageRender = false;
  }

  private void setCurrentPage(int slot, @Nullable EAnimationType animationType) {
    this.currentPage = slot;
    this.drawPagination(animationType);
  }

  private EnumSet<EClickResultFlag> handlePreviousPageClick(UIInteraction action) {
    if (this.currentPage == 0)
      return null;

    if (action.clickType.isRightClick()) {
      setCurrentPage(0, EAnimationType.SLIDE_RIGHT);
      return null;
    }

    setCurrentPage(this.currentPage - 1, EAnimationType.SLIDE_RIGHT);
    return null;
  }

  private EnumSet<EClickResultFlag> handleNextPageClick(UIInteraction action) {
    if (this.currentPage == numberOfPages - 1)
      return null;

    if (action.clickType.isRightClick()) {
      setCurrentPage(this.numberOfPages - 1, EAnimationType.SLIDE_LEFT);
      return null;
    }

    setCurrentPage(this.currentPage + 1, EAnimationType.SLIDE_LEFT);
    return null;
  }

  private IEvaluationEnvironment getPaginationEnvironment() {
    return new EvaluationEnvironmentBuilder()
      .withLiveVariable("name", viewer::getName)
      .withLiveVariable("current_page", () -> this.currentPage + 1)
      .withLiveVariable("page_size", () -> this.pageSize)
      .withLiveVariable("number_of_pages", () -> this.numberOfPages)
      .withLiveVariable("number_of_pageables", () -> this.numberOfPageables)
      .build();
  }

  @Override
  public void handleTick(long time) {
    if (time % animationPeriod == 0)
      this.animator.tick();
  }

  @Override
  public void handleInteraction(UIInteraction interaction) {
    this.animator.fastForward();
    super.handleInteraction(interaction);
  }
}
