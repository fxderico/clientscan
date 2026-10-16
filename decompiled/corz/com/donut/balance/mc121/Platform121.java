package com.donut.balance.mc121;

import com.donut.balance.ClickerConfig;
import com.donut.balance.PlatformBridge;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.class_1799;
import net.minecraft.class_2561;
import net.minecraft.class_266;
import net.minecraft.class_268;
import net.minecraft.class_269;
import net.minecraft.class_2960;
import net.minecraft.class_310;
import net.minecraft.class_3675;
import net.minecraft.class_7923;
import net.minecraft.class_8646;
import net.minecraft.class_9011;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWKeyCallback;

public class Platform121 implements PlatformBridge {
   private final class_310 client;
   private GLFWKeyCallback originalKeyCallback = null;
   private GLFWKeyCallback customKeyCallback = null;
   private boolean keyHookInstalled = false;

   public Platform121(class_310 client) {
      this.client = client;
   }

   @Override
   public void installKeyHook(Runnable onRightShiftPress) {
      if (!this.keyHookInstalled && this.client != null) {
         try {
            if (this.client.method_22683() == null) {
               return;
            }

            long handle = this.client.method_22683().method_4490();
            if (handle == 0L) {
               return;
            }

            this.customKeyCallback = new GLFWKeyCallback() {
               public void invoke(long window, int key, int scancode, int action, int mods) {
                  if (key == 344 && action == 1) {
                     try {
                        onRightShiftPress.run();
                     } catch (Throwable var8) {
                        var8.printStackTrace();
                     }
                  }

                  if (Platform121.this.originalKeyCallback != null) {
                     Platform121.this.originalKeyCallback.invoke(window, key, scancode, action, mods);
                  }
               }
            };
            this.originalKeyCallback = GLFW.glfwSetKeyCallback(handle, this.customKeyCallback);
            this.keyHookInstalled = true;
         } catch (Exception var4) {
         }
      }
   }

   @Override
   public String getPlayerName() {
      return this.client.field_1724 != null ? this.client.field_1724.method_5820() : "";
   }

   @Override
   public List<String> getSidebarLines() {
      List<String> lines = new ArrayList<>();
      if (this.client.field_1687 == null) {
         return lines;
      } else {
         class_269 sb = this.client.field_1687.method_8428();
         class_266 obj = sb.method_1189(class_8646.field_45157);
         if (obj == null) {
            return lines;
         } else {
            for (class_9011 entry : sb.method_1184(obj)) {
               if (!entry.method_55385()) {
                  class_268 team = sb.method_1164(entry.comp_2127());
                  class_2561 comp = class_268.method_1142(team, class_2561.method_43470(entry.comp_2127()));
                  lines.add(comp.getString());
                  if (entry.comp_2129() != null) {
                     lines.add(entry.comp_2129().getString());
                  }
               }
            }

            return lines;
         }
      }
   }

   @Override
   public boolean isRightShiftDown() {
      try {
         return class_3675.method_15987(this.client.method_22683().method_4490(), 344);
      } catch (Exception var2) {
         return false;
      }
   }

   @Override
   public boolean isKeyDown(int glfwKeyCode) {
      if (glfwKeyCode <= 0) {
         return false;
      } else {
         try {
            return class_3675.method_15987(this.client.method_22683().method_4490(), glfwKeyCode);
         } catch (Exception var3) {
            return false;
         }
      }
   }

   @Override
   public boolean isRightClickDown() {
      return this.client.field_1690.field_1904.method_1434();
   }

   @Override
   public void consumeRightClick() {
      while (this.client.field_1690.field_1904.method_1436()) {
      }
   }

   @Override
   public boolean isInWorld() {
      return this.client.field_1724 != null && this.client.field_1687 != null;
   }

   @Override
   public boolean isScreenOpen() {
      return this.client.field_1755 != null;
   }

   @Override
   public String getHeldItemId() {
      if (this.client.field_1724 != null) {
         class_1799 stack = this.client.field_1724.method_6047();
         if (!stack.method_7960()) {
            class_2960 rl = class_7923.field_41178.method_10221(stack.method_7909());
            if (rl != null) {
               return rl.toString();
            }
         }
      }

      return null;
   }

   @Override
   public void sendCommand(String command) {
      if (this.client.field_1724 != null && this.client.field_1724.field_3944 != null) {
         this.client.field_1724.field_3944.method_45730(command);
      }
   }

   @Override
   public void sendFeedback(String message) {
      if (this.client.field_1724 != null) {
         this.client.field_1724.method_43496(class_2561.method_43470(message));
      }
   }

   @Override
   public void sendActionbar(String message) {
      if (this.client.field_1724 != null) {
         this.client.field_1724.method_7353(class_2561.method_43470(message), true);
      }
   }

   @Override
   public Path getConfigDir() {
      return this.client.field_1697.toPath().resolve("config");
   }

   @Override
   public String getMcVersion() {
      return "1.21.1";
   }

   @Override
   public void openGui() {
      if (this.client != null) {
         this.client.execute(() -> this.client.method_1507(new SimpleInfoScreen121(new ClickerConfig())));
      }
   }
}
