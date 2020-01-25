package testDrone;

import battlecode.common.*;

// This is a file to accumulate all the random helper functions
// which don't interact with the game, but are common enough to be used in multiple places.
// For example, lots of logic involving MapLocations and Directions is common and ubiquitous.
public class Util {
	static Direction[] dirs = { Direction.NORTH, Direction.NORTHEAST, Direction.EAST, Direction.SOUTHEAST,
			Direction.SOUTH, Direction.SOUTHWEST, Direction.WEST, Direction.NORTHWEST };
	static int dirsLen = dirs.length;
	static Direction[] allDirs = Direction.allDirections();
	static int allDirsLen = allDirs.length;

	static Direction[] cardinalDirs = { Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST };
	static int cardinalDirsLen = cardinalDirs.length;
	static Direction[] diagonalDirs = { Direction.NORTHEAST, Direction.SOUTHEAST, Direction.SOUTHWEST,
			Direction.NORTHWEST };
	static int diagonalDirsLen = diagonalDirs.length;

	/**
	 * Returns a random Direction.
	 *
	 * @return a random Direction
	 */
	static Direction randomDirection() {
		return dirs[(int) (Math.random() * dirs.length)];
	}

	static int rand(int max) throws GameActionException {
		return (int) (Math.random() * max);
	}

	static int rand() throws GameActionException {
		return (int) (Math.random() * 500) + 1;
	}
}
