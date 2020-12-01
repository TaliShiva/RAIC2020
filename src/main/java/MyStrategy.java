import model.*;

import java.util.*;
import java.util.stream.Collectors;

public class MyStrategy {
    public static final int DEFAUILT_CONSTRUCTION_SIZE = 5;
    public static final int HOUSE_SIZE = 3;
    public static final int SUPPLY_BUILD_SIZE = 5;
    static List<Entity> resources = new ArrayList<>();

    public Action getAction(PlayerView playerView, DebugInterface debugInterface) {
        HashMap<Integer, EntityAction> entityActions = new HashMap<>();
        int currentTick = playerView.getCurrentTick();
        Player[] players = playerView.getPlayers();
        int myId = playerView.getMyId();
        int myRes = getMyRes(players, myId);

        List<Entity> myMelle = new ArrayList<>();
        List<Entity> myRangers = new ArrayList<>();
        List<Entity> myBuilders = new ArrayList<>();
        List<Entity> myBuilderBases = new ArrayList<>();
        List<Entity> myRangeBases = new ArrayList<>();
        List<Entity> myMelleBases = new ArrayList<>();
        List<Entity> myHouses = new ArrayList<>();
        Entity[] entities = playerView.getEntities();
        resources = Arrays.stream(entities).filter(e -> e.getEntityType() == EntityType.RESOURCE).collect(Collectors.toList());
        List<Entity> myEntities = Arrays.stream(entities).filter(e -> e.getPlayerId() != null).filter(e -> e.getPlayerId() == myId).collect(Collectors.toList());
        List<Entity> enemyEntities = Arrays.stream(entities).filter(e -> e.getPlayerId() != null).filter(e -> e.getPlayerId() != myId).collect(Collectors.toList());

        fillMyArmyAndMyBuildings(myMelle, myRangers, myBuilders, myEntities, myBuilderBases, myMelleBases, myHouses, myRangeBases);
        for (var builder : myBuilders) {
            var moveAction = new MoveAction(getNearestEntityPosition(builder, resources), true, true);
            var entityAction = new EntityAction();
            entityAction.setMoveAction(moveAction);
            entityActions.put(builder.getId(), entityAction);
        }
        var vertices = new ColoredVertex[]{
                new ColoredVertex(
                        new Vec2Float(2f, 2f),
                        new Vec2Float(0.5f, -0.5f),
                        new Color(100, 0, 0, 100))
        };
        var cock = new DebugData.Primitives(vertices,PrimitiveType.LINES);
        DebugCommand.Add debugCommand = new DebugCommand.Add();
        debugCommand.setData(cock);
        if (myRes >= 50) {
            Random rand = new Random(myBuilders.size() - 1);
            Entity houseBuilder = myBuilders.get(rand.nextInt());
            EntityAction entityAction = entityActions.get(houseBuilder.getId());
            BuildAction buildAction = new BuildAction(EntityType.HOUSE, new Vec2Int(2, 2));
            entityAction.setMoveAction(new MoveAction(new Vec2Int(5, 6), false, false));
            entityAction.setBuildAction(buildAction);


        }

        //TODO: нужно условие для постройки
        int supplyBlock = (myBuilderBases.size() + myMelleBases.size() + myHouses.size() + myRangeBases.size()) * SUPPLY_BUILD_SIZE;
        buildUnitMacro(entityActions, currentTick, myBuilders, myBuilderBases, myRangeBases, myMelleBases, supplyBlock);
        setArmyTargets(entityActions, myMelle, myRangers, enemyEntities);

        return new Action(entityActions);
    }

    private int getMyRes(Player[] players, int myId) {
        int myRes = 0;
        for (Player player : players) {
            if (player.getId() == myId) {
                myRes = player.getResource();
            }
        }
        return myRes;
    }

    private void buildUnitMacro(HashMap<Integer, EntityAction> entityActions, int currentTick, List<Entity> myBuilders, List<Entity> myBuilderBases, List<Entity> myRangeBases, List<Entity> myMelleBases, int supplyBlock) {
        if (myBuilders.size() <= (3f / 10) * supplyBlock) {
            for (var builderBase : myBuilderBases) {
                var entityAction = new EntityAction();
                var buildAction = new BuildAction();
                buildAction.setEntityType(EntityType.BUILDER_UNIT);
                buildAction.setPosition(new Vec2Int(builderBase.getPosition().getX() + DEFAUILT_CONSTRUCTION_SIZE, builderBase.getPosition().getY() + 5 - 1));

                entityAction.setBuildAction(buildAction);
                entityActions.put(builderBase.getId(), entityAction);
            }
        } else {
            for (var builderBase : myBuilderBases) {
                var entityAction = new EntityAction();
                entityActions.put(builderBase.getId(), entityAction);
            }
        }

        if (currentTick >= 40) {
            for (var rangeBase : myRangeBases) {
                var entityAction = new EntityAction();
                var buildAction = new BuildAction();
                buildAction.setEntityType(EntityType.RANGED_UNIT);
                buildAction.setPosition(new Vec2Int(rangeBase.getPosition().getX() + DEFAUILT_CONSTRUCTION_SIZE, rangeBase.getPosition().getY() + 5 - 1));
                entityAction.setBuildAction(buildAction);
                entityActions.put(rangeBase.getId(), entityAction);
            }
        }

        if (currentTick >= 40) {
            for (var melleBase : myMelleBases) {
                var entityAction = new EntityAction();
                var buildAction = new BuildAction();
                buildAction.setEntityType(EntityType.MELEE_UNIT);
                buildAction.setPosition(new Vec2Int(melleBase.getPosition().getX() + DEFAUILT_CONSTRUCTION_SIZE, melleBase.getPosition().getY() + 5 - 1));
                entityAction.setBuildAction(buildAction);
                entityActions.put(melleBase.getId(), entityAction);
            }
        }
    }

    private void setArmyTargets(HashMap<Integer, EntityAction> entityActions, List<Entity> myMelle, List<Entity> myRangers, List<Entity> enemyEntities) {
        EntityType[] validEnemyTypes = new EntityType[]{
                EntityType.RANGED_UNIT,
                EntityType.MELEE_UNIT,
                EntityType.BUILDER_UNIT,
                EntityType.RANGED_BASE,
                EntityType.BUILDER_BASE,
                EntityType.MELEE_BASE};

        for (var ranger : myRangers) {
            try {
                Vec2Int nearestEntity = getNearestEntityPosition(ranger, enemyEntities);
                var moveAction = new MoveAction();
                if (getSqrtDist(ranger.getPosition(), nearestEntity) > 6) {
                    moveAction.setTarget(nearestEntity);
                    moveAction.setFindClosestPosition(true);
                    moveAction.setBreakThrough(true);
                } else {
                    moveAction.setTarget(ranger.getPosition());
                    moveAction.setFindClosestPosition(true);
                    moveAction.setBreakThrough(true);
                }
                var autoAttack = new AutoAttack(5, validEnemyTypes);
                var attackAction = new AttackAction(getNearestEntity(ranger, enemyEntities).getId(), autoAttack);
                var entityAction = new EntityAction();
                entityAction.setAttackAction(attackAction);
                entityAction.setMoveAction(moveAction);
                entityActions.put(ranger.getId(), entityAction);
            } catch (Exception ex) {
                System.out.println(ex.getMessage());
            }
        }

        for (var melle : myMelle) {
            try {
                var moveAction = new MoveAction(getNearestEntityPosition(melle, enemyEntities), true, true);
                var autoAttack = new AutoAttack(5, validEnemyTypes);
                var attackAction = new AttackAction(getNearestEntity(melle, enemyEntities).getId(), autoAttack);
                var entityAction = new EntityAction();
                entityAction.setMoveAction(moveAction);
                entityAction.setAttackAction(attackAction);
                entityActions.put(melle.getId(), entityAction);
            } catch (Exception ex) {
                System.out.println(ex.getMessage());
            }
        }
    }

    private Vec2Int getNearestEntityPosition(Entity fromEntity, List<Entity> entities) {
        Vec2Int fromEntityPosition = fromEntity.getPosition();
        Vec2Int nearestRes = new Vec2Int();
        double minLength = 1000;
        for (Entity entity : entities) {
            double length = getSqrtDist(fromEntityPosition, entity.getPosition());
            if (length < minLength) {
                minLength = length;
                nearestRes.setX(entity.getPosition().getX());
                nearestRes.setY(entity.getPosition().getY());
            }
        }
        return nearestRes;
    }

    private Entity getNearestEntity(Entity fromEntity, List<Entity> entities) {
        Vec2Int fromEntityPosition = fromEntity.getPosition();
        double minLength = 1000;
        var outEntity = new Entity();
        for (Entity ent : entities) {
            double length = getSqrtDist(fromEntityPosition, ent.getPosition());
            if (length < minLength) {
                minLength = length;
                outEntity = ent;
            }
        }
        return outEntity;
    }

    private double getSqrtDist(Vec2Int fromEntityPosition, Vec2Int pos) {
        return Math.sqrt((pos.getX() - fromEntityPosition.getX()) * (pos.getX() - fromEntityPosition.getX())
                + (pos.getY() - fromEntityPosition.getY()) * (pos.getY() - fromEntityPosition.getY()));
    }


    private void fillMyArmyAndMyBuildings(List<Entity> myMelle, List<Entity> myRange, List<Entity> myBuilders, List<Entity> myEntities, List<Entity> myBuilderBases, List<Entity> myMelleBases, List<Entity> myHouses, List<Entity> myRangeBases) {
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
                case MELEE_BASE -> {
                    myMelleBases.add(entity);
                }
                case RANGED_BASE -> {
                    myRangeBases.add(entity);
                }
            }
        }
    }

    public void debugUpdate(PlayerView playerView, DebugInterface debugInterface) {
        debugInterface.send(new DebugCommand.Clear());
        debugInterface.getState();
    }
}