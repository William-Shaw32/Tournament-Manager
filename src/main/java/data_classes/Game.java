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
	 * Overide of the toString() method
	 * The schedule list-view uses toString() to display games in the cells of the list-view
	 */
	@Override
	public String toString()
	{
		return playerA.getName() + " VS " + playerB.getName();
	}
}
