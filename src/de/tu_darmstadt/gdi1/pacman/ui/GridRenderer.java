package de.tu_darmstadt.gdi1.pacman.ui;

import java.awt.Point;
import java.util.HashMap;

import eea.engine.component.render.ImageRenderComponent;
import eea.engine.entity.Entity;
import eea.engine.entity.StateBasedEntityManager;

import org.newdawn.slick.Image;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.geom.Vector2f;

import de.tu_darmstadt.gdi1.pacman.core.Game;
import de.tu_darmstadt.gdi1.pacman.core.Level;
import de.tu_darmstadt.gdi1.pacman.core.MapModule;

/**
 * Manages the entities related to the display of a game grid
 * after being initialized to a certain game. May be re-used
 * by multiple games by calling prepare(game) with the current game.
 * When the game state changes, call updateBitmaps() in order to reflect
 * the changes in the display.
 * 
 * 
 * @author smeister
 */
public class GridRenderer {
	private Game game;
	private Cell[][] cells;
	private StateBasedEntityManager entityManager;
	private HashMap<String, Image> cache;
	private int screenWidth, screenHeight;
	private int offsetWidth, offsetHeight;
	
	// Offset the grid to make place for displaying lives/points
	private final int OFFSET_TOP = 100;
	
	public final int BITMAP_SIZE = 35;
	
	public Vector2f gameCoordsToPx(Point pos) {
		return new Vector2f(offsetWidth+pos.y*BITMAP_SIZE, offsetHeight+OFFSET_TOP+pos.x*BITMAP_SIZE);
	}
	
	/**
	 * Represents a single grid cell.
	 */
	private class Cell {
		public Cell(int x, int y) {
			this.entity = new Entity("P" + x + "," + y);
			entity.setPosition(gameCoordsToPx(new Point(x, y)));
			entity.setSize(new Vector2f(BITMAP_SIZE, BITMAP_SIZE));
			entityManager.addEntity(Pacman.GAMEPLAY, this.entity);
			entity.setPassable(true);
		}
		
		/**
		 * Clears the cells display.
		 */
		public void clear() {
			if(image != null)
				entity.removeComponent(image);
			image = null;
		}
		
		/**
		 * Sets the image to be displayed at this cell.
		 * 
		 * @param image		the image
		 */
		public void set(ImageRenderComponent image) {
			this.image = image;
			entityManager.removeEntity(Pacman.GAMEPLAY, this.entity);
			entity.addComponent(image);
			entityManager.addEntity(Pacman.GAMEPLAY, this.entity);
		}
		
		private ImageRenderComponent image;
		private final Entity entity;
	}
	
	public GridRenderer(StateBasedEntityManager em, int screenWidth, int screenHeight) {
		entityManager = em;
		cache = new HashMap<>();
		this.screenHeight = screenHeight - OFFSET_TOP;
		this.screenWidth = screenWidth;
	}
	
	/**
	 * Prepares the grid renderer for rendering "game" by initializing
	 * a grid of adequate dimensions.
	 * 
	 * @param game	the game that will be rendered by future updateBitmaps() calls
	 * @throws SlickException
	 */
	public void prepare(Game game) throws SlickException {
		clearLevel();
		this.game = game;
		
		Level level = game.getLevel();
		// Try to center the grid as good as possible (compute offset)
		int levelWidth = level.getWidth() * BITMAP_SIZE;
		int levelHeight = level.getHeight() * BITMAP_SIZE;
		
		offsetWidth = Math.max((int)((screenWidth - levelWidth)/2.0f), 0);
		offsetHeight = Math.max((int)((screenHeight - levelHeight)/2.0f), 0);
		
		cells = new Cell[level.getHeight()][level.getWidth()];
		
		for(int i = 0; i < level.getHeight(); ++i) {
			for(int j = 0; j < level.getWidth(); ++j) {
				cells[i][j] = new Cell(i, j);
			}
		}
		updateBitmaps();
	}
	
	/**
	 * Reset.
	 */
	private void clearLevel() {
		// Remove entities from em
		if(cells != null) {
			for(int i = 0; i < cells.length; ++i) {
				for(int j = 0; j < cells[0].length; ++j) {
					entityManager.removeEntity(Pacman.GAMEPLAY, cells[i][j].entity);
				}
			}
		}
		game = null;
		cells = null;
	}
	
	/**
	 * Returns an image loaded from "path".
	 * 
	 * @param path
	 * @return
	 * @throws SlickException
	 */
	public Image loadImage(String path) throws SlickException {
		Image i = cache.get(path);
		if(i == null) {
			i = new Image(path);
			cache.put(path, i);
		}
		return i;
	}
	
	/**
	 * Updates the grid of entities according to the current game state.
	 * 
	 * @throws SlickException
	 */
	public void updateBitmaps() throws SlickException {
		if(game != null) {		
			Level level = game.getLevel();
			// Begin by drawing the level components
			for(int i = 0; i < level.getHeight(); ++i) {
				for(int j = 0; j < level.getWidth(); ++j) { 
					Cell cell = cells[i][j];
					cell.clear();
					cell.set(new ImageRenderComponent(loadImage(getBitmapPath(i, j))));
				}
			}
		}
	}

	/**
	 * Returns the path for the image to be rendered into (x, y)
	 * @param x
	 * @param y
	 * @return	a relative file system path
	 */
	private String getBitmapPath(int x, int y) {
		String path;
		Level level = game.getLevel();
		int width = level.getWidth();
		int height = level.getHeight();
		
		if(game.hasItemAt(x, y))
			path = "entities/dot";
		else {
			switch (game.getLevel().get(x, y)) 
			{
			case WALL:
				boolean above, below, right, left;
				
				above = x != 0 			&& level.get(x-1, y)   == MapModule.WALL;
				below = x != height-1 	&& level.get(x+1, y)   == MapModule.WALL;
				left  = y != 0 			&& level.get(x  , y-1) == MapModule.WALL;
				right = y != width-1 	&& level.get(x  , y+1) == MapModule.WALL;
				
				// The bits indicate a neighbouring wall in the following order:
				// left right above below
				path = String.format("map/%d%d%d%d", left? 1:0, right? 1:0, above? 1:0, below? 1:0);
				break;
			case SPEED_UP:
				path = "entities/speedup";
				break;
			case POWER_UP:
				path = "entities/powerup";
				break;
			case TELEPORT:
				path = "entities/teleporter";
				break;
			default:
				path = "map/B";
			}
		}
		return "res/pictures/" + path + ".png";
	}
}
