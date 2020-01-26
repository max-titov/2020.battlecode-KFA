package gridbot;
import battlecode.common.*;

public class DroneTemp extends Unit {
	
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
	
    public DroneTemp(RobotController r) throws GameActionException {
        super(r);
    }

    public void takeTurn() throws GameActionException {
        super.takeTurn();
        nav.tryMove(Util.randomDirection());
    }
	

}