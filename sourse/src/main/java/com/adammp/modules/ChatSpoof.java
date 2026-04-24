package com.adammp.modules;

import com.adammp.AdamsMPmodClient;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import meteordevelopment.meteorclient.events.game.SendMessageEvent;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.settings.StringSetting;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import meteordevelopment.orbit.EventHandler;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(value=EnvType.CLIENT)
public class ChatSpoof
extends Module {
    private final SettingGroup sgGeneral;
    private final Setting<String> format;
    private static final Map<String, String> COLOR_MAP = new HashMap<String, String>();

    public ChatSpoof() {
        super(AdamsMPmodClient.CATEGORY, "chat-spoof", "[OP] Full custom tellraw prefix. No default color.");
        this.sgGeneral = this.settings.getDefaultGroup();
        this.format = this.sgGeneral.add((Setting)((StringSetting.Builder)((StringSetting.Builder)((StringSetting.Builder)new StringSetting.Builder().name("format")).description("Tellraw prefix format.\nSupports:\n<red>, <blue>, <#ff8800>\n<playername>\n<reset>\nUse \\< for literal '<'\n\nExample:\n<blue>[idiot haha]<red><playername><reset>:\n")).defaultValue((Object)"<white><playername>:")).build());
    }

    @EventHandler
    private void onSendChat(SendMessageEvent event) {
        if (this.mc == null || this.mc.field_1724 == null) {
            return;
        }
        if (event.message.startsWith("/")) {
            return;
        }
        if (!this.mc.field_1724.method_64475(2)) {
            return;
        }
        event.cancel();
        String msg = event.message;
        String prefixJson = this.buildJson((String)this.format.get());
        String messageJson = this.textPart(" " + msg, null);
        ChatUtils.sendPlayerMsg((String)("/tellraw @a [" + prefixJson + "," + messageJson + "]"));
    }

    private String buildJson(String raw) {
        ArrayList<String> parts = new ArrayList<String>();
        String currentColor = null;
        StringBuilder buf = new StringBuilder();
        int i = 0;
        while (i < raw.length()) {
            char c = raw.charAt(i);
            if (c == '\\' && i + 1 < raw.length() && raw.charAt(i + 1) == '<') {
                buf.append('<');
                i += 2;
                continue;
            }
            if (c == '<') {
                String tag;
                int end = raw.indexOf(62, i + 1);
                if (end == -1) {
                    buf.append('<');
                    ++i;
                    continue;
                }
                if (buf.length() > 0) {
                    parts.add(this.textPart(buf.toString(), currentColor));
                    buf.setLength(0);
                }
                if ((tag = raw.substring(i + 1, end).trim().toLowerCase(Locale.ROOT)).equals("reset")) {
                    currentColor = null;
                } else if (tag.equals("playername")) {
                    parts.add(this.textPart(this.mc.field_1724.method_7334().getName(), currentColor));
                } else if (this.isHex(tag)) {
                    currentColor = tag;
                } else if (COLOR_MAP.containsKey(tag)) {
                    currentColor = COLOR_MAP.get(tag);
                } else {
                    parts.add(this.textPart("<" + tag + ">", currentColor));
                }
                i = end + 1;
                continue;
            }
            buf.append(c);
            ++i;
        }
        if (buf.length() > 0) {
            parts.add(this.textPart(buf.toString(), currentColor));
        }
        return String.join((CharSequence)",", parts);
    }

    private boolean isHex(String s) {
        if (s.length() != 7 || s.charAt(0) != '#') {
            return false;
        }
        for (int i = 1; i < 7; ++i) {
            char c = s.charAt(i);
            if (c >= '0' && c <= '9' || c >= 'a' && c <= 'f') continue;
            return false;
        }
        return true;
    }

    private String textPart(String text, String color) {
        if (color == null) {
            return "{\"text\":\"" + this.escape(text) + "\"}";
        }
        return "{\"text\":\"" + this.escape(text) + "\",\"color\":\"" + color + "\"}";
    }

    private String escape(String s) {
        return s.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "\\r").replace("\t", "\\t");
    }

    static {
        for (ChatColor c : ChatColor.values()) {
            COLOR_MAP.put(c.name().toLowerCase(Locale.ROOT), c.mc);
            COLOR_MAP.put(c.mc.toLowerCase(Locale.ROOT), c.mc);
            COLOR_MAP.put(c.mc.replace("_", "").toLowerCase(Locale.ROOT), c.mc);
        }
    }

    @Environment(value=EnvType.CLIENT)
    public static enum ChatColor {
        BLACK("black"),
        DARK_BLUE("dark_blue"),
        DARK_GREEN("dark_green"),
        DARK_AQUA("dark_aqua"),
        DARK_RED("dark_red"),
        DARK_PURPLE("dark_purple"),
        GOLD("gold"),
        GRAY("gray"),
        DARK_GRAY("dark_gray"),
        BLUE("blue"),
        GREEN("green"),
        AQUA("aqua"),
        RED("red"),
        LIGHT_PURPLE("light_purple"),
        YELLOW("yellow"),
        WHITE("white");

        public final String mc;

        private ChatColor(String mc) {
            this.mc = mc;
        }
    }
}

