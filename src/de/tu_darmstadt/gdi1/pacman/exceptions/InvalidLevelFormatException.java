package de.tu_darmstadt.gdi1.pacman.exceptions;

public class InvalidLevelFormatException extends LevelException
{
	private static final long serialVersionUID = 1L;

	public InvalidLevelFormatException()
	{
		super("Das Level muss rechteckig sein!");
	}

}
