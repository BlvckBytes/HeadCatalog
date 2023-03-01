package me.blvckbytes.headcatalog.gui;

import me.blvckbytes.gpeee.interpreter.EvaluationEnvironmentBuilder;
import me.blvckbytes.gpeee.interpreter.IEvaluationEnvironment;
import me.blvckbytes.headcatalog.gui.config.IPageableParameterProvider;
import org.bukkit.entity.Player;

import java.util.*;

public abstract class PageableInventoryUI<T extends IPageableParameterProvider> extends AInventoryUI<T> {

  private final List<Integer> paginationSlotIndices;
  private final int pageSize;

  private List<UISlot> pageableSlots;
  private int numberOfPageables;

  private int currentPage;
  private int numberOfPages;

  public PageableInventoryUI(T pageableParameters, Player viewer) {
    super(pageableParameters, viewer);
    this.pageableSlots = new ArrayList<>();
    this.paginationSlotIndices = parameterProvider.getPaginationSlots();
    this.pageSize = this.paginationSlotIndices.size();
  }

  @Override
  protected void decorate() {
    super.decorate();

    IEvaluationEnvironment paginationEnvironment = getPaginationEnvironment();

    for (Map.Entry<String, Set<Long>> contentEntry : parameterProvider.getSlotContents().entrySet()) {
      Set<Long> slots = contentEntry.getValue();
      UISlot slotContent = null;

      switch (contentEntry.getKey()) {
        case "previousPage":
          slotContent = new UISlot(() -> parameterProvider.getPreviousPage().build(paginationEnvironment), this::handlePreviousPageClick);
          break;

        case "nextPage":
          slotContent = new UISlot(() -> parameterProvider.getNextPage().build(paginationEnvironment), this::handleNextPageClick);
          break;

        case "currentPage":
          slotContent = new UISlot(() -> parameterProvider.getCurrentPage().build(paginationEnvironment));
          break;
      }

      if (slotContent == null)
        continue;

      setSlots(slotContent, slots);
    }
  }

  protected void setPageableSlots(Collection<UISlot> items) {
    this.pageableSlots = new ArrayList<>(items);
    this.numberOfPageables = this.pageableSlots.size();

    int numberOfIndices = this.paginationSlotIndices.size();

    if (numberOfIndices == 0) {
      this.numberOfPages = 0;
      setCurrentPage(0);
      return;
    }

    int oldNumberOfPages = this.numberOfPages;
    this.numberOfPages = (int) Math.ceil(this.paginationSlotIndices.size() / (float) numberOfIndices);

    // Try to keep the current page, if possible, only reset if it would be out of bounds
    if (this.numberOfPages < oldNumberOfPages)
      setCurrentPage(0);
  }

  private void drawCurrentPage() {
    for (int i = 0; i < pageSize; i++) {
      int slot = paginationSlotIndices.get(i);
      int pageableSlotsIndex = this.currentPage * this.pageSize + 1;

      if (pageableSlotsIndex >= this.numberOfPageables)
        break;

      setSlot(slot, pageableSlots.get(pageableSlotsIndex));
    }
  }

  private void setCurrentPage(int slot) {
    this.currentPage = slot;
    this.drawCurrentPage();
  }

  private EnumSet<EClickResultFlag> handlePreviousPageClick(UIInteraction action) {
    System.out.println("Clicked previous page");
    if (this.currentPage == 0)
      return null;

    setCurrentPage(this.currentPage - 1);
    return null;
  }

  private EnumSet<EClickResultFlag> handleNextPageClick(UIInteraction action) {
    System.out.println("Clicked next page");
    if (this.currentPage == numberOfPages - 1)
      return null;

    setCurrentPage(this.currentPage + 1);
    return null;
  }

  private IEvaluationEnvironment getPaginationEnvironment() {
    return new EvaluationEnvironmentBuilder()
      .withLiveVariable("name", viewer::getName)
      .withLiveVariable("currentPage", () -> this.currentPage)
      .withLiveVariable("pageSize", () -> this.pageSize)
      .withLiveVariable("numberOfPages", () -> this.numberOfPages)
      .withLiveVariable("numberOfPageables", () -> this.numberOfPageables)
      .build();
  }
}
