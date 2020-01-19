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

	static final int KEY = 626;
	
	//map size
	static int minX;
	static int minY;
	static int maxX;
	static int maxY;
	
	static MapLocation hqLoc;
	
	// HQ
	static int numMiners = 0;
	static boolean builtRefinery = false;
	static boolean builtBuilderMiner = false;

	// MINER
	static final int SOUP_CODE = 804;

	static MapLocation[][] visionCircles = {
			{l(0,0)},
			{l(-1,0),l(0,-1),l(1,0),l(0,1)},
			{l(-2,-1),l(-2,0),l(-2,1),l(-1,1),
				l(-1,2),l(0,2),l(1,2),l(1,1),
				l(2,1),l(2,0),l(2,-1),l(1,-1),
				l(1,-2),l(0,-2),l(-1,-2),l(-1,-1)},
			{l(-3,-2),l(-3,-1),l(-3,0),l(-3,1),
				l(-3,2),l(-2,2),l(-2,3),l(-1,3),
				l(0,3),l(1,3),l(2,3),l(2,2),
				l(3,2),l(3,1),l(3,0),l(3,-1),
				l(3,-2),l(2,-2),l(2,-3),l(1,-3),
				l(0,-3),l(-1,-3),l(-2,-3),l(-2,-2)},
			{l(-4,-2),l(-4,-1),l(-4,0),l(-4,1),
				l(-4,2),l(-3,3),l(-2,4),l(-1,4),
				l(0,4),l(1,4),l(2,4),l(3,3),
				l(4,2),l(4,1),l(4,0),l(4,-1),
				l(4,-2),l(3,-3),l(2,-4),l(1,-4),
				l(0,-4),l(-1,-4),l(-2,-4),l(-3,-3)},
			{l(-5,-3),l(-5,-2),l(-5,-1),l(-5,0),
				l(-5,1),l(-5,2),l(-5,3),l(-4,3),
				l(-4,4),l(-3,4),l(-3,5),l(-2,5),
				l(-1,5),l(0,5),l(1,5),l(2,5),
				l(3,5),l(3,4),l(4,4),l(4,3),
				l(5,3),l(5,2),l(5,1),l(5,0),
				l(5,-1),l(5,-2),l(5,-3),l(4,-3),
				l(4,-4),l(3,-4),l(3,-5),l(2,-5),
				l(1,-5),l(0,-5),l(-1,-5),l(-2,-5),
				l(-3,-5),l(-3,-4),l(-4,-4),l(-4,-3)},
			{l(0,0)}
	};
	
	static int minerType;
	static final int SOUP_MINER = 1;
	static final int BUILDER_MINER = 2;
	
	static MapLocation[] manySoups = new MapLocation[20];
	static int soupIndex = -1;
	
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

		turnCount = 0;

		minX = 0;
		minY = 0;
		maxX = rc.getMapWidth();
		maxY = rc.getMapHeight();
		while (true) {
			turnCount += 1;
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
		if (numMiners < 5) {
			for (Direction dir : directions)
				if (tryBuild(RobotType.MINER, dir)) {
					numMiners++;
				}
		}
	}
	static void runMiner() throws GameActionException {
		if(minerType == 0) {
			if(rc.getRoundNum() < 100) {
				minerType = SOUP_MINER;
			}
			else {
				minerType = BUILDER_MINER;
			}
		}
		switch(minerType) {
		case SOUP_MINER:
			runSoupMiner();
			break;
		case BUILDER_MINER:
			runBuilderMiner();
			break;
		}
			
	}
	static void runSoupMiner() throws GameActionException {
		findHQ();

		RobotInfo[] nearbyBots = rc.senseNearbyRobots();
		MapLocation currentLoc = rc.getLocation();
		// tryBlockchain();
		// scan nearby area for the nearest soup location and save to a variable
		//nearbySoup();
		

		// refining and mining
		for (int i = 0; i< allDirsLen; i++)
			tryMine(allDirs[i]);
		for (int i = 0; i< allDirsLen; i++)
			tryRefine(allDirs[i]);
		

		// where to move
		Direction desiredDir; // where the miner WANTS to go
		// if full capacity find refinery
		if (rc.getSoupCarrying() >= RobotType.MINER.soupLimit) {
			RobotInfo refinery = nearbyRobot(RobotType.REFINERY, rc.getTeam());
			if (refinery != null) { // if there is a nearby refinery go there
				Direction dirToRefinery = currentLoc.directionTo(refinery.location);
				desiredDir = dirToRefinery;
			} else { // go the hq
				Direction dirToHQ = currentLoc.directionTo(hqLoc);
				desiredDir = dirToHQ;
			}
		
		} else {
			//make this shit better
			desiredDir = randomDirection();
		}
		
		if(rc.canMove(desiredDir)) {
			rc.move(desiredDir);
		} else {
			// bug pathfinding
			desiredDir = bugPathing(desiredDir);
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

	/**
	 * Returns a random RobotType spawned by miners.
	 *
	 * @return a random RobotType
	 */
	static RobotType randomSpawnedByMiner() {
		return spawnedByMiner[(int) (Math.random() * spawnedByMiner.length)];
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
		if (rc.canDepositSoup(dir)) {
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
		MapLocation currentLoc = rc.getLocation();
//		MapLocation[] nearbySoups = rc.senseNearbySoup();
//		int len = manySoups.length;
//		for(int i = 0; i<)
	}

	static MapLocation l(int x, int y) {
		return new MapLocation(x, y);
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
		if(canMoveInDir(desiredDir.rotateRight())) {
			return desiredDir.rotateRight();
		}
		else if(canMoveInDir(desiredDir.rotateRight().rotateRight())) {
			return desiredDir.rotateRight().rotateRight();
		}
		else if(canMoveInDir(desiredDir.rotateRight().rotateRight().rotateRight())) {
			return desiredDir.rotateRight().rotateRight().rotateRight();
		}
		else {
			return null;
		}
		
	}
	
	static Direction bugPathing2(Direction desiredDir) throws GameActionException {
		if(canMoveInDir(desiredDir.rotateRight())) {
			return desiredDir.rotateRight();
		}
		else if(canMoveInDir(desiredDir.rotateRight().rotateRight())) {
			return desiredDir.rotateRight().rotateRight();
		}
		else {
			return null;
		}
		
	}
	
	static boolean canMoveInDir(Direction dir) throws GameActionException {
		if(rc.canMove(dir) && !rc.senseFlooding(rc.getLocation().add(dir))) {
			return true;
		}
		return false;
	}
	
	static int[] getMessages() throws GameActionException {
		return getMessages(rc.getRoundNum()-1);
	}
	
	static int[] getMessages(int roundNum) throws GameActionException {
		int[] messages = new int[28]; //4 per message 7 messages
		Transaction[] transactions = rc.getBlock(roundNum);
		int len = transactions.length;
		for(int i = 0; i < len; i++) {
			if(transactions[i]==null)
				return messages;
			int[] m = transactions[i].getMessage();
			if(m[0]+m[1]-m[6] == KEY) {
				for (int j = 0; j<4; j++) {
					messages[i*4+j] = m[j+2];
				}
			}
		}
		return messages;
	}
	
	static boolean sendMessage(int[] m, int cost) throws GameActionException {
		int int6 = (int)(Math.random()*KEY);
		int int1 = (int)(Math.random()*(KEY+int6-1));
		int int0 = KEY+int6-int1;
		//System.out.println(int0+"+"+int1+"-"+int6);
		int[] message = {int0,int1,m[0],m[1],m[2],m[3],int6};
		if(rc.canSubmitTransaction(message, cost)) {
			rc.submitTransaction(message, cost);
			return true;
		}
		return false;
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
}
