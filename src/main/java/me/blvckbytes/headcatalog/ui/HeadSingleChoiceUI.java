package me.blvckbytes.headcatalog.ui;

import me.blvckbytes.bukkitinventoryui.IInventoryRegistry;
import me.blvckbytes.bukkitinventoryui.singlechoice.SingleChoiceParameter;
import me.blvckbytes.bukkitinventoryui.singlechoice.SingleChoiceUI;
import me.blvckbytes.headcatalog.heads.Head;

public class HeadSingleChoiceUI extends SingleChoiceUI<Head> {

  public HeadSingleChoiceUI(IInventoryRegistry registry, SingleChoiceParameter<Head> parameter) {
    super(registry, parameter);
  }
}
