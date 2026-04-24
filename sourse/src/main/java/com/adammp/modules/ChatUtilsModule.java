package com.adammp.modules;

import com.adammp.AdamsMPmodClient;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import meteordevelopment.meteorclient.events.game.SendMessageEvent;
import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.EnumSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_2561;
import net.minecraft.class_2797;
import net.minecraft.class_7439;

@Environment(value=EnvType.CLIENT)
public class ChatUtilsModule
extends Module {
    private final SettingGroup sgGeneral;
    private final SettingGroup sgFonts;
    private final Setting<Boolean> noCmdOutput;
    private final Setting<Boolean> msgCanceller;
    private final Setting<Boolean> fontUtils;
    private final Setting<FontStyle> fontStyle;
    private final Setting<Boolean> affectCommands;
    private final Setting<Boolean> preserveCase;

    public ChatUtilsModule() {
        super(AdamsMPmodClient.CATEGORY, "chat-utils", "Hide command feedback, cancel outgoing chat, and auto-style your messages with many fonts.");
        this.sgGeneral = this.settings.getDefaultGroup();
        this.sgFonts = this.settings.createGroup("Font Utils");
        this.noCmdOutput = this.sgGeneral.add((Setting)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("no-command-output")).description("Hides system/command feedback in chat.")).defaultValue((Object)false)).build());
        this.msgCanceller = this.sgGeneral.add((Setting)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("msg-canceller")).description("Blocks all outgoing chat messages (ChatMessageC2SPacket). Commands still work.")).defaultValue((Object)false)).build());
        this.fontUtils = this.sgFonts.add((Setting)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("enable-font-utils")).description("Transforms your outgoing message using a fancy font/style.")).defaultValue((Object)false)).build());
        this.fontStyle = this.sgFonts.add((Setting)((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)new EnumSetting.Builder().name("font-style")).description("Pick the font/style to apply.")).defaultValue((Object)FontStyle.BOLD)).visible(() -> this.fontUtils.get())).build());
        this.affectCommands = this.sgFonts.add((Setting)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("affect-commands")).description("Also transform messages that start with '/'.")).defaultValue((Object)false)).visible(() -> this.fontUtils.get())).build());
        this.preserveCase = this.sgFonts.add((Setting)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("preserve-case")).description("Try to preserve upper/lower case where a style lacks lowercase glyphs.")).defaultValue((Object)true)).visible(() -> this.fontUtils.get())).build());
    }

    @EventHandler
    private void onPacketReceive(PacketEvent.Receive e) {
        if (!((Boolean)this.noCmdOutput.get()).booleanValue()) {
            return;
        }
        class_2561 t = this.tryExtractText(e.packet);
        if (t == null) {
            return;
        }
        String m = t.getString().toLowerCase(Locale.ROOT);
        if (m.startsWith("/") || m.contains("unknown command") || m.contains("usage: /") || m.contains("teleported") || m.contains("set own game mode") || m.contains("gave") || m.contains("summoned") || m.contains("filled") || m.contains("replaced") || m.contains("changed to") || m.contains("added") || m.contains("removed")) {
            e.cancel();
        }
    }

    private class_2561 tryExtractText(Object pkt) {
        Class<?> c2;
        if (pkt instanceof class_7439) {
            class_7439 gm = (class_7439)pkt;
            return gm.comp_763();
        }
        try {
            c2 = Class.forName("net.minecraft.network.packet.s2c.play.SystemMessageS2CPacket");
            if (c2.isInstance(pkt)) {
                return (class_2561)c2.getMethod("content", new Class[0]).invoke(pkt, new Object[0]);
            }
        }
        catch (Throwable c2) {
            // empty catch block
        }
        try {
            c2 = Class.forName("net.minecraft.network.packet.s2c.play.OverlayMessageS2CPacket");
            if (c2.isInstance(pkt)) {
                return (class_2561)c2.getMethod("content", new Class[0]).invoke(pkt, new Object[0]);
            }
        }
        catch (Throwable throwable) {
            // empty catch block
        }
        return null;
    }

    @EventHandler
    private void onPacketSend(PacketEvent.Send e) {
        if (((Boolean)this.msgCanceller.get()).booleanValue() && this.isChatMessageC2S(e.packet)) {
            e.cancel();
        }
    }

    private boolean isChatMessageC2S(Object pkt) {
        if (pkt instanceof class_2797) {
            return true;
        }
        try {
            Class<?> c = Class.forName("net.minecraft.network.packet.c2s.play.ChatMessageC2SPacket");
            return c.isInstance(pkt);
        }
        catch (Throwable throwable) {
            return false;
        }
    }

    @EventHandler
    private void onSendMessage(SendMessageEvent e) {
        if (!((Boolean)this.fontUtils.get()).booleanValue()) {
            return;
        }
        if (!((Boolean)this.affectCommands.get()).booleanValue() && e.message.startsWith("/")) {
            return;
        }
        e.message = FancyFonts.transform(e.message, (FontStyle)((Object)this.fontStyle.get()), (Boolean)this.preserveCase.get());
    }

    @Environment(value=EnvType.CLIENT)
    public static enum FontStyle {
        BOLD,
        ITALIC,
        BOLD_ITALIC,
        SCRIPT,
        BOLD_SCRIPT,
        FRAKTUR,
        BOLD_FRAKTUR,
        DOUBLE_STRUCK,
        SANS,
        SANS_BOLD,
        SANS_ITALIC,
        SANS_BOLD_ITALIC,
        MONOSPACE,
        FULLWIDTH,
        CIRCLED,
        NEGATIVE_CIRCLED,
        PARENTHESIZED,
        UNDERLINE,
        STRIKETHROUGH,
        OVERLINE,
        AESTHETIC,
        UPSIDE_DOWN;

    }

    @Environment(value=EnvType.CLIENT)
    private static final class FancyFonts {
        private static final String COMB_UNDER = "\u0332";
        private static final String COMB_STRIKE = "\u0336";
        private static final String COMB_OVER = "\u0305";
        private static final Map<Character, String> UPSIDE_DOWN_MAP = FancyFonts.makeUpsideDown();
        private static final Map<Character, String> SCRIPT_OVERRIDES = new HashMap<Character, String>();
        private static final Map<Character, String> DSTRK_OVERRIDES = new HashMap<Character, String>();

        private FancyFonts() {
        }

        public static String transform(String s, FontStyle style, boolean preserveCase) {
            return switch (style.ordinal()) {
                default -> throw new IncompatibleClassChangeError();
                case 0 -> FancyFonts.mapByOffsets(s, 119808, 119834, 120782, true, null);
                case 1 -> FancyFonts.mapByOffsets(s, 119860, 119886, -1, false, null);
                case 2 -> FancyFonts.mapByOffsets(s, 119912, 119938, -1, false, null);
                case 3 -> FancyFonts.mapByOffsets(s, 119964, 119990, -1, false, SCRIPT_OVERRIDES);
                case 4 -> FancyFonts.mapByOffsets(s, 120016, 120042, -1, false, null);
                case 5 -> FancyFonts.mapByOffsets(s, 120068, 120094, -1, false, null);
                case 6 -> FancyFonts.mapByOffsets(s, 120172, 120198, -1, false, null);
                case 7 -> FancyFonts.mapByOffsets(s, 120120, 120146, 120792, true, DSTRK_OVERRIDES);
                case 8 -> FancyFonts.mapByOffsets(s, 120224, 120250, 120802, true, null);
                case 9 -> FancyFonts.mapByOffsets(s, 120276, 120302, 120812, true, null);
                case 10 -> FancyFonts.mapByOffsets(s, 120328, 120354, -1, false, null);
                case 11 -> FancyFonts.mapByOffsets(s, 120380, 120406, -1, false, null);
                case 12 -> FancyFonts.mapByOffsets(s, 120432, 120458, 120822, true, null);
                case 13 -> FancyFonts.mapFullwidth(s);
                case 14 -> FancyFonts.mapCircled(s);
                case 15 -> FancyFonts.mapNegativeCircled(s, preserveCase);
                case 16 -> FancyFonts.mapParenthesized(s, preserveCase);
                case 17 -> FancyFonts.addCombiningToEach(s, COMB_UNDER);
                case 18 -> FancyFonts.addCombiningToEach(s, COMB_STRIKE);
                case 19 -> FancyFonts.addCombiningToEach(s, COMB_OVER);
                case 20 -> FancyFonts.mapAesthetic(s);
                case 21 -> FancyFonts.mapUpsideDown(s);
            };
        }

        private static String mapByOffsets(String s, int upperBase, int lowerBase, int digitBase, boolean hasDigits, Map<Character, String> overrides) {
            StringBuilder out = new StringBuilder(s.length() * 2);
            for (int i = 0; i < s.length(); ++i) {
                char c = s.charAt(i);
                if (overrides != null && overrides.containsKey(Character.valueOf(c))) {
                    out.append(overrides.get(Character.valueOf(c)));
                    continue;
                }
                if (c >= 'A' && c <= 'Z') {
                    out.append(Character.toChars(upperBase + (c - 65)));
                    continue;
                }
                if (c >= 'a' && c <= 'z') {
                    out.append(Character.toChars(lowerBase + (c - 97)));
                    continue;
                }
                if (hasDigits && c >= '0' && c <= '9') {
                    out.append(Character.toChars(digitBase + (c - 48)));
                    continue;
                }
                out.append(c);
            }
            return out.toString();
        }

        private static String mapFullwidth(String s) {
            StringBuilder out = new StringBuilder(s.length() * 2);
            for (int i = 0; i < s.length(); ++i) {
                char c = s.charAt(i);
                if (c >= 'A' && c <= 'Z') {
                    out.append(Character.toChars(65313 + (c - 65)));
                    continue;
                }
                if (c >= 'a' && c <= 'z') {
                    out.append(Character.toChars(65345 + (c - 97)));
                    continue;
                }
                if (c >= '0' && c <= '9') {
                    out.append(Character.toChars(65296 + (c - 48)));
                    continue;
                }
                if (c == ' ') {
                    out.append('\u3000');
                    continue;
                }
                out.append(c);
            }
            return out.toString();
        }

        private static String mapCircled(String s) {
            StringBuilder out = new StringBuilder(s.length() * 2);
            for (char c : s.toCharArray()) {
                if (c >= 'A' && c <= 'Z') {
                    out.append(Character.toChars(9398 + (c - 65)));
                    continue;
                }
                if (c >= 'a' && c <= 'z') {
                    out.append(Character.toChars(9424 + (c - 97)));
                    continue;
                }
                if (c >= '0' && c <= '9') {
                    if (c == '0') {
                        out.append('\u24ea');
                        continue;
                    }
                    out.append(Character.toChars(9312 + (c - 49)));
                    continue;
                }
                out.append(c);
            }
            return out.toString();
        }

        private static String mapNegativeCircled(String s, boolean preserveCase) {
            StringBuilder out = new StringBuilder(s.length() * 2);
            for (char c : s.toCharArray()) {
                char up = Character.toUpperCase(c);
                if (up >= 'A' && up <= 'Z') {
                    out.append(Character.toChars(127312 + (up - 65)));
                    continue;
                }
                if (Character.isLowerCase(c) && preserveCase) {
                    out.append(c);
                    continue;
                }
                out.append(c);
            }
            return out.toString();
        }

        private static String mapParenthesized(String s, boolean preserveCase) {
            StringBuilder out = new StringBuilder(s.length() * 2);
            for (char c : s.toCharArray()) {
                if (c >= 'A' && c <= 'Z') {
                    out.append(Character.toChars(127248 + (c - 65)));
                    continue;
                }
                if (c >= 'a' && c <= 'z') {
                    out.append(Character.toChars(9392 + (c - 97)));
                    continue;
                }
                if (c >= '1' && c <= '9') {
                    out.append(Character.toChars(9332 + (c - 49)));
                    continue;
                }
                if (c == '0' && preserveCase) {
                    out.append(c);
                    continue;
                }
                out.append(c);
            }
            return out.toString();
        }

        private static String addCombiningToEach(String s, String mark) {
            StringBuilder out = new StringBuilder(s.length() * 3);
            for (int i = 0; i < s.length(); ++i) {
                char c = s.charAt(i);
                if (Character.isWhitespace(c)) {
                    out.append(c);
                    continue;
                }
                out.append(c).append(mark);
            }
            return out.toString();
        }

        private static String mapAesthetic(String s) {
            String full = FancyFonts.mapFullwidth(s);
            StringBuilder out = new StringBuilder(full.length() * 2);
            for (int i = 0; i < full.length(); ++i) {
                char c = full.charAt(i);
                out.append(c);
                if (Character.isWhitespace(c) || i >= full.length() - 1) continue;
                out.append(' ');
            }
            return out.toString();
        }

        private static String mapUpsideDown(String s) {
            StringBuilder rev = new StringBuilder(s).reverse();
            StringBuilder out = new StringBuilder(rev.length() * 2);
            for (int i = 0; i < rev.length(); ++i) {
                char c = rev.charAt(i);
                out.append(UPSIDE_DOWN_MAP.getOrDefault(Character.valueOf(c), String.valueOf(c)));
            }
            return out.toString();
        }

        private static Map<Character, String> makeUpsideDown() {
            String[][] pairs;
            HashMap<Character, String> m = new HashMap<Character, String>();
            for (String[] p : pairs = new String[][]{{"a", "\u0250"}, {"b", "q"}, {"c", "\u0254"}, {"d", "p"}, {"e", "\u01dd"}, {"f", "\u025f"}, {"g", "\u0183"}, {"h", "\u0265"}, {"i", "\u1d09"}, {"j", "\u027e"}, {"k", "\u029e"}, {"l", "\u0283"}, {"m", "\u026f"}, {"n", "u"}, {"o", "o"}, {"p", "d"}, {"q", "b"}, {"r", "\u0279"}, {"s", "s"}, {"t", "\u0287"}, {"u", "n"}, {"v", "\u028c"}, {"w", "\u028d"}, {"x", "x"}, {"y", "\u028e"}, {"z", "z"}, {"A", "\u2200"}, {"B", "\ud801\udc12"}, {"C", "\u0186"}, {"D", "\u25d6"}, {"E", "\u018e"}, {"F", "\u2132"}, {"G", "\u05e4"}, {"H", "H"}, {"I", "I"}, {"J", "\u017f"}, {"K", "\u029e"}, {"L", "\u02e5"}, {"M", "W"}, {"N", "N"}, {"O", "O"}, {"P", "\u0500"}, {"Q", "\u038c"}, {"R", "\u1d1a"}, {"S", "S"}, {"T", "\u22a5"}, {"U", "\u2229"}, {"V", "\u039b"}, {"W", "M"}, {"X", "X"}, {"Y", "\u2144"}, {"Z", "Z"}, {"1", "\u0196"}, {"2", "\u1105"}, {"3", "\u0190"}, {"4", "\u3123"}, {"5", "\u03db"}, {"6", "9"}, {"7", "\u3125"}, {"8", "8"}, {"9", "6"}, {"0", "0"}, {".", "\u02d9"}, {",", "'"}, {"'", "\u201a"}, {"\"", "\u201e"}, {"?", "\u00bf"}, {"!", "\u00a1"}, {"(", " )"}, {" )", "("}, {"[", "]"}, {"]", "["}, {"{", "}"}, {"}", "{"}, {"_", "\u203e"}}) {
                m.put(Character.valueOf(p[0].charAt(0)), p[1]);
            }
            return m;
        }

        static {
            SCRIPT_OVERRIDES.put(Character.valueOf('B'), "\u212c");
            SCRIPT_OVERRIDES.put(Character.valueOf('E'), "\u2130");
            SCRIPT_OVERRIDES.put(Character.valueOf('F'), "\u2131");
            SCRIPT_OVERRIDES.put(Character.valueOf('H'), "\u210b");
            SCRIPT_OVERRIDES.put(Character.valueOf('I'), "\u2110");
            SCRIPT_OVERRIDES.put(Character.valueOf('L'), "\u2112");
            SCRIPT_OVERRIDES.put(Character.valueOf('M'), "\u2133");
            SCRIPT_OVERRIDES.put(Character.valueOf('R'), "\u211b");
            SCRIPT_OVERRIDES.put(Character.valueOf('e'), "\u212f");
            SCRIPT_OVERRIDES.put(Character.valueOf('g'), "\u210a");
            SCRIPT_OVERRIDES.put(Character.valueOf('o'), "\u2134");
            DSTRK_OVERRIDES.put(Character.valueOf('C'), "\u2102");
            DSTRK_OVERRIDES.put(Character.valueOf('H'), "\u210d");
            DSTRK_OVERRIDES.put(Character.valueOf('N'), "\u2115");
            DSTRK_OVERRIDES.put(Character.valueOf('P'), "\u2119");
            DSTRK_OVERRIDES.put(Character.valueOf('Q'), "\u211a");
            DSTRK_OVERRIDES.put(Character.valueOf('R'), "\u211d");
            DSTRK_OVERRIDES.put(Character.valueOf('Z'), "\u2124");
        }
    }
}

