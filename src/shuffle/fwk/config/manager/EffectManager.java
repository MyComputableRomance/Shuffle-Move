/*  ShuffleMove - A program for identifying and simulating ideal moves in the game
 *  called Pokemon Shuffle.
 *  
 *  Copyright (C) 2015  Andrew Meyers
 *  
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *  
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package shuffle.fwk.config.manager;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;

import shuffle.fwk.config.ConfigFactory;
import shuffle.fwk.config.ConfigManager;
import shuffle.fwk.data.Effect;
import shuffle.fwk.data.Species;

/**
 * @author Andrew Meyers
 *         
 */
public class EffectManager extends ConfigManager {
   
   private static final String FORMAT_MEGA_SPEEDUP_CAP = "MEGA_SPEEDUPS_%s";
   private static final String FORMAT_MEGA_THRESHOLD = "MEGA_THRESHOLD_%s";
   private static final double[] DEFAULT_ODDS = new double[] { 1.0, 1.0, 1.0, 1.0 };
   private EnumMap<Effect, double[]> oddsMap;
   
   /**
    * Creates an EffectManager which manages configurable settings for Effects.
    * 
    * @param loadPaths
    * @param writePaths
    */
   public EffectManager(List<String> loadPaths, List<String> writePaths, ConfigFactory factory) {
      super(loadPaths, writePaths, factory);
   }
   
   public EffectManager(ConfigManager manager) {
      super(manager);
   }
   
   public EnumMap<Effect, double[]> getOddsMap() {
      if (oddsMap == null) {
         oddsMap = new EnumMap<Effect, double[]>(Effect.class);
      }
      return oddsMap;
   }
   
   @Override
   protected <T extends ConfigManager> void onCopyFrom(T manager) {
      synchronized (this) {
         reloadMaps();
      }
   }
   
   @Override
   public boolean loadFromConfig() {
      boolean changed = super.loadFromConfig();
      if (changed) {
         synchronized (this) {
            reloadMaps();
         }
      }
      return changed;
   }
   
   /**
    * 
    */
   private void reloadMaps() {
      getOddsMap().clear();
      for (Effect e : Effect.values()) {
         addOddsEntryForEffect(e);
      }
   }
   /**
    * @param e
    */
   public void addOddsEntryForEffect(Effect e) {
      String oddsString = getStringValue(e.toString());
      if (oddsString == null || oddsString.isEmpty()) {
         getOddsMap().remove(e);
      } else {
         double[] oddsValues = parseOdds(oddsString);
         getOddsMap().put(e, oddsValues);
      }
   }
   
   /**
    * @param oddsString
    * @return
    */
   private double[] parseOdds(String oddsString) {
      double[] ret = Arrays.copyOf(DEFAULT_ODDS, DEFAULT_ODDS.length);
      try {
         String[] tokens = oddsString.split("\\s+");
         for (int i = 0; i <= 3 && i < tokens.length; i++) {
            String trimmed = tokens[i].trim();
            ret[i] = Integer.parseInt(trimmed) / 100.0;
         }
      } catch (NumberFormatException nfe) {
         nfe.printStackTrace();
      }
      return ret;
   }
   
   public double getOdds(Effect effect, int num) {
      int index = Math.max(Math.min(num, 6), 3) - 3;
      if (getOddsMap().containsKey(effect)) {
         return getOddsMap().get(effect)[index];
      } else {
         return DEFAULT_ODDS[index];
      }
   }
   
   public int getMegaSpeedupCap(Species species) {
      return getIntegerValue(getMegaSpeedupKey(species), 0);
   }
   
   public int getMegaThreshold(Species species) {
      return getIntegerValue(getMegaThresholdKey(species), Integer.MAX_VALUE);
   }
   
   /**
    * @param species
    *           The Species to obtain the mega speedups key for.
    * @return The mega speedups key
    */
   public String getMegaSpeedupKey(Species species) {
      String megaName = species.getMegaName();
      if (megaName == null) {
         return null;
      } else {
         return String.format(FORMAT_MEGA_SPEEDUP_CAP, megaName);
      }
   }
   
   /**
    * @param species
    *           The Species to obtain the mega threshold key for.
    * @return The mega threshold key
    */
   public String getMegaThresholdKey(Species species) {
      String megaName = species.getMegaName();
      if (megaName == null) {
         return null;
      } else {
         return String.format(FORMAT_MEGA_THRESHOLD, megaName);
      }
   }
}
