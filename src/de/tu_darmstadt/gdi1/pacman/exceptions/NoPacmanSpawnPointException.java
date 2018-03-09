package de.tu_darmstadt.gdi1.pacman.exceptions;

public class NoPacmanSpawnPointException extends LevelException
{
	private static final long serialVersionUID = 1L;
	public NoPacmanSpawnPointException()
	{
		super("Es wurde kein Spawnpunkt für Pacman definiert!");
	}
}
