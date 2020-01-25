package gridbot;
import battlecode.common.*;
import java.util.ArrayList;

public class Miner extends Unit {
	
	static int minerType;
	static final int SOUP_MINER = 1;
	static final int BUILDER_MINER = 2;
	static final int EXPLORER_MINER = 3;
	
	static MapLocation[] soupMarkers = new MapLocation[20];
	static MapLocation closestSoupMarker = null;
	static MapLocation soupLoc = null;
	static MapLocation refineryLoc = null;
	static int totalNearbySoup;
	
	static MapLocation firstSchoolLoc = null;
	static boolean builtFirstSchool = false;

    public Miner(RobotController r) {
        super(r);
    }

    public void takeTurn() throws GameActionException {
        super.takeTurn();
        
		if(minerType == 0) {
			if(rc.getRoundNum() < 6) {
				minerType = BUILDER_MINER;
			}
			else {
				minerType = SOUP_MINER;
			}
		}
        
        soupStuff();
        
        switch (minerType){
        case SOUP_MINER:
        	runSoupMiner();
        	break;
        case BUILDER_MINER:
        	runBuilderMiner();
        	runSoupMiner();
        	break;
        }
    }
    
    void runSoupMiner() throws GameActionException {
		MapLocation currentLoc = rc.getLocation();
		
		RobotInfo nearbyRefineryInfo = nearbyRobot(RobotType.REFINERY, myTeam);
		if(nearbyRefineryInfo != null) {
			refineryLoc = nearbyRefineryInfo.location;
		}
		
		//building refineries
		if(soupLoc != null) {
			boolean closeToSoup = rc.getLocation().distanceSquaredTo(soupLoc) <= 2;
			boolean farFromHQ = soupLoc.distanceSquaredTo(hqLoc) > 16;
			boolean farFromRefinery = (refineryLoc == null || soupLoc.distanceSquaredTo(refineryLoc) > 49);
			boolean enoughSoup = refineryLoc == null || totalNearbySoup >= 1000 || soupLoc.distanceSquaredTo(refineryLoc) > 120;
			if (closeToSoup && 
					farFromHQ && 
					farFromRefinery && 
					enoughSoup) {
				Direction dirToBuild = currentLoc.directionTo(findSpotOnBuildGrid());
				if(dirToBuild!=null) {
					tryBuild(RobotType.REFINERY, dirToBuild);
				}
			}
		}

		// refining and mining
		for (int i = 0; i< Util.allDirsLen; i++)
			tryMine(Util.allDirs[i]);
		for (int i = 0; i< Util.allDirsLen; i++)
			tryRefine(Util.allDirs[i]);
		

		// where to move
		MapLocation desiredLoc = null; // where the miner WANTS to go
		
		if (rc.getSoupCarrying() >= RobotType.MINER.soupLimit) {		// if full capacity find refinery
			if (refineryLoc != null) { // if there is a nearby refinery go there
				desiredLoc = refineryLoc;
			} else { // go the hq
				desiredLoc = hqLoc;
			}
		}else if (soupLoc != null) { // move towards saved soup location
			desiredLoc = soupLoc;
		}else if (closestSoupMarker != null) { // move towards soup marker
			desiredLoc = closestSoupMarker;
		}
//		System.out.println(desiredLoc);

		Direction desiredDir;
		if(desiredLoc != null) {
			desiredDir = currentLoc.directionTo(desiredLoc);
			rc.setIndicatorLine(currentLoc, desiredLoc, 0, 0, 255);
		}else {
			desiredDir = Util.randomDirection();
		}
		nav.noReturnNav(desiredDir);

	}
    
    void runBuilderMiner() throws GameActionException {
    	
    	if(firstSchoolLoc == null) {
    		MapLocation loc = comms.getDesiredSchoolPlacement();
    		if(loc != null) {
    			firstSchoolLoc = loc;
    		}
    	}
    	if(firstSchoolLoc != null) {
    		Direction dirToFirstSchool = rc.getLocation().directionTo(firstSchoolLoc);
			if(!builtFirstSchool && rc.getLocation().isAdjacentTo(firstSchoolLoc) && rc.canBuildRobot(RobotType.DESIGN_SCHOOL, dirToFirstSchool)) {
				rc.buildRobot(RobotType.DESIGN_SCHOOL, dirToFirstSchool);
			}
			
			//checking if the first school has been built
			if(!builtFirstSchool &&
					rc.canSenseLocation(firstSchoolLoc) && 
					rc.senseRobotAtLocation(firstSchoolLoc) != null &&
					rc.senseRobotAtLocation(firstSchoolLoc).getType().equals(RobotType.DESIGN_SCHOOL)) {
				builtFirstSchool = true;
			}
			
			MapLocation desiredLoc = null; // where the miner WANTS to go
			if(!builtFirstSchool) {
				desiredLoc = firstSchoolLoc;
			}
			
			if(desiredLoc != null) {
				nav.noReturnNav(desiredLoc);
			}
    	}
    	
    	
    }
    
    ////////////HELPER METHODS/////////////

    boolean tryMine(Direction dir) throws GameActionException {
        if (rc.canMineSoup(dir)) {
            rc.mineSoup(dir);
            return true;
        }
        return false;
    }

    boolean tryRefine(Direction dir) throws GameActionException {
        if (rc.canDepositSoup(dir)) {
            rc.depositSoup(dir, rc.getSoupCarrying());
            return true;
        }
        return false;
    }
    
    MapLocation findSpotOnBuildGrid() throws GameActionException {
    	for(int i = 0; i<Util.dirsLen;i++) {
    		MapLocation testLoc = rc.getLocation().add(Util.dirs[i]);
    		if(grid.isBuildingSpot(testLoc) && rc.senseRobotAtLocation(testLoc)==null) {
    			return testLoc;
    		}
    	}
    	return null;
    }
    
    ///////////////SOUP METHODS///////////////////////
	void soupStuff() throws GameActionException {
		checkForEmptySoupMarkers();
		updateSoupMarkers();
		MapLocation currentLoc = rc.getLocation();

		MapLocation nearestSoup = nearestSoup();
		if(soupLoc==null) {
			soupLoc = nearestSoup;
		}
		// remove current soup target if the target is empty
		if (soupLoc != null && rc.canSenseLocation(soupLoc) && rc.senseSoup(soupLoc) <= 0) {
			soupLoc = null;
		}
		
		int soupMarkersLen = soupMarkers.length;
		boolean shouldBroadcastSoup = true;
		if(nearestSoup != null && !currentLoc.isWithinDistanceSquared(hqLoc, 49)) { //if found nearby soup and far enough from hq
			for(int i = 0; i<soupMarkersLen; i++) {
				if(soupMarkers[i] != null) { 
					if(soupMarkers[i].distanceSquaredTo(nearestSoup) <= 36) { //if a marker exists nearby the found soup
						shouldBroadcastSoup = false; // do not broadcast to blockchain
					}
					//rc.setIndicatorDot(soupMarkers[i], 0, 255, 0);
				}
			}
			if(shouldBroadcastSoup) {
				int[] m = {comms.M_SOUP_MARKER, nearestSoup.x, nearestSoup.y, Util.rand(), Util.rand(), rc.getID()};
				comms.sendMessage(m, 1);
			}
		}else if(soupLoc == null){ //if no current soup target
			// find nearest soup marker
			int closestSoupMarkerDist = 99999;
			boolean noSoupMarkers = true;
			for(int i = 0; i<soupMarkersLen; i++) {
				// trying to find closest marker
				if(soupMarkers[i] != null) { 
					noSoupMarkers &= false;
					if(soupMarkers[i].distanceSquaredTo(currentLoc) <= closestSoupMarkerDist) {
						closestSoupMarker = soupMarkers[i];
						closestSoupMarkerDist = closestSoupMarker.distanceSquaredTo(currentLoc);
					}
					//rc.setIndicatorDot(soupMarkers[i], 255, 0, 0);
				}
			}
			if(noSoupMarkers) {
				closestSoupMarker = null;
			}
		}

	}
	
	MapLocation nearestSoup() throws GameActionException {
		MapLocation currentLoc = rc.getLocation();
		MapLocation[] nearbySoups = rc.senseNearbySoup();
		MapLocation nearestSoup = null;
		totalNearbySoup = 0;
		int nearestSoupDist = 9999;
		int nearbySoupsLen = nearbySoups.length;
		for(int i = 0; i < nearbySoupsLen; i++) {
			if(!rc.senseFlooding(nearbySoups[i])) {
				totalNearbySoup+=rc.senseSoup(nearbySoups[i]);
				if(currentLoc.distanceSquaredTo(nearbySoups[i])<nearestSoupDist) {
					nearestSoup = nearbySoups[i];
					nearestSoupDist = currentLoc.distanceSquaredTo(nearestSoup);
				}
			}
		}
		return nearestSoup;
	}
	
	void updateSoupMarkers() throws GameActionException {
		MapLocation[] newSoupMarkers = new MapLocation[7];
		int newSoupMarkersIndex = 0;
		
		MapLocation[] removeSoupMarkers = new MapLocation[7];
		int removeSoupMarkersIndex = 0;
		//find all messages with the soup marker tag
		for(int i = 0; i<28; i+=4) {
			if(currentMessages[i] == comms.M_SOUP_MARKER) {
				newSoupMarkers[newSoupMarkersIndex] = new MapLocation(currentMessages[i+1],currentMessages[i+2]);
				//System.out.println(newSoupMarkers[newSoupMarkersIndex]);
				newSoupMarkersIndex++;
			}
			else if(currentMessages[i] == comms.M_REMOVE_SOUP_MARKER) {
				removeSoupMarkers[removeSoupMarkersIndex] = new MapLocation(currentMessages[i+1],currentMessages[i+2]);
				removeSoupMarkersIndex++;
			}
		}
		newSoupMarkersIndex = 0;
		removeSoupMarkersIndex = 0;
		int soupMarkersLen = soupMarkers.length;
		for(int i = 0; i<soupMarkersLen; i++) {
			if(soupMarkers[i] != null) {
				for(int j = 0; j<7; j++) {
					if(removeSoupMarkers[j] != null && soupMarkers[i].equals(removeSoupMarkers[j])) {
						soupMarkers[i] = null;
					}
				}
			}
			if(soupMarkers[i] == null && newSoupMarkersIndex < 7 && newSoupMarkers[newSoupMarkersIndex] != null) {
				soupMarkers[i] = newSoupMarkers[newSoupMarkersIndex];
				//System.out.println(soupMarkers[i]);
				newSoupMarkersIndex++;
			}
		}
//		String soups = "";
//		for(int i = 0; i<soupMarkersLen;i++) {
//			if(soupMarkers[i]!=null)
//				soups+=soupMarkers[i].x+","+soupMarkers[i].y+" ";
//		}
//		System.out.println(soups);
		for(int i = 0; i<soupMarkersLen; i++) {
			if(soupMarkers[i] != null) {
				rc.setIndicatorDot(soupMarkers[i], 0, 0, 255);
			}
		}
	}
	
	void checkForEmptySoupMarkers() throws GameActionException {
		if(closestSoupMarker == null) {
			return;
		}
		if(rc.getLocation().isWithinDistanceSquared(closestSoupMarker, 5) && totalNearbySoup < 200) {
			int[] m = {comms.M_REMOVE_SOUP_MARKER, closestSoupMarker.x, closestSoupMarker.y, Util.rand(), Util.rand(), rc.getID()};
			closestSoupMarker = null;
			comms.sendMessage(m, 1);
		}
	}

}
