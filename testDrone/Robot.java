package testDrone;
import battlecode.common.*;

public class Robot {
    RobotController rc;
    Communications comms;
    int turnCount = 0;
    int[] currentMessages;
    int currentSensorRadius;
    RobotType myType;
    Team myTeam;
    Team opponent;
    MapLocation hqLoc;
    MapLocation enemyHQLoc;
    MapLocation currentLoc;
    int landscaperCount = 0;
    int droneCount = 0;
    int vaporatorCount = 0;
    int schoolCount = 0;
    int fulfillmentCenterCount = 0;

    public Robot(RobotController r) throws GameActionException {
        this.rc = r;
        comms = new Communications(rc);
        myType = rc.getType();
        myTeam = rc.getTeam();
        opponent = myTeam.opponent();
        
       
    }

    public void takeTurn() throws GameActionException {
        turnCount += 1;
		currentMessages = comms.getMessages();
		currentLoc = rc.getLocation();
        currentSensorRadius = rc.getCurrentSensorRadiusSquared();
        
        findHQ();
    }

    boolean tryBuild(RobotType type, Direction dir) throws GameActionException {
        if (rc.canBuildRobot(type, dir)) {
            rc.buildRobot(type, dir);
            return true;
        }
        return false;
    }
    
	RobotInfo nearbyRobot(RobotType target, Team team) throws GameActionException {
		RobotInfo[] robots = rc.senseNearbyRobots();
		for (RobotInfo r : robots) {
			if (r.getType() == target && r.team == team) {
				return r;
			}
		}
		return null;
	}
	
	RobotType buildPriority() throws GameActionException {
		
		return null;
	}
	
    void findHQ() throws GameActionException {
        if (hqLoc == null) {
        	if(rc.getType().equals(RobotType.HQ)) {
        		hqLoc = currentLoc;
        	}
            // search surroundings for HQ
            RobotInfo[] robots = rc.senseNearbyRobots();
            for (RobotInfo robot : robots) {
                if (robot.type == RobotType.HQ && robot.team == rc.getTeam()) {
                    hqLoc = robot.location;
                    return;
                }
            }
            hqLoc = comms.getHQLocFromBlockchain();
        }
    }
    
    public void findEnemyHQ() throws GameActionException {
    	enemyHQLoc = comms.getEnemyHQLocFromBlockchain();
    }
    void updateRobotCounts() throws GameActionException {
    	int[] m = comms.checkForMessage(comms.M_ROBOT_COUNTS);
    	landscaperCount = m[0];
    	droneCount = m[1];
    	vaporatorCount = m[2];
    	schoolCount = m[3];
    	fulfillmentCenterCount = m[4];
    }
    
}