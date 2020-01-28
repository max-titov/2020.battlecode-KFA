package turtlebot;
import battlecode.common.*;

public class Drone extends Unit {
	int storedUbers = 20;
	MapLocation[] pickUpLocs = new MapLocation[storedUbers];
	MapLocation[] dropOffLocs = new MapLocation[storedUbers];
	int[] IDs = new int[storedUbers];
	int[] uberExpiration = new int[storedUbers];
	
	int currentUberIndex = -1;
	boolean pickedUpBot = false;

	MapLocation[] defenseCircleCoords;
	int defenseIndex = 0;
	
	int turnsToGetToWall=0;
	
	public Drone(RobotController r) throws GameActionException{
		super(r);
		findDefenseCircleCoords();
	}

	public void takeTurn() throws GameActionException {
		super.takeTurn();
		updateUbers();
		
		if(round<500) {
			runUber();
		}
		runDefense();
	}
	
	void runUber() throws GameActionException {
		defenseIndex=0;
		
		for(int i = 0; i<storedUbers;i++) {
			if(pickUpLocs[i]!=null) {
				rc.setIndicatorLine(pickUpLocs[i], dropOffLocs[i], 255, 255, 255);
				rc.setIndicatorDot(dropOffLocs[i], 0, 255, 255);
			}
		}
		if(currentUberIndex == -1) {
			int minDist = 99999;
			int closestIndex = -1;
			for(int i = 0; i<storedUbers; i++) {
				MapLocation testLoc = pickUpLocs[i];
				if(testLoc!=null) {
					int dist = currentLoc.distanceSquaredTo(testLoc);
					if(dist<minDist) {
						minDist=dist;
						closestIndex=i;
					}
				}
			}
			if(closestIndex!=-1) {
				currentUberIndex = closestIndex;
			}
		}
		if(currentUberIndex!=-1) {
			if(dropOffLocs[currentUberIndex] == null) {
				currentUberIndex=-1;
			}
			
			MapLocation desiredLoc = null;
			if(pickedUpBot) {
				desiredLoc = dropOffLocs[currentUberIndex];
				if(currentLoc.equals(desiredLoc)) {
					desiredLoc=currentLoc.add(Util.randomDirection());
				}else if(currentLoc.isAdjacentTo(desiredLoc)) {
					Direction dir = currentLoc.directionTo(desiredLoc);
					Direction[] toTry = {dir, dir.rotateLeft(), dir.rotateRight(), dir.rotateLeft().rotateLeft(), dir.rotateRight().rotateRight()};
					for(int i = 0; i<toTry.length;i++) {
						if(rc.canDropUnit(toTry[i]) && !rc.senseFlooding(currentLoc.add(toTry[i]))) {
							rc.dropUnit(toTry[i]);
							pickedUpBot=false;
							comms.uberCompleted(pickUpLocs[currentUberIndex], dropOffLocs[currentUberIndex], IDs[currentUberIndex]);
							currentUberIndex=-1;
						}
					}
					desiredLoc=null;
				}
	
			}else  {
				MapLocation nearbyRobotWithID = nearbyRobotWithID(IDs[currentUberIndex]);
				System.out.println(IDs[currentUberIndex]);
				if(currentLoc.isAdjacentTo(pickUpLocs[currentUberIndex]) && nearbyRobotWithID==null) {
					comms.uberCompleted(pickUpLocs[currentUberIndex], dropOffLocs[currentUberIndex], IDs[currentUberIndex]);
					currentUberIndex=-1;
				}else if(nearbyRobotWithID!=null&&currentLoc.isAdjacentTo(nearbyRobotWithID)) {
					if(rc.canPickUpUnit(IDs[currentUberIndex])){
						rc.pickUpUnit(IDs[currentUberIndex]);
						pickedUpBot = true;
						desiredLoc = dropOffLocs[currentUberIndex];
					}
				}else if(nearbyRobotWithID!=null) {
					desiredLoc=nearbyRobotWithID;
				}else {
					desiredLoc =pickUpLocs[currentUberIndex];
				}
			}
			if(desiredLoc!=null)
				nav.noReturnNavDrone(desiredLoc);
		}
	}
	
	void runDefense() throws GameActionException {
		if(defenseIndex>=24) {
			defenseIndex=0;
		}
		MapLocation testLoc = defenseCircleCoords[defenseIndex];
		while(testLoc==null) {
			defenseIndex++;
			if(defenseIndex>=24) {
				defenseIndex=0;
			}
			testLoc = defenseCircleCoords[defenseIndex];
		}
		if(currentLoc.equals(testLoc)) {
			//do nothing
		} else if(rc.canSenseLocation(testLoc) && rc.senseRobotAtLocation(testLoc)!=null) {
			defenseIndex++;
			turnsToGetToWall=0;
		} else {
			nav.noReturnNavDrone(testLoc);
			turnsToGetToWall++;
		}
		if(turnsToGetToWall>30) {
			defenseIndex++;
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
				}
				else {
					nav.noReturnNavDrone(desiredDir);
				}
			}
		}
	}
	
	
	void updateUbers() {
		MapLocation[] newPickUpLocs = new MapLocation[7];
		MapLocation[] newDropOffLocs = new MapLocation[7];
		int[] newIDs = new int[7];
		int newUberIndex = 0;
		
		MapLocation[] oldPickUpLocs = new MapLocation[7];
		MapLocation[] oldDropOffLocs = new MapLocation[7];
		int[] oldIDs = new int[7];
		int oldUberIndex = 0;
		//find all messages with the uber tag
		for(int i = 0; i<42; i+=6) {
			if(currentMessages[i] == comms.M_UBER) {
				newPickUpLocs[newUberIndex] = new MapLocation(currentMessages[i+1],currentMessages[i+2]);
				newDropOffLocs[newUberIndex] = new MapLocation(currentMessages[i+3],currentMessages[i+4]);
				newIDs[newUberIndex] = currentMessages[i+5];
				newUberIndex++;
			}
			else if(currentMessages[i] == comms.M_UBER_COMPLETED) {
				oldPickUpLocs[oldUberIndex] = new MapLocation(currentMessages[i+1],currentMessages[i+2]);
				oldDropOffLocs[oldUberIndex] = new MapLocation(currentMessages[i+3],currentMessages[i+4]);
				oldIDs[oldUberIndex] = currentMessages[i+5];
				oldUberIndex++;
			}
		}
		newUberIndex=0;
		for(int i = 0; i<storedUbers; i++) {
			if(pickUpLocs[i] != null) {
				for(int j = 0; j<7; j++) {
					if(oldPickUpLocs[j] != null) {
						if(pickUpLocs[i].equals(oldPickUpLocs[j]) && 
								dropOffLocs[i].equals(oldDropOffLocs[j]) &&
								IDs[i]==oldIDs[j]) {
							pickUpLocs[i] = null;
							dropOffLocs[i] = null;
							IDs[i] = -1;
							if(i==currentUberIndex) {
								currentUberIndex=-1;
							}
						}else {
							uberExpiration[i]++;
							if(uberExpiration[i]>100) {
								pickUpLocs[i] = null;
								dropOffLocs[i] = null;
								IDs[i] = -1;
								if(i==currentUberIndex) {
									currentUberIndex=-1;
								}
							}
						}
						if(j==currentUberIndex) {
							currentUberIndex=-1;
						}
						
					}
				}
			}
			if(pickUpLocs[i] == null && 
					newUberIndex < 7 && 
					newPickUpLocs[newUberIndex] != null) {
				pickUpLocs[i] = newPickUpLocs[newUberIndex];
				dropOffLocs[i] = newDropOffLocs[newUberIndex];
				IDs[i] = newIDs[newUberIndex];
				uberExpiration[i] = 0;
				newUberIndex++;
			}
		}
	}

	MapLocation nearbyRobotWithID(int ID) {
		for(int i = 0; i<nearbyAlliedRobots.length;i++) {
			if(nearbyAlliedRobots[i].ID==ID) {
				return nearbyAlliedRobots[i].location;
			}
		}
		return null;
	}
	
	void findDefenseCircleCoords() throws GameActionException {
		if (defenseCircleCoords == null){
			defenseCircleCoords = new MapLocation[24];
			int index = 0;
			for(int i = -3; i <= 3; i++) {
				for(int j = -3; j <= 3; j++) {
					MapLocation testLoc = nav.l(hqLoc.x+i, hqLoc.y+j);
					if(Math.abs(i) == 3 || Math.abs(j) == 3 && onMap(testLoc)) {
						defenseCircleCoords[index++] = testLoc;
					}
				}
			}
		}
	}
}