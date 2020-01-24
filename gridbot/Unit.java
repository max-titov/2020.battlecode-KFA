package gridbot;
import battlecode.common.*;

public class Unit extends Robot {

    Navigation nav;

    MapLocation hqLoc;
    
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

    public void findHQ() throws GameActionException {
        if (hqLoc == null) {
            // search surroundings for HQ
            RobotInfo[] robots = rc.senseNearbyRobots();
            for (RobotInfo robot : robots) {
                if (robot.type == RobotType.HQ && robot.team == rc.getTeam()) {
                    hqLoc = robot.location;
                    return;
                }
            }
            // if still null, search the blockchain
            hqLoc = comms.getHqLocFromBlockchain();
        }
    }
}