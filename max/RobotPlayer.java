package max;

import battlecode.common.*;

public strictfp class RobotPlayer {
	// general
	static RobotController rc;

	static Direction[] directions = { Direction.NORTH, Direction.NORTHEAST, Direction.EAST, Direction.SOUTHEAST,
			Direction.SOUTH, Direction.SOUTHWEST, Direction.WEST, Direction.NORTHWEST };
	static int dirsLen = directions.length;
	static Direction[] allDirs = Direction.allDirections();
	static int allDirsLen = allDirs.length;
	static RobotType[] spawnedByMiner = { RobotType.REFINERY, RobotType.VAPORATOR, RobotType.DESIGN_SCHOOL,
			RobotType.FULFILLMENT_CENTER, RobotType.NET_GUN };

	static int turnCount;

	static final int KEY = 17;
	
	static int[] currentMessages;
	
	static Team myTeam;
	static Team opponent;
	
	static MapLocation[] possibleEnemyHQ = {null,null,null};
	static MapLocation hqLoc;
	
	//messages
	static final int M_HQ_LOC = 384;
	static final int M_SOUP_MARKER = 804;
	static final int M_REMOVE_SOUP_MARKER = 947;
	static final int M_BUILD_SCHOOL = 283;
	
	//map size
	static int minX;
	static int minY;
	static int maxX;
	static int maxY;
	
	// HQ
	static int numMiners = 0;
	static boolean builtRefinery = false;
	static boolean builtBuilderMiner = false;

	// MINER
	static int minerType;
	static final int SOUP_MINER = 1;
	static final int BUILDER_MINER = 2;
	static final int EXPLORER_MINER = 3;
	
	static MapLocation[] soupMarkers = new MapLocation[20];
	static MapLocation closestSoupMarker = null;
	static MapLocation soupLoc = null;
	static MapLocation refineryLoc = null;
	static int totalNearbySoup;
	
	static MapLocation schoolLoc = null;
	static boolean builtSchool = false;
	
	// REFINERY

	// VAPORATOR

	// DESIGN_SCHOOL

	// FULFILLMENT_CENTER

	// LANDSCAPER

	// DELIVERY DRONE

	// NET_GUN

	/**
	 * run() is the method that is called when a robot is instantiated in the
	 * Battlecode world. If this method returns, the robot dies!
	 **/
	@SuppressWarnings("unused")
	public static void run(RobotController rc) throws GameActionException {

		// This is the RobotController object. You use it to perform actions from this
		// robot,
		// and to get information on its current status.
		RobotPlayer.rc = rc;
		
		myTeam = rc.getTeam();
		opponent = myTeam.opponent();

		turnCount = 0;

		minX = 0;
		minY = 0;
		maxX = rc.getMapWidth();
		maxY = rc.getMapHeight();
		while (true) {
			turnCount += 1;
			if(rc.getRoundNum() > 1)
				currentMessages = getMessages();
			try {
				switch (rc.getType()) {
				case HQ:
					runHQ();
					break;
				case MINER:
					runMiner();
					break;
				case REFINERY:
					runRefinery();
					break;
				case VAPORATOR:
					runVaporator();
					break;
				case DESIGN_SCHOOL:
					runDesignSchool();
					break;
				case FULFILLMENT_CENTER:
					runFulfillmentCenter();
					break;
				case LANDSCAPER:
					runLandscaper();
					break;
				case DELIVERY_DRONE:
					runDeliveryDrone();
					break;
				case NET_GUN:
					runNetGun();
					break;
				}
				Clock.yield();

			} catch (Exception e) {
				System.out.println(rc.getType() + " Exception");
				e.printStackTrace();
			}
		}
	}

	static void runHQ() throws GameActionException {
		hqLoc = rc.getLocation();
		if(rc.getRoundNum() == 1) {
			int[] m = {M_HQ_LOC, hqLoc.x, hqLoc.y, rand(),rand(),rand()};
			sendMessage(m, 1);
		}
		if (numMiners < 5) {
			for (Direction dir : directions)
				if (tryBuild(RobotType.MINER, dir)) {
					numMiners++;
				}
		}
		RobotInfo school = nearbyRobot(RobotType.DESIGN_SCHOOL, myTeam);
		if(rc.getTeamSoup()>190 && school == null) {//if enough soup and no nearby school
			MapLocation desiredSchoolLoc = null;
			for(int i = 0; i<dirsLen; i+=2) {
				MapLocation testLoc = hqLoc.add(directions[i]).add(directions[i]);
				if(rc.onTheMap(testLoc)) {
					desiredSchoolLoc = testLoc;
					break;
				}
			}
			if(desiredSchoolLoc!= null) {
				int[] m = {M_BUILD_SCHOOL, desiredSchoolLoc.x, desiredSchoolLoc.y, rand(), rand(), rand()};
				sendMessage(m, 1);
			}
		}
	}
	
	static void runMiner() throws GameActionException {
		findHQ();
		soupStuff();
		
		if(minerType == 0) {
			if(rc.getRoundNum() < 3) {
				potentialEnemyHQ();
				minerType = EXPLORER_MINER;
			}
			else {
				minerType = SOUP_MINER;
			}
		}
		switch(minerType) {
		case SOUP_MINER:
			runSoupMiner();
			break;
		case BUILDER_MINER:
			runBuilderMiner();
			break;
		case EXPLORER_MINER:
			runExplorerMiner();
			break;
		}
			
	}
	
	static void runSoupMiner() throws GameActionException {

		MapLocation currentLoc = rc.getLocation();
		
		RobotInfo nearbyRefineryInfo = nearbyRobot(RobotType.REFINERY, myTeam);
		if(nearbyRefineryInfo != null) {
			refineryLoc = nearbyRefineryInfo.location;
		}
		
		//building refineries
		if(soupLoc != null) {
			boolean closeToSoup = rc.getLocation().distanceSquaredTo(soupLoc) <= 2;
			boolean farFromHQ = soupLoc.distanceSquaredTo(hqLoc) > 49;
			boolean farFromRefinery = (refineryLoc == null || soupLoc.distanceSquaredTo(refineryLoc) > 49);
			boolean enoughSoup = refineryLoc == null || totalNearbySoup >= 1000 || soupLoc.distanceSquaredTo(refineryLoc) > 100;
			if (closeToSoup && 
					farFromHQ && 
					farFromRefinery && 
					enoughSoup) {
				Direction dirToSoup = currentLoc.directionTo(soupLoc);
				tryBuild(RobotType.REFINERY, dirToSoup);
			}
		}
		
		int[] m = checkForMessage(M_BUILD_SCHOOL);
		if(m!= null) {
			schoolLoc=new MapLocation(m[0],m[1]);
		}
		
		if(schoolLoc!= null && currentLoc.isWithinDistanceSquared(schoolLoc, 2)) { //if adjacent to desired school location
			tryBuild(RobotType.DESIGN_SCHOOL, currentLoc.directionTo(schoolLoc));
		}

		// refining and mining
		for (int i = 0; i< allDirsLen; i++)
			tryMine(allDirs[i]);
		for (int i = 0; i< allDirsLen; i++)
			tryRefine(allDirs[i]);
		

		// where to move
		MapLocation desiredLoc = null; // where the miner WANTS to go
		
		RobotInfo nearbySchool = nearbyRobot(RobotType.DESIGN_SCHOOL, myTeam);		
		if(nearbySchool != null) {
			builtSchool |= true;
		}
		
		if(schoolLoc != null && !builtSchool) {
			desiredLoc = schoolLoc;
		}else if (rc.getSoupCarrying() >= RobotType.MINER.soupLimit) {		// if full capacity find refinery
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
//		System.out.println(closestSoupMarker);
		rc.setIndicatorLine(currentLoc, desiredLoc, 0, 0, 255);
		Direction desiredDir;
		if(desiredLoc != null) {
			desiredDir = currentLoc.directionTo(desiredLoc);
		}else {
			desiredDir = randomDirection();
		}
		if(safeToMove(desiredDir)) {
			rc.move(desiredDir);
		} else {
			// bug pathfinding
			desiredDir = bugPathing3(desiredDir);
			if(desiredDir != null) {
				rc.move(desiredDir);
			}
		}

	}

	static void runExplorerMiner() throws GameActionException {
		MapLocation currentLoc = rc.getLocation();
		
		MapLocation desiredLoc = null;
		int i = 0;
		for(i = 0; i<possibleEnemyHQ.length;i++) {
			if(possibleEnemyHQ[i] != null) {
				desiredLoc = possibleEnemyHQ[i];
				break;
			}
		}		
		if(rc.canSenseLocation(desiredLoc)) {
			possibleEnemyHQ[i] = null;
		}
		
		RobotInfo enemyHQ = nearbyRobot(RobotType.HQ, opponent);
		
		//if no locations left to explore or found enemy hq
		if(desiredLoc == null || enemyHQ != null)  {
			minerType = SOUP_MINER;
			runSoupMiner();
		}
		
		Direction desiredDir = currentLoc.directionTo(desiredLoc);
		if(safeToMove(desiredDir)) {
			rc.move(desiredDir);
		} else {
			// bug pathfinding
			desiredDir = bugPathing3(desiredDir);
			if(desiredDir != null) {
				rc.move(desiredDir);
			}
		}
		
	}
	
	static void runBuilderMiner() throws GameActionException {
		findHQ();
	}

	static void runRefinery() throws GameActionException {

	}

	static void runVaporator() throws GameActionException {

	}

	static void runDesignSchool() throws GameActionException {
		
	}

	static void runFulfillmentCenter() throws GameActionException {
		
	}

	static void runLandscaper() throws GameActionException {
		findHQ();
		
	}

	static void runDeliveryDrone() throws GameActionException {
		findHQ();
		Direction dirToHQ = rc.getLocation().directionTo(hqLoc);
		Direction des = dirToHQ; 
			if (rc.canMove(des))
				rc.move(des);
			else {
				des = bugPathing(des);
				if (des != null) {
					rc.move(des);
				}
			}
		
	}

	static void runNetGun() throws GameActionException {

	}

	static void findHQ() throws GameActionException {
		if (hqLoc == null) {
			RobotInfo[] nearbyBots = rc.senseNearbyRobots();
			for (RobotInfo bot : nearbyBots) {
				if (bot.type == RobotType.HQ && bot.team == rc.getTeam()) {
					hqLoc = bot.location;
				}
			}
		}
	}

	/**
	 * Returns a random Direction.
	 *
	 * @return a random Direction
	 */
	static Direction randomDirection() {
		return directions[(int) (Math.random() * dirsLen)];
	}

	static boolean tryMove() throws GameActionException {
		for (Direction dir : directions)
			if (tryMove(dir))
				return true;
		return false;
	}

	/**
	 * Attempts to move in a given direction.
	 *
	 * @param dir The intended direction of movement
	 * @return true if a move was performed
	 * @throws GameActionException
	 */
	static boolean tryMove(Direction dir) throws GameActionException {
		if (rc.canMove(dir)) {
			rc.move(dir);
			return true;
		}
		return false;
	}

	/**
	 * Attempts to build a given robot in a given direction.
	 *
	 * @param type The type of the robot to build
	 * @param dir  The intended direction of movement
	 * @return true if a move was performed
	 * @throws GameActionException
	 */
	static boolean tryBuild(RobotType type, Direction dir) throws GameActionException {
		if (rc.canBuildRobot(type, dir)) {
			rc.buildRobot(type, dir);
			return true;
		}
		return false;
	}

	/**
	 * Attempts to mine soup in a given direction.
	 *
	 * @param dir The intended direction of mining
	 * @return true if a move was performed
	 * @throws GameActionException
	 */
	static boolean tryMine(Direction dir) throws GameActionException {
		if (rc.canMineSoup(dir) && rc.getSoupCarrying() < RobotType.MINER.soupLimit) {
			rc.mineSoup(dir);
			return true;
		}
		return false;
	}

	/**
	 * Attempts to refine soup in a given direction.
	 *
	 * @param dir The intended direction of refining
	 * @return true if a move was performed
	 * @throws GameActionException
	 */
	static boolean tryRefine(Direction dir) throws GameActionException {
		if(!rc.onTheMap(rc.getLocation().add(dir))) return false;
		RobotInfo bot = rc.senseRobotAtLocation(rc.getLocation().add(dir));
		if (rc.canDepositSoup(dir) && bot!= null && bot.getTeam().equals(myTeam)) {
			rc.depositSoup(dir, rc.getSoupCarrying());
			return true;
		} 
		return false;
	}

	/**
	 * 
	 * @return the current radius in tiles
	 * @throws GameActionException
	 */
	static int radiusInTiles() throws GameActionException {
		int radiusInTiles = 1;
		while (rc.canSenseRadiusSquared(radiusInTiles * radiusInTiles)) {
			radiusInTiles++;
		}
		radiusInTiles--;
		return radiusInTiles;
	}

	static void soupStuff() throws GameActionException {
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
				int[] m = {M_SOUP_MARKER, nearestSoup.x, nearestSoup.y, rand(), rand(), rc.getID()};
				sendMessage(m, 1);
			}
		}else if(soupLoc == null){ //if no current soup target
			// find nearest soup marker
			int closestSoupMarkerDist = 99999;
			for(int i = 0; i<soupMarkersLen; i++) {
				// trying to find closest marker
				if(soupMarkers[i] != null) { 
					if(soupMarkers[i].distanceSquaredTo(currentLoc) <= closestSoupMarkerDist) {
						closestSoupMarker = soupMarkers[i];
						closestSoupMarkerDist = closestSoupMarker.distanceSquaredTo(currentLoc);
					}
					//rc.setIndicatorDot(soupMarkers[i], 255, 0, 0);
				}
			}
		}

	}
	
	static MapLocation nearestSoup() throws GameActionException {
		MapLocation currentLoc = rc.getLocation();
		MapLocation[] nearbySoups = rc.senseNearbySoup();
		MapLocation nearestSoup = null;
		totalNearbySoup = 0;
		int nearestSoupDist = 9999;
		int nearbySoupsLen = nearbySoups.length;
		for(int i = 0; i < nearbySoupsLen; i++) {
			totalNearbySoup+=rc.senseSoup(nearbySoups[i]);
			if(currentLoc.distanceSquaredTo(nearbySoups[i])<nearestSoupDist) {
				nearestSoup = nearbySoups[i];
				nearestSoupDist = currentLoc.distanceSquaredTo(nearestSoup);
			}
		}
		return nearestSoup;
	}
	
	static void updateSoupMarkers() throws GameActionException {
		MapLocation[] newSoupMarkers = new MapLocation[7];
		int newSoupMarkersIndex = 0;
		
		MapLocation[] removeSoupMarkers = new MapLocation[7];
		int removeSoupMarkersIndex = 0;
		//find all messages with the soup marker tag
		for(int i = 0; i<28; i+=4) {
			if(currentMessages[i] == M_SOUP_MARKER) {
				newSoupMarkers[newSoupMarkersIndex] = new MapLocation(currentMessages[i+1],currentMessages[i+2]);
				//System.out.println(newSoupMarkers[newSoupMarkersIndex]);
				newSoupMarkersIndex++;
			}
			else if(currentMessages[i] == M_REMOVE_SOUP_MARKER) {
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
	
	static void checkForEmptySoupMarkers() throws GameActionException {
		if(closestSoupMarker == null) {
			return;
		}
		if(rc.getLocation().isWithinDistanceSquared(closestSoupMarker, 5) && totalNearbySoup < 200) {
			int[] m = {M_REMOVE_SOUP_MARKER, closestSoupMarker.x, closestSoupMarker.y, rand(),rand(), rc.getID()};
			closestSoupMarker = null;
			sendMessage(m, 1);
		}
	}

	static RobotInfo nearbyRobot(RobotType target, Team team) throws GameActionException {
		RobotInfo[] robots = rc.senseNearbyRobots();
		for (RobotInfo r : robots) {
			if (r.getType() == target && r.team == team) {
				return r;
			}
		}
		return null;
	}
	
	static Direction bugPathing(Direction desiredDir) throws GameActionException {
		if(safeToMove(desiredDir.rotateRight())) {
			return desiredDir.rotateRight();
		}
		else if(safeToMove(desiredDir.rotateRight().rotateRight())) {
			return desiredDir.rotateRight().rotateRight();
		}
		else if(safeToMove(desiredDir.rotateRight().rotateRight().rotateRight())) {
			return desiredDir.rotateRight().rotateRight().rotateRight();
		}
		else {
			return null;
		}
		
	}
	
	static Direction bugPathing2(Direction desiredDir) throws GameActionException {
		if(safeToMove(desiredDir.rotateRight())) {
			return desiredDir.rotateRight();
		}
		else if(safeToMove(desiredDir.rotateRight().rotateRight())) {
			return desiredDir.rotateRight().rotateRight();
		}
		else {
			return null;
		}
		
	}
	
	static Direction bugPathing3(Direction dir) throws GameActionException {
		//Direction[] toTry = {dir, dir.rotateLeft(), dir.rotateLeft().rotateLeft(), dir.rotateRight(), dir.rotateRight().rotateRight()};
		Direction[] toTry = {dir, dir.rotateLeft(), dir.rotateLeft().rotateLeft()};
		int len = toTry.length;
		for(int i = 0; i<len; i++) {
			if(safeToMove(toTry[i])) {
				return toTry[i];
			}
		}
		return null;
	}
	
	static boolean safeToMove(Direction dir) throws GameActionException {
		if(rc.canMove(dir) && !rc.senseFlooding(rc.getLocation().add(dir))) {
			return true;
		}
		return false;
	}
	
	static int[] getMessages() throws GameActionException {
		return getMessages(rc.getRoundNum()-1);
	}
	
	static int[] getMessages(int roundNum) throws GameActionException {
		int[] messages = new int[42]; //6 per message 7 messages
		Transaction[] transactions = rc.getBlock(roundNum);
		int len = transactions.length;
		for(int i = 0; i < len; i++) {
			if(transactions[i]==null)
				return messages;
			int[] m = transactions[i].getMessage();
			//check if its our message
			int divisor = m[6]*KEY;
			boolean ourMessage = true;
			for(int j = 0; j<6; j++) {
				if(m[j]%divisor!=0) {
					ourMessage = false;
					break;
				}
			}
			//if our message add to messages array
			if(ourMessage) {
				for(int j = 0; j<6; j++) {
					messages[i*6+j] = m[j]/divisor;
				}
			}
		}
		return messages;
	}
		
	static boolean sendMessage(int[] m, int cost) throws GameActionException {
		int encoder = rand();
		// encode message
		for(int i = 0; i<6; i++) {
			m[i] = m[i]* KEY*encoder;
		}
		int[] message = {m[0],m[1],m[2],m[3],m[4],m[5],encoder};
		if(rc.canSubmitTransaction(message, cost)) {
			rc.submitTransaction(message, cost);
			return true;
		}
		return false;
	}
	
	static int[] checkForMessage(int tag) throws GameActionException {
		int[] m = getMessages();
		for(int i = 0; i<m.length; i+=6) {
			if(m[i] == tag) {
				int[] ret = {m[i+1],m[i+2],m[i+3],m[i+4],m[i+5]};
				return ret;
			}
		}
		return null;
	}
	
	static boolean nextToBorder() throws GameActionException {
		for(int i = 0; i<dirsLen;i++) {
			if(rc.onTheMap(rc.adjacentLocation(directions[i]))) {
				return true;
			}
		}
		return false;
	}
	
	static MapLocation randomLoc() throws GameActionException {
		return new MapLocation((int)(Math.random()*maxX), (int)(Math.random()*maxY));
	}
	
	static int rand(int max) throws GameActionException {
		return (int)(Math.random()*max);
	}

	static int rand() throws GameActionException {
		return (int)(Math.random()*500)+1;
	}

	static void potentialEnemyHQ() throws GameActionException {
		MapLocation mp = new MapLocation(maxX/2, maxY/2);
			
		if(hqLoc.x < mp.x) {
			if(hqLoc.y < mp.y) { //bottom left
				possibleEnemyHQ[2] = l(hqLoc.x, maxY-hqLoc.y);
				possibleEnemyHQ[1] = l(maxX-hqLoc.x, maxY-hqLoc.y);
				possibleEnemyHQ[0] = l(maxX-hqLoc.x, hqLoc.y);
			}else { //top left
				possibleEnemyHQ[2] = l(maxX-hqLoc.x, hqLoc.y);
				possibleEnemyHQ[1] = l(maxX-hqLoc.x, maxY-hqLoc.y);
				possibleEnemyHQ[0] = l(hqLoc.x, maxY-hqLoc.y);
			}
		}else {
			if(hqLoc.y < mp.y) { //bottom right
				possibleEnemyHQ[2] = l(maxX-hqLoc.x, hqLoc.y);
				possibleEnemyHQ[1] = l(maxX-hqLoc.x, maxY-hqLoc.y);
				possibleEnemyHQ[0] = l(hqLoc.x, maxY-hqLoc.y);
			}else { //top right
				possibleEnemyHQ[2] = l(hqLoc.x, maxY-hqLoc.y);
				possibleEnemyHQ[1] = l(maxX-hqLoc.x, maxY-hqLoc.y);
				possibleEnemyHQ[0] = l(maxX-hqLoc.x, hqLoc.y);
			}
		}
		
	}
	
	static MapLocation l(int x, int y) {
		return new MapLocation(x, y);
	}
	

}
