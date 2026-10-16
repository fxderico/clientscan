package com.donut.balance;

import java.nio.file.Path;
import java.util.List;

public interface PlatformBridge {
   String getPlayerName();

   List<String> getSidebarLines();

   boolean isRightShiftDown();

   boolean isKeyDown(int var1);

   boolean isRightClickDown();

   void consumeRightClick();

   boolean isInWorld();

   boolean isScreenOpen();

   String getHeldItemId();

   void sendCommand(String var1);

   void sendFeedback(String var1);

   void sendActionbar(String var1);

   Path getConfigDir();

   String getMcVersion();

   void installKeyHook(Runnable var1);

   void openGui();
}
