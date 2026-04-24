package com.adammp.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import fi.dy.masa.litematica.world.SchematicWorldHandler;
import fi.dy.masa.litematica.world.WorldSchematic;
import java.util.HashSet;
import meteordevelopment.meteorclient.commands.Command;
import meteordevelopment.meteorclient.commands.Commands;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.utils.player.FindItemResult;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.meteorclient.utils.world.BlockUtils;
import meteordevelopment.orbit.EventHandler;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_1792;
import net.minecraft.class_2172;
import net.minecraft.class_2338;
import net.minecraft.class_2382;
import net.minecraft.class_239;
import net.minecraft.class_243;
import net.minecraft.class_2680;
import net.minecraft.class_3965;

@Environment(value=EnvType.CLIENT)
public class SecureCommand
extends Command {
    private static boolean enabled = false;
    private static int tickCounter = 0;
    private static final HashSet<class_2338> tracked = new HashSet();
    private static class_243 originalPos = null;
    private static boolean returning = false;
    private static final int TICK_INTERVAL = 5;
    private static final int CAPTURE_RADIUS = 6;
    private static final double TP_DIST_SQ = 36.0;

    public static void init() {
        Commands.add((Command)new SecureCommand());
    }

    public SecureCommand() {
        super("secure", "Auto-fix schematic blocks (on/off)", new String[0]);
    }

    public void build(LiteralArgumentBuilder<class_2172> builder) {
        ((LiteralArgumentBuilder)builder.then(SecureCommand.literal((String)"on").executes(this::enable))).then(SecureCommand.literal((String)"off").executes(this::disable));
    }

    private int enable(CommandContext<class_2172> ctx) {
        enabled = true;
        tracked.clear();
        originalPos = null;
        returning = false;
        this.info("Secure enabled", new Object[0]);
        return 1;
    }

    private int disable(CommandContext<class_2172> ctx) {
        enabled = false;
        tracked.clear();
        originalPos = null;
        returning = false;
        this.info("Secure disabled", new Object[0]);
        return 1;
    }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        if (!enabled || SecureCommand.mc.field_1724 == null || SecureCommand.mc.field_1687 == null) {
            return;
        }
        if (++tickCounter % 5 != 0) {
            return;
        }
        WorldSchematic schematic = SchematicWorldHandler.getSchematicWorld();
        if (schematic == null) {
            return;
        }
        this.captureLookedArea(schematic);
        for (class_2338 pos : tracked) {
            class_2680 actual;
            class_2680 required = schematic.method_8320(pos);
            if (required == null || required.method_26215() || (actual = SecureCommand.mc.field_1687.method_8320(pos)).method_26204() == required.method_26204()) continue;
            if (originalPos == null) {
                originalPos = SecureCommand.mc.field_1724.method_19538();
                returning = true;
            }
            this.teleportNearIfFar(pos);
            class_1792 item = required.method_26204().method_8389();
            if (item == null) {
                return;
            }
            FindItemResult any = InvUtils.find((class_1792[])new class_1792[]{item});
            if (!any.found()) {
                return;
            }
            FindItemResult hotbar = InvUtils.findInHotbar((class_1792[])new class_1792[]{item});
            if (!hotbar.found()) {
                InvUtils.move().from(any.slot()).toHotbar(SecureCommand.mc.field_1724.method_31548().field_7545);
                hotbar = InvUtils.findInHotbar((class_1792[])new class_1792[]{item});
            }
            BlockUtils.place((class_2338)pos, (FindItemResult)hotbar, (boolean)false, (int)0, (boolean)true, (boolean)true, (boolean)true);
            this.returnToOriginal();
            break;
        }
    }

    private void captureLookedArea(WorldSchematic schematic) {
        class_239 hit = SecureCommand.mc.field_1724.method_5745(6.0, 0.0f, false);
        if (hit.method_17783() != class_239.class_240.field_1332) {
            return;
        }
        class_2338 center = ((class_3965)hit).method_17777();
        for (class_2338 pos : class_2338.method_10097((class_2338)center.method_10069(-6, -6, -6), (class_2338)center.method_10069(6, 6, 6))) {
            class_2680 req = schematic.method_8320(pos);
            if (req == null || req.method_26215()) continue;
            tracked.add(pos.method_10062());
        }
    }

    private void teleportNearIfFar(class_2338 pos) {
        class_243 c = class_243.method_24953((class_2382)pos);
        if (SecureCommand.mc.field_1724.method_5707(c) <= 36.0) {
            return;
        }
        SecureCommand.mc.field_1724.method_30634(c.field_1352, c.field_1351 + 1.0, c.field_1350);
    }

    private void returnToOriginal() {
        if (!returning || originalPos == null) {
            return;
        }
        SecureCommand.mc.field_1724.method_30634(SecureCommand.originalPos.field_1352, SecureCommand.originalPos.field_1351, SecureCommand.originalPos.field_1350);
        originalPos = null;
        returning = false;
    }
}

