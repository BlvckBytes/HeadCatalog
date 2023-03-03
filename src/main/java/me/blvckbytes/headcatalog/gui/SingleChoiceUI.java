package me.blvckbytes.headcatalog.gui;

import me.blvckbytes.bbconfigmapper.ScalarType;
import me.blvckbytes.bbreflect.packets.communicator.IFakeSlotCommunicator;
import me.blvckbytes.gpeee.interpreter.EvaluationEnvironmentBuilder;
import me.blvckbytes.gpeee.interpreter.IEvaluationEnvironment;
import me.blvckbytes.headcatalog.gui.config.ISingleChoiceParameterProvider;
import org.bukkit.Bukkit;
import org.bukkit.inventory.Inventory;

import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

public class SingleChoiceUI<DataType> extends PageableInventoryUI<ISingleChoiceParameterProvider, SingleChoiceParameter, DataType> {

  // TODO: Updatable inventory names would be useful

  private static final String KEY_SEARCH = "search";

  public SingleChoiceUI(IFakeSlotCommunicator fakeSlotCommunicator, SingleChoiceParameter parameter) {
    super(fakeSlotCommunicator, parameter);
  }

  @Override
  protected void decorate() {
    super.decorate();

    for (Map.Entry<String, Set<Integer>> contentEntry : slotContents.entrySet()) {
      UISlot slotContent = null;

      switch (contentEntry.getKey()) {
        case KEY_SEARCH:
          slotContent = new UISlot(() -> parameter.provider.getSearch().build(), this::handleSearchClick);
          break;
      }

      if (slotContent == null)
        continue;

      setSlots(slotContent, contentEntry.getValue());
    }
  }

  private EnumSet<EClickResultFlag> handleSearchClick(UIInteraction action) {
    System.out.println("Clicked search");
    return null;
  }

  @Override
  protected Inventory createInventory() {
    IEvaluationEnvironment titleEnvironment = getTitleEnvironment();
    String title = parameter.provider.getTitle().asScalar(ScalarType.STRING, titleEnvironment);
    return Bukkit.createInventory(null, parameter.provider.getNumberOfRows() * 9, title);
  }

  @Override
  protected void handleClose() {
    super.handleClose();
    System.out.println("Closed");
  }

  @Override
  protected boolean canInteractWithOwnInventory() {
    return true;
  }

  private IEvaluationEnvironment getTitleEnvironment() {
    return new EvaluationEnvironmentBuilder()
      .withLiveVariable("name", parameter.viewer::getName)
      .build();
  }
}
