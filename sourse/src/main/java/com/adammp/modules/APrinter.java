package com.adammp.modules;

import com.adammp.AdamsMPmodClient;
import fi.dy.masa.litematica.world.SchematicWorldHandler;
import fi.dy.masa.litematica.world.WorldSchematic;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.BlockListSetting;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.EnumSetting;
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
import net.minecraft.class_1661;
import net.minecraft.class_1792;
import net.minecraft.class_2248;
import net.minecraft.class_2338;
import net.minecraft.class_2350;
import net.minecraft.class_2382;
import net.minecraft.class_243;
import net.minecraft.class_2680;
import net.minecraft.class_7923;

@Environment(value=EnvType.CLIENT)
public class APrinter
extends Module {
    private final SettingGroup sgGeneral;
    private final Setting<Boolean> teleporter;
    private final Setting<Double> offsetX;
    private final Setting<Double> offsetY;
    private final Setting<Double> offsetZ;
    private final Setting<Boolean> breakWrongBlocks;
    private final Setting<Boolean> autoSummon;
    private final Setting<Boolean> autoClearWhenFull;
    private final Setting<Integer> minFreeSlots;
    private final Setting<Integer> clearCooldownTicks;
    private final Setting<Boolean> batchByBlock;
    private final Setting<FillDir> fillDirection;
    private final Setting<Integer> scanSize;
    private final Setting<Integer> scanCap;
    private final Setting<Integer> blocksPerTick;
    private final Setting<Integer> rescanEvery;
    private final Setting<Integer> breaksPerTick;
    private final Setting<Integer> breakRetryTicks;
    private final Setting<Integer> summonCooldown;
    private final Setting<Integer> summonStacks;
    private final Setting<List<class_2248>> ignoreBlocks;
    private final ArrayDeque<Target> placeQueue;
    private int rescanTimer;
    private final ArrayDeque<BreakJob> breakQueue;
    private final HashSet<Long> breakQueued;
    private final HashMap<Long, Integer> nextBreakTryTick;
    private int lastSummonTick;
    private String lastRequestedName;
    private int lastClearTick;
    private static final boolean REQUIRE_ANCHOR = true;
    private static final double TP_DIST_SQ = 36.0;
    private static final int BATCH_PICK_SCAN = 512;
    private static final int BATCH_SCAN_CAP = 4096;
    private static final int SUMMON_SCAN = 256;
    private class_1792 activeBatchItem;
    private int activeBatchStall;
    private static final int ACTIVE_BATCH_STALL_MAX = 2;

    public APrinter() {
        super(AdamsMPmodClient.CATEGORY, "APrinter", "AdamPaprotkaPrinter: smooth Litematica printer + multi-break per tick (retry-based).");
        this.sgGeneral = this.settings.getDefaultGroup();
        this.teleporter = this.sgGeneral.add((Setting)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("teleporter")).description("Teleport near targets before breaking/placing. (May kick / spam packets.)")).defaultValue((Object)false)).build());
        this.offsetX = this.sgGeneral.add((Setting)((DoubleSetting.Builder)((DoubleSetting.Builder)((DoubleSetting.Builder)new DoubleSetting.Builder().name("offset-x")).description("Teleport offset on X.")).defaultValue(0.0).min(-10.0).sliderMin(-10.0).max(10.0).sliderMax(10.0).visible(() -> this.teleporter.get())).build());
        this.offsetY = this.sgGeneral.add((Setting)((DoubleSetting.Builder)((DoubleSetting.Builder)((DoubleSetting.Builder)new DoubleSetting.Builder().name("offset-y")).description("Teleport offset on Y.")).defaultValue(1.0).min(-10.0).sliderMin(-10.0).max(10.0).sliderMax(10.0).visible(() -> this.teleporter.get())).build());
        this.offsetZ = this.sgGeneral.add((Setting)((DoubleSetting.Builder)((DoubleSetting.Builder)((DoubleSetting.Builder)new DoubleSetting.Builder().name("offset-z")).description("Teleport offset on Z.")).defaultValue(0.0).min(-10.0).sliderMin(-10.0).max(10.0).sliderMax(10.0).visible(() -> this.teleporter.get())).build());
        this.breakWrongBlocks = this.sgGeneral.add((Setting)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("break-wrong-blocks")).description("Break wrong blocks that don't match the schematic before placing.")).defaultValue((Object)true)).build());
        this.autoSummon = this.sgGeneral.add((Setting)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("auto-summon")).description("If required block isn't in inventory, run /i <BLOCK_NAME> <stacks>.")).defaultValue((Object)true)).build());
        this.autoClearWhenFull = this.sgGeneral.add((Setting)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("auto-/clear-when-full")).description("If inventory is too full and we need /i, run /clear first. (WARNING: wipes your inventory.)")).defaultValue((Object)false)).visible(() -> this.autoSummon.get())).build());
        this.minFreeSlots = this.sgGeneral.add((Setting)((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)new IntSetting.Builder().name("min-free-slots")).description("If empty slots in hotbar+main (0..35) are <= this, run /clear before /i.")).defaultValue((Object)0)).min(0).sliderMin(0).max(36).sliderMax(36).visible(() -> (Boolean)this.autoSummon.get() != false && (Boolean)this.autoClearWhenFull.get() != false)).build());
        this.clearCooldownTicks = this.sgGeneral.add((Setting)((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)new IntSetting.Builder().name("/clear-cooldown")).description("Minimum ticks between /clear commands.")).defaultValue((Object)40)).min(1).sliderMin(1).max(400).sliderMax(400).visible(() -> (Boolean)this.autoSummon.get() != false && (Boolean)this.autoClearWhenFull.get() != false)).build());
        this.batchByBlock = this.sgGeneral.add((Setting)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("batch-by-block")).description("Place blocks grouped by item to reduce swapping. (One item per tick.)")).defaultValue((Object)true)).build());
        this.fillDirection = this.sgGeneral.add((Setting)((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)new EnumSetting.Builder().name("fill-direction")).description("Ordering for printing (curves look nicer).")).defaultValue((Object)FillDir.RIGHT_TO_LEFT)).build());
        this.scanSize = this.sgGeneral.add((Setting)((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)new IntSetting.Builder().name("scan-size")).description("Scan radius around you for mismatching schematic blocks.")).defaultValue((Object)10)).min(1).sliderMin(1).max(128).sliderMax(128).build());
        this.scanCap = this.sgGeneral.add((Setting)((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)new IntSetting.Builder().name("scan-cap")).description("Hard cap of positions checked per rescan (prevents lag).")).defaultValue((Object)60000)).min(1000).sliderMin(1000).max(600000).sliderMax(600000).build());
        this.blocksPerTick = this.sgGeneral.add((Setting)((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)new IntSetting.Builder().name("blocks-per-tick")).description("How many blocks to PLACE per tick.")).defaultValue((Object)5)).min(1).sliderMin(1).max(50).sliderMax(50).build());
        this.rescanEvery = this.sgGeneral.add((Setting)((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)new IntSetting.Builder().name("rescan-every")).description("How often to rebuild the PLACE queue (ticks).")).defaultValue((Object)15)).min(1).sliderMin(1).max(200).sliderMax(200).build());
        this.breaksPerTick = this.sgGeneral.add((Setting)((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)new IntSetting.Builder().name("breaks-per-tick")).description("How many blocks to BREAK per tick (retry-based).")).defaultValue((Object)5)).min(1).sliderMin(1).max(50).sliderMax(50).visible(() -> this.breakWrongBlocks.get())).build());
        this.breakRetryTicks = this.sgGeneral.add((Setting)((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)new IntSetting.Builder().name("break-retry-ticks")).description("Retry breaking the same block every X ticks while it still exists. (2 \u2248 0.1s)")).defaultValue((Object)2)).min(1).sliderMin(1).max(20).sliderMax(20).visible(() -> this.breakWrongBlocks.get())).build());
        this.summonCooldown = this.sgGeneral.add((Setting)((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)new IntSetting.Builder().name("summon-cooldown")).description("Minimum ticks between repeating /i for the same block.")).defaultValue((Object)40)).min(5).sliderMin(5).max(400).sliderMax(400).visible(() -> this.autoSummon.get())).build());
        this.summonStacks = this.sgGeneral.add((Setting)((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)new IntSetting.Builder().name("summon-stacks")).description("Second argument to /i (e.g. 2 = 2 stacks on your server).")).defaultValue((Object)2)).min(1).sliderMin(1).max(64).sliderMax(64).visible(() -> this.autoSummon.get())).build());
        this.ignoreBlocks = this.sgGeneral.add((Setting)((BlockListSetting.Builder)((BlockListSetting.Builder)((BlockListSetting.Builder)new BlockListSetting.Builder().name("ignore-blocks")).description("Schematic blocks to skip.")).defaultValue(List.of())).build());
        this.placeQueue = new ArrayDeque();
        this.rescanTimer = 0;
        this.breakQueue = new ArrayDeque();
        this.breakQueued = new HashSet();
        this.nextBreakTryTick = new HashMap();
        this.lastSummonTick = -999999;
        this.lastRequestedName = "";
        this.lastClearTick = -999999;
        this.activeBatchItem = null;
        this.activeBatchStall = 0;
    }

    public void onActivate() {
        this.placeQueue.clear();
        this.breakQueue.clear();
        this.breakQueued.clear();
        this.nextBreakTryTick.clear();
        this.rescanTimer = 0;
        this.lastSummonTick = -999999;
        this.lastRequestedName = "";
        this.lastClearTick = -999999;
        this.activeBatchItem = null;
        this.activeBatchStall = 0;
    }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        if (this.mc.field_1724 == null || this.mc.field_1687 == null) {
            return;
        }
        WorldSchematic schematic = SchematicWorldHandler.getSchematicWorld();
        if (schematic == null) {
            return;
        }
        int now = this.mc.field_1724.field_6012;
        if (((Boolean)this.breakWrongBlocks.get()).booleanValue()) {
            this.processBreakQueue(now);
        }
        if (this.placeQueue.isEmpty() || this.rescanTimer++ >= (Integer)this.rescanEvery.get()) {
            this.rescanTimer = 0;
            this.rebuildPlaceQueue(schematic);
            if (this.activeBatchItem != null && !this.hasCandidateForItem(this.activeBatchItem)) {
                this.activeBatchItem = null;
                this.activeBatchStall = 0;
            }
        }
        this.processPlaceQueue(now);
    }

    private void enqueueBreak(class_2338 pos, class_2248 requiredBlock) {
        long key = pos.method_10063();
        if (this.breakQueued.add(key)) {
            this.breakQueue.addLast(new BreakJob(pos.method_10062(), requiredBlock));
            this.nextBreakTryTick.putIfAbsent(key, -999999);
        }
    }

    private void processBreakQueue(int now) {
        BreakJob job;
        if (this.breakQueue.isEmpty()) {
            return;
        }
        int breaks = 0;
        int tries = 0;
        int maxTries = Math.max(64, (Integer)this.breaksPerTick.get() * 64);
        while (breaks < (Integer)this.breaksPerTick.get() && !this.breakQueue.isEmpty() && tries++ < maxTries && (job = this.breakQueue.pollFirst()) != null) {
            class_2338 pos = job.pos();
            long key = pos.method_10063();
            class_2680 cur = this.mc.field_1687.method_8320(pos);
            if (cur.method_26215() || cur.method_26204() == job.requiredBlock()) {
                this.breakQueued.remove(key);
                this.nextBreakTryTick.remove(key);
                continue;
            }
            int next = this.nextBreakTryTick.getOrDefault(key, -999999);
            if (now < next) {
                this.breakQueue.addLast(job);
                continue;
            }
            if (((Boolean)this.teleporter.get()).booleanValue()) {
                this.teleportNearIfFar(pos);
            }
            BlockUtils.breakBlock((class_2338)pos, (boolean)true);
            ++breaks;
            this.nextBreakTryTick.put(key, now + (Integer)this.breakRetryTicks.get());
            this.breakQueue.addLast(job);
        }
    }

    private void processPlaceQueue(int now) {
        Target t;
        FindItemResult firAny;
        if (this.placeQueue.isEmpty()) {
            return;
        }
        if (!((Boolean)this.batchByBlock.get()).booleanValue()) {
            this.processPlaceQueueSimple(now);
            return;
        }
        this.scanAndSummonMissing(now);
        if (this.activeBatchItem == null || !this.hasCandidateForItem(this.activeBatchItem)) {
            this.activeBatchItem = this.pickNextBatchItem();
            this.activeBatchStall = 0;
            if (this.activeBatchItem == null) {
                return;
            }
        }
        if (!(firAny = InvUtils.find((class_1792[])new class_1792[]{this.activeBatchItem})).found()) {
            this.activeBatchItem = null;
            return;
        }
        FindItemResult firHotbar = this.ensureInHotbar(this.activeBatchItem, firAny);
        if (!firHotbar.found()) {
            return;
        }
        int placed = 0;
        int scanLimit = Math.min(this.placeQueue.size(), 4096);
        int scanned = 0;
        while (placed < (Integer)this.blocksPerTick.get() && !this.placeQueue.isEmpty() && scanned++ < scanLimit && (t = this.placeQueue.pollFirst()) != null) {
            class_2338 pos = t.pos();
            class_2680 required = t.required();
            class_2680 actual = this.mc.field_1687.method_8320(pos);
            if (actual.method_26204() == required.method_26204()) continue;
            if (((Boolean)this.breakWrongBlocks.get()).booleanValue() && !actual.method_26215() && actual.method_26204() != required.method_26204()) {
                this.enqueueBreak(pos, required.method_26204());
                this.placeQueue.addLast(t);
                continue;
            }
            if (!this.hasAnchorNeighbor(pos)) {
                this.placeQueue.addLast(t);
                continue;
            }
            class_1792 needItem = required.method_26204().method_8389();
            if (needItem == null) {
                this.placeQueue.addLast(t);
                continue;
            }
            if (needItem != this.activeBatchItem) {
                this.placeQueue.addLast(t);
                continue;
            }
            if (((Boolean)this.autoSummon.get()).booleanValue() && !InvUtils.find((class_1792[])new class_1792[]{needItem}).found()) {
                this.handleSummonOrClear(now, required);
                this.placeQueue.addLast(t);
                continue;
            }
            if (((Boolean)this.teleporter.get()).booleanValue()) {
                this.teleportNearIfFar(pos);
            }
            BlockUtils.place((class_2338)pos, (FindItemResult)firHotbar, (boolean)false, (int)0, (boolean)true, (boolean)true, (boolean)true);
            ++placed;
        }
        if (placed == 0) {
            if (++this.activeBatchStall >= 2) {
                this.activeBatchItem = null;
                this.activeBatchStall = 0;
            }
        } else {
            this.activeBatchStall = 0;
        }
    }

    private void scanAndSummonMissing(int now) {
        if (!((Boolean)this.autoSummon.get()).booleanValue()) {
            return;
        }
        int scanned = 0;
        for (Target t : this.placeQueue) {
            if (scanned++ >= 256) break;
            class_2680 required = t.required();
            class_1792 needItem = required.method_26204().method_8389();
            if (needItem == null) continue;
            if (InvUtils.find((class_1792[])new class_1792[]{needItem}).found()) continue;
            this.handleSummonOrClear(now, required);
            break;
        }
    }

    private void processPlaceQueueSimple(int now) {
        Target t;
        int placed = 0;
        int tries = 0;
        int maxTries = Math.max(128, (Integer)this.blocksPerTick.get() * 128);
        while (placed < (Integer)this.blocksPerTick.get() && !this.placeQueue.isEmpty() && tries++ < maxTries && (t = this.placeQueue.pollFirst()) != null) {
            class_2338 pos = t.pos();
            class_2680 required = t.required();
            class_2680 actual = this.mc.field_1687.method_8320(pos);
            if (actual.method_26204() == required.method_26204()) continue;
            if (((Boolean)this.breakWrongBlocks.get()).booleanValue() && !actual.method_26215() && actual.method_26204() != required.method_26204()) {
                this.enqueueBreak(pos, required.method_26204());
                this.placeQueue.addLast(t);
                continue;
            }
            if (!this.hasAnchorNeighbor(pos)) {
                this.placeQueue.addLast(t);
                continue;
            }
            class_1792 item = required.method_26204().method_8389();
            if (item == null) {
                this.placeQueue.addLast(t);
                continue;
            }
            FindItemResult firAny = InvUtils.find((class_1792[])new class_1792[]{item});
            if (((Boolean)this.autoSummon.get()).booleanValue() && !firAny.found()) {
                this.handleSummonOrClear(now, required);
                this.placeQueue.addLast(t);
                continue;
            }
            if (!firAny.found()) {
                this.placeQueue.addLast(t);
                continue;
            }
            FindItemResult firHotbar = this.ensureInHotbar(item, firAny);
            if (!firHotbar.found()) {
                this.placeQueue.addLast(t);
                continue;
            }
            if (((Boolean)this.teleporter.get()).booleanValue()) {
                this.teleportNearIfFar(pos);
            }
            BlockUtils.place((class_2338)pos, (FindItemResult)firHotbar, (boolean)false, (int)0, (boolean)true, (boolean)true, (boolean)true);
            ++placed;
        }
    }

    private class_1792 pickNextBatchItem() {
        int scanned = 0;
        for (Target t : this.placeQueue) {
            class_1792 item;
            if (scanned++ >= 512) break;
            class_2338 pos = t.pos();
            class_2680 required = t.required();
            class_2680 actual = this.mc.field_1687.method_8320(pos);
            if (actual.method_26204() == required.method_26204() || ((Boolean)this.breakWrongBlocks.get()).booleanValue() && !actual.method_26215() && actual.method_26204() != required.method_26204() || !this.hasAnchorNeighbor(pos) || (item = required.method_26204().method_8389()) == null || !InvUtils.find((class_1792[])new class_1792[]{item}).found()) continue;
            return item;
        }
        return null;
    }

    private boolean hasCandidateForItem(class_1792 item) {
        int scanned = 0;
        for (Target t : this.placeQueue) {
            class_1792 need;
            if (scanned++ >= 512) break;
            class_2338 pos = t.pos();
            class_2680 required = t.required();
            class_2680 actual = this.mc.field_1687.method_8320(pos);
            if (actual.method_26204() == required.method_26204() || ((Boolean)this.breakWrongBlocks.get()).booleanValue() && !actual.method_26215() && actual.method_26204() != required.method_26204() || !this.hasAnchorNeighbor(pos) || (need = required.method_26204().method_8389()) == null || need != item || !InvUtils.find((class_1792[])new class_1792[]{item}).found()) continue;
            return true;
        }
        return false;
    }

    private FindItemResult ensureInHotbar(class_1792 item, FindItemResult firAny) {
        FindItemResult firHotbar = InvUtils.findInHotbar((class_1792[])new class_1792[]{item});
        if (!firHotbar.found()) {
            InvUtils.move().from(firAny.slot()).toHotbar(this.mc.field_1724.method_31548().field_7545);
            firHotbar = InvUtils.findInHotbar((class_1792[])new class_1792[]{item});
        }
        return firHotbar;
    }

    private void handleSummonOrClear(int now, class_2680 required) {
        if (((Boolean)this.autoClearWhenFull.get()).booleanValue() && this.isInventoryTooFull()) {
            if (now - this.lastClearTick >= (Integer)this.clearCooldownTicks.get()) {
                ChatUtils.sendPlayerMsg((String)"/clear");
                this.lastClearTick = now;
            }
            return;
        }
        String upper = this.getUpperSnakeName(required.method_26204());
        if (this.shouldSendSummon(now, upper)) {
            ChatUtils.sendPlayerMsg((String)("/i " + upper + " " + String.valueOf(this.summonStacks.get())));
            this.lastSummonTick = now;
            this.lastRequestedName = upper;
        }
    }

    private void rebuildPlaceQueue(WorldSchematic schematic) {
        this.placeQueue.clear();
        class_2338 playerPos = this.mc.field_1724.method_24515();
        int r = (Integer)this.scanSize.get();
        int cap = (Integer)this.scanCap.get();
        ArrayList<Target> all = new ArrayList<Target>();
        int checked = 0;
        for (class_2338 pos : class_2338.method_10097((class_2338)playerPos.method_10069(-r, -r, -r), (class_2338)playerPos.method_10069(r, r, r))) {
            class_2680 actual;
            if (checked++ >= cap) break;
            class_2680 required = schematic.method_8320(pos);
            if (required == null || required.method_26215()) continue;
            class_2248 reqBlock = required.method_26204();
            if (((List)this.ignoreBlocks.get()).contains(reqBlock) || (actual = this.mc.field_1687.method_8320(pos)).method_26204() == reqBlock) continue;
            all.add(new Target(pos.method_10062(), required));
        }
        all.sort(this.targetComparator((FillDir)((Object)this.fillDirection.get())));
        all.forEach(this.placeQueue::addLast);
    }

    private void teleportNearIfFar(class_2338 pos) {
        class_243 c = class_243.method_24953((class_2382)pos);
        if (this.mc.field_1724.method_5707(c) <= 36.0) {
            return;
        }
        this.mc.field_1724.method_30634(c.field_1352 + (Double)this.offsetX.get(), c.field_1351 + (Double)this.offsetY.get(), c.field_1350 + (Double)this.offsetZ.get());
    }

    private boolean hasAnchorNeighbor(class_2338 pos) {
        for (class_2350 d : class_2350.values()) {
            if (this.mc.field_1687.method_8320(pos.method_10093(d)).method_26215()) continue;
            return true;
        }
        return false;
    }

    private boolean isInventoryTooFull() {
        class_1661 inv = this.mc.field_1724.method_31548();
        int empty = 0;
        for (int i = 0; i < 36; ++i) {
            if (!inv.method_5438(i).method_7960()) continue;
            ++empty;
        }
        return empty <= (Integer)this.minFreeSlots.get();
    }

    private Comparator<Target> targetComparator(FillDir dir) {
        return (a, b) -> {
            int cx2;
            int cy = Integer.compare(a.pos().method_10264(), b.pos().method_10264());
            if (cy != 0) {
                return cy;
            }
            if (dir == FillDir.RIGHT_TO_LEFT) {
                cx = Integer.compare(b.pos().method_10263(), a.pos().method_10263());
                if (cx != 0) {
                    return cx;
                }
                int cz = Integer.compare(a.pos().method_10260(), b.pos().method_10260());
                if (cz != 0) {
                    return cz;
                }
            } else if (dir == FillDir.LEFT_TO_RIGHT) {
                cx = Integer.compare(a.pos().method_10263(), b.pos().method_10263());
                if (cx != 0) {
                    return cx;
                }
                int cz = Integer.compare(a.pos().method_10260(), b.pos().method_10260());
                if (cz != 0) {
                    return cz;
                }
            } else if (dir == FillDir.FRONT_TO_BACK) {
                cz = Integer.compare(a.pos().method_10260(), b.pos().method_10260());
                if (cz != 0) {
                    return cz;
                }
                int cx = Integer.compare(a.pos().method_10263(), b.pos().method_10263());
                if (cx != 0) {
                    return cx;
                }
            } else {
                cz = Integer.compare(b.pos().method_10260(), a.pos().method_10260());
                if (cz != 0) {
                    return cz;
                }
                int cx = Integer.compare(a.pos().method_10263(), b.pos().method_10263());
                if (cx != 0) {
                    return cx;
                }
            }
            if ((cx2 = Integer.compare(a.pos().method_10263(), b.pos().method_10263())) != 0) {
                return cx2;
            }
            return Integer.compare(a.pos().method_10260(), b.pos().method_10260());
        };
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

    @Environment(value=EnvType.CLIENT)
    public static enum FillDir {
        RIGHT_TO_LEFT,
        LEFT_TO_RIGHT,
        FRONT_TO_BACK,
        BACK_TO_FRONT;

    }

    @Environment(value=EnvType.CLIENT)
    private record BreakJob(class_2338 pos, class_2248 requiredBlock) {
    }

    @Environment(value=EnvType.CLIENT)
    private record Target(class_2338 pos, class_2680 required) {
    }
}

