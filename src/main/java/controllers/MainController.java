package controllers;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
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
    private Player playerA;
    private Player playerB;

    // Primatives
    private AtomicBoolean dragDropEnabled = new AtomicBoolean(false);
    private BooleanProperty tournamentIsActive = new SimpleBooleanProperty(false);
    private int numGamesRemaining = 0;
    private int numColoursGenerated;

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
        scoreToWinTextField.setDisable(true);
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
        DragDropUtilities.configureDragDrop(scheduleListView, game -> new Label(game.toString()), this::reorderGame, dragDropEnabled);
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
                Alert.AlertType.ERROR, "Error", 
                "Cannot generate schedule", 
                "If the number of players is odd, then each player must play an even number of games").showAndWait();
            return;
        }
        generateScheduleButton.setText("Regenerate");
        roundsPagination.setCurrentPageIndex(0);
        ScheduleBuilder sb = new ScheduleBuilder(players, numGamesEach);
        schedule = sb.build(); 
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
    private void reorderGame(int dragIndex, int dropIndex)
    {
        int baseIndex = roundsPagination.getCurrentPageIndex() * schedule.getNumGamesInFullRound();
        int oldIndex = baseIndex + dragIndex;
        int newIndex = baseIndex + dropIndex;
        schedule.changeGameIndex(oldIndex, newIndex);
        displayRound();
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
        scoreToWinTextField.setDisable(false);

        // Pull first game
        Game nextGame = schedule.getNextGame();
        playerA = nextGame.getPlayerA();
        playerB = nextGame.getPlayerB();
        player1ScoreLabel.setText(playerA.getName());
        player2ScoreLabel.setText(playerB.getName());
        Background b1 = new Background(new BackgroundFill(playerA.getColour(), CornerRadii.EMPTY, Insets.EMPTY));
        Background b2 = new Background(new BackgroundFill(playerB.getColour(), CornerRadii.EMPTY, Insets.EMPTY));
        player1ScoreLabel.setBackground(b1);
        player2ScoreLabel.setBackground(b2);
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
        tournamentIsActive.set(false);
        startEndTournamentButton.setText("Start Tournament");
        schedule.clear();
        scheduleListView.getItems().clear();
        numGamesRemaining = 0;
        numGamesRemainingLabel.setText("0");
        generateScheduleButton.setText("Generate");
        scheduleConfigHBox.setDisable(false);
        addPlayerButton.setDisable(false);
        startEndTournamentButton.setDisable(true);
        roundsPagination.setPageCount(Pagination.INDETERMINATE);
        endGameButton.setDisable(true);
        scoreToWinTextField.setDisable(true);
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
        // Updating Stats
        int player1Score = player1Spinner.getValue();
        int player2Score = player2Spinner.getValue();

        if(player1Score == player2Score)
            return;
        
        if(player1Score > player2Score)
        {
            playerA.updateStats(player1Score, player2Score, true);
            playerB.updateStats(player2Score, player1Score, false);
        }
        else
        {
           playerA.updateStats(player1Score, player2Score, false);
           playerB.updateStats(player2Score, player1Score, true);
        }
        playersTableView.refresh();

        // Pull next game
        Game nextGame = schedule.getNextGame();
        if(nextGame == null)
        {
            player1ScoreLabel.setText("");
            player2ScoreLabel.setText("");
            Background b = new Background(new BackgroundFill(Color.web("#243847"), CornerRadii.EMPTY, Insets.EMPTY));
            player1ScoreLabel.setBackground(b);
            player2ScoreLabel.setBackground(b);
            player1Spinner.getValueFactory().setValue(0);
            player2Spinner.getValueFactory().setValue(0);
            return;
        }
        playerA = nextGame.getPlayerA();
        playerB = nextGame.getPlayerB();
        player1ScoreLabel.setText(playerA.getName());
        player2ScoreLabel.setText(playerB.getName());
        Background b1 = new Background(new BackgroundFill(playerA.getColour(), CornerRadii.EMPTY, Insets.EMPTY));
        Background b2 = new Background(new BackgroundFill(playerB.getColour(), CornerRadii.EMPTY, Insets.EMPTY));
        player1ScoreLabel.setBackground(b1);
        player2ScoreLabel.setBackground(b2);
        player1Spinner.getValueFactory().setValue(0);
        player2Spinner.getValueFactory().setValue(0);
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
