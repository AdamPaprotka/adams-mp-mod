package com.adammp.modules;

import com.adammp.AdamsMPmodClient;
import fi.dy.masa.litematica.world.SchematicWorldHandler;
import fi.dy.masa.litematica.world.WorldSchematic;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.world.BlockUtils;
import meteordevelopment.orbit.EventHandler;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_2246;
import net.minecraft.class_2338;
import net.minecraft.class_2382;
import net.minecraft.class_243;
import net.minecraft.class_2680;

@Environment(value=EnvType.CLIENT)
public class AntiSchematicGrief
extends Module {
    private final SettingGroup sgGeneral;
    private final Setting<Integer> radius;
    private final Setting<Integer> delay;
    private final Setting<Integer> blocksPerStep;
    private final Setting<Integer> minY;
    private final Setting<Double> offsetX;
    private final Setting<Double> offsetY;
    private final Setting<Double> offsetZ;
    private int tickTimer;

    public AntiSchematicGrief() {
        super(AdamsMPmodClient.CATEGORY, "anti-schematic-grief", "Breaks blocks that do NOT exist in your Litematica schematic (anti-grief).");
        this.sgGeneral = this.settings.getDefaultGroup();
        this.radius = this.sgGeneral.add((Setting)((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)new IntSetting.Builder().name("search-radius")).description("How far around you to scan for grief blocks.")).defaultValue((Object)8)).min(2).sliderMin(2).max(32).sliderMax(24).build());
        this.delay = this.sgGeneral.add((Setting)((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)new IntSetting.Builder().name("delay-ticks")).description("Delay between breaking attempts (ticks).")).defaultValue((Object)4)).min(1).sliderMin(1).max(40).sliderMax(40).build());
        this.blocksPerStep = this.sgGeneral.add((Setting)((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)new IntSetting.Builder().name("blocks-per-step")).description("How many grief blocks to break each step.")).defaultValue((Object)2)).min(1).sliderMin(1).max(10).sliderMax(5).build());
        this.minY = this.sgGeneral.add((Setting)((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)new IntSetting.Builder().name("min-y")).description("Ignore blocks below this Y (grass below 0 is always ignored).")).defaultValue((Object)-64)).min(-2032).sliderMin(-64).max(320).sliderMax(320).build());
        this.offsetX = this.sgGeneral.add((Setting)((DoubleSetting.Builder)new DoubleSetting.Builder().name("offset-x")).defaultValue(0.0).min(-5.0).max(5.0).sliderMin(-2.0).sliderMax(2.0).build());
        this.offsetY = this.sgGeneral.add((Setting)((DoubleSetting.Builder)new DoubleSetting.Builder().name("offset-y")).defaultValue(1.0).min(-5.0).max(5.0).sliderMin(-2.0).sliderMax(2.0).build());
        this.offsetZ = this.sgGeneral.add((Setting)((DoubleSetting.Builder)new DoubleSetting.Builder().name("offset-z")).defaultValue(0.0).min(-5.0).max(5.0).sliderMin(-2.0).sliderMax(2.0).build());
        this.tickTimer = 0;
    }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        class_2338 target;
        if (this.mc.field_1724 == null || this.mc.field_1687 == null) {
            return;
        }
        WorldSchematic schematic = SchematicWorldHandler.getSchematicWorld();
        if (schematic == null) {
            return;
        }
        if (this.tickTimer++ < (Integer)this.delay.get()) {
            return;
        }
        this.tickTimer = 0;
        for (int broke = 0; broke < (Integer)this.blocksPerStep.get() && (target = this.findNearestGriefBlock(schematic)) != null; ++broke) {
            class_243 c = class_243.method_24953((class_2382)target);
            this.mc.field_1724.method_30634(c.field_1352 + (Double)this.offsetX.get(), c.field_1351 + (Double)this.offsetY.get(), c.field_1350 + (Double)this.offsetZ.get());
            BlockUtils.breakBlock((class_2338)target, (boolean)true);
        }
    }

    private class_2338 findNearestGriefBlock(WorldSchematic schematic) {
        class_2338 playerPos = this.mc.field_1724.method_24515();
        class_2338 best = null;
        double bestDist = Double.MAX_VALUE;
        int r = (Integer)this.radius.get();
        class_2338 min = playerPos.method_10069(-r, -r, -r);
        class_2338 max = playerPos.method_10069(r, r, r);
        for (class_2338 pos : class_2338.method_10097((class_2338)min, (class_2338)max)) {
            double d;
            class_2680 schemState;
            boolean schemIsAir;
            class_2680 worldState;
            if (pos.equals((Object)playerPos) || pos.method_10264() < (Integer)this.minY.get() || (worldState = this.mc.field_1687.method_8320(pos)).method_26215() || pos.method_10264() < 0 && (worldState.method_27852(class_2246.field_10219) || worldState.method_27852(class_2246.field_10219)) || !(schemIsAir = (schemState = schematic.method_8320(pos)) == null || schemState.method_26215()) || !((d = playerPos.method_10262((class_2382)pos)) < bestDist)) continue;
            bestDist = d;
            best = pos.method_10062();
        }
        return best;
    }
}

