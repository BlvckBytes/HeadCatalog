package me.blvckbytes.headcatalog.gui;

import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;

@FunctionalInterface
public interface IInteractionHandler {

  @Nullable EnumSet<EClickResultFlag> handle(UIInteraction action);

}
