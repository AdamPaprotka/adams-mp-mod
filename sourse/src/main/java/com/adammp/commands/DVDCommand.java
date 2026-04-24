package com.adammp.commands;

import com.adammp.commands.FillCommand;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import meteordevelopment.meteorclient.commands.Command;
import meteordevelopment.meteorclient.commands.Commands;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_2172;
import net.minecraft.class_2338;
import net.minecraft.class_2586;
import net.minecraft.class_2596;
import net.minecraft.class_2625;
import net.minecraft.class_2877;

@Environment(value=EnvType.CLIENT)
public class DVDCommand
extends Command {
    private static boolean running = false;
    private static float delay = 0.1f;
    private static int pixelSize = 1;
    private static int gX = 0;
    private static int gY = 0;
    private static int dX = 1;
    private static int dY = 1;
    private static int gridW = 0;
    private static int gridH = 0;
    private static ArrayList<ArrayList<class_2625>> screen = new ArrayList();

    public static void init() {
        Commands.add((Command)new DVDCommand());
    }

    public DVDCommand() {
        super("dvd", "DVD logo simulation across signs with pixel size.", new String[0]);
    }

    private static void buildGrid() {
        class_2625 s2;
        if (FillCommand.sel1 == null || FillCommand.sel2 == null) {
            return;
        }
        class_2338 min = new class_2338(Math.min(FillCommand.sel1.method_10263(), FillCommand.sel2.method_10263()), Math.min(FillCommand.sel1.method_10264(), FillCommand.sel2.method_10264()), Math.min(FillCommand.sel1.method_10260(), FillCommand.sel2.method_10260()));
        class_2338 max = new class_2338(Math.max(FillCommand.sel1.method_10263(), FillCommand.sel2.method_10263()), Math.max(FillCommand.sel1.method_10264(), FillCommand.sel2.method_10264()), Math.max(FillCommand.sel1.method_10260(), FillCommand.sel2.method_10260()));
        ArrayList<class_2625> signs = new ArrayList<class_2625>();
        for (int x = min.method_10263(); x <= max.method_10263(); ++x) {
            for (int y = min.method_10264(); y <= max.method_10264(); ++y) {
                for (int z = min.method_10260(); z <= max.method_10260(); ++z) {
                    class_2338 p = new class_2338(x, y, z);
                    class_2586 class_25862 = DVDCommand.mc.field_1687.method_8321(p);
                    if (!(class_25862 instanceof class_2625)) continue;
                    class_2625 s3 = (class_2625)class_25862;
                    signs.add(s3);
                }
            }
        }
        if (signs.isEmpty()) {
            ChatUtils.info((String)"No signs found.", (Object[])new Object[0]);
            return;
        }
        signs.sort(Comparator.comparingInt(s -> -s.method_11016().method_10264()).thenComparingInt(s -> s.method_11016().method_10263()));
        int firstY = ((class_2625)signs.get(0)).method_11016().method_10264();
        gridW = 0;
        Iterator y = signs.iterator();
        while (y.hasNext() && (s2 = (class_2625)y.next()).method_11016().method_10264() == firstY) {
            ++gridW;
        }
        gridH = (int)Math.ceil((double)signs.size() / (double)gridW);
        screen.clear();
        int idx = 0;
        for (int r = 0; r < gridH; ++r) {
            ArrayList<class_2625> row = new ArrayList<class_2625>();
            for (int c = 0; c < gridW; ++c) {
                if (idx < signs.size()) {
                    row.add((class_2625)signs.get(idx++));
                    continue;
                }
                row.add(null);
            }
            screen.add(row);
        }
        gX = 0;
        gY = 0;
        dX = 1;
        dY = 1;
    }

    private static void drawPixel(class_2625 sign, int px, int py, boolean active, int size) {
        if (sign == null) {
            return;
        }
        String bg = "\u00a70\u2588\u00a70\u2588\u00a70\u2588\u00a70\u2588\u00a70\u2588\u00a70\u2588\u00a70\u2588\u00a70\u2588\u00a70\u2588\u00a70\u2588\u00a70\u2588";
        String[] rows = new String[]{bg, bg, bg, bg};
        if (active) {
            for (int sy = 0; sy < size; ++sy) {
                int row = py + sy;
                if (row < 0 || row >= 4) continue;
                StringBuilder sb = new StringBuilder(bg);
                for (int sx = 0; sx < size; ++sx) {
                    int col = px + sx;
                    if (col < 0 || col >= 10) continue;
                    int index = col * 3;
                    sb.replace(index, index + 3, "\u00a7f\u2588");
                }
                rows[row] = sb.toString();
            }
        }
        String[] out = new String[]{rows[0], rows[1], rows[2], rows[3]};
        mc.method_1562().method_52787((class_2596)new class_2877(sign.method_11016(), true, out[0], out[1], out[2], out[3]));
    }

    private static void frame() {
        if (!running) {
            return;
        }
        int totalW = gridW * 10;
        int totalH = gridH * 4;
        gY += dY;
        if ((gX += dX) < 0) {
            gX = 0;
            dX *= -1;
        } else if (gX + pixelSize > totalW) {
            gX = totalW - pixelSize;
            dX *= -1;
        }
        if (gY < 0) {
            gY = 0;
            dY *= -1;
        } else if (gY + pixelSize > totalH) {
            gY = totalH - pixelSize;
            dY *= -1;
        }
        int signX = gX / 10;
        int signY = gY / 4;
        int px = gX % 10;
        int py = gY % 4;
        if (signX < 0 || signX >= gridW) {
            return;
        }
        if (signY < 0 || signY >= gridH) {
            return;
        }
        for (int y = 0; y < gridH; ++y) {
            for (int x = 0; x < gridW; ++x) {
                class_2625 sign = screen.get(y).get(x);
                boolean active = x == signX && y == signY;
                DVDCommand.drawPixel(sign, px, py, active, pixelSize);
            }
        }
    }

    public void build(LiteralArgumentBuilder<class_2172> builder) {
        builder.then(DVDCommand.argument((String)"size", (ArgumentType)IntegerArgumentType.integer((int)1)).then(DVDCommand.argument((String)"run", (ArgumentType)BoolArgumentType.bool()).then(DVDCommand.argument((String)"delay", (ArgumentType)FloatArgumentType.floatArg((float)0.01f)).executes(ctx -> {
            pixelSize = IntegerArgumentType.getInteger((CommandContext)ctx, (String)"size");
            boolean start = BoolArgumentType.getBool((CommandContext)ctx, (String)"run");
            delay = FloatArgumentType.getFloat((CommandContext)ctx, (String)"delay");
            if (!start) {
                running = false;
                ChatUtils.info((String)"DVD stopped.", (Object[])new Object[0]);
                return 1;
            }
            DVDCommand.buildGrid();
            if (screen.isEmpty()) {
                ChatUtils.info((String)"No screen to render DVD.", (Object[])new Object[0]);
                return 1;
            }
            running = true;
            ChatUtils.info((String)("DVD started (pixel size " + pixelSize + ")."), (Object[])new Object[0]);
            new Thread(() -> {
                while (running) {
                    DVDCommand.frame();
                    try {
                        Thread.sleep((long)(delay * 1000.0f));
                    }
                    catch (Exception exception) {}
                }
            }).start();
            return 1;
        }))));
    }
}

