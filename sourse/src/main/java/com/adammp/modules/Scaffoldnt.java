package com.adammp.modules;

import com.adammp.AdamsMPmodClient;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.player.FindItemResult;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.meteorclient.utils.world.BlockUtils;
import meteordevelopment.orbit.EventHandler;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_1747;
import net.minecraft.class_2338;
import net.minecraft.class_2350;
import net.minecraft.class_2382;
import net.minecraft.class_243;
import net.minecraft.class_3532;

@Environment(value=EnvType.CLIENT)
public class Scaffoldnt
extends Module {
    private final SettingGroup sgGeneral;
    private final Setting<Integer> backDistance;
    private final Setting<Integer> yOffset;
    private final Setting<Integer> placeEveryTicks;
    private final Setting<Boolean> useVelocityDir;
    private final Setting<Boolean> rotate;
    private final Setting<Boolean> swing;
    private final Setting<Boolean> onlyWhileHoldingBlocks;
    private final Setting<Boolean> autoSprint;
    private int tickCounter;

    public Scaffoldnt() {
        super(AdamsMPmodClient.CATEGORY, "Scaffoldn't", "Scaffold but cursed: lays blocks behind you as you move.");
        this.sgGeneral = this.settings.getDefaultGroup();
        this.backDistance = this.sgGeneral.add((Setting)((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)new IntSetting.Builder().name("back-distance")).description("How many blocks behind you to place.")).defaultValue((Object)1)).min(1).max(5).build());
        this.yOffset = this.sgGeneral.add((Setting)((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)new IntSetting.Builder().name("y-offset")).description("Vertical offset from your feet (use -1 to place as a floor).")).defaultValue((Object)-1)).min(-4).max(4).build());
        this.placeEveryTicks = this.sgGeneral.add((Setting)((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)new IntSetting.Builder().name("place-interval-ticks")).description("Attempt a place every N ticks.")).defaultValue((Object)1)).min(1).max(5).build());
        this.useVelocityDir = this.sgGeneral.add((Setting)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("use-velocity-direction")).description("Figure out \u201cbehind\u201d from your current movement; falls back to yaw if too slow.")).defaultValue((Object)true)).build());
        this.rotate = this.sgGeneral.add((Setting)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("rotate")).description("Rotate toward the target when placing.")).defaultValue((Object)true)).build());
        this.swing = this.sgGeneral.add((Setting)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("swing")).description("Swing hand on place.")).defaultValue((Object)true)).build());
        this.onlyWhileHoldingBlocks = this.sgGeneral.add((Setting)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("only-while-holding-blocks")).description("Run only if a block item is in main hand.")).defaultValue((Object)false)).build());
        this.autoSprint = this.sgGeneral.add((Setting)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("force-sprint")).description("Keep you sprinting while enabled.")).defaultValue((Object)false)).build());
        this.tickCounter = 0;
    }

    public void onActivate() {
        this.tickCounter = 0;
    }

    public void onDeactivate() {
    }

    @EventHandler
    private void onTick(TickEvent.Pre e) {
        boolean placed;
        if (this.mc.field_1724 == null || this.mc.field_1687 == null) {
            return;
        }
        if (((Boolean)this.autoSprint.get()).booleanValue()) {
            this.mc.field_1724.method_5728(true);
        }
        if (((Boolean)this.onlyWhileHoldingBlocks.get()).booleanValue() && !(this.mc.field_1724.method_6047().method_7909() instanceof class_1747)) {
            return;
        }
        FindItemResult blocks = InvUtils.findInHotbar(stack -> stack.method_7909() instanceof class_1747);
        if (!blocks.found()) {
            return;
        }
        ++this.tickCounter;
        if (this.tickCounter % (Integer)this.placeEveryTicks.get() != 0) {
            return;
        }
        class_2350 forward = this.getForwardDirection();
        class_2382 f = forward.method_62675();
        class_2338 feet = this.mc.field_1724.method_24515();
        class_2338 target = feet.method_10069(-f.method_10263() * (Integer)this.backDistance.get(), 0, -f.method_10260() * (Integer)this.backDistance.get()).method_10069(0, ((Integer)this.yOffset.get()).intValue(), 0);
        if (!this.mc.field_1687.method_8320(target).method_45474()) {
            class_2338 alt = target.method_10069(-f.method_10263(), 0, -f.method_10260());
            if (this.mc.field_1687.method_8320(alt).method_45474()) {
                target = alt;
            } else {
                return;
            }
        }
        try {
            placed = BlockUtils.place((class_2338)target, (FindItemResult)blocks, (boolean)((Boolean)this.rotate.get()), (int)50, (boolean)((Boolean)this.swing.get()), (boolean)true);
        }
        catch (Throwable t) {
            placed = BlockUtils.place((class_2338)target, (FindItemResult)blocks, (boolean)((Boolean)this.rotate.get()), (int)50);
        }
        if (placed && !InvUtils.findInHotbar(stack -> stack.method_7909() instanceof class_1747).found()) {
            this.toggle();
        }
    }

    private class_2350 getForwardDirection() {
        class_243 v;
        float yaw = this.mc.field_1724.method_36454();
        if (((Boolean)this.useVelocityDir.get()).booleanValue() && (v = this.mc.field_1724.method_18798()).method_1027() > 4.0E-4) {
            double ang = Math.atan2(v.field_1350, v.field_1352);
            float deg = (float)Math.toDegrees(ang);
            return this.angleToHorizontal(deg);
        }
        return this.yawToHorizontal(yaw);
    }

    private class_2350 yawToHorizontal(float yaw) {
        int i = class_3532.method_15357((double)((double)(yaw * 4.0f / 360.0f) + 0.5)) & 3;
        switch (i) {
            case 0: {
                return class_2350.field_11035;
            }
            case 1: {
                return class_2350.field_11039;
            }
            case 2: {
                return class_2350.field_11043;
            }
        }
        return class_2350.field_11034;
    }

    private class_2350 angleToHorizontal(float deg) {
        deg = (deg % 360.0f + 360.0f) % 360.0f;
        int idx = Math.round(deg / 90.0f) & 3;
        switch (idx) {
            case 0: {
                return class_2350.field_11034;
            }
            case 1: {
                return class_2350.field_11043;
            }
            case 2: {
                return class_2350.field_11039;
            }
        }
        return class_2350.field_11035;
    }
}

