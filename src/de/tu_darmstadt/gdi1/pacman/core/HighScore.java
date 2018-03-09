package de.tu_darmstadt.gdi1.pacman.core;

import java.util.ArrayList;
import java.util.Scanner;

/**
 * Manages a simple highscore.
 * Create a new one or load from string by calling
 * fromString().
 * 
 * @author smeister
 */
public class HighScore {
	
	class Score {
		int points;
		String name;
		
		Score(String name, int points) {
			this.name = name;
			this.points = points;
		}
	}
	
	ArrayList<Score> entries;
	private int limit;
	
	public final int DEFAULT_LIMIT = 10;
	
	/**
	 * Initializes.
	 * 
	 * @param limit		the maximum number of entries
	 */
	public HighScore(int limit) {
		entries = new ArrayList<>();
		this.limit = limit;
	}
	
	public HighScore() {
		entries = new ArrayList<>();
		this.limit = DEFAULT_LIMIT;
	}
	
	
	/**
	 * Stringifies the HighScore.
	 * 
	 * @return	a string
	 */
	public String toString() {
		StringBuilder sb = new StringBuilder();
		for(Score score: entries) {
			sb.append(String.format("%s %d\n", score.name, score.points));
		}
		return sb.toString();
	}
	
	/**
	 * Inserts a new entry into the HighScore if it fits.
	 * 
	 * @param name		the player's name
	 * @param points	the player's points
	 * @return			true if successful, false otherwise
	 */
	public boolean addEntry(String name, int points) {
		Score score;
		for(int i = 0, len = Math.min(entries.size(), limit-1); i <= len; ++i) {
			score = i < entries.size()? entries.get(i): null;
			if(score == null || points > score.points) {
				entries.add(i, new Score(name, points));
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Returns a list of names in the HighScore in ascending order.
	 * 
	 * @return	a list of strings
	 */
	public String[] getNames() {
		String[] names = new String[entries.size()];
		for(int i = 0; i < entries.size(); ++i)
			names[i] = entries.get(i).name;
		return names;
	}
	
	/**
	 * Returns a list of points in the HighScore in ascending order.
	 * @return a list of strings
	 */
	public int[] getPoints() {
		int[] points = new int[entries.size()];
		for(int i = 0; i < entries.size(); ++i)
			points[i] = entries.get(i).points;
		return points;
	}
	
	/**
	 * @return the number of entries in the HighScore
	 */
	public int getSize() {
		return entries.size();
	}
	
	/**
	 * Recovers a HighScore that was saved via toString().
	 *  
	 * @return	the reconstructed highscore
	 */
	public static HighScore fromString(String s) {
		HighScore highscore = new HighScore();
		
		Scanner sc = new Scanner(s);
		String[] parts;
		while(sc.hasNextLine()) {
			parts = sc.nextLine().split(" ");
			highscore.addEntry(parts[0], Integer.parseInt(parts[1]));
		}
		sc.close();
		
		return highscore;
	}
}
