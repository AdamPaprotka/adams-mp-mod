package com.adammp.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import meteordevelopment.meteorclient.commands.Command;
import meteordevelopment.meteorclient.commands.Commands;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_2172;
import net.minecraft.class_2561;

@Environment(value=EnvType.CLIENT)
public class TestCommandMadeByMeYay
extends Command {
    private class_2561 ToTextLiteral(String str) {
        return class_2561.method_43470((String)str);
    }

    public TestCommandMadeByMeYay() {
        super("test", "Test By Me!", new String[0]);
    }

    public void build(LiteralArgumentBuilder<class_2172> builder) {
        builder.executes(ctx -> {
            ChatUtils.sendMsg((class_2561)class_2561.method_43470((String)"Hello world!"));
            ChatUtils.sendMsg((class_2561)this.ToTextLiteral("Desc: " + this.getDescription()));
            ChatUtils.sendMsg((class_2561)this.ToTextLiteral(""));
            return 0;
        });
    }

    public static void init() {
        Commands.add((Command)new TestCommandMadeByMeYay());
    }
}

