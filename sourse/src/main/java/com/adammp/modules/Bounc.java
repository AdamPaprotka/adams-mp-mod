package com.adammp.modules;

import com.adammp.AdamsMPmodClient;
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
import net.minecraft.class_1297;
import net.minecraft.class_238;
import net.minecraft.class_243;
import net.minecraft.class_265;
import net.minecraft.class_310;

@Environment(value=EnvType.CLIENT)
public class Bounc
extends Module {
    private final class_310 mc = class_310.method_1551();
    private final SettingGroup sg = this.settings.getDefaultGroup();
    private final Setting<Double> strength = this.sg.add((Setting)((DoubleSetting.Builder)((DoubleSetting.Builder)new DoubleSetting.Builder().name("strength")).description("Base bounce strength multiplier.")).defaultValue(1.0).min(0.0).sliderMax(3.0).build());
    private final Setting<Double> decay = this.sg.add((Setting)((DoubleSetting.Builder)((DoubleSetting.Builder)new DoubleSetting.Builder().name("decay")).description("Energy loss per bounce. Lower = stops bouncing sooner.")).defaultValue(0.75).min(0.1).sliderMax(0.99).build());
    private final Setting<Boolean> wallBounce = this.sg.add((Setting)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("wall-bounce")).description("Bounce/deflect off walls (true reflection).")).defaultValue((Object)true)).build());
    private final Setting<Double> wallStrength = this.sg.add((Setting)((DoubleSetting.Builder)((DoubleSetting.Builder)((DoubleSetting.Builder)new DoubleSetting.Builder().name("wall-strength")).description("Wall bounce strength multiplier.")).defaultValue(1.0).min(0.0).sliderMax(3.0).visible(() -> this.wallBounce.get())).build());
    private final Setting<Boolean> topBounce = this.sg.add((Setting)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("top-bounce")).description("Bounce down when hitting the ceiling.")).defaultValue((Object)true)).build());
    private final Setting<Double> topStrength = this.sg.add((Setting)((DoubleSetting.Builder)((DoubleSetting.Builder)((DoubleSetting.Builder)new DoubleSetting.Builder().name("top-strength")).description("Ceiling bounce strength multiplier.")).defaultValue(1.0).min(0.0).sliderMax(3.0).visible(() -> this.topBounce.get())).build());
    private final Setting<Integer> resetTicks = this.sg.add((Setting)((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)new IntSetting.Builder().name("reset-after-ticks")).description("Energy resets to 1.0 if you haven't bounced for this many ticks.")).defaultValue((Object)10)).min(0).sliderMax(40).build());
    private final Setting<Boolean> resetOnGround = this.sg.add((Setting)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("reset-on-ground")).description("Reset energy when you're on ground and stable.")).defaultValue((Object)true)).build());
    private final Setting<Boolean> resetOnUpward = this.sg.add((Setting)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("reset-on-upward")).description("Reset energy when you're moving up (jumping/stairs) without hitting ceiling.")).defaultValue((Object)true)).build());
    private double energy = 1.0;
    private double lastYVel = 0.0;
    private boolean wasOnGround = false;
    private boolean wasTouchingCeiling = false;
    private boolean wasHorizontalCollision = false;
    private int ticksSinceBounce = 0;

    public Bounc() {
        super(AdamsMPmodClient.CATEGORY, "bounc", "bouncyy");
    }

    public void onActivate() {
        this.energy = 1.0;
        this.lastYVel = 0.0;
        this.wasOnGround = false;
        this.wasTouchingCeiling = false;
        this.wasHorizontalCollision = false;
        this.ticksSinceBounce = 0;
    }

    private void onDidBounce() {
        this.energy *= ((Double)this.decay.get()).doubleValue();
        this.ticksSinceBounce = 0;
        if (this.energy < 0.03) {
            this.energy = 0.0;
        }
    }

    private boolean hasBlockCollision(class_238 box) {
        if (this.mc.field_1687 == null || this.mc.field_1724 == null) {
            return false;
        }
        for (class_265 shape : this.mc.field_1687.method_20812((class_1297)this.mc.field_1724, box)) {
            if (shape.method_1110()) continue;
            return true;
        }
        return false;
    }

    private class_243 estimateWallNormal() {
        if (this.mc.field_1724 == null) {
            return null;
        }
        class_238 box = this.mc.field_1724.method_5829();
        double eps = 0.01;
        boolean hitPosX = this.hasBlockCollision(box.method_989(0.01, 0.0, 0.0));
        boolean hitNegX = this.hasBlockCollision(box.method_989(-0.01, 0.0, 0.0));
        boolean hitPosZ = this.hasBlockCollision(box.method_989(0.0, 0.0, 0.01));
        boolean hitNegZ = this.hasBlockCollision(box.method_989(0.0, 0.0, -0.01));
        double nx = 0.0;
        double nz = 0.0;
        if (hitPosX) {
            nx -= 1.0;
        }
        if (hitNegX) {
            nx += 1.0;
        }
        if (hitPosZ) {
            nz -= 1.0;
        }
        if (hitNegZ) {
            nz += 1.0;
        }
        if (nx == 0.0 && nz == 0.0) {
            return null;
        }
        class_243 n = new class_243(nx, 0.0, nz);
        double len = Math.sqrt(n.field_1352 * n.field_1352 + n.field_1350 * n.field_1350);
        if (len < 1.0E-9) {
            return null;
        }
        return new class_243(n.field_1352 / len, 0.0, n.field_1350 / len);
    }

    private class_243 reflectHorizontal(class_243 v, class_243 n) {
        class_243 vh = new class_243(v.field_1352, 0.0, v.field_1350);
        double dot = vh.method_1026(n);
        if (dot >= 0.0) {
            return v;
        }
        class_243 reflected = vh.method_1020(n.method_1021(2.0 * dot));
        return new class_243(reflected.field_1352, v.field_1351, reflected.field_1350);
    }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        if (this.mc.field_1724 == null) {
            return;
        }
        class_243 vel = this.mc.field_1724.method_18798();
        boolean onGround = this.mc.field_1724.method_24828();
        boolean touchingCeiling = this.mc.field_1724.field_5992 && vel.field_1351 > 0.0 && !onGround;
        boolean horizCol = this.mc.field_1724.field_5976;
        ++this.ticksSinceBounce;
        if ((Integer)this.resetTicks.get() > 0 && this.ticksSinceBounce > (Integer)this.resetTicks.get()) {
            this.energy = 1.0;
        }
        if (((Boolean)this.resetOnGround.get()).booleanValue() && onGround && Math.abs(vel.field_1351) < 0.05) {
            this.energy = 1.0;
        }
        if (((Boolean)this.resetOnUpward.get()).booleanValue() && vel.field_1351 > 0.1 && !this.mc.field_1724.field_5992) {
            this.energy = 1.0;
        }
        if (!this.wasOnGround && onGround && this.lastYVel < -0.15 && this.energy > 0.0) {
            double impact = Math.abs(this.lastYVel);
            double bounceY = impact * (Double)this.strength.get() * this.energy;
            this.mc.field_1724.method_18800(vel.field_1352, bounceY, vel.field_1350);
            this.onDidBounce();
            vel = this.mc.field_1724.method_18798();
        }
        if (((Boolean)this.topBounce.get()).booleanValue() && !this.wasTouchingCeiling && touchingCeiling && vel.field_1351 > 0.0 && this.energy > 0.0) {
            double newY = -Math.abs(vel.field_1351) * (Double)this.strength.get() * (Double)this.topStrength.get() * this.energy;
            this.mc.field_1724.method_18800(vel.field_1352, newY, vel.field_1350);
            this.onDidBounce();
            vel = this.mc.field_1724.method_18798();
        }
        if (((Boolean)this.wallBounce.get()).booleanValue() && !this.wasHorizontalCollision && horizCol && this.energy > 0.0) {
            Object n = this.estimateWallNormal();
            if (n == null) {
                double vx = vel.field_1352;
                double vz = vel.field_1350;
                if (Math.abs(vx) > Math.abs(vz)) {
                    vx = -vx;
                } else {
                    vz = -vz;
                }
                n = new class_243(Math.signum(-vx), 0.0, Math.signum(-vz));
                n = n.method_1027() < 1.0E-9 ? null : n.method_1029();
            }
            if (n != null) {
                class_243 reflected = this.reflectHorizontal(vel, (class_243)n);
                double mult = (Double)this.wallStrength.get() * this.energy;
                this.mc.field_1724.method_18800(reflected.field_1352 * mult, reflected.field_1351, reflected.field_1350 * mult);
                this.onDidBounce();
            }
        }
        this.lastYVel = this.mc.field_1724.method_18798().field_1351;
        this.wasOnGround = onGround;
        this.wasTouchingCeiling = touchingCeiling;
        this.wasHorizontalCollision = horizCol;
    }
}

