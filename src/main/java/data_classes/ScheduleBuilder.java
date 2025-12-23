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
	private ArrayList<Match> matchesLeftInRound = new ArrayList<>();        // List of unique matches left to be scheduled in a given round
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
		private String name;            // Player name
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

		/**
		 * 
		 * @param copy Another schedulee
		 */
		private Schedulee(Schedulee copy)
		{
			this.name = copy.name;
			this.numMatchesIn = copy.numMatchesIn;
			this.lastMatchIndex = copy.lastMatchIndex;
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
		public void updateScheduleeInfoAfterRemoval(int matchIndexParam)
		{
			scheduleeA.numMatchesIn++;
			scheduleeB.numMatchesIn++;
			scheduleeA.lastMatchIndex = matchIndexParam;
			scheduleeB.lastMatchIndex = matchIndexParam;
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
		createMatches(schedulees);
		scheduleAllRounds();
		convertToGames();
		// Final built schedule
		Schedule schedule = new Schedule(scheduledGames, numGamesInFullRound);
		return schedule;
	}
	
	/**
	 * Private helper to create a list of all unique matches
	 */
	private void createMatches(ArrayList<Schedulee> scheduleesParam)
	{
		uniqueMatches.clear();
		// Enumerates unique matches (nC2)
		for(int i = 0; i < scheduleesParam.size(); i++)
		{
			for(int j = i + 1; j < scheduleesParam.size(); j++)
			{
				uniqueMatches.add(new Match(scheduleesParam.get(i), scheduleesParam.get(j)));
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
		boolean partialRoundScheduled = false;
		while(partialRoundScheduled == false)
		{
			// Schedules final partial round
			partialRoundScheduled = schedulePartialRound();
		}
	}

	/**
	 * Private helper to schedule a single full round
	 */
	private void scheduleFullRound()
	{
		Collections.shuffle(uniqueMatches); // Randomization
		// List of unique matches left to be scheduled in a given round (Shrinks with each iteration)
		matchesLeftInRound = new ArrayList<>(uniqueMatches); 
		for(int i = 0; i < numGamesInFullRound; i++)
		{
			Match bestMatch = selectBestMatch(); // Best match out of the remaining unique matches
			scheduledMatches.add(bestMatch);
			matchIndex++;
			bestMatch.updateScheduleeInfoAfterRemoval(matchIndex);
		}
	}
	
	/**
	 * Private helper to schedule the partial round
	 * @return True if the partial round was scheduled successfully, false if it got stuck
	 */
	private boolean schedulePartialRound()
	{
		HashMap<Schedulee, Schedulee> copyToOriginal = new HashMap<>();
		ArrayList<Schedulee> scheduleesCopy = deepCopySchedulees(schedulees, copyToOriginal); // Deep copies the schedulees list
		createMatches(scheduleesCopy); 												// Recreates the set of unique matches
		Collections.shuffle(uniqueMatches);
		// List of unique matches left to be scheduled in a given round (Shrinks with each iteration)
		matchesLeftInRound = new ArrayList<>(uniqueMatches); 
		ArrayList<Match> potentialMatches = new ArrayList<>();
		int tempMatchIndex = matchIndex;
		for(int i = 0; i < numGamesInPartialRound; i++)
		{
			// Checks if the algorithm has gotten stuck
			if(matchesLeftInRound.isEmpty())
				return false;
			Match bestMatch = selectBestMatch(); // Best match out of the remaining unique matches
			potentialMatches.add(bestMatch);
			tempMatchIndex++;
			bestMatch.updateScheduleeInfoAfterRemoval(tempMatchIndex);
			// Prunes matches left in pool for schedulees who are finished in a partial round
			if(bestMatch.scheduleeA.numMatchesIn >= numGamesEach)
			pruneMatchesForSchedulee(bestMatch.scheduleeA);
			if(bestMatch.scheduleeB.numMatchesIn >= numGamesEach)
			pruneMatchesForSchedulee(bestMatch.scheduleeB);	
		}
		// Maps schedulee copies back to the originals and adds new matches to the list of matches
		for (int i = 0; i < potentialMatches.size(); i++)
		{
    		Schedulee originalA = copyToOriginal.get(potentialMatches.get(i).scheduleeA);
    		Schedulee originalB = copyToOriginal.get(potentialMatches.get(i).scheduleeB);
    		scheduledMatches.add(new Match(originalA, originalB));
		}
		matchIndex = tempMatchIndex; // Commits the tempMatchIndex as the matchIndex
		return true;
	}
	
	/**
	 * Private helper to find the best next match. Greedy search function. Local Optimization
	 * Prioritizes the matches with the lowest sum of numMatchesIn
	 * Tie Breaks with the lowest sum of lastMatchIndex
	 * @return Best next match
	 */
	private Match selectBestMatch()
	{
		Match bestMatch = matchesLeftInRound.get(0); // Replaced when better match found
		int bestIndex = 0;
		// Loops through all remaining matches
		for(int i = 1; i < matchesLeftInRound.size(); i++)
		{
			Match candidateMatch = matchesLeftInRound.get(i);
			// Outer heuristic (Sum of matches scheduled)
			if(candidateMatch.sumNumMatchesIn() < bestMatch.sumNumMatchesIn())
			{
				bestMatch = candidateMatch;
				bestIndex = i;
			}
			else if(candidateMatch.sumNumMatchesIn() == bestMatch.sumNumMatchesIn())
			{
				// Inner heuristic (Sum of indices of last matches scheduled)
				if(candidateMatch.sumLastMatchIndices() < bestMatch.sumLastMatchIndices())
				{
					bestMatch = candidateMatch;
					bestIndex = i;
				}
			}
		}
		matchesLeftInRound.remove(bestIndex); // Removes the scheduled match
		return bestMatch;
	}

	/**
	 * Private helper to prune matches from players who are finished all their matches in the partial round
	 * @param schedulee The schedulee who is finished all their matches
	 */
	private void pruneMatchesForSchedulee(Schedulee schedulee)
	{
    	for (int i = matchesLeftInRound.size() - 1; i >= 0; i--)
    	{
        	Match match = matchesLeftInRound.get(i);
        	if (match.scheduleeA == schedulee || match.scheduleeB == schedulee)
        	{
        	    matchesLeftInRound.remove(i);
        	}
    	}
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

	/**
	 * Private helper to deep copy the list of schedulees
	 * @param originals Original list of schedulees
	 * @param copyToOriginal Map from the copies back to the originals
	 * @return A new list of deep copied schedulees
	 */
	private ArrayList<Schedulee> deepCopySchedulees(ArrayList<Schedulee> originals, HashMap<Schedulee, Schedulee> copyToOriginal)
	{
		ArrayList<Schedulee> copies = new ArrayList<>(originals.size());
		for(int i = 0; i < originals.size(); i++)
		{
			Schedulee original = originals.get(i);
			Schedulee copy = new Schedulee(original);
			copies.add(copy);
			copyToOriginal.put(copy, original);
		}
		return copies;
	}
}
