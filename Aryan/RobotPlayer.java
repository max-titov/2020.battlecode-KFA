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
	static int mapCorner;
	static final int QUADRANT1 = 1;
	static final int QUADRANT2 = 2;
	static final int QUADRANT3 = 3;
	static final int QUADRANT4 = 4;
	static final int KEY = 23;

	static final int M_HQ_LOC = 898;

	// HQ
	static boolean builtMiner;
	// MINER

	// REFINERY

	// VAPORATOR

	// DESIGN_SCHOOL

	// FULFILLMENT_CENTER
	static Direction[] directionsToBuild = null;
	static int directionIndex;
	// LANDSCAPER

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
	static final int M_FOUND_ENEMY_HQ = 732;

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
		hqLoc = rc.getLocation();
		if(rc.getRoundNum() == 1) {
			int[] m = {M_HQ_LOC, hqLoc.x, hqLoc.y, rand(),rand(),rand()};
			sendMessage(m, 1);
		}
		if(mapCorner == 0) {
			findCorner();
		}
		// Shoots Net Gun if HQ detects drone and is able to shoot
		for (RobotInfo ri : rc.senseNearbyRobots(GameConstants.NET_GUN_SHOOT_RADIUS_SQUARED, rc.getTeam().opponent())) {
			if (rc.canShootUnit(ri.getID())) {
				rc.shootUnit(ri.getID());
			}
		}
		if(!builtMiner) {
			tryBuild(RobotType.MINER, Direction.NORTH);
			builtMiner = true;
		}
	}

	static void runMiner() throws GameActionException {
		if(rc.getRoundNum() < 20) {
			tryMove(Direction.EAST);
		}
		else if(rc.getRoundNum() < 30){
			tryBuild(RobotType.FULFILLMENT_CENTER, Direction.NORTH);
		}
		else {
			tryMove(Direction.SOUTH);
		}
	}

	static void runRefinery() throws GameActionException {

	}

	static void runVaporator() throws GameActionException {

	}

	static void runDesignSchool() throws GameActionException {

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
					desiredDir = bugPathing(desiredDir);
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

	static boolean canMoveInDir(Direction dir) throws GameActionException {
		if(rc.canMove(dir) && !rc.senseFlooding(rc.getLocation().add(dir))) {
			return true;
		}
		return false;
	}

	static Direction bugPathing(Direction dir) throws GameActionException {
		Direction[] toTry = {dir, dir.rotateLeft(), dir.rotateRight(), dir.rotateLeft().rotateLeft(), dir.rotateRight().rotateRight()};
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
			messages = getMessages(1);
			for(int i = 0; i<messages.length; i+=6) {
				if(messages[i] == M_HQ_LOC) {
					hqLoc= new MapLocation(messages[i+1],messages[i+2]);
				}
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
			for(int i = -2; i <= 2; i++) {
				for(int j = -2; j <= 2; j++) {
					if(Math.abs(i) == 2 || Math.abs(j) == 2) {
						defenseCircleCoords[index++] = new MapLocation(hqLoc.x+i, hqLoc.y+j);
					}
				}
			}
		}
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

	static int rand() throws GameActionException {
		return (int)(Math.random()*500)+1;
	}
}
