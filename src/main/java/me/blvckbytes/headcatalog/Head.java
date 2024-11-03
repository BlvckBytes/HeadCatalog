package me.blvckbytes.headcatalog;

import java.util.List;

public record Head(
  String name,

  String category,
  String normalizedCategory,
  int numericCategory,

  List<String> tags,
  String base64Textures
) {}
