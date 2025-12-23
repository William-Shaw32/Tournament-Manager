package terminal_testing;

import java.util.ArrayList;

import data_classes.*;

/**
 * This class can be used to test the schedule builder in the terminal
 *  @author William Shaw
 */
public class ScheduleBuilderTesting 
{
    /**
     * Standard java main function
     * @param args Command line arguments
     */
    public static void main(String[] args)
    {
        ArrayList<Player> players = new ArrayList<>();
        players.add(new Player("Player 1"));
        players.add(new Player("Player 2"));
        players.add(new Player("Player 3"));
        players.add(new Player("Player 4"));
        players.add(new Player("Player 5"));

        int numGamesEach = 4;

        ScheduleBuilder scheduleBuilder = new ScheduleBuilder(players, numGamesEach);
        Schedule schedule = scheduleBuilder.build();

        schedule.printGames();
    }
}