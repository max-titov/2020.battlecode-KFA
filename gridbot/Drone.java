package gridbot;
import battlecode.common.*;

public class Drone extends Unit {

	final int SCOUT_DRONE = 1;
	final int DEFENSE_DRONE = 2;
	final int ATTACK_DRONE = 3;

	int droneType;
	Direction heading;
	MapLocation targetLoc;
	boolean readyAttack;
	boolean readyDefense;
	int defenseIndex;
	MapLocation[] defenseCircleCoords;
	int enemyHQLocIndex;
	MapLocation[] possibleEnemyHQ;
	RobotInfo currentlyHolding;
	
	MapLocation closestFlooding;

	public Drone(RobotController r) throws GameActionException{
		super(r);
		hqLoc = comms.getHQLocFromBlockchain();
		
	}

	public void takeTurn() throws GameActionException {
		super.takeTurn();
		if(possibleEnemyHQ == null) {
			findPotentialEnemyHQ();
		}
		enemyHQLoc = comms.getEnemyHQLocFromBlockchain();
		if(enemyHQLoc == null) {
			droneType = SCOUT_DRONE;
		}
		else if(comms.checkForMessage(comms.M_DEFENSE_READY) == null) {
			droneType = DEFENSE_DRONE;
		}
		else {
			droneType = ATTACK_DRONE;
		}
		switch (droneType) {
		case SCOUT_DRONE:
			runScoutDeliveryDrone();
		case DEFENSE_DRONE:
			runDefenseDeliveryDrone();
			break;
		case ATTACK_DRONE:
			runAttackDeliveryDrone();
			break;
		}
	}

	void runScoutDeliveryDrone() throws GameActionException {
		senseFlooding();
		MapLocation desiredLoc = null;
		if(!rc.isCurrentlyHoldingUnit()) {
			desiredLoc = possibleEnemyHQ[enemyHQLocIndex];
			if(currentLoc.distanceSquaredTo(desiredLoc) > currentSensorRadius) {
				nav.noReturnNav(desiredLoc);
			}
			else {
				RobotInfo occupiedRobot = rc.senseRobotAtLocation(desiredLoc);
				if(occupiedRobot != null && occupiedRobot.getTeam() == opponent && occupiedRobot.getType() == RobotType.HQ) {
					comms.sendEnemyHQLoc(desiredLoc);
					if(!rc.isCurrentlyHoldingUnit()) {
						for (RobotInfo ri : rc.senseNearbyRobots(2, opponent)) {
							int ID = ri.getID();
							Team team = ri.getTeam();
							RobotType type = ri.getType();
							if((type == RobotType.LANDSCAPER || type == RobotType.MINER) && rc.canPickUpUnit(ID)) {
								currentlyHolding = ri;
								rc.pickUpUnit(ID);
							}
						}
					}
				}
				else {
					desiredLoc = possibleEnemyHQ[++enemyHQLocIndex];
					nav.noReturnNav(desiredLoc);
				}
			}
		}
		else {
			desiredLoc = closestFlooding;
			if(rc.senseFlooding(currentLoc.add(currentLoc.directionTo(desiredLoc)))) {
				rc.dropUnit(currentLoc.directionTo(desiredLoc));
			}
			else {
				nav.noReturnNav(desiredLoc);
			}
		}
	}

	void runDefenseDeliveryDrone() throws GameActionException {
		findDefenseCircleCoords();
		MapLocation desiredLoc = defenseCircleCoords[defenseIndex];
		while(desiredLoc.x < 0 || desiredLoc.y < 0 ||desiredLoc.x > rc.getMapWidth() || desiredLoc.y > rc.getMapHeight()) {
			desiredLoc = defenseCircleCoords[++defenseIndex];
		}
		Direction desiredDir = currentLoc.directionTo(desiredLoc);
		if(!currentLoc.equals(desiredLoc)) {
			if(!nav.tryMove(desiredDir)) {
				if(currentLoc.isAdjacentTo(desiredLoc) && rc.getCurrentSensorRadiusSquared() > 2) {
					RobotInfo occupiedRobot = rc.senseRobotAtLocation(desiredLoc);
					if(occupiedRobot.getType() == rc.getType() && occupiedRobot.getTeam() == myTeam) {
						desiredLoc = defenseCircleCoords[++defenseIndex];
					}
					if(defenseIndex >= defenseCircleCoords.length) {
						readyDefense = true;
						int[] m = {comms.M_DEFENSE_READY, Util.rand(), Util.rand(), Util.rand(), Util.rand(), Util.rand()};
						comms.sendMessage(m, 1);
					}
				}
				else {
					nav.noReturnNav(desiredDir);
				}
			}
		}
	}

	void runAttackDeliveryDrone() throws GameActionException {
		if(!rc.isCurrentlyHoldingUnit()) {
			for (RobotInfo ri : rc.senseNearbyRobots(2, myTeam)) {
				int ID = ri.getID();
				RobotType type = ri.getType();
				double rand = Math.random();
				if(type == RobotType.LANDSCAPER && rc.canPickUpUnit(ID) && rand <= 0.2) {
					rc.pickUpUnit(ID);
				}
				else if(type == RobotType.MINER && rc.canPickUpUnit(ID) && rand >= 0.9) {
					rc.pickUpUnit(ID);
				}
			}
		}
		if(comms.checkForMessage(comms.M_ATTACK_READY) != null) {
			readyAttack = true;
		}
		MapLocation currentLoc = rc.getLocation();
		Direction dir = currentLoc.directionTo(enemyHQLoc);
		int distanceToHQ = currentLoc.add(dir).distanceSquaredTo(enemyHQLoc);
		if(distanceToHQ > GameConstants.NET_GUN_SHOOT_RADIUS_SQUARED) {
			nav.noReturnNav(dir);
		}
		else {
			if(!readyAttack) {
				nav.tryMove(dir.rotateRight().rotateRight());
			}
			else {
				if(!rc.isCurrentlyHoldingUnit()) {
					for (RobotInfo ri : rc.senseNearbyRobots(2, opponent)) {
						int ID = ri.getID();
						if(rc.canPickUpUnit(ID)) {
							rc.pickUpUnit(ID);
							currentlyHolding = ri;
						}
					}
					if(!rc.isCurrentlyHoldingUnit()) {
						nav.noReturnNav(dir);
					}
				}
				if(currentlyHolding != null && currentlyHolding.getTeam() == myTeam && currentlyHolding.getType() == RobotType.LANDSCAPER && distanceToHQ <= 2) {
					rc.dropUnit(dir);
				}
			}
		}
	}

	void findDefenseCircleCoords() throws GameActionException {
		if (defenseCircleCoords == null){
			defenseCircleCoords = new MapLocation[24];
			int index = 0;
			for(int i = -3; i <= 3; i++) {
				for(int j = -3; j <= 3; j++) {
					if(Math.abs(i) == 3 || Math.abs(j) == 3) {
						defenseCircleCoords[index++] = nav.l(hqLoc.x+i, hqLoc.y+j);
					}
				}
			}
		}
	}

	void findPotentialEnemyHQ() throws GameActionException {
		MapLocation mp = nav.l(nav.MAP_WIDTH/2, nav.MAP_HEIGHT/2);
		possibleEnemyHQ = new MapLocation[3];
		if(hqLoc.x < mp.x) {
			if(hqLoc.y < mp.y) { //bottom left
				possibleEnemyHQ[2] = nav.l(hqLoc.x, nav.MAP_HEIGHT-hqLoc.y);
				possibleEnemyHQ[1] = nav.l(nav.MAP_WIDTH-hqLoc.x, nav.MAP_HEIGHT-hqLoc.y);
				possibleEnemyHQ[0] = nav.l(nav.MAP_WIDTH-hqLoc.x, hqLoc.y);
			}else { //top left
				possibleEnemyHQ[2] = nav.l(nav.MAP_WIDTH-hqLoc.x, hqLoc.y);
				possibleEnemyHQ[1] = nav.l(nav.MAP_WIDTH-hqLoc.x, nav.MAP_HEIGHT-hqLoc.y);
				possibleEnemyHQ[0] = nav.l(hqLoc.x, nav.MAP_HEIGHT-hqLoc.y);
			}
		}else {
			if(hqLoc.y < mp.y) { //bottom right
				possibleEnemyHQ[2] = nav.l(nav.MAP_WIDTH-hqLoc.x, hqLoc.y);
				possibleEnemyHQ[1] = nav.l(nav.MAP_WIDTH-hqLoc.x, nav.MAP_HEIGHT-hqLoc.y);
				possibleEnemyHQ[0] = nav.l(hqLoc.x, nav.MAP_HEIGHT-hqLoc.y);
			}else { //top right
				possibleEnemyHQ[2] = nav.l(hqLoc.x, nav.MAP_HEIGHT-hqLoc.y);
				possibleEnemyHQ[1] = nav.l(nav.MAP_WIDTH-hqLoc.x, nav.MAP_HEIGHT-hqLoc.y);
				possibleEnemyHQ[0] = nav.l(nav.MAP_WIDTH-hqLoc.x, hqLoc.y);
			}
		}

	}

	void senseFlooding() throws GameActionException {
		int radiusInTiles = nav.radiusInTiles();
		for (int radius = 0; radius <= radiusInTiles; radius++) {
			for (int i = 0; i < nav.visionCircles[radius].length; i++) {
				MapLocation loc = nav.l(currentLoc.x + nav.visionCircles[radius][i].x, currentLoc.y + nav.visionCircles[radius][i].y);
				if (rc.canSenseLocation(loc) && rc.senseFlooding(loc)) {
					closestFlooding = loc;
					return;
				}
			}
		}
		return;
	}
}