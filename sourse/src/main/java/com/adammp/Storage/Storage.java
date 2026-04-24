package com.adammp.Storage;

import java.util.ArrayList;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_2338;

@Environment(value=EnvType.CLIENT)
public class Storage {
    public static final List<class_2338> savedPositions = new ArrayList<class_2338>();
    public static final List<class_2338> antispamblockPositions = new ArrayList<class_2338>();
    public static int currentbadappleFrame = 0;
}

