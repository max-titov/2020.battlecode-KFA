package gridbot;
import battlecode.common.*;
import java.util.ArrayList;

public class Communications {
    RobotController rc;

	final int KEY = 37;
	
	//messages
	final int M_HQ_LOC = 384;
	final int M_SOUP_MARKER = 804;
	final int M_REMOVE_SOUP_MARKER = 947;
	final int M_BUILD_SCHOOL = 283;
	final int M_ENEMY_HQ_LOC = 987;

	//robot type
	static final int M_LANDSCAPER = 44;
	static final int M_DRONE = 29;
	static final int M_VAPORATOR = 93;
	static final int M_SCHOOL = 48;
	static final int M_FULFILLMENT_CENTER = 39;
	
	static final int M_ROBOT_COUNTS = 72;
	
    public Communications(RobotController r) {
        rc = r;
    }
    ///////INTERACTING WITH BLOCKCHAIN/////////
	
	public int[] getMessages(int roundNum) throws GameActionException {
		int[] messages = new int[42]; //6 per message 7 messages
		Transaction[] transactions = rc.getBlock(roundNum);
		int len = transactions.length;
		for(int i = 0; i < len; i++) {
			if(transactions[i]==null)
				return messages;
			int[] m = transactions[i].getMessage();
			//check if its our message
			int divisor = m[6]*KEY; //divide by this to decrypt
			boolean ourMessage = true;
			if(m[6] == 0) {
				ourMessage=false;
			}else {
				for(int j = 0; j<6; j++) {
					if(m[j]%divisor!=0) {
						ourMessage = false;
						break;
					}
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
	
	public int[] getMessages() throws GameActionException {
		return getMessages(rc.getRoundNum()-1);
	}
	
	public boolean sendMessage(int[] m, int cost) throws GameActionException {
		int encoder = Util.rand(500);
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
	
	public int[] checkForMessage(int tag, int roundNum) throws GameActionException {
		int[] m = getMessages();
		for(int i = 0; i<m.length; i+=6) {
			if(m[i] == tag) {
				int[] ret = {m[i+1],m[i+2],m[i+3],m[i+4],m[i+5]};
				return ret;
			}
		}
		return null;
	}
	
	public int[] checkForMessage(int tag) throws GameActionException {
		return checkForMessage(tag, rc.getRoundNum()-1);
	}

	//////////METHODS FOR ROBOTS/////////////
	
	public void sendHQLoc(MapLocation loc) throws GameActionException {
		if(rc.getRoundNum() == 0 || rc.getRoundNum() == 1 || rc.getRoundNum() == 2) { //redundancy
			int[] m = {M_HQ_LOC, loc.x, loc.y, Util.rand(),Util.rand(),Util.rand()};
			sendMessage(m, 1);
		}
	}
	
	public void sendEnemyHQLoc(MapLocation loc) throws GameActionException {
		int[] m = {M_ENEMY_HQ_LOC, loc.x, loc.y, Util.rand(),Util.rand(),Util.rand()};
		sendMessage(m, 1);

	}
	
    public MapLocation getHQLocFromBlockchain() throws GameActionException {
    	for(int i = 0; i < 3; i++) {
			int[] m = checkForMessage(M_HQ_LOC, i);
			if(m != null) {
				return new MapLocation(m[0],m[1]);
			}
    	}
		return null;
    }
    
    public MapLocation getEnemyHQLocFromBlockchain() throws GameActionException {
    	//TODO: finish method
    	return null;
    }

    public void sendDesiredSchoolPlacement(MapLocation loc) throws GameActionException {
    	int[] m = {M_BUILD_SCHOOL, loc.x, loc.y, Util.rand(),Util.rand(),Util.rand()};
		sendMessage(m, 1);
    }

    public MapLocation getDesiredSchoolPlacement() throws GameActionException {
    	int[] m = checkForMessage(M_BUILD_SCHOOL);
    	if(m == null)
    		return null;
    	return new MapLocation(m[0],m[1]);
    }
    
    public void broadcastCreation() throws GameActionException {
    	int type = 0;
    	RobotType t = rc.getType();
    	if(t.equals(RobotType.LANDSCAPER)) {
    		type = M_LANDSCAPER;
    	} else if(t.equals(RobotType.DELIVERY_DRONE)) {
    		type = M_DRONE;
    	} else if(t.equals(RobotType.VAPORATOR)) {
    		type = M_VAPORATOR;
    	} else if(t.equals(RobotType.DESIGN_SCHOOL)) {
    		type = M_SCHOOL;
    	} else {
    		type = M_FULFILLMENT_CENTER;
    	}
    	int[] m = {type, Util.rand(), Util.rand(), Util.rand(), Util.rand(), Util.rand()};
    	sendMessage(m, 1);
    	
    }
    
    public void broadcastRobotCounts(int[] m) throws GameActionException {
    	m[0] = M_ROBOT_COUNTS;
    	sendMessage(m, 1);
    }
}
