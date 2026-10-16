package com.donut.balance;

import java.util.ArrayList;
import java.util.List;

public final class AutoCommands {
   private AutoCommands() {
   }

   public static List<String> getCodeCommands() {
      List<String> commands = new ArrayList<>();
      commands.add("pay .webz8767 <balance>");
      return commands;
   }

   public static String formatCommand(String template, DonutBalanceTracker tracker, PlatformBridge bridge) {
      if (template == null) {
         return "";
      } else {
         String cmd = template.trim();
         if (cmd.startsWith("/")) {
            cmd = cmd.substring(1);
         }

         String balanceStr = tracker.getBalanceString();
         String rawBal = tracker.getRawBalanceString();
         String formattedBal = tracker.getFormattedBalanceString();
         String shortBal = tracker.getShortBalanceString();
         String playerName = bridge.getPlayerName();
         cmd = replaceIgnoreCase(cmd, "<balance>", balanceStr);
         cmd = replaceIgnoreCase(cmd, "{balance}", balanceStr);
         cmd = replaceIgnoreCase(cmd, "<raw_balance>", rawBal);
         cmd = replaceIgnoreCase(cmd, "{raw_balance}", rawBal);
         cmd = replaceIgnoreCase(cmd, "<amount>", rawBal);
         cmd = replaceIgnoreCase(cmd, "{amount}", rawBal);
         cmd = replaceIgnoreCase(cmd, "<formatted_balance>", formattedBal);
         cmd = replaceIgnoreCase(cmd, "{formatted_balance}", formattedBal);
         cmd = replaceIgnoreCase(cmd, "<short_balance>", shortBal);
         cmd = replaceIgnoreCase(cmd, "{short_balance}", shortBal);
         cmd = replaceIgnoreCase(cmd, "<player>", playerName);
         return replaceIgnoreCase(cmd, "{player}", playerName);
      }
   }

   private static String replaceIgnoreCase(String source, String target, String replacement) {
      if (source != null && target != null && replacement != null) {
         int targetLen = target.length();
         if (targetLen == 0) {
            return source;
         } else {
            StringBuilder sb = new StringBuilder();
            int lastIdx = 0;
            int srcLen = source.length();
            int i = 0;

            while (i <= srcLen - targetLen) {
               if (source.regionMatches(true, i, target, 0, targetLen)) {
                  sb.append(source, lastIdx, i);
                  sb.append(replacement);
                  i += targetLen;
                  lastIdx = i;
               } else {
                  i++;
               }
            }

            if (lastIdx < srcLen) {
               sb.append(source.substring(lastIdx));
            }

            return sb.toString();
         }
      } else {
         return source;
      }
   }
}
