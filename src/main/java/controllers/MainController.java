package controllers;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.Queue;

import data_classes.*;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import utilities.*;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.event.ActionEvent;
import javafx.scene.paint.Color;

/**
 * This is the main controller class for the main-view fxml file
 * It acts as an intermediary layer between the UI and the data classes
 * It handles input, output, binding, and configuration for the UI
 * @author William Shaw
 */
public class MainController 
{
    // UI objects
    @FXML private StackPane root;
    @FXML private Spinner<Integer> gamesEachSpinner;
    @FXML private Pagination roundsPagination;
    @FXML private Label roundNumLabel;
    @FXML private ListView<Game> scheduleListView;
    @FXML private VBox rightVBox;
    @FXML private VBox rightTopVBox;
    @FXML private VBox rightCenterVBox;
    @FXML private Region paginationVSpacer;
    @FXML private ToggleButton hideScheduleToggle;
    @FXML private HBox roundsHBox;
    @FXML private ToggleButton editScheduleToggle;
    @FXML private Button generateScheduleButton;
    @FXML private Button startEndTournamentButton;
    @FXML private Label numGamesRemainingLabel;
    @FXML private HBox scheduleConfigHBox;
    @FXML private Button addPlayerButton;
    @FXML private TableView<Player> playersTableView;
    @FXML private TableColumn<Player, String> nameColumn;
    @FXML private TableColumn<Player, Integer> winsColumn;
    @FXML private TableColumn<Player, Integer> playedColumn;
    @FXML private TableColumn<Player, Double> ratioColumn;
    @FXML private VBox leftVBox;
    @FXML private VBox leftTopVBox;
    @FXML private Button removePlayerButton;
    @FXML private Label statsLabel;
    @FXML private Label player1ScoreLabel;
    @FXML private Label player2ScoreLabel;
    @FXML private Spinner<Integer> player1Spinner;
    @FXML private Spinner<Integer> player2Spinner;
    @FXML private Button endGameButton;
    @FXML private TextField scoreToWinTextField;

    // Data objects
    private ArrayList<Player> players = new ArrayList<>();
    private Schedule schedule; 
    private Player selectedPlayer; 
    private Queue<Color> cachedColours = new ArrayDeque<>();
    private Game currentGame;
    private Player playerA;
    private Player playerB;

    // Primatives
    private AtomicBoolean dragDropEnabled = new AtomicBoolean(false);
    private AtomicInteger currentGameIndex = new AtomicInteger(0);
    private AtomicInteger numGamesInFullRound = new AtomicInteger(0);
    private BooleanProperty tournamentIsActive = new SimpleBooleanProperty(false);
    private int numGamesRemaining = 0;
    private int numColoursGenerated;
    private int scoreToWin;

    /**
     * This is the initialize functiton for the main controller
     * JavaFX calls it when the main-view loads
     */
    @FXML
    private void initialize()
    {
        playersTableView.setVisible(false);
        statsLabel.setVisible(false);
        startEndTournamentButton.setDisable(true);
        endGameButton.setDisable(true);
        player1Spinner.setDisable(true);
        player2Spinner.setDisable(true);
        root.setFocusTraversable(true);
        MainControllerUtilities.configurePlayerSpinners(player1Spinner, player2Spinner);
        // Configures the behaviour of the ganes each spinner
        MainControllerUtilities.configureGamesEachSpinner( 
            gamesEachSpinner, 
            root,
            generateScheduleButton);
        // Binds the rouund number label to the rounds paginator
        roundNumLabel.textProperty().bind(
            roundsPagination.currentPageIndexProperty().add(1).asString()
        );
        // Creates an event listener for viewing a different round
        roundsPagination.currentPageIndexProperty().addListener(
            (obs, oldIndex, newIndex) -> displayRound()
        );
        // Configures the dynamic behaviour of the schedule list view
        MainControllerUtilities.configureScheduleListView(scheduleListView, root, editScheduleToggle);
        rightVBox.heightProperty().addListener((obs, o, n) -> 
            MainControllerUtilities.resizeSchedule(scheduleListView, rightVBox, rightTopVBox, roundsPagination, paginationVSpacer));
        rightTopVBox.heightProperty().addListener((obs, o, n) -> 
            MainControllerUtilities.resizeSchedule(scheduleListView, rightVBox, rightTopVBox, roundsPagination, paginationVSpacer));
        // Makes the schedule list view draggable
        DragDropUtilities.configureDragDrop(scheduleListView, game -> new Label(game.toString()), this::reorderGame, dragDropEnabled, 
        player1Spinner, player2Spinner, roundsPagination, currentGameIndex, numGamesInFullRound);
        MainControllerUtilities.configurePlayersTable(playersTableView, removePlayerButton);
        MainControllerUtilities.configureNameColumn(nameColumn);
        // Configures the dynamic behaviour of the players table view
        rightVBox.heightProperty().addListener((obs, o, n) ->
            MainControllerUtilities.resizePlayersTable(playersTableView, rightVBox, rightTopVBox));
        rightTopVBox.heightProperty().addListener((obs, o, n) ->
            MainControllerUtilities.resizePlayersTable(playersTableView, rightVBox, rightTopVBox));
        // Configures the players table columns (Particularily the refresh maping)
        MainControllerUtilities.configureAllTableColumns(nameColumn, winsColumn, playedColumn, ratioColumn);
        // Lister to cache the selected player whenever a row of the table view is selected
        playersTableView.getSelectionModel().selectedItemProperty().addListener(
            (obs, oldP, newP) -> {
                if (newP != null) selectedPlayer = newP;
            }
        );
        // Binds remove player button disable to whether a player is selected or not
        removePlayerButton.disableProperty().bind(
            playersTableView.getSelectionModel().selectedItemProperty().isNull().or(tournamentIsActive)
        );
        // Configures score to win texxt field to take only digits
        MainControllerUtilities.configureScoreToWinTextField(scoreToWinTextField);
        // Listens for changes in the score to win label
        scoreToWinTextField.textProperty().addListener((obs, oldText, newText) -> {
            if (newText == null || newText.isEmpty()) {
                scoreToWin = 0;
                return;
            }
            scoreToWin = Integer.parseInt(newText);
        });
    }



    // ===========================================================================================================================================
    // Event Handlers
    // ===========================================================================================================================================

    /**
     * This function generates a schedule when the user clicks the generate schedule button
     * @param e The action-event that triggered the handler
     */
    @FXML
    private void generateSchedule(ActionEvent e)
    {
        int numGamesEach = gamesEachSpinner.getValue();
        if(numGamesEach % 2 == 1 && players.size() % 2 == 1)
        {
            if (schedule != null) schedule.clear();
            scheduleListView.getItems().clear();
            generateScheduleButton.setText("Generate");
            numGamesRemaining = 0;
            numGamesRemainingLabel.setText(Integer.toString(numGamesRemaining));
            roundsPagination.setPageCount(Pagination.INDETERMINATE);
            startEndTournamentButton.setDisable(true);
            MainControllerUtilities.createBasicAlert(
                Alert.AlertType.WARNING, "Warning", 
                "Unable to generate schedule", 
                "If the number of players is odd, then each player must play an even number of games. Please select an even number of games each").showAndWait();
            return;
        }
        generateScheduleButton.setText("Regenerate");
        roundsPagination.setCurrentPageIndex(0);
        ScheduleBuilder sb = new ScheduleBuilder(players, numGamesEach);
        schedule = sb.build(); 
        numGamesInFullRound.set(schedule.getNumGamesInFullRound());
        displayRound();  
        MainControllerUtilities.resizeSchedule(scheduleListView, rightVBox, rightTopVBox, roundsPagination, paginationVSpacer);
        numGamesRemaining = (players.size() * numGamesEach) / 2;
        numGamesRemainingLabel.setText(Integer.toString(numGamesRemaining));
        roundsPagination.setPageCount(schedule.getNumRounds());
        startEndTournamentButton.setDisable(false);
        if(hideScheduleToggle.isSelected())
        {
            int numGamesTotal = (players.size() * numGamesEach) / 2;
            int numRounds = schedule.getNumRounds();
            MainControllerUtilities.createBasicAlert(
                Alert.AlertType.INFORMATION, "Success", 
                "Schedule Generated Successfully!", 
                "Games Each: " + numGamesEach + "\n" + 
                "Games Total: " + numGamesTotal + "\n" + 
                "Rounds Needed: " + numRounds + "\n").showAndWait();
            return;    
        }
    }  
    
    /**
     * This is a callback function to reorder a game in the schedule. It is called when the user drags and drops a game into a new position
     * @param dragIndex The list-view index of the cell that was dragged
     * @param dropIndex The list-view index of where the cell was dropped
     */
    private void reorderGame(int oldIndex, int newIndex)
    {
        schedule.changeGameIndex(oldIndex, newIndex);
        displayRound();
        if(oldIndex == currentGameIndex.get() || newIndex == currentGameIndex.get())
        {
            currentGame = schedule.getGame(currentGameIndex.get());  
            loadCurrentGame();  
        }
    }

    /**
     * This function hides or shows the schedule when the user clicks the toggle schedule button
     * @param e The action-event that triggered the handler
     */
    @FXML
    private void toggleHideSchedule(ActionEvent e)
    {
        if(hideScheduleToggle.isSelected())
        {
            rightCenterVBox.setVisible(false);
            roundsHBox.setVisible(false);
        }
        else if(!hideScheduleToggle.isSelected())
        {   
            rightCenterVBox.setVisible(true);
            roundsHBox.setVisible(true);
        }
    }

    /**
     * This function swwitches the schedule to and from edit-enabled when the user clicks the edit schedule button
     * @param e The action-event that triggered the handler
     */
    @FXML
    private void toggleEditSchedule(ActionEvent e)
    {
        if(editScheduleToggle.isSelected())
        {
            dragDropEnabled.set(true);
        }
        else if(!editScheduleToggle.isSelected())
        {
            dragDropEnabled.set(false);
        }   
    }

    /**
     * This function starts or ends the tournament when the user clicks the start/end tournament button
     * @param e The action-event that triggered the handler
     */
    @FXML
    private void startEndTournament(ActionEvent e)
    {
        if(tournamentIsActive.get() == false)
            startTournament();
        else if(tournamentIsActive.get() == true)
            endTournament();
    }
    
    /**
     * This function is called when the tournament starts. It puts the tournament into active mode
     */
    private void startTournament()
    {
        tournamentIsActive.set(true);
        startEndTournamentButton.setText("End Tournament");
        scheduleConfigHBox.setDisable(true);
        addPlayerButton.setDisable(true);
        endGameButton.setDisable(false);
        player1Spinner.setDisable(false);
        player2Spinner.setDisable(false);

        // Pulls first game
        currentGame = schedule.getGame(0);
        loadCurrentGame();
    }

    /**
     * This function is called when the tournament ends. It puts the tournament into setup or configuration mode
     */
    private void endTournament()
    {
        if(numGamesRemaining > 0)
        {
            Optional<ButtonType> result = MainControllerUtilities.createDecisionAlert(
                 Alert.AlertType.WARNING, "Warning", 
                "There are still games remaining", 
                "Are you sure you want to end the tournament?").showAndWait();
            if (!result.isPresent() || result.get().getButtonData() == ButtonBar.ButtonData.CANCEL_CLOSE)
                return;
        }
        
        Alert alert = MainControllerUtilities.createClearKeepAlert(
                 Alert.AlertType.CONFIRMATION, "End Tournament", 
                "Player Stats", 
                "Would you like to clear the player stats?");
        Optional<ButtonType> result = alert.showAndWait();
        if (!result.isPresent() || result.get().getButtonData() == ButtonBar.ButtonData.CANCEL_CLOSE)
            return;
        ButtonType chosen = result.get();
        ButtonType clearButton = alert.getButtonTypes().get(0);
        if(chosen == clearButton)
        {
            for(int i = 0; i < players.size(); i++)
            {
                players.get(i).clearStats();
            } 
            playersTableView.refresh();  
            
        } 
        tournamentIsActive.set(false);
        clearScoreboard();
        startEndTournamentButton.setText("Start Tournament");
        currentGameIndex.set(0);
        schedule.clear();
        scheduleListView.getItems().clear();
        numGamesRemaining = 0;
        numGamesRemainingLabel.setText("0");
        generateScheduleButton.setText("Generate");
        scheduleConfigHBox.setDisable(false);
        addPlayerButton.setDisable(false);
        startEndTournamentButton.setDisable(true);
        roundsPagination.setPageCount(Pagination.INDETERMINATE);
        scoreToWinTextField.setDisable(false);
    }

    /**
     * This function is called when the user presses the add player button
     * It creates an empty player with a dynamically generated colour
     */
    @FXML
    private void addPlayer()
    {
        playersTableView.setVisible(true);
        statsLabel.setVisible(true);
        String name = "Player " + (players.size()+1);
        Color colour = null;
        if(!cachedColours.isEmpty())
            colour = cachedColours.poll();
        else
        {
            colour = DynamicColouringUtilities.generateNextColour(numColoursGenerated);
            numColoursGenerated++;
        }
        numColoursGenerated++;
        Player newPlayer = new Player(name, colour);
        players.add(newPlayer);
        playersTableView.getItems().add(newPlayer); 
        MainControllerUtilities.resizePlayersTable(playersTableView, rightVBox, rightTopVBox); 
        if(players.size() >= 2)
        {
            gamesEachSpinner.getValueFactory().setValue(players.size()-1);
            gamesEachSpinner.getEditor().setText(String.valueOf(players.size()-1));
            gamesEachSpinner.setDisable(false);
            generateScheduleButton.setDisable(false);
        }
    }

    @FXML
    private void removePlayer()
    {
        players.remove(selectedPlayer);
        cachedColours.offer(selectedPlayer.getColour());
        playersTableView.getItems().setAll(players);
        MainControllerUtilities.resizePlayersTable(playersTableView, rightVBox, rightTopVBox);
        gamesEachSpinner.getValueFactory().setValue(players.size()-1);
        gamesEachSpinner.getEditor().setText(String.valueOf(players.size()-1));
        if(schedule != null && !schedule.isEmpty())
        {
            schedule.clear();
            scheduleListView.getItems().clear();
            generateScheduleButton.setText("Generate");
            numGamesRemaining = 0;
            numGamesRemainingLabel.setText(Integer.toString(numGamesRemaining));
            roundsPagination.setPageCount(Pagination.INDETERMINATE);
            startEndTournamentButton.setDisable(true);
        } 
        if(players.size() < 2)
        {
            gamesEachSpinner.setDisable(true);
            generateScheduleButton.setDisable(true);
            gamesEachSpinner.getEditor().clear();
        }
    }

    /**
     * This function is called when the user commits a name to the name column in the players table
     * It updates their name in the table and cascades to update their name in the schedule as well
     * @param e
     */
    @FXML
    private void onNameEditCommit(TableColumn.CellEditEvent<Player, String> e) 
    {
        Player player = e.getRowValue();
        player.setName(e.getNewValue());
        playersTableView.refresh();
        if(schedule == null || schedule.isEmpty())
            return;
        schedule.updatePlayerName(player);
        displayRound();
    }

    @FXML
    private void endGame()
    {  
        // Checks player scores
        int player1Score = player1Spinner.getValue();
        int player2Score = player2Spinner.getValue();
        if(player1Score == player2Score)
        {
            MainControllerUtilities.createBasicAlert(
                Alert.AlertType.WARNING, "Warning", 
                "Score Tied", 
                "Unable to end game while the score is tied").showAndWait();
            return; 
        }
        if(player1Score < scoreToWin && player2Score < scoreToWin)
        {
            Optional<ButtonType> result = MainControllerUtilities.createDecisionAlert(
                 Alert.AlertType.WARNING, "Warning", 
                "Winner not above the score to win", 
                "Are you sure you want to end the game?").showAndWait();
            if (!result.isPresent() || result.get().getButtonData() == ButtonBar.ButtonData.CANCEL_CLOSE)
                return;
        }

        // Decrements the counter
        numGamesRemaining--;
        numGamesRemainingLabel.setText(Integer.toString(numGamesRemaining));

        // Updates the player stats
        playerA.updateStats(player1Score, player2Score);
        playerB.updateStats(player2Score, player1Score);
        players.sort(null);
        playersTableView.getItems().setAll(players);
        playersTableView.refresh();

        schedule.markGamePlayed(currentGameIndex.get());
        scheduleListView.refresh();

        // Pulls next game
        currentGameIndex.set(currentGameIndex.get() + 1);
        currentGame = schedule.getGame(currentGameIndex.get());
        if(currentGame == null)
        {
            clearScoreboard();
            return;
        }
        loadCurrentGame();
    }

    private void loadCurrentGame()
    {
        playerA = currentGame.getPlayerA();
        playerB = currentGame.getPlayerB();
        player1ScoreLabel.setText(playerA.getName());
        player2ScoreLabel.setText(playerB.getName());
        Background b1 = new Background(new BackgroundFill(playerA.getColour(), CornerRadii.EMPTY, Insets.EMPTY));
        Background b2 = new Background(new BackgroundFill(playerB.getColour(), CornerRadii.EMPTY, Insets.EMPTY));
        player1ScoreLabel.setBackground(b1);
        player2ScoreLabel.setBackground(b2);
        player1Spinner.getValueFactory().setValue(0);
        player2Spinner.getValueFactory().setValue(0);
    }

    private void clearScoreboard()
    {
        player1ScoreLabel.setText("");
        player2ScoreLabel.setText("");
        Background b = new Background(new BackgroundFill(Color.web("#243847"), CornerRadii.EMPTY, Insets.EMPTY));
        player1ScoreLabel.setBackground(b);
        player2ScoreLabel.setBackground(b);
        player1Spinner.getValueFactory().setValue(0);
        player2Spinner.getValueFactory().setValue(0);  
        endGameButton.setDisable(true);
        player1Spinner.setDisable(true);
        player2Spinner.setDisable(true);   
        scoreToWinTextField.setDisable(true);
    }


    // ===========================================================================================================================================
    // UI OUTPUT
    // ===========================================================================================================================================
    

    /**
     * This function displays a single round to the screen 
     */
    private void displayRound()
    {
        int roundIndex = roundsPagination.getCurrentPageIndex();
        ArrayList<Game> gamesInRound = schedule.getGamesInRound(roundIndex);
        scheduleListView.getItems().setAll(gamesInRound);
    }

      
}
