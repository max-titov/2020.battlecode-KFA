package gridbot;
import battlecode.common.*;

public class DesignSchool extends Building {
    int schoolType = 0;
    final int FIRST_SCHOOL = 1;
	
    boolean shouldBuildLandscapers = true;
	
	public DesignSchool(RobotController r) throws GameActionException {
        super(r);
        findHQ();
    }

    public void takeTurn() throws GameActionException {
        super.takeTurn();
        if(schoolType == 0) {
        	if(rc.getLocation().isAdjacentTo(hqLoc)) {
        		schoolType = FIRST_SCHOOL;
        	}
        }
        
        switch(schoolType) {
        case FIRST_SCHOOL:
        	runFirstSchool();
        	break;
        }
    }
    
    void runFirstSchool() throws GameActionException {
    	if(shouldBuildLandscapers) {
	    	for(int i = 0; i < Util.dirsLen; i++) {
				if(tryBuild(RobotType.LANDSCAPER, Util.dirs[i])) {
					
				}
	        }
    	}
    }
}
