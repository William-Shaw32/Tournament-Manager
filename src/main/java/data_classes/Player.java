package data_classes;

import java.math.BigDecimal;
import java.math.RoundingMode;
import javafx.scene.paint.Color;

/**
 * This class represents a player in the tournament
 * The player has a name and colour as well as stats which evolve as the tournament progresses
 * @author William Shaw
 * @author Gavin Pitcher
 */
public class Player implements Comparable<Player>
{
	private String name;      // Player name
	private Color colour;     // Player colour (Generated dynamically on creation)
	private int wins;         // Number of wins 
	private int gamesPlayed;  // Number of games played
	private int ralliesWon;   // Total number of rallies won
	private int ralliesLost;  // Total number of rallies lost
	private double ratio;     // Ratio of total rallies won over total rallies lost 

	
	/**
	 * Constructor
	 * Stats are initalized to default values
	 * @param name Player name
	 * @param colour Player colour
	 */
	public Player(String name, Color colour)
	{
		this.name = name;
		this.colour = colour;
		wins = 0;
		gamesPlayed = 0;
		ratio = 1.0;
		ralliesWon = 0;
		ralliesLost = 0;
	}
	
	/**
	 * Getter for player name
	 * @return Player name
	 */
	public String getName()
	{
		return name;
	}

	/**
	 * Getter for player colour
	 * @return Player colour
	 */
	public Color getColour()
	{
		return colour;
	}

	/**
	 * Getter for wins
	 * @return Number of games won
	 */
	public int getWins()
	{
		return wins;
	}

	/**
	 * Getter for games played
	 * @return Total games played
	 */
	public int getGamesPlayed()
	{
		return gamesPlayed;
	}

	/**
	 * Getter for ratio
	 * @return Player ratio (rallies won / rallies lost)
	 */
	public double getRatio()
	{
		double roundedRatio = BigDecimal.valueOf(ratio).setScale(2, RoundingMode.HALF_UP).doubleValue();
		return roundedRatio;
	}

	/**
	 * Setter for player name
	 * @param name Player name
	 */
	public void setName(String name)
	{
		this.name = name;
	}

	/**
	 * Mutator for player stats
	 * This method is called after a player plays a game
	 * @param gameRalliesWon Rallies won in the game that was played
	 * @param gameRalliesLost Rallies lost in the game that was played
	 */
	public void updateStats(int gameRalliesWon, int gameRalliesLost)
	{
		// Increments games played
		gamesPlayed++;
		// Aggregates rallies won and rallies lost
		ralliesWon += gameRalliesWon;
		ralliesLost += gameRalliesLost;
		// Handles divide by 0 issue
		if(ralliesLost == 0)
		{
			ralliesLost = 1;
			ratio = (double)ralliesWon / (double)ralliesLost;
			ralliesLost = 0;	
		}
		// Calculates the new ratio
		else
			ratio = (double)ralliesWon / (double)ralliesLost;
		// Increments wins if the player won the game
		if(gameRalliesWon > gameRalliesLost) wins++;
	}
	
	/**
	 * Resets the player stats to default
	 */
	public void clearStats()
	{
		wins = 0;
		gamesPlayed = 0;
		ralliesWon = 0;
		ralliesLost = 0;
		ratio = 1.0;	
	}

	/**
	 * Override of the the compareTo() method of the comparable interface
	 * It defines how players should be ordered for the purpose of sorting
	 * @param otherPlayer Another player
	 * @return -1 if this before other, 1 if other before this, 0 if players are considered equal
	 */
	@Override
	public int compareTo(Player otherPlayer)
	{
		// Give this player higher ranking (Wins)
		if(this.getWins() > otherPlayer.getWins())
			return -1;
		// Give other player higher ranking (Wins)
		else if(this.getWins() < otherPlayer.getWins())
			return 1;
		// Secondary Criteria (Ratio)
		else if(this.getWins() == otherPlayer.getWins())
		{
			// Give this player higher ranking (Ratio)
			if(this.getRatio() > otherPlayer.getRatio())
				return -1;
			// Give other player higher ranking (Ratio)
			else if(this.getRatio() < otherPlayer.getRatio())
				return 1;
		}
		// Same number of wins and same ratio
		return 0;
	}
}
