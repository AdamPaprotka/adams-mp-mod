package com.adammp.commands;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import java.util.ArrayList;
import java.util.List;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.commands.Command;
import meteordevelopment.meteorclient.commands.Commands;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import meteordevelopment.meteorclient.utils.player.FindItemResult;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.meteorclient.utils.world.BlockUtils;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_1792;
import net.minecraft.class_1802;
import net.minecraft.class_2172;
import net.minecraft.class_2246;
import net.minecraft.class_2338;
import net.minecraft.class_2374;
import net.minecraft.class_243;
import net.minecraft.class_638;
import net.minecraft.class_742;
import net.minecraft.class_746;

@Environment(value=EnvType.CLIENT)
public class TrapCommand
extends Command {
    private static boolean trapping = false;
    private static class_742 target;
    private static final List<class_2338> boxPositions;

    public TrapCommand() {
        super("trap", "Traps a player in a 5x5x5 glass box and rebuilds it if broken.", new String[0]);
    }

    public static void init() {
        Commands.add((Command)new TrapCommand());
        Commands.add((Command)new StopTrapCommand());
    }

    public void build(LiteralArgumentBuilder<class_2172> builder) {
        builder.then(TrapCommand.argument((String)"player", (ArgumentType)StringArgumentType.string()).executes(ctx -> {
            String name = (String)ctx.getArgument("player", String.class);
            target = MeteorClient.mc.field_1687.method_18456().stream().filter(p -> p.method_5477().getString().equalsIgnoreCase(name)).findFirst().orElse(null);
            this.startTrap();
            return 1;
        }));
    }

    private void startTrap() {
        if (trapping) {
            this.warning("Already trapping someone!", new Object[0]);
            return;
        }
        if (target == null) {
            this.warning("Target player not found!", new Object[0]);
            return;
        }
        trapping = true;
        this.info("Started trapping " + target.method_5477().getString() + ".", new Object[0]);
        new Thread(() -> {
            class_746 self = MeteorClient.mc.field_1724;
            class_638 world = MeteorClient.mc.field_1687;
            boolean used = false;
            while (trapping && target.method_5805()) {
                try {
                    class_243 pos = target.method_19538();
                    class_2338 center = class_2338.method_49638((class_2374)pos.method_1031(0.0, 1.0, 0.0));
                    boxPositions.clear();
                    int r = 2;
                    for (int x = -r; x <= r; ++x) {
                        for (int y = -r; y <= r; ++y) {
                            for (int z = -r; z <= r; ++z) {
                                if (Math.abs(x) <= 1 && Math.abs(z) <= 1 && y >= -1 && y <= 1) continue;
                                boxPositions.add(center.method_10069(x, y, z));
                            }
                        }
                    }
                    FindItemResult glass = InvUtils.findInHotbar((class_1792[])new class_1792[]{class_1802.field_8280});
                    if (!glass.found()) {
                        this.info("Out of glass! Running /i GLASS", new Object[0]);
                        ChatUtils.sendPlayerMsg((String)"/i GLASS");
                        Thread.sleep(1L);
                        continue;
                    }
                    class_243 oldPos = self.method_19538();
                    if (!used) {
                        self.method_30634(pos.field_1352, pos.field_1351 - 3.0, pos.field_1350);
                    }
                    for (class_2338 bp : boxPositions) {
                        if (!world.method_8320(bp).method_26215()) continue;
                        BlockUtils.place((class_2338)bp, (FindItemResult)glass, (int)0);
                    }
                    Thread.sleep(1L);
                    if (!used) {
                        self.method_30634(oldPos.field_1352, oldPos.field_1351, oldPos.field_1350);
                        used = true;
                    }
                    for (class_2338 bp : boxPositions) {
                        if (world.method_8320(bp).method_26204() == class_2246.field_10033) continue;
                        self.method_30634(pos.field_1352, pos.field_1351 + 2.0, pos.field_1350);
                        Thread.sleep(1L);
                        BlockUtils.place((class_2338)bp, (FindItemResult)glass, (int)0);
                        Thread.sleep(1L);
                        self.method_30634(oldPos.field_1352, oldPos.field_1351, oldPos.field_1350);
                    }
                    Thread.sleep(1L);
                }
                catch (Exception e) {
                    this.error("Trap loop crashed: " + e.getMessage(), new Object[0]);
                    e.printStackTrace();
                    trapping = false;
                }
            }
            this.info("Stopped trapping (target left or trap stopped).", new Object[0]);
        }, "TrapThread").start();
    }

    static {
        boxPositions = new ArrayList<class_2338>();
    }

    @Environment(value=EnvType.CLIENT)
    private static class StopTrapCommand
    extends Command {
        public StopTrapCommand() {
            super("stoptrap", "Stops the current trap loop.", new String[0]);
        }

        public void build(LiteralArgumentBuilder<class_2172> builder) {
            builder.executes(ctx -> {
                trapping = false;
                this.info("Trap stopped.", new Object[0]);
                return 1;
            });
        }
    }
}

