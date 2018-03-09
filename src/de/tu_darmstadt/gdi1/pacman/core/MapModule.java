package de.tu_darmstadt.gdi1.pacman.core;

/**
 * Represents a field of a level.
 * 
 * @author smeister
 */
public enum MapModule {
	
	PLAYER_SPAWN	('P', true, false),
	GHOST_SPAWN		('G', false, false),
	WALL			('X', false, true),
	SPEED_UP		('S', false, false),
	TELEPORT		('T', false, false),
	POWER_UP		('U', false, false),
	BACKGROUND		('B', false, true),
	EMPTY			(' ', true, false);
	
	public final char value;		// The code
	public final boolean hasItem;	// Whether an item can be placed on fields of this type
	public final boolean obstacle;  // Whether a creature can not move through fields of this type
	
	private MapModule(char c, boolean hasItem, boolean obstacle) {
		value = c;
		this.hasItem = hasItem;
		this.obstacle = obstacle;
	}
	
	/**
	 * Returns the MapModule with "value" c
	 * @param c		the value to select
	 * @return		a MapModule
	 */
	public static MapModule fromChar(char c) {
		for(MapModule m : MapModule.values())
			if (c == m.value)
				return m;
		throw new IllegalArgumentException(c + " is not a valid character");
	}
}