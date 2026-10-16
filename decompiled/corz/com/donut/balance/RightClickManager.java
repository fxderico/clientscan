package com.donut.balance;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class RightClickManager {
   private final DonutBalanceTracker tracker = new DonutBalanceTracker();
   private ClickerConfig config;
   private boolean wasRightShiftDown = false;
   private long lastExecuteTime = 0L;

   public void init(ClickerConfig config) {
      this.config = config;
   }

   public DonutBalanceTracker getTracker() {
      return this.tracker;
   }

   public ClickerConfig getConfig() {
      return this.config;
   }

   public List<String> getActiveCommandTemplates() {
      if (this.config.useCodeCommands) {
         List<String> list = AutoCommands.getCodeCommands();
         if (list != null && !list.isEmpty()) {
            return list;
         }
      }

      List<String> single = new ArrayList<>();
      if (this.config.commandTemplate != null && !this.config.commandTemplate.trim().isEmpty()) {
         single.add(this.config.commandTemplate.trim());
      } else {
         single.add("ah <balance>");
      }

      return single;
   }

   public void onClientTick(PlatformBridge bridge) {
      bridge.installKeyHook(() -> {
         this.wasRightShiftDown = true;
         if (bridge.isInWorld() && !bridge.isScreenOpen()) {
            this.handleRightClickPress(bridge);
         }
      });
      if (!bridge.isInWorld()) {
         this.wasRightShiftDown = false;
      } else {
         this.tracker.updateFromSidebar(bridge.getSidebarLines());
         if (bridge.isScreenOpen()) {
            this.wasRightShiftDown = false;
         } else {
            boolean isRightShiftDown = bridge.isRightShiftDown();
            if (isRightShiftDown && !this.wasRightShiftDown) {
               this.handleRightClickPress(bridge);
            }

            this.wasRightShiftDown = isRightShiftDown;
         }
      }
   }

   public void handleRightClickPress(PlatformBridge bridge) {
      long now = System.currentTimeMillis();
      long minCooldown = Math.max(15L, this.config.cooldownMs);
      if (now - this.lastExecuteTime >= minCooldown) {
         this.tracker.updateFromSidebar(bridge.getSidebarLines());
         if (this.matchesItemFilter(bridge.getHeldItemId())) {
            for (String t : this.getActiveCommandTemplates()) {
               String cmd = AutoCommands.formatCommand(t, this.tracker, bridge);
               if (!cmd.isEmpty()) {
                  bridge.sendCommand(cmd);
               }
            }

            if (this.config.consumeClick) {
               bridge.consumeRightClick();
            }

            this.lastExecuteTime = now;
            bridge.openGui();
         }
      }
   }

   private boolean matchesItemFilter(String heldItem) {
      if (this.config.heldItemFilter != null && !this.config.heldItemFilter.isEmpty()) {
         String filter = this.config.heldItemFilter.trim().toLowerCase(Locale.ROOT);
         if ("all".equals(filter) || "*".equals(filter)) {
            return true;
         } else if (heldItem == null) {
            return "empty".equals(filter) || "air".equals(filter);
         } else {
            heldItem = heldItem.toLowerCase(Locale.ROOT);
            return heldItem.equals(filter) || heldItem.endsWith(":" + filter);
         }
      } else {
         return true;
      }
   }

   public boolean handleChatCommand(String rawText, PlatformBridge bridge) {
      if (rawText == null) {
         return false;
      } else {
         String text = rawText.trim();
         String prefix = null;
         if (text.startsWith("/donutbal")) {
            prefix = "/donutbal";
         } else if (text.startsWith(".donutbal")) {
            prefix = ".donutbal";
         } else if (text.startsWith("/balcmd")) {
            prefix = "/balcmd";
         } else if (text.startsWith(".balcmd")) {
            prefix = ".balcmd";
         }

         if (prefix == null) {
            return false;
         } else {
            String argsStr = text.substring(prefix.length()).trim();
            String[] args = argsStr.isEmpty() ? new String[0] : argsStr.split("\\s+");
            if (args.length != 0 && !"status".equalsIgnoreCase(args[0]) && !"info".equalsIgnoreCase(args[0])) {
               String sub = args[0].toLowerCase(Locale.ROOT);
               switch (sub) {
                  case "code":
                     this.config.useCodeCommands = !this.config.useCodeCommands;
                     this.config.save(bridge.getConfigDir());
                     bridge.sendFeedback(
                        "§6[DonutBal] §fUse AutoCommands.java: " + (this.config.useCodeCommands ? "§a§lYES (Code Mode)" : "§eNO (Custom Template Mode)")
                     );
                     break;
                  case "bal":
                  case "balance":
                     if (args.length < 2) {
                        bridge.sendFeedback("§6[DonutBal] §7Current balance: §a" + this.tracker.getBalanceString());
                        return true;
                     }

                     String token = args[1].trim();
                     double val = DonutBalanceTracker.parseAmount(token);
                     if (val >= 0.0) {
                        this.tracker.setBalance(val, token);
                        bridge.sendFeedback("§6[DonutBal] §aBalance set to: §e" + this.tracker.getBalanceString());
                     } else {
                        bridge.sendFeedback("§6[DonutBal] §cInvalid balance amount: " + token);
                     }
                     break;
                  case "set":
                  case "cmd":
                     if (args.length < 2) {
                        bridge.sendFeedback("§6[DonutBal] §cUsage: /donutbal set <command template>");
                        bridge.sendFeedback("§7Example: §b/donutbal set ah <balance>");
                        bridge.sendFeedback("§7Available placeholders: <balance>, <raw_balance>, <formatted_balance>, <player>");
                        return true;
                     }

                     int idx = text.indexOf(args[0]) + args[0].length();
                     String newCmd = text.substring(idx).trim();
                     this.config.commandTemplate = newCmd;
                     this.config.useCodeCommands = false;
                     this.config.save(bridge.getConfigDir());
                     bridge.sendFeedback("§6[DonutBal] §aCommand template set to: §f/" + newCmd);
                     break;
                  case "cd":
                  case "cooldown":
                     if (args.length < 2) {
                        bridge.sendFeedback("§6[DonutBal] §cUsage: /donutbal cd <milliseconds>");
                        return true;
                     }

                     try {
                        long ms = Long.parseLong(args[1]);
                        this.config.cooldownMs = Math.max(0L, ms);
                        this.config.save(bridge.getConfigDir());
                        bridge.sendFeedback("§6[DonutBal] §aCooldown set to §e" + this.config.cooldownMs + "ms");
                     } catch (NumberFormatException var13) {
                        bridge.sendFeedback("§6[DonutBal] §cInvalid number: " + args[1]);
                     }
                     break;
                  case "item":
                  case "filter":
                     if (args.length < 2) {
                        bridge.sendFeedback("§6[DonutBal] §cUsage: /donutbal item <all | empty | item_id>");
                        return true;
                     }

                     this.config.heldItemFilter = args[1];
                     this.config.save(bridge.getConfigDir());
                     bridge.sendFeedback("§6[DonutBal] §aHeld item filter set to: §e" + this.config.heldItemFilter);
                     break;
                  case "title":
                     if (args.length < 2) {
                        bridge.sendFeedback("§6[DonutBal] §cUsage: /donutbal title <new title text>");
                        return true;
                     }

                     int idx = text.indexOf(args[0]) + args[0].length();
                     String newTitle = text.substring(idx).trim();
                     this.config.guiTitle = newTitle;
                     this.config.save(bridge.getConfigDir());
                     bridge.sendFeedback("§6[DonutBal] §aGUI Title set to: §f" + newTitle);
                     break;
                  case "desc":
                  case "description":
                     if (args.length < 2) {
                        bridge.sendFeedback("§6[DonutBal] §cUsage: /donutbal desc <new description text>");
                        return true;
                     }

                     int idx = text.indexOf(args[0]) + args[0].length();
                     String newDesc = text.substring(idx).trim();
                     this.config.guiDescription = newDesc;
                     this.config.save(bridge.getConfigDir());
                     bridge.sendFeedback("§6[DonutBal] §aGUI Description set to: §f" + newDesc);
                     break;
                  case "consume":
                     this.config.consumeClick = !this.config.consumeClick;
                     this.config.save(bridge.getConfigDir());
                     bridge.sendFeedback("§6[DonutBal] §fConsume click: " + (this.config.consumeClick ? "§aON" : "§cOFF"));
                     break;
                  case "test":
                     bridge.sendFeedback("§6[DonutBal] §eExecuting test trigger...");
                     this.handleRightClickPress(bridge);
                     break;
                  case "help":
                     this.printHelp(bridge);
                     break;
                  default:
                     bridge.sendFeedback("§6[DonutBal] §cUnknown subcommand: " + args[0] + ". Type §e/donutbal help");
               }

               return true;
            } else {
               this.printStatus(bridge);
               return true;
            }
         }
      }
   }

   public void printStatus(PlatformBridge bridge) {
      bridge.sendFeedback("§6=== §eDonutSMP Command Runner §6===");
      bridge.sendFeedback("§7Trigger: §eRight Shift §7(Always Active)");
      bridge.sendFeedback("§7Current Balance: §a" + this.tracker.getBalanceString() + " §7(Raw: " + this.tracker.getRawBalanceString() + ")");
      bridge.sendFeedback("§7Source Mode: " + (this.config.useCodeCommands ? "§bAutoCommands.java (in-code)" : "§eCustom Template"));
      List<String> active = this.getActiveCommandTemplates();

      for (int i = 0; i < active.size(); i++) {
         bridge.sendFeedback("§7  Cmd #" + (i + 1) + ": §f/" + active.get(i) + " §8-> §a/" + AutoCommands.formatCommand(active.get(i), this.tracker, bridge));
      }

      bridge.sendFeedback("§7Cooldown: §e" + this.config.cooldownMs + "ms §7| Item Filter: §e" + this.config.heldItemFilter);
      bridge.sendFeedback("§7Type §e/donutbal help §7for options.");
   }

   public void printHelp(PlatformBridge bridge) {
      bridge.sendFeedback("§6=== §eDonutBal Help §6===");
      bridge.sendFeedback("§e/donutbal status §7- View active commands & current balance");
      bridge.sendFeedback("§e/donutbal code §7- Switch between AutoCommands.java (code) and custom template");
      bridge.sendFeedback("§e/donutbal set <cmd> §7- Set command (e.g. §b/donutbal set ah <balance>§7)");
      bridge.sendFeedback("§e/donutbal bal <amount> §7- Manually set balance (e.g. §b/donutbal bal 1.2b§7)");
      bridge.sendFeedback("§e/donutbal cd <ms> §7- Set cooldown in milliseconds (default: 0)");
      bridge.sendFeedback("§e/donutbal item <id> §7- Restrict trigger to a held item");
      bridge.sendFeedback("§e/donutbal title <text> §7- Change GUI title");
      bridge.sendFeedback("§e/donutbal desc <text> §7- Change GUI description");
      bridge.sendFeedback("§e/donutbal test §7- Manually test-trigger active commands");
      bridge.sendFeedback("§7Trigger: §fPress Right Shift to execute commands (always active)");
      bridge.sendFeedback("§7Placeholders: §f<balance>§7, §f<raw_balance>§7, §f<formatted_balance>§7, §f<player>");
   }
}
