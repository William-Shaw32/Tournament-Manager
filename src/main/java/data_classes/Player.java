package data_classes;

import javafx.scene.paint.Color;

public class Player 
{
	private String name;
	private Color colour;
	
	public Player(String name, Color colour)
	{
		this.name = name;
		this.colour = colour;
	}
	
	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public Color getColour()
	{
		return colour;
	}
}
