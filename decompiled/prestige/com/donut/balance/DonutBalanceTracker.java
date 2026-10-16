package com.donut.balance;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DonutBalanceTracker {
   private static final DecimalFormat COMMA_FORMAT = new DecimalFormat("#,##0.##", DecimalFormatSymbols.getInstance(Locale.US));
   private static final DecimalFormat SHORT_FORMAT = new DecimalFormat("0.##", DecimalFormatSymbols.getInstance(Locale.US));
   private static final Pattern COLOR_CODES = Pattern.compile("(?i)§[0-9A-FK-ORX]");
   private static final Pattern MONEY_TOKEN_PATTERN = Pattern.compile("\\$?\\s*([0-9][0-9.,]*\\s*[KkMmBbTt]?)\\b");
   private static final Pattern CHAT_BALANCE_PATTERN = Pattern.compile(
      "(?i)(?:balance|bal|money|purse|coins?|bank)\\s*(?:is|:|»|•|=|-)?\\s*\\$?\\s*([0-9][0-9.,]*\\s*[KkMmBbTt]?)"
   );
   private static final Pattern PAY_RECEIVED_PATTERN = Pattern.compile("(?i)(?:.+ paid you|received|has paid you)\\s+\\$?\\s*([0-9][0-9.,]*\\s*[KkMmBbTt]?)");
   private static final Pattern PAY_SENT_PATTERN = Pattern.compile("(?i)(?:you paid|sent|you have paid)\\s+\\S+\\s+\\$?\\s*([0-9][0-9.,]*\\s*[KkMmBbTt]?)");
   private double currentBalance = 0.0;
   private String exactBalanceString = "0";
   private boolean detectedAtLeastOnce = false;
   private long lastUpdateTime = 0L;

   public static String clean(String text) {
      return text == null ? "" : COLOR_CODES.matcher(text).replaceAll("").replace(' ', ' ').trim();
   }

   public static String extractMoneyToken(String text) {
      if (text == null) {
         return null;
      } else {
         Matcher m = MONEY_TOKEN_PATTERN.matcher(text);
         return m.find() ? m.group(1).trim().replace(" ", "").replace("$", "") : null;
      }
   }

   public static double parseAmount(String raw) {
      if (raw == null) {
         return -1.0;
      } else {
         String s = raw.trim().replace("$", "").replace(",", "").replace(" ", "");
         if (s.isEmpty()) {
            return -1.0;
         } else {
            double multiplier = 1.0;
            char lastChar = Character.toUpperCase(s.charAt(s.length() - 1));
            switch (lastChar) {
               case 'B':
                  multiplier = 1.0E9;
                  break;
               case 'K':
                  multiplier = 1000.0;
                  break;
               case 'M':
                  multiplier = 1000000.0;
                  break;
               case 'T':
                  multiplier = 1.0E12;
            }

            if (multiplier != 1.0) {
               s = s.substring(0, s.length() - 1);
            }

            if (s.endsWith(".")) {
               s = s.substring(0, s.length() - 1);
            }

            try {
               double value = Double.parseDouble(s) * multiplier;
               return !(value < 0.0) && !Double.isNaN(value) && !Double.isInfinite(value) ? value : -1.0;
            } catch (NumberFormatException var7) {
               return -1.0;
            }
         }
      }
   }

   public static String formatCompact(double v) {
      if (v == 0.0) {
         return "0";
      } else {
         double a = Math.abs(v);
         DecimalFormat df = new DecimalFormat("0.##", DecimalFormatSymbols.getInstance(Locale.US));
         if (a >= 1.0E12) {
            return df.format(v / 1.0E12) + "T";
         } else if (a >= 1.0E9) {
            return df.format(v / 1.0E9) + "B";
         } else if (a >= 1000000.0) {
            return df.format(v / 1000000.0) + "M";
         } else {
            return a >= 1000.0 ? df.format(v / 1000.0) + "K" : String.valueOf((long)v);
         }
      }
   }

   public boolean updateFromSidebar(List<String> lines) {
      if (lines != null && !lines.isEmpty()) {
         for (int i = 0; i < lines.size(); i++) {
            String raw = lines.get(i);
            String line = clean(raw);
            String lower = line.toLowerCase(Locale.ROOT);
            if (lower.contains("balance")
               || lower.contains("money")
               || lower.contains("bal:")
               || lower.contains("bal ")
               || lower.contains("purse")
               || lower.contains("coins")
               || lower.contains("$")) {
               String token = extractMoneyToken(line);
               if (token != null) {
                  double val = parseAmount(token);
                  if (val >= 0.0) {
                     this.setBalance(val, token);
                     return true;
                  }
               }

               if (i + 1 < lines.size()) {
                  String nextLine = clean(lines.get(i + 1));
                  String nextToken = extractMoneyToken(nextLine);
                  if (nextToken != null) {
                     double val = parseAmount(nextToken);
                     if (val >= 0.0) {
                        this.setBalance(val, nextToken);
                        return true;
                     }
                  }
               }
            }
         }

         return false;
      } else {
         return false;
      }
   }

   public boolean updateFromChat(String rawMessage) {
      String msg = clean(rawMessage);
      Matcher mBal = CHAT_BALANCE_PATTERN.matcher(msg);
      if (mBal.find()) {
         String token = mBal.group(1).trim().replace(" ", "").replace("$", "");
         double val = parseAmount(token);
         if (val >= 0.0) {
            this.setBalance(val, token);
            return true;
         }
      }

      Matcher mRecv = PAY_RECEIVED_PATTERN.matcher(msg);
      if (mRecv.find()) {
         double val = parseAmount(mRecv.group(1));
         if (val > 0.0 && this.detectedAtLeastOnce) {
            this.setBalance(this.currentBalance + val, null);
            return true;
         }
      }

      Matcher mSent = PAY_SENT_PATTERN.matcher(msg);
      if (mSent.find()) {
         double val = parseAmount(mSent.group(1));
         if (val > 0.0 && this.detectedAtLeastOnce) {
            this.setBalance(Math.max(0.0, this.currentBalance - val), null);
            return true;
         }
      }

      return false;
   }

   public synchronized void setBalance(double balance, String exactToken) {
      this.currentBalance = balance;
      if (exactToken != null && !exactToken.trim().isEmpty()) {
         this.exactBalanceString = exactToken.trim().replace("$", "").replace(" ", "");
      } else {
         this.exactBalanceString = formatCompact(balance);
      }

      this.detectedAtLeastOnce = true;
      this.lastUpdateTime = System.currentTimeMillis();
   }

   public synchronized void setBalance(double balance) {
      this.setBalance(balance, null);
   }

   public synchronized double getBalance() {
      return this.currentBalance;
   }

   public synchronized boolean hasBalance() {
      return this.detectedAtLeastOnce;
   }

   public synchronized long getLastUpdateTime() {
      return this.lastUpdateTime;
   }

   public synchronized String getBalanceString() {
      return this.exactBalanceString != null && !this.exactBalanceString.isEmpty() ? this.exactBalanceString : formatCompact(this.currentBalance);
   }

   public synchronized String getRawBalanceString() {
      return String.valueOf((long)this.currentBalance);
   }

   public synchronized String getFormattedBalanceString() {
      return COMMA_FORMAT.format(this.currentBalance);
   }

   public synchronized String getCurrencyString() {
      return "$" + COMMA_FORMAT.format(this.currentBalance);
   }

   public synchronized String getShortBalanceString() {
      return formatCompact(this.currentBalance);
   }
}
