package de.tu_darmstadt.gdi1.pacman.core;

import java.util.ArrayDeque;
import java.util.Collections;
import java.util.Comparator;
import java.util.Random;
import java.util.ArrayList;

/**
 * A collection of functions for generating (valid) levels.
 * 
 * @author smeister
 */
public class LevelGenerator {
	//private static final int MAX_SQUARE_SIZE = 20;
	//private static final int MIN_SQUARE_SIZE = 9;
	private static Random rng = new Random();
	
	private static int MIN_SPAWNS = 2;
	private static float P_BORDER_WALL = 0.85f;
	
	/**
	 * Returns a random level of fixed width and height by
	 * generating canditates until a valid one is found.
	 * 
	 * @param width		the width
	 * @param height	the height
	 * @return			a valid level
	 */
	public static Level generateRandom(int width, int height) {
		Level level = null;
		try {
			level = generate(width, height);
			level.validate();
		}
		catch(Exception e) {
			return generateRandom(width, height);
		}
		return level;
	}
	

	private static class Field {
		public Field(int x, int y, float weight)  {
			this.x = x;
			this.y = y;
			this.weight = weight;
		}
		// The weight is used to order fields for selection
		public float weight;
		public int x, y;
	}
	
	/**
	 * Generates a random, possibly invalid, level of fixed width and height.
	 * 
	 * @param width		the width
	 * @param height	the height
	 * @return			a level
	 */
	private static Level generate(int width, int height) {
		// The level is initialized with whitespace
		Level level = new Level(width, height);
		// Begin by populating the borders (or leaving them open)
		for(int i = 0; i < height; ++i) {
			if(rng.nextFloat() < P_BORDER_WALL) {
				level.setField(i, 0, 'X');
				level.setField(i, width-1, 'X');
			}
		}
		for(int i = 0; i < width; ++i) {
			if(rng.nextFloat() < P_BORDER_WALL) {
				level.setField(0, i, 'X');
				level.setField(height-1, i, 'X');
			}
		}
		// Create a random "queue" of inner fields
		ArrayList<Field> Q = new ArrayList<>();
		for(int i = 1; i < height-1; ++i)
			for(int j = 1; j < width-1; ++j)
				Q.add(new Field(i, j, rng.nextFloat()));
		
		Collections.sort(Q, new Comparator<Field>() {
			public int compare(Field a, Field b) {
				return a.weight < b.weight? 1: -1;
			  }
		});
		ArrayDeque<Field> deque = new ArrayDeque<>(Q);
		
		// Add essential modules first. Caution:
		// Make sure that the maximum possible number of added modules
		// does never exceed the number of inner grid points.
		int noPlayerSpawns = MIN_SPAWNS + rng.nextInt(3);
		int noGhostSpawns = MIN_SPAWNS + rng.nextInt(3);
		int noPups = 1 + rng.nextInt(3);
		int noSups = rng.nextInt(3);
		int noTeleports = rng.nextInt(2);
		for(int i = 0; i < noPlayerSpawns; ++i) {
			Field f = deque.pop();
			level.setField(f.x, f.y, 'P');
		}
		for(int i = 0; i < noGhostSpawns; ++i) {
			Field f = deque.pop();
			level.setField(f.x, f.y, 'G');
		}
		for(int i = 0; i < noGhostSpawns; ++i) {
			Field f = deque.pop();
			level.setField(f.x, f.y, 'G');
		}
		for(int i = 0; i < noSups; ++i) {
			Field f = deque.pop();
			level.setField(f.x, f.y, 'S');
		}
		for(int i = 0; i < noPups; ++i) {
			Field f = deque.pop();
			level.setField(f.x, f.y, 'U');
		}
		for(int i = 0; i < noTeleports; ++i) {
			Field f = deque.pop();
			level.setField(f.x, f.y, 'T');
		}
		// Convert some of the remaining empty fields into walls
		int noWallPieces = rng.nextInt(deque.size());
		for(int i = 0; i < noWallPieces; ++i) {
			Field f = deque.pop();
			level.setField(f.x, f.y, 'X');
		}
		return level;
	}
	
	/**
	 * Creates a random valid level with dimensions randomly
	 * chosen.
	 * 
	 * @return 	a valid level
	 */
	public static Level generateRandom() {
		return generateRandom(14, 9);
		
		//TODO: The code below somehow causes weird rendering errors.
		// I have no idea why, but if the dimensions are fixed, random
		// generation works.
		//int rngTo = MAX_SQUARE_SIZE - MIN_SQUARE_SIZE + 1;
		//return generateRandom(rng.nextInt(rngTo) + MIN_SQUARE_SIZE, 
		//		rng.nextInt(rngTo) + MIN_SQUARE_SIZE);
	}
}
