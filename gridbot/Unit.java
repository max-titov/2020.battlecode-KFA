package gridbot;
import battlecode.common.*;

public class Unit extends Robot {

    Navigation nav;
    
    Grid grid;

    public Unit(RobotController r) {
        super(r);
        nav = new Navigation(rc);
    }

    public void takeTurn() throws GameActionException {
        super.takeTurn();       
        findHQ();
        if(grid == null) {
        	grid = new Grid(hqLoc);
        }
    }
}