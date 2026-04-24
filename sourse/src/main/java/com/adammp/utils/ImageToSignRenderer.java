package com.adammp.utils;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(value=EnvType.CLIENT)
public class ImageToSignRenderer {
    private static final Map<Integer, String> COLOR_MAP = Map.ofEntries(Map.entry(0, "\u00a70"), Map.entry(170, "\u00a71"), Map.entry(43520, "\u00a72"), Map.entry(43690, "\u00a73"), Map.entry(0xAA0000, "\u00a74"), Map.entry(0xAA00AA, "\u00a75"), Map.entry(0xFFAA00, "\u00a76"), Map.entry(0xAAAAAA, "\u00a77"), Map.entry(0x555555, "\u00a78"), Map.entry(0x5555FF, "\u00a79"), Map.entry(0x55FF55, "\u00a7a"), Map.entry(0x55FFFF, "\u00a7b"), Map.entry(0xFF5555, "\u00a7c"), Map.entry(0xFF55FF, "\u00a7d"), Map.entry(0xFFFF55, "\u00a7e"), Map.entry(0xFFFFFF, "\u00a7f"));

    private static String getClosestColor(int rgb) {
        Color input = new Color(rgb);
        double minDist = Double.MAX_VALUE;
        String closest = "\u00a7f";
        for (Map.Entry<Integer, String> entry : COLOR_MAP.entrySet()) {
            Color c = new Color(entry.getKey());
            double dist = Math.pow(c.getRed() - input.getRed(), 2.0) + Math.pow(c.getGreen() - input.getGreen(), 2.0) + Math.pow(c.getBlue() - input.getBlue(), 2.0);
            if (!(dist < minDist)) continue;
            minDist = dist;
            closest = entry.getValue();
        }
        return closest;
    }

    private static int clamp(int val) {
        return Math.max(0, Math.min(255, val));
    }

    public static BufferedImage resizeImage(BufferedImage original, int width, int height) {
        Image tmp = original.getScaledInstance(width, height, 4);
        BufferedImage resized = new BufferedImage(width, height, 1);
        Graphics2D g2d = resized.createGraphics();
        g2d.drawImage(tmp, 0, 0, null);
        g2d.dispose();
        return resized;
    }

    public static List<String[]> imageToSignTextBayer(BufferedImage image) {
        int width = image.getWidth();
        int height = image.getHeight();
        int[][] bayer10x4 = new int[][]{{15, 135, 45, 165, 30, 150, 60, 180, 90, 210}, {195, 75, 225, 105, 210, 90, 240, 120, 60, 180}, {60, 180, 30, 150, 15, 135, 45, 165, 225, 105}, {240, 120, 210, 90, 195, 75, 225, 105, 15, 135}};
        int signCols = (int)Math.ceil((double)width / 10.0);
        int signRows = (int)Math.ceil((double)height / 4.0);
        ArrayList<String[]> signs = new ArrayList<String[]>();
        for (int sy = 0; sy < signRows; ++sy) {
            for (int sx = 0; sx < signCols; ++sx) {
                String[] lines = new String[4];
                for (int ly = 0; ly < 4; ++ly) {
                    int px;
                    int py = sy * 4 + ly;
                    if (py >= height) {
                        lines[ly] = "";
                        continue;
                    }
                    StringBuilder line = new StringBuilder();
                    String lastColor = "";
                    for (int lx = 0; lx < 10 && (px = sx * 10 + lx) < width; ++lx) {
                        int b;
                        int g;
                        Color orig = new Color(image.getRGB(px, py));
                        int threshold = bayer10x4[ly][lx] - 128;
                        int r = ImageToSignRenderer.clamp(orig.getRed() + threshold);
                        int ditheredRGB = new Color(r, g = ImageToSignRenderer.clamp(orig.getGreen() + threshold), b = ImageToSignRenderer.clamp(orig.getBlue() + threshold)).getRGB();
                        String color = ImageToSignRenderer.getClosestColor(ditheredRGB);
                        if (!color.equals(lastColor)) {
                            line.append(color);
                            lastColor = color;
                        }
                        line.append("\u2588");
                    }
                    lines[ly] = line.toString();
                }
                signs.add(lines);
            }
        }
        return signs;
    }

    public static List<String[]> imageToSignTextNoDither(BufferedImage image) {
        int width = image.getWidth();
        int height = image.getHeight();
        int signCols = (int)Math.ceil((double)width / 10.0);
        int signRows = (int)Math.ceil((double)height / 4.0);
        ArrayList<String[]> signs = new ArrayList<String[]>();
        for (int sy = 0; sy < signRows; ++sy) {
            for (int sx = 0; sx < signCols; ++sx) {
                String[] lines = new String[4];
                for (int ly = 0; ly < 4; ++ly) {
                    int px;
                    int py = sy * 4 + ly;
                    if (py >= height) {
                        lines[ly] = "";
                        continue;
                    }
                    StringBuilder line = new StringBuilder();
                    String lastColor = "";
                    for (int lx = 0; lx < 10 && (px = sx * 10 + lx) < width; ++lx) {
                        int rgb = image.getRGB(px, py);
                        String color = ImageToSignRenderer.getClosestColor(rgb);
                        if (!color.equals(lastColor)) {
                            line.append(color);
                            lastColor = color;
                        }
                        line.append("\u2588");
                    }
                    lines[ly] = line.toString();
                }
                signs.add(lines);
            }
        }
        return signs;
    }
}

