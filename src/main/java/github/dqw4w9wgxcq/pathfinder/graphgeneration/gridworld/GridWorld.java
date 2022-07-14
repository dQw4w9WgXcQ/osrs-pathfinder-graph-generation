package github.dqw4w9wgxcq.pathfinder.graphgeneration.gridworld;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import github.dqw4w9wgxcq.pathfinder.graphgeneration.CacheData;
import github.dqw4w9wgxcq.pathfinder.graphgeneration.commons.RegionUtil;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.cache.definitions.ObjectDefinition;
import net.runelite.cache.region.Location;
import net.runelite.cache.region.Region;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Slf4j
public class GridWorld {
    public static final int PLANES_SIZE = 4;

    @Getter
    private final int sizeX, sizeY;
    @Getter
    private final PlaneGrid[] planes;

    @VisibleForTesting
    GridWorld(int sizeX, int sizeY) {
        log.info("Creating map world with size x{}y{}", sizeX, sizeY);

        planes = new PlaneGrid[PLANES_SIZE];
        Arrays.setAll(planes, x -> new PlaneGrid(sizeX, sizeY));

        this.sizeX = sizeX;
        this.sizeY = sizeY;
    }

    public static GridWorld create(CacheData data) {
        var out = new GridWorld(data.highestWorldX(), data.highestWorldY());

        for (var region : data.regions().values()) {
            addFloorFromRegion(out.planes, region);
        }

        for (var region : data.regions().values()) {
            addLocationsFromRegion(out.planes, region.getLocations(), data.objectDefinitions());
        }

        return out;
    }

    public PlaneGrid getPlane(int plane) {
        Preconditions.checkArgument(plane >= 0 && plane < PLANES_SIZE, "plane must be between 0 and 3");

        return planes[plane];
    }

    static void addFloorFromRegion(PlaneGrid[] planes, Region region) {
        log.debug("adding region " + region.getRegionID() + " x" + region.getRegionX() + "y" + region.getRegionY());
        var baseX = region.getBaseX();
        var baseY = region.getBaseY();

        for (var z = 0; z < planes.length; z++) {
            for (var x = 0; x < RegionUtil.SIZE; x++) {
                for (var y = 0; y < RegionUtil.SIZE; y++) {
                    var tileSetting = region.getTileSetting(z, x, y);
                    if ((tileSetting & 1) == 1) {
                        var modifiedZ = z;
                        if ((region.getTileSetting(1, x, y) & 2) == 2) {
                            modifiedZ = z - 1;
                            log.trace("z was modified from " + z + " to " + modifiedZ + " at " + "x" + x + "y" + y);
                        }

                        if (modifiedZ >= 0) {
                            planes[modifiedZ].addFlag(x + baseX, y + baseY, CollisionFlags.FLOOR);
                        }
                    }
                }
            }
        }
    }

    static void addLocationsFromRegion(PlaneGrid[] planes, List<Location> locations, Map<Integer, ObjectDefinition> definitions) {
        for (var location : locations) {
            applyObjectLocationFlags(planes[location.getPosition().getZ()], location, definitions);
        }
    }

    static void applyObjectLocationFlags(PlaneGrid grid, Location location, Map<Integer, ObjectDefinition> definitions) {
        log.debug("adding location " + location);

        var position = location.getPosition();

        var x = position.getX();
        var y = position.getY();

        var objectDefinition = definitions.get(location.getId());

        log.debug("obj location name : " + objectDefinition.getName());

        var orientation = location.getOrientation();

        //rotate according to the orientation
        int sizeX;
        int sizeY;
        if (orientation == 1 || orientation == 3) {
            sizeX = objectDefinition.getSizeY();
            sizeY = objectDefinition.getSizeX();
        } else {
            sizeX = objectDefinition.getSizeX();
            sizeY = objectDefinition.getSizeY();
        }

        var locationType = location.getType();
        var interactType = objectDefinition.getInteractType();
        switch (locationType) {
            //wall objects
            case 0, 1, 2, 3 -> {
                if (interactType != 0) {
                    grid.addWallFlags(x, y, locationType, orientation);
                }
            }
            //wall decoration
            case 4, 5, 6, 7, 8, 9 -> {
                //no collisions
            }
            //game object
            case 10, 11 -> {
                if (interactType != 0) {
                    grid.addObjectFlags(x, y, sizeX, sizeY, false);
                }
            }
            //floor decoration
            case 22 -> {
                if (interactType == 1) {
                    grid.addObjectFlags(x, y, sizeX, sizeY, true);
                }
            }
            default -> {
                //
                if (locationType >= 12 && locationType <= 21) {
                    if (interactType != 0) {
                        grid.addObjectFlags(x, y, sizeX, sizeY, false);
                    }
                } else {
                    throw new IllegalArgumentException("expect:  0 =< locationType <= 22, found:" + locationType);
                }
            }
        }
    }
}

//from decompile for reference
/*
//floor flags added when render flags update
private void method2905(int[][][] renderFlags) {
    int var2;
    int var3;
    int var4;
    int var5;
    for (var2 = 0; var2 < 4; ++var2) {
        for (var3 = 0; var3 < 104; ++var3) {
            for (var4 = 0; var4 < 104; ++var4) {
                if ((renderFlags[var2][var3][var4] & 1) == 1) {
                    var5 = var2;
                    if ((renderFlags[1][var3][var4] & 2) == 2) {
                        var5 = var2 - 1;
                    }

                    if (var5 >= 0) {
                        addTileFlag(var2, var3, var4, 2097152);
                    }
                }
            }
        }
    }

    ...
}
 */