package de.tu_darmstadt.gdi1.pacman.ui;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;

import org.lwjgl.input.Mouse;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Image;
import org.newdawn.slick.Input;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.geom.Vector2f;
import org.newdawn.slick.state.StateBasedGame;

import de.tu_darmstadt.gdi1.pacman.core.HighScore;
import de.tu_darmstadt.gdi1.pacman.util.PacmanGameState;
import eea.engine.action.Action;
import eea.engine.action.basicactions.ChangeStateAction;
import eea.engine.component.Component;
import eea.engine.component.render.ImageRenderComponent;
import eea.engine.entity.Entity;
import eea.engine.event.ANDEvent;
import eea.engine.event.basicevents.KeyPressedEvent;
import eea.engine.event.basicevents.MouseClickedEvent;
import eea.engine.event.basicevents.MouseEnteredEvent;

/**
 * The state for display of the current highscore.
 * 
 * @author Nicolas Acero
 */
public class ScoreViewState extends PacmanGameState {
	
	private final int SPACING = 15;
    private final int BTN_HI = 100;
    final ImageRenderComponent img1;
	final ImageRenderComponent img2;
    private HighScore scores;

	public ScoreViewState(HashMap<String, String> globals, int sid) throws SlickException {
		super(globals, sid);
		//Button images
		img1 = new ImageRenderComponent(new Image("res/pictures/ui/buttons/back_0.png"));
		img2 = new ImageRenderComponent(new Image("res/pictures/ui/buttons/back_1.png"));
	}

	@Override
	public void init(GameContainer arg0, StateBasedGame arg1)
			throws SlickException {
		
        //ESC Key Event
      	Entity esc_Listener = new Entity("ESC_Listener");
      	KeyPressedEvent esc_pressed = new KeyPressedEvent(Input.KEY_ESCAPE);
      	esc_pressed.addAction(new ChangeStateAction(Pacman.MENU));
      	esc_Listener.addComponent(esc_pressed);
      	addEntity(esc_Listener);
      	
      	// Init background
    	Entity background = new Entity("menu");
    	background.setPosition(new Vector2f(400,300));
    	background.setScale(1f);
    	background.addComponent(new ImageRenderComponent(new Image("res/pictures/ui/highscore.png")));
    	
      	// Init Back-Button
    	final Entity goBack = new Entity("goBack");
    	goBack.setPosition(new Vector2f(60,50));
    	goBack.setScale(0.3f);
    	goBack.addComponent(img1);
    	MouseEnteredEvent in = new MouseEnteredEvent();
    	in.addAction(new Action() {
			
			@Override
			public void update(GameContainer gc, StateBasedGame sb, int delta,
					Component event) {
				goBack.removeComponent(img1);
				goBack.addComponent(img2);
				
			}
		});
    	
    	MouseClickedEvent click = new MouseClickedEvent();
    	ANDEvent buttonClick = new ANDEvent(in, click);
    	buttonClick.addAction(new ChangeStateAction(Pacman.MENU));
    	//Add components to the button
    	goBack.addComponent(in);
    	goBack.addComponent(buttonClick);
    	
    	//Add entities
    	addEntity(esc_Listener);
    	addEntity(background);
    	addEntity(goBack);
		
	}
	
	@Override
	public void enter(GameContainer container, StateBasedGame game) 
			throws SlickException {
		try {
			byte[] data = Files.readAllBytes(Paths
					.get(gameDir + "highScores.txt")); //Load a highscore from txt file
			String content = new String(data);
			scores = HighScore.fromString(content); //Recovers the HighScore that was saved in the txt file
		} catch (Exception e) {
			scores = new HighScore();	
		}
		loadOptions();
	}
	
	@Override
	public void update(GameContainer container, StateBasedGame game, int delta)
			throws SlickException {
		Entity goBack = entityManager.getEntity(stateID,"goBack");
		if (Mouse.getX() > goBack.getPosition().x || Mouse.getY() > goBack.getPosition().y) {
			goBack.removeComponent(img2);
			goBack.addComponent(img1);
		}
		entityManager.updateEntities(container, game, delta);
	}
    
	@Override
	public void render(GameContainer container, StateBasedGame game, Graphics g)
			throws SlickException {
		entityManager.renderEntities(container, game, g);
		
		g.drawString(translator.translateMessage("name"), 210, BTN_HI);
		g.drawString(translator.translateMessage("score"), 500, BTN_HI);
		for (int i = 0; i < scores.getSize(); i++) {
		g.drawString(i+1+". "+scores.getNames()[i], 210, BTN_HI+SPACING*(i+1));
		}
		
		for (int i = 0; i < scores.getSize(); i++) {
			Integer points = scores.getPoints()[i];
			g.drawString(points.toString(), 500, BTN_HI+SPACING*(i+1));
		}
	}

}
