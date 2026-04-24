package com.adammp.modules;

import com.adammp.AdamsMPmodClient;
import com.adammp.commands.FillCommand;
import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Queue;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.EnumSetting;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.settings.StringSetting;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import meteordevelopment.meteorclient.utils.player.FindItemResult;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.meteorclient.utils.world.BlockUtils;
import meteordevelopment.orbit.EventHandler;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_1792;
import net.minecraft.class_1922;
import net.minecraft.class_2246;
import net.minecraft.class_2338;
import net.minecraft.class_2350;
import net.minecraft.class_2382;
import net.minecraft.class_243;
import net.minecraft.class_2680;
import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;

@Environment(value=EnvType.CLIENT)
public class MathSurface
extends Module {
    private final SettingGroup sg;
    private final Setting<Mode> mode;
    private final Setting<String> equation;
    private final Setting<Double> scale;
    private final Setting<Integer> opsPerTick;
    private final Setting<Boolean> teleporter;
    private Expression expr;
    private boolean exprOk;
    private final ArrayDeque<class_2338> bgQueue;
    private final ArrayDeque<class_2338> curveQueue;
    private final HashSet<class_2338> queued;
    private int minX;
    private int maxX;
    private int minY;
    private int maxY;
    private int zPlane;
    private boolean boundsOk;

    public MathSurface() {
        super(AdamsMPmodClient.CATEGORY, "math-surface", "Draws math curves with background + teleport \ud83e\udd21\ud83c\udf54");
        this.sg = this.settings.getDefaultGroup();
        this.mode = this.sg.add((Setting)((EnumSetting.Builder)((EnumSetting.Builder)new EnumSetting.Builder().name("mode")).defaultValue((Object)Mode.Y_EQUALS)).build());
        this.equation = this.sg.add((Setting)((StringSetting.Builder)((StringSetting.Builder)new StringSetting.Builder().name("equation")).defaultValue((Object)"tan(x)")).build());
        this.scale = this.sg.add((Setting)((DoubleSetting.Builder)new DoubleSetting.Builder().name("scale")).defaultValue(0.25).min(0.001).build());
        this.opsPerTick = this.sg.add((Setting)((IntSetting.Builder)((IntSetting.Builder)new IntSetting.Builder().name("ops-per-tick")).defaultValue((Object)10)).min(1).build());
        this.teleporter = this.sg.add((Setting)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("teleport")).defaultValue((Object)true)).build());
        this.bgQueue = new ArrayDeque();
        this.curveQueue = new ArrayDeque();
        this.queued = new HashSet();
    }

    public void onActivate() {
        if (this.mc.field_1724 == null || this.mc.field_1687 == null) {
            return;
        }
        ChatUtils.sendPlayerMsg((String)"/i BLACK_CONCRETE 64");
        ChatUtils.sendPlayerMsg((String)"/i WHITE_CONCRETE 64");
        this.rebuild();
    }

    public void onDeactivate() {
        this.bgQueue.clear();
        this.curveQueue.clear();
        this.queued.clear();
        this.boundsOk = false;
    }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        if (!this.boundsOk || this.mc.field_1724 == null || this.mc.field_1687 == null) {
            return;
        }
        FindItemResult black = this.ensureInHotbar(class_2246.field_10458.method_8389());
        FindItemResult white = this.ensureInHotbar(class_2246.field_10107.method_8389());
        if (!black.found() || !white.found()) {
            return;
        }
        int ops = 0;
        while (ops < (Integer)this.opsPerTick.get()) {
            FindItemResult use;
            class_2338 pos;
            if (!this.bgQueue.isEmpty()) {
                pos = this.bgQueue.poll();
                use = black;
            } else {
                if (this.curveQueue.isEmpty()) break;
                pos = this.curveQueue.poll();
                use = white;
            }
            this.queued.remove(pos);
            if (!this.mc.field_1687.method_8320(pos).method_26215()) {
                ++ops;
                continue;
            }
            if (((Boolean)this.teleporter.get()).booleanValue()) {
                this.teleportNear(pos);
            }
            if (!this.hasNeighbor(pos)) {
                this.buildScaffold(pos);
                this.enqueue(this.bgQueue, pos);
                ++ops;
                continue;
            }
            InvUtils.swap((int)use.slot(), (boolean)false);
            BlockUtils.place((class_2338)pos, (FindItemResult)use, (boolean)true, (int)50, (boolean)false, (boolean)true, (boolean)false);
            ++ops;
        }
    }

    private void rebuild() {
        this.bgQueue.clear();
        this.curveQueue.clear();
        this.queued.clear();
        this.boundsOk = false;
        if (FillCommand.sel1 == null || FillCommand.sel2 == null) {
            return;
        }
        class_2338 a = FillCommand.sel1;
        class_2338 b = FillCommand.sel2;
        if (a.method_10260() != b.method_10260()) {
            return;
        }
        this.minX = Math.min(a.method_10263(), b.method_10263());
        this.maxX = Math.max(a.method_10263(), b.method_10263());
        this.minY = Math.min(a.method_10264(), b.method_10264());
        this.maxY = Math.max(a.method_10264(), b.method_10264());
        this.zPlane = a.method_10260();
        if (!this.compileEquation()) {
            return;
        }
        int cx = (this.minX + this.maxX) / 2;
        int cy = (this.minY + this.maxY) / 2;
        for (int y = this.minY; y <= this.maxY; ++y) {
            for (int x = this.minX; x <= this.maxX; ++x) {
                this.enqueue(this.bgQueue, new class_2338(x, y, this.zPlane));
            }
        }
        for (int x = this.minX; x <= this.maxX; ++x) {
            int by;
            double mx = (double)(x - cx) * (Double)this.scale.get();
            double yVal = this.eval(mx, 0.0);
            if (!Double.isFinite(yVal) || (by = cy + (int)Math.round(yVal)) < this.minY || by > this.maxY) continue;
            this.enqueue(this.curveQueue, new class_2338(x, by, this.zPlane));
        }
        this.boundsOk = true;
    }

    private boolean compileEquation() {
        try {
            this.expr = new ExpressionBuilder((String)this.equation.get()).variables(new String[]{"x", "y", "pi", "e"}).build();
            this.expr.setVariable("pi", Math.PI);
            this.expr.setVariable("e", Math.E);
            this.exprOk = true;
            return true;
        }
        catch (Exception e) {
            this.exprOk = false;
            return false;
        }
    }

    private double eval(double x, double y) {
        if (!this.exprOk) {
            return Double.NaN;
        }
        this.expr.setVariable("x", x);
        this.expr.setVariable("y", y);
        return this.expr.evaluate();
    }

    private boolean hasNeighbor(class_2338 pos) {
        for (class_2350 d : class_2350.values()) {
            class_2338 n = pos.method_10093(d);
            class_2680 s = this.mc.field_1687.method_8320(n);
            if (s.method_26215() || !s.method_26212((class_1922)this.mc.field_1687, n)) continue;
            return true;
        }
        return false;
    }

    private void buildScaffold(class_2338 target) {
        class_2338 p = target.method_10074();
        for (int i = 0; i < 64; ++i) {
            class_2680 s = this.mc.field_1687.method_8320(p);
            if (!s.method_26215() && s.method_26212((class_1922)this.mc.field_1687, p)) {
                this.enqueue(this.bgQueue, p.method_10084());
                return;
            }
            this.enqueue(this.bgQueue, p);
            p = p.method_10074();
        }
    }

    private void teleportNear(class_2338 pos) {
        class_243 c = class_243.method_24953((class_2382)pos);
        this.mc.field_1724.method_30634(c.field_1352, c.field_1351 + 1.2, c.field_1350);
    }

    private void enqueue(Queue<class_2338> q, class_2338 pos) {
        if (this.queued.add(pos)) {
            q.add(pos);
        }
    }

    private FindItemResult ensureInHotbar(class_1792 item) {
        FindItemResult hot = InvUtils.findInHotbar((class_1792[])new class_1792[]{item});
        if (hot.found()) {
            return hot;
        }
        FindItemResult any = InvUtils.find((class_1792[])new class_1792[]{item});
        if (!any.found()) {
            return hot;
        }
        InvUtils.move().from(any.slot()).toHotbar(this.mc.field_1724.method_31548().field_7545);
        return InvUtils.findInHotbar((class_1792[])new class_1792[]{item});
    }

    @Environment(value=EnvType.CLIENT)
    public static enum Mode {
        Y_EQUALS,
        X_EQUALS;

    }
}

