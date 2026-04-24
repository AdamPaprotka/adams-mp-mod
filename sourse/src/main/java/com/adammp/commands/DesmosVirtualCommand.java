package com.adammp.commands;

import com.adammp.commands.FillCommand;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import meteordevelopment.meteorclient.commands.Command;
import meteordevelopment.meteorclient.commands.Commands;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_2172;
import net.minecraft.class_2338;
import net.minecraft.class_2561;
import net.minecraft.class_2596;
import net.minecraft.class_2877;
import net.minecraft.class_310;
import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;

@Environment(value=EnvType.CLIENT)
public class DesmosVirtualCommand
extends Command {
    private final class_310 mc = class_310.method_1551();
    private static final int TILE_W = 10;
    private static final int TILE_H = 4;
    private static final double X_HALF_RANGE = 10.0;
    private static final double Y_HALF_RANGE = 10.0;
    private static final char C_BG = '0';
    private static final char C_AXIS = '7';
    private static final char C_GRAF = 'f';

    public static void init() {
        Commands.add((Command)new DesmosVirtualCommand());
    }

    public DesmosVirtualCommand() {
        super("desmosv", "Plots a smooth graph on signs using a merged virtual canvas (10x4 per sign).", new String[0]);
    }

    public void build(LiteralArgumentBuilder<class_2172> builder) {
        builder.then(DesmosVirtualCommand.argument((String)"function", (ArgumentType)StringArgumentType.greedyString()).executes(ctx -> {
            String func = StringArgumentType.getString((CommandContext)ctx, (String)"function");
            this.render(func);
            return 1;
        }));
    }

    private void render(String func) {
        int y;
        Expression expr;
        if (this.mc.field_1724 == null || this.mc.field_1687 == null) {
            return;
        }
        class_2338 p1 = FillCommand.sel1;
        class_2338 p2 = FillCommand.sel2;
        if (p1 == null || p2 == null) {
            ChatUtils.sendMsg((class_2561)class_2561.method_43470((String)"Use .sel1 and .sel2 first."));
            return;
        }
        int minX = Math.min(p1.method_10263(), p2.method_10263());
        int maxX = Math.max(p1.method_10263(), p2.method_10263());
        int minY = Math.min(p1.method_10264(), p2.method_10264());
        int maxY = Math.max(p1.method_10264(), p2.method_10264());
        int z = Math.min(p1.method_10260(), p2.method_10260());
        int signW = maxX - minX + 1;
        int signH = maxY - minY + 1;
        int pixW = signW * 10;
        int pixH = signH * 4;
        int centerX = pixW / 2;
        int centerY = pixH / 2;
        ChatUtils.sendMsg((class_2561)class_2561.method_43470((String)("Canvas: " + pixW + "x" + pixH + " (signs " + signW + "x" + signH + ")")));
        func = func.replace(" ", "");
        if (func.startsWith("y=")) {
            func = func.substring(2);
        }
        try {
            expr = new ExpressionBuilder(func).variables(new String[]{"x"}).build();
        }
        catch (Exception e) {
            ChatUtils.sendMsg((class_2561)class_2561.method_43470((String)("Invalid function: " + e.getMessage())));
            return;
        }
        char[][] fb = new char[pixH][pixW];
        for (y = 0; y < pixH; ++y) {
            for (int x = 0; x < pixW; ++x) {
                fb[y][x] = 48;
            }
        }
        for (int x = 0; x < pixW; ++x) {
            fb[centerY][x] = 55;
        }
        for (y = 0; y < pixH; ++y) {
            fb[y][centerX] = 55;
        }
        boolean prevValid = false;
        int prevX = 0;
        int prevY = 0;
        for (int px = 0; px < pixW; ++px) {
            double yVal;
            double xVal = this.mapX(px, centerX);
            try {
                yVal = expr.setVariable("x", xVal).evaluate();
            }
            catch (Exception ex) {
                prevValid = false;
                continue;
            }
            if (!Double.isFinite(yVal)) {
                prevValid = false;
                continue;
            }
            int py = this.mapY(yVal, centerY);
            if (py < 0 || py >= pixH) {
                prevValid = false;
                continue;
            }
            if (!prevValid) {
                this.plot(fb, px, py, 'f');
                prevValid = true;
                prevX = px;
                prevY = py;
                continue;
            }
            if (Math.abs(py - prevY) > pixH / 3) {
                this.plot(fb, px, py, 'f');
                prevX = px;
                prevY = py;
                continue;
            }
            this.drawLineBresenham(fb, prevX, prevY, px, py, 'f');
            prevX = px;
            prevY = py;
        }
        for (int sy = 0; sy < signH; ++sy) {
            for (int sx = 0; sx < signW; ++sx) {
                int baseX = sx * 10;
                int baseY = sy * 4;
                String l1 = this.buildSignLine(fb, baseY + 0, baseX);
                String l2 = this.buildSignLine(fb, baseY + 1, baseX);
                String l3 = this.buildSignLine(fb, baseY + 2, baseX);
                String l4 = this.buildSignLine(fb, baseY + 3, baseX);
                class_2338 pos = new class_2338(minX + sx, minY + sy, z);
                this.mc.field_1724.field_3944.method_52787((class_2596)new class_2877(pos, true, l1, l2, l3, l4));
            }
        }
        ChatUtils.sendMsg((class_2561)class_2561.method_43470((String)"Done (smooth curves)."));
    }

    private double mapX(int px, int centerX) {
        if (centerX == 0) {
            return 0.0;
        }
        return (double)(px - centerX) / (double)centerX * 10.0;
    }

    private int mapY(double yVal, int centerY) {
        if (centerY == 0) {
            return 0;
        }
        return centerY - (int)Math.round(yVal / 10.0 * (double)centerY);
    }

    private void plot(char[][] fb, int x, int y, char color) {
        if (y < 0 || y >= fb.length) {
            return;
        }
        if (x < 0 || x >= fb[0].length) {
            return;
        }
        fb[y][x] = color;
    }

    private void drawLineBresenham(char[][] fb, int x0, int y0, int x1, int y1, char color) {
        int dx = Math.abs(x1 - x0);
        int dy = Math.abs(y1 - y0);
        int sx = x0 < x1 ? 1 : -1;
        int sy = y0 < y1 ? 1 : -1;
        int err = dx - dy;
        int x = x0;
        int y = y0;
        while (true) {
            this.plot(fb, x, y, color);
            if (x == x1 && y == y1) break;
            int e2 = 2 * err;
            if (e2 > -dy) {
                err -= dy;
                x += sx;
            }
            if (e2 >= dx) continue;
            err += dx;
            y += sy;
        }
    }

    private String buildSignLine(char[][] fb, int rowY, int startX) {
        StringBuilder sb = new StringBuilder(32);
        char last = fb[rowY][startX];
        sb.append('\u00a7').append(last);
        for (int i = 0; i < 10; ++i) {
            char c = fb[rowY][startX + i];
            if (c != last) {
                sb.append('\u00a7').append(c);
                last = c;
            }
            sb.append('\u2588');
        }
        return sb.toString();
    }
}

