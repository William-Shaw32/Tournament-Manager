package Utilities;

import data_classes.Game;
import javafx.application.Platform;
import javafx.css.PseudoClass;
import javafx.scene.Node;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.control.*;

/**
 * This class contains static utility functions to support the Main Controller class
 * @author William Shaw
 */
public class MainControllerUtilities 
{
    // Pseudo css class for when the schedule list-view is put into edit mode
    private static final PseudoClass EDIT_MODE = PseudoClass.getPseudoClass("edit-mode");

    /**
     * This function configures the behaviour of the games each spinner
     * @param numPlayers       The number of players in the tournament
     * @param gamesEachSpinner The games each spinner UI element
     * @param root             The root stackpane UI element
     */
    public static void configureGamesEachSpinner(int numPlayers, Spinner<Integer> gamesEachSpinner, StackPane root, Button generateScheduleButton)
    {
        Integer defaultValue = numPlayers - 1;

        SpinnerValueFactory.IntegerSpinnerValueFactory vf =
                new SpinnerValueFactory.IntegerSpinnerValueFactory(
                        1, Integer.MAX_VALUE, defaultValue
                );

        gamesEachSpinner.setValueFactory(vf);
        gamesEachSpinner.setEditable(true);
        
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
     */
    private static void commitSpinner(Spinner<Integer> gamesEachSpinner, Button generateScheduleButton)
    {
        String text = gamesEachSpinner.getEditor().getText();
        if (text == null || text.isEmpty()) 
            gamesEachSpinner.getValueFactory().setValue(1);
        else 
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
     * This function configures the dynamic behaviour of the schedule list view as well as its off-click behaviour
     * @param scheduleListView The schedule list view
     * @param root The root node of the main-view
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
}       );
    }

    /**
    * Checks whether a given node is the specified container or a descendant of it in the scene graph
    * @param node The node to test
    * @param container The node to test containment against
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
    * Checks whether the given node is part of the hierarchy of a JavaFX control
    * @param node The node to test
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
     * @param scheduleListView
     * @param availableHeight
     */
    public static void resizeListViewToContent(ListView<Game> scheduleListView, double availableHeight) 
    {
        int itemCount = scheduleListView.getItems().size();
        double buffer = 4;
        double contentHeight = scheduleListView.getFixedCellSize() * itemCount + buffer;
        double cappedHeight = Math.min(contentHeight, availableHeight);
        scheduleListView.setPrefHeight(cappedHeight);
    } 
}
