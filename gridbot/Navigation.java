package gridbot;
import battlecode.common.*;

public class Navigation {
    RobotController rc;

	final int MAP_WIDTH;
	final int MAP_HEIGHT;
    
	int prevLocsSize = 5;
	MapLocation[] prevLocs = new MapLocation[prevLocsSize];
	int prevLocsIndex = 0;

    public Navigation(RobotController r) {
        rc = r;
        MAP_WIDTH = rc.getMapWidth();
        MAP_HEIGHT = rc.getMapHeight();
    }
    
    boolean tryMove(Direction dir) throws GameActionException {
        if (rc.canMove(dir) && !rc.senseFlooding(rc.getLocation().add(dir))) {
            rc.move(dir);
            return true;
        }
        return false;
    }

    void noReturnNav(Direction dir) throws GameActionException {
		MapLocation currentLoc = rc.getLocation();
		Direction[] toTry = {dir, dir.rotateLeft(), dir.rotateRight(), dir.rotateLeft().rotateLeft(), dir.rotateRight().rotateRight(), dir.rotateLeft().rotateLeft().rotateLeft(), dir.rotateRight().rotateRight().rotateRight(), dir.opposite()};
		int lenToTry = toTry.length;
		for(int i = 0; i<lenToTry; i++) {
			boolean movedThereAlready = false;
			int len = prevLocs.length+prevLocsIndex;
			for(int j = prevLocsIndex;j<len; j++) {
				MapLocation testLoc = prevLocs[j%prevLocs.length];
				if(testLoc!= null) {
//					rc.setIndicatorDot(testLoc, 0, 255-j*20, j*20);
					if(currentLoc.add(toTry[i]).equals(testLoc)) {
						movedThereAlready=true;
						break;
					}
				}
			}
			if(!movedThereAlready && tryMove(toTry[i])) {
				prevLocs[prevLocsIndex] = currentLoc;
				prevLocsIndex=(prevLocsIndex+1)%prevLocs.length;
				return;
			}
		}
		//if cant move reset previous locations array
		prevLocs = new MapLocation[prevLocsSize];
		prevLocsIndex=0;
    }

    // navigate towards a particular location
    void noReturnNav(MapLocation destination) throws GameActionException {
        noReturnNav(rc.getLocation().directionTo(destination));
    }
}