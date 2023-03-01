package me.blvckbytes.headcatalog.gui;

public enum EClickResultFlag {
  ALLOW_ALL,
  ALLOW_PICKUP,
  ALLOW_PLACE
  ;

  public boolean isCancelling(UIInteraction interaction) {
    // TODO: Implement
    return true;
  }
}
