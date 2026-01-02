package utilities;

import data_classes.Game;
import data_classes.Player;

import javafx.application.Platform;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.css.PseudoClass;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.util.converter.DefaultStringConverter;
import javafx.scene.control.TextFormatter;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.control.TextField;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.Button;
import javafx.scene.control.Alert;
import javafx.scene.control.DialogPane;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ListView;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.Pagination;
import javafx.scene.control.TableView;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableColumn;
import javafx.event.EventHandler;

/**
 * This class contains static utility functions to support the Main Controller class
 * @author William Shaw
 */
public class MainControllerUtilities 
{
    // Pseudo css class for when the schedule list-view is put into edit mode
    private static final PseudoClass EDIT_MODE = PseudoClass.getPseudoClass("edit-mode");

    /**
     * This function configures the score to win text field
     * In particular it configures the text field to only accept decimal input
     * @param scoreToWinTextField The score to win text field
     */
    public static void configureScoreToWinTextField(TextField scoreToWinTextField)
    {
        TextFormatter<String> formatter = new TextFormatter<>(change ->
            change.getControlNewText().matches("\\d*") ? change : null
        );
        scoreToWinTextField.setTextFormatter(formatter);
    }

    /**
     * This function configures the player spinners
     * In particular they are configured as integer spinners which cannot go below 0 and start at 0
     * @param spinner1 The spinner for playerA
     * @param spinner2 The spinner for playerB
     */
    public static void configurePlayerSpinners(Spinner<Integer> spinner1, Spinner<Integer> spinner2)
    {
        SpinnerValueFactory.IntegerSpinnerValueFactory vf1 =
                new SpinnerValueFactory.IntegerSpinnerValueFactory(
                        0, Integer.MAX_VALUE, 0
                );
        SpinnerValueFactory.IntegerSpinnerValueFactory vf2 =
                new SpinnerValueFactory.IntegerSpinnerValueFactory(
                        0, Integer.MAX_VALUE, 0
                );       
        spinner1.setValueFactory(vf1);
        spinner2.setValueFactory(vf2);
    }

    /**
     * This function configures the behaviour of the games each spinner
     * @param gamesEachSpinner       The games each spinner UI element
     * @param root                   The root stackpane UI element
     * @param generateScheduleButton The generate schedule button
     */
    public static void configureGamesEachSpinner(Spinner<Integer> gamesEachSpinner, StackPane root, Button generateScheduleButton)
    {
        SpinnerValueFactory.IntegerSpinnerValueFactory vf =
                new SpinnerValueFactory.IntegerSpinnerValueFactory(
                        1, Integer.MAX_VALUE, 0
                );

        gamesEachSpinner.setValueFactory(vf);
        gamesEachSpinner.setEditable(true);
        gamesEachSpinner.getEditor().clear();
        gamesEachSpinner.setDisable(true);
        generateScheduleButton.setDisable(true);
        
        TextFormatter<String> formatter = new TextFormatter<>(change ->
                change.getControlNewText().matches("\\d*") ? change : null
        );
        gamesEachSpinner.getEditor().setTextFormatter(formatter);
        
        gamesEachSpinner.getEditor().textProperty().addListener((obs, oldText, newText) -> {
            generateScheduleButton.setText("Generate");
            if(newText == null || newText.isEmpty())
                generateScheduleButton.setDisable(true);
            else
                generateScheduleButton.setDisable(false);
        });

        gamesEachSpinner.getEditor().setOnAction(e -> {
            commitSpinner(gamesEachSpinner, generateScheduleButton);
            root.requestFocus();
        });

        gamesEachSpinner.getEditor().focusedProperty().addListener((obs, was, is) -> {
            if (!is) {
                commitSpinner(gamesEachSpinner, generateScheduleButton);
            }
        });

        root.setFocusTraversable(true);
        root.addEventFilter(MouseEvent.MOUSE_PRESSED, e -> 
        {
            Node n = (Node) e.getTarget();
            while (n != null) 
            {
                if (n == gamesEachSpinner)
                    return;
            n = n.getParent();
            }
        root.requestFocus();
        });
    }
    
    /**
     * Helper function to commit the current value to the spinner
     * @param gamesEachSpinner The games each spinner UI element
     * @param generateScheduleButton The generate schedule button
     */
    private static void commitSpinner(Spinner<Integer> gamesEachSpinner, Button generateScheduleButton)
    {
        gamesEachSpinner.increment(0);
        gamesEachSpinner.getEditor().setText(
                String.valueOf(gamesEachSpinner.getValue()));
    }

    /**
     * This function creates a custom alert and applies the css styling for that alert
     * @param type The alert type
     * @param title The alert title
     * @param header The alert header text
     * @param content The alert content text
     * @return A new alert
     */
    public static Alert createBasicAlert(Alert.AlertType type, String title, String header, String content) 
    {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        DialogPane pane = alert.getDialogPane();
        pane.getStylesheets().add(MainControllerUtilities.class.getResource("/css/main-view.css").toExternalForm());
        return alert;
    }

    /**
     * This function creates a custom decision alert and applies css styling to that alert
     * @param type The alert type
     * @param title The alert title
     * @param header The alert header text
     * @param content The alert content text
     * @return A new alert
     */
    public static Alert createDecisionAlert(Alert.AlertType type, String title, String header, String content)
    {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        ButtonType yesButton = new ButtonType("Yes", ButtonBar.ButtonData.YES);
        ButtonType noButton  = new ButtonType("No", ButtonBar.ButtonData.CANCEL_CLOSE);
        alert.getButtonTypes().setAll(yesButton, noButton);
        DialogPane pane = alert.getDialogPane();
        pane.getStylesheets().add(MainControllerUtilities.class.getResource("/css/main-view.css").toExternalForm());
        return alert;
    }

    /**
     * This function creates a custom clear or keep alert and applies css styling to that alert
     * @param type The alert type
     * @param title The alert title
     * @param header The alert header text
     * @param content The alert content text
     * @return A new alert
     */
    public static Alert createClearKeepAlert(Alert.AlertType type, String title, String header, String content)
    {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        ButtonType clearButton = new ButtonType("Clear", ButtonBar.ButtonData.YES);
        ButtonType keepButton  = new ButtonType("Keep", ButtonBar.ButtonData.NO);
        ButtonType cancelButton = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
        alert.getButtonTypes().setAll(clearButton, keepButton, cancelButton);
        DialogPane pane = alert.getDialogPane();
        pane.getStylesheets().add(MainControllerUtilities.class.getResource("/css/main-view.css").toExternalForm());
        return alert;
    }

    /**
     * This function configures the dynamic behaviour of the schedule list view as well as its off-click behaviour
     * @param scheduleListView The schedule list view
     * @param root The root node of the main-view
     * @param editScheduleToggle The toggle button to make the schedule editable
     */
    public static void configureScheduleListView(ListView<Game> scheduleListView, StackPane root, ToggleButton editScheduleToggle)
    {
        scheduleListView.setFixedCellSize(36);
        scheduleListView.setMinHeight(0);
        scheduleListView.setMaxHeight(Region.USE_PREF_SIZE);
        VBox.setVgrow(scheduleListView, Priority.NEVER);
        root.sceneProperty().addListener((obs, oldScene, scene) -> {
            if (scene == null) return;
            root.setFocusTraversable(true);
            root.setPickOnBounds(true);
            scene.addEventFilter(MouseEvent.MOUSE_PRESSED, e -> {
                Node target = (Node) e.getTarget();
                if (isInside(target, scheduleListView)) return;
                if (isInsideAControl(target)) return;
                Platform.runLater(() -> {
                    scheduleListView.getSelectionModel().clearSelection();
                    root.requestFocus();
                });
            });
        });
        editScheduleToggle.selectedProperty().addListener((obs, was, is) -> 
        {
            scheduleListView.pseudoClassStateChanged(EDIT_MODE, is);
        });
    }

    /**
    * This function checks whether a given node is the specified container or a descendant of it in the scene graph
    * @param node The node to test
    * @param container The node to test containment against
    * @return True if the node is inside the container, false otherwise
    */
    private static boolean isInside(Node node, Node container) 
    {
        while (node != null) 
        {
            if (node == container) return true;
            node = node.getParent();
        }
        return false;
    }

    /**
    * This function checks whether the given node is part of the hierarchy of a JavaFX control
    * @param node The node to test
    * @return True if the node is part of the hierarchy of a javafx controller, false otherwise
    */
    private static boolean isInsideAControl(Node node) 
    {
        while (node != null) 
        {
            if (node instanceof javafx.scene.control.Control) return true;
            node = node.getParent();
        }
        return false;
    }

    /**
     * This function is a wrapper function to resize the schedule list view when a new shedule is made or the window size is changed
     * @param scheduleListView The schedule list view
     * @param rightVBox The right vertical box
     * @param rightTopVBox The top vertical box in the right vertical box
     * @param roundsPagination The rounds pagination
     * @param paginationVSpacer The spacer between the list view and the rounds pagination
     */
    public static void resizeSchedule(ListView<Game> scheduleListView, VBox rightVBox, VBox rightTopVBox, Pagination roundsPagination, Region paginationVSpacer)
    {
        if(scheduleListView.getItems().isEmpty())
            return;
        double availableHeight =
        rightVBox.getHeight()
        - rightTopVBox.getHeight()
        - paginationVSpacer.getHeight()
        - roundsPagination.getHeight()
        - rightVBox.getSpacing() * 2
        - 4;
        resizeListViewToContent(scheduleListView, availableHeight);
    }

    /**
     * This function sets the height of the schedule list view based on the content in the schedule list view
     * @param scheduleListView The schedule list view
     * @param availableHeight The available height to expand into
     */
    public static void resizeListViewToContent(ListView<Game> scheduleListView, double availableHeight) 
    {
        int itemCount = scheduleListView.getItems().size();
        double buffer = 4;
        double contentHeight = scheduleListView.getFixedCellSize() * itemCount + buffer;
        double cappedHeight = Math.min(contentHeight, availableHeight); 
        scheduleListView.setPrefHeight(cappedHeight);
    } 

    /**
     * This function is a wrapper function to resize the players table when a new shedule is made or the window size is changed
     * @param playersTableView The players table-view
     * @param leftVBox The left vertical box
     * @param leftTopVBox The top vertical box in the left vertical box
     */
    public static void resizePlayersTable(TableView<Player> playersTableView, VBox leftVBox, VBox leftTopVBox)
    {
        if (playersTableView.getItems().isEmpty())
            return;

        double availableHeight =
                leftVBox.getHeight()
                - leftTopVBox.getHeight()
                - leftVBox.getSpacing()          
                - 4;

        resizePlayersTableToContent(playersTableView, availableHeight);
    }

    /**
     * This function sets the height of the players table based on the content in the players table
     * @param playersTableView The players table-view
     * @param availableHeight The available height to expand into
     */
    public static void resizePlayersTableToContent(TableView<Player> playersTableView, double availableHeight)
    {
        int itemCount = playersTableView.getItems().size();
        double rowHeight = playersTableView.getFixedCellSize();
        if (rowHeight <= 0)
            return;
        double buffer = 4;
        double headerHeight = getPlayersTableHeaderHeight(playersTableView);
        double contentHeight = headerHeight + rowHeight * itemCount + buffer;
        double cappedHeight = Math.min(contentHeight, availableHeight);
        playersTableView.setPrefHeight(cappedHeight);
    }

    /**
     * Returns the height of the header area of the given table-view
     * @param playersTableView The players table-view
     * @return The height of the table's column header, or 0 if the header node cannot be found
     */
    private static double getPlayersTableHeaderHeight(TableView<Player> playersTableView)
    {
        Node header = playersTableView.lookup(".column-header-background");
        if (header == null)
            return 0;
        return header.getBoundsInLocal().getHeight();
    }

    /**
     * Configures the players table
     * Configures the dynamic colouring as well as the click-away commits feature
     * @param playersTableView The players table-view
     * @param removePlayerButton The remove player button
     */
    public static void configurePlayersTable(TableView<Player> playersTableView, Button removePlayerButton)
    {
        playersTableView.setRowFactory(tv -> new TableRow<Player>() {
            /**
             * Updates the visual state of the table row when its associated item or empty state changes
             * @param player The player associated with this row
             * @param empty True if the row is empty, false if it is not empty
             */
            @Override
            protected void updateItem(Player player, boolean empty)
            {
                super.updateItem(player, empty);
                if (empty || player == null)
                {
                    setBackground(null);
                }
                else
                {
                    setBackground(new Background(new BackgroundFill(
                        player.getColour(), CornerRadii.EMPTY, Insets.EMPTY
                    )));
                }
            }
        });
        final EventHandler<MouseEvent> clearOnOutsideClick = e -> {
            Object target = e.getTarget();
            if (!(target instanceof Node node))
            {
                playersTableView.getSelectionModel().clearSelection();
                return;
            }

            // Skip deselect if clicking the Remove button
            if (removePlayerButton != null && isInNode(node, removePlayerButton))
            {
                return;
            }

            // Skip deselect if clicking inside the table
            if (isInNode(node, playersTableView))
            {
                return;
            }

            // Otherwise: clicked outside
            playersTableView.getSelectionModel().clearSelection();
        };

        playersTableView.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (oldScene != null)
            {
                oldScene.removeEventFilter(MouseEvent.MOUSE_PRESSED, clearOnOutsideClick);
            }
            if (newScene != null)
            {
                newScene.addEventFilter(MouseEvent.MOUSE_PRESSED, clearOnOutsideClick);
            }
        });

        if (playersTableView.getScene() != null)
        {
            playersTableView.getScene().addEventFilter(MouseEvent.MOUSE_PRESSED, clearOnOutsideClick);
        }
    }

    /**
     * Configures the name column
     * Configures editing of a player's name
     * @param nameColumn The name column in the players table
     */
    public static void configureNameColumn(TableColumn<Player, String> nameColumn) 
    {
        nameColumn.setCellFactory(col ->
            new TextFieldTableCell<Player, String>(new DefaultStringConverter())
            {
                private TextField textField;
                private final javafx.beans.value.ChangeListener<Boolean> focusListener =
                    (obs, had, has) -> {
                        if (!has && isEditing() && textField != null) {
                            commitEdit(textField.getText());
                        }
                    };
                /**
                 * Initializes editing for the cell and attaches a focus listener to the underlying text field when editing begins
                 */
                @Override
                public void startEdit()
                {
                    super.startEdit();

                    if (getGraphic() instanceof TextField tf)
                    {
                        textField = tf;
                        textField.focusedProperty().removeListener(focusListener);
                        textField.focusedProperty().addListener(focusListener);
                    }
                }
                /**
                 * Cancels editing for the cell and removes the focus listener from the text field
                 */
                @Override
                public void cancelEdit()
                {
                    super.cancelEdit();
                    if (textField != null) 
                    {
                        textField.focusedProperty().removeListener(focusListener);
                    }
                }
            }
        );
    }

    /**
     * Determines whether the given target node is contained within the specified container node
     * @param target The node to test for containment
     * @param container The node to test against as a potential ancestor
     * @return True if the container is an ancestor of the target (or the same node), false otherwise
     */
    private static boolean isInNode(Node target, Node container)
    {
        for (Node n = target; n != null; n = n.getParent())
        {
            if (n == container) return true;
        }
        return false;
    }

    /**
     * Configures all table columns
     * Particularily the refresh feature of all the columns
     * @param nameColumn The name column
     * @param winsColumn The wins column
     * @param playedColumn The played column
     * @param ratioColumn The ratio column
     */
    public static void configureAllTableColumns(
        TableColumn<Player, String> nameColumn,
        TableColumn<Player, Integer> winsColumn,
        TableColumn<Player, Integer> playedColumn,
        TableColumn<Player, Double> ratioColumn)
    {
        nameColumn.setCellValueFactory(cellData ->
            new ReadOnlyObjectWrapper<>(cellData.getValue().getName())
        );   
        winsColumn.setCellValueFactory(cellData ->
            new ReadOnlyObjectWrapper<>(cellData.getValue().getWins())
        ); 
        playedColumn.setCellValueFactory(cellData ->
            new ReadOnlyObjectWrapper<>(cellData.getValue().getGamesPlayed())
        ); 
        ratioColumn.setCellValueFactory(cellData ->
            new ReadOnlyObjectWrapper<>(cellData.getValue().getRatio())
        ); 
    }
}
