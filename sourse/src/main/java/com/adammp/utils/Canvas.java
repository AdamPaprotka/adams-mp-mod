package com.adammp.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(value=EnvType.CLIENT)
public class Canvas {
    private static final int[][] PIXEL_MAP = new int[][]{{1, 8}, {2, 16}, {4, 32}, {64, 128}};
    private static final int BRAILLE_OFFSET = 10240;
    private final Map<Integer, Map<Integer, Object>> chars = new HashMap<Integer, Map<Integer, Object>>();
    private final String lineEnding;

    public Canvas() {
        this(System.lineSeparator());
    }

    public Canvas(String lineEnding) {
        this.lineEnding = lineEnding;
    }

    private static int normalize(Number n) {
        if (n instanceof Integer) {
            return n.intValue();
        }
        if (n instanceof Double || n instanceof Float) {
            return (int)Math.round(n.doubleValue());
        }
        throw new IllegalArgumentException();
    }

    private static int[] getPos(int x, int y) {
        return new int[]{x / 2, y / 4};
    }

    public void clear() {
        this.chars.clear();
    }

    public void set(Number xN, Number yN) {
        int x = Canvas.normalize(xN);
        int y = Canvas.normalize(yN);
        int[] pos = Canvas.getPos(x, y);
        int col = pos[0];
        int row = pos[1];
        this.chars.computeIfAbsent(row, k -> new HashMap());
        Object v = this.chars.get(row).getOrDefault(col, 0);
        if (!(v instanceof Integer)) {
            return;
        }
        int mask = PIXEL_MAP[y % 4][x % 2];
        this.chars.get(row).put(col, (Integer)v | mask);
    }

    public void unset(Number xN, Number yN) {
        int x = Canvas.normalize(xN);
        int y = Canvas.normalize(yN);
        int[] pos = Canvas.getPos(x, y);
        int col = pos[0];
        int row = pos[1];
        Map<Integer, Object> rowMap = this.chars.get(row);
        if (rowMap == null) {
            return;
        }
        Object v = rowMap.get(col);
        if (v instanceof Integer) {
            int mask = PIXEL_MAP[y % 4][x % 2];
            int nv = (Integer)v & ~mask;
            if (nv == 0) {
                rowMap.remove(col);
            } else {
                rowMap.put(col, nv);
            }
        }
        if (rowMap.isEmpty()) {
            this.chars.remove(row);
        }
    }

    public void toggle(Number xN, Number yN) {
        int x = Canvas.normalize(xN);
        int y = Canvas.normalize(yN);
        int[] pos = Canvas.getPos(x, y);
        int col = pos[0];
        int row = pos[1];
        Object v = this.chars.getOrDefault(row, Collections.emptyMap()).get(col);
        int mask = PIXEL_MAP[y % 4][x % 2];
        if (!(v instanceof Integer) || ((Integer)v & mask) != 0) {
            this.unset(x, y);
        } else {
            this.set(x, y);
        }
    }

    public void setText(Number xN, Number yN, String text) {
        int x = Canvas.normalize(xN);
        int y = Canvas.normalize(yN);
        int[] pos = Canvas.getPos(x, y);
        int col = pos[0];
        int row = pos[1];
        this.chars.computeIfAbsent(row, k -> new HashMap());
        for (int i = 0; i < text.length(); ++i) {
            this.chars.get(row).put(col + i, String.valueOf(text.charAt(i)));
        }
    }

    public boolean get(Number xN, Number yN) {
        int x = Canvas.normalize(xN);
        int y = Canvas.normalize(yN);
        int[] pos = Canvas.getPos(x, y);
        int col = pos[0];
        int row = pos[1];
        Object v = this.chars.getOrDefault(row, Collections.emptyMap()).get(col);
        if (v == null) {
            return false;
        }
        if (!(v instanceof Integer)) {
            return true;
        }
        int mask = PIXEL_MAP[y % 4][x % 2];
        return ((Integer)v & mask) != 0;
    }

    public List<String> rows(Integer minX, Integer minY, Integer maxX, Integer maxY) {
        int minCol;
        int maxRow;
        int minRow;
        int n = minY != null ? minY / 4 : (minRow = this.chars.isEmpty() ? 0 : Collections.min(this.chars.keySet()));
        int n2 = maxY != null ? (maxY - 1) / 4 : (maxRow = this.chars.isEmpty() ? -1 : Collections.max(this.chars.keySet()));
        int n3 = minX != null ? minX / 2 : (minCol = this.chars.isEmpty() ? 0 : this.chars.values().stream().mapToInt(m -> (Integer)Collections.min(m.keySet())).min().getAsInt());
        int maxCol = maxX != null ? (maxX - 1) / 2 : (this.chars.isEmpty() ? -1 : this.chars.values().stream().mapToInt(m -> (Integer)Collections.max(m.keySet())).max().getAsInt());
        ArrayList<String> out = new ArrayList<String>();
        for (int r = minRow; r <= maxRow; ++r) {
            Map<Integer, Object> row = this.chars.get(r);
            StringBuilder sb = new StringBuilder();
            for (int c = minCol; c <= maxCol; ++c) {
                Object v;
                Object object = v = row != null ? row.get(c) : null;
                if (v == null) {
                    sb.append('\u2800');
                    continue;
                }
                if (v instanceof Integer) {
                    sb.append(Character.toChars(10240 + (Integer)v));
                    continue;
                }
                sb.append(v);
            }
            out.add(sb.toString());
        }
        return out;
    }

    public String frame(Integer minX, Integer minY, Integer maxX, Integer maxY) {
        return String.join((CharSequence)this.lineEnding, this.rows(minX, minY, maxX, maxY));
    }

    public String frame() {
        return this.frame(null, null, null, null);
    }
}

