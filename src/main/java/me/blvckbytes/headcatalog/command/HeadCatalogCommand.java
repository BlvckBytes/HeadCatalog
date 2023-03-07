package me.blvckbytes.headcatalog.command;

import me.blvckbytes.autowirer.ICleanable;
import me.blvckbytes.autowirer.IInitializable;
import me.blvckbytes.bukkitcommands.PlayerCommand;
import me.blvckbytes.bukkitinventoryui.IInventoryRegistry;
import me.blvckbytes.bukkitinventoryui.anvilsearch.IAnvilSearchParameterProvider;
import me.blvckbytes.bukkitinventoryui.anvilsearch.ISearchFilterEnum;
import me.blvckbytes.bukkitinventoryui.base.DataBoundUISlot;
import me.blvckbytes.bukkitinventoryui.singlechoice.ISingleChoiceParameterProvider;
import me.blvckbytes.bukkitinventoryui.singlechoice.SingleChoiceParameter;
import me.blvckbytes.headcatalog.config.HeadCatalogCommandSection;
import me.blvckbytes.headcatalog.ui.*;
import me.blvckbytes.headcatalog.heads.Head;
import me.blvckbytes.headcatalog.heads.IHeadManager;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.*;
import java.util.stream.Collectors;

public class HeadCatalogCommand extends PlayerCommand implements IInitializable, ICleanable, Listener {

  private final IInventoryRegistry inventoryRegistry;
  private final IHeadManager headManager;

  private List<DataBoundUISlot<Head>> headSlots;
  private final IAnvilSearchParameterProvider anvilSearchProvider;
  private final ISingleChoiceParameterProvider singleChoiceProvider;

  private final Map<Player, HeadSingleChoiceUI> headUiByPlayer;

  public HeadCatalogCommand(
    HeadCatalogCommandSection commandSection,
    IHeadManager headManager,
    IInventoryRegistry inventoryRegistry,
    IAnvilSearchParameterProvider anvilSearchProvider,
    ISingleChoiceParameterProvider singleChoiceProvider
  ) {
    super(commandSection);
    this.inventoryRegistry = inventoryRegistry;
    this.headManager = headManager;
    this.anvilSearchProvider = anvilSearchProvider;
    this.singleChoiceProvider = singleChoiceProvider;
    this.headUiByPlayer = new HashMap<>();
  }

  @Override
  protected List<String> onTabComplete(CommandSender commandSender, String s, String[] strings) {
    return EMPTY_STRING_LIST;
  }

  @Override
  protected void onPlayerInvocation(Player player, String alias, String[] args) {
    if (this.headSlots == null) {
      player.sendMessage("§cHeads aren't ready yet");
      return;
    }

    createOrGetHeadUI(player).show();
  }

  private HeadSingleChoiceUI createOrGetHeadUI(Player player) {
    HeadSingleChoiceUI singleChoiceUI = headUiByPlayer.get(player);

    if (singleChoiceUI == null) {
      SingleChoiceParameter<Head> singleChoiceParameter = new SingleChoiceParameter<>(
        singleChoiceProvider, player,
        anvilSearchProvider, HeadModelSearchFilter.HEAD_EVERYWHERE, this::applyHeadsFilter
      );

      singleChoiceUI = new HeadSingleChoiceUI(inventoryRegistry, singleChoiceParameter);
      singleChoiceUI.setPageableSlots(this.headSlots);
      this.headUiByPlayer.put(player, singleChoiceUI);
    }

    return singleChoiceUI;
  }

  private List<DataBoundUISlot<Head>> applyHeadsFilter(ISearchFilterEnum<?, Head> searchFilter, String text) {
    if (this.headSlots == null)
      return new ArrayList<>();

    String[] searchWords = text.toLowerCase(Locale.ROOT).split(" ");
    Map<DataBoundUISlot<Head>, Integer> results = new HashMap<>();

    for (DataBoundUISlot<Head> slotItem : this.headSlots) {
      String[] texts = searchFilter.getTexts().apply(slotItem.data);
      int diff = calculateDifference(searchWords, texts);

      if (diff >= 0)
        results.put(slotItem, diff);
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
    List<DataBoundUISlot<Head>> result = new ArrayList<>();
    for (Head head : heads) {
      result.add(new DataBoundUISlot<>(() -> head.item, interaction -> {
        interaction.ui.getViewer().getInventory().addItem(head.item);
        interaction.ui.close();
        return null;
      }, head));
    }

    this.headSlots = result;
    for (HeadSingleChoiceUI ui : this.headUiByPlayer.values())
      ui.setPageableSlots(this.headSlots);
  }

  @Override
  public void cleanup() {
    this.headManager.unregisterUpdateCallback(this::updateHeads);
  }

  @Override
  public void initialize() {
    this.headManager.registerUpdateCallback(this::updateHeads);
  }

  @EventHandler
  public void onQuit(PlayerQuitEvent event) {
    this.headUiByPlayer.remove(event.getPlayer());
  }
}
