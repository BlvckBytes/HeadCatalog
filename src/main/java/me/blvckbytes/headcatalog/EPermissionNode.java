package me.blvckbytes.headcatalog;

import me.blvckbytes.bukkitcommands.config.IPermissionNode;

public enum EPermissionNode implements IPermissionNode {

  OPEN("open", "headcatalog.open")
  ;

  private final String internalName;
  private final String fallbackNode;

  EPermissionNode(String internalName, String fallbackNode) {
    this.internalName = internalName;
    this.fallbackNode = fallbackNode;
  }

  @Override
  public String getInternalName() {
    return this.internalName;
  }

  @Override
  public String getFallbackNode() {
    return this.fallbackNode;
  }
}
