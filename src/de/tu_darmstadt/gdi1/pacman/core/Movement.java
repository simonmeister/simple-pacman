package de.tu_darmstadt.gdi1.pacman.core;

/**
 * Represents a player's movement.
 * 
 * @author smeister
 */
public enum Movement {
	RIGHT(0, 1),
	UP(-1, 0),
	LEFT(0, -1),
	DOWN(1, 0);
	
	public final int x;
	public final int y;
	
	private Movement(int x, int y) {
		this.x = x;
		this.y = y;
	}
	
	/**
	 * Returns the movement, inverted.
	 * 
	 * @param mv	a movement
	 * @return		the inverse of mv
	 */
	public static Movement invert(Movement mv) {
		if(mv == RIGHT)
			return LEFT;
		else if(mv == LEFT)
			return RIGHT;
		else if(mv == UP)
			return DOWN;
		else
			return UP;
	}
	
	/**
	 * Creates a movement from coordinates.
	 * 
	 * @param x
	 * @param y
	 * @return a Movement (x, y)
	 */
	public static Movement fromCoords(int x, int y) {
		for(Movement mv : Movement.values())
			if (mv.x == x && mv.y == y)
				return mv;
		throw new IllegalArgumentException(x + ", " + y + " is not a valid movement");
	}
}
