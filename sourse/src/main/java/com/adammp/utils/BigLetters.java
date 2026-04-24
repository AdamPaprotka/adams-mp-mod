package com.adammp.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(value=EnvType.CLIENT)
public class BigLetters {
    public List<String> ReturnLetter(String letter) {
        ArrayList<String> ls = new ArrayList<String>();
        if (Objects.equals(letter, "A") || Objects.equals(letter, "a")) {
            ls.add("    /\\    ");
            ls.add("   /__\\   ");
            ls.add("  /----\\  ");
            ls.add(" /         \\ ");
            return ls;
        }
        if (Objects.equals(letter, "B") || Objects.equals(letter, "b")) {
            ls.add(" ______\\");
            ls.add("|      /");
            ls.add("|------\\");
            ls.add("|______/");
        }
        if (Objects.equals(letter, "C") || Objects.equals(letter, "c")) {
            ls.add(" ______");
            ls.add("|      ");
            ls.add("|      ");
            ls.add("|______");
        }
        if (Objects.equals(letter, "D") || Objects.equals(letter, "d")) {
            ls.add(" _____");
            ls.add("|      \\");
            ls.add("|      |");
            ls.add("|_____/");
        }
        if (Objects.equals(letter, "E") || Objects.equals(letter, "e")) {
            ls.add(" _____");
            ls.add("|      \\");
            ls.add("|      |");
            ls.add("|_____/");
        }
        return Collections.singletonList("None");
    }
}

