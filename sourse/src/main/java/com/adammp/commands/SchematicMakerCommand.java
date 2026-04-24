package com.adammp.commands;

import com.adammp.commands.FillCommand;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import meteordevelopment.meteorclient.commands.Command;
import meteordevelopment.meteorclient.commands.Commands;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_2172;
import net.minecraft.class_2246;
import net.minecraft.class_2248;
import net.minecraft.class_2338;
import net.minecraft.class_2487;
import net.minecraft.class_2499;
import net.minecraft.class_2501;
import net.minecraft.class_2507;
import net.minecraft.class_2520;
import net.minecraft.class_2960;
import net.minecraft.class_310;
import net.minecraft.class_7923;

@Environment(value=EnvType.CLIENT)
public class SchematicMakerCommand
extends Command {
    public SchematicMakerCommand() {
        super("schematicmaker", "Create parametric .litematic shapes", new String[0]);
    }

    public void build(LiteralArgumentBuilder<class_2172> builder) {
        builder.then(SchematicMakerCommand.literal((String)"fill").then(SchematicMakerCommand.argument((String)"block", (ArgumentType)StringArgumentType.word()).executes(ctx -> {
            this.runFill(StringArgumentType.getString((CommandContext)ctx, (String)"block"));
            return 1;
        })));
        builder.then(SchematicMakerCommand.literal((String)"grid").then(SchematicMakerCommand.argument((String)"block1", (ArgumentType)StringArgumentType.word()).then(((RequiredArgumentBuilder)SchematicMakerCommand.argument((String)"block2", (ArgumentType)StringArgumentType.word()).executes(ctx -> {
            this.runGrid(StringArgumentType.getString((CommandContext)ctx, (String)"block1"), StringArgumentType.getString((CommandContext)ctx, (String)"block2"), 1);
            return 1;
        })).then(SchematicMakerCommand.argument((String)"fsize", (ArgumentType)IntegerArgumentType.integer((int)1)).executes(ctx -> {
            this.runGrid(StringArgumentType.getString((CommandContext)ctx, (String)"block1"), StringArgumentType.getString((CommandContext)ctx, (String)"block2"), IntegerArgumentType.getInteger((CommandContext)ctx, (String)"fsize"));
            return 1;
        })))));
        builder.then(SchematicMakerCommand.literal((String)"clear").executes(ctx -> {
            this.runClear();
            return 1;
        }));
        builder.then(SchematicMakerCommand.literal((String)"ball").then(SchematicMakerCommand.argument((String)"radius", (ArgumentType)IntegerArgumentType.integer((int)1)).then(SchematicMakerCommand.argument((String)"block", (ArgumentType)StringArgumentType.word()).executes(ctx -> {
            this.runBall(IntegerArgumentType.getInteger((CommandContext)ctx, (String)"radius"), StringArgumentType.getString((CommandContext)ctx, (String)"block"), false);
            return 1;
        }))));
        builder.then(SchematicMakerCommand.literal((String)"hball").then(SchematicMakerCommand.argument((String)"radius", (ArgumentType)IntegerArgumentType.integer((int)1)).then(SchematicMakerCommand.argument((String)"block", (ArgumentType)StringArgumentType.word()).executes(ctx -> {
            this.runBall(IntegerArgumentType.getInteger((CommandContext)ctx, (String)"radius"), StringArgumentType.getString((CommandContext)ctx, (String)"block"), true);
            return 1;
        }))));
        builder.then(SchematicMakerCommand.literal((String)"cube").then(SchematicMakerCommand.argument((String)"size", (ArgumentType)IntegerArgumentType.integer((int)1)).then(SchematicMakerCommand.argument((String)"block", (ArgumentType)StringArgumentType.word()).executes(ctx -> {
            this.runCube(IntegerArgumentType.getInteger((CommandContext)ctx, (String)"size"), StringArgumentType.getString((CommandContext)ctx, (String)"block"), false);
            return 1;
        }))));
        builder.then(SchematicMakerCommand.literal((String)"hcube").then(SchematicMakerCommand.argument((String)"size", (ArgumentType)IntegerArgumentType.integer((int)1)).then(SchematicMakerCommand.argument((String)"block", (ArgumentType)StringArgumentType.word()).executes(ctx -> {
            this.runCube(IntegerArgumentType.getInteger((CommandContext)ctx, (String)"size"), StringArgumentType.getString((CommandContext)ctx, (String)"block"), true);
            return 1;
        }))));
        builder.then(((LiteralArgumentBuilder)SchematicMakerCommand.literal((String)"mengersponge").then(SchematicMakerCommand.literal((String)"normal").then(SchematicMakerCommand.argument((String)"layer", (ArgumentType)IntegerArgumentType.integer((int)1, (int)5)).then(SchematicMakerCommand.argument((String)"block", (ArgumentType)StringArgumentType.word()).executes(ctx -> {
            this.runMenger("normal", IntegerArgumentType.getInteger((CommandContext)ctx, (String)"layer"), StringArgumentType.getString((CommandContext)ctx, (String)"block"));
            return 1;
        }))))).then(SchematicMakerCommand.literal((String)"noise").then(SchematicMakerCommand.argument((String)"layer", (ArgumentType)IntegerArgumentType.integer((int)1, (int)5)).then(SchematicMakerCommand.argument((String)"block", (ArgumentType)StringArgumentType.greedyString()).executes(ctx -> {
            this.runMenger("noise", IntegerArgumentType.getInteger((CommandContext)ctx, (String)"layer"), StringArgumentType.getString((CommandContext)ctx, (String)"block"));
            return 1;
        })))));
    }

    private void runGrid(String block1Name, String block2Name, int fsize) {
        if (FillCommand.sel1 == null || FillCommand.sel2 == null) {
            ChatUtils.error((String)"sel1 / sel2 not set", (Object[])new Object[0]);
            return;
        }
        class_2248 b1 = this.getBlock(block1Name);
        if (b1 == null) {
            return;
        }
        class_2248 b2 = this.getBlock(block2Name);
        if (b2 == null) {
            return;
        }
        class_2338 a = FillCommand.sel1;
        class_2338 c = FillCommand.sel2;
        int sx = Math.abs(a.method_10263() - c.method_10263()) + 1;
        int sy = Math.abs(a.method_10264() - c.method_10264()) + 1;
        int sz = Math.abs(a.method_10260() - c.method_10260()) + 1;
        int[] ids = new int[sx * sy * sz];
        int i = 0;
        for (int y = 0; y < sy; ++y) {
            for (int z = 0; z < sz; ++z) {
                for (int x = 0; x < sx; ++x) {
                    int gx = x / fsize;
                    int gz = z / fsize;
                    boolean first = (gx + gz & 1) == 0;
                    ids[i++] = first ? 1 : 2;
                }
            }
        }
        this.saveGrid(block1Name, block2Name, sx, sy, sz, b1, b2, ids);
    }

    private void saveGrid(String block1, String block2, int x, int y, int z, class_2248 b1, class_2248 b2, int[] ids) {
        try {
            class_2487 root = new class_2487();
            root.method_10569("Version", 6);
            root.method_10569("MinecraftDataVersion", 4189);
            root.method_10566("Metadata", (class_2520)this.meta("grid", block1 + "," + block2, x, y, z));
            root.method_10566("Regions", (class_2520)this.regionsGrid(b1, b2, ids, x, y, z));
            File dir = new File(class_310.method_1551().field_1697, "schematics");
            dir.mkdirs();
            String safe1 = block1.replace(':', '_');
            String safe2 = block2.replace(':', '_');
            String name = "schematicmaker_grid_" + safe1 + "_" + safe2 + "_" + x + "x" + y + "x" + z + ".litematic";
            try (FileOutputStream out = new FileOutputStream(new File(dir, name));){
                class_2507.method_10634((class_2487)root, (OutputStream)out);
            }
            ChatUtils.info((String)("Saved " + name), (Object[])new Object[0]);
        }
        catch (Exception e) {
            e.printStackTrace();
            ChatUtils.error((String)"Failed to save grid schematic.", (Object[])new Object[0]);
        }
    }

    private class_2487 regionsGrid(class_2248 b1, class_2248 b2, int[] ids, int x, int y, int z) {
        class_2487 r = new class_2487();
        class_2487 size = new class_2487();
        size.method_10569("x", x);
        size.method_10569("y", y);
        size.method_10569("z", z);
        r.method_10566("Size", (class_2520)size);
        class_2487 pos = new class_2487();
        pos.method_10569("x", 0);
        pos.method_10569("y", 0);
        pos.method_10569("z", 0);
        r.method_10566("Position", (class_2520)pos);
        class_2499 palette = new class_2499();
        palette.add((Object)this.state(class_2246.field_10124));
        palette.add((Object)this.state(b1));
        palette.add((Object)this.state(b2));
        r.method_10566("BlockStatePalette", (class_2520)palette);
        r.method_10566("BlockStates", (class_2520)new class_2501(this.pack(ids, 3)));
        r.method_10566("TileEntities", (class_2520)new class_2499());
        r.method_10566("Entities", (class_2520)new class_2499());
        class_2487 regions = new class_2487();
        regions.method_10566("main", (class_2520)r);
        return regions;
    }

    private void runMenger(String mode, int layer, String blockArg) {
        class_2248 single;
        boolean noise = mode.equalsIgnoreCase("noise");
        boolean normal = mode.equalsIgnoreCase("normal");
        if (!noise && !normal) {
            ChatUtils.error((String)"Mode must be normal or noise", (Object[])new Object[0]);
            return;
        }
        ArrayList<class_2248> palette = new ArrayList<class_2248>();
        if (noise) {
            for (String s : blockArg.split(",")) {
                class_2248 b = this.getBlock(s.trim());
                if (b == null) continue;
                palette.add(b);
            }
            if (palette.isEmpty()) {
                ChatUtils.error((String)"No valid blocks in list.", (Object[])new Object[0]);
                return;
            }
        }
        class_2248 class_22482 = single = normal ? this.getBlock(blockArg) : null;
        if (normal && single == null) {
            return;
        }
        int size = 1;
        for (int i = 0; i < layer; ++i) {
            size *= 3;
        }
        int[] ids = new int[size * size * size];
        LinkedHashMap<class_2248, Integer> paletteMap = new LinkedHashMap<class_2248, Integer>();
        paletteMap.put(class_2246.field_10124, 0);
        Random rnd = new Random();
        int i = 0;
        for (int y = 0; y < size; ++y) {
            for (int z = 0; z < size; ++z) {
                for (int x = 0; x < size; ++x) {
                    if (!this.isMengerSolid(x, y, z)) {
                        ids[i++] = 0;
                        continue;
                    }
                    class_2248 b = normal ? single : (class_2248)palette.get(rnd.nextInt(palette.size()));
                    int id = paletteMap.computeIfAbsent(b, k -> paletteMap.size());
                    ids[i++] = id;
                }
            }
        }
        this.saveMenger("mengersponge_" + mode, layer, paletteMap, ids, size);
    }

    private boolean isMengerSolid(int x, int y, int z) {
        while (x > 0 || y > 0 || z > 0) {
            if (x % 3 == 1 && y % 3 == 1 || x % 3 == 1 && z % 3 == 1 || y % 3 == 1 && z % 3 == 1) {
                return false;
            }
            x /= 3;
            y /= 3;
            z /= 3;
        }
        return true;
    }

    private void saveMenger(String type, int layer, Map<class_2248, Integer> paletteMap, int[] ids, int size) {
        try {
            class_2487 root = new class_2487();
            root.method_10569("Version", 6);
            root.method_10569("MinecraftDataVersion", 4189);
            root.method_10566("Metadata", (class_2520)this.meta(type, "mixed", size, size, size));
            class_2487 r = new class_2487();
            class_2487 sizeTag = new class_2487();
            sizeTag.method_10569("x", size);
            sizeTag.method_10569("y", size);
            sizeTag.method_10569("z", size);
            r.method_10566("Size", (class_2520)sizeTag);
            class_2487 pos = new class_2487();
            pos.method_10569("x", 0);
            pos.method_10569("y", 0);
            pos.method_10569("z", 0);
            r.method_10566("Position", (class_2520)pos);
            class_2499 palette = new class_2499();
            for (class_2248 b : paletteMap.keySet()) {
                palette.add((Object)this.state(b));
            }
            r.method_10566("BlockStatePalette", (class_2520)palette);
            r.method_10566("BlockStates", (class_2520)new class_2501(this.pack(ids, paletteMap.size())));
            r.method_10566("TileEntities", (class_2520)new class_2499());
            r.method_10566("Entities", (class_2520)new class_2499());
            class_2487 regions = new class_2487();
            regions.method_10566("main", (class_2520)r);
            root.method_10566("Regions", (class_2520)regions);
            File dir = new File(class_310.method_1551().field_1697, "schematics");
            dir.mkdirs();
            String name = "schematicmaker_" + type + "_l" + layer + ".litematic";
            try (FileOutputStream out = new FileOutputStream(new File(dir, name));){
                class_2507.method_10634((class_2487)root, (OutputStream)out);
            }
            ChatUtils.info((String)("Saved " + name), (Object[])new Object[0]);
        }
        catch (Exception e) {
            e.printStackTrace();
            ChatUtils.error((String)"Failed to save Menger schematic.", (Object[])new Object[0]);
        }
    }

    private void runBall(int r, String blockName, boolean hollow) {
        class_2248 b = this.getBlock(blockName);
        if (b == null) {
            return;
        }
        int size = r * 2 + 1;
        int[] ids = new int[size * size * size];
        int c = r;
        int r2 = r * r;
        int inner2 = (r - 1) * (r - 1);
        int i = 0;
        for (int y = 0; y < size; ++y) {
            for (int z = 0; z < size; ++z) {
                for (int x = 0; x < size; ++x) {
                    int dx = x - c;
                    int dy = y - c;
                    int dz = z - c;
                    int d2 = dx * dx + dy * dy + dz * dz;
                    boolean ok = d2 <= r2 && (!hollow || d2 >= inner2);
                    ids[i++] = ok ? 1 : 0;
                }
            }
        }
        this.save("ball", blockName, size, size, size, b, ids);
    }

    private void runCube(int s, String blockName, boolean hollow) {
        class_2248 b = this.getBlock(blockName);
        if (b == null) {
            return;
        }
        int[] ids = new int[s * s * s];
        int i = 0;
        for (int y = 0; y < s; ++y) {
            for (int z = 0; z < s; ++z) {
                for (int x = 0; x < s; ++x) {
                    boolean shell = !hollow || x == 0 || y == 0 || z == 0 || x == s - 1 || y == s - 1 || z == s - 1;
                    ids[i++] = shell ? 1 : 0;
                }
            }
        }
        this.save("cube", blockName, s, s, s, b, ids);
    }

    private void runFill(String blockName) {
        if (FillCommand.sel1 == null || FillCommand.sel2 == null) {
            ChatUtils.error((String)"sel1 / sel2 not set", (Object[])new Object[0]);
            return;
        }
        class_2248 b = this.getBlock(blockName);
        if (b == null) {
            return;
        }
        class_2338 a = FillCommand.sel1;
        class_2338 c = FillCommand.sel2;
        int sx = Math.abs(a.method_10263() - c.method_10263()) + 1;
        int sy = Math.abs(a.method_10264() - c.method_10264()) + 1;
        int sz = Math.abs(a.method_10260() - c.method_10260()) + 1;
        int[] ids = new int[sx * sy * sz];
        Arrays.fill(ids, 1);
        this.save("fill", blockName, sx, sy, sz, b, ids);
    }

    private void runClear() {
        File dir = new File(class_310.method_1551().field_1697, "schematics");
        int n = 0;
        for (File f : Objects.requireNonNull(dir.listFiles())) {
            if (!f.getName().startsWith("schematicmaker_") || !f.delete()) continue;
            ++n;
        }
        ChatUtils.info((String)("Deleted " + n + " schematics."), (Object[])new Object[0]);
    }

    private void save(String type, String block, int x, int y, int z, class_2248 b, int[] ids) {
        try {
            class_2487 root = new class_2487();
            root.method_10569("Version", 6);
            root.method_10569("MinecraftDataVersion", 4189);
            root.method_10566("Metadata", (class_2520)this.meta(type, block, x, y, z));
            root.method_10566("Regions", (class_2520)this.regions(b, ids, x, y, z));
            File dir = new File(class_310.method_1551().field_1697, "schematics");
            dir.mkdirs();
            String name = "schematicmaker_" + type + "_" + block.replace(':', '_') + "_" + x + "x" + y + "x" + z + ".litematic";
            try (FileOutputStream out = new FileOutputStream(new File(dir, name));){
                class_2507.method_10634((class_2487)root, (OutputStream)out);
            }
            ChatUtils.info((String)("Saved " + name), (Object[])new Object[0]);
        }
        catch (Exception e) {
            e.printStackTrace();
            ChatUtils.error((String)"Failed to save schematic.", (Object[])new Object[0]);
        }
    }

    private class_2487 meta(String type, String block, int x, int y, int z) {
        long t = Instant.now().getEpochSecond();
        class_2487 m = new class_2487();
        m.method_10582("Name", type + " " + block);
        m.method_10582("Author", "Adam");
        m.method_10544("TimeCreated", t);
        m.method_10544("TimeModified", t);
        class_2487 s = new class_2487();
        s.method_10569("x", x);
        s.method_10569("y", y);
        s.method_10569("z", z);
        m.method_10566("EnclosingSize", (class_2520)s);
        m.method_10569("RegionCount", 1);
        return m;
    }

    private class_2487 regions(class_2248 b, int[] ids, int x, int y, int z) {
        class_2487 r = new class_2487();
        class_2487 size = new class_2487();
        size.method_10569("x", x);
        size.method_10569("y", y);
        size.method_10569("z", z);
        r.method_10566("Size", (class_2520)size);
        class_2487 pos = new class_2487();
        pos.method_10569("x", 0);
        pos.method_10569("y", 0);
        pos.method_10569("z", 0);
        r.method_10566("Position", (class_2520)pos);
        class_2499 palette = new class_2499();
        palette.add((Object)this.state(class_2246.field_10124));
        palette.add((Object)this.state(b));
        r.method_10566("BlockStatePalette", (class_2520)palette);
        r.method_10566("BlockStates", (class_2520)new class_2501(this.pack(ids, 2)));
        r.method_10566("TileEntities", (class_2520)new class_2499());
        r.method_10566("Entities", (class_2520)new class_2499());
        class_2487 regions = new class_2487();
        regions.method_10566("main", (class_2520)r);
        return regions;
    }

    private class_2248 getBlock(String name) {
        class_2960 id = class_2960.method_12829((String)name.toLowerCase(Locale.ROOT));
        if (id == null || !class_7923.field_41175.method_10250(id)) {
            ChatUtils.error((String)("Unknown block: " + name), (Object[])new Object[0]);
            return null;
        }
        return (class_2248)class_7923.field_41175.method_63535(id);
    }

    private class_2487 state(class_2248 b) {
        class_2487 n = new class_2487();
        n.method_10582("Name", class_7923.field_41175.method_10221((Object)b).toString());
        return n;
    }

    private long[] pack(int[] ids, int palette) {
        int bits = Math.max(2, 32 - Integer.numberOfLeadingZeros(palette - 1));
        int per = 64 / bits;
        long[] arr = new long[(ids.length + per - 1) / per];
        int i = 0;
        for (int l = 0; l < arr.length; ++l) {
            long v = 0L;
            for (int j = 0; j < per && i < ids.length; ++j, ++i) {
                v |= (long)ids[i] << j * bits;
            }
            arr[l] = v;
        }
        return arr;
    }

    public static void init() {
        Commands.add((Command)new SchematicMakerCommand());
    }
}

