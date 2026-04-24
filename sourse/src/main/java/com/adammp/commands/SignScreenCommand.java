package com.adammp.commands;

import com.adammp.commands.FillCommand;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.commands.Command;
import meteordevelopment.meteorclient.commands.Commands;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.orbit.EventHandler;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_1011;
import net.minecraft.class_2172;
import net.minecraft.class_2338;
import net.minecraft.class_2586;
import net.minecraft.class_2596;
import net.minecraft.class_2625;
import net.minecraft.class_276;
import net.minecraft.class_2877;
import net.minecraft.class_310;
import net.minecraft.class_318;

@Environment(value=EnvType.CLIENT)
public class SignScreenCommand
extends Command {
    private final class_310 mc = class_310.method_1551();
    private boolean running = false;
    private boolean dither = true;
    private long lastUpdate = 0L;
    private final List<class_2625> signs = new ArrayList<class_2625>();
    private int signsX;
    private int signsY;
    private float[][] errR;
    private float[][] errG;
    private float[][] errB;
    private static final int[][] MC_COLORS = new int[][]{{0, 0, 0}, {0, 0, 170}, {0, 170, 0}, {0, 170, 170}, {170, 0, 0}, {170, 0, 170}, {255, 170, 0}, {170, 170, 170}, {85, 85, 85}, {85, 85, 255}, {85, 255, 85}, {85, 255, 255}, {255, 85, 85}, {255, 85, 255}, {255, 255, 85}, {255, 255, 255}};
    private static final char[] MC_CODES = "0123456789abcdef".toCharArray();

    public SignScreenCommand() {
        super("signscreen", "Render your screen onto signs.", new String[0]);
    }

    public void build(LiteralArgumentBuilder<class_2172> builder) {
        builder.then(((LiteralArgumentBuilder)((LiteralArgumentBuilder)SignScreenCommand.literal((String)"start").executes(ctx -> {
            this.start(true);
            return 1;
        })).then(SignScreenCommand.literal((String)"dither").executes(ctx -> {
            this.start(true);
            return 1;
        }))).then(SignScreenCommand.literal((String)"nodither").executes(ctx -> {
            this.start(false);
            return 1;
        })));
        builder.then(SignScreenCommand.literal((String)"stop").executes(ctx -> {
            this.stop();
            return 1;
        }));
    }

    public static void init() {
        Commands.add((Command)new SignScreenCommand());
    }

    private void start(boolean dither) {
        if (this.running) {
            return;
        }
        if (this.mc.field_1687 == null || FillCommand.sel1 == null || FillCommand.sel2 == null) {
            return;
        }
        this.dither = dither;
        this.collectSigns();
        this.running = true;
        MeteorClient.EVENT_BUS.subscribe((Object)this);
    }

    private void stop() {
        if (!this.running) {
            return;
        }
        this.running = false;
        this.signs.clear();
        MeteorClient.EVENT_BUS.unsubscribe((Object)this);
    }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        if (!this.running) {
            return;
        }
        if (System.currentTimeMillis() - this.lastUpdate < 150L) {
            return;
        }
        this.lastUpdate = System.currentTimeMillis();
        this.render();
    }

    private void collectSigns() {
        this.signs.clear();
        class_2338 a = FillCommand.sel1;
        class_2338 b = FillCommand.sel2;
        int minX = Math.min(a.method_10263(), b.method_10263());
        int minY = Math.min(a.method_10264(), b.method_10264());
        int minZ = Math.min(a.method_10260(), b.method_10260());
        int maxX = Math.max(a.method_10263(), b.method_10263());
        int maxY = Math.max(a.method_10264(), b.method_10264());
        int maxZ = Math.max(a.method_10260(), b.method_10260());
        for (int x = minX; x <= maxX; ++x) {
            for (int y = minY; y <= maxY; ++y) {
                for (int z = minZ; z <= maxZ; ++z) {
                    class_2586 be = this.mc.field_1687.method_8321(new class_2338(x, y, z));
                    if (!(be instanceof class_2625)) continue;
                    class_2625 sign = (class_2625)be;
                    this.signs.add(sign);
                }
            }
        }
        this.signs.sort(Comparator.comparingInt(s -> s.method_11016().method_10264()).reversed().thenComparingInt(s -> s.method_11016().method_10263()));
        this.signsX = (int)this.signs.stream().map(s -> s.method_11016().method_10263()).distinct().count();
        this.signsY = (int)this.signs.stream().map(s -> s.method_11016().method_10264()).distinct().count();
    }

    private void render() {
        if (this.signs.isEmpty()) {
            return;
        }
        if (this.mc.field_1724 == null || this.mc.field_1687 == null) {
            return;
        }
        class_1011 img = class_318.method_1663((class_276)this.mc.method_1522());
        int w = this.signsX * 10;
        int h = this.signsY * 4;
        class_1011 scaled = new class_1011(w, h, false);
        img.method_4300(0, 0, img.method_4307(), img.method_4323(), scaled);
        if (this.dither) {
            this.errR = new float[h][w];
            this.errG = new float[h][w];
            this.errB = new float[h][w];
        } else {
            this.errB = null;
            this.errG = null;
            this.errR = null;
        }
        int index = 0;
        for (int sy = 0; sy < this.signsY; ++sy) {
            for (int sx = 0; sx < this.signsX; ++sx) {
                if (index >= this.signs.size()) {
                    return;
                }
                class_2625 sign = this.signs.get(index++);
                String[] lines = new String[4];
                for (int row = 0; row < 4; ++row) {
                    StringBuilder sb = new StringBuilder();
                    for (int col = 0; col < 10; ++col) {
                        int px = sx * 10 + col;
                        int py = sy * 4 + row;
                        int argb = scaled.method_61940(px, py);
                        float r = argb >> 16 & 0xFF;
                        float g = argb >> 8 & 0xFF;
                        float b = argb & 0xFF;
                        if (this.dither) {
                            r += this.errR[py][px];
                            g += this.errG[py][px];
                            b += this.errB[py][px];
                        }
                        r = this.clamp(r);
                        g = this.clamp(g);
                        b = this.clamp(b);
                        int ci = this.nearestColor((int)r, (int)g, (int)b);
                        int[] crgb = MC_COLORS[ci];
                        if (this.dither) {
                            float er = r - (float)crgb[0];
                            float eg = g - (float)crgb[1];
                            float eb = b - (float)crgb[2];
                            this.distributeError(px, py, er, eg, eb, w, h);
                        }
                        sb.append('\u00a7').append(MC_CODES[ci]).append('\u2588');
                    }
                    lines[row] = sb.toString();
                }
                this.mc.field_1724.field_3944.method_52787((class_2596)new class_2877(sign.method_11016(), true, lines[0], lines[1], lines[2], lines[3]));
            }
        }
    }

    private void distributeError(int x, int y, float er, float eg, float eb, int w, int h) {
        if (x + 1 < w) {
            this.addErr(x + 1, y, er * 7.0f / 16.0f, eg * 7.0f / 16.0f, eb * 7.0f / 16.0f);
        }
        if (y + 1 < h) {
            if (x > 0) {
                this.addErr(x - 1, y + 1, er * 3.0f / 16.0f, eg * 3.0f / 16.0f, eb * 3.0f / 16.0f);
            }
            this.addErr(x, y + 1, er * 5.0f / 16.0f, eg * 5.0f / 16.0f, eb * 5.0f / 16.0f);
            if (x + 1 < w) {
                this.addErr(x + 1, y + 1, er * 1.0f / 16.0f, eg * 1.0f / 16.0f, eb * 1.0f / 16.0f);
            }
        }
    }

    private void addErr(int x, int y, float r, float g, float b) {
        float[] fArray = this.errR[y];
        int n = x;
        fArray[n] = fArray[n] + r;
        float[] fArray2 = this.errG[y];
        int n2 = x;
        fArray2[n2] = fArray2[n2] + g;
        float[] fArray3 = this.errB[y];
        int n3 = x;
        fArray3[n3] = fArray3[n3] + b;
    }

    private int nearestColor(int r, int g, int b) {
        int best = Integer.MAX_VALUE;
        int idx = 0;
        for (int i = 0; i < MC_COLORS.length; ++i) {
            int dr = r - MC_COLORS[i][0];
            int dg = g - MC_COLORS[i][1];
            int db = b - MC_COLORS[i][2];
            int dist = dr * dr + dg * dg + db * db;
            if (dist >= best) continue;
            best = dist;
            idx = i;
        }
        return idx;
    }

    private float clamp(float v) {
        return Math.max(0.0f, Math.min(255.0f, v));
    }
}

