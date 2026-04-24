package com.adammp.commands;

import com.adammp.commands.FillCommand;
import com.adammp.utils.ImageToSignRenderer;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;
import javax.imageio.ImageIO;
import meteordevelopment.meteorclient.commands.Command;
import meteordevelopment.meteorclient.commands.Commands;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_2172;
import net.minecraft.class_2338;
import net.minecraft.class_2596;
import net.minecraft.class_2625;
import net.minecraft.class_2877;
import net.minecraft.class_310;

@Environment(value=EnvType.CLIENT)
public class RepImageCommand
extends Command {
    private final class_310 mc = class_310.method_1551();

    public RepImageCommand() {
        super("rep", "Render image onto selected signs.", new String[0]);
    }

    public void build(LiteralArgumentBuilder<class_2172> builder) {
        builder.then(RepImageCommand.argument((String)"imgPath", (ArgumentType)StringArgumentType.string()).executes(ctx -> {
            BufferedImage img;
            if (FillCommand.sel1 == null || FillCommand.sel2 == null) {
                ChatUtils.error((String)"Selection not set! Use .sel1 and .sel2.", (Object[])new Object[0]);
                return 1;
            }
            String imgPath = StringArgumentType.getString((CommandContext)ctx, (String)"imgPath");
            File file = new File(imgPath);
            if (!file.exists()) {
                ChatUtils.error((String)("File not found: " + imgPath), (Object[])new Object[0]);
                return 1;
            }
            try {
                img = ImageIO.read(file);
            }
            catch (IOException e) {
                ChatUtils.error((String)"Failed to read image file.", (Object[])new Object[0]);
                return 1;
            }
            class_2338 min = new class_2338(Math.min(FillCommand.sel1.method_10263(), FillCommand.sel2.method_10263()), Math.min(FillCommand.sel1.method_10264(), FillCommand.sel2.method_10264()), Math.min(FillCommand.sel1.method_10260(), FillCommand.sel2.method_10260()));
            class_2338 max = new class_2338(Math.max(FillCommand.sel1.method_10263(), FillCommand.sel2.method_10263()), Math.max(FillCommand.sel1.method_10264(), FillCommand.sel2.method_10264()), Math.max(FillCommand.sel1.method_10260(), FillCommand.sel2.method_10260()));
            int signCols = max.method_10263() - min.method_10263() + 1;
            int signRows = max.method_10264() - min.method_10264() + 1;
            BufferedImage resized = ImageToSignRenderer.resizeImage(img, signCols * 10, signRows * 4);
            List<String[]> signTexts = ImageToSignRenderer.imageToSignTextBayer(resized);
            int i = 0;
            block2: for (int y = max.method_10264(); y >= min.method_10264(); --y) {
                for (int x = min.method_10263(); x <= max.method_10263(); ++x) {
                    class_2338 pos = new class_2338(x, y, min.method_10260());
                    if (!(this.mc.field_1687.method_8321(pos) instanceof class_2625)) continue;
                    if (i >= signTexts.size()) continue block2;
                    String[] lines = signTexts.get(i++);
                    this.mc.method_1562().method_52787((class_2596)new class_2877(pos, true, lines[0], lines[1], lines[2], lines[3]));
                }
            }
            ChatUtils.info((String)("\u2705 Rendered " + i + " signs."), (Object[])new Object[0]);
            return 1;
        }));
    }

    public static void init() {
        Commands.add((Command)new RepImageCommand());
    }
}

