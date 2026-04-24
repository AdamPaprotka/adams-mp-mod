package com.adammp.modules;

import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.systems.modules.Category;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_2596;
import net.minecraft.class_2846;

@Environment(value=EnvType.CLIENT)
public class NoBreak
extends Module {
    public NoBreak(Category category) {
        super(category, "no-break", "Cancels all block break packets. (Great when ur building.)");
    }

    @EventHandler
    private void onSendPacket(PacketEvent.Send event) {
        class_2596 class_25962 = event.packet;
        if (class_25962 instanceof class_2846) {
            class_2846 packet = (class_2846)class_25962;
            switch (packet.method_12363()) {
                case field_12968: 
                case field_12973: 
                case field_12971: {
                    event.cancel();
                    break;
                }
            }
        }
    }
}

