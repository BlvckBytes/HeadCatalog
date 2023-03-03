package me.blvckbytes.headcatalog.gui;

import me.blvckbytes.bbconfigmapper.ScalarType;
import me.blvckbytes.bbreflect.packets.communicator.FakeSlotCommunicator;
import me.blvckbytes.gpeee.interpreter.EvaluationEnvironmentBuilder;
import me.blvckbytes.gpeee.interpreter.IEvaluationEnvironment;
import me.blvckbytes.headcatalog.gui.config.IAnvilSearchParameterProvider;
import org.bukkit.Bukkit;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;

import java.util.*;

public class AnvilSearchUI<DataType> extends PageableInventoryUI<IAnvilSearchParameterProvider, AnvilSearchParameter<DataType>, DataType> {

  private static final String
    KEY_FILTER = "filter",
    KEY_BACK = "back",
    KEY_SEARCH_ITEM = "searchItem";

  private final Map<String, Boolean> filterStates;
  private ISearchFilterEnum<?, DataType> currentFilter;
  private String searchText;

  public AnvilSearchUI(FakeSlotCommunicator fakeSlotCommunicator, IAnvilSearchParameterProvider parameterProvider, AnvilSearchParameter<DataType> parameter) {
    super(fakeSlotCommunicator, parameterProvider, parameter);

    this.searchText = " ";

    this.filterStates = new LinkedHashMap<>();
    this.currentFilter = parameter.searchFilter;
    this.setupFilterStates();
  }

  @Override
  protected Inventory createInventory() {
    String title = parameterProvider.getTitle().asScalar(ScalarType.STRING, inventoryEnvironment);
    return Bukkit.createInventory(null, InventoryType.ANVIL, title);
  }

  @Override
  protected void decorate() {
    super.decorate();

    for (Map.Entry<String, Set<Integer>> contentEntry : slotContents.entrySet()) {
      UISlot slotContent = null;

      switch (contentEntry.getKey()) {
        case KEY_FILTER: {
          IEvaluationEnvironment filterEnvironment = getFilterEnvironment();
          slotContent = new UISlot(() -> parameterProvider.getFilter().build(filterEnvironment), this::handleFilterClick);
          break;
        }

        case KEY_SEARCH_ITEM:
          slotContent = new UISlot(() -> parameterProvider.getSearchItem().build(inventoryEnvironment));
          break;

        case KEY_BACK:
          slotContent = new UISlot(() -> parameterProvider.getBack().build(inventoryEnvironment), this::handleBackClick);
          break;
      }

      if (slotContent == null)
        continue;

      setSlots(slotContent, contentEntry.getValue());
    }

    invokeFilterFunctionAndUpdatePageSlots();
  }

  private void invokeFilterFunctionAndUpdatePageSlots() {
    List<DataBoundUISlot<DataType>> slots = parameter.filterFunction.applyFilter(currentFilter, searchText);
    setPageableSlots(slots);
  }

  @Override
  public void handleItemRename(String name) {
    super.handleItemRename(name);
    this.searchText = name;
    invokeFilterFunctionAndUpdatePageSlots();
  }

  @Override
  protected boolean canInteractWithOwnInventory() {
    return false;
  }

  @Override
  public void show() {
    blockWindowItems();
    super.show();
  }

  @Override
  protected void handleClose() {
    unblockWindowItems();
    super.handleClose();
  }

  private void setupFilterStates() {
    for (ISearchFilterEnum<?, DataType> searchFilter : parameter.searchFilter.listValues())
      filterStates.put(searchFilter.name(), searchFilter == currentFilter);
  }

  private EnumSet<EClickResultFlag> handleFilterClick(UIInteraction action) {
    this.filterStates.put(this.currentFilter.name(), false);
    this.currentFilter = this.currentFilter.nextValue();
    this.filterStates.put(this.currentFilter.name(), true);
    drawNamedSlot(KEY_FILTER);
    return null;
  }

  private EnumSet<EClickResultFlag> handleBackClick(UIInteraction action) {
    System.out.println("Back");
    return null;
  }

  private IEvaluationEnvironment getFilterEnvironment() {
    return new EvaluationEnvironmentBuilder()
      .withStaticVariable("filters", this.filterStates)
      .build();
  }
}
