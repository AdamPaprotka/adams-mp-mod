package com.adammp.modules;

import com.adammp.AdamsMPmodClient;
import com.adammp.Storage.Storage;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashSet;
import java.util.Set;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.world.BlockUtils;
import meteordevelopment.orbit.EventHandler;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_2248;
import net.minecraft.class_2338;
import net.minecraft.class_2382;
import net.minecraft.class_239;
import net.minecraft.class_243;
import net.minecraft.class_2680;
import net.minecraft.class_3965;

@Environment(value=EnvType.CLIENT)
public class AntiSpamBlock
extends Module {
    private final SettingGroup sgGeneral;
    private final Setting<Integer> delay;
    private final Setting<Integer> blocksPerStep;
    private final Setting<Integer> blocksPerTick;
    private final Setting<Boolean> diagonals;
    private final Setting<Integer> minY;
    private final Setting<Integer> maxCluster;
    private final Setting<Boolean> useLists;
    private final Setting<Integer> listDelay;
    private final Setting<Integer> listBlocksPerBurst;
    private int tickTimer;
    private int burstRemaining;
    private final Deque<class_2338> queue;
    private final Set<class_2338> visited;
    private class_2248 targetBlock;
    private boolean listScanning;
    private int listIndex;
    private int listTickTimer;

    public AntiSpamBlock() {
        super(AdamsMPmodClient.CATEGORY, "anti-spam-block", "Breaks connected clusters of matching blocks (normal) or scan-to-list + burst breaking (list mode).");
        this.sgGeneral = this.settings.getDefaultGroup();
        this.delay = this.sgGeneral.add((Setting)((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)new IntSetting.Builder().name("delay-ticks")).description("Ticks between bursts of breaking (normal mode only).")).defaultValue((Object)5)).min(1).sliderMin(1).max(40).sliderMax(40).build());
        this.blocksPerStep = this.sgGeneral.add((Setting)((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)new IntSetting.Builder().name("blocks-per-step")).description("How many blocks to break each time the delay elapses. Set to 0 for infinite (normal mode only).")).defaultValue((Object)3)).min(0).sliderMin(0).max(500).sliderMax(100).build());
        this.blocksPerTick = this.sgGeneral.add((Setting)((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)new IntSetting.Builder().name("blocks-per-tick")).description("Maximum blocks to break per game tick (normal mode only).")).defaultValue((Object)32)).min(1).sliderMin(1).max(512).sliderMax(128).build());
        this.diagonals = this.sgGeneral.add((Setting)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("include-diagonals")).description("If enabled, uses 26-way connectivity (faces+edges+corners). Otherwise 6-way (faces only).")).defaultValue((Object)false)).build());
        this.minY = this.sgGeneral.add((Setting)((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)new IntSetting.Builder().name("min-y")).description("Ignore and never break blocks below this Y.")).defaultValue((Object)0)).min(-64).sliderMin(-64).max(320).sliderMax(320).build());
        this.maxCluster = this.sgGeneral.add((Setting)((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)new IntSetting.Builder().name("max-cluster")).description("Safety cap on how many blocks can be explored in one cluster.")).defaultValue((Object)2048)).min(64).sliderMin(256).max(20000).sliderMax(10000).build());
        this.useLists = this.sgGeneral.add((Setting)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("use-lists")).description("If enabled, scans a full cluster into Storage list, then breaks a burst every few ticks from that list.")).defaultValue((Object)false)).build());
        this.listDelay = this.sgGeneral.add((Setting)((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)new IntSetting.Builder().name("list-delay-ticks")).description("Ticks between list-mode break bursts.")).defaultValue((Object)3)).min(1).sliderMin(1).max(20).sliderMax(10).build());
        this.listBlocksPerBurst = this.sgGeneral.add((Setting)((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)new IntSetting.Builder().name("list-blocks-per-burst")).description("How many blocks to break each burst in list mode.")).defaultValue((Object)3)).min(1).sliderMin(1).max(64).sliderMax(16).build());
        this.tickTimer = 0;
        this.burstRemaining = 0;
        this.queue = new ArrayDeque<class_2338>();
        this.visited = new HashSet<class_2338>();
        this.targetBlock = null;
        this.listScanning = false;
        this.listIndex = 0;
        this.listTickTimer = 0;
    }

    public void onActivate() {
        this.clearWork();
    }

    public void onDeactivate() {
        this.clearWork();
    }

    private void clearWork() {
        this.queue.clear();
        this.visited.clear();
        Storage.antispamblockPositions.clear();
        this.targetBlock = null;
        this.tickTimer = 0;
        this.burstRemaining = 0;
        this.listScanning = false;
        this.listIndex = 0;
        this.listTickTimer = 0;
    }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        if (this.mc.field_1724 == null || this.mc.field_1687 == null) {
            return;
        }
        if (this.targetBlock == null && this.queue.isEmpty() && Storage.antispamblockPositions.isEmpty()) {
            if (this.mc.field_1765 == null || this.mc.field_1765.method_17783() != class_239.class_240.field_1332) {
                return;
            }
            class_2338 start = ((class_3965)this.mc.field_1765).method_17777();
            if (start.method_10264() < (Integer)this.minY.get()) {
                return;
            }
            class_2680 startState = this.mc.field_1687.method_8320(start);
            if (startState.method_26215()) {
                return;
            }
            this.targetBlock = startState.method_26204();
            this.queue.add(start);
            this.visited.add(start);
            this.tickTimer = 0;
            this.burstRemaining = 0;
            if (((Boolean)this.useLists.get()).booleanValue()) {
                Storage.antispamblockPositions.clear();
                Storage.antispamblockPositions.add(start);
                this.listScanning = true;
                this.listIndex = 0;
                this.listTickTimer = 0;
            }
        }
        if (((Boolean)this.useLists.get()).booleanValue() && this.targetBlock != null) {
            if (this.listScanning) {
                int scanLimit = 1024;
                int scanned = 0;
                while (scanned < scanLimit && !this.queue.isEmpty() && this.visited.size() < (Integer)this.maxCluster.get()) {
                    class_2680 state;
                    class_2338 pos2 = this.queue.removeFirst();
                    ++scanned;
                    if (pos2.method_10264() < (Integer)this.minY.get() || (state = this.mc.field_1687.method_8320(pos2)).method_26215() || state.method_26204() != this.targetBlock) continue;
                    for (class_2338 n : this.neighbors(pos2, (Boolean)this.diagonals.get())) {
                        class_2680 ns;
                        if (n.method_10264() < (Integer)this.minY.get() || !this.visited.add(n) || (ns = this.mc.field_1687.method_8320(n)).method_26215() || ns.method_26204() != this.targetBlock) continue;
                        this.queue.addLast(n);
                        Storage.antispamblockPositions.add(n);
                    }
                }
                if (this.queue.isEmpty() || this.visited.size() >= (Integer)this.maxCluster.get()) {
                    this.listScanning = false;
                    this.visited.clear();
                    this.queue.clear();
                    this.listIndex = 0;
                    this.listTickTimer = 0;
                }
                return;
            }
            Storage.antispamblockPositions.removeIf(pos -> {
                if (pos.method_10264() < (Integer)this.minY.get()) {
                    return true;
                }
                class_2680 st = this.mc.field_1687.method_8320(pos);
                return st.method_26215() || st.method_26204() != this.targetBlock;
            });
            if (Storage.antispamblockPositions.isEmpty()) {
                this.clearWork();
                return;
            }
            if (this.listIndex >= Storage.antispamblockPositions.size()) {
                this.listIndex = 0;
            }
            if (this.listIndex < 0) {
                this.listIndex = 0;
            }
            if (this.listTickTimer++ < (Integer)this.listDelay.get()) {
                return;
            }
            this.listTickTimer = 0;
            int toBreak = (Integer)this.listBlocksPerBurst.get();
            int broke = 0;
            while (broke < toBreak && !Storage.antispamblockPositions.isEmpty()) {
                class_2338 pos3;
                if (this.listIndex >= Storage.antispamblockPositions.size()) {
                    this.listIndex = 0;
                }
                if ((pos3 = Storage.antispamblockPositions.get(this.listIndex)).method_10264() < (Integer)this.minY.get()) {
                    Storage.antispamblockPositions.remove(this.listIndex);
                    continue;
                }
                class_2680 st = this.mc.field_1687.method_8320(pos3);
                if (st.method_26215() || st.method_26204() != this.targetBlock) {
                    Storage.antispamblockPositions.remove(this.listIndex);
                    continue;
                }
                class_243 tp = class_243.method_24953((class_2382)pos3);
                this.mc.field_1724.method_30634(tp.field_1352, tp.field_1351, tp.field_1350);
                BlockUtils.breakBlock((class_2338)pos3, (boolean)true);
                Storage.antispamblockPositions.remove(this.listIndex);
                ++broke;
            }
            return;
        }
        if (this.queue.isEmpty()) {
            this.targetBlock = null;
            this.visited.clear();
            this.burstRemaining = 0;
            this.tickTimer = 0;
            return;
        }
        if (this.burstRemaining <= 0) {
            if (this.tickTimer++ < (Integer)this.delay.get()) {
                return;
            }
            this.tickTimer = 0;
            int step = (Integer)this.blocksPerStep.get();
            this.burstRemaining = step == 0 ? Integer.MAX_VALUE : step;
        }
        int limit = Math.min((Integer)this.blocksPerTick.get(), this.burstRemaining);
        int broke = 0;
        while (broke < limit && !this.queue.isEmpty()) {
            class_2680 state;
            class_2338 p = this.queue.removeFirst();
            if (p.method_10264() < (Integer)this.minY.get() || (state = this.mc.field_1687.method_8320(p)).method_26215() || state.method_26204() != this.targetBlock) continue;
            class_243 tp = class_243.method_24953((class_2382)p);
            this.mc.field_1724.method_30634(tp.field_1352, tp.field_1351, tp.field_1350);
            BlockUtils.breakBlock((class_2338)p, (boolean)true);
            ++broke;
            --this.burstRemaining;
            if (this.visited.size() >= (Integer)this.maxCluster.get()) continue;
            for (class_2338 n : this.neighbors(p, (Boolean)this.diagonals.get())) {
                class_2680 ns;
                if (n.method_10264() < (Integer)this.minY.get() || !this.visited.add(n) || (ns = this.mc.field_1687.method_8320(n)).method_26215() || ns.method_26204() != this.targetBlock) continue;
                this.queue.addLast(n);
            }
        }
        if (this.queue.isEmpty()) {
            this.targetBlock = null;
            this.visited.clear();
            this.burstRemaining = 0;
            this.tickTimer = 0;
        }
    }

    private Iterable<class_2338> neighbors(class_2338 p, boolean diag) {
        ArrayDeque<class_2338> out = new ArrayDeque<class_2338>();
        if (!diag) {
            out.add(p.method_10069(1, 0, 0));
            out.add(p.method_10069(-1, 0, 0));
            out.add(p.method_10069(0, 1, 0));
            out.add(p.method_10069(0, -1, 0));
            out.add(p.method_10069(0, 0, 1));
            out.add(p.method_10069(0, 0, -1));
        } else {
            for (int dx = -1; dx <= 1; ++dx) {
                for (int dy = -1; dy <= 1; ++dy) {
                    for (int dz = -1; dz <= 1; ++dz) {
                        if (dx == 0 && dy == 0 && dz == 0) continue;
                        out.add(p.method_10069(dx, dy, dz));
                    }
                }
            }
        }
        return out;
    }
}

