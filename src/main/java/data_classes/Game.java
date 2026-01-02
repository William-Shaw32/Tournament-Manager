package data_classes;
/**
 * This class represents a game between two players
 * The schedule holds a list of games
 * A game can be pulled from the schedule and set as the current match
 * @author William Shaw
 */
public class Game 
{
	// The two players in a game
    private Player playerA;
	private Player playerB;
	private boolean played; // True if the game has already been played, false if it has not been played
	
	/**
	 * Constructor
	 * @param playerA Player A
	 * @param playerB Player B
	 */
	public Game(Player playerA, Player playerB)
	{
		// Randomizes assignment of A and B for random ordering when displayed
		if (Math.random() < 0.5) 
		{
        	this.playerA = playerA;
        	this.playerB = playerB;
    	}
		else 
		{
        	this.playerA = playerB;
        	this.playerB = playerA;
    	}
		played = false;
	}

	/**
	 * Getter for playerA
	 * @return playerA
	 */
	public Player getPlayerA()
	{
		return playerA;
	}

	/**
	 * Getter for playerB
	 * @return playerB
	 */
	public Player getPlayerB()
	{
		return playerB;
	}

	/**
	 * Getter for played
	 * @return True if the game has already been played, false if it has not been played
	
	 */
	public boolean getPlayed()
	{
		return played; 
	}
	
	/**
	 * Mutator to set played to true
	 */
	public void markPlayed()
	{
		played = true;
	}

	/**
	 * Overide of the toString() method
	 * The schedule list-view uses toString() to display games in the cells of the list-view
	 * @return How the game should be presented as a string (playerA VS player B)
	 */
	@Override
	public String toString()
	{
		return playerA.getName() + " VS " + playerB.getName();
	}
}
