package me.blvckbytes.headcatalog;

public class NanoTimer {

  public static double timeExecutionMs(Runnable executable) {
    long start = System.nanoTime();
    executable.run();
    return Math.round((System.nanoTime() - start) / 1000.0 / 1000.0 * 100.0) / 100.0;
  }
}
