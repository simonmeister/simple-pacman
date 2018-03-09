package de.tu_darmstadt.gdi1.pacman.exceptions;

public class InvalidLevelCharacterException extends LevelException
{
	private static final long serialVersionUID = 1L;

	public InvalidLevelCharacterException(char c)
	{
		super("'" + c + "' ist kein gültiger Levelbaustein!");
	}
}
