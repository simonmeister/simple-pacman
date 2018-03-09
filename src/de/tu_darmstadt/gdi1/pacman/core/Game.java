package de.tu_darmstadt.gdi1.pacman.core;

import java.awt.Point;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

import org.newdawn.slick.Music;
import org.newdawn.slick.SlickException;

import de.tu_darmstadt.gdi1.pacman.exceptions.LevelException;
import de.tu_darmstadt.gdi1.pacman.ui.GridRenderer;
import eea.engine.entity.StateBasedEntityManager;

/**
 * Represents a single game session.
 * Always initialize a level calling changeLevel(level)
 * before attempting to update/step the game. 
 * To update, call update() or, for fine grained control
 * use  
 *		updatePacman(), 	(optional)
 *		updateGhosts(), 	(optional)
 *		updateCollisions(),
 * 		finishUpdate()
 * 
 * Call
 * 		updatePacmanAnimations()
 * 		updateGhostAnimations()
 * 
 * every time the display is updated to ensure smooth
 * movements. Note that these functions do not manipulate
 * game state.
 * 
 * @author smeister
 */
public class Game {
	protected Level level;
	protected ArrayList<Ghost> ghosts;
	protected Pacman pacman;
	protected int INITIAL_LIFES = 3;
	
	// The probability of spawning a ghost in an update
	protected static float GHOST_SPAWN_PROBABILITY = 0.3f;
	// The number of update cicles X-Up's last
	protected int SPEEDUP_DURATION = 30;
	protected int POWERUP_DURATION = 22;
	
	// The probability of a ghost changing direction spontaneously
	float RANDOM_CHANGEDIR = 0.2f;
	// The maximum number of ghosts alive at any time
	protected int GHOST_LIMIT = 3;
	
	protected Random rng;
	protected HashMap<Point, String> items;
	
	private StateBasedEntityManager entityManager;
	private GridRenderer renderer;
	
	//Sounds
	//private Music collectSound;
	private Music deathSound;
	private Music eatGhost;
	private Music powerUpSound;
	private Music speedUpSound;
	
	/**
	 * This is used in some places to make sure ghosts
	 * do not cross level borders in test mode.
	 * 
	 * @return	whether the game runs in test mode
	 */
	public boolean testing() {
		return false;
	}
	
	public Game(StateBasedEntityManager entityManager, GridRenderer renderer) throws SlickException {
		ghosts = new ArrayList<>();
		rng = new Random();
		items = new HashMap<>();
		pacman = new Pacman(new Point(7, 7), renderer, entityManager, POWERUP_DURATION, INITIAL_LIFES);
		this.entityManager = entityManager;
		this.renderer = renderer;
		//Load sounds
		//collectSound =  new Music("res/sounds/pacman_chomp.wav");
		deathSound = new Music("res/sounds/pacman_death.wav");
		eatGhost = new Music("res/sounds/pacman_eatghost.wav");
		powerUpSound = new Music("res/sounds/pacman_intermission.wav");
		speedUpSound = new Music("res/sounds/pacman_speedUp.wav");
	}
	
	/**
	 * Proceed to the next level.
	 * 
	 * @param level 	the new level
	 */
	public void changeLevel(Level level) {
		this.level = level;
		items.clear();
		for(int i = 0; i < level.getHeight(); ++i) {
			for(int j = 0; j < level.getWidth(); ++j) {
				if(level.get(i, j).hasItem)
					items.put(new Point(i, j), "");
			}
		}
		clearGhosts();
		spawnPacman();
		update();
	}
	
	/**
	 * Call this when the game is to be deleted.
	 */
	public void finalize() {
		pacman.finalize();
		for(Ghost ghost: ghosts)
			ghost.finalize();
	}
	
	/**
	 * Spawns the pacman. Do not call with a null level.
	 */
	private void spawnPacman() {
		if(level != null) {
			Point pos = level.getRandomFieldPosition(MapModule.PLAYER_SPAWN);
			pacman.setPos(pos);
		}	
	}
	
	/**
	 * @return the current level
	 */
	public Level getLevel() {
		return level;
	}
	
	/**
	 * Kills all ghosts.
	 */
	public void clearGhosts() {
		for(Ghost ghost: ghosts)
			ghost.finalize();
		ghosts.clear();
	}
	
	/**
	 * @return all ghosts in a list
	 */
	public ArrayList<Ghost> getGhosts() {
		return ghosts;
	}
	
	/**
	 * Moves a figure. This does not check the movement, so you must make 
	 * sure to call isValidMovement() before passing arbitrary movements.
	 * 
	 * @param creature	the figure
	 * @param mv		the movement
	 * @return			true iff the figure was moved
	 */
	private void moveCreature(Creature creature, Movement mv) {	
		creature.setPos(getTargetPosition(creature, mv, true));
	}
	
	/**
	 * Returns the position 'creature' goes to when the specified
	 * movement is executed. May return null if wrap is false
	 * and the movement would have to cross a border.
	 *  
	 * @param creature	a creature
	 * @param mv		a movement
	 * @param wrap		consider movements across the level borders
	 * @return			the new point
	 */
	private Point getTargetPosition(Creature creature, Movement mv, boolean wrap) {
		int x = creature.getPos().x;
		int y = creature.getPos().y;
		
		// Make sure that we do not move into a wall/background.
		// Wrap around if we are on a border field.
		x += mv.x;
		y += mv.y;
		return level.wrapCoordinates(x, y, wrap);
	}
	
	/**
	 * Validates a movement.
	 * 
	 * @param creature	a creature
	 * @param mv		the movement to be validated
	 * @param wrap		consider movements across the level borders
	 * @return			true if creature can execute the movement
	 */
	private boolean isValidMovement(Creature creature, Movement mv, boolean wrap) {
		Point p = getTargetPosition(creature, mv, wrap);
		if(p == null) return false;
		MapModule target = level.get(p.x, p.y);
		return !target.obstacle;
	}
	
	/**
	 * Moves a ghost, depending on whether there is sight contact
	 * with pacman.
	 * 
	 * @param ghost		the ghost to move
	 * @return			the movement done
	 */
	private Movement moveGhost(Ghost ghost) {	
		Point ghostPos = ghost.getPos(),
			  pacmanPos = pacman.getPos();
		
		boolean xmatch = ghostPos.x == pacmanPos.x,
				ymatch = ghostPos.y == pacmanPos.y;
		
		if(xmatch || ymatch) {
			Movement mv = null;
			// Move along the matching axis.
			int xinc = ymatch? 1: 0,
				xdec = -xinc,
				yinc = ymatch? 0: 1,
				ydec = -yinc;
			
			// If there is sight contact in one axis, move directly towards pacman.
			// Always choose the shortest path.
			if(!pacman.hasPowerUp()) {
				int incSteps = level.linearMovementPossible(ghostPos, pacmanPos, xinc, yinc, testing());
				int decSteps = level.linearMovementPossible(ghostPos, pacmanPos, xdec, ydec, testing());
				
				// Choose the movement (incremental or decremental) that requires the smallest amount of
				// steps toward our goal (pacman).
				if(incSteps != -1 && decSteps != -1)
					mv = incSteps < decSteps? Movement.fromCoords(xinc, yinc) : 
						Movement.fromCoords(xdec, ydec); 
				else if(incSteps != -1)
					mv = Movement.fromCoords(xinc, yinc);
				else if(decSteps != -1)
					mv = Movement.fromCoords(xdec, ydec);
			} 
			// Try to maximize the distance. 
			// Imitate Pacman's shortest distance movement, if there are no obstacles in the way.
			else {
				int incSteps = level.linearMovementPossible(pacmanPos, ghostPos, xinc, yinc, testing());
				int decSteps = level.linearMovementPossible(pacmanPos, ghostPos, xdec, ydec, testing());
				
				if(incSteps != -1 && decSteps != -1)
					mv = incSteps < decSteps? Movement.fromCoords(xinc, yinc) : 
						Movement.fromCoords(xdec, ydec); 
				else if(incSteps != -1 && !level.get(ghostPos.x + xinc, ghostPos.y + yinc).obstacle)
					mv = Movement.fromCoords(xinc, yinc);
				else if(decSteps != -1 && !level.get(ghostPos.x + xdec, ghostPos.y + ydec).obstacle)
					mv = Movement.fromCoords(xdec, ydec);
			}
			if(mv != null) {
				moveCreature(ghost, mv);
				return mv;
			}
		}
		
		// Otherwise generate a movement (or preserve the last) and check if it's valid.
		Movement mv = ghost.getLastMovement();
		if(mv == null || rng.nextFloat() < RANDOM_CHANGEDIR) {
			if(rng.nextBoolean())
				mv = rng.nextBoolean()? Movement.LEFT: Movement.RIGHT;
			else
				mv = rng.nextBoolean()? Movement.UP: Movement.DOWN;	
		}
		// Make sure the movement is valid.
		if(isValidMovement(ghost, mv, !testing())) {
			moveCreature(ghost, mv);
			return mv;
		} // No? Try again ...
		else {
			return moveGhost(ghost);
		}
	}
	
	/**
	 * @return	the game's pacman
	 */
	public Pacman getPacman() {
		return pacman;
	}
	
	/**
	 * Sets the next movement for pacman.
	 * 
	 * @param mv	the movement
	 * @return		whether the movement is possible
	 */
	public boolean movePacman(Movement mv) {
		if(mv == null || pacman.getNextMovement() != null || !isValidMovement(pacman, mv, true))
			return false;
		pacman.setNextMovement(mv);
		return true;
	}
	
	/**
	 * Moves all ghosts.
	 */
	public void updateGhosts() {
		for(Ghost ghost: ghosts)
			ghost.setLastMovement(moveGhost(ghost));
	}
	
	/**
	 * Moves pacman and collects collectibles and items.
	 */
	public void updatePacman() {
		if(level == null)
			return;
		
		Movement mv = pacman.getNextMovement();
		if(pacman.getNextMovement() != null)
			moveCreature(pacman, mv);
		pacman.update();
		
		Point pacmanPos = pacman.getPos();
		
		// Collect an item
		String item = items.remove(pacmanPos);
		if(item != null) {
			pacman.incPoints();
			//collectSound.play();
		}
		
		// Collect a speedUp / powerUp or teleport pacman
		MapModule mod = level.get(pacmanPos.x, pacmanPos.y);
		boolean collect = false;
		if(mod == MapModule.POWER_UP) {
			collect = true;
			pacman.powerUp();
			if (!testing()) powerUpSound.play(); //We don't want to play sounds while testing
			
		}
		else if(mod == MapModule.SPEED_UP) {
			collect = true;
			pacman.speedUp(SPEEDUP_DURATION);
			if (!testing()) speedUpSound.play();
		
		}
		if(collect) {
			level.collect(pacmanPos.x, pacmanPos.y);
			pacman.incPoints();
		}
		if(mod == MapModule.TELEPORT) {
			while(true) {
				Point pos = level.getRandomFieldPosition(MapModule.EMPTY);
				if(!pos.equals(pacmanPos)) {
					pacman.setPos(pos);
					break;
				}
			}
		}
	}
	
	/**
	 * Checks for collisions and executes them.
	 */
	public void updateCollisions() {
		Ghost collisionWith = null;
		
		// Check for collisions (and make sure that the creatures do not move through each other)
		Point pacmanPos = pacman.getPos();
		Movement lastPacmanMv = pacman.getLastMovement();
		Point lastPacmanPos = lastPacmanMv == null? 
				pacmanPos : new Point(pacmanPos.x-lastPacmanMv.x, pacmanPos.y-lastPacmanMv.y);	
		for(Ghost ghost: ghosts) {
			// Compute the ghosts previous position
			Point ghostPos = ghost.getPos();
			Movement lastGhostMv = ghost.getLastMovement();
			Point lastGhostPos = lastGhostMv == null? 
					ghostPos : new Point(ghostPos.x-lastGhostMv.x, ghostPos.y-lastGhostMv.y);
	
			if(ghostPos.equals(pacmanPos)
			   || (lastGhostPos.equals(pacmanPos) && lastPacmanPos.equals(ghostPos))) {
				collisionWith = ghost;
			}
		}

		// Execute
		if(collisionWith != null) {
			if(pacman.hasPowerUp()) {
				pacman.incKills();
				collisionWith.finalize();
				ghosts.remove(collisionWith);
				if (!testing()) eatGhost.play();
			}
			else {
				if(!pacman.isDead()) {
					pacman.hit();
					spawnPacman();
					if(!testing()) deathSound.play();
				}
				else 
					if (!testing()) deathSound.play();
				
			}
		}
	}
	
	/**
	 * Completes an update.
	 */
	public void finishUpdate() {
		pacman.useSpeedUp();
		pacman.usePowerUp();
		
		spawnGhost(pacman.getPos());
	}
	
	/**
	 * Performs a full update.
	 */
	public void update() {
		updateCollisions();
		updatePacman();
		updateGhosts();
		updateCollisions();
		finishUpdate();
	}
	
	/**
	 * Positions pacman, linearly interpolating between the 
	 * creature's current (next) and previous position.
	 * 
	 * @param currentMs		the current "position" 0 < currentMs <= duration
	 * @param duration		the duration of a single cycle
	 */
	public void updatePacmanAnimations(int currentMs, int duration) {
		pacman.updateAnimations(currentMs, duration);
	}
	
	/**
	 * Positions the ghosts, linearly interpolating between the 
	 * creature's current (next) and previous position.
	 * 
	 * @param currentMs		the current "position" 0 < currentMs <= duration
	 * @param duration		the duration of a single cycle
	 */
	public void updateGhostAnimations(int currentMs, int duration) {
		for(Ghost ghost: ghosts)
			ghost.updateAnimations(currentMs, duration);
	}
	
	/**
	 * Spawn a ghost if possible, with probability as specified by GHOST_SPAWN_PROBABILITY.
	 * The ghost is never spawned at pacman's position.
	 * 
	 * @param pacmanPos		pacman's position
	 */
	protected void spawnGhost(Point pacmanPos) {
		// Spawn a ghost if possible
		if(ghosts.size() < GHOST_LIMIT && rng.nextFloat() <= GHOST_SPAWN_PROBABILITY) {
			Point pos = level.getRandomFieldPosition(MapModule.GHOST_SPAWN);
			if(pos != pacmanPos) {
				Ghost ghost = new Ghost(pos, renderer, entityManager);
				ghosts.add(ghost);
			}
		}	
	}
	
	/**
	 * @return whether the game is won
	 */
	public boolean isWon() {
		return items.isEmpty();
	}
	
	/**
	 * @param x		x
	 * @param y		y
	 * @return		true if there is an item at (x, y)
	 */
	public boolean hasItemAt(int x, int y) {
		return items.containsKey(new Point(x, y));
	}
	
	/**
	 * @return whether the game is lost
	 */
	public boolean isLost() {
		return pacman.isDead();
		
	}
	
	/**
	 * @return	the player's score
	 */
	public int getPoints() {
		return pacman.getPoints();
	}
	
	/**
	 * @return	how many ghosts were killed
	 */
	public int getKills() {
		return pacman.getKills();
	}
	
	/**
	 * Stringifies the game.
	 */
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(level.toString());
		sb.append("\n\n");
		sb.append(pacman.toString());
		sb.append("\n\n");
		for(Ghost ghost: ghosts) {
			sb.append(ghost.toString());
			sb.append("\n");
		}
		if(ghosts.size() == 0)
			sb.append("\n");
		sb.append("\n");
		for(Point pos: items.keySet()) {
			sb.append(pos.x + "," + pos.y);
			sb.append("\n");
		}
		return sb.toString();
	}
	
	/**
	 * Loads a game from String.
	 * 
	 * @param s		the string
	 * @return		a valid game
	 * @throws SlickException 
	 */
	public static Game fromString(String s, GridRenderer renderer, StateBasedEntityManager entityManager) throws SlickException {
		String[] parts = s.split("\\n\\n");
		Game game = new Game(entityManager, renderer);
		try {
			Level level = LevelParser.fromString(parts[0]);
			level.validate();
			game.level = level;
		} catch (LevelException e) {
			return null;
		}
		game.pacman = Pacman.fromString(parts[1], renderer, entityManager);
		String[] ghosts = parts[2].split("\n");
		for(String g: ghosts)
			game.ghosts.add(Ghost.fromString(g, renderer, entityManager));
		String[] items = parts[3].split("\n");
		for(String i: items) {
			String[] item = i.split(",");
			game.items.put(new Point(Integer.parseInt(item[0]), Integer.parseInt(item[1])), "");
		}
		return game;
	}
	
	/**
	 * @return the last valid movement executed
	 */
	public Movement getLastMovement() {
		return pacman.getLastMovement();
	}
}
