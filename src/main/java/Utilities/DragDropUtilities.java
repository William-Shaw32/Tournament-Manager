package utilities;

import java.util.concurrent.atomic.AtomicReference;

import data_classes.Game;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.ListCell;
import javafx.scene.image.Image;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DataFormat;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseButton;
import javafx.scene.input.TransferMode;
import javafx.scene.control.ListView;
import javafx.css.PseudoClass;
import javafx.scene.Node;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import javafx.scene.control.Spinner;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.Pagination;

/**
 * This class handles the drag and drop functionality for the edit schedule list-view feature
 * @author William Shaw
 */
public class DragDropUtilities 
{
    private static final DataFormat DRAG_INDEX = new DataFormat("drag-index");               // The initial index of the list-view cell being dragged
    private static final PseudoClass DROP_ABOVE = PseudoClass.getPseudoClass("drop-above");  // CSS pseudo class for a list-view cell when droping above
    private static final PseudoClass DROP_BELOW = PseudoClass.getPseudoClass("drop-below");  // CSS psuedo class for a list-view cell when dropping below

    /**
     * Functional interface for building a draggable cell node used to reorder
     */
    @FunctionalInterface
    public interface CellRenderer<T>
    {
        /**
         * Builds a UI node representing the given item for use in drag-and-drop reordering.
         * @param item The item to render
         * @return A node representing the item
         */
        Node build(T item);
    }  

    /**
     * Functional interface for reordering a game in the schedule list-view
     */
    @FunctionalInterface
    public interface ReorderGameHandler
    {
        /**
         * Reorders a game in the schedule list-view
         * @param oldIndex The list-view index of the cell that was dragged
         * @param newIndex The list-view index of where the cell was dropped
         */
        void reorderGame(int dragIndex, int dropIndex);
    }

    /**
     * This function configures the drag and drop functionality of the edit schedule list-view feature
     * @param <T>                 The type of the list-view being set up for reordering (Game)
     * @param listView            The list-view being set up for reordering
     * @param renderer            The functional interface for building a draggable cell node used to reorder
     * @param reorderGameHandler  Functional interface for reordering a game in the schedule list-view
     * @param dragDropEnabled     Atomic boolean used to store whether drag and drop is currently enabled
     */
    public static <T> void configureDragDrop(
        ListView<T> listView,
        CellRenderer<T> renderer,
        ReorderGameHandler reorderGameHandler,
        AtomicBoolean dragDropEnabled,
        Spinner<Integer> player1Spinner,
        Spinner<Integer> player2Spinner, 
        Pagination roundsPagination,
        AtomicInteger currentGameIndex,
        AtomicInteger numGamesInFullRound
    )
    {
        // Sets the cell factory on the list view
        listView.setCellFactory(lv ->
        {
            final AtomicReference<ListCell<T>> lastCell = new AtomicReference<>(null);
            Runnable clearIndicator = () -> 
            { 
                ListCell<T> c = lastCell.get();
                if (c != null) {
                    c.pseudoClassStateChanged(DROP_ABOVE, false);
                    c.pseudoClassStateChanged(DROP_BELOW, false);
                    lastCell.set(null);
                }
            };
            listView.setOnDragExited(e -> clearIndicator.run());

            // Instantiates a template cell for the list-view
            ListCell<T> cell = new ListCell<>()
            {
                /**
                 * Updates the visual representation of this cell
                 * @param item The item associated with the cell
                 * @param empty True if the cell is empty, false otherwise
                 */
                @Override
                protected void updateItem(T item, boolean empty)
                {   
                    super.updateItem(item, empty);
                    pseudoClassStateChanged(DROP_ABOVE, false);
                    pseudoClassStateChanged(DROP_BELOW, false);
                    if (empty || item == null)
                    {
                        setText(null);
                        setOpacity(1.0);       
                    }
                    else
                    {
                        setText(item.toString());
                        // Dim played games
                        if (item instanceof Game g)
                            setOpacity(g.getPlayed() ? 0.40 : 1.0);
                        else
                            setOpacity(1.0);
                    }
                }   
            };

            // Event handler for drag detected
            cell.setOnDragDetected(e ->
            {
                if (!dragDropEnabled.get()) return;
                if (cell.isEmpty()) return;
                if (e.getButton() != MouseButton.PRIMARY) return;
                Dragboard db = cell.startDragAndDrop(TransferMode.MOVE);

                 // Blocks dragging for played games
                T item = cell.getItem();
                if (item instanceof Game g && g.getPlayed())
                    return;

                ClipboardContent content = new ClipboardContent();
                content.put(DRAG_INDEX, cell.getIndex());
                db.setContent(content);
                Image ghost = cell.snapshot(new SnapshotParameters(), null);
                db.setDragView(ghost, e.getX(), e.getY());
                e.consume();
            });

            // Event handler for drag over
            cell.setOnDragOver(e -> 
            {
                if (!dragDropEnabled.get()) return;
                Dragboard db = e.getDragboard();
                if (!db.hasContent(DRAG_INDEX)) return;
                if (cell.isEmpty()) return;

                // Blocks using played games as drop targets
                T target = cell.getItem();
                if (target instanceof Game g && g.getPlayed())
                    return;

                e.acceptTransferModes(TransferMode.MOVE);

                boolean above = e.getY() < (cell.getHeight() / 2.0);

                if (lastCell.get() != cell) 
                {
                    clearIndicator.run();
                    lastCell.set(cell);
                }               

                cell.pseudoClassStateChanged(DROP_ABOVE, above);
                cell.pseudoClassStateChanged(DROP_BELOW, !above);

                e.consume();
            });

            // Event handler for drag exited
            cell.setOnDragExited(e -> 
            {
                if (!dragDropEnabled.get()) return;
                if (lastCell.get() == cell) 
                    clearIndicator.run();
                e.consume();
            });

            // Event handler for drag dropped
            cell.setOnDragDropped(e -> 
            {
                if (!dragDropEnabled.get()) return;
                Dragboard db = e.getDragboard();
                if (!db.hasContent(DRAG_INDEX)) return;
                int dragIndex = (Integer) db.getContent(DRAG_INDEX);
                int targetIndex = cell.getIndex();
                boolean insertAbove = e.getY() < (cell.getHeight() / 2.0);
                int dropIndex = 0;
                if(insertAbove == true)
                    dropIndex = targetIndex;
                else
                    dropIndex = targetIndex + 1;

                // Checks if the current game is being reordered
                int currentIndex = currentGameIndex.get();
                int baseIndex = roundsPagination.getCurrentPageIndex() * numGamesInFullRound.get();
                int oldIndex = baseIndex + dragIndex;
                int newIndex = baseIndex + dropIndex;
                if (oldIndex == currentIndex || newIndex == currentIndex)
                {
                    int score1 = (player1Spinner == null || player1Spinner.getValue() == null) ? 0 : player1Spinner.getValue();
                    int score2 = (player2Spinner == null || player2Spinner.getValue() == null) ? 0 : player2Spinner.getValue();

                    if (score1 > 0 || score2 > 0)
                    {
                        Optional<ButtonType> result = MainControllerUtilities.createDecisionAlert(
                            Alert.AlertType.WARNING, "Warning", 
                            "Reorder current game", 
                            "This action will discard the score of the current game").showAndWait();
                        if (!result.isPresent() || result.get().getButtonData() == ButtonBar.ButtonData.CANCEL_CLOSE)
                        {
                            clearIndicator.run();
                            e.setDropCompleted(false);
                            e.consume();
                            return;    
                        }
                    }
                }
    
                reorderGameHandler.reorderGame(oldIndex, newIndex);
                clearIndicator.run();
                e.setDropCompleted(true);
                e.consume();
            });
            
            // Event handler for drag done
            cell.setOnDragDone(e -> 
            {
                if (!dragDropEnabled.get()) return;
                clearIndicator.run();
                e.consume();
            });
            
            return cell;
        });
    }  
}
