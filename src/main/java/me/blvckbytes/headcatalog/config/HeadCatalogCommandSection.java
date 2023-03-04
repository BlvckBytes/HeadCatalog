package me.blvckbytes.headcatalog.config;

import me.blvckbytes.bbconfigmapper.sections.CSAlways;
import me.blvckbytes.bbconfigmapper.sections.IConfigSection;
import me.blvckbytes.bukkitboilerplate.ICommandConfigProvider;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.util.List;

public class HeadCatalogCommandSection implements IConfigSection, ICommandConfigProvider {

  private String name, description, usage;

  @CSAlways
  private List<String> aliases;

  @Override
  public @Nullable Object defaultFor(Field field) {
    if (field.getName().equals("name"))
      return "headcatalog";

    if (field.getType() == String.class)
      return "";

    return null;
  }

  @Override
  public String getName() {
    return this.name;
  }

  @Override
  public List<String> getAliases() {
    return this.aliases;
  }

  @Override
  public String getDescription() {
    return this.description;
  }

  @Override
  public String getUsage() {
    return this.usage;
  }
}
