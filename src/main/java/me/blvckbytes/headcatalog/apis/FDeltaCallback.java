package me.blvckbytes.headcatalog.apis;

import java.util.Collection;

@FunctionalInterface
public interface FDeltaCallback {

  void accept(Collection<HeadModel> heads, EDeltaMode mode);

}
