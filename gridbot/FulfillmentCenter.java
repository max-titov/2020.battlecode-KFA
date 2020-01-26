package gridbot;
import battlecode.common.*;

public class FulfillmentCenter extends Building {
    public FulfillmentCenter(RobotController r) throws GameActionException {
        super(r);
    }

    public void takeTurn() throws GameActionException {
        super.takeTurn();
        RobotType robotToBuild = buildPriority();
    	if(robotToBuild!= null && robotToBuild.equals(RobotType.DELIVERY_DRONE)) {
	    	for(int i = 0; i < Util.dirsLen; i++) {
				if(tryBuild(RobotType.DELIVERY_DRONE, Util.dirs[i])) {
					
				}
	        }
    	}
    }
}
