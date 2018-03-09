package de.tu_darmstadt.gdi1.pacman.core;

import de.tu_darmstadt.gdi1.pacman.exceptions.NoGhostSpawnPointException;
import de.tu_darmstadt.gdi1.pacman.exceptions.NoItemsException;
import de.tu_darmstadt.gdi1.pacman.exceptions.NoPacmanSpawnPointException;
import de.tu_darmstadt.gdi1.pacman.exceptions.ReachabilityException;

import java.awt.Point;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Random;

/**
 * Represents the (generally) static parts of a game.
 * For simplicity, collectibles (speed/power-ups) can be removed from
 * the level after the game was started.
 * 
 * @author smeister
 */
public class Level {
	
	private int width, height;
	private MapModule[][] grid;
	private Random rng;
	
	public Level(int width, int height) {
		rng = new Random();
		grid = new MapModule[height][width];
		
		// Initialize the grid. Note that this level does not validate yet.
		// It must be mutated by calls to setField.
		for(int i = 0; i < height; ++i)
			for(int j = 0; j < width; ++j)
				grid[i][j] = MapModule.EMPTY;
		this.width = width;
		this.height = height;
	}
	
	/**
	 * Call this to make sure a level is valid to be played.
	 * 
	 * @throws NoPacmanSpawnPointException
	 * @throws ReachabilityException
	 * @throws NoGhostSpawnPointException
	 * @throws NoItemsException
	 */
	public void validate()  throws NoPacmanSpawnPointException, ReachabilityException, NoGhostSpawnPointException, NoItemsException {
		// Compress all of the level into a single string
		StringBuilder sb = new StringBuilder();
		for(int i = 0; i < height; ++i)
			for(int j = 0; j < width; ++j)
				sb.append(grid[i][j].value);
		String chars = sb.toString();
		// Check for the presence of required modules
		if(!chars.contains("P"))
			throw new NoPacmanSpawnPointException();
		if(!chars.contains("G"))
			throw new NoGhostSpawnPointException();
		if(!chars.contains(" "))
			throw new NoItemsException();	
		// Enforce reachability (using a simple made-up algorithm)
		HashSet<String> mustBeReachable = new HashSet<String>();
		for(int i = 0; i < height; ++i)
			for(int j = 0; j < width; ++j)
				if(grid[i][j] != MapModule.WALL && grid[i][j] != MapModule.BACKGROUND)
					mustBeReachable.add(i + " " + j);
			// Find a point that is not a wall, then try to accumulate
			// a set of all points reachable from this point. If this
			// set contains all non-wall points, reachability is guaranteed for all points.
		for(int i = 0; i < height; ++i) {
			for(int j = 0; j < width; ++j) {
				if(grid[i][j] != MapModule.WALL && grid[i][j] != MapModule.BACKGROUND) {
					// Collect all reachable points from this point
					HashSet<String> set = new HashSet<String>();
					expand(set, i, j, grid);		
					if(!set.equals(mustBeReachable))
						throw new ReachabilityException();
					return;
				}
			}
		}
	}
	
	/**
	 * Adds all points to the set that are reachable from (x, y), if they are
	 * not already contained.
	 * 
	 * @param set
	 * @param x
	 * @param y
	 * @param grid
	 */
	private void expand(HashSet<String> set, int x, int y, MapModule[][] grid) {
		set.add(x + " " + y);
		
		// Select neighbouring points if we are not on the border,
		// try to wrap around otherwise.
		int[][] neighbourMvIncrements = {
			{0, 1}, {1, 0}, {-1, 0}, {0, -1}
		};
		// Expand once for every possible movement.
		for(int[] inc: neighbourMvIncrements) {
			Point p = wrapCoordinates(x+inc[0], y+inc[1], true);
			expandNeighbour(set, p.x, p.y, grid);
		}
	}
	
	/**
	 * Adds all neighbours of field (x, y) to the set.
	 * 
	 * @param set
	 * @param x
	 * @param y
	 * @param grid
	 */
	private void expandNeighbour(HashSet<String> set, int x, int y, MapModule[][] grid) {
		if(!set.contains(x + " " + y) && grid[x][y] != MapModule.WALL)
			expand(set, x, y, grid);
	}
	
	/**
	 * Select a random field of type "type" and return the position.
	 * 
	 * @param type	the field type
	 * @return		it's position
	 */
	public Point getRandomFieldPosition(MapModule type) {
		ArrayList<Point> positions = getFieldPositions(type);
		return positions.get(rng.nextInt(positions.size()));
				
	}
	
	/**
	 * Select all fields of type "type" and return their positions. 
	 * 
	 * @param type 	the field type
	 * @return		the positions
	 */
	public ArrayList<Point> getFieldPositions(MapModule type) {
		ArrayList<Point> positions = new ArrayList<>();	
		for(int i = 0; i < height; ++i)
			for(int j = 0; j < width; ++j)
				if(grid[i][j] == type)
					positions.add(new Point(i, j));
		return positions;
	}
	
	/**
	 * @return 	the level's width (max. y component)
	 */
	public int getWidth() {
		return width;
	}
	
	/**
	 * @return 	the level's height (max. x component)
	 */	
	public int getHeight() {
		return height;
	}
	
	/**
	 * @param x
	 * @param y
	 * @return		the field content at (x, y)
	 */
	public MapModule get(int x, int y) {
		Point p = wrapCoordinates(x, y, true);
		return grid[p.x][p.y];
	}
	
	/**
	 * Normalizes a set of coordinates to wrap around, 
	 * if safe is true. Return null on invalid coordinates,
	 * if safe is false.
	 * 
	 * @param x
	 * @param y
	 * @param safe	if set to true, always return valid coordinates
	 * @return		a safe-to-use coordinate set
	 */
	public Point wrapCoordinates(int x, int y, boolean safe)  {
		boolean yl = y < 0, 
				yr = y >= width,
				xl = x < 0,
				xr = x >= height;
		
		if(!safe && (yl || yr || xl || xr))
			return null;
				
		if(yl) 		y = width-1;
		else if(yr)	y = 0;
		if(xl) 		x = height-1;
		else if(xr)	x = 0;
		
		return new Point(x, y);
	}
	
	public String toString() {
		StringBuilder sb = new StringBuilder();
		
		for(int i = 0; i < height; ++i) {
			for(int j = 0; j < width; ++j)
				sb.append(grid[i][j].value);
			if(i != height-1)
				sb.append('\n');
		}
		return sb.toString();
	}
	
	/**
	 * Changes to field content at (x, y) to the MapModule
	 * corresponding to c.
	 * 
	 * @param x		y
	 * @param y		x
	 * @param c		a field descriptor
	 */
	public void setField(int x, int y, char c) {
		grid[x][y] = MapModule.fromChar(c);
	}
	
	/**
	 * Collect a speed-up or power-up at (x, y).
	 * 
	 * @param x		x 
	 * @param y		y
	 * @return		whether the module at (x, y) was collectible
	 */
	public boolean collect(int x, int y) {
		if(grid[x][y] == MapModule.SPEED_UP || grid[x][y] == MapModule.POWER_UP) {
			grid[x][y] = MapModule.EMPTY;
			return true;
		}
		return false;
	}
	
	/**
	 * Tests whether there are any obstacles in the path from
	 * 'from' to 'to' generated by movements of 'xinc' and 'yinc'
	 * increments.
	 * CAUTION: Make sure that the parameter combination makes
	 * sense and the movement is theoretically possible.
	 * 
	 * @param from		a point
	 * @param to		a point
	 * @param xinc  	the linear x increment
	 * @param yinc		the linear y increment
	 * @param testing	whether the game runs in test mode
	 * @return			-1 if the movement is impossible, the number of
	 * 					steps it takes otherwise
	 */
	public int linearMovementPossible(Point from, Point to, int xinc, int yinc, boolean testing) {
		Point current = new Point(from);
		int steps = 0;
		while(current.x != to.x || current.y != to.y) {
			if(get(current.x, current.y).obstacle)
				return -1;
			current.x += xinc;
			current.y += yinc;
			++steps;
		
			current = wrapCoordinates(current.x, current.y, !testing);
			if(current == null) return -1;
		}
		boolean ok = !get(current.x, current.y).obstacle;
		// Make sure that the steps are not in fact zero, in which
		// case we do not have to move.
		return (ok && steps > 0)? steps: -1;
	}
}