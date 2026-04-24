package com.adammp.modules;

import com.adammp.AdamsMPmodClient;
import meteordevelopment.meteorclient.systems.modules.Module;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(value=EnvType.CLIENT)
public class NoNoclipOverlay
extends Module {
    public NoNoclipOverlay() {
        super(AdamsMPmodClient.CATEGORY, "no-noclip-overlay", "Removes the inside-block (suffocation) overlay without enabling noclip.");
    }
}

