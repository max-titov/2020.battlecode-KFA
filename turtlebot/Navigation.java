package turtlebot;
import battlecode.common.*;

public class Navigation {
    RobotController rc;

	final int MAP_WIDTH;
	final int MAP_HEIGHT;
    
	int prevLocsSize = 5;
	MapLocation[] prevLocs = new MapLocation[prevLocsSize];
	int prevLocsIndex = 0;

	MapLocation[][] visionCircles = {
			{l(0,0)},
			{l(-1,0),l(0,-1),l(1,0),l(0,1)},
			{l(-2,-1),l(-2,0),l(-2,1),l(-1,1),
				l(-1,2),l(0,2),l(1,2),l(1,1),
				l(2,1),l(2,0),l(2,-1),l(1,-1),
				l(1,-2),l(0,-2),l(-1,-2),l(-1,-1)},
			{l(-3,-2),l(-3,-1),l(-3,0),l(-3,1),
				l(-3,2),l(-2,2),l(-2,3),l(-1,3),
				l(0,3),l(1,3),l(2,3),l(2,2),
				l(3,2),l(3,1),l(3,0),l(3,-1),
				l(3,-2),l(2,-2),l(2,-3),l(1,-3),
				l(0,-3),l(-1,-3),l(-2,-3),l(-2,-2)},
			{l(-4,-2),l(-4,-1),l(-4,0),l(-4,1),
				l(-4,2),l(-3,3),l(-2,4),l(-1,4),
				l(0,4),l(1,4),l(2,4),l(3,3),
				l(4,2),l(4,1),l(4,0),l(4,-1),
				l(4,-2),l(3,-3),l(2,-4),l(1,-4),
				l(0,-4),l(-1,-4),l(-2,-4),l(-3,-3)},
			{l(-5,-3),l(-5,-2),l(-5,-1),l(-5,0),
				l(-5,1),l(-5,2),l(-5,3),l(-4,3),
				l(-4,4),l(-3,4),l(-3,5),l(-2,5),
				l(-1,5),l(0,5),l(1,5),l(2,5),
				l(3,5),l(3,4),l(4,4),l(4,3),
				l(5,3),l(5,2),l(5,1),l(5,0),
				l(5,-1),l(5,-2),l(5,-3),l(4,-3),
				l(4,-4),l(3,-4),l(3,-5),l(2,-5),
				l(1,-5),l(0,-5),l(-1,-5),l(-2,-5),
				l(-3,-5),l(-3,-4),l(-4,-4),l(-4,-3)},
			{l(0,0)}
	};
	
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
    
    void noReturnNavDrone(Direction dir) throws GameActionException {
		MapLocation currentLoc = rc.getLocation();
		Direction[] toTry = {dir, dir.rotateLeft(), dir.rotateRight(), dir.rotateLeft().rotateLeft(), dir.rotateRight().rotateRight(), dir.rotateLeft().rotateLeft().rotateLeft(), dir.rotateRight().rotateRight().rotateRight(), dir.opposite()};
		int lenToTry = toTry.length;
		for(int i = 0; i<lenToTry; i++) {
			boolean movedThereAlready = false;
			int len = prevLocs.length+prevLocsIndex;
			for(int j = prevLocsIndex;j<len; j++) {
				MapLocation testLoc = prevLocs[j%prevLocs.length];
				if(testLoc!= null) {
					if(currentLoc.add(toTry[i]).equals(testLoc)) {
						movedThereAlready=true;
						break;
					}
				}
			}
			if(!movedThereAlready && rc.canMove(toTry[i])) {
				rc.move(toTry[i]);
				prevLocs[prevLocsIndex] = currentLoc;
				prevLocsIndex=(prevLocsIndex+1)%prevLocs.length;
				return;
			}
		}
		//if cant move reset previous locations array
		prevLocs = new MapLocation[prevLocsSize];
		prevLocsIndex=0;
    }
    
    void noReturnNavDrone(MapLocation destination) throws GameActionException {
        noReturnNavDrone(rc.getLocation().directionTo(destination));
    }
    
	/**
	 * 
	 * @return the current radius in tiles
	 * @throws GameActionException
	 */
	int radiusInTiles() throws GameActionException {
		int radiusInTiles = 1;
		while (rc.canSenseRadiusSquared(radiusInTiles * radiusInTiles)) {
			radiusInTiles++;
		}
		radiusInTiles--;
		return radiusInTiles;
	}

	MapLocation l(int x, int y) {
		return new MapLocation(x, y);
	}

}