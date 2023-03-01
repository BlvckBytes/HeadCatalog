package me.blvckbytes.headcatalog.gui.config;

import me.blvckbytes.bbconfigmapper.IEvaluable;
import me.blvckbytes.bbconfigmapper.ScalarType;
import me.blvckbytes.bbconfigmapper.sections.CSAlways;
import me.blvckbytes.bbconfigmapper.sections.IConfigSection;
import me.blvckbytes.bukkitevaluable.BukkitEvaluable;
import me.blvckbytes.bukkitevaluable.IItemBuildable;
import me.blvckbytes.bukkitevaluable.ItemBuilder;
import me.blvckbytes.gpeee.GPEEE;
import org.bukkit.Material;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.util.*;

public class UIBaseLayoutSection implements IConfigSection, IInventoryUIParameterProvider {

  private static final IItemBuildable ITEM_NOT_CONFIGURED;

  static {
    List<String> loreLines = new ArrayList<>();
    loreLines.add("§cThis item has not been configured");
    loreLines.add("§cproperly. Please check your config.");

    ITEM_NOT_CONFIGURED = new ItemBuilder(Material.BARRIER, 1)
      .setName(BukkitEvaluable.of("§4Not configured"))
      .overrideLore(BukkitEvaluable.of(loreLines));
  }

  private int numberOfRows;

  @CSAlways
  private IEvaluable title;

  private @Nullable IItemBuildable fill;
  private @Nullable IItemBuildable border;

  @CSAlways
  private Map<String, IEvaluable> slotContents;

  private Map<String, Set<Long>> evaluatedSlotContents;

  @Override
  public void afterParsing(List<Field> fields) throws Exception {
    this.evaluatedSlotContents = new HashMap<>();
    for (Map.Entry<String, IEvaluable> entry : slotContents.entrySet()) {
      Set<Long> indices = entry.getValue().asSet(ScalarType.LONG, GPEEE.EMPTY_ENVIRONMENT);
      this.evaluatedSlotContents.put(entry.getKey(), indices);
    }
  }

  @Override
  public @Nullable Object defaultFor(Field field) {
    String name = field.getName();
    if (
      IItemBuildable.class.isAssignableFrom(field.getType()) &&
      (!name.equals("fill")) && (!name.equals("border"))
    ) {
      return ITEM_NOT_CONFIGURED;
    }
    return null;
  }

  @Override
  public IEvaluable getTitle() {
    return title;
  }

  @Override
  public int getNumberOfRows() {
    return numberOfRows;
  }

  @Override
  public @Nullable IItemBuildable getFill() {
    return fill;
  }

  @Override
  public @Nullable IItemBuildable getBorder() {
    return border;
  }

  @Override
  public Map<String, Set<Long>> getSlotContents() {
    return evaluatedSlotContents;
  }
}
