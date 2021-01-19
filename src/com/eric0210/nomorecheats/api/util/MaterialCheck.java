package com.eric0210.nomorecheats.api.util;

import org.bukkit.Location;
import org.bukkit.Material;

public abstract class MaterialCheck
{
    public final boolean checkMaterial(Location location) {
        return this.checkMaterial(location.getBlock().getType());
    }

    public abstract boolean checkMaterial(Material var1);
}
