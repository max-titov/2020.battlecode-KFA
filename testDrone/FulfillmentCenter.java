package testDrone;
import battlecode.common.*;

public class FulfillmentCenter extends Building {
    public FulfillmentCenter(RobotController r) throws GameActionException {
        super(r);
    }

    public void takeTurn() throws GameActionException {
        super.takeTurn();
        tryBuild(RobotType.DELIVERY_DRONE, Direction.NORTH);
    }
}
