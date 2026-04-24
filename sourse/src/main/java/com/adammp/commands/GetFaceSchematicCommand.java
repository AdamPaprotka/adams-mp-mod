package com.adammp.commands;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.imageio.ImageIO;
import meteordevelopment.meteorclient.commands.Command;
import meteordevelopment.meteorclient.commands.Commands;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_2172;
import net.minecraft.class_2246;
import net.minecraft.class_2248;
import net.minecraft.class_2487;
import net.minecraft.class_2499;
import net.minecraft.class_2501;
import net.minecraft.class_2507;
import net.minecraft.class_2520;
import net.minecraft.class_2960;
import net.minecraft.class_310;
import net.minecraft.class_7923;

@Environment(value=EnvType.CLIENT)
public class GetFaceSchematicCommand
extends Command {
    private static final Map<class_2248, int[]> BLOCK_RGB = new HashMap<class_2248, int[]>();

    public GetFaceSchematicCommand() {
        super("getfaceshem", "Creates a Litematica schematic of a player's 2D face (8x8).", new String[0]);
    }

    public void build(LiteralArgumentBuilder<class_2172> builder) {
        builder.then(GetFaceSchematicCommand.argument((String)"player", (ArgumentType)StringArgumentType.word()).executes(ctx -> {
            String player = StringArgumentType.getString((CommandContext)ctx, (String)"player");
            try {
                this.run(player);
            }
            catch (Exception e) {
                e.printStackTrace();
                ChatUtils.error((String)("Failed: " + e.getMessage()), (Object[])new Object[0]);
            }
            return 1;
        }));
    }

    private void run(String username) throws Exception {
        ChatUtils.info((String)("Fetching skin for " + username + "..."), (Object[])new Object[0]);
        BufferedImage skin = this.downloadSkin(username);
        if (skin == null) {
            throw new IOException("Failed to read skin image.");
        }
        int[][] face = this.composeFaceRGBA(skin);
        List<class_2248> paletteBlocks = this.buildPalette();
        int[] blockIds = new int[64];
        ArrayList<class_2248> usedBlocks = new ArrayList<class_2248>();
        HashMap<class_2248, Integer> blockToIndex = new HashMap<class_2248, Integer>();
        for (int z = 0; z < 8; ++z) {
            for (int x = 0; x < 8; ++x) {
                int argb = face[z][x];
                int a = argb >>> 24 & 0xFF;
                int r = argb >>> 16 & 0xFF;
                int g = argb >>> 8 & 0xFF;
                int b = argb & 0xFF;
                class_2248 block = a < 10 ? class_2246.field_10124 : this.nearestBlock(paletteBlocks, r, g, b);
                Integer idx = (Integer)blockToIndex.get(block);
                if (idx == null) {
                    idx = usedBlocks.size();
                    usedBlocks.add(block);
                    blockToIndex.put(block, idx);
                }
                blockIds[z * 8 + x] = idx;
            }
        }
        class_2487 root = new class_2487();
        root.method_10569("Version", 6);
        root.method_10569("MinecraftDataVersion", 3953);
        root.method_10566("Metadata", (class_2520)this.makeMetadata(username));
        root.method_10566("Regions", (class_2520)this.makeRegions(usedBlocks, blockIds));
        Path runDir = class_310.method_1551().field_1697.toPath();
        File schemDir = runDir.resolve("schematics").toFile();
        if (!schemDir.exists()) {
            schemDir.mkdirs();
        }
        String fileName = username + "FaceSchematic.litematic";
        File outFile = new File(schemDir, fileName);
        try (FileOutputStream fos = new FileOutputStream(outFile);){
            class_2507.method_10634((class_2487)root, (OutputStream)fos);
        }
        ChatUtils.info((String)("Saved: /schematics/" + fileName), (Object[])new Object[0]);
    }

    private BufferedImage downloadSkin(String username) throws IOException {
        String encoded = URLEncoder.encode(username, StandardCharsets.UTF_8);
        URL url = URI.create("https://minotar.net/skin/" + encoded).toURL();
        HttpURLConnection c = (HttpURLConnection)url.openConnection();
        c.setConnectTimeout(7000);
        c.setReadTimeout(10000);
        c.setRequestProperty("User-Agent", "AdamsMP-SkinFetcher/1.0");
        c.connect();
        int code = c.getResponseCode();
        if (code != 200) {
            ChatUtils.warning((String)("Skin HTTP " + code + " for " + username + ". Using Steve fallback."), (Object[])new Object[0]);
            URL steve = URI.create("https://minotar.net/skin/steve").toURL();
            try (InputStream in = steve.openStream();){
                BufferedImage bufferedImage = ImageIO.read(in);
                return bufferedImage;
            }
        }
        try (InputStream in = c.getInputStream();){
            BufferedImage bufferedImage = ImageIO.read(in);
            return bufferedImage;
        }
    }

    private int[][] composeFaceRGBA(BufferedImage skin) {
        boolean hasSecondLayer;
        int[][] out = new int[8][8];
        boolean bl = hasSecondLayer = skin.getHeight() >= 64;
        if (!hasSecondLayer) {
            ChatUtils.warning((String)"Skin is classic 64x32 format. No overlay layer.", (Object[])new Object[0]);
        }
        for (int y = 0; y < 8; ++y) {
            for (int x = 0; x < 8; ++x) {
                int over;
                int oa;
                int base = this.safeGetRGB(skin, 8 + x, 8 + y);
                out[y][x] = hasSecondLayer && (oa = (over = this.safeGetRGB(skin, 40 + x, 8 + y)) >>> 24 & 0xFF) > 10 ? this.alphaBlend(base, over) : base;
            }
        }
        return out;
    }

    private int safeGetRGB(BufferedImage img, int x, int y) {
        x = Math.max(0, Math.min(img.getWidth() - 1, x));
        y = Math.max(0, Math.min(img.getHeight() - 1, y));
        return img.getRGB(x, y);
    }

    private int alphaBlend(int base, int over) {
        int ba = base >>> 24 & 0xFF;
        int br = base >>> 16 & 0xFF;
        int bg = base >>> 8 & 0xFF;
        int bb = base & 0xFF;
        int oa = over >>> 24 & 0xFF;
        int or_ = over >>> 16 & 0xFF;
        int og = over >>> 8 & 0xFF;
        int ob = over & 0xFF;
        float aO = (float)oa / 255.0f;
        float aB = (float)ba / 255.0f;
        float aOut = aO + aB * (1.0f - aO);
        if ((double)aOut < 1.0E-6) {
            return 0;
        }
        int r = Math.round(((float)or_ * aO + (float)br * aB * (1.0f - aO)) / aOut);
        int g = Math.round(((float)og * aO + (float)bg * aB * (1.0f - aO)) / aOut);
        int b = Math.round(((float)ob * aO + (float)bb * aB * (1.0f - aO)) / aOut);
        int a = Math.round(aOut * 255.0f);
        return a << 24 | r << 16 | g << 8 | b;
    }

    private List<class_2248> buildPalette() {
        String[] ids = new String[]{"white_concrete", "light_gray_concrete", "gray_concrete", "black_concrete", "brown_concrete", "red_concrete", "orange_concrete", "yellow_concrete", "lime_concrete", "green_concrete", "cyan_concrete", "light_blue_concrete", "blue_concrete", "purple_concrete", "magenta_concrete", "pink_concrete", "white_terracotta", "orange_terracotta", "light_gray_terracotta", "brown_terracotta", "red_terracotta", "yellow_terracotta", "white_wool", "light_gray_wool", "gray_wool", "black_wool", "brown_wool", "red_wool", "orange_wool", "yellow_wool", "lime_wool", "green_wool", "cyan_wool", "light_blue_wool", "blue_wool", "purple_wool", "magenta_wool", "pink_wool"};
        ArrayList<class_2248> list = new ArrayList<class_2248>();
        for (String id : ids) {
            class_2248 b = (class_2248)class_7923.field_41175.method_63535(class_2960.method_60655((String)"minecraft", (String)id));
            if (b == class_2246.field_10124) continue;
            list.add(b);
        }
        return list;
    }

    private class_2248 nearestBlock(List<class_2248> palette, int r, int g, int b) {
        class_2248 best = class_2246.field_10107;
        double bestD = Double.MAX_VALUE;
        for (class_2248 blk : palette) {
            double db;
            double dg;
            int[] rgb = this.approxBlockColor(blk);
            double dr = rgb[0] - r;
            double d = dr * dr + (dg = (double)(rgb[1] - g)) * dg + (db = (double)(rgb[2] - b)) * db;
            if (!(d < bestD)) continue;
            bestD = d;
            best = blk;
        }
        return best;
    }

    private static void putRGB(String id, int r, int g, int b) {
        class_2248 blk = (class_2248)class_7923.field_41175.method_63535(class_2960.method_60655((String)"minecraft", (String)id));
        if (blk != class_2246.field_10124) {
            BLOCK_RGB.put(blk, new int[]{r, g, b});
        }
    }

    private int[] approxBlockColor(class_2248 b) {
        int[] c = BLOCK_RGB.get(b);
        if (c != null) {
            return c;
        }
        return new int[]{128, 128, 128};
    }

    private class_2487 makeMetadata(String username) {
        long now = Instant.now().getEpochSecond();
        class_2487 m = new class_2487();
        m.method_10582("Name", username + " Face");
        m.method_10582("Author", "AdamsMP");
        m.method_10544("TimeCreated", now);
        m.method_10544("TimeModified", now);
        class_2487 size = new class_2487();
        size.method_10569("x", 8);
        size.method_10569("y", 1);
        size.method_10569("z", 8);
        m.method_10566("EnclosingSize", (class_2520)size);
        m.method_10569("RegionCount", 1);
        return m;
    }

    private class_2487 makeRegions(List<class_2248> usedBlocks, int[] blockIds) {
        class_2487 region = new class_2487();
        class_2487 size = new class_2487();
        size.method_10569("x", 8);
        size.method_10569("y", 1);
        size.method_10569("z", 8);
        region.method_10566("Size", (class_2520)size);
        class_2487 pos = new class_2487();
        pos.method_10569("x", 0);
        pos.method_10569("y", 0);
        pos.method_10569("z", 0);
        region.method_10566("Position", (class_2520)pos);
        class_2499 palette = new class_2499();
        for (class_2248 b : usedBlocks) {
            class_2487 entry = new class_2487();
            entry.method_10582("Name", class_7923.field_41175.method_10221((Object)b).toString());
            palette.add((Object)entry);
        }
        region.method_10566("BlockStatePalette", (class_2520)palette);
        long[] packed = this.packBlockStates(blockIds, usedBlocks.size());
        region.method_10566("BlockStates", (class_2520)new class_2501(packed));
        region.method_10566("TileEntities", (class_2520)new class_2499());
        region.method_10566("Entities", (class_2520)new class_2499());
        class_2487 regions = new class_2487();
        regions.method_10566("face", (class_2520)region);
        return regions;
    }

    private long[] packBlockStates(int[] ids, int paletteSize) {
        int bits = Math.max(2, 32 - Integer.numberOfLeadingZeros(Math.max(1, paletteSize - 1)));
        int valuesPerLong = 64 / bits;
        int longCount = (int)Math.ceil((double)ids.length / (double)valuesPerLong);
        long[] arr = new long[longCount];
        int idx = 0;
        for (int i = 0; i < longCount; ++i) {
            long v = 0L;
            for (int j = 0; j < valuesPerLong && idx < ids.length; ++j, ++idx) {
                long val = (long)ids[idx] & (1L << bits) - 1L;
                v |= val << j * bits;
            }
            arr[i] = v;
        }
        return arr;
    }

    public static void init() {
        Commands.add((Command)new GetFaceSchematicCommand());
    }

    static {
        GetFaceSchematicCommand.putRGB("white_concrete", 207, 213, 214);
        GetFaceSchematicCommand.putRGB("light_gray_concrete", 125, 125, 115);
        GetFaceSchematicCommand.putRGB("gray_concrete", 54, 57, 61);
        GetFaceSchematicCommand.putRGB("black_concrete", 8, 10, 15);
        GetFaceSchematicCommand.putRGB("brown_concrete", 96, 59, 31);
        GetFaceSchematicCommand.putRGB("red_concrete", 142, 33, 33);
        GetFaceSchematicCommand.putRGB("orange_concrete", 224, 97, 0);
        GetFaceSchematicCommand.putRGB("yellow_concrete", 241, 175, 21);
        GetFaceSchematicCommand.putRGB("lime_concrete", 94, 168, 24);
        GetFaceSchematicCommand.putRGB("green_concrete", 73, 91, 36);
        GetFaceSchematicCommand.putRGB("cyan_concrete", 21, 119, 136);
        GetFaceSchematicCommand.putRGB("light_blue_concrete", 36, 137, 199);
        GetFaceSchematicCommand.putRGB("blue_concrete", 44, 46, 143);
        GetFaceSchematicCommand.putRGB("purple_concrete", 100, 32, 156);
        GetFaceSchematicCommand.putRGB("magenta_concrete", 169, 48, 159);
        GetFaceSchematicCommand.putRGB("pink_concrete", 214, 101, 143);
        GetFaceSchematicCommand.putRGB("white_terracotta", 209, 178, 161);
        GetFaceSchematicCommand.putRGB("orange_terracotta", 161, 83, 37);
        GetFaceSchematicCommand.putRGB("light_gray_terracotta", 135, 106, 97);
        GetFaceSchematicCommand.putRGB("brown_terracotta", 77, 51, 36);
        GetFaceSchematicCommand.putRGB("red_terracotta", 143, 61, 47);
        GetFaceSchematicCommand.putRGB("yellow_terracotta", 186, 133, 35);
        GetFaceSchematicCommand.putRGB("white_wool", 234, 236, 237);
        GetFaceSchematicCommand.putRGB("light_gray_wool", 142, 142, 134);
        GetFaceSchematicCommand.putRGB("gray_wool", 63, 68, 72);
        GetFaceSchematicCommand.putRGB("black_wool", 22, 22, 26);
        GetFaceSchematicCommand.putRGB("brown_wool", 114, 71, 40);
        GetFaceSchematicCommand.putRGB("red_wool", 160, 39, 34);
        GetFaceSchematicCommand.putRGB("orange_wool", 240, 119, 21);
        GetFaceSchematicCommand.putRGB("yellow_wool", 249, 199, 35);
        GetFaceSchematicCommand.putRGB("lime_wool", 112, 185, 25);
        GetFaceSchematicCommand.putRGB("green_wool", 84, 109, 27);
        GetFaceSchematicCommand.putRGB("cyan_wool", 21, 137, 145);
        GetFaceSchematicCommand.putRGB("light_blue_wool", 58, 175, 217);
        GetFaceSchematicCommand.putRGB("blue_wool", 53, 57, 157);
        GetFaceSchematicCommand.putRGB("purple_wool", 122, 42, 172);
        GetFaceSchematicCommand.putRGB("magenta_wool", 199, 78, 189);
        GetFaceSchematicCommand.putRGB("pink_wool", 237, 141, 172);
    }
}

