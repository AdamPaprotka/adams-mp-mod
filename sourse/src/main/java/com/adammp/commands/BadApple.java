package com.adammp.commands;

import com.adammp.commands.FillCommand;
import com.adammp.utils.Canvas;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileAttribute;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;
import meteordevelopment.meteorclient.commands.Command;
import meteordevelopment.meteorclient.commands.Commands;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.class_2172;
import net.minecraft.class_2338;
import net.minecraft.class_2382;
import net.minecraft.class_243;
import net.minecraft.class_2586;
import net.minecraft.class_2596;
import net.minecraft.class_2625;
import net.minecraft.class_2877;
import net.minecraft.class_310;

@Environment(value=EnvType.CLIENT)
public class BadApple
extends Command {
    private static final int SIGNS_W = 4;
    private static final int SIGNS_H = 4;
    private static final int TICKS_PER_FRAME = 2;
    private static final int BLOCKY_SIGN_COLS = 10;
    private static final int BLOCKY_SIGN_ROWS = 4;
    private static final int BLOCKY_FRAME_W = 40;
    private static final int BLOCKY_FRAME_H = 16;
    private static final int TILE_PX_W = 60;
    private static final int TILE_PX_H = 16;
    private static final int CANVAS_FRAME_W = 240;
    private static final int CANVAS_FRAME_H = 64;
    private static State state = State.STOP;
    private static Mode mode = Mode.BLOCKY;
    private static boolean useSectionColors = false;
    private static boolean mirrorLeft = false;
    private static boolean mirrorRight = true;
    private static int tickCounter = 0;
    private static final class_2625[][] GRID = new class_2625[4][4];
    private static final List<String[]> FRAMES_BLOCKY = new ArrayList<String[]>();
    private static final List<String[]> FRAMES_CANVAS = new ArrayList<String[]>();
    private static int frameIndexBlocky = 0;
    private static int frameIndexCanvas = 0;
    private static final Canvas CANVAS = new Canvas("\n");
    private static final Canvas TILE_TMP = new Canvas("\n");
    private static final Path ROOT_DIR = Paths.get("C:\\Users\\papro\\AppData\\Roaming\\ModrinthApp\\profiles\\Meteor\\badapplewtf", new String[0]);
    private static final Path FRAMES_BLOCKY_DIR = ROOT_DIR.resolve("frames_blocky");
    private static final Path FRAMES_CANVAS_DIR = ROOT_DIR.resolve("frames_canvas");

    public static void init() {
        Commands.add((Command)new BadApple());
        ClientTickEvents.END_CLIENT_TICK.register(client -> BadApple.onTick());
        System.out.println("[BadApple] init OK (mode=blocky/canvas, 10 fps)");
    }

    public BadApple() {
        super("badapple", "Bad Apple on signs (blocky + canvas, 10 fps)", new String[0]);
    }

    public void build(LiteralArgumentBuilder<class_2172> builder) {
        ((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)builder.then(BadApple.literal((String)"run").executes(ctx -> {
            if (FillCommand.sel1 == null || FillCommand.sel2 == null) {
                this.error("sel1 / sel2 not set", new Object[0]);
                return 0;
            }
            if (mode == Mode.BLOCKY) {
                if (FRAMES_BLOCKY.isEmpty() && !BadApple.loadFramesBlocky()) {
                    this.error("failed to load blocky frames from " + String.valueOf(FRAMES_BLOCKY_DIR), new Object[0]);
                    return 0;
                }
            } else if (FRAMES_CANVAS.isEmpty() && !BadApple.loadFramesCanvas()) {
                this.error("failed to load canvas frames from " + String.valueOf(FRAMES_CANVAS_DIR), new Object[0]);
                return 0;
            }
            class_310 mc = class_310.method_1551();
            if (mc.field_1687 == null || mc.field_1724 == null) {
                return 0;
            }
            ArrayList<class_2625> signs = new ArrayList<class_2625>();
            class_2338 min = new class_2338(Math.min(FillCommand.sel1.method_10263(), FillCommand.sel2.method_10263()), Math.min(FillCommand.sel1.method_10264(), FillCommand.sel2.method_10264()), Math.min(FillCommand.sel1.method_10260(), FillCommand.sel2.method_10260()));
            class_2338 max = new class_2338(Math.max(FillCommand.sel1.method_10263(), FillCommand.sel2.method_10263()), Math.max(FillCommand.sel1.method_10264(), FillCommand.sel2.method_10264()), Math.max(FillCommand.sel1.method_10260(), FillCommand.sel2.method_10260()));
            for (class_2338 p : class_2338.method_10097((class_2338)min, (class_2338)max)) {
                class_2586 be = mc.field_1687.method_8321(p);
                if (!(be instanceof class_2625)) continue;
                class_2625 sign = (class_2625)be;
                signs.add(sign);
            }
            int need = 16;
            if (signs.size() != need) {
                this.error("need EXACTLY " + need + " signs, found " + signs.size(), new Object[0]);
                return 0;
            }
            class_243 look = mc.field_1724.method_5828(1.0f);
            double rx = look.field_1350;
            double rz = -look.field_1352;
            double len = Math.hypot(rx, rz);
            if (len < 1.0E-9) {
                rx = 1.0;
                rz = 0.0;
                len = 1.0;
            }
            double rightX = rx / len;
            double rightZ = rz / len;
            signs.sort((s1, s2) -> {
                class_2338 p1 = s1.method_11016();
                class_2338 p2 = s2.method_11016();
                int byY = Integer.compare(p2.method_10264(), p1.method_10264());
                if (byY != 0) {
                    return byY;
                }
                class_243 c1 = class_243.method_24953((class_2382)p1);
                class_243 c2 = class_243.method_24953((class_2382)p2);
                double r1 = c1.field_1352 * rightX + c1.field_1350 * rightZ;
                double r2 = c2.field_1352 * rightX + c2.field_1350 * rightZ;
                return Double.compare(r1, r2);
            });
            for (int i = 0; i < need; ++i) {
                BadApple.GRID[i / 4][i % 4] = (class_2625)signs.get(i);
            }
            state = State.RUN;
            tickCounter = 0;
            this.info("run " + mode.name().toLowerCase() + " @10fps (mirrorLeft=" + mirrorLeft + ", mirrorRight=" + mirrorRight + ")", new Object[0]);
            return 1;
        }))).then(BadApple.literal((String)"pause").executes(ctx -> {
            state = State.PAUSE;
            this.info("pause", new Object[0]);
            return 1;
        }))).then(BadApple.literal((String)"stop").executes(ctx -> {
            state = State.STOP;
            tickCounter = 0;
            this.info("stop", new Object[0]);
            return 1;
        }))).then(BadApple.literal((String)"reset").executes(ctx -> {
            state = State.STOP;
            tickCounter = 0;
            frameIndexBlocky = 0;
            frameIndexCanvas = 0;
            this.info("reset", new Object[0]);
            return 1;
        }))).then(((LiteralArgumentBuilder)BadApple.literal((String)"mode").then(BadApple.literal((String)"blocky").executes(ctx -> {
            mode = Mode.BLOCKY;
            this.info("mode=blocky", new Object[0]);
            return 1;
        }))).then(BadApple.literal((String)"canvas").executes(ctx -> {
            mode = Mode.CANVAS;
            this.info("mode=canvas", new Object[0]);
            return 1;
        })))).then(BadApple.literal((String)"left").then(BadApple.literal((String)"flip").executes(ctx -> {
            mirrorLeft = !mirrorLeft;
            this.info("mirrorLeft=" + mirrorLeft, new Object[0]);
            return 1;
        })))).then(BadApple.literal((String)"right").then(BadApple.literal((String)"flip").executes(ctx -> {
            mirrorRight = !mirrorRight;
            this.info("mirrorRight=" + mirrorRight, new Object[0]);
            return 1;
        })))).then(BadApple.literal((String)"plain").executes(ctx -> {
            useSectionColors = false;
            this.info("colors=plain", new Object[0]);
            return 1;
        }))).then(BadApple.literal((String)"section").executes(ctx -> {
            useSectionColors = true;
            this.info("colors=section", new Object[0]);
            return 1;
        }));
    }

    private static void onTick() {
        if (state != State.RUN) {
            return;
        }
        if (++tickCounter % 2 != 0) {
            return;
        }
        if (mode == Mode.BLOCKY) {
            String[] frame = BadApple.getNextFrameBlocky();
            if (frame != null) {
                BadApple.renderFrameBlocky(frame);
            }
        } else {
            String[] frame = BadApple.getNextFrameCanvas();
            if (frame != null) {
                BadApple.renderFrameCanvas(frame);
            }
        }
    }

    private static void renderFrameBlocky(String[] frame) {
        for (int r = 0; r < 4; ++r) {
            for (int c = 0; c < 4; ++c) {
                class_2625 sign = GRID[r][c];
                if (sign == null) continue;
                boolean mirror = c < 2 ? mirrorLeft : mirrorRight;
                int rowStart = r * 4;
                int colStart = c * 10;
                String[] lines = BadApple.extractBlocky(frame, rowStart, colStart, mirror);
                BadApple.updateSign(sign, lines);
            }
        }
    }

    private static String[] extractBlocky(String[] frame, int rowStart, int colStart, boolean mirrorBits) {
        String[] out = new String[4];
        for (int i = 0; i < 4; ++i) {
            String row = frame[rowStart + i];
            if (row.length() < colStart + 10) {
                row = BadApple.padRight(row, colStart + 10, '0');
            }
            String bits = row.substring(colStart, colStart + 10);
            if (mirrorBits) {
                bits = new StringBuilder(bits).reverse().toString();
            }
            out[i] = BadApple.bitsToLine(bits);
        }
        return out;
    }

    private static String bitsToLine(String bits) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < bits.length(); ++i) {
            boolean black;
            char b = bits.charAt(i);
            boolean bl = black = b == '1';
            if (useSectionColors) {
                sb.append(black ? "\u00a70\u2588" : "\u00a7f\u2588");
                continue;
            }
            sb.append(black ? "\u2588" : " ");
        }
        return sb.toString();
    }

    private static void renderFrameCanvas(String[] frameBits) {
        CANVAS.clear();
        for (int y = 0; y < 64; ++y) {
            String row = frameBits[y];
            for (int x = 0; x < 240; ++x) {
                if (row.charAt(x) != '1') continue;
                CANVAS.set(x, y);
            }
        }
        for (int r = 0; r < 4; ++r) {
            for (int c = 0; c < 4; ++c) {
                class_2625 sign = GRID[r][c];
                if (sign == null) continue;
                boolean mirror = c < 2 ? mirrorLeft : mirrorRight;
                int ox = c * 60;
                int oy = r * 16;
                String[] lines = BadApple.extractCanvasTileLines(ox, oy, mirror);
                BadApple.updateSign(sign, lines);
            }
        }
    }

    private static String[] extractCanvasTileLines(int ox, int oy, boolean mirror) {
        List<String> rows;
        if (!mirror) {
            rows = CANVAS.rows(ox, oy, ox + 60, oy + 16);
        } else {
            TILE_TMP.clear();
            for (int y = 0; y < 16; ++y) {
                for (int x = 0; x < 60; ++x) {
                    int srcX = ox + (59 - x);
                    int srcY = oy + y;
                    if (!CANVAS.get(srcX, srcY)) continue;
                    TILE_TMP.set(x, y);
                }
            }
            rows = TILE_TMP.rows(0, 0, 60, 16);
        }
        String[] out = new String[4];
        for (int i = 0; i < 4; ++i) {
            out[i] = i < rows.size() ? rows.get(i) : "";
        }
        return out;
    }

    private static void updateSign(class_2625 sign, String[] lines) {
        class_310 mc = class_310.method_1551();
        if (mc.field_1724 == null || sign == null) {
            return;
        }
        mc.field_1724.field_3944.method_52787((class_2596)new class_2877(sign.method_11016(), true, lines[0], lines[1], lines[2], lines[3]));
    }

    private static boolean loadFramesBlocky() {
        FRAMES_BLOCKY.clear();
        frameIndexBlocky = 0;
        try {
            Files.createDirectories(FRAMES_BLOCKY_DIR, new FileAttribute[0]);
            try (Stream<Path> s = Files.list(FRAMES_BLOCKY_DIR);){
                List<Path> files = s.filter(p -> p.toString().endsWith(".txt")).sorted(Comparator.comparing(p -> p.getFileName().toString())).toList();
                for (Path p2 : files) {
                    List<String> lines = Files.readAllLines(p2);
                    if (lines.size() != 16) continue;
                    boolean ok = true;
                    for (String l : lines) {
                        if (l.length() == 40) continue;
                        ok = false;
                        break;
                    }
                    if (!ok) continue;
                    FRAMES_BLOCKY.add((String[])lines.toArray(String[]::new));
                }
            }
        }
        catch (IOException e) {
            return false;
        }
        return !FRAMES_BLOCKY.isEmpty();
    }

    private static boolean loadFramesCanvas() {
        FRAMES_CANVAS.clear();
        frameIndexCanvas = 0;
        try {
            Files.createDirectories(FRAMES_CANVAS_DIR, new FileAttribute[0]);
            try (Stream<Path> s = Files.list(FRAMES_CANVAS_DIR);){
                List<Path> files = s.filter(p -> p.toString().endsWith(".txt")).sorted(Comparator.comparing(p -> p.getFileName().toString())).toList();
                for (Path p2 : files) {
                    List<String> lines = Files.readAllLines(p2);
                    if (lines.size() != 64) continue;
                    boolean ok = true;
                    for (String l : lines) {
                        if (l.length() == 240) continue;
                        ok = false;
                        break;
                    }
                    if (!ok) continue;
                    FRAMES_CANVAS.add((String[])lines.toArray(String[]::new));
                }
            }
        }
        catch (IOException e) {
            return false;
        }
        return !FRAMES_CANVAS.isEmpty();
    }

    private static String[] getNextFrameBlocky() {
        if (FRAMES_BLOCKY.isEmpty()) {
            return null;
        }
        String[] f = FRAMES_BLOCKY.get(frameIndexBlocky);
        frameIndexBlocky = (frameIndexBlocky + 1) % FRAMES_BLOCKY.size();
        return f;
    }

    private static String[] getNextFrameCanvas() {
        if (FRAMES_CANVAS.isEmpty()) {
            return null;
        }
        String[] f = FRAMES_CANVAS.get(frameIndexCanvas);
        frameIndexCanvas = (frameIndexCanvas + 1) % FRAMES_CANVAS.size();
        return f;
    }

    private static String padRight(String s, int len, char ch) {
        if (s.length() >= len) {
            return s;
        }
        StringBuilder sb = new StringBuilder(s);
        while (sb.length() < len) {
            sb.append(ch);
        }
        return sb.toString();
    }

    @Environment(value=EnvType.CLIENT)
    private static enum State {
        RUN,
        PAUSE,
        STOP;

    }

    @Environment(value=EnvType.CLIENT)
    private static enum Mode {
        BLOCKY,
        CANVAS;

    }
}

