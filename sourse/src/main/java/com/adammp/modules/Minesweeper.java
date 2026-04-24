package com.adammp.modules;

import com.adammp.AdamsMPmodClient;
import com.adammp.commands.FillCommand;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Random;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.DoubleSetting;
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
import net.minecraft.class_1792;
import net.minecraft.class_2246;
import net.minecraft.class_2248;
import net.minecraft.class_2338;

@Environment(value=EnvType.CLIENT)
public class Minesweeper
extends Module {
    private final SettingGroup sgGeneral;
    private final Setting<Integer> bps;
    private final Setting<Boolean> teleport;
    private final Setting<Double> tpOffsetY;
    private class_2338 boardOrigin;
    private int cellsX;
    private int cellsZ;
    private final Map<class_2338, Cell> cells;
    private final Queue<PlaceJob> placeQueue;
    private final Random random;

    public Minesweeper() {
        super(AdamsMPmodClient.CATEGORY, "Minesweeper", "mate why do we have FUCKING minesweeper");
        this.sgGeneral = this.settings.getDefaultGroup();
        this.bps = this.sgGeneral.add((Setting)((IntSetting.Builder)((IntSetting.Builder)new IntSetting.Builder().name("blocks-per-tick")).defaultValue((Object)3)).min(1).max(20).build());
        this.teleport = this.sgGeneral.add((Setting)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("teleport")).defaultValue((Object)true)).build());
        this.tpOffsetY = this.sgGeneral.add((Setting)((DoubleSetting.Builder)((DoubleSetting.Builder)new DoubleSetting.Builder().name("tp-offset-y")).defaultValue(1.0).min(-3.0).max(3.0).visible(() -> this.teleport.get())).build());
        this.cells = new HashMap<class_2338, Cell>();
        this.placeQueue = new ArrayDeque<PlaceJob>();
        this.random = new Random();
    }

    public void onActivate() {
        if (FillCommand.sel1 == null || FillCommand.sel2 == null) {
            ChatUtils.error((String)"No selection. Use FillCommand first.", (Object[])new Object[0]);
            this.toggle();
            return;
        }
        this.cells.clear();
        this.placeQueue.clear();
        this.buildBoardFromSelection();
        ChatUtils.info((String)"Board ready.", (Object[])new Object[0]);
        ChatUtils.info((String)"mate why do we have FUCKING minesweeper", (Object[])new Object[0]);
    }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        for (int placed = 0; !this.placeQueue.isEmpty() && placed < (Integer)this.bps.get(); ++placed) {
            PlaceJob job = this.placeQueue.poll();
            FindItemResult fir = InvUtils.findInHotbar((class_1792[])new class_1792[]{job.block.method_8389()});
            if (!fir.found()) continue;
            this.teleportNear(job.pos);
            BlockUtils.place((class_2338)job.pos, (FindItemResult)fir, (boolean)true, (int)0, (boolean)true);
        }
    }

    private void teleportNear(class_2338 pos) {
        double z;
        double y;
        if (!((Boolean)this.teleport.get()).booleanValue() || this.mc.field_1724 == null) {
            return;
        }
        double x = (double)pos.method_10263() + 0.5;
        if (this.mc.field_1724.method_5649(x, y = (double)pos.method_10264() + (Double)this.tpOffsetY.get(), z = (double)pos.method_10260() + 0.5) > 25.0) {
            this.mc.field_1724.method_30634(x, y, z);
        }
    }

    private void buildBoardFromSelection() {
        class_2338 s1 = FillCommand.sel1;
        class_2338 s2 = FillCommand.sel2;
        int minX = Math.min(s1.method_10263(), s2.method_10263());
        int minZ = Math.min(s1.method_10260(), s2.method_10260());
        int maxX = Math.max(s1.method_10263(), s2.method_10263());
        int maxZ = Math.max(s1.method_10260(), s2.method_10260());
        int width = maxX - minX + 1;
        int depth = maxZ - minZ + 1;
        this.cellsX = width / 5;
        this.cellsZ = depth / 5;
        this.boardOrigin = new class_2338(minX, s1.method_10264(), minZ);
        this.buildOuterBorder();
        this.buildCells();
        this.generateMines();
    }

    private void buildOuterBorder() {
        int w = this.cellsX * 5;
        int h = this.cellsZ * 5;
        for (int x = -1; x <= w; ++x) {
            for (int z = -1; z <= h; ++z) {
                if (x != -1 && z != -1 && x != w && z != h) continue;
                this.placeQueue.add(new PlaceJob(this.boardOrigin.method_10069(x, 0, z), class_2246.field_10038));
            }
        }
    }

    private void buildCells() {
        for (int cx = 0; cx < this.cellsX; ++cx) {
            for (int cz = 0; cz < this.cellsZ; ++cz) {
                class_2338 origin = this.boardOrigin.method_10069(cx * 5, 0, cz * 5);
                Cell cell = new Cell(origin);
                this.cells.put(origin, cell);
                for (int x = 0; x < 5; ++x) {
                    for (int z = 0; z < 5; ++z) {
                        boolean border = x == 0 || z == 0 || x == 4 || z == 4;
                        this.placeQueue.add(new PlaceJob(origin.method_10069(x, 0, z), border ? class_2246.field_10038 : class_2246.field_10172));
                    }
                }
            }
        }
    }

    private void generateMines() {
        for (Cell c : this.cells.values()) {
            c.mine = this.random.nextFloat() < 0.15f;
        }
        for (Cell c : this.cells.values()) {
            c.adjacent = Math.min(7, this.countNeighbors(c));
        }
    }

    private int countNeighbors(Cell c) {
        int count = 0;
        for (Cell n : this.neighbors(c)) {
            if (!n.mine) continue;
            ++count;
        }
        return count;
    }

    private List<Cell> neighbors(Cell c) {
        ArrayList<Cell> list = new ArrayList<Cell>();
        int cx = (c.origin.method_10263() - this.boardOrigin.method_10263()) / 5;
        int cz = (c.origin.method_10260() - this.boardOrigin.method_10260()) / 5;
        for (int dx = -1; dx <= 1; ++dx) {
            for (int dz = -1; dz <= 1; ++dz) {
                class_2338 o;
                Cell n;
                if (dx == 0 && dz == 0 || (n = this.cells.get(o = this.boardOrigin.method_10069((cx + dx) * 5, 0, (cz + dz) * 5))) == null) continue;
                list.add(n);
            }
        }
        return list;
    }

    @Environment(value=EnvType.CLIENT)
    private record PlaceJob(class_2338 pos, class_2248 block) {
    }

    @Environment(value=EnvType.CLIENT)
    private static class Cell {
        final class_2338 origin;
        boolean mine;
        int adjacent;

        Cell(class_2338 origin) {
            this.origin = origin;
        }
    }
}

