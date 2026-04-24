package com.adammp.commands;

import com.adammp.commands.FillCommand;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import java.util.LinkedList;
import java.util.Queue;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.commands.Command;
import meteordevelopment.meteorclient.commands.Commands;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import meteordevelopment.meteorclient.utils.world.BlockUtils;
import meteordevelopment.orbit.EventHandler;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_2172;
import net.minecraft.class_2246;
import net.minecraft.class_2248;
import net.minecraft.class_2338;
import net.minecraft.class_2382;
import net.minecraft.class_243;
import net.minecraft.class_2960;
import net.minecraft.class_7923;

@Environment(value=EnvType.CLIENT)
public class ExcavateCommand
extends Command {
    private static final Queue<class_2338> queue = new LinkedList<class_2338>();
    private static int tickTimer = 0;
    private static int tickDelay = 2;
    private static int blocksPerSecond = 1;
    private static boolean repeat = false;
    private static boolean waitForBreak = false;
    private static boolean usePackets = false;
    private static boolean whitelist = false;
    private static class_2248 whitelistBlock = null;
    private static boolean running = false;
    private static class_243 oldPos;

    public ExcavateCommand() {
        super("excavate", "Clears all blocks between .sel1 and .sel2 (MinePlay style).", new String[0]);
        MeteorClient.EVENT_BUS.subscribe((Object)this);
    }

    public void build(LiteralArgumentBuilder<class_2172> builder) {
        builder.then(ExcavateCommand.argument((String)"speed", (ArgumentType)IntegerArgumentType.integer((int)1, (int)40)).then(ExcavateCommand.argument((String)"bps", (ArgumentType)IntegerArgumentType.integer((int)1, (int)20)).then(ExcavateCommand.argument((String)"repeat", (ArgumentType)BoolArgumentType.bool()).then(ExcavateCommand.argument((String)"wait", (ArgumentType)BoolArgumentType.bool()).then(ExcavateCommand.argument((String)"packets", (ArgumentType)BoolArgumentType.bool()).then(ExcavateCommand.argument((String)"includeWhitelist", (ArgumentType)BoolArgumentType.bool()).then(ExcavateCommand.argument((String)"blockName", (ArgumentType)StringArgumentType.word()).executes(ctx -> {
            if (FillCommand.sel1 == null || FillCommand.sel2 == null) {
                ChatUtils.error((String)"Selections not set! Use .sel1 and .sel2 first.", (Object[])new Object[0]);
                return 1;
            }
            tickDelay = IntegerArgumentType.getInteger((CommandContext)ctx, (String)"speed");
            blocksPerSecond = IntegerArgumentType.getInteger((CommandContext)ctx, (String)"bps");
            repeat = BoolArgumentType.getBool((CommandContext)ctx, (String)"repeat");
            waitForBreak = BoolArgumentType.getBool((CommandContext)ctx, (String)"wait");
            usePackets = BoolArgumentType.getBool((CommandContext)ctx, (String)"packets");
            whitelist = BoolArgumentType.getBool((CommandContext)ctx, (String)"includeWhitelist");
            whitelistBlock = null;
            if (whitelist) {
                String blockName = StringArgumentType.getString((CommandContext)ctx, (String)"blockName").toLowerCase();
                class_2248 blockObj = (class_2248)class_7923.field_41175.method_63535(class_2960.method_60655((String)"minecraft", (String)blockName));
                if (blockObj == class_2246.field_10124) {
                    ChatUtils.error((String)("Invalid whitelist block: " + blockName), (Object[])new Object[0]);
                    return 1;
                }
                whitelistBlock = blockObj;
            }
            oldPos = ExcavateCommand.mc.field_1724.method_19538();
            ExcavateCommand.enqueueAll();
            tickTimer = 0;
            if (queue.isEmpty()) {
                ChatUtils.info((String)"Nothing to excavate (all air or no matching whitelist).", (Object[])new Object[0]);
                return 1;
            }
            running = true;
            ChatUtils.info((String)("Excavating " + queue.size() + " blocks | delay=" + tickDelay + " | bps=" + blocksPerSecond + " | repeat=" + repeat + " | wait=" + waitForBreak + " | packets=" + usePackets + (String)(whitelist ? " | whitelist=" + whitelistBlock.method_9518().getString() : "")), (Object[])new Object[0]);
            return 1;
        }))))))));
    }

    private static void enqueueAll() {
        class_2338 min = new class_2338(Math.min(FillCommand.sel1.method_10263(), FillCommand.sel2.method_10263()), Math.min(FillCommand.sel1.method_10264(), FillCommand.sel2.method_10264()), Math.min(FillCommand.sel1.method_10260(), FillCommand.sel2.method_10260()));
        class_2338 max = new class_2338(Math.max(FillCommand.sel1.method_10263(), FillCommand.sel2.method_10263()), Math.max(FillCommand.sel1.method_10264(), FillCommand.sel2.method_10264()), Math.max(FillCommand.sel1.method_10260(), FillCommand.sel2.method_10260()));
        queue.clear();
        for (int x = min.method_10263(); x <= max.method_10263(); ++x) {
            for (int z = min.method_10260(); z <= max.method_10260(); ++z) {
                for (int y = min.method_10264(); y <= max.method_10264(); ++y) {
                    class_2338 pos = new class_2338(x, y, z);
                    class_2248 block = ExcavateCommand.mc.field_1687.method_8320(pos).method_26204();
                    if (block == class_2246.field_10124 || whitelist && block != whitelistBlock) continue;
                    queue.add(pos);
                }
            }
        }
    }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        if (!running) {
            return;
        }
        if (ExcavateCommand.mc.field_1724 == null || ExcavateCommand.mc.field_1687 == null) {
            return;
        }
        if (queue.isEmpty()) {
            if (repeat) {
                ExcavateCommand.enqueueAll();
                if (queue.isEmpty()) {
                    ExcavateCommand.stopExcavate("Finished excavating (no more blocks).");
                }
            }
            return;
        }
        if (tickTimer++ < tickDelay) {
            return;
        }
        tickTimer = 0;
        for (int i = 0; i < blocksPerSecond && !queue.isEmpty(); ++i) {
            class_2338 target = queue.peek();
            if (target == null) {
                return;
            }
            if (waitForBreak && !ExcavateCommand.mc.field_1687.method_8320(target).method_26215()) {
                ExcavateCommand.breakBlock(target);
                return;
            }
            queue.poll();
            ExcavateCommand.breakBlock(target);
        }
        if (queue.isEmpty() && !repeat) {
            ExcavateCommand.stopExcavate("Finished excavating!");
        }
    }

    private static void breakBlock(class_2338 pos) {
        class_243 tp = class_243.method_24953((class_2382)pos);
        ExcavateCommand.mc.field_1724.method_30634(tp.field_1352, tp.field_1351, tp.field_1350);
        if (usePackets) {
            ExcavateCommand.mc.field_1761.method_2910(pos, ExcavateCommand.mc.field_1724.method_5735());
            ExcavateCommand.mc.field_1761.method_2899(pos);
        } else {
            BlockUtils.breakBlock((class_2338)pos, (boolean)true);
        }
    }

    private static void stopExcavate(String reason) {
        running = false;
        queue.clear();
        if (oldPos != null && ExcavateCommand.mc.field_1724 != null) {
            ExcavateCommand.mc.field_1724.method_30634(ExcavateCommand.oldPos.field_1352, ExcavateCommand.oldPos.field_1351, ExcavateCommand.oldPos.field_1350);
        }
        oldPos = null;
        ChatUtils.info((String)reason, (Object[])new Object[0]);
    }

    public static void init() {
        Commands.add((Command)new ExcavateCommand());
        Commands.add((Command)new StopExcavateCommand());
    }

    @Environment(value=EnvType.CLIENT)
    public static class StopExcavateCommand
    extends Command {
        public StopExcavateCommand() {
            super("stopexcavate", "Stops the current excavation.", new String[0]);
        }

        public void build(LiteralArgumentBuilder<class_2172> builder) {
            builder.executes(ctx -> {
                if (!running) {
                    ChatUtils.info((String)"No excavation running.", (Object[])new Object[0]);
                    return 1;
                }
                ExcavateCommand.stopExcavate("Excavation stopped by user.");
                return 1;
            });
        }
    }
}

