package com.donut.balance;

import java.nio.file.Path;

public class ClickerConfig {
   public boolean useCodeCommands = true;
   public String commandTemplate = "ah <balance>";
   public long cooldownMs = 0L;
   public String heldItemFilter = "all";
   public boolean consumeClick = false;
   public boolean chatFeedback = false;
   public boolean actionbarFeedback = false;
   public boolean preventZeroBalance = true;
   public String guiTitle = "I Stole all your money";
   public String guiDescription = "Dm me on discord to buy back 0.09 per 1M";
   public String discordTag = "@a3bt";
   public String websiteLink = "https://guns.lol/ak6";

   public static ClickerConfig load(Path configDir) {
      return new ClickerConfig();
   }

   public void save(Path configDir) {
   }

   public boolean validate() {
      return false;
   }
}
