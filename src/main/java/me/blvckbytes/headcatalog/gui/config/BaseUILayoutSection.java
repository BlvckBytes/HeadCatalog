package me.blvckbytes.headcatalog.gui.config;

import me.blvckbytes.bbconfigmapper.IEvaluable;
import me.blvckbytes.bbconfigmapper.ScalarType;
import me.blvckbytes.bbconfigmapper.sections.CSAlways;
import me.blvckbytes.bbconfigmapper.sections.IConfigSection;
import me.blvckbytes.bukkitevaluable.BukkitEvaluable;
import me.blvckbytes.bukkitevaluable.IItemBuildable;
import me.blvckbytes.bukkitevaluable.ItemBuilder;
import me.blvckbytes.gpeee.interpreter.IEvaluationEnvironment;
import org.bukkit.Material;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.util.*;

public class BaseUILayoutSection implements IConfigSection, IInventoryUIParameterProvider {

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
  private BukkitEvaluable title;

  private @Nullable IItemBuildable fill;
  private @Nullable IItemBuildable border;

  private boolean animating;

  private int animationPeriod;

  @CSAlways
  private Map<String, IEvaluable> slotContents;

  @Override
  public void afterParsing(List<Field> fields) throws Exception {
    this.animationPeriod = Math.max(1, animationPeriod);
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
  public boolean isAnimating() {
    return animating;
  }

  @Override
  public int getAnimationPeriod() {
    return animationPeriod;
  }

  @Override
  public Map<String, Set<Integer>> getSlotContents(IEvaluationEnvironment environment) {
    Map<String, Set<Integer>> evaluatedSlotContents = new HashMap<>();

    for (Map.Entry<String, IEvaluable> entry : slotContents.entrySet()) {
      Set<Integer> indices = entry.getValue().asSet(ScalarType.INT, environment);
      evaluatedSlotContents.put(entry.getKey(), indices);
    }

    return evaluatedSlotContents;
  }
}
