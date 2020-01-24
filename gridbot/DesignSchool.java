package gridbot;
import battlecode.common.*;

public class DesignSchool extends Building {
    public DesignSchool(RobotController r) {
        super(r);
    }

    public void takeTurn() throws GameActionException {
        super.takeTurn();
        for(int i = 0; i < Util.dirsLen; i++) {
			if(tryBuild(RobotType.LANDSCAPER, Util.dirs[i])) {
				
			}
        }
    }
}
