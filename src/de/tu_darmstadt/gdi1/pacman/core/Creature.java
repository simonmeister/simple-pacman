package de.tu_darmstadt.gdi1.pacman.core;

import java.awt.Point;

import org.newdawn.slick.Image;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.geom.Vector2f;

import de.tu_darmstadt.gdi1.pacman.ui.GridRenderer;
import de.tu_darmstadt.gdi1.pacman.ui.Pacman;
import de.tu_darmstadt.gdi1.pacman.util.Util;
import eea.engine.component.render.ImageRenderComponent;
import eea.engine.entity.Entity;
import eea.engine.entity.StateBasedEntityManager;


/**
 * A basic movable creature.
 * 
 * @author smeister
 */
public abstract class Creature {
	// Null values may occur here (indicating test mode)
	GridRenderer renderer;
	StateBasedEntityManager entityManager;
	
	/**
	 * @return	whether the creature was initialized in a non-displayed game
	 */
	public boolean testing() {
		return renderer == null || entityManager == null;
	}
	
	ImageRenderComponent image;
	
	// The current grid position in a game (NOTE: this is not the display position)
	private Point pos; 
	// The display entity (holding the display position)
	private Entity entity;
	
	protected Movement lastMovement;
	
	/**
	 * Initializes a creature and optionally the entity to display it,
	 * if we have a renderer and entityManager (a displayed game).
	 * 
	 * @param p				the initial (logical) position
	 * @param renderer
	 * @param entityManager
	 */
	public Creature(Point p, GridRenderer renderer, StateBasedEntityManager entityManager) {
		this.renderer = renderer;
		this.entityManager = entityManager;
		pos = p;
		if(testing()) return;
		
		this.entity = new Entity("Creature");
		entity.setPosition(renderer.gameCoordsToPx(pos));
		entity.setSize(new Vector2f(35, 35));
		entityManager.addEntity(Pacman.GAMEPLAY, this.entity);
		entity.setPassable(true);
		
		updateImage();
	}
	
	/**
	 * @return the last (logical) movement executed
	 */
	public Movement getLastMovement() {
		return lastMovement;
	}
	
	public void setLastMovement(Movement mv) {
		lastMovement = mv;
	}
	
	/**
	 * Should return the path of the current image.
	 * 
	 * @return	a valid path
	 */
	public abstract String getImagePath();
	
	/**
	 * Updates the displayed image. (if necessary)
	 */
	public void updateImage() {
		if(testing()) return;	
			
		if(image != null) {
			entity.removeComponent(image);
		}
		entityManager.removeEntity(Pacman.GAMEPLAY, entity);
		try {
			image = new ImageRenderComponent(new Image(getImagePath()));
		} catch (SlickException e) {
			e.printStackTrace();
		}
		entity.addComponent(image);
		entityManager.addEntity(Pacman.GAMEPLAY, entity);
	}
	
	/**
	 * Positions the display entity,
	 * linearly interpolating between the current (next) and previous position.
	 * 
	 * @param currentMs		the current "position" 0 < currentMs <= duration
	 * @param duration		the duration of a single cycle
	 */
	public void updateAnimations(int currentMs, int duration) {
		if(testing()) return;

		// Updates the display image
		updateImage();

		if(lastMovement == null) {
			// Nothing to do...
			entity.setPosition(renderer.gameCoordsToPx(pos));
			return;
		}
		
		Point lastPos = new Point(pos.x-lastMovement.x, pos.y-lastMovement.y);
		// Get us to the pixel space
		Vector2f lastPxPos = renderer.gameCoordsToPx(lastPos);
		Vector2f pxPos = renderer.gameCoordsToPx(pos);
		
		// The "shift" towards pxPos.
		float weight = (float)currentMs/(float)duration;
		
		Vector2f displayPos = new Vector2f(Util.lerp(lastPxPos.x, pxPos.x, weight),
				Util.lerp(lastPxPos.y, pxPos.y, weight));
		entity.setPosition(displayPos);
	}
	
	/**
	 * Call this on a displayed creature when it is no longer used.
	 */
	public void finalize() {
		if(testing()) return;
		
		entityManager.removeEntity(de.tu_darmstadt.gdi1.pacman.ui.Pacman.GAMEPLAY, entity);
		entity = null;
		renderer = null;
		entityManager = null;
	}
	
	// Getter/Setter for logical positions
	public Point getPos() {
		return pos;
	}
	
	public void setPos(Point p) {
		pos = p;
	}
}
