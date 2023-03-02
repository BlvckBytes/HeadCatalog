package me.blvckbytes.headcatalog.gui;

import me.blvckbytes.bbconfigmapper.ScalarType;
import me.blvckbytes.gpeee.interpreter.EvaluationEnvironmentBuilder;
import me.blvckbytes.gpeee.interpreter.IEvaluationEnvironment;
import me.blvckbytes.headcatalog.gui.config.ISingleChoiceParameterProvider;
import org.bukkit.Bukkit;
import org.bukkit.inventory.Inventory;

import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

public class SingleChoiceUI extends PageableInventoryUI<ISingleChoiceParameterProvider> {

  // TODO: Updatable inventory names would be useful

  private static final String KEY_SEARCH = "search";

  public SingleChoiceUI(ISingleChoiceParameterProvider singleChoiceParameters, SingleChoiceParameter parameter) {
    super(singleChoiceParameters, parameter.viewer);
  }

  @Override
  protected void decorate() {
    super.decorate();

    for (Map.Entry<String, Set<Long>> contentEntry : parameterProvider.getSlotContents().entrySet()) {
      Set<Long> slots = contentEntry.getValue();
      UISlot slotContent = null;

      switch (contentEntry.getKey()) {
        case KEY_SEARCH:
          slotContent = new UISlot(() -> parameterProvider.getSearch().build(), this::handleSearchClick);
          break;
      }

      if (slotContent == null)
        continue;

      setSlots(slotContent, slots);
    }
  }

  private EnumSet<EClickResultFlag> handleSearchClick(UIInteraction action) {
    System.out.println("Clicked search");
    return null;
  }

  @Override
  protected Inventory createInventory() {
    IEvaluationEnvironment titleEnvironment = getTitleEnvironment();
    String title = parameterProvider.getTitle().asScalar(ScalarType.STRING, titleEnvironment);
    return Bukkit.createInventory(null, parameterProvider.getNumberOfRows() * 9, title);
  }

  @Override
  protected void handleClose() {
    System.out.println("Closed");
  }

  @Override
  protected boolean canInteractWithOwnInventory() {
    return true;
  }

  private IEvaluationEnvironment getTitleEnvironment() {
    return new EvaluationEnvironmentBuilder()
      .withLiveVariable("name", viewer::getName)
      .build();
  }
}
