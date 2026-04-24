package com.adammp.commands;

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
import meteordevelopment.meteorclient.utils.player.FindItemResult;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.meteorclient.utils.world.BlockUtils;
import meteordevelopment.orbit.EventHandler;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_1792;
import net.minecraft.class_2172;
import net.minecraft.class_2246;
import net.minecraft.class_2248;
import net.minecraft.class_2338;
import net.minecraft.class_2382;
import net.minecraft.class_243;
import net.minecraft.class_2960;
import net.minecraft.class_7923;

@Environment(value=EnvType.CLIENT)
public class FillCommand
extends Command {
    public static class_2338 sel1 = null;
    public static class_2338 sel2 = null;
    private static final Queue<class_2338> queue = new LinkedList<class_2338>();
    private static int tickTimer = 0;
    private static int tickDelay = 2;
    private static int blocksPerTick = 1;
    private static class_1792 itemToPlace;
    private static int xOff;
    private static int yOff;
    private static int zOff;
    private static boolean hollow;
    private static boolean repeatUntilCorrect;
    private static class_243 oldPos;
    private static int lastSummonTick;
    private static String lastRequestedName;
    private static class_2248 blockToPlace;
    private static String upperBlockName;

    public FillCommand() {
        super("mpfill", "Fills an area with blocks (MinePlay style teleport + delay).", new String[0]);
        MeteorClient.EVENT_BUS.subscribe((Object)this);
    }

    public void build(LiteralArgumentBuilder<class_2172> builder) {
        builder.then(FillCommand.argument((String)"block", (ArgumentType)StringArgumentType.word()).then(FillCommand.argument((String)"xOff", (ArgumentType)IntegerArgumentType.integer()).then(FillCommand.argument((String)"yOff", (ArgumentType)IntegerArgumentType.integer()).then(FillCommand.argument((String)"zOff", (ArgumentType)IntegerArgumentType.integer()).then(FillCommand.argument((String)"delay", (ArgumentType)IntegerArgumentType.integer((int)1, (int)40)).then(FillCommand.argument((String)"bpt", (ArgumentType)IntegerArgumentType.integer((int)1, (int)100)).then(FillCommand.argument((String)"hollow", (ArgumentType)BoolArgumentType.bool()).then(FillCommand.argument((String)"repeat", (ArgumentType)BoolArgumentType.bool()).executes(ctx -> {
            if (sel1 == null || sel2 == null) {
                ChatUtils.error((String)"Selections not set! Use .sel1 and .sel2 first.", (Object[])new Object[0]);
                return 1;
            }
            String blockName = StringArgumentType.getString((CommandContext)ctx, (String)"block").toLowerCase();
            blockToPlace = (class_2248)class_7923.field_41175.method_63535(class_2960.method_60655((String)"minecraft", (String)blockName));
            if (blockToPlace == class_2246.field_10124) {
                ChatUtils.error((String)("Invalid block: " + blockName), (Object[])new Object[0]);
                return 1;
            }
            upperBlockName = blockToPlace.method_63499().replace("block.minecraft.", "").replace("item.minecraft.", "").toUpperCase();
            itemToPlace = blockToPlace.method_8389();
            xOff = IntegerArgumentType.getInteger((CommandContext)ctx, (String)"xOff");
            yOff = IntegerArgumentType.getInteger((CommandContext)ctx, (String)"yOff");
            zOff = IntegerArgumentType.getInteger((CommandContext)ctx, (String)"zOff");
            tickDelay = IntegerArgumentType.getInteger((CommandContext)ctx, (String)"delay");
            blocksPerTick = IntegerArgumentType.getInteger((CommandContext)ctx, (String)"bpt");
            hollow = BoolArgumentType.getBool((CommandContext)ctx, (String)"hollow");
            repeatUntilCorrect = BoolArgumentType.getBool((CommandContext)ctx, (String)"repeat");
            class_2338 min = new class_2338(Math.min(sel1.method_10263(), sel2.method_10263()), Math.min(sel1.method_10264(), sel2.method_10264()), Math.min(sel1.method_10260(), sel2.method_10260()));
            class_2338 max = new class_2338(Math.max(sel1.method_10263(), sel2.method_10263()), Math.max(sel1.method_10264(), sel2.method_10264()), Math.max(sel1.method_10260(), sel2.method_10260()));
            oldPos = FillCommand.mc.field_1724.method_19538();
            queue.clear();
            for (int x = min.method_10263(); x <= max.method_10263(); ++x) {
                for (int z = min.method_10260(); z <= max.method_10260(); ++z) {
                    for (int y = min.method_10264(); y <= max.method_10264(); ++y) {
                        if (hollow) {
                            boolean onFace;
                            boolean bl = onFace = x == min.method_10263() || x == max.method_10263() || y == min.method_10264() || y == max.method_10264() || z == min.method_10260() || z == max.method_10260();
                            if (!onFace) continue;
                        }
                        queue.add(new class_2338(x, y, z));
                    }
                }
            }
            tickTimer = 0;
            ChatUtils.info((String)((hollow ? "Queued hollow " : "Queued solid ") + queue.size() + " blocks | delay " + tickDelay + " ticks | " + blocksPerTick + " blocks/tick | repeat: " + repeatUntilCorrect), (Object[])new Object[0]);
            return 1;
        })))))))));
    }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        class_2338 target;
        if (FillCommand.mc.field_1724 == null || FillCommand.mc.field_1687 == null) {
            if (!queue.isEmpty()) {
                queue.clear();
                ChatUtils.info((String)"\u23f9 Fill stopped (world unloaded or kicked).", (Object[])new Object[0]);
            }
            return;
        }
        if (queue.isEmpty()) {
            if (repeatUntilCorrect && sel1 != null && sel2 != null) {
                class_2338 min = new class_2338(Math.min(sel1.method_10263(), sel2.method_10263()), Math.min(sel1.method_10264(), sel2.method_10264()), Math.min(sel1.method_10260(), sel2.method_10260()));
                class_2338 max = new class_2338(Math.max(sel1.method_10263(), sel2.method_10263()), Math.max(sel1.method_10264(), sel2.method_10264()), Math.max(sel1.method_10260(), sel2.method_10260()));
                for (int x = min.method_10263(); x <= max.method_10263(); ++x) {
                    for (int z = min.method_10260(); z <= max.method_10260(); ++z) {
                        for (int y = min.method_10264(); y <= max.method_10264(); ++y) {
                            class_2338 pos = new class_2338(x, y, z);
                            if (hollow) {
                                boolean onFace;
                                boolean bl = onFace = x == min.method_10263() || x == max.method_10263() || y == min.method_10264() || y == max.method_10264() || z == min.method_10260() || z == max.method_10260();
                                if (!onFace) continue;
                            }
                            if (!FillCommand.mc.field_1687.method_8320(pos).method_26215()) continue;
                            queue.add(pos);
                        }
                    }
                }
                if (queue.isEmpty()) {
                    repeatUntilCorrect = false;
                    ChatUtils.info((String)"\u2705 Region is now correct. Done!", (Object[])new Object[0]);
                    FillCommand.mc.field_1724.method_30634(FillCommand.oldPos.field_1352, FillCommand.oldPos.field_1351, FillCommand.oldPos.field_1350);
                } else {
                    ChatUtils.info((String)("\u267b\ufe0f Rechecking region: " + queue.size() + " blocks missing, retrying..."), (Object[])new Object[0]);
                }
            }
            return;
        }
        if (tickTimer++ < tickDelay) {
            return;
        }
        tickTimer = 0;
        int placedCount = 0;
        while (placedCount < blocksPerTick && !queue.isEmpty() && (target = queue.poll()) != null) {
            if (!FillCommand.mc.field_1687.method_8320(target).method_26215()) continue;
            class_243 tp = class_243.method_24953((class_2382)target).method_1031((double)xOff, (double)yOff, (double)zOff);
            FillCommand.mc.field_1724.method_30634(tp.field_1352, tp.field_1351, tp.field_1350);
            FindItemResult firNow = InvUtils.findInHotbar((class_1792[])new class_1792[]{itemToPlace});
            if (!firNow.found() && !this.hasAnyInInventory(itemToPlace)) {
                int now = FillCommand.mc.field_1724.field_6012;
                if (!upperBlockName.equals(lastRequestedName) || now - lastSummonTick > 40) {
                    ChatUtils.sendPlayerMsg((String)("/i " + upperBlockName));
                    ChatUtils.info((String)("Auto-summoning /i " + upperBlockName + "..."), (Object[])new Object[0]);
                    lastRequestedName = upperBlockName;
                    lastSummonTick = now;
                }
            }
            if (!firNow.found()) {
                ChatUtils.info((String)("\u231b Waiting for /i " + upperBlockName + "..."), (Object[])new Object[0]);
                return;
            }
            InvUtils.swap((int)firNow.slot(), (boolean)true);
            boolean placed = BlockUtils.place((class_2338)target, (FindItemResult)firNow, (boolean)true, (int)0, (boolean)true, (boolean)true, (boolean)true);
            if (!placed) {
                ChatUtils.info((String)("\u26a0\ufe0f Failed to place at " + target.method_23854()), (Object[])new Object[0]);
                continue;
            }
            ++placedCount;
        }
    }

    private boolean hasAnyInInventory(class_1792 item) {
        return InvUtils.find((class_1792[])new class_1792[]{item}).found() || InvUtils.findInHotbar((class_1792[])new class_1792[]{item}).found();
    }

    public static void init() {
        Commands.add((Command)new Sel1Command());
        Commands.add((Command)new Sel2Command());
        Commands.add((Command)new FillCommand());
    }

    static {
        repeatUntilCorrect = false;
        lastSummonTick = 0;
        lastRequestedName = "";
    }

    @Environment(value=EnvType.CLIENT)
    public static class Sel1Command
    extends Command {
        public Sel1Command() {
            super("sel1", "Set first selection point.", new String[0]);
        }

        public void build(LiteralArgumentBuilder<class_2172> builder) {
            builder.executes(ctx -> {
                sel1 = Sel1Command.mc.field_1724.method_24515();
                ChatUtils.info((String)("Selection 1 set to " + sel1.method_23854()), (Object[])new Object[0]);
                return 1;
            });
        }
    }

    @Environment(value=EnvType.CLIENT)
    public static class Sel2Command
    extends Command {
        public Sel2Command() {
            super("sel2", "Set second selection point.", new String[0]);
        }

        public void build(LiteralArgumentBuilder<class_2172> builder) {
            builder.executes(ctx -> {
                sel2 = Sel2Command.mc.field_1724.method_24515();
                ChatUtils.info((String)("Selection 2 set to " + sel2.method_23854()), (Object[])new Object[0]);
                return 1;
            });
        }
    }
}

