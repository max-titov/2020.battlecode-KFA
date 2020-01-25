package gridbot;
import battlecode.common.*;

public class Robot {
    RobotController rc;
    Communications comms;

    int turnCount = 0;
    int[] currentMessages;
    Team myTeam;
    Team opponent;
    MapLocation hqLoc;

    public Robot(RobotController r) {
        this.rc = r;
        comms = new Communications(rc);
        myTeam = rc.getTeam();
        opponent = myTeam.opponent();
    }

    public void takeTurn() throws GameActionException {
        turnCount += 1;
		currentMessages = comms.getMessages();
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
	
    public void findHQ() throws GameActionException {
        if (hqLoc == null) {
            // search surroundings for HQ
            RobotInfo[] robots = rc.senseNearbyRobots();
            for (RobotInfo robot : robots) {
                if (robot.type == RobotType.HQ && robot.team == rc.getTeam()) {
                    hqLoc = robot.location;
                    return;
                }
            }
            // if still null, search the blockchain
            hqLoc = comms.getHqLocFromBlockchain();
        }
    }
}