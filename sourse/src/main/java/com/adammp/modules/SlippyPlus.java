package com.adammp.modules;

import com.adammp.AdamsMPmodClient;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_2338;
import net.minecraft.class_243;
import net.minecraft.class_2680;

@Environment(value=EnvType.CLIENT)
public class SlippyPlus
extends Module {
    private final SettingGroup sgGeneral;
    private final Setting<Double> slip;

    public SlippyPlus() {
        super(AdamsMPmodClient.CATEGORY, "slippy+", "Uncapped Slippy. Extreme sliding by modifying player friction.");
        this.sgGeneral = this.settings.getDefaultGroup();
        this.slip = this.sgGeneral.add((Setting)((DoubleSetting.Builder)((DoubleSetting.Builder)new DoubleSetting.Builder().name("slip-multiplier")).description("Higher = more sliding (1.0 = normal, 5.0 = insane).")).defaultValue(1.1).min(-1520.0).sliderMin(0.0).max(1520.0).sliderMax(5.0).build());
    }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        if (this.mc.field_1724 == null || this.mc.field_1687 == null) {
            return;
        }
        if (!this.mc.field_1724.method_24828()) {
            return;
        }
        class_2338 under = this.mc.field_1724.method_24515().method_10074();
        class_2680 state = this.mc.field_1687.method_8320(under);
        float baseSlip = state.method_26204().method_9499();
        double mult = (Double)this.slip.get();
        double factor = (double)baseSlip * mult / (double)baseSlip;
        class_243 v = this.mc.field_1724.method_18798();
        this.mc.field_1724.method_18800(v.field_1352 * factor, v.field_1351, v.field_1350 * factor);
    }
}

