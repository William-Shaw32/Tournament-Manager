package data_classes;

import java.math.BigDecimal;
import java.math.RoundingMode;

import javafx.scene.paint.Color;

public class Player 
{
	private String name;
	private Color colour;
	private int wins;
	private int gamesPlayed;
	private double ratio;
	private int ralliesWon;
	private int ralliesLost;

	
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

	public int getWins()
	{
		return wins;
	}

	public int getGamesPlayed()
	{
		return gamesPlayed;
	}

	public double getRatio()
	{
		double roundedRatio = BigDecimal.valueOf(ratio).setScale(2, RoundingMode.HALF_UP).doubleValue();
		return roundedRatio;
	}

	public void updateStats(int gameRalliesWon, int gameRalliesLost)
	{
		gamesPlayed++;
		ralliesWon += gameRalliesWon;
		ralliesLost += gameRalliesLost;
		if(ralliesLost == 0) ralliesLost = 1;
		ratio = (double)ralliesWon / (double)ralliesLost;
		if(gameRalliesWon > gameRalliesLost) wins++;
	}
}
