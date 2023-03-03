package me.blvckbytes.headcatalog.gui;

import me.blvckbytes.bbconfigmapper.ScalarType;
import me.blvckbytes.gpeee.interpreter.EvaluationEnvironmentBuilder;
import me.blvckbytes.gpeee.interpreter.IEvaluationEnvironment;
import me.blvckbytes.headcatalog.gui.config.ISingleChoiceParameterProvider;
import org.bukkit.Bukkit;
import org.bukkit.inventory.Inventory;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

public abstract class SingleChoiceUI<DataType> extends PageableInventoryUI<ISingleChoiceParameterProvider, SingleChoiceParameter<DataType>, DataType> {

  private static final String KEY_SEARCH = "search";
  private final AnvilSearchUI<DataType> searchUI;

  public SingleChoiceUI(IInventoryRegistry registry, SingleChoiceParameter<DataType> parameter) {
    super(registry, parameter);

    this.searchUI = new AnvilSearchUI<>(
      registry, parameter.makeAnvilSearchParameter(ui -> this.show())
    );
  }

  @Override
  protected void decorate() {
    super.decorate();

    for (Map.Entry<String, Set<Integer>> contentEntry : slotContents.entrySet()) {
      UISlot slotContent = null;

      switch (contentEntry.getKey()) {
        case KEY_SEARCH:
          slotContent = new UISlot(() -> parameter.provider.getSearch().build(), interaction -> {
            this.searchUI.show();
            return null;
          });
          break;
      }

      if (slotContent == null)
        continue;

      setSlots(slotContent, contentEntry.getValue());
    }
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
  }

  @Override
  protected boolean canInteractWithOwnInventory() {
    return true;
  }

  @Override
  public void setPageableSlots(Collection<DataBoundUISlot<DataType>> items) {
    super.setPageableSlots(items);

    if (this.searchUI.isRegistered())
      this.searchUI.invokeFilterFunctionAndUpdatePageSlots();
  }

  private IEvaluationEnvironment getTitleEnvironment() {
    return new EvaluationEnvironmentBuilder()
      .withLiveVariable("name", parameter.viewer::getName)
      .build();
  }
}
