package gridbot;

import battlecode.common.*;

public class Grid {
	
	MapLocation hqLoc;
	
	public Grid(MapLocation hq) {
        hqLoc = hq;
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
}
