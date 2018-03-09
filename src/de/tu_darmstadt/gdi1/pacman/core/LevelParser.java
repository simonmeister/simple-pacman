package de.tu_darmstadt.gdi1.pacman.core;

import java.util.Scanner;

import de.tu_darmstadt.gdi1.pacman.exceptions.InvalidLevelCharacterException;
import de.tu_darmstadt.gdi1.pacman.exceptions.InvalidLevelFormatException;

/**
 * Loads a level from a string. Note that this does not validate
 * the semantic correctness of the level but only the syntax-level correctness
 * (ensures consistent dimensions and valid characters).
 * 
 * @author smeister
 */
public class LevelParser {
	public static Level fromString(String s) throws InvalidLevelCharacterException, InvalidLevelFormatException {
		// Check for invalid characters
		String[] valid = {"P", "G", "X", "S", "T", "U", " ", "B"};
		String invalid = s;
		for(String v: valid)
			invalid = invalid.replace(v, "");
		invalid = invalid.replace("\r", "");
		invalid = invalid.replace("\n", "");
		if(invalid.length() > 0)
			throw new InvalidLevelCharacterException(invalid.charAt(0));
		// Determine and validate the dimensions of the level
		Scanner sc = new Scanner(s);
		int width = 0, height = 0, current;
		while(sc.hasNextLine()) {
			current = sc.nextLine().length();
			if(width != 0 && current != width) {
				sc.close();
				throw new InvalidLevelFormatException();
			}
			width = current;
			++height;
		}
		// Parse the level
		sc.close();
		sc = new Scanner(s);
		Level level = new Level(width, height);
		for(int i = 0; sc.hasNextLine(); ++i) {
			String line = sc.nextLine();
			for(int j = 0; j < line.length(); ++j)
				level.setField(i, j, line.charAt(j));
		}
		sc.close();
		return level;
	}
}
