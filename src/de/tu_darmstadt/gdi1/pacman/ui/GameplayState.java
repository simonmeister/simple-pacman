package de.tu_darmstadt.gdi1.pacman.ui;

import org.newdawn.slick.Color;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Image;
import org.newdawn.slick.Input;
import org.newdawn.slick.Music;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.geom.Vector2f;
import org.newdawn.slick.state.StateBasedGame;

import de.tu_darmstadt.gdi1.pacman.core.Game;
import de.tu_darmstadt.gdi1.pacman.core.HighScore;
import de.tu_darmstadt.gdi1.pacman.core.Level;
import de.tu_darmstadt.gdi1.pacman.core.LevelGenerator;
import de.tu_darmstadt.gdi1.pacman.core.LevelParser;
import de.tu_darmstadt.gdi1.pacman.core.Movement;
import de.tu_darmstadt.gdi1.pacman.util.PacmanGameState;
import eea.engine.component.render.ImageRenderComponent;
import eea.engine.entity.Entity;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Random;

import javax.swing.JOptionPane;
import javax.swing.UIManager;

/**
 * The state for playing the game.
 * 
 * @author Simon Meister
 */
public class GameplayState extends PacmanGameState {
	private boolean paused;
	private boolean askName;
	private Game game;
	private Movement lastKey;
	private GridRenderer drawer;
	private int lastKeyPressed;
	private final int TICK_DURATION_MS = 250;
	protected HighScore highscore;
	private Image life;
	private Image pausedImg;
	private Image resumeImg;
	private Image saveImg;
	private Image restartImg;
	private Image exitImg;
	private Image pauseButton;
	private Image soundOn;
	private Image soundOff;
	private Music introSound;
	StateBasedGame stateBasedGame;
	private int sinceLastPacmanUpdate = TICK_DURATION_MS;
	private int sinceLastGhostUpdate = TICK_DURATION_MS;
	private static Random rng = new Random();
	private final float RANDOM_LEVEL_PROBABILITY = 0.39f;
	
    GameplayState(HashMap<String, String> globals, int sid) {
       super(globals, sid);
    }  
    
    @Override
	public void init(GameContainer container, StateBasedGame g) throws SlickException {
    	askName = false;
    	this.stateBasedGame = g;
    	
    	drawer = new GridRenderer(entityManager, g.getContainer().getWidth(), g.getContainer().getHeight());
              
        //Load images 
        pausedImg = new Image("res/pictures/ui/paused.png");
        pauseButton = new Image("res/pictures/ui/buttons/pause.png");
        soundOn = new Image ("res/pictures/ui/buttons/sound-on.png");
        soundOff = new Image ("res/pictures/ui/buttons/sound-off.png");
        life = new Image ("res/pictures/ui/life.png");
        
        //Load intro-Sound
        introSound = new Music("res/sounds/pacman_beginning.wav");
    }
    
    @Override
    public void enter(GameContainer container, StateBasedGame sbg) throws SlickException {
    	/* Prepare resources */
    	lastKeyPressed = 0;
    	lastKey = null;
    	paused = false;
    	
    	loadScores();
		// Loads options and the images of the pause menu
    	loadOptions(); 
    	loadLocalImg();
   	 	stateBasedGame.getContainer().setMusicOn(sound); //Enables or disable the sound output
   	 												  //According to sound preferences
    	 
    	/* Start the game */
    	// Load one from string
    	String loadIt = globals.remove("load_game");
    	boolean loaded = false;
    	if(loadIt != null) {
    		File folder = new File(gameDir);
    		folder.mkdirs();	 //Creates the saveGame directory if it didn't exist
    		
    		File[] files = folder.listFiles(); //Recovers all the files in the directory
    		LinkedList<File> games = new LinkedList<>();
    		//Filters the recovered files by name and put only the saved games in a list
    		for (int i = 0; i < files.length; i++) {
    			if (files[i].getName().contains("game")) {
    				games.add(files[i]);
    			}
    		}
    		
    		// Get the file with maximum "Last modified"
    		if(games.size() > 0) {
	    		long max = games.get(0).lastModified();
	    		int idx = 0;
	    		for(int i = 1; i < games.size(); ++i) {
	    			long modified = games.get(i).lastModified();
	    			if(modified > max && games.get(i).getName().contains("game")) {
	    				max = modified;
	    				idx = i;
	    			}
	    		}
	    		String str;
				try {
					str = new String(Files.readAllBytes(games.get(idx).toPath()));
					clearGame();
					game = Game.fromString(str, drawer, entityManager);
					if(game.isLost()) {
						loaded = false;
					} else {
						drawer.prepare(game);
			    		loaded = true;
					}
				} catch (IOException e) {
					// This can not happen (the file exists)
					e.printStackTrace();
				}		
    		}
    	}
    	if(!loaded) {
	    	// Or start a fresh game
    		clearGame();
	    	game = new Game(entityManager, drawer);
	    	restart(false);
    	}
    	introSound.play(); //Loads the intro sound
    	
    	//Resets the colors of the dialog windows if they were changed
    	//before by the about window in the main menu
    	UIManager.put("OptionPane.background", Color.lightGray);
		UIManager.put("Panel.background", Color.lightGray);
    }
    
    public void clearGame() {
    	if(game != null)
 			game.finalize();
    }
    
    /**
     * Restart/reset the game.
     * 
     * @param reset				set this to true to reset the game to a clean state
     * 							(0 points, full lives). Otherwise we just continue and load
     * 							a new level.
     * @throws SlickException
     */
    public void restart(boolean reset) throws SlickException {	
    	try {
    		// Load a level from the res/levels folder or create one randomly
    		Level level;
    		File file = new File("res/levels");
			File[] levels = file.listFiles();
    		if(levels.length > 0 && rng.nextFloat() > RANDOM_LEVEL_PROBABILITY) {
    			File levelFile = levels[rng.nextInt(levels.length)];
    			level = LevelParser.fromString(new String(Files.readAllBytes(levelFile.toPath())));
    		}
    		else {
    			level = LevelGenerator.generateRandom();
    		}
    		
 	    	if(reset) {
 	    		clearGame();
 	    		game = new Game(entityManager, drawer);
 	    		introSound.play();
 	    	}
 	    	game.changeLevel(level);
 	    }
 	    catch(Exception e) {}
 	   	drawer.prepare(game);
    }
    
	public void keyPressed(int key, char c) {
		switch(key)
        {
        case Input.KEY_UP:
        	lastKey = Movement.UP;
        	break;
        case Input.KEY_DOWN:
        	lastKey = Movement.DOWN;
        	break;
        case Input.KEY_LEFT:
        	lastKey = Movement.LEFT;
        	break;
        case Input.KEY_RIGHT:
        	lastKey = Movement.RIGHT;
        	break;
        case Input.KEY_SPACE:
        	paused = !paused;
        	break;
        case Input.KEY_ESCAPE:
        	paused = !paused;
        	break;
        case Input.KEY_R: //TODO: this is only for testing
        	try{restart(true);}catch(Exception e){}
        }
		lastKeyPressed = key;
	}

	public void keyReleased(int key, char c) {
		// If the user presses down a new button before releasing 
		// the last one (which is quite common) we have to ignore
		// the keyReleased event of the last button when it is eventually released.
        if(key == lastKeyPressed)
        	lastKey = null;
    }
	
    public void mousePressed(int button, int x, int y)
    {
        if(paused)
        {
        	final int SPACING = 10;
		    final int BTN_HI = 50;
		    final int OFFSET_X1 = 300;
		    final int OFFSET_X2 = 510;
		    final int OFFSET_Y = 200;
		    
		    //Resume
            if ((x > OFFSET_X1 && x < OFFSET_X2) && (y > OFFSET_Y && y < OFFSET_Y+BTN_HI)) {
                paused = !paused; //this will take me to the game.
            }
            //Save
           else if ((x > OFFSET_X1 && x < OFFSET_X2) && (y > OFFSET_Y+BTN_HI+SPACING*1 && y < OFFSET_Y+BTN_HI*2+SPACING*1)) {
        	   	saveGame(game.toString());
           }
            //Restart
            else if ((x > OFFSET_X1 && x < OFFSET_X2) && (y > OFFSET_Y+BTN_HI*2+SPACING*2 && y < OFFSET_Y+BTN_HI*3+SPACING*2)) {
				try {
					paused = !paused;
					restart(true);
				} catch (SlickException e) {
					e.printStackTrace();
				}
            }
            //Exit to menu
            else if ((x > OFFSET_X1 && x < OFFSET_X2) && (y > OFFSET_Y+BTN_HI*3+SPACING*3 && y < OFFSET_Y+BTN_HI*4+SPACING*3)) {
					clearState();
            		stateBasedGame.getContainer().getGraphics().setColor(new Color(255f, 255f, 255f));
            		stateBasedGame.getContainer().setMusicOn(false); //Silence all the sounds
            		stateBasedGame.enterState(Pacman.MENU);
            }
                
        }
        
        else {
        	 if ((x > 10 && x < 40) && (y > 8 && y < 38)) {
                 paused = !paused; //pause the game
             }
        	 else if (((x > 45 && x < 75) && (y > 8 && y < 38))) {
        		 sound = !sound; //Disables sound
        		 stateBasedGame.getContainer().setMusicOn(sound);
        		 saveOptions(); //Saves the new configuration
        	 }
        }
    }
	
	
	/**
	 * Loads the previous high scores from file (if exist)
	 */
	private void loadScores() {
		String content;
		try {
			byte[] data = Files.readAllBytes(Paths
					.get(gameDir + "highScores.txt"));
			content = new String(data);
			highscore = HighScore.fromString(content); //Recovers the HighScore that was saved in the txt file
		} catch (Exception e) {
			highscore = new HighScore();
		}
	}
	
	/**
	 * Writes the current high score to a file.
	 */
	private void writeScores(String scores) {
		try {
			File file = new File(gameDir);
			file.mkdir();
			FileWriter fw = new FileWriter(gameDir + "highScores.txt");
			fw.write(scores);
			fw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		stateBasedGame.enterState(Pacman.MENU);
	}
	
	/**
	 * Saves the current game to a file.
	 */
	private void saveGame(String game) {
		try {
			File folder = new File(gameDir);
    		folder.mkdirs();
			String timeStamp = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss").format(Calendar.getInstance().getTime());
			//Saves the game file in the user's directory
			FileWriter fw = new FileWriter(gameDir +"game" + timeStamp + ".txt");
			fw.write(game);
			fw.close();
			
			JOptionPane.showMessageDialog(null, translator.translateMessage("saveOK"));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Loads the images of the pause menu, according to
	 * the language preference.
	 * 
	 * @throws SlickException 
	 */
	private void loadLocalImg() throws SlickException {
		resumeImg = new Image (translator.translateMessage("resumeImg"));
        saveImg = new Image (translator.translateMessage("saveImg"));
        restartImg = new Image (translator.translateMessage("restartImg"));
        exitImg = new Image (translator.translateMessage("exitImg"));
	}
	
	/**
	 * Removes all entities from the state.
	 */
	public void clearState() {
		entityManager.clearEntitiesFromState(stateID);
	}

    @Override
	public void update(GameContainer container, StateBasedGame g, int delta) throws SlickException {
    	//Ask the player's name, when the game is over
    	if (askName) {
    		askName = false;
    		String name = JOptionPane.showInputDialog(translator.translateMessage("gameOver"));
    		if(name == null) {
    			//The dialog was cancelled
    			stateBasedGame.enterState(Pacman.MENU);
    		} else {
	    		//Adds a new entry to the high scores
	    		highscore.addEntry(name.trim(), game.getPoints());
	    		//Saves the new high score list in a file text
	    		writeScores(highscore.toString());
    		}
    		clearState();
    		return;
    	}
    	
    	if(game.isLost() && !askName) {
    		paused = true;
    		clearState();
    		
    		// Init game over background
        	Entity background = new Entity("menu");
        	background.setPosition(new Vector2f(400,300));
        	background.setScale(1f);
        	background.addComponent(new ImageRenderComponent(new Image("res/pictures/ui/gameOver.png")));
        	addEntity(background);
        	askName = true;
    	}
    	else if(game.isWon()) {
    		restart(false);
    	}
    	
    	if(!paused) {
    		updateGame(delta);
    	}
	}
    
    /**
     * Performs a single step of game logic.
     * 
     * @param delta		ms since the last update
     */
    public void updateGame(int delta) throws SlickException {
    	int pacmanTickDuration = game.getPacman().hasSpeedUp()? (int)(TICK_DURATION_MS/2.0f) : TICK_DURATION_MS;
		// Update ghosts (all TICK_DURATION_MS miliseconds)
		if(sinceLastGhostUpdate > TICK_DURATION_MS) {
			game.updateGhosts();
			sinceLastGhostUpdate = 0;
		}
		else
			sinceLastGhostUpdate += delta;
		
		// Update pacman (frequency depending on hasSpeedUp())
		if(sinceLastPacmanUpdate > pacmanTickDuration) {
			game.movePacman(lastKey);
			game.updatePacman();
			sinceLastPacmanUpdate = 0;
		}
		else
			sinceLastPacmanUpdate += delta;
		
		game.updateCollisions(); // (always)
		// The ghost update is the "regular" update, so we use it as a reference
		// for the consumption of power ups/speed ups (all TICK_DURATION_MS miliseconds)
		if(sinceLastGhostUpdate > TICK_DURATION_MS) {
			game.finishUpdate();
		}
		
		drawer.updateBitmaps();
		game.updatePacmanAnimations(sinceLastPacmanUpdate, pacmanTickDuration);
		game.updateGhostAnimations(sinceLastGhostUpdate, TICK_DURATION_MS);
    }
    
    @Override
	public void render(GameContainer container, StateBasedGame gam, Graphics g)
			throws SlickException {
    	entityManager.renderEntities(container, gam, g);
    	
		int points = game.getPoints();
		int lifes = game.getPacman().getLifes();
		int w = container.getWidth();
		for(int i = 0; i < lifes; ++i)
			g.drawImage(life, w/2-100+i*50, 10);
		
		g.drawString(translator.translateMessage("points")+ " " + String.valueOf(points), w-100, 10);
		g.drawString("Kills " + String.valueOf(game.getKills()), w-100, 25);
		
		//Draw pause button
		if (!game.isLost()) {
			g.drawImage(pauseButton, 10, 8);
		}
		//Draw soundOff button
		if (!game.isLost() && !sound) {
			g.drawImage(soundOff, 45, 8);
		}
		//Draw soundOn button
		if (!game.isLost() && sound) {
			g.drawImage(soundOn, 45, 8);
		}
		
		//Draw pause menu
		if (paused && !game.isLost()) {
			final int SPACING = 10;
		    final int BTN_HI = 50;
		    final int OFFSET = 200;
			g.setColor(new Color(0f, 0f, 0f, 0.5f)); //Set transparency
			g.fillRect(0,0, container.getScreenWidth(), container.getScreenHeight());
			g.drawImage(pausedImg, 265, 50);
			g.drawImage(resumeImg, 300, OFFSET);
			g.drawImage(saveImg, 300, OFFSET+BTN_HI+SPACING*1);
			g.drawImage(restartImg, 300, OFFSET+BTN_HI*2+SPACING*2);
			g.drawImage(exitImg, 300, OFFSET+BTN_HI*3+SPACING*3);
			
		}
		else {
			g.setColor(new Color(255f, 255f, 255f));
		}
	}
}
