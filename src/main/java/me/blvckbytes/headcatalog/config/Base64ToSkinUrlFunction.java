package me.blvckbytes.headcatalog.config;

import com.google.gson.JsonParser;
import me.blvckbytes.gpeee.functions.AExpressionFunction;
import me.blvckbytes.gpeee.functions.ExpressionFunctionArgument;
import me.blvckbytes.gpeee.interpreter.IEvaluationEnvironment;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

public class Base64ToSkinUrlFunction extends AExpressionFunction {

  private final JsonParser jsonParser;

  public Base64ToSkinUrlFunction() {
    this.jsonParser = new JsonParser();
  }

  @Override
  public Object apply(IEvaluationEnvironment environment, List<@Nullable Object> args) {
    String base64String = nonNull(args, 0);

    byte[] decodedBytes = Base64.getDecoder().decode(base64String);
    String decodedString = new String(decodedBytes);

    // {"textures":{"SKIN":{"url": ?}}}
    return this.jsonParser.parse(decodedString)
      .getAsJsonObject()
      .getAsJsonObject("textures")
      .getAsJsonObject("SKIN")
      .get("url")
      .getAsString();
  }

  @Override
  public @Nullable List<ExpressionFunctionArgument> getArguments() {
    List<ExpressionFunctionArgument> arguments = new ArrayList<>();

    arguments.add(new ExpressionFunctionArgument("input", "Input base64 string", true, String.class));

    return arguments;
  }
}
