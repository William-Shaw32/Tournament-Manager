package utilities;

import java.util.concurrent.atomic.AtomicReference;
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
import java.util.concurrent.atomic.AtomicBoolean;

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
        AtomicBoolean dragDropEnabled
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
                        setGraphic(null);
                    }
                    else
                    {
                        setGraphic(null);
                        setText(item.toString());
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
                int dragIndex = (Integer) db.getContent(DRAG_INDEX);
                int targetIndex = cell.getIndex();
                boolean insertAbove = e.getY() < (cell.getHeight() / 2.0);
                int dropIndex = 0;
                if(insertAbove == true)
                    dropIndex = targetIndex;
                else
                    dropIndex = targetIndex + 1;
                reorderGameHandler.reorderGame(dragIndex, dropIndex);
                clearIndicator.run();
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
