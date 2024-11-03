package me.blvckbytes.headcatalog;

import me.blvckbytes.gpeee.interpreter.EvaluationEnvironmentBuilder;
import me.blvckbytes.syllables_matcher.Syllables;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class CatalogHead {

  /*
    When matching, assign a numeric score to results
    Sort syllables by length ascending
    Those who match nearer to the beginning have a higher score

    as to match Alphabet A with A being A, not A in apple, for example.
   */

  private final EvaluationEnvironmentBuilder headEnvironmentBuilder;

  public final Head handle;

  public final Syllables nameSyllables;
  public final Syllables categorySyllables;
  public final Syllables allTagsSyllables;

  public CatalogHead(Head head) {
    this.handle = head;

    this.nameSyllables = getSyllables(head.name());
    this.categorySyllables = getSyllables(head.category());
    this.allTagsSyllables = getSyllables(concatWithDelimiter(head.tags()));

    this.headEnvironmentBuilder = new EvaluationEnvironmentBuilder()
      .withStaticVariable("head_name", head.name())
      .withStaticVariable("head_category", head.category())
      .withStaticVariable("head_tags", head.tags())
      .withStaticVariable("head_base64_textures", head.base64Textures());
  }

  public EvaluationEnvironmentBuilder getHeadEnvironmentBuilder() {
    return headEnvironmentBuilder.duplicate();
  }

  private static @NotNull Syllables getSyllables(String input) {
    var result = Syllables.forString(false, normalize(input), Syllables.DELIMITER_SEARCH_PATTERN);

    // Since inputs do not process wildcards, there cannot be any errors
    assert result.syllables() != null;

    return result.syllables();
  }

  private static String concatWithDelimiter(List<String> items) {
    var result = new StringBuilder();

    for (var i = 0; i < items.size(); ++i) {
      if (i != 0)
        result.append(Syllables.DELIMITER_SEARCH_PATTERN);

      result.append(items.get(i));
    }

    return result.toString();
  }

  private static String normalize(String input) {
    var result = input.toCharArray();

    for (var i = 0; i < result.length; ++i) {
      var c = result[i];

      var newChar = switch (c) {
        case ' ', '_' -> '-';
        default -> c;
      };

      if (newChar != c)
        result[i] = newChar;
    }

    return new String(result);
  }
}
