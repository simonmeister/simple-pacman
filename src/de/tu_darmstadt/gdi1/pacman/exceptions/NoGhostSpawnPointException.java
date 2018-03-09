package de.tu_darmstadt.gdi1.pacman.exceptions;

public class NoGhostSpawnPointException extends LevelException
{
	private static final long serialVersionUID = 1L;
	
	public NoGhostSpawnPointException()
	{
		super("Es wurde kein Spawnpunkt für die Geister definiert!");
	}
}
