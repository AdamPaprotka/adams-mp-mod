package com.adammp.modules;

import com.adammp.AdamsMPmodClient;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.settings.StringSetting;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_1657;
import net.minecraft.class_243;

@Environment(value=EnvType.CLIENT)
public class LookAt
extends Module {
    private final SettingGroup sg;
    private final Setting<String> target;
    private final Setting<Boolean> smooth;
    private final Setting<Double> speed;

    public LookAt() {
        super(AdamsMPmodClient.CATEGORY, "LookAt", "Looks at a player. Simple and cursed.");
        this.sg = this.settings.getDefaultGroup();
        this.target = this.sg.add((Setting)((StringSetting.Builder)((StringSetting.Builder)((StringSetting.Builder)new StringSetting.Builder().name("target")).description("Player name (can be shortened).")).defaultValue((Object)"")).build());
        this.smooth = this.sg.add((Setting)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("smooth")).defaultValue((Object)true)).build());
        this.speed = this.sg.add((Setting)((DoubleSetting.Builder)new DoubleSetting.Builder().name("speed")).defaultValue(0.2).min(0.01).max(1.0).build());
    }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        if (this.mc.field_1724 == null || this.mc.field_1687 == null) {
            return;
        }
        if (((String)this.target.get()).isEmpty()) {
            return;
        }
        class_1657 p = this.findPlayer((String)this.target.get());
        if (p == null) {
            return;
        }
        class_243 eyes = this.mc.field_1724.method_33571();
        class_243 targetEyes = p.method_33571();
        class_243 d = targetEyes.method_1020(eyes);
        double distXZ = Math.sqrt(d.field_1352 * d.field_1352 + d.field_1350 * d.field_1350);
        float yaw = (float)(Math.toDegrees(Math.atan2(d.field_1350, d.field_1352)) - 90.0);
        float pitch = (float)(-Math.toDegrees(Math.atan2(d.field_1351, distXZ)));
        if (((Boolean)this.smooth.get()).booleanValue()) {
            this.mc.field_1724.method_36456(this.lerpAngle(this.mc.field_1724.method_36454(), yaw, ((Double)this.speed.get()).floatValue()));
            this.mc.field_1724.method_36457(this.lerp(this.mc.field_1724.method_36455(), pitch, ((Double)this.speed.get()).floatValue()));
        } else {
            this.mc.field_1724.method_36456(yaw);
            this.mc.field_1724.method_36457(pitch);
        }
    }

    private class_1657 findPlayer(String name) {
        for (class_1657 p : this.mc.field_1687.method_18456()) {
            if (!p.method_5477().getString().toLowerCase().startsWith(name.toLowerCase())) continue;
            return p;
        }
        return null;
    }

    private float lerp(float from, float to, float amt) {
        return from + (to - from) * amt;
    }

    private float lerpAngle(float from, float to, float amt) {
        float delta = (to - from + 540.0f) % 360.0f - 180.0f;
        return from + delta * amt;
    }
}

