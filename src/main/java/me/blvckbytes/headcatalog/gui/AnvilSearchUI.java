package me.blvckbytes.headcatalog.gui;

import me.blvckbytes.bbconfigmapper.ScalarType;
import me.blvckbytes.bbreflect.packets.communicator.FakeSlotCommunicator;
import me.blvckbytes.bukkitevaluable.BukkitEvaluable;
import me.blvckbytes.bukkitevaluable.ItemBuilder;
import me.blvckbytes.gpeee.interpreter.EvaluationEnvironmentBuilder;
import me.blvckbytes.gpeee.interpreter.IEvaluationEnvironment;
import me.blvckbytes.headcatalog.gui.config.IAnvilSearchParameterProvider;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;

import java.util.*;

public class AnvilSearchUI extends PageableInventoryUI<IAnvilSearchParameterProvider, AnvilSearchParameter> {

  private static final String KEY_FILTER = "filter";

  private final Map<String, Boolean> filterStates;
  private ISearchFilterEnum<?> currentFilter;
  private String searchText;

  public AnvilSearchUI(FakeSlotCommunicator fakeSlotCommunicator, IAnvilSearchParameterProvider parameterProvider, AnvilSearchParameter parameter) {
    super(fakeSlotCommunicator, parameterProvider, parameter);

    this.searchText = " ";

    this.filterStates = new LinkedHashMap<>();
    this.currentFilter = parameter.searchFilter;
    this.setupFilterStates();
  }

  private void setupFilterStates() {
    for (ISearchFilterEnum<?> searchFilter : parameter.searchFilter.listValues())
      filterStates.put(searchFilter.name(), searchFilter == currentFilter);
  }

  @Override
  protected Inventory createInventory() {
    String title = parameterProvider.getTitle().asScalar(ScalarType.STRING, inventoryEnvironment);
    return Bukkit.createInventory(null, InventoryType.ANVIL, title);
  }

  @Override
  protected void handleClose() {
    fakeSlotCommunicator.unblockWindowItems(parameter.viewer);
    super.handleClose();
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
      }

      if (slotContent == null)
        continue;

      setSlots(slotContent, contentEntry.getValue());
    }

    setSlot(0, new UISlot(() -> (
      new ItemBuilder(Material.QUARTZ_BLOCK, 1)
        .setName(BukkitEvaluable.of(" "))
        .build()
    )));

    List<String> loreLines = new ArrayList<>();
    loreLines.add("&8● &7This just shows you, what");
    loreLines.add("&8● &7you've &dtyped &7into the anvil.");

    setSlot(this.inventory.getSize() + 4, new UISlot(() -> (
      new ItemBuilder(Material.DIAMOND_AXE, 1)
        .setName(BukkitEvaluable.of("&8»&5" + this.searchText))
        .overrideLore(BukkitEvaluable.of(loreLines))
        .overrideFlags(BukkitEvaluable.of("HIDE_ATTRIBUTES"))
        .build()
    )));
  }

  @Override
  public void handleItemRename(String name) {
    super.handleItemRename(name);
    this.searchText = name;
    drawSlot(this.inventory.getSize() + 4);
  }

  @Override
  protected boolean canInteractWithOwnInventory() {
    return false;
  }

  @Override
  public void show() {
    fakeSlotCommunicator.blockWindowItems(parameter.viewer, fakeSlotItemCache::get);
    super.show();
  }

  private EnumSet<EClickResultFlag> handleFilterClick(UIInteraction action) {
    this.filterStates.put(this.currentFilter.name(), false);
    this.currentFilter = this.currentFilter.nextValue();
    this.filterStates.put(this.currentFilter.name(), true);
    drawNamedSlot(KEY_FILTER);
    return null;
  }

  private IEvaluationEnvironment getFilterEnvironment() {
    return new EvaluationEnvironmentBuilder()
      .withStaticVariable("filters", this.filterStates)
      .build();
  }
}
