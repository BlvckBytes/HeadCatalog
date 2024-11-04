package me.blvckbytes.headcatalog.index;

import me.blvckbytes.headcatalog.Head;

public record WordBucketEntry(Head head, WordType wordType) {

  @Override
  public String toString() {
    return "WordBucketEntry(wordType=" + wordType + ", head=" + head + ")";
  }
}
