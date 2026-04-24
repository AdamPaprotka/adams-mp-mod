package com.adammp;

import com.adammp.commands.BadApple;
import com.adammp.commands.DVDCommand;
import com.adammp.commands.DesmosVirtualCommand;
import com.adammp.commands.ExcavateCommand;
import com.adammp.commands.FillCommand;
import com.adammp.commands.GetFaceSchematicCommand;
import com.adammp.commands.GreifCommand;
import com.adammp.commands.LetterSign;
import com.adammp.commands.RepImageCommand;
import com.adammp.commands.RepNoDitherCommand;
import com.adammp.commands.SchematicMakerCommand;
import com.adammp.commands.SecureCommand;
import com.adammp.commands.SignScreenCommand;
import com.adammp.commands.TestCommandMadeByMeYay;
import com.adammp.commands.TrapCommand;
import com.adammp.commands.addpos;
import com.adammp.commands.crashrobloxcommand;
import com.adammp.modules.APrinter;
import com.adammp.modules.AntiSchematicGrief;
import com.adammp.modules.AntiSpamBlock;
import com.adammp.modules.Bounc;
import com.adammp.modules.ChatSpoof;
import com.adammp.modules.ChatUtilsModule;
import com.adammp.modules.LitematicaPrinterHelper;
import com.adammp.modules.LookAt;
import com.adammp.modules.MathSurface;
import com.adammp.modules.Minesweeper;
import com.adammp.modules.NoBreak;
import com.adammp.modules.NoNoclipOverlay;
import com.adammp.modules.Noclip;
import com.adammp.modules.PacketLogger;
import com.adammp.modules.Scaffoldnt;
import com.adammp.modules.SillyJumpSpin;
import com.adammp.modules.SlippyPlus;
import meteordevelopment.meteorclient.addons.MeteorAddon;
import meteordevelopment.meteorclient.systems.modules.Category;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.Modules;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_1799;
import net.minecraft.class_1802;
import net.minecraft.class_1935;

@Environment(value=EnvType.CLIENT)
public class AdamsMPmodClient
extends MeteorAddon {
    public static final Category CATEGORY = new Category("Adams MP mods", new class_1799((class_1935)class_1802.field_8866));

    public void onInitialize() {
        Modules.get().add((Module)new LitematicaPrinterHelper());
        Modules.get().add((Module)new AntiSpamBlock());
        Modules.get().add((Module)new AntiSchematicGrief());
        Modules.get().add((Module)new ChatUtilsModule());
        Modules.get().add((Module)new NoBreak(CATEGORY));
        Modules.get().add((Module)new Scaffoldnt());
        Modules.get().add((Module)new APrinter());
        Modules.get().add((Module)new Minesweeper());
        Modules.get().add((Module)new SlippyPlus());
        Modules.get().add((Module)new ChatSpoof());
        Modules.get().add((Module)new MathSurface());
        Modules.get().add((Module)new Bounc());
        Modules.get().add((Module)new SillyJumpSpin());
        Modules.get().add((Module)new LookAt());
        Modules.get().add((Module)new Noclip());
        Modules.get().add((Module)new PacketLogger());
        Modules.get().add((Module)new NoNoclipOverlay());
        FillCommand.init();
        GetFaceSchematicCommand.init();
        GreifCommand.init();
        ExcavateCommand.init();
        RepImageCommand.init();
        RepNoDitherCommand.init();
        DVDCommand.init();
        TrapCommand.init();
        TestCommandMadeByMeYay.init();
        LetterSign.init();
        crashrobloxcommand.init();
        addpos.init();
        SchematicMakerCommand.init();
        BadApple.init();
        SignScreenCommand.init();
        SecureCommand.init();
        DesmosVirtualCommand.init();
    }

    public String getPackage() {
        return "com.adammp";
    }

    public void onRegisterCategories() {
        Modules.registerCategory((Category)CATEGORY);
    }
}

