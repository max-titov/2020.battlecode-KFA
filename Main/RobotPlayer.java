package Main;

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
	static MapLocation enemyHQLoc;
	static int mapCorner;
	static final int QUADRANT1 = 1;
	static final int QUADRANT2 = 2;
	static final int QUADRANT3 = 3;
	static final int QUADRANT4 = 4;
	
	//navigation
	static int preLocsSize = 5;
	static MapLocation[] prevLocs = new MapLocation[preLocsSize];
	static int prevLocsIndex = 0;
	
	//messages
	static final int M_HQ_LOC = 384;
	static final int M_SOUP_MARKER = 804;
	static final int M_REMOVE_SOUP_MARKER = 947;
	static final int M_BUILD_SCHOOL = 283;
	static final int M_FOUND_ENEMY_HQ = 732;
	
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
	static boolean builtWall = false;
	
	// REFINERY

	// VAPORATOR

	// DESIGN_SCHOOL
	static int landscaperCount = 0;

	// FULFILLMENT_CENTER
	static Direction[] directionsToBuild = null;
	static int directionIndex;
	
	// LANDSCAPER
	static final MapLocation[] hqCircles = {l(1,1),l(1,0),l(1,-1),l(0,-1),l(-1,-1),l(-1,0),l(-1,1),l(0,1),
			l(-2,-1),l(-2,1),l(-2,2),l(-1,2),l(1,2),l(2,2),l(2,1),l(2,-1),l(2,-2),l(1,-2),l(-1,-2),l(-2,-2)};
//	static final MapLocation[] hqCircles = {l(-1,0),l(-1,1),l(0,1),l(1,1),l(1,0),l(1,-1),l(0,-1),l(-1,-1),
//			l(-2,-1),l(-2,0),l(-2,1),l(-2,2),l(-1,2),l(0,2),l(1,2),l(2,2),l(2,1),l(2,0),l(2,-1),l(2,-2),l(1,-2),l(0,-2),l(-1,-2),l(-2,-2)};
	static MapLocation[] wallLocs = new MapLocation[hqCircles.length];
	static MapLocation[] avaliableWallLocs = new MapLocation[hqCircles.length];
	static MapLocation desiredWallLoc = null;
	static final MapLocation[] unadjustedDiggingSpots = {l(-2,0),l(0,2),l(2,0),l(0,-2),l(-3,3),l(3,3),l(3,-3),l(-3,-3)};
	static MapLocation[] diggingSpots = new MapLocation[unadjustedDiggingSpots.length];
	// DELIVERY DRONE
	static final int SCOUT_DRONE = 1;
	static final int DEFENSE_DRONE = 2;
	static final int ATTACK_DRONE = 3;
	static int droneType;
	static Direction heading;
	static MapLocation targetLoc;
	static boolean readyAttack;
	static boolean readyDefense;
	static MapLocation fulfillmentLoc;
	static MapLocation[] defenseCircleCoords;
	static int defenseIndex;
	
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
		for (RobotInfo ri : rc.senseNearbyRobots(GameConstants.NET_GUN_SHOOT_RADIUS_SQUARED, rc.getTeam().opponent())) {
			if (rc.canShootUnit(ri.getID())) {
				rc.shootUnit(ri.getID());
			}
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
				MapLocation testLoc = hqLoc.add(directions[i]).add(directions[i]).add(directions[i]);
				if(rc.onTheMap(testLoc) && !rc.senseFlooding(testLoc) && Math.abs(rc.senseElevation(testLoc)-rc.senseElevation(hqLoc))<=9) {
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
			boolean farFromHQ = soupLoc.distanceSquaredTo(hqLoc) > 16;
			boolean farFromRefinery = (refineryLoc == null || soupLoc.distanceSquaredTo(refineryLoc) > 49);
			boolean enoughSoup = refineryLoc == null || totalNearbySoup >= 1000 || soupLoc.distanceSquaredTo(refineryLoc) > 120;
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
		if(!builtWall) {
			builtWall = nearbyBotCount(RobotType.LANDSCAPER)>=8;
		}
		
		if(schoolLoc != null && !builtSchool) {
			desiredLoc = schoolLoc;
		}else if (rc.getSoupCarrying() >= RobotType.MINER.soupLimit) {		// if full capacity find refinery
			if (refineryLoc != null) { // if there is a nearby refinery go there
				desiredLoc = refineryLoc;
			} else if(!builtWall){ // go the hq
				desiredLoc = hqLoc;
			} else if(closestSoupMarker != null) {
				desiredLoc = closestSoupMarker;
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
			desiredDir = randomDirection();
		}
		if(schoolLoc== null || !currentLoc.add(desiredDir).equals(schoolLoc)) {
			bugPathing4(desiredDir);
			//old movement
//			if(safeToMove(desiredDir)) {
//				rc.move(desiredDir);
//			} else {
//				// bug pathfinding
//				desiredDir = bugPathing3(desiredDir);
//				if(desiredDir != null) {
//					rc.move(desiredDir);
//				}
//			}
		} else {
			tryMove();
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
		bugPathing4(desiredDir);
		
	}
	
	static void runBuilderMiner() throws GameActionException {
		findHQ();
	}

	static void runRefinery() throws GameActionException {

	}

	static void runVaporator() throws GameActionException {

	}

	static void runDesignSchool() throws GameActionException {
		findHQ();
		Direction desiredDir = rc.getLocation().directionTo(hqLoc).rotateLeft();
		if(landscaperCount < 21 && rc.getTeamSoup() >210) {
			for(int i = 0; i < dirsLen; i++) {
				if(tryBuild(RobotType.LANDSCAPER, desiredDir)) {
					landscaperCount++;
					break;
				}else {
					desiredDir = desiredDir.rotateRight();
				}
			}
		}
	}

	static void runFulfillmentCenter() throws GameActionException {
		findHQ();
		findCorner();
		if(directionsToBuild == null) {
			directionsToBuild = new Direction[3];
			switch (mapCorner) {
			case QUADRANT1:
				directionsToBuild[0] = Direction.WEST;
				directionsToBuild[1] = Direction.SOUTHWEST;
				directionsToBuild[2] = Direction.SOUTH;
				break;
			case QUADRANT2:
				directionsToBuild[0] = Direction.EAST;
				directionsToBuild[1] = Direction.SOUTHEAST;
				directionsToBuild[2] = Direction.SOUTH;
				break;
			case QUADRANT3:
				directionsToBuild[0] = Direction.NORTH;
				directionsToBuild[1] = Direction.NORTHEAST;
				directionsToBuild[2] = Direction.EAST;
				break;
			case QUADRANT4:
				directionsToBuild[0] = Direction.WEST;
				directionsToBuild[1] = Direction.NORTHWEST;
				directionsToBuild[2] = Direction.NORTH;
				break;

			}
		}
		if(tryBuild(RobotType.DELIVERY_DRONE, directionsToBuild[directionIndex])) {
			directionIndex = directionIndex<2 ? directionIndex+1 : 0;
		}
	}

	static void runLandscaper() throws GameActionException {
		findHQ();
		
		MapLocation currentLoc = rc.getLocation();
		
		if(wallLocs[0]==null) {
			createWallLocs();
		}
		
		updateWallLocs();

		for(int i = 0; i<avaliableWallLocs.length; i++) {
			if(avaliableWallLocs[i]!=null) {
				desiredWallLoc = avaliableWallLocs[i];
				break;
			}
		}
		
		if(desiredWallLoc!=null&&desiredWallLoc.equals(currentLoc)) {
			//start digging
			MapLocation diggingSpot = currentLoc;
			for(int i = 0; i<diggingSpots.length; i++) {
				if(currentLoc.isAdjacentTo(diggingSpots[i])) {
					diggingSpot = diggingSpots[i];
					break;
				}
			}
			MapLocation depositSpot = currentLoc;
			if(currentLoc.isAdjacentTo(hqLoc)) {//inner landscaper
				if(nearbyBotCount(RobotType.LANDSCAPER)>10) {
					depositSpot = lowestWall();
					if(depositSpot == null)depositSpot= currentLoc;
				}
			}else {//outer landscaper
				if(GameConstants.getWaterLevel(rc.getRoundNum())+2>rc.senseElevation(currentLoc)) { //if water level is close
					
				}else {
					depositSpot = lowestWall();
					if(depositSpot == null)depositSpot= currentLoc;
//					depositSpot = currentLoc.add(currentLoc.directionTo(hqLoc));
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

		
		Direction desiredDir;
		if(desiredWallLoc != null) {
			desiredDir = currentLoc.directionTo(desiredWallLoc);
			rc.setIndicatorLine(currentLoc, desiredWallLoc, 0, 0, 255);
		}else {
			desiredDir = currentLoc.directionTo(hqLoc);
		}
		bugPathing4(desiredDir);
	}

	static void runDeliveryDrone() throws GameActionException {
		findHQ();
		int[] messages = getMessages();
		for(int i = 0; i<messages.length; i+=6) {
			if(messages[i] == M_FOUND_ENEMY_HQ) {
				enemyHQLoc = new MapLocation(messages[i+1],messages[i+2]);
			}
		}
		//TODO: Check if defense is ready from block chain
		if (droneType == 0) {
			if (!readyDefense){
				droneType = DEFENSE_DRONE;
			}
			else {
				droneType = ATTACK_DRONE;
			}
		}
		switch (droneType) {
		case DEFENSE_DRONE:
			runDefenseDeliveryDrone();
			break;
		case ATTACK_DRONE:
			runAttackDeliveryDrone();
			break;
		}
	}

	static void runDefenseDeliveryDrone() throws GameActionException {
		MapLocation currentLoc = rc.getLocation();
		findDefenseCircleCoords();
		MapLocation desiredLoc = defenseCircleCoords[defenseIndex];
		while(desiredLoc.x < 0 || desiredLoc.y < 0 ||desiredLoc.x > rc.getMapWidth() || desiredLoc.y > rc.getMapHeight()) {
			desiredLoc = defenseCircleCoords[++defenseIndex];
		}
		Direction desiredDir = currentLoc.directionTo(desiredLoc);
		if(!currentLoc.equals(desiredLoc)) {
			if(!tryMove(desiredDir)) {
				if(currentLoc.isAdjacentTo(desiredLoc) && rc.getCurrentSensorRadiusSquared() > 2) {
					RobotInfo occupiedRobot = rc.senseRobotAtLocation(desiredLoc);
					if(occupiedRobot.getType() == rc.getType() && occupiedRobot.getTeam() == rc.getTeam()) {
						desiredLoc = defenseCircleCoords[++defenseIndex];
					}
					if(defenseIndex >= defenseCircleCoords.length) {
						readyDefense = true;
						//TODO:Broadcast to block chain that defense is ready
					}
				}
				else {
					desiredDir = bugPathing3(desiredDir);
					if(desiredDir != null) {
						tryMove(desiredDir);
					}
				}
			}
		}
	}

	static void runAttackDeliveryDrone() throws GameActionException {
	}

	static void runNetGun() throws GameActionException {

	}

	static void findHQ() throws GameActionException { //Tries to find HQ location
		if(hqLoc == null) {
			for (RobotInfo bot : rc.senseNearbyRobots()) {
				if(bot.type == RobotType.HQ && bot.team == rc.getTeam()) {
					hqLoc = bot.location;
					return;
				}
			}
			int[] messages = getMessages(1);
			for(int i = 0; i<messages.length; i+=6) {
				if(messages[i] == M_HQ_LOC) {
					hqLoc= new MapLocation(messages[i+1],messages[i+2]);
				}
			}
		}
	}

	static void findCorner() throws GameActionException {
		int myX = hqLoc.x;
		int myY = hqLoc.y;
		boolean mapHeightLarger = rc.getMapHeight()/2 > myY;
		boolean mapWidthLarger = rc.getMapWidth()/2 > myX;
		if(mapHeightLarger) {
			if(mapWidthLarger) {
				mapCorner = QUADRANT3;
			}
			else {
				mapCorner = QUADRANT4;
			}
		}
		else {
			if(mapWidthLarger) {
				mapCorner = QUADRANT2;
			}
			else {
				mapCorner = QUADRANT1;
			}
		}
	}

	static void findFulfillmentCenter() throws GameActionException {
		if(fulfillmentLoc == null) {
			for (RobotInfo bot : rc.senseNearbyRobots()) {
				if(bot.type == RobotType.FULFILLMENT_CENTER && bot.team == rc.getTeam()) {
					fulfillmentLoc = bot.location;
				}
			}
		}
	}

	static void findScoutHeading() throws GameActionException {
		if(hqLoc != null && heading == null) {
			int myX = rc.getLocation().x;
			int myY = rc.getLocation().y;
			boolean xCheck = myX == fulfillmentLoc.x;
			boolean yCheck = myY == fulfillmentLoc.y;
			boolean xGreaterCheck = myX > fulfillmentLoc.x;
			boolean yGreaterCheck = myY > fulfillmentLoc.y;
			if(xGreaterCheck) {
				if(yGreaterCheck) {
					heading = Direction.NORTHEAST;
				}
				else if (yCheck) {
					heading = Direction.EAST;
				}
				else {
					heading = Direction.SOUTHEAST;
				}
			}
			else if(xCheck) {
				if(yGreaterCheck) {
					heading = Direction.NORTH;
				}
				else {
					heading = Direction.SOUTH;
				}
			}
			else {
				if(yGreaterCheck) {
					heading = Direction.NORTHWEST;
				}
				else if (yCheck) {
					heading = Direction.WEST;
				}
				else {
					heading = Direction.SOUTHWEST;
				}
			}
		}
	}

	static void findTargetLocation() throws GameActionException {
		int mapHeight = rc.getMapHeight();
		int mapWidth = rc.getMapWidth();
		int myHQX = hqLoc.x;
		int myHQY = hqLoc.y;
		int xDiff = mapWidth-myHQX-1;
		int yDiff = mapHeight-myHQY-1;
		if(targetLoc == null) {
			switch (heading) {
			case NORTH:
			case SOUTH:
				targetLoc = new MapLocation(myHQX, yDiff);
				break;
			case EAST:
			case WEST:
				targetLoc = new MapLocation(xDiff, myHQY);
				break;
			case NORTHEAST:
			case NORTHWEST:
			case SOUTHEAST:
			case SOUTHWEST:
				targetLoc = new MapLocation(xDiff, yDiff);
				break;
			}
		}
	}

	static void findDefenseCircleCoords() throws GameActionException {
		if (defenseCircleCoords == null){
			defenseCircleCoords = new MapLocation[16];
			int index = 0;
			for(int i = -3; i <= 3; i++) {
				for(int j = -3; j <= 3; j++) {
					if(Math.abs(i) == 3 || Math.abs(j) == 3) {
						defenseCircleCoords[index++] = new MapLocation(hqLoc.x+i, hqLoc.y+j);
					}
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
	
	static MapLocation nearestSoup() throws GameActionException {
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
		Direction[] toTry = {dir, dir.rotateLeft(), dir.rotateRight(), dir.rotateLeft().rotateLeft(), dir.rotateLeft().rotateLeft().rotateLeft()};
		int len = toTry.length;
		for(int i = 0; i<len; i++) {
			if(safeToMove(toTry[i])) {
				return toTry[i];
			}
		}
		return null;
	}
	
	static void bugPathing4(Direction dir) throws GameActionException {
		MapLocation currentLoc = rc.getLocation();
		Direction[] toTry = {dir, dir.rotateLeft(), dir.rotateRight(), dir.rotateLeft().rotateLeft(), dir.rotateRight().rotateRight(), dir.rotateLeft().rotateLeft().rotateLeft(), dir.rotateRight().rotateRight().rotateRight(), dir.opposite()};
		int lenToTry = toTry.length;
		for(int i = 0; i<lenToTry; i++) {
			boolean movedThereAlready = false;
			int len = prevLocs.length+prevLocsIndex;
			for(int j = prevLocsIndex;j<len; j++) {
				MapLocation testLoc = prevLocs[j%prevLocs.length];
				if(testLoc!= null) {
//					rc.setIndicatorDot(testLoc, 0, 255-j*20, j*20);
					if(currentLoc.add(toTry[i]).equals(testLoc)) {
						movedThereAlready=true;
						break;
					}
				}
			}
			if(!movedThereAlready && safeToMove(toTry[i])) {
				rc.move(toTry[i]);
				prevLocs[prevLocsIndex] = currentLoc;
				prevLocsIndex=(prevLocsIndex+1)%prevLocs.length;
				return;
			}
		}
		//if cant move reset previous locations array
		prevLocs = new MapLocation[preLocsSize];
		prevLocsIndex=0;
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
			if(m[6] == 0) {
				ourMessage=false;
			}else {
				for(int j = 0; j<6; j++) {
					if(m[j]%divisor!=0) {
						ourMessage = false;
						break;
					}
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
	
	static void createWallLocs() throws GameActionException {
		int hqCirclesLen = hqCircles.length;
		for(int i = 0; i <hqCirclesLen; i++) {
			wallLocs[i] = hqCircles[i].translate(hqLoc.x, hqLoc.y);
			avaliableWallLocs[i] = wallLocs[i];
		}
		
		for(int i = 0; i<unadjustedDiggingSpots.length;i++) {
			diggingSpots[i] = unadjustedDiggingSpots[i].translate(hqLoc.x, hqLoc.y);
		}
	}
	
	static void updateWallLocs() throws GameActionException {
		int wallLocsLen = wallLocs.length;
		for(int i = 0; i <wallLocsLen; i++) {
			MapLocation testLoc = wallLocs[i];
			if(rc.canSenseLocation(testLoc) && (rc.senseRobotAtLocation(testLoc) == null || rc.getLocation().equals(testLoc))) { //if location is available
				avaliableWallLocs[i] = testLoc;
			}else {
				avaliableWallLocs[i] = null;
			}
		}
	}
	
	static MapLocation lowestWall() throws GameActionException {
		int smallestWallHeight = 999999;
		MapLocation smallestWall = null;
		for(int i = 0; i <8; i++) {
			MapLocation testLoc = wallLocs[i];
			if(rc.canSenseLocation(testLoc)&&rc.getLocation().isAdjacentTo(testLoc)&& rc.senseElevation(testLoc)<smallestWallHeight) {
				smallestWallHeight=rc.senseElevation(testLoc);
				smallestWall = testLoc;
			}
		}
		return smallestWall;
	}

	static int nearbyBotCount(RobotType type) {
		int count = 0;
		RobotInfo[] robots = rc.senseNearbyRobots();
		for (RobotInfo r : robots) {
			if (r.getType() == type) {
				count++;
			}
		}
		return count;
	}

}