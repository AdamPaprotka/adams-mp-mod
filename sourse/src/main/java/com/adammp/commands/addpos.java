package com.adammp.commands;

import com.adammp.Storage.Storage;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import meteordevelopment.meteorclient.commands.Command;
import meteordevelopment.meteorclient.commands.Commands;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_2172;
import net.minecraft.class_2561;

@Environment(value=EnvType.CLIENT)
public class addpos
extends Command {
    public addpos() {
        super("addpos", "Adds position. (uses 1.0.9 Storage.Java)", new String[0]);
    }

    public void build(LiteralArgumentBuilder<class_2172> builder) {
        builder.executes(ctx -> {
            assert (addpos.mc.field_1724 != null);
            Storage.savedPositions.add(addpos.mc.field_1724.method_24515());
            ChatUtils.sendMsg((class_2561)class_2561.method_43470((String)("Saved " + addpos.mc.field_1724.method_24515().method_23854())));
            ChatUtils.sendMsg((class_2561)class_2561.method_43470((String)Storage.savedPositions.toString().replace("BlockPos", "").replace("{", "[").replace("}", "]")));
            return 0;
        });
    }

    public static void init() {
        Commands.add((Command)new addpos());
        Commands.add((Command)new clearpos());
    }

    @Environment(value=EnvType.CLIENT)
    static class clearpos
    extends Command {
        public clearpos() {
            super("clearpos", "Clears the position array (Uses 1.0.9 Storage.java)", new String[0]);
        }

        public void build(LiteralArgumentBuilder<class_2172> builder) {
            builder.executes(ctx -> {
                Storage.savedPositions.clear();
                return 0;
            });
        }
    }
}

