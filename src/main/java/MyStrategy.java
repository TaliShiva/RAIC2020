import model.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

public class MyStrategy {
    static List<Entity> resources = new ArrayList<>();

    public Action getAction(PlayerView playerView, DebugInterface debugInterface) {
        HashMap<Integer, EntityAction> entityActions = new HashMap<>();
        int currentTick = playerView.getCurrentTick();
        Player[] players = playerView.getPlayers();
        int myId = playerView.getMyId();
        int id = players[0].getId();


        List<Entity> enemies = new ArrayList<>();
        List<Entity> myMelle = new ArrayList<>();
        List<Entity> myRange = new ArrayList<>();
        List<Entity> myBuilders = new ArrayList<>();
        List<Entity> myBuilderBases = new ArrayList<>();
        List<Entity> myBuild = new ArrayList<>();
        List<Entity> enemiesBuilds = new ArrayList<>();
        Entity[] entities = playerView.getEntities();
        resources = Arrays.stream(entities).filter(e -> e.getEntityType() == EntityType.RESOURCE).collect(Collectors.toList());

        List<Entity> myEntities = Arrays.stream(entities).filter(e -> e.getPlayerId() != null).filter(e -> e.getPlayerId() == myId).collect(Collectors.toList());
        fillMyArmyAndMyBuildings(myMelle, myRange, myBuilders, myEntities, myBuilderBases);
        for (var builder : myBuilders) {
            var moveAction = new MoveAction(getNearestRes(builder), true, true);
            var entityAction = new EntityAction();
            entityAction.setMoveAction(moveAction);
            entityActions.put(builder.getId(), entityAction);
        }
        //TODO: нужно условие для постройки
        if (currentTick % 10 == 0) {
            for (var builderBase : myBuilderBases) {
                var entityAction = new EntityAction();
                var buildAction = new BuildAction();
                buildAction.setEntityType(EntityType.BUILDER_UNIT);
                buildAction.setPosition(new Vec2Int(builderBase.getPosition().getX() + 5, builderBase.getPosition().getY() + 5 - 1));

                entityAction.setBuildAction(buildAction);
                entityActions.put(builderBase.getId(), entityAction);
            }
        }
        return new Action(entityActions);
    }

    private Vec2Int getNearestRes(Entity fromEntity) {
        Vec2Int fromEntityPosition = fromEntity.getPosition();
        Vec2Int nearestRes = new Vec2Int();
        double minLength = 1000;
        List<Vec2Int> positions = new ArrayList<>();
        for (var res : resources) {
            positions.add(res.getPosition());
        }
        for (Vec2Int pos : positions) {
            double length = Math.sqrt((pos.getX() - fromEntityPosition.getX()) * (pos.getX() - fromEntityPosition.getX())
                    + (pos.getY() - fromEntityPosition.getY()) * (pos.getY() - fromEntityPosition.getY()));
            if (length < minLength) {
                minLength = length;
                nearestRes.setX(pos.getX());
                nearestRes.setY(pos.getY());
            }
        }
        return nearestRes;
    }

    private void fillMyArmyAndMyBuildings(List<Entity> myMelle, List<Entity> myRange, List<Entity> myBuilders, List<Entity> myEntities, List<Entity> myBuilderBases) {
        for (Entity entity : myEntities) {
            switch (entity.getEntityType()) {
                case MELEE_UNIT -> {
                    myMelle.add(entity);
                }
                case RANGED_UNIT -> {
                    myRange.add(entity);
                }
                case BUILDER_UNIT -> {
                    myBuilders.add(entity);
                }
                case BUILDER_BASE -> {
                    myBuilderBases.add(entity);
                }
            }
        }
    }

    public void debugUpdate(PlayerView playerView, DebugInterface debugInterface) {
        debugInterface.send(new DebugCommand.Clear());
        debugInterface.getState();
    }
}