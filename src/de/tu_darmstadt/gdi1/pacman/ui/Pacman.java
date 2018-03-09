package de.tu_darmstadt.gdi1.pacman.ui;

import java.util.HashMap;

import org.newdawn.slick.GameContainer;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.state.StateBasedGame;

import eea.engine.entity.StateBasedEntityManager;

public class Pacman extends StateBasedGame
{
	
    public static final int MENU = 0;
    public static final int GAMEPLAY = 1;
    public static final int LEVEL_EDITOR = 2;
    public static final int HIGH_SCORE = 3;
    // This is shared across states
    private HashMap<String, String> globals;
    
	public Pacman() throws SlickException
	{
		super("Yet another Pacman clone");
		globals = new HashMap<>();
	}

	/**
	 * Executed once to initialice the game.
	 */
	@Override
	public void initStatesList(GameContainer gc) throws SlickException
	{	
		// Fuege dem StateBasedGame die States hinzu 
		// (der zuerst hinzugefuegte State wird als erster State gestartet)
		addState(new MainMenuState(globals, MENU));
        addState(new GameplayState(globals, GAMEPLAY));
        addState(new ScoreViewState(globals, HIGH_SCORE));

        // Fuege dem StateBasedEntityManager die States hinzu
        StateBasedEntityManager.getInstance().addState(MENU);
        StateBasedEntityManager.getInstance().addState(GAMEPLAY);
        StateBasedEntityManager.getInstance().addState(HIGH_SCORE);
	}
}