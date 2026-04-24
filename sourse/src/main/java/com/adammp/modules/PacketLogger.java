package com.adammp.modules;

import com.adammp.AdamsMPmodClient;
import java.util.Set;
import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.EnumSetting;
import meteordevelopment.meteorclient.settings.PacketListSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.network.PacketUtils;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import meteordevelopment.orbit.EventHandler;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_124;
import net.minecraft.class_2561;
import net.minecraft.class_2596;

@Environment(value=EnvType.CLIENT)
public class PacketLogger
extends Module {
    private final SettingGroup sgGeneral;
    private final Setting<Boolean> showC2S;
    private final Setting<Boolean> showS2C;
    private final Setting<Boolean> showPacketValue;
    private final Setting<FilterMode> filterMode;
    private final Setting<Set<Class<? extends class_2596<?>>>> c2sPackets;
    private final Setting<Set<Class<? extends class_2596<?>>>> s2cPackets;

    public PacketLogger() {
        super(AdamsMPmodClient.CATEGORY, "packet-logger", "Logs C2S and S2C packets with whitelist/blacklist filtering. You absolutely dont need this unless ur a developer");
        this.sgGeneral = this.settings.getDefaultGroup();
        this.showC2S = this.sgGeneral.add((Setting)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("show-c2s")).description("Show client \u2192 server packets.")).defaultValue((Object)true)).build());
        this.showS2C = this.sgGeneral.add((Setting)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("show-s2c")).description("Show server \u2192 client packets.")).defaultValue((Object)true)).build());
        this.showPacketValue = this.sgGeneral.add((Setting)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("show-packet-value")).description("Show packet toString() value.")).defaultValue((Object)false)).build());
        this.filterMode = this.sgGeneral.add((Setting)((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)new EnumSetting.Builder().name("filter-mode")).description("Whitelist or blacklist packet lists.")).defaultValue((Object)FilterMode.Whitelist)).build());
        this.c2sPackets = this.sgGeneral.add((Setting)((PacketListSetting.Builder)((PacketListSetting.Builder)new PacketListSetting.Builder().name("c2s-packets")).description("Client \u2192 Server packets.")).filter(PacketUtils.getC2SPackets()::contains).build());
        this.s2cPackets = this.sgGeneral.add((Setting)((PacketListSetting.Builder)((PacketListSetting.Builder)new PacketListSetting.Builder().name("s2c-packets")).description("Server \u2192 Client packets.")).filter(PacketUtils.getS2CPackets()::contains).build());
        this.runInMainMenu = true;
    }

    private boolean shouldLog(Class<? extends class_2596<?>> packetClass, boolean c2s) {
        Set list = c2s ? (Set)this.c2sPackets.get() : (Set)this.s2cPackets.get();
        boolean contains = list.contains(packetClass);
        return this.filterMode.get() == FilterMode.Whitelist ? contains : !contains;
    }

    private class_2561 buildPacketText(Class<? extends class_2596<?>> packetClass, class_2596<?> packet) {
        String value;
        String name = PacketUtils.getName(packetClass);
        if (!((Boolean)this.showPacketValue.get()).booleanValue()) {
            return class_2561.method_43470((String)name).method_27694(s -> s.method_10977(class_124.field_1068));
        }
        try {
            value = packet.toString();
        }
        catch (Throwable t) {
            value = "<toString failed>";
        }
        return class_2561.method_43470((String)(name + " ")).method_27694(s -> s.method_10977(class_124.field_1068)).method_10852((class_2561)class_2561.method_43470((String)value).method_27694(s -> s.method_10977(class_124.field_1080)));
    }

    @EventHandler
    private void onReceive(PacketEvent.Receive event) {
        if (!((Boolean)this.showS2C.get()).booleanValue()) {
            return;
        }
        Class packetClass = event.packet.getClass();
        if (!this.shouldLog(packetClass, false)) {
            return;
        }
        ChatUtils.sendMsg((class_2561)class_2561.method_43470((String)"[S2C] ").method_27694(s -> s.method_10977(class_124.field_1060)).method_10852(this.buildPacketText(packetClass, event.packet)));
    }

    @EventHandler
    private void onSend(PacketEvent.Send event) {
        if (!((Boolean)this.showC2S.get()).booleanValue()) {
            return;
        }
        Class packetClass = event.packet.getClass();
        if (!this.shouldLog(packetClass, true)) {
            return;
        }
        ChatUtils.sendMsg((class_2561)class_2561.method_43470((String)"[C2S] ").method_27694(s -> s.method_10977(class_124.field_1061)).method_10852(this.buildPacketText(packetClass, event.packet)));
    }

    @Environment(value=EnvType.CLIENT)
    public static enum FilterMode {
        Whitelist,
        Blacklist;

    }
}

