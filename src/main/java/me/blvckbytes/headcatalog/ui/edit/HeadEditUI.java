package me.blvckbytes.headcatalog.ui.edit;

import me.blvckbytes.bukkitinventoryui.IInventoryRegistry;
import me.blvckbytes.bukkitinventoryui.base.*;
import me.blvckbytes.gpeee.interpreter.IEvaluationEnvironment;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;

public class HeadEditUI implements IInventoryUI {

  private static final String
    KEY_REPRESENTATIVE = "representative",
    KEY_BLOCK_TOGGLE = "blockToggle",
    KEY_CATEGORIES = "categories",
    KEY_TAGS = "tags",
    KEY_NAME = "name",
    KEY_PRICE = "price";

  private final BaseInventoryUI handle;
  private final HeadEditUIParameter parameter;
  private final IInventoryRegistry registry;

  public HeadEditUI(HeadEditUIParameter parameter, IInventoryRegistry registry) {
    this.parameter = parameter;
    this.registry = registry;
    this.handle = new BaseInventoryUI(parameter.provider, this::createInventory, parameter.viewer, null);
  }

  private void setHeadUISlots() {
    this.handle.setSlotByName(KEY_REPRESENTATIVE, new UISlot(() -> parameter.target.item));
    this.handle.drawSlotByName(KEY_REPRESENTATIVE);

    this.handle.setSlotByName(KEY_BLOCK_TOGGLE, new UISlot(() -> parameter.provider.getBlockToggle().build(parameter.target.environment), this::handleBlockToggleClick));
    this.handle.drawSlotByName(KEY_BLOCK_TOGGLE);

    this.handle.setSlotByName(KEY_CATEGORIES, new UISlot(() -> parameter.provider.getCategories().build(parameter.target.environment), this::handleCategoriesClick));
    this.handle.drawSlotByName(KEY_CATEGORIES);

    this.handle.setSlotByName(KEY_TAGS, new UISlot(() -> parameter.provider.getTags().build(parameter.target.environment), this::handleTagsClick));
    this.handle.drawSlotByName(KEY_TAGS);

    this.handle.setSlotByName(KEY_NAME, new UISlot(() -> parameter.provider.getName().build(parameter.target.environment), this::handleNameClick));
    this.handle.drawSlotByName(KEY_NAME);

    this.handle.setSlotByName(KEY_PRICE, new UISlot(() -> parameter.provider.getPrice().build(parameter.target.environment), this::handlePriceClick));
    this.handle.drawSlotByName(KEY_PRICE);
  }

  private EnumSet<EClickResultFlag> handlePriceClick(UIInteraction action) {
    System.out.println("price");
    return null;
  }

  private EnumSet<EClickResultFlag> handleNameClick(UIInteraction action) {
    System.out.println("name");
    return null;
  }

  private EnumSet<EClickResultFlag> handleTagsClick(UIInteraction action) {
    System.out.println("tags");
    return null;
  }

  private EnumSet<EClickResultFlag> handleCategoriesClick(UIInteraction action) {
    System.out.println("categories");
    return null;
  }

  private EnumSet<EClickResultFlag> handleBlockToggleClick(UIInteraction action) {
    // TODO: This action should invoke a store call and update all head list viewers
    parameter.target.model.blocked = !parameter.target.model.blocked;
    drawSlotByName(KEY_BLOCK_TOGGLE);
    return null;
  }

  private Inventory createInventory(String title) {
    return Bukkit.createInventory(null, parameter.provider.getNumberOfRows() * 9, title);
  }

  @Override
  public void setSlotById(int slot, @Nullable UISlot value) {
    this.handle.setSlotById(slot, value);
  }

  public void setSlotByName(String name, UISlot value) {
    this.handle.setSlotByName(name, value);
  }

  public void drawSlotById(int slot) {
    this.handle.drawSlotById(slot);
  }

  public void drawSlotByName(String name) {
    this.handle.drawSlotByName(name);
  }

  public void setItem(int slot, ItemStack item) {
    this.handle.setItem(slot, item);
  }

  public @Nullable ItemStack getItem(int slot) {
    return this.handle.getItem(slot);
  }

  public void handleInteraction(UIInteraction interaction) {
    this.handle.handleInteraction(interaction);
  }

  public void handleClose() {
    this.handle.handleClose();
  }

  public void show() {
    this.registry.registerUI(this);
    this.handle.show();
    this.setHeadUISlots();
  }

  public void close() {
    this.handle.close();
  }

  public Inventory getInventory() {
    return this.handle.getInventory();
  }

  public Player getViewer() {
    return this.handle.getViewer();
  }

  public IEvaluationEnvironment getInventoryEnvironment() {
    return this.handle.getInventoryEnvironment();
  }

  public boolean isOpen() {
    return this.handle.isOpen();
  }
}
