package me.blvckbytes.headcatalog.command;

import me.blvckbytes.gpeee.interpreter.IEvaluationEnvironment;
import me.blvckbytes.headcatalog.apis.HeadModel;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class Head implements Comparable<Head> {

  public final HeadModel model;
  public final IEvaluationEnvironment environment;
  public final ItemStack item;

  public Head(HeadModel model, IEvaluationEnvironment environment, ItemStack item) {
    this.model = model;
    this.item = item;
    this.environment = environment;
  }

  @Override
  public int compareTo(@NotNull Head o) {
    return model.compareTo(o.model);
  }

  @Override
  public boolean equals(Object other) {
    if (!(other instanceof Head))
      return false;
    return ((Head) other).model.equals(model);
  }

  @Override
  public int hashCode() {
    return model.hashCode();
  }
}
