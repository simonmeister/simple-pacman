package de.tu_darmstadt.gdi1.pacman.ui;

import org.newdawn.slick.AppGameContainer;
import org.newdawn.slick.SlickException;

/**
 * The entry point for game execution.
 * 
 * @author smeister
 */
public class Main
{
	public static void main(String[] args) throws SlickException
	{
		// standardpfade initialisieren
		setPaths();
		Pacman game = new Pacman();
		AppGameContainer app = new AppGameContainer(game);

		app.setDisplayMode(800, 600, false);
		app.setShowFPS(false);
		app.setResizable(false);
		app.setTargetFrameRate(200);
		app.start();
	}
	
	/**
	 * Standardpfade initialisieren
	 */
	private static void setPaths()
	{
		String libraryPath = null;
		if (System.getProperty("os.name").toLowerCase().contains("windows"))
			libraryPath = System.getProperty("user.dir") + "/lib/natives/windows";
		else if (System.getProperty("os.name").toLowerCase().contains("mac"))
			libraryPath = System.getProperty("user.dir") + "/lib/natives/macosx";
		else if (System.getProperty("os.name").toLowerCase().contains("linux"))
			libraryPath = System.getProperty("user.dir") + "/lib/natives/linux";
		else
			libraryPath = System.getProperty("user.dir") + "/lib/natives/" + System.getProperty("os.name").toLowerCase();
		
		System.setProperty("org.lwjgl.librarypath", libraryPath);
	}
}