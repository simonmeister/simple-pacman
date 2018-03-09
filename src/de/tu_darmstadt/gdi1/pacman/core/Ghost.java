package de.tu_darmstadt.gdi1.pacman.core;

import java.awt.Point;
import java.util.Random;

import de.tu_darmstadt.gdi1.pacman.ui.GridRenderer;
import eea.engine.entity.StateBasedEntityManager;

/**
 * A creepy creepy ghost.
 * 
 * @author smeister
 */
public class Ghost extends Creature {
	private static Random rng = new Random();
	private int color = rng.nextInt(4);

	public Ghost(Point p, GridRenderer renderer, StateBasedEntityManager entityManager) {
		super(p, renderer, entityManager);
	}
	
	public int getColor() {
		return color;
	}

	/**
	 * Call this during the game update, passing in the actual movement
	 * computed for the ghost.
	 * 
	 * @param mv	the movement
	 */
	public void update(Movement mv) {
		this.lastMovement = mv;
	}
	
	public String toString() {
		return getPos().x + " " + getPos().y + " " + color;
	}
	
	public static Ghost fromString(String s, GridRenderer renderer, StateBasedEntityManager entityManager) {
		String[] parts = s.split(" ");
		Ghost ghost = new Ghost(new Point(Integer.parseInt(parts[0]), Integer.parseInt(parts[1])),
				renderer, entityManager);
		ghost.color = Integer.parseInt(parts[2]);
		return ghost;
	}

	@Override
	public String getImagePath() {
		return String.format("res/pictures/entities/G%d.png", color);
	}
}