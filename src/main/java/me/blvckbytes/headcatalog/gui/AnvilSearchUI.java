package me.blvckbytes.headcatalog.gui;

import me.blvckbytes.bbconfigmapper.ScalarType;
import me.blvckbytes.bukkitevaluable.BukkitEvaluable;
import me.blvckbytes.bukkitevaluable.ItemBuilder;
import me.blvckbytes.gpeee.interpreter.EvaluationEnvironmentBuilder;
import me.blvckbytes.gpeee.interpreter.IEvaluationEnvironment;
import me.blvckbytes.headcatalog.gui.config.IAnvilSearchParameterProvider;
import me.blvckbytes.headcatalog.gui.reflect.FakeSlotCommunicator;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;

import java.util.ArrayList;
import java.util.List;

public class AnvilSearchUI extends AInventoryUI<IAnvilSearchParameterProvider> {

  private String searchText;

  public AnvilSearchUI(FakeSlotCommunicator fakeSlotCommunicator, IAnvilSearchParameterProvider parameterProvider, AnvilSearchParameter parameter) {
    super(fakeSlotCommunicator, parameterProvider, parameter.viewer);
    this.searchText = " ";
  }

  @Override
  protected Inventory createInventory() {
    IEvaluationEnvironment titleEnvironment = getTitleEnvironment();
    String title = parameterProvider.getTitle().asScalar(ScalarType.STRING, titleEnvironment);
    return Bukkit.createInventory(null, InventoryType.ANVIL, title);
  }

  @Override
  protected void handleClose() {
    super.handleClose();
  }

  @Override
  protected void decorate() {
    super.decorate();

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

  private IEvaluationEnvironment getTitleEnvironment() {
    return new EvaluationEnvironmentBuilder()
      .withLiveVariable("name", viewer::getName)
      .build();
  }
}
