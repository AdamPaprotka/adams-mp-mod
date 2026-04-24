package com.adammp.commands;

import com.adammp.commands.FillCommand;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
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
import net.minecraft.class_2338;
import net.minecraft.class_243;

@Environment(value=EnvType.CLIENT)
public class GreifCommand
extends Command {
    private static final Queue<class_2338> queue = new LinkedList<class_2338>();
    private static int tickDelay = 2;
    private static int tickTimer = 0;

    public GreifCommand() {
        super("greif", "Breaks all non-air blocks in the selection from mpfill.", new String[0]);
        MeteorClient.EVENT_BUS.subscribe((Object)this);
    }

    public void build(LiteralArgumentBuilder<class_2172> builder) {
        builder.executes(ctx -> {
            if (FillCommand.sel1 == null || FillCommand.sel2 == null) {
                ChatUtils.error((String)"Selections not set! Use .sel1 and .sel2 first.", (Object[])new Object[0]);
                return 1;
            }
            class_2338 min = new class_2338(Math.min(FillCommand.sel1.method_10263(), FillCommand.sel2.method_10263()), Math.min(FillCommand.sel1.method_10264(), FillCommand.sel2.method_10264()), Math.min(FillCommand.sel1.method_10260(), FillCommand.sel2.method_10260()));
            class_2338 max = new class_2338(Math.max(FillCommand.sel1.method_10263(), FillCommand.sel2.method_10263()), Math.max(FillCommand.sel1.method_10264(), FillCommand.sel2.method_10264()), Math.max(FillCommand.sel1.method_10260(), FillCommand.sel2.method_10260()));
            queue.clear();
            for (int x = min.method_10263(); x <= max.method_10263(); ++x) {
                for (int y = min.method_10264(); y <= max.method_10264(); ++y) {
                    for (int z = min.method_10260(); z <= max.method_10260(); ++z) {
                        class_2338 pos = new class_2338(x, y, z);
                        if (GreifCommand.mc.field_1687.method_8320(pos).method_26215()) continue;
                        queue.add(pos);
                    }
                }
            }
            tickTimer = 0;
            ChatUtils.info((String)("Queued " + queue.size() + " blocks to break."), (Object[])new Object[0]);
            return 1;
        });
    }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        if (GreifCommand.mc.field_1724 == null || GreifCommand.mc.field_1687 == null) {
            return;
        }
        if (queue.isEmpty()) {
            return;
        }
        if (tickTimer++ < tickDelay) {
            return;
        }
        tickTimer = 0;
        class_2338 target = queue.poll();
        if (target == null) {
            return;
        }
        class_243 tp = new class_243((double)target.method_10263() + 0.5, (double)(target.method_10264() + 1), (double)target.method_10260() + 0.5);
        GreifCommand.mc.field_1724.method_30634(tp.field_1352, tp.field_1351, tp.field_1350);
        BlockUtils.breakBlock((class_2338)target, (boolean)true);
        if (queue.isEmpty()) {
            ChatUtils.info((String)"Finished griefing selection.", (Object[])new Object[0]);
        }
    }

    public static void init() {
        Commands.add((Command)new GreifCommand());
    }
}

