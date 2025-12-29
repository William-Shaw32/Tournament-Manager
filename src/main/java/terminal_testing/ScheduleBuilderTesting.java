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
        players.add(new Player("Player 1", null));
        players.add(new Player("Player 2", null));
        players.add(new Player("Player 3", null));
        players.add(new Player("Player 4", null));
        players.add(new Player("Player 5", null));

        int numGamesEach = 4;

        ScheduleBuilder scheduleBuilder = new ScheduleBuilder(players, numGamesEach);
        Schedule schedule = scheduleBuilder.build();

        schedule.printGames();
    }
}