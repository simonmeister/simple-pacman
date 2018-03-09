package de.tu_darmstadt.gdi1.pacman.ui;

import javax.swing.ImageIcon;
import javax.swing.JOptionPane;
import javax.swing.UIManager;

import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Image;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.geom.Vector2f;
import org.newdawn.slick.state.StateBasedGame;

import java.awt.Color;
import java.util.HashMap;
import java.util.Locale;

import de.tu_darmstadt.gdi1.pacman.util.PacmanGameState;
import eea.engine.action.Action;
import eea.engine.action.basicactions.ChangeStateInitAction;
import eea.engine.action.basicactions.QuitAction;
import eea.engine.component.Component;
import eea.engine.component.render.ImageRenderComponent;
import eea.engine.entity.Entity;
import eea.engine.event.ANDEvent;
import eea.engine.event.basicevents.MouseClickedEvent;
import eea.engine.event.basicevents.MouseEnteredEvent;

/**
 * The state for managing the game menu.
 * 
 * @author Simon Meister
 */

public class MainMenuState extends PacmanGameState {
	private final int SPACING = 35;
    private final int BTN_HI = 170;
    private ImageIcon aboutImg;
    
    public MainMenuState(HashMap<String, String> globals, int sid) {
    	super(globals, sid);
    }
    
    @Override
	public void init(GameContainer container, StateBasedGame game) throws SlickException {
    	
    	// Init background
    	Entity background = new Entity("menu");
    	background.setPosition(new Vector2f(400,300));
    	background.setScale(0.5f);
    	background.addComponent(new ImageRenderComponent(new Image("res/pictures/ui/menuBackground.jpg")));

    	// Create a button to change the state to GAMEPLAY
    	Entity onNewGame = new Entity("start");
    	onNewGame.setPosition(new Vector2f(400, 180));
    	onNewGame.setScale(0.30f);
    	ANDEvent evt;
    	evt = new ANDEvent(new MouseEnteredEvent(), new MouseClickedEvent());
    	evt.addAction(new ChangeStateInitAction(Pacman.GAMEPLAY));
    	onNewGame.addComponent(evt);
    	onNewGame.setSize(new Vector2f(198, 20));
    	
    	// Create a button to load a previous game
    	Entity onLoadGame = new Entity("load");
    	onLoadGame.setPosition(new Vector2f(400, 215));
    	onLoadGame.setScale(0.30f);
    	evt = new ANDEvent(new MouseEnteredEvent(), new MouseClickedEvent());
    	evt.addAction(new Action() {
			@Override
			public void update(GameContainer gc, StateBasedGame sb, int delta,
					Component event) {
				globals.put("load_game", "true");
				sb.enterState(Pacman.GAMEPLAY);
			}
		});
    	onLoadGame.addComponent(evt);
    	onLoadGame.setSize(new Vector2f(90, 20));
    	
    	// Create a button to change the state to HighScore
    	Entity onHighScore = new Entity("highscore");
    	onHighScore.setPosition(new Vector2f(400, 250));
    	onHighScore.setScale(0.30f);
    	evt = new ANDEvent(new MouseEnteredEvent(), new MouseClickedEvent());
    	evt.addAction(new ChangeStateInitAction(Pacman.HIGH_SCORE));
    	onHighScore.addComponent(evt);
    	onHighScore.setSize(new Vector2f(80, 20));
    	
    	// Create a quit button
    	Entity onQuit = new Entity("quit");
    	onQuit.setPosition(new Vector2f(400, 285));
    	onQuit.setScale(0.28f);
    	
    	evt = new ANDEvent(new MouseEnteredEvent(), new MouseClickedEvent());
    	evt.addAction(new QuitAction());
    	onQuit.addComponent(evt);
    	onQuit.setSize(new Vector2f(80, 15));
    	
    	// Create a button to change the state to OPTIONS
    	Entity onOptions = new Entity("options");
    	onOptions.setPosition(new Vector2f(670, 330));
    	onOptions.setScale(0.40f);
    	evt = new ANDEvent(new MouseEnteredEvent(), new MouseClickedEvent());
    	evt.addAction(new Action() {
			
			@Override
			public void update(GameContainer gc, StateBasedGame sb, int delta,
					Component event) {
				showOptions();
				
			}
		});
    	onOptions.addComponent(evt);
    	onOptions.setSize(new Vector2f(35, 35));
    	onOptions.addComponent(new ImageRenderComponent(new Image("res/pictures/ui/buttons/options.png")));
    	
    	// Create a button to change the state to open the about screen
    	Entity onAbout = new Entity("about");
    	onAbout.setPosition(new Vector2f(730, 330));
    	onAbout.setScale(0.45f);
    	evt = new ANDEvent(new MouseEnteredEvent(), new MouseClickedEvent());
    	evt.addAction(new Action() {

			@Override
			public void update(GameContainer gc, StateBasedGame sb, int delta,
					Component event) {
				showAbout();
			}
    		
    	});
    	onAbout.addComponent(evt);
    	onAbout.setSize(new Vector2f(35, 35));
    	onAbout.addComponent(new ImageRenderComponent(new Image("res/pictures/ui/buttons/about.png")));
    	
    	addEntity(background);
    	addEntity(onQuit);
    	addEntity(onNewGame);
    	addEntity(onHighScore);
    	addEntity(onOptions);
    	addEntity(onAbout);
    	addEntity(onLoadGame);
    	
    	//Loads the about-screen image
    	aboutImg = new ImageIcon("res/pictures/ui/pacman.jpg");
    }
    
    @Override
	public void update(GameContainer container, StateBasedGame game, int delta)
			throws SlickException {
		entityManager.updateEntities(container, game, delta);
	}
    
	@Override
	public void render(GameContainer container, StateBasedGame game, Graphics g)
			throws SlickException {
		entityManager.renderEntities(container, game, g);
	
		g.drawString(translator.translateMessage("newGame"), 320, BTN_HI);
		g.drawString(translator.translateMessage("loadGame"), 360, BTN_HI+SPACING);
		g.drawString(translator.translateMessage("highscore"), 360, BTN_HI+2*SPACING);
		g.drawString(translator.translateMessage("exit"), 370, BTN_HI+3*SPACING);
	}
	
	/**
	 * Shows a dialog to change the language preferences
	 */
	private void showOptions() {
		Object[] possibilities = {"Deutsch", "English", "Español", "Italiano"};
		String s = (String)JOptionPane.showInputDialog(null,
				translator.translateMessage("language"), translator.translateMessage("options"),
				JOptionPane.PLAIN_MESSAGE, null, possibilities, "English");

		//If a language was selected, save the option and set the new language.
		if ((s != null) && (s.length() > 0)) {
		    Locale lang;
		    switch(s) {
		    case "English":
		    	lang = new Locale("en", "US");
		    	break;
		    case "Español":
		    	lang = new Locale("es", "CO");
		    break;
		    case "Italiano":
		    	lang = new Locale("it", "IT");
		    	break;
		    default:
		    	lang = new Locale ("de", "DE"); //Default language
		    	break;
		    }
		    language = lang;
		    saveOptions(); //Saves the new configuration
		    translator.setTranslatorLocale(lang); //Applies the new configuration
		}
	}
	
	/**
	 * Shows the about-screen
	 */
	private void showAbout() {
		UIManager.put("OptionPane.background", Color.black);
		UIManager.put("Panel.background", Color.black);
		JOptionPane.showMessageDialog(null, "", translator.translateMessage("about"), JOptionPane.INFORMATION_MESSAGE, aboutImg);
	}
	
}
