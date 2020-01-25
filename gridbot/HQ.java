package gridbot;
import battlecode.common.*;

public class HQ extends Shooter {
    int numMiners = 0;
    
    MapLocation hqLoc;
    int hqElevation; 

    public HQ(RobotController r) throws GameActionException {
        super(r);
        hqLoc = rc.getLocation();
        hqElevation = rc.senseElevation(hqLoc);
        comms.sendHQLoc(hqLoc);
    }

    public void takeTurn() throws GameActionException {
        super.takeTurn();
        int round = rc.getRoundNum();
        
        if(round<=2) { //sends several times for redundecy purposes
        	comms.sendHQLoc(rc.getLocation());
        }

        if(numMiners < 5) {
            for (Direction dir : Util.dirs)
                if(tryBuild(RobotType.MINER, dir)){
                    numMiners++;
                }
        }
        
        if(round == 100) {
        	sendDesiredSchoolLoc();
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