package turtlebot;
import battlecode.common.*;

public class DesignSchool extends Building {
	int requiredLandscapers = 0;
	
	public DesignSchool(RobotController r) throws GameActionException {
        super(r);
        calculateRequiredLandscapers();
    }

    public void takeTurn() throws GameActionException {
        super.takeTurn();
        Direction desiredDir = rc.getLocation().directionTo(hqLoc).rotateLeft();
		if(landscaperCount < requiredLandscapers+1 && rc.getTeamSoup() >210) {
			for(int i = 0; i < Util.dirsLen; i++) {
				if(tryBuild(RobotType.LANDSCAPER, desiredDir)) {
					break;
				}else {
					desiredDir = desiredDir.rotateRight();
				}
			}
		}
    }
    
    void calculateRequiredLandscapers() throws GameActionException{
    	int unadjustedInnerWallLocsLen = unadjustedInnerWallLocs.length;
		for(int i = 0; i <unadjustedInnerWallLocsLen; i++) {
			MapLocation testLoc = unadjustedInnerWallLocs[i].translate(hqLoc.x, hqLoc.y);
			if(onMap(testLoc) && distanceToCorner(testLoc)>1) {
				requiredLandscapers++;
			}
		}
		int unadjustedOuterWallLocsLen = unadjustedOuterWallLocs.length;
		for(int i = 0; i <unadjustedOuterWallLocsLen; i++) {
			MapLocation testLoc = unadjustedOuterWallLocs[i].translate(hqLoc.x, hqLoc.y);
			if(onMap(testLoc) && distanceToCorner(testLoc)>1) {
				requiredLandscapers++;
			}
		}
    }
    
    
}
