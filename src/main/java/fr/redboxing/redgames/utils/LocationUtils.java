package fr.redboxing.redgames.utils;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.WorldBorder;

import java.util.Random;

public class LocationUtils {
    private static final Random RANDOM = new Random();
    public static Location GetRandomLocation(World world, int maxRadius) {
        WorldBorder border = world.getWorldBorder();
        Location borderCenter = border.getCenter();
        double borderSize = border.getSize() / 2;

        int centerX = (int) borderCenter.getX();
        int centerZ = (int )borderCenter.getZ();

        int MaxX = (int) (centerX + borderSize);
        int MinX = (int) (centerX - borderSize);
        int MaxZ = (int) (centerZ + borderSize);
        int MinZ = (int) (centerZ - borderSize);

        if (MaxX > centerX + maxRadius) MaxX = centerX + maxRadius;
        if (MinX < centerX - maxRadius) MinX = centerX - maxRadius;
        if (MaxZ > centerZ + maxRadius) MaxZ = centerZ + maxRadius;
        if (MinZ < centerZ - maxRadius) MinZ = centerZ - maxRadius;

        Location loc = new Location(world, RANDOM.nextInt(MinX, MaxX), 62, RANDOM.nextInt(MinZ, MaxZ));
        if (world.getHighestBlockAt(loc).isLiquid()) return GetRandomLocation(world, maxRadius);
        loc = world.getHighestBlockAt(loc).getLocation().add(0.5, 1, 0.5);
        return loc;
    }
}
