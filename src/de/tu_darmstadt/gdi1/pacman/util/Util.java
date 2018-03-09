package de.tu_darmstadt.gdi1.pacman.util;

/**
 * 
 * @author smeister
 */
public class Util {
	/**
	 * Linearly interpolate between to values.
	 * 
	 * @param a			first value
	 * @param b			second value
	 * @param weight	the weight of b
	 * @return			interpolated value between a and b
	 */
	public static float lerp(float a, float b, float weight) {
		return a*(1.0f-weight) + b*weight;
	}
}
