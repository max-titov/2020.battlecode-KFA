package gridbot;
import battlecode.common.*;

public class DroneAryan extends Unit {
	
	final int DEFENSE_DRONE = 1;
	final int ATTACK_DRONE = 2;
	
	int droneType;
	Direction heading;
	MapLocation targetLoc;
	boolean readyAttack;
	boolean readyDefense;

	int defenseIndex;
	MapLocation[] defenseCircleCoords;
	
	RobotInfo currentlyHolding;
	
    public DroneAryan(RobotController r) throws GameActionException {
        super(r);
    }

    public void takeTurn() throws GameActionException {
        super.takeTurn();
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
						//TODO:Broadcast to block chain that defense is ready
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
			for (RobotInfo ri : rc.senseNearbyRobots(2, myTeam.opponent())) {
				int ID = ri.getID();
				Team team = ri.getTeam();
				RobotType type = ri.getType();
				if(team == myTeam && type == RobotType.LANDSCAPER && rc.canPickUpUnit(ID)) {
					rc.pickUpUnit(ID);
				}
			}
		}
		//TODO: check blockchain for ready attack
		MapLocation currentLoc = rc.getLocation();
		enemyHQLoc = new MapLocation(35,26);
		//TODO: get enemy HQ location from blockchain
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
						Team team = ri.getTeam();
						RobotType type = ri.getType();
						if(rc.canPickUpUnit(ID)) {
							rc.pickUpUnit(ID);
							currentlyHolding = ri;
						}
					}
					if(!rc.isCurrentlyHoldingUnit()) {
						nav.noReturnNav(dir);
					}
				}
				if(currentlyHolding.getTeam() == myTeam && currentlyHolding.getType() == RobotType.LANDSCAPER && distanceToHQ <= 2) {
					
				}
			}
		}
	}

	void findDefenseCircleCoords() throws GameActionException {
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

}