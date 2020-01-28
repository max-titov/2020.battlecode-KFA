package gridbot;

import battlecode.common.*;

public class Grid {
	RobotController rc;
	MapLocation hqLoc;
	
	int roundRatio = 7;
	
	public Grid(RobotController r, MapLocation hq) {
        rc = r;
		hqLoc = hq;
		roundRatio = 7;
    }
	
	public void changeRoundRatio(int ratio) {
		roundRatio = ratio;
	}
	
    boolean isDiggingSpot(MapLocation loc) {
    	MapLocation modCheckLoc = hqLoc.translate(loc.x+1, loc.y+1);
    	if(modCheckLoc.x%2 == 0 && modCheckLoc.y%2 ==0 && !tooCloseToHQ(loc)) {
    		return true;
    	}
    	return false;
    }
    
    boolean isBuildingSpot(MapLocation loc) {
    	MapLocation modCheckLoc = hqLoc.translate(loc.x, loc.y);
    	if(modCheckLoc.x%2 == 0 && modCheckLoc.y%2 ==0 && !tooCloseToHQ(loc)) {
    		return true;
    	}
    	return false;
    }
    
    boolean tooCloseToHQ(MapLocation loc) {
    	int dist = hqLoc.distanceSquaredTo(loc);
    	if(dist <= 8) {
    		return true;
    	}
    	return false;
    }
    
    boolean withinBoundsOfGrid(MapLocation loc) {
    	if(hqLoc.isWithinDistanceSquared(loc, rc.getRoundNum()/roundRatio)) {
    		return true;
    	}
    	return false;
    }
}
