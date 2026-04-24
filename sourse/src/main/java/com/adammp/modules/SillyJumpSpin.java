package com.adammp.modules;

import com.adammp.AdamsMPmodClient;
import com.adammp.commands.FillCommand;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_2382;
import net.minecraft.class_243;
import net.minecraft.class_310;

@Environment(value=EnvType.CLIENT)
public class SillyJumpSpin
extends Module {
    private final class_310 mc = class_310.method_1551();
    private final SettingGroup sg = this.settings.getDefaultGroup();
    private final Setting<Double> maxSpeed = this.sg.add((Setting)((DoubleSetting.Builder)((DoubleSetting.Builder)new DoubleSetting.Builder().name("max-speed")).description("Max horizontal speed.")).defaultValue(0.85).min(0.05).sliderMax(2.5).build());
    private final Setting<Double> acceleration = this.sg.add((Setting)((DoubleSetting.Builder)((DoubleSetting.Builder)new DoubleSetting.Builder().name("acceleration")).description("How fast we steer toward the desired velocity. Higher = snappier tween.")).defaultValue(0.35).min(0.01).sliderMax(1.0).build());
    private final Setting<Double> slowDownDistance = this.sg.add((Setting)((DoubleSetting.Builder)((DoubleSetting.Builder)new DoubleSetting.Builder().name("slowdown-distance")).description("Start easing down when within this distance of the target.")).defaultValue(3.0).min(0.5).sliderMax(10.0).build());
    private final Setting<Double> arriveRadius = this.sg.add((Setting)((DoubleSetting.Builder)((DoubleSetting.Builder)new DoubleSetting.Builder().name("arrive-radius")).description("Distance considered 'arrived' (target flips).")).defaultValue(0.35).min(0.1).sliderMax(2.0).build());
    private final Setting<Double> minSpeedNearTarget = this.sg.add((Setting)((DoubleSetting.Builder)((DoubleSetting.Builder)new DoubleSetting.Builder().name("min-speed-near-target")).description("Minimum speed close to target (gives the spring 'push into it' feeling).")).defaultValue(0.1).min(0.0).sliderMax(0.6).build());
    private final Setting<Double> hopStrength = this.sg.add((Setting)((DoubleSetting.Builder)((DoubleSetting.Builder)new DoubleSetting.Builder().name("hop-strength")).description("Small hop strength.")).defaultValue(0.15).min(0.0).sliderMax(0.8).build());
    private final Setting<Boolean> autoHop = this.sg.add((Setting)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("auto-hop")).description("Hop whenever you can (whenever you touch ground).")).defaultValue((Object)false)).build());
    private final Setting<Integer> hopInterval = this.sg.add((Setting)((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)new IntSetting.Builder().name("hop-interval")).description("Ticks between hops (if auto-hop is off).")).defaultValue((Object)10)).min(2).sliderMax(30).visible(() -> (Boolean)this.autoHop.get() == false)).build());
    private final Setting<Double> spinSpeed = this.sg.add((Setting)((DoubleSetting.Builder)((DoubleSetting.Builder)new DoubleSetting.Builder().name("spin-speed")).description("Fixed yaw spin speed (degrees per tick).")).defaultValue(14.0).min(0.0).sliderMax(60.0).build());
    private class_243 a;
    private class_243 b;
    private class_243 target;
    private boolean goingToB = true;
    private int tick;
    private float spinYaw;

    public SillyJumpSpin() {
        super(AdamsMPmodClient.CATEGORY, "silly-jump-spin", "TikTok NPC tween movement (why the fuck did i make this)");
    }

    public void onActivate() {
        if (FillCommand.sel1 == null || FillCommand.sel2 == null) {
            this.error("sel1 or sel2 not set.", new Object[0]);
            this.toggle();
            return;
        }
        this.a = class_243.method_24953((class_2382)FillCommand.sel1);
        this.b = class_243.method_24953((class_2382)FillCommand.sel2);
        this.goingToB = true;
        this.target = this.b;
        this.tick = 0;
        this.spinYaw = this.mc.field_1724 != null ? this.mc.field_1724.method_36454() : 0.0f;
    }

    public void onDeactivate() {
        if (this.mc.field_1724 != null) {
            class_243 v = this.mc.field_1724.method_18798();
            this.mc.field_1724.method_18800(0.0, v.field_1351, 0.0);
        }
    }

    private static double smoothstep(double x) {
        x = Math.max(0.0, Math.min(1.0, x));
        return x * x * (3.0 - 2.0 * x);
    }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        if (this.mc.field_1724 == null || this.target == null) {
            return;
        }
        class_243 pos = this.mc.field_1724.method_19538();
        class_243 toTarget = this.target.method_1020(pos);
        double dist = toTarget.method_1033();
        if (dist < (Double)this.arriveRadius.get()) {
            this.goingToB = !this.goingToB;
            this.target = this.goingToB ? this.b : this.a;
            this.tick = 0;
            return;
        }
        class_243 dir = toTarget.method_1029();
        double slowFactor = dist / (Double)this.slowDownDistance.get();
        slowFactor = SillyJumpSpin.smoothstep(Math.min(1.0, slowFactor));
        double desiredSpeed = (Double)this.maxSpeed.get() * slowFactor;
        desiredSpeed = Math.max(desiredSpeed, (Double)this.minSpeedNearTarget.get());
        class_243 desiredVel = new class_243(dir.field_1352 * desiredSpeed, 0.0, dir.field_1350 * desiredSpeed);
        class_243 cur = this.mc.field_1724.method_18798();
        class_243 curH = new class_243(cur.field_1352, 0.0, cur.field_1350);
        double alpha = Math.max(0.0, Math.min(1.0, (Double)this.acceleration.get()));
        class_243 newH = curH.method_35590(desiredVel, alpha);
        double yVel = cur.field_1351;
        if ((Double)this.hopStrength.get() > 0.0 && this.mc.field_1724.method_24828()) {
            if (((Boolean)this.autoHop.get()).booleanValue()) {
                yVel = (Double)this.hopStrength.get();
            } else if (this.tick % (Integer)this.hopInterval.get() == 0) {
                yVel = (Double)this.hopStrength.get();
            }
        }
        this.mc.field_1724.method_18800(newH.field_1352, yVel, newH.field_1350);
        this.spinYaw += ((Double)this.spinSpeed.get()).floatValue();
        if (this.spinYaw > 360.0f) {
            this.spinYaw -= 360.0f;
        }
        this.mc.field_1724.method_36456(this.spinYaw);
        this.mc.field_1724.method_36457(0.0f);
        ++this.tick;
    }
}

