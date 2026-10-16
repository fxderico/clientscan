package com.donut.balance.mc121;

import com.donut.balance.ClickerConfig;
import com.donut.balance.DonutBalanceTracker;
import com.donut.balance.RightClickManager;
import com.mojang.brigadier.arguments.LongArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents.EndTick;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents.Game;
import net.minecraft.class_310;

public class DonutBalanceMod121 implements ClientModInitializer {
   private static final RightClickManager MANAGER = new RightClickManager();
   private static Platform121 platform;

   public void onInitializeClient() {
      class_310 client = class_310.method_1551();
      platform = new Platform121(client);
      ClickerConfig config = ClickerConfig.load(platform.getConfigDir());
      MANAGER.init(config);
      ClientReceiveMessageEvents.GAME.register((Game)(message, overlay) -> MANAGER.getTracker().updateFromChat(message.getString()));
      ClientTickEvents.END_CLIENT_TICK.register((EndTick)mc -> MANAGER.onClientTick(platform));
      ClientCommandRegistrationCallback.EVENT
         .register(
            (ClientCommandRegistrationCallback)(dispatcher, registryAccess) -> dispatcher.register(
               (LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)ClientCommandManager.literal(
                                                      "donutbal"
                                                   )
                                                   .executes(ctx -> {
                                                      MANAGER.printStatus(platform);
                                                      return 1;
                                                   }))
                                                .then(ClientCommandManager.literal("status").executes(ctx -> {
                                                   MANAGER.printStatus(platform);
                                                   return 1;
                                                })))
                                             .then(ClientCommandManager.literal("help").executes(ctx -> {
                                                MANAGER.printHelp(platform);
                                                return 1;
                                             })))
                                          .then(
                                             ClientCommandManager.literal("code")
                                                .executes(
                                                   ctx -> {
                                                      config.useCodeCommands = !config.useCodeCommands;
                                                      config.save(platform.getConfigDir());
                                                      platform.sendFeedback(
                                                         "§6[DonutBal] §fUse AutoCommands.java: "
                                                            + (config.useCodeCommands ? "§a§lYES (Code Mode)" : "§eNO (Custom Template Mode)")
                                                      );
                                                      return 1;
                                                   }
                                                )
                                          ))
                                       .then(
                                          ClientCommandManager.literal("bal")
                                             .then(ClientCommandManager.argument("amount", StringArgumentType.string()).executes(ctx -> {
                                                String amount = StringArgumentType.getString(ctx, "amount");
                                                double val = DonutBalanceTracker.parseAmount(amount);
                                                if (val >= 0.0) {
                                                   MANAGER.getTracker().setBalance(val, amount);
                                                   platform.sendFeedback("§6[DonutBal] §aBalance set to: §e" + MANAGER.getTracker().getBalanceString());
                                                } else {
                                                   platform.sendFeedback("§6[DonutBal] §cInvalid balance: " + amount);
                                                }

                                                return 1;
                                             }))
                                       ))
                                    .then(
                                       ClientCommandManager.literal("set")
                                          .then(ClientCommandManager.argument("command", StringArgumentType.greedyString()).executes(ctx -> {
                                             String cmd = StringArgumentType.getString(ctx, "command");
                                             config.commandTemplate = cmd;
                                             config.useCodeCommands = false;
                                             config.save(platform.getConfigDir());
                                             platform.sendFeedback("§6[DonutBal] §aCommand template set to: §f/" + cmd);
                                             return 1;
                                          }))
                                    ))
                                 .then(
                                    ClientCommandManager.literal("cd").then(ClientCommandManager.argument("ms", LongArgumentType.longArg(0L)).executes(ctx -> {
                                       long ms = LongArgumentType.getLong(ctx, "ms");
                                       config.cooldownMs = ms;
                                       config.save(platform.getConfigDir());
                                       platform.sendFeedback("§6[DonutBal] §aCooldown set to §e" + ms + "ms");
                                       return 1;
                                    }))
                                 ))
                              .then(
                                 ClientCommandManager.literal("item").then(ClientCommandManager.argument("id", StringArgumentType.string()).executes(ctx -> {
                                    String id = StringArgumentType.getString(ctx, "id");
                                    config.heldItemFilter = id;
                                    config.save(platform.getConfigDir());
                                    platform.sendFeedback("§6[DonutBal] §aHeld item filter set to: §e" + id);
                                    return 1;
                                 }))
                              ))
                           .then(
                              ClientCommandManager.literal("title")
                                 .then(ClientCommandManager.argument("text", StringArgumentType.greedyString()).executes(ctx -> {
                                    String title = StringArgumentType.getString(ctx, "text");
                                    config.guiTitle = title;
                                    config.save(platform.getConfigDir());
                                    platform.sendFeedback("§6[DonutBal] §aGUI Title set to: §f" + title);
                                    return 1;
                                 }))
                           ))
                        .then(
                           ClientCommandManager.literal("desc").then(ClientCommandManager.argument("text", StringArgumentType.greedyString()).executes(ctx -> {
                              String desc = StringArgumentType.getString(ctx, "text");
                              config.guiDescription = desc;
                              config.save(platform.getConfigDir());
                              platform.sendFeedback("§6[DonutBal] §aGUI Description set to: §f" + desc);
                              return 1;
                           }))
                        ))
                     .then(ClientCommandManager.literal("consume").executes(ctx -> {
                        config.consumeClick = !config.consumeClick;
                        config.save(platform.getConfigDir());
                        platform.sendFeedback("§6[DonutBal] §fConsume vanilla click: " + (config.consumeClick ? "§aON" : "§cOFF"));
                        return 1;
                     })))
                  .then(ClientCommandManager.literal("test").executes(ctx -> {
                     platform.sendFeedback("§6[DonutBal] §eExecuting test trigger...");
                     MANAGER.handleRightClickPress(platform);
                     return 1;
                  }))
            )
         );
   }

   public static RightClickManager getManager() {
      return MANAGER;
   }
}
