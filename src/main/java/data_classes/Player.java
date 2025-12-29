package data_classes;

import javafx.scene.paint.Color;
import utilities.DynamicColouringUtilities;

public class Player 
{
	private String name;
	private Color colour;
	
	public Player(String name)
	{
		this.name = name;
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

	public void setColour(int numColoursGenerated)
	{
		colour = DynamicColouringUtilities.generateNextColour(numColoursGenerated);	
	}
}
