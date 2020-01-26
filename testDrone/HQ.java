package testDrone;
import battlecode.common.*;

public class HQ extends Shooter {
    int numMiners = 0;
    boolean built;
    MapLocation hqLoc;
    int hqElevation; 

    public HQ(RobotController r) throws GameActionException {
        super(r);
        hqLoc = rc.getLocation();
        hqElevation = rc.senseElevation(hqLoc);
        if(rc.getRoundNum() == 1)
        	comms.sendHQLoc(hqLoc);
    }

    public void takeTurn() throws GameActionException {
    	if(!built) {
    		tryBuild(RobotType.MINER, Direction.EAST);
    		built = true;
    	}
        
    }
    
    public void sendDesiredSchoolLoc() throws GameActionException {
    	//prefers locations that are north, east, south, or west
    	for(int i = 0; i<Util.cardinalDirsLen; i++) {
    		MapLocation checkLoc = hqLoc.add(Util.cardinalDirs[i]);
    		if(rc.onTheMap(checkLoc) && Math.abs(rc.senseElevation(checkLoc) - hqElevation)<=3 && rc.senseRobotAtLocation(checkLoc) == null) {
    			comms.sendDesiredSchoolPlacement(checkLoc);
    			return;
    		}
    	}
    	for(int i = 0; i<Util.diagonalDirsLen; i++) {
    		MapLocation checkLoc = hqLoc.add(Util.diagonalDirs[i]);
    		if(rc.onTheMap(checkLoc) && Math.abs(rc.senseElevation(checkLoc) - hqElevation)<=3 && rc.senseRobotAtLocation(checkLoc) == null) {
    			comms.sendDesiredSchoolPlacement(checkLoc);
    			return;
    		}
    	}
    }
}