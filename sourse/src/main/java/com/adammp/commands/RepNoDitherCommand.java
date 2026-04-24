package com.adammp.commands;

import com.adammp.commands.FillCommand;
import com.adammp.utils.ImageToSignRenderer;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.List;
import javax.imageio.ImageIO;
import meteordevelopment.meteorclient.commands.Command;
import meteordevelopment.meteorclient.commands.Commands;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_2172;
import net.minecraft.class_2338;
import net.minecraft.class_2596;
import net.minecraft.class_2877;
import net.minecraft.class_310;

@Environment(value=EnvType.CLIENT)
public class RepNoDitherCommand
extends Command {
    private final class_310 mc = class_310.method_1551();

    public RepNoDitherCommand() {
        super("repnodither", "Renders an image onto selected signs without dithering.", new String[0]);
    }

    public void build(LiteralArgumentBuilder<class_2172> builder) {
        builder.then(RepNoDitherCommand.argument((String)"path", (ArgumentType)StringArgumentType.string()).executes(context -> {
            String path = StringArgumentType.getString((CommandContext)context, (String)"path");
            this.execute(path);
            return 1;
        }));
    }

    private void execute(String path) {
        if (FillCommand.sel1 == null || FillCommand.sel2 == null) {
            this.info("Please set selection with .sel1 and .sel2 first.", new Object[0]);
            return;
        }
        try {
            File imgFile = new File(path);
            if (!imgFile.exists()) {
                this.error("File not found: " + imgFile.getAbsolutePath(), new Object[0]);
                return;
            }
            BufferedImage image = ImageIO.read(imgFile);
            int width = Math.abs(FillCommand.sel2.method_10263() - FillCommand.sel1.method_10263()) + 1;
            int height = Math.abs(FillCommand.sel2.method_10264() - FillCommand.sel1.method_10264()) + 1;
            BufferedImage resized = ImageToSignRenderer.resizeImage(image, width * 10, height * 4);
            List<String[]> signData = ImageToSignRenderer.imageToSignTextNoDither(resized);
            this.placeSigns(signData);
            this.info("\u2705 Rendered " + imgFile.getName() + " without dithering!", new Object[0]);
        }
        catch (Exception e) {
            this.error("Error: " + e.getMessage(), new Object[0]);
        }
    }

    private void placeSigns(List<String[]> signs) {
        int minX = Math.min(FillCommand.sel1.method_10263(), FillCommand.sel2.method_10263());
        int minY = Math.min(FillCommand.sel1.method_10264(), FillCommand.sel2.method_10264());
        int minZ = Math.min(FillCommand.sel1.method_10260(), FillCommand.sel2.method_10260());
        int maxX = Math.max(FillCommand.sel1.method_10263(), FillCommand.sel2.method_10263());
        int maxY = Math.max(FillCommand.sel1.method_10264(), FillCommand.sel2.method_10264());
        int maxZ = Math.max(FillCommand.sel1.method_10260(), FillCommand.sel2.method_10260());
        boolean flip = FillCommand.sel1.method_10263() > FillCommand.sel2.method_10263();
        int signIndex = 0;
        for (int y = maxY; y <= minY; --y) {
            int x;
            int n = x = flip ? maxX : minX;
            while (flip ? x >= minX : x <= maxX) {
                for (int z = minZ; z <= maxZ; ++z) {
                    if (signIndex >= signs.size()) {
                        return;
                    }
                    class_2338 pos = new class_2338(x, y, z);
                    String[] lines = signs.get(signIndex);
                    if (this.mc.method_1562() != null) {
                        this.mc.method_1562().method_52787((class_2596)new class_2877(pos, true, lines[0], lines[1], lines[2], lines[3]));
                    }
                    ++signIndex;
                }
                x += flip ? -1 : 1;
            }
        }
    }

    public static void init() {
        Commands.add((Command)new RepNoDitherCommand());
    }
}

