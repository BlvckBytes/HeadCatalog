package me.blvckbytes.headcatalog.heads;

import me.blvckbytes.autowirer.ICleanable;
import me.blvckbytes.autowirer.IInitializable;
import me.blvckbytes.bukkitevaluable.IItemBuildable;
import me.blvckbytes.gpeee.functions.AExpressionFunction;
import me.blvckbytes.gpeee.interpreter.EvaluationEnvironmentBuilder;
import me.blvckbytes.gpeee.interpreter.IEvaluationEnvironment;
import me.blvckbytes.headcatalog.config.SkinUrlToBase64Function;
import me.blvckbytes.headcatalog.apis.HeadModel;
import me.blvckbytes.headcatalog.apis.IHeadApisManager;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Consumer;

public class HeadManager implements IHeadManager, IInitializable, ICleanable {

  private final Set<Consumer<Collection<Head>>> updateConsumers;
  private final IItemBuildable representativeItem;
  private final AExpressionFunction skinUrlToBase64Function;
  private final IHeadApisManager apisManager;

  private @Nullable Collection<Head> headsUnmodifiable;

  public HeadManager(
    IHeadApisManager apisManager,
    IRepresentativeProvider representativeProvider
  ) {
    this.updateConsumers = new HashSet<>();
    this.apisManager = apisManager;
    this.representativeItem = representativeProvider.getRepresentative().asItem();
    this.skinUrlToBase64Function = new SkinUrlToBase64Function();
  }

  @Override
  public @Nullable Collection<Head> getHeadsUnmodifiable() {
    return this.headsUnmodifiable;
  }

  @Override
  public void registerUpdateCallback(Consumer<Collection<Head>> consumer) {
    updateConsumers.add(consumer);
  }

  @Override
  public void unregisterUpdateCallback(Consumer<Collection<Head>> consumer) {
    updateConsumers.remove(consumer);
  }

  private void notifyUpdateConsumers() {
    Collection<Head> heads = getHeadsUnmodifiable();
    for (Consumer<Collection<Head>> consumer : updateConsumers)
      consumer.accept(heads);
  }

  private IEvaluationEnvironment getHeadItemEnvironment(HeadModel headModel) {
    return new EvaluationEnvironmentBuilder()
      .withStaticVariable("head", headModel)
      .withFunction("skin_url_to_base64", skinUrlToBase64Function)
      .build();
  }

  private void onHeadModelsUpdate(Collection<HeadModel> headModels) {
    List<Head> heads = new ArrayList<>();

    for (HeadModel headModel : headModels) {
      IEvaluationEnvironment environment = getHeadItemEnvironment(headModel);
      ItemStack item = representativeItem.build(environment);
      heads.add(new Head(headModel, item));
    }

    headsUnmodifiable = Collections.unmodifiableCollection(heads);
    notifyUpdateConsumers();
  }

  @Override
  public void cleanup() {
    this.apisManager.unregisterUpdateCallback(this::onHeadModelsUpdate);
  }

  @Override
  public void initialize() {
    this.apisManager.registerUpdateCallback(this::onHeadModelsUpdate);
  }
}
