package de.tu_darmstadt.gdi1.pacman.util;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Locale;

import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.state.BasicGameState;
import org.newdawn.slick.state.StateBasedGame;

import translator.Translator;
import eea.engine.entity.Entity;
import eea.engine.entity.StateBasedEntityManager;

/**
 * Extends the BasicGameState to reduce boilerplate code.
 * 
 * @author smeister
 */
public abstract class PacmanGameState extends BasicGameState {
	protected int stateID;
	protected Locale language;
	protected boolean sound;
	protected StateBasedEntityManager entityManager;
	protected Translator translator;
	protected HashMap<String, String> globals;
	protected final String gameDir;
	
	public PacmanGameState(HashMap<String, String> globals, int sid) {
		stateID = sid;
		this.globals = globals;
		language = new Locale ("de","DE"); //Default language
		sound = true; //Sound enabled by default
		translator = new Translator("res/msg/msg", language);
		gameDir = System.getProperty("user.home") + System.getProperty("file.separator") + "_pacman_" + System.getProperty("file.separator");
		entityManager = StateBasedEntityManager.getInstance();
	}
	
	@Override
	public int getID() {
		return stateID;
	}
	
	@Override
	public void update(GameContainer container, StateBasedGame game, int delta)
			throws SlickException {
		entityManager.updateEntities(container, game, delta);
	}
	
	@Override
    public void enter(GameContainer container, StateBasedGame game) throws SlickException {
    	 loadOptions();
    }
    
	@Override
	public void render(GameContainer container, StateBasedGame game, Graphics g)
			throws SlickException {
		entityManager.renderEntities(container, game, g);
	}
	
	public void addEntity(Entity e) {
		entityManager.addEntity(stateID, e);
	}
	
	public void removeEntity(Entity e) {
		entityManager.removeEntity(stateID, e);
	}
	
	/**
	 * Loads the user's preferences from a file
	 */
	protected void loadOptions() {
		try {
			byte[] data = Files.readAllBytes(Paths
					.get(gameDir + "settings.txt")); //Loads the settings from txt file
			String content = new String(data);
			String parts[] = content.split("\n\n");
			String lang[] = parts[0].split("_");
			language = new Locale (lang[0], lang[1]);
			sound = parts[1].equals("true") ? true : false;
		} catch (Exception e) {}
    	translator.setTranslatorLocale(language);
	}
	
	/**
	 * Saves the user's preferences in a file
	 * @param options String with the user's preferences
	 */
	protected void saveOptions() {
		StringBuilder sb = new StringBuilder();
		sb.append(language.toString());
		sb.append("\n\n");
		sb.append(sound);
		try {
			File file = new File(gameDir);
			file.mkdir();
			FileWriter fw = new FileWriter(gameDir + "settings.txt");
			fw.write(sb.toString());
			fw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
