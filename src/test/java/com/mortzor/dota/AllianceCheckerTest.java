package com.mortzor.dota;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.*;

class AllianceCheckerTest {

  AllianceCheckerTest() throws IOException {}

  // Change these
  private static int threads = 11;
  private static int printLinePer = 10000000;
  private static int desiredSynergyLevel = 12;


  // current list of all allies. Comment a line out to not include them in the calculation
  private final String allHeroesString = "[" +
      "{\"unit\":\"Abaddon\",\"type1\":\"Heartless\",\"type2\":\"Knight\",\"type3\":\"\"}," +
      "{\"unit\":\"Arc warden\",\"type1\":\"Primordial\",\"type2\":\"Summoner\",\"type3\":\"\"}," +
      "{\"unit\":\"Axe\",\"type1\":\"Brawny\",\"type2\":\"Brute\",\"type3\":\"\"}," +
      "{\"unit\":\"Batrider\",\"type1\":\"Troll\",\"type2\":\"Knight\",\"type3\":\"\"}," +
      "{\"unit\":\"Beastmaster\",\"type1\":\"Brawny\",\"type2\":\"Hunter\",\"type3\":\"\"}," +
      "{\"unit\":\"Bloodseeker\",\"type1\":\"Bloodbound\",\"type2\":\"Deadeye\",\"type3\":\"\"}," +
      "{\"unit\":\"Bristleback\",\"type1\":\"Brawny\",\"type2\":\"Savage\",\"type3\":\"\"}," +
      "{\"unit\":\"Broodmother\",\"type1\":\"Insect\",\"type2\":\"Warlocks\",\"type3\":\"\"}," +
      "{\"unit\":\"Chaos knight\",\"type1\":\"Demon\",\"type2\":\"Knight\",\"type3\":\"\"}," +
      "{\"unit\":\"Crystal maiden\",\"type1\":\"Human\",\"type2\":\"Mage\",\"type3\":\"\"}," +
      "{\"unit\":\"Dazzle\",\"type1\":\"Troll\",\"type2\":\"Healer\",\"type3\":\"\"}," +
      "{\"unit\":\"Disruptor\",\"type1\":\"Brawny\",\"type2\":\"Warlocks\",\"type3\":\"\"}," +
      "{\"unit\":\"Doom\",\"type1\":\"Demon\",\"type2\":\"Brute\",\"type3\":\"\"}," +
      "{\"unit\":\"Dragon knight\",\"type1\":\"Human\",\"type2\":\"Dragon\",\"type3\":\"Knight\"}," +
      "{\"unit\":\"Drow ranger\",\"type1\":\"Heartless\",\"type2\":\"Vigilant\",\"type3\":\"Hunter\"}," +
      "{\"unit\":\"Earth spirit\",\"type1\":\"Spirit\",\"type2\":\"Warrior\",\"type3\":\"\"}," +
      "{\"unit\":\"Ember spirit\",\"type1\":\"Spirit\",\"type2\":\"Assassin\",\"type3\":\"\"}," +
      "{\"unit\":\"Enigma\",\"type1\":\"Primordial\",\"type2\":\"Void\",\"type3\":\"\"}," +
      "{\"unit\":\"Faceless void\",\"type1\":\"Void\",\"type2\":\"Assassin\",\"type3\":\"\"}," +
      "{\"unit\":\"Io\",\"type1\":\"Primordial\",\"type2\":\"Druid\",\"type3\":\"\"}," +
      "{\"unit\":\"Juggernaut\",\"type1\":\"Brawny\",\"type2\":\"Warrior\",\"type3\":\"\"}," +
      "{\"unit\":\"Keeper of the light\",\"type1\":\"Human\",\"type2\":\"Mage\",\"type3\":\"\"}," +
      "{\"unit\":\"Legion\",\"type1\":\"Human\",\"type2\":\"Champion\",\"type3\":\"\"}," +
      "{\"unit\":\"Lich\",\"type1\":\"Heartless\",\"type2\":\"Mage\",\"type3\":\"\"}," +
      "{\"unit\":\"Lifestealer\",\"type1\":\"Heartless\",\"type2\":\"Brute\",\"type3\":\"\"}," +
      "{\"unit\":\"Lone druid\",\"type1\":\"Savage\",\"type2\":\"Druid\",\"type3\":\"Summoner\"}," +
      "{\"unit\":\"Luna\",\"type1\":\"Knight\",\"type2\":\"Vigilant\",\"type3\":\"\"}," +
      "{\"unit\":\"Lycan\",\"type1\":\"Human\",\"type2\":\"Savage\",\"type3\":\"Summoner\"}," +
      "{\"unit\":\"Magnus\",\"type1\":\"Savage\",\"type2\":\"Druid\",\"type3\":\"\"}," +
      "{\"unit\":\"Medusa\",\"type1\":\"Scaled\",\"type2\":\"Hunter\",\"type3\":\"\"}," +
      "{\"unit\":\"Mirana\",\"type1\":\"Vigilant\",\"type2\":\"Hunter\",\"type3\":\"\"}," +
      "{\"unit\":\"Morphling\",\"type1\":\"Primordial\",\"type2\":\"Mage\",\"type3\":\"\"}," +
      "{\"unit\":\"Nature's prophet\",\"type1\":\"Summoner\",\"type2\":\"Druid\",\"type3\":\"\"}," +
      "{\"unit\":\"Necrophos\",\"type1\":\"Heartless\",\"type2\":\"Warlocks\",\"type3\":\"Healer\"}," +
      "{\"unit\":\"Nyx assassin\",\"type1\":\"Insect\",\"type2\":\"Assassin\",\"type3\":\"\"}," +
      "{\"unit\":\"Ogre magi\",\"type1\":\"Bloodbound\",\"type2\":\"Brute\",\"type3\":\"Mage\"}," +
      "{\"unit\":\"Omniknight\",\"type1\":\"Human\",\"type2\":\"Knight\",\"type3\":\"Healer\"}," +
      "{\"unit\":\"Pudge\",\"type1\":\"Heartless\",\"type2\":\"Warrior\",\"type3\":\"\"}," +
      "{\"unit\":\"Queen of pain\",\"type1\":\"Demon\",\"type2\":\"Assassin\",\"type3\":\"\"}," +
      "{\"unit\":\"Razor\",\"type1\":\"Primordial\",\"type2\":\"Mage\",\"type3\":\"\"}," +
      "{\"unit\":\"Sand king\",\"type1\":\"Savage\",\"type2\":\"Insect\",\"type3\":\"\"}," +
      "{\"unit\":\"Shadow demon\",\"type1\":\"Heartless\",\"type2\":\"Demon\",\"type3\":\"\"}," +
      "{\"unit\":\"Shadow fiend\",\"type1\":\"Demon\",\"type2\":\"Warlocks\",\"type3\":\"\"}," +
      "{\"unit\":\"Shadow shaman\",\"type1\":\"Troll\",\"type2\":\"Summoner\",\"type3\":\"\"}," +
      "{\"unit\":\"Slardar\",\"type1\":\"Scaled\",\"type2\":\"Warrior\",\"type3\":\"\"}," +
      "{\"unit\":\"Slark\",\"type1\":\"Scaled\",\"type2\":\"Assassin\",\"type3\":\"\"}," +
      "{\"unit\":\"Snapfire\",\"type1\":\"Brawny\",\"type2\":\"Dragon\",\"type3\":\"\"}," +
      "{\"unit\":\"Storm Spirit\",\"type1\":\"Spirit\",\"type2\":\"Mage\",\"type3\":\"\"},"+
      "{\"unit\":\"Sven\",\"type1\":\"Human\",\"type2\":\"Scaled\",\"type3\":\"Knight\"}," +
      "{\"unit\":\"Templar assassin\",\"type1\":\"Vigilant\",\"type2\":\"Void\",\"type3\":\"Assassin\"}," +
      "{\"unit\":\"Terrorblade\",\"type1\":\"Demon\",\"type2\":\"Hunter\",\"type3\":\"\"}," +
      "{\"unit\":\"Tidehunter\",\"type1\":\"Scaled\",\"type2\":\"Warrior\",\"type3\":\"\"}," +
      "{\"unit\":\"Tiny\",\"type1\":\"Primordial\",\"type2\":\"Warrior\",\"type3\":\"\"}," +
      "{\"unit\":\"Treant protector\",\"type1\":\"Druid\",\"type2\":\"Brute\",\"type3\":\"\"}," +
      "{\"unit\":\"Troll warlord\",\"type1\":\"Troll\",\"type2\":\"Warrior\",\"type3\":\"\"}," +
      "{\"unit\":\"Tusk\",\"type1\":\"Savage\",\"type2\":\"Warrior\",\"type3\":\"\"}," +
      "{\"unit\":\"Venomancer\",\"type1\":\"Scaled\",\"type2\":\"Summoner\",\"type3\":\"\"}," +
      "{\"unit\":\"Viper\",\"type1\":\"Dragon\",\"type2\":\"Assassin\",\"type3\":\"\"}," +
      "{\"unit\":\"Void spirit\",\"type1\":\"Void\",\"type2\":\"Spirit\",\"type3\":\"\"}," +
      "{\"unit\":\"Warlock Hero\",\"type1\":\"Bloodbound\",\"type2\":\"Warlocks\",\"type3\":\"Healer\"}," +
      "{\"unit\":\"Weaver\",\"type1\":\"Insect\",\"type2\":\"Hunter\",\"type3\":\"\"}," +
      "{\"unit\":\"Windranger\",\"type1\":\"Vigilant\",\"type2\":\"Hunter\",\"type3\":\"\"}," +
      "{\"unit\":\"WitchDoctor\",\"type1\":\"Troll\",\"type2\":\"Warlocks\",\"type3\":\"\"}" +
      "]";


  // Heroes that you deem must be included. Adding more to this list drastically reduces compute time.
  // If you want it to finish in a reasonable amount of time.
  // I recommend 4, though it takes me 10 minutes to run with 3.
  private final List<String> reqHeroes = List.of("Legion", "Lycan", "Tiny", "Sven");


  private final List<Creature> requiredHeroes = new ArrayList<>();
  private final List<Creature> allHeroes = new ObjectMapper().readValue(allHeroesString, new TypeReference<>() {});

  {
    allHeroes.forEach(h -> {
      if (reqHeroes.contains(h.getUnit())) {
        requiredHeroes.add(new Creature(h.getUnit(), h.getType1(), h.getType2(), h.getType3()));
      }
    });
  }

  // Put it in an array for access speed.
  private final Creature[] heroArray = new Creature[allHeroes.size() - requiredHeroes.size()];
  private final Creature[] requiredHeroesArray = new Creature[requiredHeroes.size()];

  // The Current Synergies and the count required to activate them
  private static final Map<String, Integer> alliances = new HashMap<>();
  static {
    alliances.put("Assassin", 3);
    alliances.put("Bloodbound", 2);
    alliances.put("Brawny", 2);
    alliances.put("Brute", 2);
    alliances.put("Champion", 1);
   // alliances.put("Deadeye", 1);
    alliances.put("Demon", 1);
    alliances.put("Dragon", 2);
    alliances.put("Druid", 2);
    alliances.put("Healer", 2);
    alliances.put("Heartless", 2);
    alliances.put("Human", 2);
    alliances.put("Spirit", 3);
    alliances.put("Hunter", 3);
    alliances.put("Insect", 2);
    alliances.put("Knight", 2);
    alliances.put("Mage", 3);
    alliances.put("Primordial", 2);
    alliances.put("Savage", 2);
    alliances.put("Scaled", 2);
    alliances.put("Scrappy", 2);
    alliances.put("Shaman", 2);
    alliances.put("Summoner", 2);
    alliances.put("Troll", 2);
    alliances.put("Vigilant", 2);
    alliances.put("Void", 3);
    alliances.put("Warlocks", 2);
    alliances.put("Warrior", 3);
  }

  @Test
  void doIt() throws InterruptedException {
    int index = 0;
    for (Creature creature : allHeroes) {
      if (!requiredHeroes.contains(creature)) {
        heroArray[index] = creature;
        index++;
      }
    }
    index = 0;
    for (Creature creature : requiredHeroes) {
      requiredHeroesArray[index] = creature;
      index++;
    }

    LinkedBlockingQueue<byte[]> currentListToExecute = new LinkedBlockingQueue<>(10000);

    new Thread(() -> generate((byte) heroArray.length, (byte) ((byte) 10 - requiredHeroes.size()), currentListToExecute)).start();
    doSleep(10000);

    Runnable r = () -> {
      while (!currentListToExecute.isEmpty()) {
        try {
          operateOnList(currentListToExecute.poll(10, TimeUnit.SECONDS));
        } catch (NoSuchElementException | InterruptedException ignored) {
          System.out.println(ignored);
        }
      }
    };

    // execute on as many cores as possible.
    ExecutorService pool = Executors.newFixedThreadPool(threads);
    for (int i = 0; i < threads; i++) {
      pool.execute(r);
    }
    pool.shutdown();
    pool.awaitTermination(1000, TimeUnit.DAYS);
  }


  public void generate(byte size, byte choose, BlockingQueue<byte[]> currentListToExecute) {
    long count = 1;
    byte[] combination = new byte[choose];

    // initialize with lowest lexicographic combination
    for (byte i = 0; i < choose; i++) {
      combination[i] = i;
    }

    while (combination[choose - 1] < size) {
      try {
        currentListToExecute.offer(combination.clone(), 10, TimeUnit.SECONDS);
        if (count % printLinePer == 0) {
          System.out.println(count);
        }
        count++;
      } catch (InterruptedException ignored) {
      }
      // generate next combination in lexicographic order
      int t = choose - 1;
      while (t != 0 && combination[t] == size - choose + t) {
        t--;
      }
      combination[t]++;
      for (int i = t + 1; i < choose; i++) {
        combination[i] = (byte) (combination[i - 1] + 1);
      }
    }
  }

  private void operateOnList(byte[] sublist) {
    final StringBuilder out = new StringBuilder(Thread.currentThread().getName()).append("= ");
    if (sublist == null) {
      System.out.println("None exist");
      return;
    }
    final Map<String, Integer> combos = new HashMap<>();
    // Add computed heroes to list
    for (int i : sublist) {
      out.append(heroArray[i].getUnit()).append(",");
      addToCombo(combos, heroArray[i].getType1());
      addToCombo(combos, heroArray[i].getType2());
      addToCombo(combos, heroArray[i].getType3());
    }
    // Add required Heroes to list
    for (Creature creature : requiredHeroesArray) {
      out.append(creature.getUnit()).append(",");
      addToCombo(combos, creature.getType1());
      addToCombo(combos, creature.getType2());
      addToCombo(combos, creature.getType3());
    }
    out.append(" ###### ,");
    // Count the number of synergies
    int synergies = 0;
    for (Map.Entry<String, Integer> entry : combos.entrySet()) {
      String alliance = entry.getKey();
      Integer num = entry.getValue();
      if (alliances.containsKey(alliance) && num >= alliances.get(alliance)) {
        out.append(alliance).append(":").append(num);
        synergies++;
      }
    }

    // Change this number to change the synergies requirement for printing
    if (synergies >= desiredSynergyLevel) {
      out.append(" --- ").append(synergies);
      System.out.println(out);
    }
  }

  private void addToCombo(Map<String, Integer> combos, String alliance) {
    if (alliance != null) {
      combos.merge(alliance, 1, Integer::sum);
    }
  }

  private static void doSleep(long millisToSleep) {
    try {
      Thread.sleep(millisToSleep);
    } catch (InterruptedException ignored) {
    }
  }
}

@NoArgsConstructor
class Creature {
  public String unit;
  public String type1;
  public String type2;
  public String type3;

  public Creature(String unit, String type1, String type2, String type3) {
    this.unit = unit == "" ? null : unit;
    this.type1 = type1 == "" ? null : type1;
    this.type2 = type2 == "" ? null : type2;
    this.type3 = type3 == "" ? null : type3;
  }


  public String getUnit() {
    return unit;
  }
  public String getType1() {
    return type1;
  }

  public String getType2() {
    return type2;
  }

  public String getType3() {
    return type3;
  }

  public void setUnit(String unit) {
    this.unit = unit == "" ? null : unit;
  }

  public void setType1(String type1) {
    this.type1 = type1 == "" ? null : type1;
  }

  public void setType2(String type2) {
    this.type2 = type2 == "" ? null : type2;
  }

  public void setType3(String type3) {
    this.type3 = type3 == "" ? null : type3;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    Creature creature = (Creature) o;

    return Objects.equals(unit, creature.unit);
  }

  @Override
  public int hashCode() {
    return unit != null ? unit.hashCode() : 0;
  }
}