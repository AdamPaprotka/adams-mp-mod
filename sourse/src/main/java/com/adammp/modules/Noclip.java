package com.adammp.modules;

import com.adammp.AdamsMPmodClient;
import meteordevelopment.meteorclient.events.world.CollisionShapeEvent;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_2350;
import net.minecraft.class_259;

@Environment(value=EnvType.CLIENT)
public class Noclip
extends Module {
    private final SettingGroup sgGeneral;
    private final Setting<Double> feetOffset;
    private final Setting<Boolean> disableOnSneak;

    public Noclip() {
        super(AdamsMPmodClient.CATEGORY, "noclip", "Disables collisions above your feet while keeping ground solid.");
        this.sgGeneral = this.settings.getDefaultGroup();
        this.feetOffset = this.sgGeneral.add((Setting)((DoubleSetting.Builder)((DoubleSetting.Builder)new DoubleSetting.Builder().name("feet-offset")).description("Offset above feet where noclip starts.")).defaultValue(0.05).min(-0.5).sliderMax(0.5).build());
        this.disableOnSneak = this.sgGeneral.add((Setting)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("disable-on-sneak")).description("Disable noclip while sneaking.")).defaultValue((Object)true)).build());
    }

    @EventHandler(priority=-175)
    private void onCollisionShape(CollisionShapeEvent event) {
        if (this.mc.field_1724 == null) {
            return;
        }
        if (((Boolean)this.disableOnSneak.get()).booleanValue() && this.mc.field_1724.method_5715()) {
            return;
        }
        if (event.shape == null || event.shape.method_1110()) {
            return;
        }
        double feetY = this.mc.field_1724.method_23318() + (Double)this.feetOffset.get();
        double blockTopY = (double)event.pos.method_10264() + event.shape.method_1105(class_2350.class_2351.field_11052);
        if (blockTopY > feetY) {
            event.shape = class_259.method_1073();
        }
    }
}

