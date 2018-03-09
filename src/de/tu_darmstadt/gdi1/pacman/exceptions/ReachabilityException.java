package de.tu_darmstadt.gdi1.pacman.exceptions;

public class ReachabilityException extends LevelException
{
	private static final long serialVersionUID = 1L;
	public ReachabilityException()
	{
		super("Im Level sind unerreichbare Bereiche vorhanden.");
	}
}
