package me.blvckbytes.headcatalog.gui;

import me.blvckbytes.headcatalog.gui.config.AUIParameter;
import me.blvckbytes.headcatalog.gui.config.ISingleChoiceParameterProvider;
import org.bukkit.entity.Player;

public class SingleChoiceParameter extends AUIParameter<ISingleChoiceParameterProvider> {

  public SingleChoiceParameter(ISingleChoiceParameterProvider provider, Player viewer) {
    super(provider, viewer);
  }
}
