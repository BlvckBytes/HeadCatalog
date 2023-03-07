package me.blvckbytes.headcatalog.config;

import me.blvckbytes.gpeee.functions.AExpressionFunction;
import me.blvckbytes.gpeee.functions.ExpressionFunctionArgument;
import me.blvckbytes.gpeee.interpreter.IEvaluationEnvironment;
import me.blvckbytes.headcatalog.apis.HeadModel;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MakeHeadFunction extends AExpressionFunction {

  private final Logger logger;

  public MakeHeadFunction(Logger logger) {
    this.logger = logger;
  }

  @Override
  public Object apply(IEvaluationEnvironment environment, List<@Nullable Object> args) {
    String name = nonNull(args, 0);
    String value = nonNull(args, 1);
    String uuidString = nullable(args, 2);
    Collection<Object> categories = nullable(args, 3);
    Collection<Object> tags = nullable(args, 4);

    return new HeadModel(name, value, convertStringCollection(categories), decideUUID(uuidString), convertStringCollection(tags));
  }

  @Override
  public @Nullable List<ExpressionFunctionArgument> getArguments() {
    List<ExpressionFunctionArgument> arguments = new ArrayList<>();

    arguments.add(new ExpressionFunctionArgument("name", "Name of the head texture", true, String.class));
    arguments.add(new ExpressionFunctionArgument("skinUrl", "URL of the skin sprite", true, String.class));
    arguments.add(new ExpressionFunctionArgument("uuid", "UUID to use for the GameProfile", false, String.class));
    arguments.add(new ExpressionFunctionArgument("categories", "Categories of the head texture", false, Collection.class));
    arguments.add(new ExpressionFunctionArgument("tags", "Tags this head is a member of", false, Collection.class));

    return arguments;
  }

  private UUID decideUUID(@Nullable String uuidString) {
    if (uuidString == null)
      return UUID.randomUUID();

    try {
      return UUID.fromString(uuidString);
    } catch (Exception e) {
      this.logger.log(Level.SEVERE, e, () -> "Could not parse the following UUID in the MakeHeadFunction: " + uuidString);
      this.logger.log(Level.SEVERE, "Generated a random UUID in order to keep operating");
      return UUID.randomUUID();
    }
  }

  private Set<String> convertStringCollection(@Nullable Collection<Object> values) {
    if (values == null)
      return new HashSet<>();

    Set<String> stringSet = new HashSet<>();
    for (Object value : values)
      stringSet.add(String.valueOf(value));

    return stringSet;
  }
}
