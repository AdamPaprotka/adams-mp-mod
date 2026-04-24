package com.adammp.modules;

import com.adammp.AdamsMPmodClient;
import fi.dy.masa.litematica.world.SchematicWorldHandler;
import fi.dy.masa.litematica.world.WorldSchematic;
import java.util.List;
import java.util.Locale;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.BlockListSetting;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import meteordevelopment.meteorclient.utils.player.FindItemResult;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.meteorclient.utils.world.BlockUtils;
import meteordevelopment.orbit.EventHandler;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_1792;
import net.minecraft.class_2248;
import net.minecraft.class_2338;
import net.minecraft.class_2382;
import net.minecraft.class_243;
import net.minecraft.class_2680;
import net.minecraft.class_7923;

@Environment(value=EnvType.CLIENT)
public class LitematicaPrinterHelper
extends Module {
    private final SettingGroup sgGeneral;
    private final Setting<Boolean> teleporter;
    private final Setting<Boolean> printer;
    private final Setting<Boolean> breakWrongBlocks;
    private final Setting<Boolean> autoSummon;
    private final Setting<Integer> delay;
    private final Setting<Integer> radius;
    private final Setting<Integer> summonCooldown;
    private final Setting<List<class_2248>> ignoreBlocks;
    private final Setting<Double> offsetX;
    private final Setting<Double> offsetY;
    private final Setting<Double> offsetZ;
    private int tickTimer;
    private int lastSummonTick;
    private String lastRequestedName;
    private class_2338 breakingPos;
    private class_2248 lastRequiredAtBreaking;

    public LitematicaPrinterHelper() {
        super(AdamsMPmodClient.CATEGORY, "litematica-helper", "Teleport or place missing Litematica schematic blocks.");
        this.sgGeneral = this.settings.getDefaultGroup();
        this.teleporter = this.sgGeneral.add((Setting)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("teleporter")).description("Teleport to missing schematic blocks.")).defaultValue((Object)false)).build());
        this.printer = this.sgGeneral.add((Setting)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("printer")).description("Automatically place missing schematic blocks.")).defaultValue((Object)false)).build());
        this.breakWrongBlocks = this.sgGeneral.add((Setting)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("break-wrong-blocks")).description("Automatically mine blocks that don\u2019t match the schematic at the target position.")).defaultValue((Object)true)).build());
        this.autoSummon = this.sgGeneral.add((Setting)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("auto-summon")).description("If the required block isn't in your inventory/hotbar, run /i <BLOCK_NAME>.")).defaultValue((Object)true)).build());
        this.delay = this.sgGeneral.add((Setting)((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)new IntSetting.Builder().name("delay")).description("Delay between actions in ticks (2 = 0.1s).")).defaultValue((Object)2)).min(1).sliderMin(1).max(40).sliderMax(40).build());
        this.radius = this.sgGeneral.add((Setting)((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)new IntSetting.Builder().name("search-radius")).description("How far to search for missing blocks.")).defaultValue((Object)5)).min(1).sliderMin(1).max(20).sliderMax(20).build());
        this.summonCooldown = this.sgGeneral.add((Setting)((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)new IntSetting.Builder().name("summon-cooldown")).description("Minimum ticks between /i commands.")).defaultValue((Object)20)).min(5).sliderMin(5).max(200).sliderMax(200).build());
        this.ignoreBlocks = this.sgGeneral.add((Setting)((BlockListSetting.Builder)((BlockListSetting.Builder)((BlockListSetting.Builder)new BlockListSetting.Builder().name("ignore-blocks")).description("Schematic blocks to skip (no teleporting or printing).")).defaultValue(List.of())).build());
        this.offsetX = this.sgGeneral.add((Setting)((DoubleSetting.Builder)((DoubleSetting.Builder)new DoubleSetting.Builder().name("offset-x")).description("Offset from target block on X axis.")).defaultValue(0.0).min(-5.0).sliderMin(-5.0).max(5.0).sliderMax(5.0).build());
        this.offsetY = this.sgGeneral.add((Setting)((DoubleSetting.Builder)((DoubleSetting.Builder)new DoubleSetting.Builder().name("offset-y")).description("Offset from target block on Y axis.")).defaultValue(1.0).min(-5.0).sliderMin(-5.0).max(5.0).sliderMax(5.0).build());
        this.offsetZ = this.sgGeneral.add((Setting)((DoubleSetting.Builder)((DoubleSetting.Builder)new DoubleSetting.Builder().name("offset-z")).description("Offset from target block on Z axis.")).defaultValue(0.0).min(-5.0).sliderMin(-5.0).max(5.0).sliderMax(5.0).build());
        this.tickTimer = 0;
        this.lastSummonTick = -9999;
        this.lastRequestedName = "";
        this.breakingPos = null;
        this.lastRequiredAtBreaking = null;
    }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        String upperBlockName;
        int now;
        if (this.mc.field_1724 == null || this.mc.field_1687 == null) {
            return;
        }
        WorldSchematic schematic = SchematicWorldHandler.getSchematicWorld();
        if (schematic == null) {
            return;
        }
        if (((Boolean)this.breakWrongBlocks.get()).booleanValue() && this.breakingPos != null) {
            class_2680 cur = this.mc.field_1687.method_8320(this.breakingPos);
            if (cur.method_26215() || this.lastRequiredAtBreaking != null && cur.method_26204() == this.lastRequiredAtBreaking) {
                this.breakingPos = null;
                this.lastRequiredAtBreaking = null;
            } else {
                class_243 center;
                if (((Boolean)this.teleporter.get()).booleanValue() && this.mc.field_1724.method_5707(center = class_243.method_24953((class_2382)this.breakingPos)) > 36.0) {
                    this.mc.field_1724.method_30634(center.field_1352 + (Double)this.offsetX.get(), center.field_1351 + (Double)this.offsetY.get(), center.field_1350 + (Double)this.offsetZ.get());
                }
                this.attemptBreak(this.breakingPos);
            }
        }
        if (this.tickTimer++ < (Integer)this.delay.get()) {
            return;
        }
        this.tickTimer = 0;
        class_2338 playerPos = this.mc.field_1724.method_24515();
        class_2338 target = null;
        double closest = Double.MAX_VALUE;
        class_2680 requiredState = null;
        for (class_2338 pos : class_2338.method_10097((class_2338)playerPos.method_10069(-((Integer)this.radius.get()).intValue(), -((Integer)this.radius.get()).intValue(), -((Integer)this.radius.get()).intValue()), (class_2338)playerPos.method_10069(((Integer)this.radius.get()).intValue(), ((Integer)this.radius.get()).intValue(), ((Integer)this.radius.get()).intValue()))) {
            double dist;
            class_2680 actual;
            class_2680 required = schematic.method_8320(pos);
            if (required == null || required.method_26215() || ((List)this.ignoreBlocks.get()).contains(required.method_26204()) || (actual = this.mc.field_1687.method_8320(pos)).method_26204() == required.method_26204() || !((dist = playerPos.method_10262((class_2382)pos)) < closest)) continue;
            closest = dist;
            target = pos.method_10062();
            requiredState = required;
        }
        if (target == null) {
            return;
        }
        if (((Boolean)this.teleporter.get()).booleanValue()) {
            class_243 center = class_243.method_24953(target);
            this.mc.field_1724.method_30634(center.field_1352 + (Double)this.offsetX.get(), center.field_1351 + (Double)this.offsetY.get(), center.field_1350 + (Double)this.offsetZ.get());
        }
        if (requiredState == null) {
            return;
        }
        class_2680 actualAtTarget = this.mc.field_1687.method_8320(target);
        if (((Boolean)this.breakWrongBlocks.get()).booleanValue() && !actualAtTarget.method_26215() && actualAtTarget.method_26204() != requiredState.method_26204()) {
            this.breakingPos = target.method_10062();
            this.lastRequiredAtBreaking = requiredState.method_26204();
            this.attemptBreak(this.breakingPos);
            return;
        }
        class_1792 item = requiredState.method_26204().method_8389();
        FindItemResult firHotbar = item != null ? InvUtils.findInHotbar((class_1792[])new class_1792[]{item}) : InvUtils.findEmpty();
        boolean haveInHotbar = firHotbar.found();
        FindItemResult firAny = !haveInHotbar && item != null ? InvUtils.find((class_1792[])new class_1792[]{item}) : firHotbar;
        boolean haveInInventory = firAny.found();
        if (((Boolean)this.autoSummon.get()).booleanValue() && item != null && !haveInInventory && this.shouldSendSummon(now = this.mc.field_1724.field_6012, upperBlockName = this.getUpperSnakeName(requiredState.method_26204()))) {
            ChatUtils.sendPlayerMsg((String)("/i " + upperBlockName));
            this.lastSummonTick = now;
            this.lastRequestedName = upperBlockName;
        }
        if (((Boolean)this.printer.get()).booleanValue()) {
            if (((List)this.ignoreBlocks.get()).contains(requiredState.method_26204())) {
                return;
            }
            if (firAny.found()) {
                InvUtils.swap((int)firAny.slot(), (boolean)true);
                BlockUtils.place((class_2338)target, (FindItemResult)firAny, (boolean)true, (int)0, (boolean)true, (boolean)true, (boolean)true);
            }
        }
    }

    private String getUpperSnakeName(class_2248 block) {
        String path = class_7923.field_41175.method_10221((Object)block).method_12832();
        return path.toUpperCase(Locale.ROOT);
    }

    private boolean shouldSendSummon(int nowTick, String name) {
        if (!name.equals(this.lastRequestedName)) {
            return true;
        }
        return nowTick - this.lastSummonTick >= (Integer)this.summonCooldown.get();
    }

    private void attemptBreak(class_2338 pos) {
        if (this.mc.field_1724 == null || this.mc.field_1687 == null || pos == null) {
            return;
        }
        BlockUtils.breakBlock((class_2338)pos, (boolean)true);
    }
}

