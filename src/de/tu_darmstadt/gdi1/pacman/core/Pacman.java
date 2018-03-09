package de.tu_darmstadt.gdi1.pacman.core;

import java.awt.Point;

import de.tu_darmstadt.gdi1.pacman.ui.GridRenderer;
import eea.engine.entity.StateBasedEntityManager;

/**
 * The pacman creature.
 * 
 * @author smeister
 */
public class Pacman extends Creature {
	
	private Movement nextMovement;
	
	private int powerUp = 0;
	private int speedUp = 0;
	private int powerUpDuration;
	private int lifes;
	private int kills = 0;
	private int points = 0;
	
	private String lastFacingDirection;
	
	public Pacman(Point p, GridRenderer renderer, StateBasedEntityManager entityManager, int powerUpDuration, int lifes) {
		super(p, renderer, entityManager);
		this.powerUpDuration = powerUpDuration;
		this.lifes = lifes;
	}
	
	public boolean usePowerUp() {
		if(powerUp > 0) {
			--powerUp;
			return true;
		}
		return false;
	}
	
	public boolean hasPowerUp() {
		return powerUp > 0;
	}
	
	public boolean hasSpeedUp() {
		return speedUp > 0;
	}
	
	public void powerUp() {
		powerUp += powerUpDuration;
	}
	
	public void deactivatePowerUp() {
		powerUp = 0;
	}
	
	public void speedUp(int duration) {
		speedUp += duration;
	}
	
	public boolean useSpeedUp() {
		if(speedUp > 0) {
			--speedUp;
			return true;
		}
		return false;
	}
	
	public Movement getNextMovement() {
		return nextMovement;
	}
	
	public void setNextMovement(Movement mv) {
		nextMovement = mv;
	}
	
	/**
	 * Call this during the game update.
	 */
	public void update() {
		this.lastMovement = nextMovement;
		nextMovement = null;
	}

	public boolean isDead() {
		return lifes == 0;
	}
	
	/**
	 * Hit pacman. Resets any speedUp effects.
	 */
	public void hit() {
		if(!isDead()) {
			--lifes;
			speedUp = 0;
		}
	}
	
	public int getLifes() {
		return lifes;
	}
	
	public void incPoints() {
		points++;
	}
	
	public int getPoints() {
		return points;
	}
	
	public void incKills() {
		kills++;
	}
	
	public int getKills() {
		return kills;
	}
	
	public String toString() {
		return getPos().x + " " + getPos().y + " " + lifes + " " + powerUp + " " 
				+ powerUpDuration + " " + kills + " " + points;
	}
	
	public static Pacman fromString(String s, GridRenderer renderer, StateBasedEntityManager entityManager) {
		String[] parts = s.split(" ");
		Pacman pacman = new Pacman(new Point(Integer.parseInt(parts[0]), Integer.parseInt(parts[1])),
				renderer, entityManager,
				Integer.parseInt(parts[4]), Integer.parseInt(parts[2]));
		pacman.powerUp = Integer.parseInt(parts[3]);
		pacman.kills = Integer.parseInt(parts[5]);
		pacman.points = Integer.parseInt(parts[6]);
		return pacman;
	}

	@Override
	public String getImagePath() {
		// Make sure to initialize the facing direction on game start and keep the
		// last facing direction if we stop moving.
		if(lastFacingDirection == null)
			lastFacingDirection = "RIGHT";
		String facingDirection = lastMovement != null? lastMovement.toString(): lastFacingDirection;
		String path = String.format("res/pictures/entities/P_%s%s.png",
				facingDirection, 
				hasPowerUp()? "_POW": "");
		lastFacingDirection = facingDirection;
		return path;
	}
}
