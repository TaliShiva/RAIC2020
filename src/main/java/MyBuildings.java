import model.Entity;

import java.util.ArrayList;
import java.util.List;

public class MyBuildings {
    List<Entity> myBuilderBases = new ArrayList<>();
    List<Entity> myRangeBases = new ArrayList<>();
    List<Entity> myMelleBases = new ArrayList<>();
    List<Entity> myHouses = new ArrayList<>();

    List<Entity> getAllMyBuildings(){
        List<Entity> allMyBuildings = new ArrayList<>(myBuilderBases);
        allMyBuildings.addAll(myHouses);
        allMyBuildings.addAll(myMelleBases);
        allMyBuildings.addAll(myRangeBases);
        return allMyBuildings;
    }
}
