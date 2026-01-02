package data_classes;

import java.util.ArrayList;

/**
 * This class represents the long term attributes and behaviour of a schedule
 * All creational logic belongs to the schedule builder which constructs schedules
 * There is generally only 1 schedule instance at a time
 * @author William Shaw
 */
public class Schedule 
{
    private ArrayList<Game> games;   // List of games
	private int numGamesInFullRound; // The number of games in a full round
	
	/**
	 * Constructor
	 * Called by the schedule builder once the schedule builder has produced a list of games
	 * @param games List of games
	 * @param numGamesInFullRound The number of games in a full round
	 */
	public Schedule(ArrayList<Game> games, int numGamesInFullRound)
	{
		this.games = games;
		this.numGamesInFullRound = numGamesInFullRound;
	}

	/**
	 * Returns a game from the list of games by index
	 * @param index The index of the requested game
	 * @return The requested game
	 */
	public Game getGame(int index)
	{
		if(index >= games.size())
			return null;
		return games.get(index);
	}

	/**
	 * Gets all games in a given round by round index
	 * The round could be full or partial
	 * @param roundIndex The index of the round being requested
	 * @return A list of games in a single round to be displayed
	 */
	public ArrayList<Game> getGamesInRound(int roundIndex)
	{
		int startIndex = roundIndex * numGamesInFullRound;
		if(startIndex >= games.size())
			return new ArrayList<>();
		int endIndex = startIndex + numGamesInFullRound;
		endIndex = Math.min(endIndex, games.size());
		ArrayList<Game> gamesInRound = new ArrayList<>(games.subList(startIndex, endIndex));
		return gamesInRound;
	}

	/**
	 * Gets the number of rounds in the schedule
	 * @return The number of rounds
	 */
	public int getNumRounds()
	{
		if(games.size() % numGamesInFullRound == 0)
			return games.size() / numGamesInFullRound;
		else
			return (games.size() / numGamesInFullRound) + 1;
	}

	/**
	 * Getter for the number of games in a full round
	 * @return The number of games in a full round
	 */
	public int getNumGamesInFullRound()
	{
		return numGamesInFullRound;
	}

	/**
	 * Changes the index of a game in the schedule
	 * Used to reorder the schedule
	 * @param oldIndex The old index of the game 
	 * @param newIndex The new index where the game will go
	 */
	public void changeGameIndex(int oldIndex, int newIndex)
	{
		Game game = games.remove(oldIndex);
		if(newIndex > oldIndex)
			newIndex--;
		games.add(newIndex, game);
	}	

	/**
	 * Clears the schedule
	 */
	public void clear()
	{
		games.clear();
	}

	/**
	 * Checks if the schedule is empty
	 * @return True if empty, false otherwise
	 */
	public boolean isEmpty()
	{
		return games.isEmpty();
	}


	/**
	 * Updates all occurances of a player's name in the schedule
	 * @param player The player whose name was changed
	 */
	public void updatePlayerName(Player player)
	{
		for(int i = 0; i < games.size(); i++)
		{
			Player playerA = games.get(i).getPlayerA();
			Player playerB = games.get(i).getPlayerB();
			if(player == playerA)
				playerA.setName(player.getName());
			else if(player == playerB)
				playerB.setName(player.getName());
		}
	}

	/**
	 * Marks a game as played in the schedule by index
	 * @param index The index of the game to be marked as played
	 */
	public void markGamePlayed(int index)
	{
		games.get(index).markPlayed();
	}
	
	/**
	 * Prints the list of games out to a terminal
	 * Used for testing the schedule builder with terminal testing
	 */
	public void printGames()
	{
		int roundNumber = 1;
		System.out.println("Round 1: ");
		System.out.println(games.get(0));
		for(int i = 1; i < games.size(); i++)
		{
			if(i % numGamesInFullRound == 0)
				System.out.println("\nRound " + ++roundNumber + ":");
			System.out.println(games.get(i));
		}
	}
}
