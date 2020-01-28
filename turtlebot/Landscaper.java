package turtlebot;
import battlecode.common.*;

public class Landscaper extends Unit {
	
	
	MapLocation[] innerWallLocs = new MapLocation[unadjustedInnerWallLocs.length];
	MapLocation[] innerAvaliableWallLocs = new MapLocation[unadjustedInnerWallLocs.length];
	
	
	MapLocation[] outerWallLocs = new MapLocation[unadjustedOuterWallLocs.length];
	MapLocation[] outerAvaliableWallLocs = new MapLocation[unadjustedOuterWallLocs.length];
	MapLocation desiredWallLoc = null;
	
	final MapLocation[] unadjustedDiggingSpots = {l(-2,0),l(0,2),l(2,0),l(0,-2),l(-3,3),l(3,3),l(3,-3),l(-3,-3),
			l(-2,-3),l(-3,-2),l(-3,2),l(-2,3),l(2,3),l(3,2),l(3,-2),l(2,-3),
			l(-1,-3),l(-3,-1),l(-3,1),l(-1,3),l(1,3),l(3,1),l(3,-1),l(1,-3)};
	MapLocation[] diggingSpots = new MapLocation[unadjustedDiggingSpots.length];
	
	MapLocation prevDesiredLoc = currentLoc;
	int closestToDesiredLoc = 99999;
	int roundsSinceClosest = 0;
	int uberRequestRound = -90;
	
	public Landscaper(RobotController r) throws GameActionException {
        super(r);
        createWallLocs();
      
    }

    public void takeTurn() throws GameActionException {
        super.takeTurn();
        runWallLandScaper();
    }
    
    void runWallLandScaper() throws GameActionException {
    	if (hqLoc != null && hqLoc.isAdjacentTo(rc.getLocation())) {
            Direction dirtohq = rc.getLocation().directionTo(hqLoc);
            if(rc.canDigDirt(dirtohq)){
                rc.digDirt(dirtohq);
            }
        }
    	
    	updateWallLocs();
    	MapLocation desiredLoc = null;
    	int closestToCorner = 99999;
    	
    	for(int i = 0; i < innerAvaliableWallLocs.length;i++) {
    		MapLocation testLoc = innerAvaliableWallLocs[i];
    		if(testLoc != null) {
	    		int testDist = distanceToCorner(testLoc);
	    		if(testDist<closestToCorner) {
	    			desiredLoc = testLoc;
	    			closestToCorner = testDist;
	    		}
    		}
    	}
    	if(desiredLoc == null) {
    		for(int i = 0; i < outerAvaliableWallLocs.length;i++) {
        		MapLocation testLoc = outerAvaliableWallLocs[i];
        		if(testLoc != null) {
    	    		int testDist = distanceToCorner(testLoc);
    	    		if(testDist<closestToCorner) {
    	    			desiredLoc = testLoc;
    	    			closestToCorner = testDist;
    	    		}
        		}
        	}
    	}
    	if(desiredLoc!=null) {
	    	if(!desiredLoc.equals(currentLoc)) {
    			if(prevDesiredLoc!=null) {
    				if(roundsSinceClosest>15 && round-uberRequestRound>30) {
    					comms.requestUber(currentLoc, desiredLoc);
    					uberRequestRound=round;
    				}
    				if(desiredLoc.equals(prevDesiredLoc)) {
    					int dist = currentLoc.distanceSquaredTo(desiredLoc);
    					if(dist<closestToDesiredLoc) {
    						roundsSinceClosest = 0;
    						closestToDesiredLoc=dist;
    					}else {
    						roundsSinceClosest++;
    					}
    				}else {
    					closestToDesiredLoc=99999;
    					roundsSinceClosest=0;
    				}
    			}else {
    				closestToDesiredLoc=99999;
    				roundsSinceClosest=0;
    			}
    			prevDesiredLoc=desiredLoc;
	    		nav.noReturnNav(desiredLoc);
	    	}else {
	    		//start digging
				MapLocation diggingSpot = currentLoc;
				for(int i = 0; i<diggingSpots.length; i++) {
					if(diggingSpots[i]!=null&&currentLoc.isAdjacentTo(diggingSpots[i])) {
						diggingSpot = diggingSpots[i];
						break;
					}
				}
				MapLocation depositSpot = currentLoc;
				if(currentLoc.isAdjacentTo(hqLoc)) {//inner landscaper
					if(round >= 350) {
						depositSpot = lowestWall();
						if(depositSpot == null)depositSpot= currentLoc;
					}
				}else {//outer landscaper
					if(GameConstants.getWaterLevel(rc.getRoundNum())+2>rc.senseElevation(currentLoc)) { //if water level is close
						
					}else {
						depositSpot = lowestWall();
						if(depositSpot == null)depositSpot= currentLoc;
//						depositSpot = currentLoc.add(currentLoc.directionTo(hqLoc));
					}
				}
				Direction diggingSpotDir = currentLoc.directionTo(diggingSpot);
				Direction depositSpotDir = currentLoc.directionTo(depositSpot);
				if(rc.canDepositDirt(depositSpotDir)) {
					rc.depositDirt(depositSpotDir);
				}else if(rc.canDigDirt(diggingSpotDir)) {
					rc.digDirt(diggingSpotDir);
				}
	    	}
    	}
    	
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
    
    void createWallLocs() throws GameActionException {
		int unadjustedInnerWallLocsLen = unadjustedInnerWallLocs.length;
		for(int i = 0; i <unadjustedInnerWallLocsLen; i++) {
			innerWallLocs[i] = unadjustedInnerWallLocs[i].translate(hqLoc.x, hqLoc.y);
			innerAvaliableWallLocs[i] = unadjustedInnerWallLocs[i].translate(hqLoc.x, hqLoc.y);
		}
		int unadjustedOuterWallLocsLen = unadjustedOuterWallLocs.length;
		for(int i = 0; i <unadjustedOuterWallLocsLen; i++) {
			outerWallLocs[i] = unadjustedOuterWallLocs[i].translate(hqLoc.x, hqLoc.y);
			outerAvaliableWallLocs[i] = unadjustedOuterWallLocs[i].translate(hqLoc.x, hqLoc.y);
		}
		
		for(int i = 0; i<unadjustedDiggingSpots.length;i++) {
			MapLocation testLoc = unadjustedDiggingSpots[i].translate(hqLoc.x, hqLoc.y);
			if(onMap(testLoc)) {
				diggingSpots[i] = testLoc;
			}
		}
	}
    
	void updateWallLocs() throws GameActionException {
		int innerWallLocsLen = innerWallLocs.length;
		for(int i = 0; i <innerWallLocsLen; i++) {
			MapLocation testLoc = innerWallLocs[i];
			if(!onMap(testLoc) || distanceToCorner(testLoc)<=1) {
				innerAvaliableWallLocs[i] = null;
			}else if(rc.canSenseLocation(testLoc)) { //if can be sensed
				if((rc.senseRobotAtLocation(testLoc) == null || rc.getLocation().equals(testLoc))&&!rc.senseFlooding(testLoc)) {
					innerAvaliableWallLocs[i] = testLoc;
				}else {
					innerAvaliableWallLocs[i] = null;
				}
			}
		}
		int outerWallLocsLen = outerWallLocs.length;
		for(int i = 0; i <outerWallLocsLen; i++) {
			MapLocation testLoc = outerWallLocs[i];
			if(!onMap(testLoc) || distanceToCorner(testLoc)<=1) {
				outerAvaliableWallLocs[i] = null;
			}else if(rc.canSenseLocation(testLoc)) { //if can be sensed
				if((rc.senseRobotAtLocation(testLoc) == null || rc.getLocation().equals(testLoc))&&!rc.senseFlooding(testLoc)) {
					outerAvaliableWallLocs[i] = testLoc;
				}else {
					outerAvaliableWallLocs[i] = null;
				}
			}
		}
	}
	
	MapLocation lowestWall() throws GameActionException {
		int smallestWallHeight = 999999;
		MapLocation smallestWall = null;
		int innerWallLocsLen = innerWallLocs.length;
		for(int i = 0; i <innerWallLocsLen; i++) {
			MapLocation testLoc = innerWallLocs[i];
			if(rc.canSenseLocation(testLoc)&&rc.getLocation().isAdjacentTo(testLoc)&& rc.senseElevation(testLoc)<smallestWallHeight && distanceToCorner(testLoc)>1) {
				smallestWallHeight=rc.senseElevation(testLoc);
				smallestWall = testLoc;
			}
		}
		return smallestWall;
	}
    
    MapLocation l(int x, int y) {
		return new MapLocation(x, y);
	}
    
   
}