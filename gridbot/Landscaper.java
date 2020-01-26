package gridbot;
import battlecode.common.*;

public class Landscaper extends Unit {
	int landHeight = 10;
	Direction preferedDir;
	int landscaperType = 0;
	final int GRID_LANDSCAPER = 1;
    public Landscaper(RobotController r) throws GameActionException {
        super(r);
        preferedDir = Util.dirs[Util.rand(8)];
    }

    public void takeTurn() throws GameActionException {
        super.takeTurn();
        if(landscaperType == 0) {
			landscaperType = GRID_LANDSCAPER;
		}
        
        switch (landscaperType) {
        case GRID_LANDSCAPER:
        	runGridLandscaper();
        	break;
        }
    }
    
    void runGridLandscaper() throws GameActionException {
    	if(!defendRush()) {
    		workOnGrid();
    	}
    	
    	
    }
    
    boolean defendRush() throws GameActionException {
    	//if far from hq or no nearby enemy bots
    	if(!currentLoc.isWithinDistanceSquared(hqLoc, 50) || !nearbyAttackRobots()) {
    		return false;
    	}
    	System.out.println(currentLoc.directionTo(hqLoc));
    	//move towards hq and dig dirt from it

    	if(!currentLoc.isAdjacentTo(hqLoc)) {
    		nav.noReturnNav(hqLoc);
    		
    	} else {
    		Direction dirtohq = currentLoc.directionTo(hqLoc);
            if(rc.canDigDirt(dirtohq)){
                rc.digDirt(dirtohq);
            }
    	}
    	
    	//check for adjacent enemy buildings
    	for(int i = 0; i<Util.dirsLen;i++) {
    		RobotInfo testInfo = rc.senseRobotAtLocation(currentLoc.add(Util.dirs[i]));
    		if(testInfo!= null && building(testInfo, opponent)) {
    			if(rc.canDepositDirt(Util.dirs[i])){
                    rc.depositDirt(Util.dirs[i]);
                }
    		}
    	}
    	//check for adjacent allied buildings
    	for(int i = 0; i<Util.dirsLen;i++) {
    		RobotInfo testInfo = rc.senseRobotAtLocation(currentLoc.add(Util.dirs[i]));
    		if(testInfo!= null && building(testInfo, myTeam)) {
    			if(rc.canDigDirt(Util.dirs[i])){
                    rc.depositDirt(Util.dirs[i]);
                }
    		}
    	}
    	
    	return true;
    }
    
    
    void workOnGrid() throws GameActionException {
    	MapLocation nonDiggingSpot = whereToDigNonDiggingSpot();
    	if(rc.getDirtCarrying() < RobotType.LANDSCAPER.dirtLimit && nonDiggingSpot != null) {
    		Direction digDir = currentLoc.directionTo(nonDiggingSpot);
    		if(rc.canDigDirt(digDir)) {
    			rc.digDirt(digDir);
    		}
    	} else if (rc.getDirtCarrying() == 0) {
    		MapLocation digLoc = whereToDig();
    		if(digLoc != null) {
    			//rc.setIndicatorDot(digLoc, 255, 0, 0);
	    		Direction digDir = currentLoc.directionTo(digLoc);
	    		if(rc.canDigDirt(digDir)) {
	    			rc.digDirt(digDir);
	    		}
    		}
    	}else {
    		MapLocation depositLoc = whereToDeposit();
    		if(depositLoc != null && !grid.tooCloseToHQ(depositLoc)) {
    			//rc.setIndicatorDot(depositLoc, 0, 255, 255);
	    		Direction depositDir = currentLoc.directionTo(depositLoc);
	    		if(rc.canDepositDirt(depositDir)) {
	    			rc.depositDirt(depositDir);
	    		}
    		}
    	}
    	Direction movementDir = currentLoc.directionTo(hqLoc).opposite();
    	if(!currentLoc.isWithinDistanceSquared(hqLoc, round/10)) {
    		movementDir = movementDir.rotateLeft().rotateLeft();
    	}
    	
    	if(rc.isReady()) {
    		nav.noReturnNav(movementDir);
    	}
    }
    
    ///////////HELPER METHODS///////////
    MapLocation whereToDigNonDiggingSpot() throws GameActionException {
    	MapLocation currentLoc = rc.getLocation();
    	int currentElevation = rc.senseElevation(currentLoc);
    	//prioritize non digging spots for digging
    	for(int i = 0; i < Util.dirsLen; i++) {
    		MapLocation testLoc = currentLoc.add(Util.dirs[i]);
    		boolean lowEnough = rc.canSenseLocation(testLoc) && 
    				rc.senseElevation(testLoc)-3 > currentElevation && 
    				currentElevation >= landHeight && 
    				rc.senseElevation(testLoc) < RobotType.LANDSCAPER.dirtLimit+landHeight;
    		if(rc.canSenseLocation(testLoc) && rc.senseElevation(testLoc) > landHeight && lowEnough) { //if greater than 3 elevation than current elevation
    			return testLoc;
    		}
    	}
    	return null;
    }
    
    MapLocation whereToDig() throws GameActionException {
    	MapLocation currentLoc = rc.getLocation();
    	int currentElevation = rc.senseElevation(currentLoc);
    	
    	for(int i = 0; i < Util.dirsLen; i++) {
    		MapLocation testLoc = currentLoc.add(Util.dirs[i]);
    		if(rc.canSenseLocation(testLoc) && grid.isDiggingSpot(testLoc)) { //if a digging spot
    			return testLoc;
    		}
    	}
    	return null;
    }
    
    MapLocation whereToDeposit() throws GameActionException {
    	MapLocation currentLoc = rc.getLocation();
    	int currentElevation = rc.senseElevation(currentLoc);
    	for(int i = 0; i < Util.allDirsLen; i++) {
    		MapLocation testLoc = currentLoc.add(Util.allDirs[i]);
    		//if not a digging spot and less than 3 elevation than current elevation
    		// || rc.senseElevation(testLoc)+3 < currentElevation
    		if(rc.canSenseLocation(testLoc) && 
    				!grid.isDiggingSpot(testLoc) && 
    				(rc.senseElevation(testLoc) < landHeight || rc.senseElevation(testLoc)+3 < currentElevation) &&
    				rc.senseElevation(testLoc) > -101 &&
    				!ourBuildingThere(testLoc)) {     			
    			return testLoc;
    		}
    	}
    	return null;
    }
    
    boolean ourBuildingThere(MapLocation loc) throws GameActionException {
    	RobotInfo info = rc.senseRobotAtLocation(loc);
    	if(info != null && info.getTeam().equals(myTeam)&&
    			(info.getType().equals(RobotType.REFINERY) || 
    					info.getType().equals(RobotType.DESIGN_SCHOOL) || 
    					info.getType().equals(RobotType.FULFILLMENT_CENTER) || 
    					info.getType().equals(RobotType.NET_GUN) || 
    					info.getType().equals(RobotType.VAPORATOR))) {
    		return true;
    	}
    	return false;
    }

    boolean nearbyAttackRobots() {
    	for (int i = 0; i<nearbyEnemyRobots.length;i++) {
			if (nearbyEnemyRobots[i].getType().equals(RobotType.DESIGN_SCHOOL) ||
					nearbyEnemyRobots[i].getType().equals(RobotType.LANDSCAPER) ||
					nearbyEnemyRobots[i].getType().equals(RobotType.MINER)) {
				return true;
			}
		}
    	return false;
    }
}