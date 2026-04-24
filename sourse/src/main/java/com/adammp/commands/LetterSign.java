package com.adammp.commands;

import com.adammp.utils.BigLetters;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import java.util.List;
import meteordevelopment.meteorclient.commands.Command;
import meteordevelopment.meteorclient.commands.Commands;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_2172;
import net.minecraft.class_2338;
import net.minecraft.class_2586;
import net.minecraft.class_2596;
import net.minecraft.class_2625;
import net.minecraft.class_2877;

@Environment(value=EnvType.CLIENT)
public class LetterSign
extends Command {
    public LetterSign() {
        super("lettersign", "Uses /'s and \\'s to make letters", new String[0]);
    }

    public void build(LiteralArgumentBuilder<class_2172> builder) {
        builder.then(LetterSign.argument((String)"letter", (ArgumentType)StringArgumentType.string()).executes(ctx -> {
            String arg_str = StringArgumentType.getString((CommandContext)ctx, (String)"letter");
            assert (LetterSign.mc.field_1724 != null);
            class_2338 PositionPlayer = LetterSign.mc.field_1724.method_24515();
            if (arg_str.length() == 1) {
                assert (LetterSign.mc.field_1687 != null);
                class_2586 blok = LetterSign.mc.field_1687.method_8321(PositionPlayer);
                BigLetters big = new BigLetters();
                List<String> aaa = big.ReturnLetter(arg_str);
                if (blok instanceof class_2625) {
                    class_2625 sign = (class_2625)blok;
                    LetterSign.mc.field_1724.field_3944.method_52787((class_2596)new class_2877(PositionPlayer, true, aaa.get(0), aaa.get(1), aaa.get(2), aaa.get(3)));
                }
            }
            return 0;
        }));
    }

    public static void init() {
        Commands.add((Command)new LetterSign());
    }
}

