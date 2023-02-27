package me.blvckbytes.headcatalog.config;

import me.blvckbytes.gpeee.functions.AExpressionFunction;
import me.blvckbytes.gpeee.functions.ExpressionFunctionArgument;
import me.blvckbytes.gpeee.interpreter.IEvaluationEnvironment;
import org.jetbrains.annotations.Nullable;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

public class SkinUrlToBase64Function extends AExpressionFunction {

  @Override
  public Object apply(IEvaluationEnvironment environment, List<@Nullable Object> args) {
    String skinUrl = nonNull(args, 0);
    String textureJson = "{\"textures\":{\"SKIN\":{\"url\":\"" + skinUrl + "\"}}}";
    return Base64.getEncoder().encodeToString(textureJson.getBytes(StandardCharsets.UTF_8));
  }

  @Override
  public @Nullable List<ExpressionFunctionArgument> getArguments() {
    List<ExpressionFunctionArgument> arguments = new ArrayList<>();

    arguments.add(new ExpressionFunctionArgument("input", "Input skin URL string", true, String.class));

    return arguments;
  }
}
