package me.blvckbytes.headcatalog.command;

import me.blvckbytes.autowirer.ICleanable;
import me.blvckbytes.autowirer.IInitializable;
import me.blvckbytes.bukkitboilerplate.PlayerCommand;
import me.blvckbytes.headcatalog.config.HeadCatalogCommandSection;
import me.blvckbytes.headcatalog.gui.*;
import me.blvckbytes.headcatalog.heads.Head;
import me.blvckbytes.headcatalog.heads.IHeadManager;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.stream.Collectors;

public class HeadCatalogCommand extends PlayerCommand implements IInitializable, ICleanable {

  private final InventoryRegistry inventoryRegistry;
  private final IHeadManager headManager;

  private List<DataBoundUISlot<Head>> headSlots;

  public HeadCatalogCommand(
    HeadCatalogCommandSection commandSection,
    IHeadManager headManager,
    InventoryRegistry inventoryRegistry
  ) {
    super(commandSection);
    this.inventoryRegistry = inventoryRegistry;
    this.headManager = headManager;
  }

  @Override
  protected void onPlayerExecution(Player player, String s, String[] strings) {
    if (this.headSlots == null) {
      player.sendMessage("§cHeads aren't ready yet");
      return;
    }

    // FIXME: This generics warning SUCKS
    AnvilSearchUI<Head> ui = inventoryRegistry.createInventory(AnvilSearchUI.class, new AnvilSearchParameter<>(player, this::applyHeadsFilter, HeadModelSearchFilter.HEAD_EVERYWHERE));
//    SingleChoiceUI ui = inventoryRegistry.createInventory(SingleChoiceUI.class, new SingleChoiceParameter(player));
    ui.show();
//    ui.setPageableSlots(this.headSlots);
  }

  private List<DataBoundUISlot<Head>> applyHeadsFilter(ISearchFilterEnum<?, Head> searchFilter, String text) {
    if (this.headSlots == null)
      return new ArrayList<>();

    String[] searchWords = text.toLowerCase(Locale.ROOT).split(" ");
    Map<DataBoundUISlot<Head>, Integer> results = new HashMap<>();

    for (DataBoundUISlot<Head> headSlot : this.headSlots) {
      String[] texts = searchFilter.getTexts().apply(headSlot.data);
      int diff = calculateDifference(searchWords, texts);

      if (diff >= 0)
        results.put(headSlot, diff);
    }

    return results.entrySet().stream()
      .sorted(Comparator.comparingInt(Map.Entry::getValue))
      .map(Map.Entry::getKey)
      .collect(Collectors.toList());
  }

  /**
   * Calculates a number which represents the difference between all available words
   * within the list of texts and the search words, where every text word may only match once.
   * @param words Words to match
   * @param texts Texts to search in
   * @return Difference, < 0 if there was no match for all words
   */
  private int calculateDifference(String[] words, String... texts) {
    // Create a list of unique words which all of the texts contain
    Set<String> availWords = new HashSet<>();
    for (String text : texts) {

      // Account for provider errors
      if (text == null)
        continue;

      for (String textWord : text.split(" "))
        availWords.add(textWord.toLowerCase());
    }

    // Iterate all words and count sum the total diff
    int totalDiff = 0;
    for (String word : words) {

      // Find the best match for the current word in all remaining words
      String bestMatch = null;
      int bestMatchDiff = Integer.MAX_VALUE;
      for (String availWord : availWords) {

        // Not containing the target word
        int index = availWord.indexOf(word.toLowerCase());
        if (index < 0)
          continue;

        // The difference is determined by how many other chars are padding the search word
        int diff = availWord.length() - word.length();

        // Compare and update the local best match
        if (diff < bestMatchDiff) {
          bestMatchDiff = diff;
          bestMatch = availWord;
        }
      }

      // No match found for the current word, all words need to match, thus cancel
      if (bestMatch == null)
        return -1;

      // Remove the matching word from the list and add it's difference to the total
      availWords.remove(bestMatch);
      totalDiff += bestMatchDiff;
    }

    return totalDiff;
  }

  private void updateHeads(Collection<Head> heads) {
    // TODO: Notify UIs to update their items also
    this.headSlots = new ArrayList<>();

    for (Head head : heads) {
      headSlots.add(new DataBoundUISlot<>(() -> head.item, interaction -> {
        interaction.ui.getViewer().getInventory().addItem(head.item);
        interaction.ui.close();
        return null;
      }, head));
    }
  }

  @Override
  public void cleanup() {
    this.headManager.unregisterUpdateCallback(this::updateHeads);
  }

  @Override
  public void initialize() {
    this.headManager.registerUpdateCallback(this::updateHeads);
  }
}
