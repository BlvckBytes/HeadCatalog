package me.blvckbytes.headcatalog.index;

import me.blvckbytes.headcatalog.AsyncHeadRegistryLoadEvent;
import me.blvckbytes.headcatalog.Head;
import me.blvckbytes.headcatalog.HeadRegistry;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.*;
import java.util.logging.Logger;

public class HeadIndex implements Listener {

  private final HeadRegistry headRegistry;
  private final Logger logger;
  private final List<WordBucket> wordBuckets;

  public HeadIndex(HeadRegistry headRegistry, Logger logger) {
    this.headRegistry = headRegistry;
    this.logger = logger;
    this.wordBuckets = new ArrayList<>();
  }

  @EventHandler
  public void onRegistryLoad(AsyncHeadRegistryLoadEvent event) {
    var start = System.nanoTime();
    logger.info("Building the head-index for " + headRegistry.heads.size() + " heads...");

    for (var head : headRegistry.heads) {
      addWordBucketEntries(extractLowerCaseWords(head.name), head, WordType.NAME);
      addWordBucketEntries(extractLowerCaseWords(head.category), head, WordType.CATEGORY);

      for (var tag : head.tags)
        addWordBucketEntries(extractLowerCaseWords(tag), head, WordType.TAG);
    }

    var end = System.nanoTime();
    logger.info("Finished building the head-index; took " + Math.round((end - start) / 1000.0 / 1000.0) + "ms");
  }

  private int binarySearchBucketIndex(String word) {
    int low = 0;
    int high = wordBuckets.size() - 1;

    while (low <= high) {
      int mid = (low + high) >>> 1;
      WordBucket midVal = wordBuckets.get(mid);

      int comparisonResult;

      if ((comparisonResult = Integer.compare(midVal.word.length(), word.length())) == 0)
        comparisonResult = midVal.word.compareTo(word);

      if (comparisonResult < 0)
        low = mid + 1;
      else if (comparisonResult > 0)
        high = mid - 1;
      else
        return mid;
    }

    return -(low + 1);
  }

  private WordBucket locateOrCreateBucket(String word) {
    var wordBucket = new WordBucket(word);
    var bucketIndex = binarySearchBucketIndex(word);

    if (bucketIndex < 0)
      wordBuckets.add(-bucketIndex - 1, wordBucket);
    else
      wordBucket = wordBuckets.get(bucketIndex);

    return wordBucket;
  }

  private void addWordBucketEntries(List<String> words, Head head, WordType wordType) {
    for (var word : words) {
      var wordBucket = locateOrCreateBucket(word);
      wordBucket.entries.add(new WordBucketEntry(head, wordType));
    }
  }

  public List<Head> search(Player player, String query, EnumSet<WordType> wordTypes, boolean matchAllWords) {
    var start = System.nanoTime();
    var permissionCacheByNumericCategory = new Boolean[headRegistry.normalizedCategories.size()];
    var results = new ArrayList<Head>();

    for (var result : filterHeads(query, wordTypes, matchAllWords)) {
      if (!hasPermissionForHead(player, result, permissionCacheByNumericCategory))
        continue;

      results.add(result);
    }

    var end = System.nanoTime();
    logger.info("Search took " + (end - start) / 1000.0 / 1000.0);

    return results;
  }

  private Collection<Head> filterHeads(String query, EnumSet<WordType> wordTypes, boolean matchAllWords) {
    if (query.isBlank())
      return headRegistry.heads;

    var words = extractLowerCaseWords(query);
    var resultGroups = new ArrayList<Set<Head>>(words.size());

    for (var word : words) {
      // All results for this word will have the same index, thus the same priority, so
      // we can unique-ify at this stage already, as the same head may have ended up multiple
      // times in the same bucket.
      var wordResults = new HashSet<Head>();
      var bucketIndex = binarySearchBucketIndex(word);

      WordBucket wordBucket;

      if (bucketIndex < 0) {
        var lastLowIndex = -bucketIndex - 1;

        wordBucket = wordBuckets.get(lastLowIndex);

        logger.info("lastLowIndex: " + wordBucket.word);

        // Allow for buckets which start with this word, not just direct matches
        // TODO: If that works, try to expand outwards, top and bottom, for word-buckets whose
        //       bucket.word.charAt(word.length() - 1) == word.charAt(word.length() - 1), meaning
        //       that they also start with this word. Drawback: There could be more longer words
        //       at a further index... maybe also group buckets by length, and conduct the above
        //       on each group, as to get all starting matches?
        // TODO: That doesn't work... :')
        if (!wordBucket.word.startsWith(word)) {
          // Fast-path: if all words need to match, and there's no bucket for
          // this word, then no result can ever be a full-match
          if (matchAllWords)
            return List.of();

          continue;
        }

        logger.info("startsWith: " + wordBucket.word + " with " + word);
      }

      else
        wordBucket = wordBuckets.get(bucketIndex);

      for (var bucketEntry : wordBucket.entries) {
        // Does not search through words of this type, skip result
        if (!wordTypes.contains(bucketEntry.wordType()))
          continue;

        wordResults.add(bucketEntry.head());
      }

      if (wordResults.isEmpty()) {
        // Fast-path: if all words need to match, and there are no results for
        // this word, then no result can ever be a full-match
        if (matchAllWords)
          return List.of();
      }

      resultGroups.add(wordResults);
    }

    // Fast-path: only a single word has been searched for
    if (resultGroups.size() == 1)
      return resultGroups.get(0);

    // Take the unique list of all results
    if (!matchAllWords) {
      var result = new HashSet<Head>();
      resultGroups.forEach(result::addAll);
      return result;
    }

    // If all words need to match, take only the heads which are in all result-groups.

    var result = resultGroups.get(0);

    for (var i = 1; i < resultGroups.size(); ++i)
      result.retainAll(resultGroups.get(i));

    return result;
  }

  private boolean hasPermissionForHead(Player player, Head head, Boolean[] permissionCacheByNumericCategory) {
    Boolean hasPermission;

    if ((hasPermission = permissionCacheByNumericCategory[head.numericCategory]) == null) {
      hasPermission = player.hasPermission("headcatalog.category." + head.normalizedCategory);
      permissionCacheByNumericCategory[head.numericCategory] = hasPermission;
    }

    return hasPermission;
  }

  private List<String> extractLowerCaseWords(String input) {
    var result = new ArrayList<String>();

    var wordBeginCharIndex = -1;
    var lastNonSpaceCharIndex = -1;

    var queryLength = input.length();
    for (var i = 0; i < queryLength; ++i) {
      var currentChar = input.charAt(i);
      var isSpace = currentChar == ' ';

      if (!isSpace) {
        lastNonSpaceCharIndex = i;

        if (wordBeginCharIndex < 0)
          wordBeginCharIndex = i;
      }

      if (isSpace || i == queryLength - 1) {
        if (wordBeginCharIndex < 0)
          continue;

        var word = input.substring(wordBeginCharIndex, lastNonSpaceCharIndex + 1);

        result.add(word.toLowerCase());
        wordBeginCharIndex = -1;
      }
    }

    return result;
  }
}
