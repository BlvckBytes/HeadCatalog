package me.blvckbytes.headcatalog.config;

import me.blvckbytes.bukkitboilerplate.ELogLevel;
import me.blvckbytes.bukkitboilerplate.ILogger;
import me.blvckbytes.gpeee.functions.AExpressionFunction;
import me.blvckbytes.gpeee.functions.ExpressionFunctionArgument;
import me.blvckbytes.gpeee.interpreter.IEvaluationEnvironment;
import me.blvckbytes.headcatalog.apis.HeadModel;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class MakeHeadFunction extends AExpressionFunction {

  private final ILogger logger;

  public MakeHeadFunction(ILogger logger) {
    this.logger = logger;
  }

  @Override
  public Object apply(IEvaluationEnvironment environment, List<@Nullable Object> args) {
    String name = nonNull(args, 0);
    String value = nonNull(args, 1);
    Collection<Object> categories = nullable(args, 2);
    String uuidString = nullable(args, 3);
    Collection<Object> tags = nullable(args, 4);

    return new HeadModel(name, value, convertStringCollection(categories), decideUUID(uuidString), convertStringCollection(tags));
  }

  @Override
  public @Nullable List<ExpressionFunctionArgument> getArguments() {
    List<ExpressionFunctionArgument> arguments = new ArrayList<>();

    arguments.add(new ExpressionFunctionArgument("name", "Name of the head texture", true, String.class));
    arguments.add(new ExpressionFunctionArgument("value", "Base64 value of the head texture", true, String.class));
    arguments.add(new ExpressionFunctionArgument("categories", "Categories of the head texture", false, Collection.class));
    arguments.add(new ExpressionFunctionArgument("uuid", "UUID to use for the GameProfile", false, String.class));
    arguments.add(new ExpressionFunctionArgument("tags", "Tags this head is a member of", false, Collection.class));

    return arguments;
  }

  private @Nullable UUID decideUUID(@Nullable String uuidString) {
    if (uuidString == null)
      return null;

    try {
      return UUID.fromString(uuidString);
    } catch (Exception e) {
      this.logger.log(ELogLevel.ERROR, "Could not parse the following UUID in the MakeHeadFunction: " + uuidString);
      this.logger.log(ELogLevel.ERROR, "Generated a random UUID in order to keep operating");
      this.logger.logError(e);
      return UUID.randomUUID();
    }
  }

  private @Nullable Set<String> convertStringCollection(@Nullable Collection<Object> values) {
    if (values == null)
      return null;

    Set<String> stringSet = new HashSet<>();
    for (Object value : values)
      stringSet.add(String.valueOf(value));

    return stringSet;
  }
}
