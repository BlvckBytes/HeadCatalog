package me.blvckbytes.headcatalog;

import me.blvckbytes.gpeee.interpreter.EvaluationEnvironmentBuilder;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class Head {

  private final EvaluationEnvironmentBuilder headEnvironmentBuilder;

  public final String name;
  public final String category;

  // Used for permission-checks
  public final String normalizedCategory;

  // Used to cache permission-check boolean-results
  public final int numericCategory;

  public final Collection<String> tags;
  public final String base64Textures;

  public Head(
    String name,
    String category,
    String normalizedCategory,
    int numericCategory,
    List<String> tags,
    String base64Textures
  ) {
    this.name = name;
    this.category = category;
    this.normalizedCategory = normalizedCategory;
    this.numericCategory = numericCategory;
    this.tags = Collections.unmodifiableCollection(tags);
    this.base64Textures = base64Textures;

    this.headEnvironmentBuilder = new EvaluationEnvironmentBuilder()
      .withStaticVariable("head_name", name)
      .withStaticVariable("head_category", category)
      .withStaticVariable("head_tags", tags)
      .withStaticVariable("head_base64_textures", base64Textures);
  }

  public EvaluationEnvironmentBuilder getHeadEnvironmentBuilder() {
    return headEnvironmentBuilder.duplicate();
  }
}
