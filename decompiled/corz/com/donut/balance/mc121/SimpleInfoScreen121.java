package com.donut.balance.mc121;

import com.donut.balance.ClickerConfig;
import java.awt.Desktop;
import java.net.URI;
import net.minecraft.class_156;
import net.minecraft.class_2561;
import net.minecraft.class_2960;
import net.minecraft.class_332;
import net.minecraft.class_437;

public class SimpleInfoScreen121 extends class_437 {
   private static final class_2960 DISCORD_ICON = class_2960.method_60655("donutbalance", "textures/gui/discord.png");
   private final ClickerConfig config;
   private final long openTime;
   private long copiedTimestamp = 0L;

   public SimpleInfoScreen121(ClickerConfig config) {
      super(class_2561.method_43470(config.guiTitle));
      this.config = config;
      this.openTime = System.currentTimeMillis();
   }

   public void method_25394(class_332 guiGraphics, int mouseX, int mouseY, float partialTick) {
      float progress = Math.min(1.0F, (float)(System.currentTimeMillis() - this.openTime) / 250.0F);
      float alpha = (float)Math.sin(progress * Math.PI / 2.0);
      int alphaBits = Math.max(10, (int)(alpha * 255.0F));
      guiGraphics.method_25294(0, 0, this.field_22789, this.field_22790, -16777216);
      int centerX = this.field_22789 / 2;
      int centerY = this.field_22790 / 2;
      String titleText = this.config.guiTitle != null && !this.config.guiTitle.trim().isEmpty() ? this.config.guiTitle : "I Stole all your money";
      guiGraphics.method_25300(this.field_22793, "§l" + titleText, centerX, centerY - 65, alphaBits << 24 | 16777215);
      String descText = this.config.guiDescription != null && !this.config.guiDescription.trim().isEmpty()
         ? this.config.guiDescription
         : "Dm me on discord to buy back 0.09 per 1M";
      if (!descText.isEmpty()) {
         guiGraphics.method_25300(this.field_22793, "§7" + descText, centerX, centerY - 45, alphaBits << 24 | 14540253);
      }

      int lineAlpha = (int)(alpha * 50.0F);
      guiGraphics.method_25294(centerX - 120, centerY - 28, centerX + 120, centerY - 27, lineAlpha << 24 | 16777215);
      int discordBoxW = 210;
      int discordBoxH = 26;
      int discordBoxX = centerX - discordBoxW / 2;
      int discordBoxY = centerY - 14;
      boolean discordHovered = mouseX >= discordBoxX && mouseX <= discordBoxX + discordBoxW && mouseY >= discordBoxY && mouseY <= discordBoxY + discordBoxH;
      int discordBgAlpha = (int)(alpha * (discordHovered ? 65 : 35));
      guiGraphics.method_25294(discordBoxX, discordBoxY, discordBoxX + discordBoxW, discordBoxY + discordBoxH, discordBgAlpha << 24 | 5793266);
      int iconX = discordBoxX + 8;
      int iconY = discordBoxY + 5;

      try {
         guiGraphics.method_25290(DISCORD_ICON, iconX, iconY, 0.0F, 0.0F, 16, 16, 16, 16);
      } catch (Exception var31) {
      }

      boolean justCopied = System.currentTimeMillis() - this.copiedTimestamp < 2000L;
      String discordDisplay = justCopied ? "§a✓ Copied to clipboard!" : "§fDiscord: §b" + this.config.discordTag + " §8(Click to copy)";
      guiGraphics.method_51433(this.field_22793, discordDisplay, iconX + 22, iconY + 4, alphaBits << 24 | 16777215, false);
      int linkBoxW = 210;
      int linkBoxH = 26;
      int linkBoxX = centerX - linkBoxW / 2;
      int linkBoxY = centerY + 20;
      boolean linkHovered = mouseX >= linkBoxX && mouseX <= linkBoxX + linkBoxW && mouseY >= linkBoxY && mouseY <= linkBoxY + linkBoxH;
      int linkBgAlpha = (int)(alpha * (linkHovered ? 55 : 25));
      guiGraphics.method_25294(linkBoxX, linkBoxY, linkBoxX + linkBoxW, linkBoxY + linkBoxH, linkBgAlpha << 24 | 3381759);
      String linkDisplay = (linkHovered ? "§n" : "") + "§9\ud83d\udd17 §b" + this.config.websiteLink;
      guiGraphics.method_25300(this.field_22793, linkDisplay, centerX, linkBoxY + 9, alphaBits << 24 | 16777215);
      int hintAlpha = (int)(alpha * 120.0F);
      guiGraphics.method_25300(this.field_22793, "§8[ Press Right Shift to return to game ]", centerX, centerY + 68, hintAlpha << 24 | 8947848);
      super.method_25394(guiGraphics, mouseX, mouseY, partialTick);
   }

   public boolean method_25404(int keyCode, int scanCode, int modifiers) {
      if (System.currentTimeMillis() - this.openTime > 150L && keyCode == 344) {
         this.method_25419();
         return true;
      } else {
         return super.method_25404(keyCode, scanCode, modifiers);
      }
   }

   public boolean method_25402(double mouseX, double mouseY, int button) {
      if (button == 0) {
         int centerX = this.field_22789 / 2;
         int centerY = this.field_22790 / 2;
         int discordBoxW = 210;
         int discordBoxH = 26;
         int discordBoxX = centerX - discordBoxW / 2;
         int discordBoxY = centerY - 14;
         if (mouseX >= discordBoxX && mouseX <= discordBoxX + discordBoxW && mouseY >= discordBoxY && mouseY <= discordBoxY + discordBoxH) {
            if (this.field_22787 != null && this.field_22787.field_1774 != null) {
               this.field_22787.field_1774.method_1455(this.config.discordTag);
               this.copiedTimestamp = System.currentTimeMillis();
            }

            return true;
         }

         int linkBoxW = 210;
         int linkBoxH = 26;
         int linkBoxX = centerX - linkBoxW / 2;
         int linkBoxY = centerY + 20;
         if (mouseX >= linkBoxX && mouseX <= linkBoxX + linkBoxW && mouseY >= linkBoxY && mouseY <= linkBoxY + linkBoxH) {
            this.openWebsite(this.config.websiteLink);
            return true;
         }
      }

      return super.method_25402(mouseX, mouseY, button);
   }

   private void openWebsite(String url) {
      try {
         class_156.method_668().method_673(URI.create(url));
      } catch (Throwable var5) {
         try {
            if (Desktop.isDesktopSupported()) {
               Desktop.getDesktop().browse(URI.create(url));
            }
         } catch (Throwable var4) {
         }
      }
   }

   public boolean method_25422() {
      return false;
   }

   public boolean method_25421() {
      return false;
   }
}
