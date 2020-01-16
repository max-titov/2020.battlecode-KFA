package Aryan;

import battlecode.common.*;

public strictfp class RobotPlayer {
	static RobotController rc;

	// General
	static Direction[] directions = { Direction.NORTH, Direction.NORTHEAST, Direction.EAST, Direction.SOUTHEAST,
			Direction.SOUTH, Direction.SOUTHWEST, Direction.WEST, Direction.NORTHWEST };
	static RobotType[] spawnedByMiner = { RobotType.REFINERY, RobotType.VAPORATOR, RobotType.DESIGN_SCHOOL,
			RobotType.FULFILLMENT_CENTER, RobotType.NET_GUN };
	static int turnCount;
	static MapLocation hqLoc;
	static MapLocation enemyHQLoc;

	// HQ

	// MINER

	// REFINERY

	// VAPORATOR

	// DESIGN_SCHOOL

	// FULFILLMENT_CENTER

	// LANDSCAPER

	// DELIVERY DRONE
	static final int SCOUT_DRONE = 1;
	static final int ATTACK_DRONE = 2;
	static int droneType;
	static Direction heading;
	static MapLocation targetLoc;

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
		// Shoots Net Gun if HQ detects drone and is able to shoot
		for (RobotInfo ri : rc.senseNearbyRobots(GameConstants.NET_GUN_SHOOT_RADIUS_SQUARED, rc.getTeam().opponent())) {
			if (rc.canShootUnit(ri.getID())) {
				rc.shootUnit(ri.getID());
			} else {
				System.out.println(rc.getCooldownTurns());
			}
		}
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
		if (droneType == 0) {
			if (rc.getRoundNum() < 100) {
				droneType = SCOUT_DRONE;
			} else {
				droneType = ATTACK_DRONE;
			}
		}
		switch (droneType) {
		case SCOUT_DRONE:
			runScoutDeliveryDrone();
			break;
		case ATTACK_DRONE:
			runAttackDeliveryDrone();
			break;
		}
	}

	static void runScoutDeliveryDrone() throws GameActionException {
		if(enemyHQLoc == null) {
			findScoutHeading();
			findTargetLocation();
			if(!rc.getLocation().isWithinDistanceSquared(targetLoc, 35)) {
				tryMove(heading);
			}
			else {
				for(RobotInfo ri:rc.senseNearbyRobots(35, rc.getTeam().opponent())) {
					if(ri.getType() == RobotType.HQ) {
						enemyHQLoc = ri.getLocation();
						//TODO: Broadcast to block chain
					}
				}
			}
		}
	}

	static void runAttackDeliveryDrone() throws GameActionException {

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

	static void findHQ() throws GameActionException { //Tries to find HQ location
		if (hqLoc == null) {
			for (RobotInfo bot : rc.senseNearbyRobots()) {
				if (bot.type == RobotType.HQ && bot.team == rc.getTeam()) {
					hqLoc = bot.location;
				}
			}
		}
	}

	static void findScoutHeading() throws GameActionException {
		findHQ();
		if(hqLoc != null && heading == null) {
			int myX = rc.getLocation().x;
			int myY = rc.getLocation().y;
			boolean xCheck = myX == hqLoc.x;
			boolean yCheck = myY == hqLoc.y;
			boolean xGreaterCheck = myX > hqLoc.x;
			boolean yGreaterCheck = myY > hqLoc.y;
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
}
