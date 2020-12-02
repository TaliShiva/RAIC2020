import model.Entity;
import model.EntityType;
import model.Vec2Int;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.List;

public class BuildingScheduler {
    final static int MAP_SIZE_X = 80;
    final static int MAP_SIZE_Y = 80;
    final static int START_SQUARE_SIZE = 23;
    HashSet<Vec2Int> occupiedTiles = new HashSet<>();

    public BuildingScheduler(@NotNull final List<Entity> resources, @NotNull final List<Entity> buildings) {
        for (var res : resources) {
            Vec2Int position = res.getPosition();
            occupiedTiles.add(position);
        }

        for (var build : buildings) {
            Vec2Int position = build.getPosition();
            EntityType entityType = build.getEntityType();
            int sizeForThisBuild = getSizeForBuildType(entityType);
            for (int x = position.getX(); x < position.getX() + sizeForThisBuild; x++) {
                for (int y = position.getY(); y < position.getY() + sizeForThisBuild; y++) {
                    occupiedTiles.add(new Vec2Int(x, y));
                }
            }
        }
    }

    Vec2Int getEmptyPlaceForBuild(final EntityType buildType) {
        for (int x = 0; x < START_SQUARE_SIZE; x++) {
            for (int y = 0; y < START_SQUARE_SIZE; y++) {
                if (checkBuildPlace(new Vec2Int(x, y), getSizeForBuildType(buildType), occupiedTiles)) {
                    return new Vec2Int(x, y);
                }
            }
        }
        throw new IllegalStateException("Haven't place for building");
    }

    Vec2Int getPositionForBuilderForBuilding(Vec2Int buildingPos, Vec2Int currentBuilderPos, EntityType buildType) {
        return new Vec2Int();//TODO
    }

    boolean checkBuildPlace(Vec2Int position, int sizeForThisBuild, HashSet<Vec2Int> occupiedTiles) {
        for (int x = position.getX(); x < position.getX() + sizeForThisBuild; x++) {
            for (int y = position.getY(); y < position.getY() + sizeForThisBuild; y++) {
                if (occupiedTiles.contains(new Vec2Int(x, y))) {
                    return false;
                }
            }
        }
        return true;
    }

    int getSizeForBuildType(@NotNull EntityType entityType) {
        switch (entityType) {
            case BUILDER_BASE, MELEE_BASE, RANGED_BASE -> {
                return EXPERT_CONSTANT.DEFAUILT_CONSTRUCTION_SIZE;
            }
            case WALL -> {
                return EXPERT_CONSTANT.SIMPLE_ENTITY_SIZE;
            }
            case HOUSE -> {
                return EXPERT_CONSTANT.HOUSE_SIZE;
            }
            case TURRET -> {
                return EXPERT_CONSTANT.TORRET_SIZE;
            }
            default -> {
                return 0;
            }
        }
    }
}
