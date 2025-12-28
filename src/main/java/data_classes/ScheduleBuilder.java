package data_classes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

/**
 * This class is responsible for constructing schedule objects
 * It follows the builder design pattern
 * It uses a greedy search algorithm to schedule games according to heuristics like numMatchesIn and lastMatchIndex
 */
public class ScheduleBuilder 
{
    // Private data structures
	private ArrayList<Schedulee> schedulees = new ArrayList<>();            // List of schedulees
	private ArrayList<Match> uniqueMatches = new ArrayList<>();             // List of all unique matches
	private ArrayList<Match> scheduledMatches = new ArrayList<>();          // List of scheduled matches (Accumulates as rounds are scheduled)
	private ArrayList<Game> scheduledGames = new ArrayList<>();             // List of scheduled games (Converted from matches)
	private HashMap<Schedulee, Player> scheduleeToPlayer = new HashMap<>(); // Maps schedulees back to players
	
	// Private attributes
	private int matchIndex = 0;          // Index of the current match being schedule. Counts total matches scheduled
	private int numGamesEach;            // The total number of games each person plays
	private int numGamesInFullRound;     // The number of games in a full round (Round robin) (nC2)
	private int numGamesInPartialRound;  // The number of games in the final partial round (Remainder)
	private int numFullRounds;           // The number of complete rounds (Round robins)
	
	/**
	 * Private Inner Class Schedulee
	 * Mirror of the Player class with a different set of attributes needed for the scheduling algorithm
	 * @author William Shaw
	 */
	private class Schedulee
	{
		@SuppressWarnings("unused")
		private String name;            // Player name (For debugging only)
		private int numMatchesIn = 0;   // The number of matches the schedulee has been scheduled in already (Greedy heuristic)
		private int lastMatchIndex = 0; // THe index of the last match the schedulee was scheduled in (Greedy heuristic)
		
		/**
		 * Constructor
		 * @param name Player name
		 */
		private Schedulee(String name)
		{
			this.name = name;
		}
	}	
	
	/**
	 * Private Inner Class Match
	 * Mirror of the Game class with a different set of attributes needed for the scheduling algorithm
	 * @author William Shaw
	 */
	private class Match
	{
		// Pair of schedulees
		private Schedulee scheduleeA;
		private Schedulee scheduleeB;
		
		/**
		 * Constructor
		 * @param scheduleeA First schedulee
		 * @param scheduleeB Second Schedulee
		 */
		private Match(Schedulee scheduleeA, Schedulee scheduleeB)
		{
			this.scheduleeA = scheduleeA;
			this.scheduleeB = scheduleeB;
		}
		
		/**
		 * Sums the number of matches each player has already been scheduled in (Greedy heuristic)
		 * @return
		 */
		private int sumNumMatchesIn()
		{
			return scheduleeA.numMatchesIn + scheduleeB.numMatchesIn;
		}
		
		/**
		 * Sums the indices of the last match each player was scheduled in (Greedy heuristic)
		 * @return
		 */
		public int sumLastMatchIndices()
		{
			return scheduleeA.lastMatchIndex + scheduleeB.lastMatchIndex;
		}
		
		/**
		 * Updates the schedulee info after a match is scheduled
		 */
		public void updateScheduleeInfoAfterRemoval()
		{
			scheduleeA.numMatchesIn++;
			scheduleeB.numMatchesIn++;
			scheduleeA.lastMatchIndex = matchIndex;
			scheduleeB.lastMatchIndex = matchIndex;
		}

		/**
		 * Undoes the schedulee info after backtracking (Partial round only)
		 * @param oldLastMatchIndexA
		 * @param oldLastMatchIndexB
		 */
		public void undoScheduleeInfoAfterRemoval(int oldLastMatchIndexA, int oldLastMatchIndexB)
		{
			scheduleeA.numMatchesIn--;
			scheduleeB.numMatchesIn--;
			scheduleeA.lastMatchIndex = oldLastMatchIndexA;
			scheduleeB.lastMatchIndex = oldLastMatchIndexB;
		}
	}
	
	/**
	 * Constructor for the ScheduleBuilder class
	 * @param players List of all players in the tournament
	 * @param numGamesEach The number of games each player will play
	 */
	public ScheduleBuilder(ArrayList<Player> players, int numGamesEach)
	{
		// Fails if both the number of players and the number of games each are odd
		if(players.size() % 2 == 1 && numGamesEach % 2 == 1)
		{
			System.err.println("Error - Cannot build schedule: If the number of players is odd, then the number of games each must be even");
			System.exit(1);
		}
		// Converts players into schedulees
		for(int i = 0; i < players.size(); i++)
		{
			String playerName = players.get(i).getName();
			Schedulee schedulee = new Schedulee(playerName);
			scheduleeToPlayer.put(schedulee, players.get(i));
			schedulees.add(schedulee);
		}
		// Sets numerical attributes
		this.numGamesEach = numGamesEach;
		int numGamesTotal = (players.size() * numGamesEach) / 2; // (ng/2)
		numGamesInFullRound = (players.size() * (players.size() - 1)) / 2; // (nC2)
		numGamesInPartialRound = numGamesTotal % numGamesInFullRound; // Remainder
		numFullRounds = numGamesTotal / numGamesInFullRound;
	}
	
	/**
	 * Public facing build function. Used to execute the creation of a schedule object
	 * @return A schedule object
	 */
	public Schedule build()
	{
		createMatches();
		scheduleAllRounds();
		convertToGames();
		// Final built schedule
		Schedule schedule = new Schedule(scheduledGames, numGamesInFullRound);
		return schedule;
	}
	
	/**
	 * Private helper to create a list of all unique matches
	 */
	private void createMatches()
	{
		uniqueMatches.clear();
		// Enumerates unique matches (nC2)
		for(int i = 0; i < schedulees.size(); i++)
		{
			for(int j = i + 1; j < schedulees.size(); j++)
			{
				uniqueMatches.add(new Match(schedulees.get(i), schedulees.get(j)));
			}
		}
	}
	
	/**
	 * Private helper to schedule all rounds (Populate the scheduledMatches list)
	 */
	private void scheduleAllRounds()
	{
		// Schedules all complete rounds
		for(int i = 0; i < numFullRounds; i++)
		{
			scheduleFullRound();
		}
		schedulePartialRound();
	
	}

	/**
	 * Private helper to schedule a single full round
	 */
	private void scheduleFullRound()
	{
		// Candidate matches in a round. Shrinks as matches are scheduled
		ArrayList<Match> candidateMatches = new ArrayList<>(uniqueMatches); 
		// Randomization
		Collections.shuffle(candidateMatches);
		// Recursively schedules the next match (Retuns true if successful, false if unsuccessful)
		boolean success = scheduleNextMatch(0, numGamesInFullRound, candidateMatches);
		// Fails loudly if the full round could not be scheduled
		if(!success)
		{
			System.err.println("Schedule full round failed");
			System.exit(1);	
		}
	}
	
	/**
	 * Private helper to schedule the partial round
	 */
	private void schedulePartialRound()
	{
		// Candidate matches in a round. Shrinks as matches are scheduled
		ArrayList<Match> candidateMatches = new ArrayList<>(uniqueMatches); 
		// Randomization
		Collections.shuffle(candidateMatches);
		// Recursively schedules the next match (Retuns true if successful, false if unsuccessful)
		boolean success = scheduleNextMatch(0, numGamesInPartialRound, candidateMatches);
		// Fails loudly if the partial round could not be sceduled
		if(!success)
		{
			System.err.println("Schedule partial round failed");
			System.exit(1);		
		}
	}


	/**
	 * Recursive method to schedule the next match
	 * Backtracks in the partial round when the algorithm gets stuck
	 * Never needs to backtrack when scheduling a full round
	 * @param numGamesScheduled Counts the number of games scheduled so far in the round
	 * @param numGamesInRound numGamesInFullRound or numGamesInPartialRound depending on round type
	 * @param candidateMatches Candidate matches in a round. Shrinks as matches are scheduled
	 * @return True if a match was scheduled, false if backtracking is necessary
	 */
	private boolean scheduleNextMatch(int numGamesScheduled, int numGamesInRound, ArrayList<Match> candidateMatches)
	{
		// Base case: Returns true when all games have been scheduled successfully
		if (numGamesScheduled == numGamesInRound)
			return true;

		// Local list of tried matches in a single recursion frame
		ArrayList<Match> triedMatches = new ArrayList<>();

		// Loops through all candidate matches in each recursion frame if necessary (Only continues looping when best matche is invalid)
		while (candidateMatches.size() > 0)
		{
			Match bestMatch = selectBestMatch(candidateMatches); // Finds the best match from the candidate matches
			// Removes the best match from the candidates list since it has been used
			candidateMatches.remove(bestMatch); 
			// Adds the best match to the local list of tried matches                  
			triedMatches.add(bestMatch); 
			// Continues to get a new best match if the best match is invalid
			if(bestMatch.scheduleeA.numMatchesIn >= numGamesEach)
				continue;
			if(bestMatch.scheduleeB.numMatchesIn >= numGamesEach)
				continue;
			// Saves the old last match indices before commiting the best match
			int oldLastMatchIndexA = bestMatch.scheduleeA.lastMatchIndex;
			int oldLastMatchIndexB = bestMatch.scheduleeB.lastMatchIndex;
			// Commits the best match (Could still be changed in the future through backtracking)
        	scheduledMatches.add(bestMatch);
        	matchIndex++;
        	bestMatch.updateScheduleeInfoAfterRemoval();
			// Recursively schedules the next match 
			if (scheduleNextMatch(numGamesScheduled+1, numGamesInRound, candidateMatches))
        		return true;
			// BACKTRACKING NEEDED
			// Removes the last scheduled match from the list of scheduled matches
			scheduledMatches.remove(scheduledMatches.size() - 1);
			// Decrements the match index
			matchIndex--;
			// Undoes the updates to the schedulee info once the match is uncommited
			bestMatch.undoScheduleeInfoAfterRemoval(oldLastMatchIndexA, oldLastMatchIndexB);
		}

		// Adds all tried matches back to the candidate matches list before backtracking 
    	for (int i = 0; i < triedMatches.size(); i++)
    	{
        	candidateMatches.add(triedMatches.get(i));
    	}
		
		// Backtracks
		return false;
	}
	
	/**
	 * Private helper to find the best next match. Greedy search function. Local Optimization
	 * Prioritizes the matches with the lowest sum of numMatchesIn
	 * Tie Breaks with the lowest sum of lastMatchIndex
	 * @param candidateMatches Candidate matches in a round. Shrinks as matches are scheduled
	 * @return Best next match
	 */
	private Match selectBestMatch(ArrayList<Match> candidateMatches)
	{
		// The best match out of the candidate matches (initialized at match 0)
		Match bestMatch = candidateMatches.get(0);
		// Loops through all remaining matches
		for(int i = 1; i < candidateMatches.size(); i++)
		{
			Match candidateMatch = candidateMatches.get(i);
			// Outer heuristic (Sum of matches scheduled)
			if(candidateMatch.sumNumMatchesIn() < bestMatch.sumNumMatchesIn())
			{
				bestMatch = candidateMatch;
			}
			else if(candidateMatch.sumNumMatchesIn() == bestMatch.sumNumMatchesIn())
			{
				// Inner heuristic (Sum of indices of last matches scheduled)
				if(candidateMatch.sumLastMatchIndices() < bestMatch.sumLastMatchIndices())
				{
					bestMatch = candidateMatch;
				}
			}
		}
		return bestMatch;	
	}	
	
	/**
	 * Private helper to convert the list of scheduled matches into a list of scheduled games
	 * This is needed because the schedule object contains a list of games not matches
	 */
	private void convertToGames()
	{
		for(int i = 0; i < scheduledMatches.size(); i++)
		{
			// Maps schedulees back to players
			Player playerA = scheduleeToPlayer.get(scheduledMatches.get(i).scheduleeA);
			Player playerB = scheduleeToPlayer.get(scheduledMatches.get(i).scheduleeB);
			// Creates and adds a new game from the players
			Game game = new Game(playerA, playerB);
			scheduledGames.add(game);
		}
	}
}
