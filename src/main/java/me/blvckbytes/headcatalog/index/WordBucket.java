package me.blvckbytes.headcatalog.index;

import java.util.ArrayList;
import java.util.List;

public class WordBucket {

  public final String word;
  public final List<WordBucketEntry> entries;

  public WordBucket(String word) {
    this.word = word;
    this.entries = new ArrayList<>();
  }

  @Override
  public String toString() {
    var result = new StringBuilder("WordBucket(word=" + word + ", entries=[");

    for (var entry : entries)
      result.append('\n').append(entry.toString());

    result.append("\n])");
    return result.toString();
  }
}
