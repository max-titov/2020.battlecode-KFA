package turtlebot;
import battlecode.common.*;

public class FulfillmentCenter extends Building {
	int requiredLandscapers = 0;
    public FulfillmentCenter(RobotController r) throws GameActionException {
        super(r);
        calculateRequiredLandscapers();
    }

    public void takeTurn() throws GameActionException {
        super.takeTurn();
    	if(droneCount==0) {
        	for(int i = 0; i < Util.dirsLen; i++) {
    			if(tryBuild(RobotType.DELIVERY_DRONE, Util.dirs[i])) {
    				
    			}
            }
        } else if(landscaperCount > requiredLandscapers && rc.getTeamSoup() >210){
        	for(int i = 0; i < Util.dirsLen; i++) {
				if(tryBuild(RobotType.DELIVERY_DRONE, Util.dirs[i])) {
					
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
