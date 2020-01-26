package gridbot;
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
    int round;
    RobotInfo[] nearbyAlliedRobots;
    RobotInfo[] nearbyEnemyRobots;
    
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
        if(round > 1) {
        	getMasterRobotCounts();
        }
        
    }

    public void takeTurn() throws GameActionException {
        turnCount += 1;
		currentMessages = comms.getMessages();
		currentLoc = rc.getLocation();
        currentSensorRadius = rc.getCurrentSensorRadiusSquared();
        round = rc.getRoundNum();
        nearbyAlliedRobots = rc.senseNearbyRobots(currentSensorRadius, myTeam);
        nearbyEnemyRobots = rc.senseNearbyRobots(currentSensorRadius, opponent);
        
        findHQ();
        if(round > 1) {
        	if(turnCount <= 10) {
            	getMasterRobotCounts();
            }
        	updateRobotCounts();
        }
        if(turnCount == 1) {
        	for(int i = round-10; i<round;i++) {
        		if(i>0) {
        			getMasterRobotCounts(i);
        		}
        	}
        }
        System.out.println(landscaperCount+" "+droneCount+" "+vaporatorCount+" "+schoolCount+" "+fulfillmentCenterCount);
    }

    boolean tryBuild(RobotType type, Direction dir) throws GameActionException {
        if (rc.canBuildRobot(type, dir)) {
            rc.buildRobot(type, dir);
            comms.broadcastCreation(type);
            return true;
        }
        return false;
    }
    
	RobotInfo nearbyRobot(RobotType target, Team team) throws GameActionException {
		RobotInfo[] robots = nearbyEnemyRobots;
		if(team.equals(myTeam)) {
			robots = nearbyAlliedRobots;
		}
		for (RobotInfo r : robots) {
			if (r.getType() == target) {
				return r;
			}
		}
		return null;
	}
	
    void findHQ() throws GameActionException {
        if (hqLoc == null) {
        	if(rc.getType().equals(RobotType.HQ)) {
        		hqLoc = rc.getLocation();
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
    void getMasterRobotCounts(int roundNum) throws GameActionException {
    	int[] m = comms.checkForMessage(comms.M_ROBOT_COUNTS, roundNum);
    	if(m == null) {
    		return;
    	}
    	landscaperCount = m[0];
    	droneCount = m[1];
    	vaporatorCount = m[2];
    	schoolCount = m[3];
    	fulfillmentCenterCount = m[4];
    }
    
    void getMasterRobotCounts() throws GameActionException {
    	getMasterRobotCounts(round-1);
    }
    
    void updateRobotCounts(int roundNum) throws GameActionException{
    	for(int i = 0; i<42; i+=6) {
    		switch(comms.getMessages()[i]) {
    		case Communications.M_LANDSCAPER:
    			landscaperCount++;
    			break;
    		case Communications.M_DRONE:
    			droneCount++;
    			break;
    		case Communications.M_VAPORATOR:
    			vaporatorCount++;
    			break;
    		case Communications.M_SCHOOL:
    			schoolCount++;
    			break;
    		case Communications.M_FULFILLMENT_CENTER:
    			fulfillmentCenterCount++;
    			break;
    		}
    	}
    }
    
    void updateRobotCounts() throws GameActionException {
    	updateRobotCounts(round-1);
    }
    
    RobotType buildPriority() throws GameActionException {
    	int[] priority = {landscaperCount, droneCount, vaporatorCount, schoolCount*7, fulfillmentCenterCount*7};
    	int smallest = landscaperCount;
    	int smallestIndex = 0;
    	for(int i = 1; i< priority.length;i++) {
    		if(priority[i]<=smallest) {
    			smallest = priority[i];
    			smallestIndex = i;
    		}
    	}
    	
    	switch(smallestIndex) {
    	case 0:
    		return RobotType.LANDSCAPER;
    	case 1:
    		return RobotType.DELIVERY_DRONE;
    	case 2:
    		return RobotType.VAPORATOR;
    	case 3:
    		return RobotType.DESIGN_SCHOOL;
    	case 4:
    		return RobotType.FULFILLMENT_CENTER;
    	}
    	return null;
    	
    	
    }
    
    boolean building(RobotInfo info, Team team) {
    	if(!info.getTeam().equals(team)) {
    		return false;
    	}
    	RobotType type = info.type;
    	if(type.equals(RobotType.DESIGN_SCHOOL) || 
    			type.equals(RobotType.FULFILLMENT_CENTER) ||
    			type.equals(RobotType.VAPORATOR) || 
    			type.equals(RobotType.NET_GUN) || 
    			type.equals(RobotType.HQ)) {
    		return true;
    	}
    	return false;
    }
    
}