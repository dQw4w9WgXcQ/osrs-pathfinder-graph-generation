package github.dqw4w9wgxcq.pathfinder.graphgeneration.cachedata;

import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;
import lombok.extern.slf4j.Slf4j;
import net.runelite.cache.fs.Store;
import net.runelite.cache.region.Region;
import net.runelite.cache.region.RegionLoader;
import net.runelite.cache.util.XteaKeyManager;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

//todo need to handle runelite returns empty locations list if no xteas for region
@Slf4j
public record RegionData(Map<Integer, Region> regions, int highestRegionX, int highestRegionY) {
    static RegionData load(Store store, XteaKeyManager xteaManager) throws IOException, JsonIOException, JsonSyntaxException {
        var regionLoader = new RegionLoader(store, xteaManager);

        regionLoader.loadRegions();//throws JsonIOException, JsonSyntaxException

        //cant access the internal map so have to reconstruct it
        var regionsCol = regionLoader.getRegions();
        var regions = new HashMap<Integer, Region>(regionsCol.size());
        for (var region : regionsCol) {
            log.debug("adding region " + region.getRegionID());
            regions.put(region.getRegionID(), region);
        }

        regionLoader.calculateBounds();
        var highestRegionX = regionLoader.getHighestX().getRegionX();
        var highestRegionY = regionLoader.getHighestY().getRegionY();

        log.info("loaded {} regions, highestRegionX:{} highestRegionY:{}", regions.size(), highestRegionX, highestRegionY);

        return new RegionData(
                regions,
                highestRegionX,
                highestRegionY
        );
    }
}
