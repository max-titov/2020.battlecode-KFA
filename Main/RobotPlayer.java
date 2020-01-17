package Main;

import battlecode.common.*;

public strictfp class RobotPlayer {
	static RobotController rc;

	// General
	static Direction[] directions = { 
			Direction.NORTH, Direction.NORTHEAST, Direction.EAST, Direction.SOUTHEAST,
			Direction.SOUTH, Direction.SOUTHWEST, Direction.WEST, Direction.NORTHWEST };
	static int dirsLen = directions.length;
	static Direction[] allDirs = Direction.allDirections();
	static int allDirsLen = allDirs.length;
	static RobotType[] spawnedByMiner = { 
			RobotType.REFINERY, RobotType.VAPORATOR, RobotType.DESIGN_SCHOOL,
			RobotType.FULFILLMENT_CENTER, RobotType.NET_GUN };
	static int turnCount;
	static final int KEY = 626;
	static int minX;
	static int minY;
	static int maxX;
	static int maxY;
	
	
	// HQ
	static int numMiners = 0;
	static boolean builtRefinery = false;
	static boolean builtBuilderMiner = false;

	// MINER
	static final int SOUP_CODE = 804;
	static MapLocation hqLoc;
	static MapLocation soupLoc;
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
	static Direction preferedDir;
	static MapLocation globalSoupLoc;
	static MapLocation randomLoc;
	static final int SOUP_MINER = 1;
	static final int BUILDER_MINER = 2;
	static int minerType;
			
	// REFINERY

	// VAPORATOR

	// DESIGN_SCHOOL

	// FULFILLMENT_CENTER

	// LANDSCAPER
	static int landscaperCount;
	
	// DELIVERY DRONE

	// NET_GUN

	/**
	 * run() is the method that is called when a robot is instantiated in the
	 * Battlecode world. If this method returns, the robot dies!
	 **/
	@SuppressWarnings("unused")
	public static void run(RobotController rc) throws GameActionException {
		// This is the RobotController object. You use it to perform actions from this robot,
		// and to get information on its current status.
		RobotPlayer.rc = rc;
		turnCount = 0;
		minX = 0;
		minY = 0;
		maxX = rc.getMapWidth();
		maxY = rc.getMapHeight();
		while (true) {
			turnCount += 1;
			// Try/catch blocks stop unhandled exceptions, which cause your robot to explode
            try {
                // Here, we've separated the controls into a different method for each RobotType.
                // You can add the missing ones or rewrite this into your own control structure.
                switch (rc.getType()) {
                    case HQ:                 runHQ();                break;
                    case MINER:              runMiner();             break;
                    case REFINERY:           runRefinery();          break;
                    case VAPORATOR:          runVaporator();         break;
                    case DESIGN_SCHOOL:      runDesignSchool();      break;
                    case FULFILLMENT_CENTER: runFulfillmentCenter(); break;
                    case LANDSCAPER:         runLandscaper();        break;
                    case DELIVERY_DRONE:     runDeliveryDrone();     break;
                    case NET_GUN:            runNetGun();            break;
                }
                // Clock.yield() makes the robot wait until the next turn, then it will perform this loop again
                Clock.yield();
            } catch (Exception e) {
                System.out.println(rc.getType() + " Exception");
                e.printStackTrace();
            }
		}
	}

	static void runHQ() throws GameActionException {
		
	}

	static void runMiner() throws GameActionException {

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

	}

	static void runDeliveryDrone() throws GameActionException {

	}

	static void runNetGun() throws GameActionException {

	}
	/**
	 * Returns a random Direction.
	 *
	 * @return a random Direction
	 */
	static Direction randomDirection() {
		return directions[(int) (Math.random() * directions.length)];
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
		// MapLocation loc = rc.getLocation();
		// if (loc.x < 10 && loc.x < loc.y)
		// return tryMove(Direction.EAST);
		// else if (loc.x < 10)
		// return tryMove(Direction.SOUTH);
		// else if (loc.x > loc.y)
		// return tryMove(Direction.WEST);
		// else
		// return tryMove(Direction.NORTH);
	}

	/**
	 * Attempts to move in a given direction.
	 *
	 * @param dir The intended direction of movement
	 * @return true if a move was performed
	 * @throws GameActionException
	 */
	static boolean tryMove(Direction dir) throws GameActionException {
		// System.out.println("I am trying to move " + dir + "; " + rc.isReady() + " " +
		// rc.getCooldownTurns() + " " + rc.canMove(dir));
		if (rc.isReady() && rc.canMove(dir)) {
			rc.move(dir);
			return true;
		} else
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
		if (rc.isReady() && rc.canBuildRobot(type, dir)) {
			rc.buildRobot(type, dir);
			return true;
		} else
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
		if (rc.isReady() && rc.canMineSoup(dir)) {
			rc.mineSoup(dir);
			return true;
		} else
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
		if (rc.isReady() && rc.canDepositSoup(dir)) {
			rc.depositSoup(dir, rc.getSoupCarrying());
			return true;
		} else
			return false;
	}

	static void tryBlockchain() throws GameActionException {
		if (turnCount < 3) {
			int[] message = new int[7];
			for (int i = 0; i < 7; i++) {
				message[i] = 123;
			}
			if (rc.canSubmitTransaction(message, 10))
				rc.submitTransaction(message, 10);
		}
		// System.out.println(rc.getRoundMessages(turnCount-1));
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

	static void nearbySoup() throws GameActionException {
		MapLocation currentLoc = rc.getLocation();
		int x = currentLoc.x;
		int y = currentLoc.y;
		int radiusInTiles = radiusInTiles();
		for (int radius = 0; radius <= radiusInTiles; radius++) {
			for (int i = 0; i < visionCircles[radius].length; i++) {
				MapLocation loc = new MapLocation(x + visionCircles[radius][i].x, y + visionCircles[radius][i].y);
				if (rc.canSenseLocation(loc) && rc.senseSoup(loc) > 0 && !rc.senseFlooding(loc)) {
					soupLoc = loc;
					return;
				}
			}
		}
		// remove current soup target if the target is empty
		if (soupLoc != null && rc.canSenseLocation(soupLoc) && rc.senseSoup(soupLoc) <= 0) {
			soupLoc = null;
		}
	}

	static MapLocation l(int x, int y) {
		return new MapLocation(x, y);
	}

	static RobotInfo nearbyRobot(RobotType target) throws GameActionException {
		RobotInfo[] robots = rc.senseNearbyRobots();
		for (RobotInfo r : robots) {
			if (r.getType() == target) {
				return r;
			}
		}
		return null;
	}

	static Direction bugPathing(Direction desiredDir) throws GameActionException {
		Direction result;
		int desiredDirIndex = 0;
		// find index of desired direction
		for (int i = 0; i < directions.length; i++) {
			if (directions[i].equals(desiredDir)) {
				desiredDirIndex = i;
			}
		}
		// test for possible tiles clockwise
		for (int i = desiredDirIndex + 1; i < desiredDirIndex + 4; i++) {
			// wrap around the array
			Direction testingDir = directions[i % directions.length];
			if (rc.canMove(testingDir) && !rc.senseFlooding(rc.getLocation().add(testingDir))) {
				return testingDir;
			}
		}

		// //test for possible tiles counter-clockwise
		// for(int i = desiredDirIndex-1; i > desiredDirIndex-3; i--) {
		// //wrap around the array
		// int testingIndex = i;
		// if(i < 0) {
		// testingIndex = i + directions.length;
		// }
		// Direction testingDir = directions[testingIndex];
		// if(rc.canMove(testingDir) &&
		// !rc.senseFlooding(rc.getLocation().add(testingDir))) {
		// return testingDir;
		// }
		// }
		// no path found
		return null;
		// directions[(desiredDirIndex+4)%directions.length]
	}
}
