package com.programming.todolist;

import com.programming.todolist.datamodel.TodoData;
import com.programming.todolist.datamodel.TodoItem;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.util.Callback;

import java.io.IOException;
import java.time.LocalDate;
import java.time.Month;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Predicate;

public class Controller {
    private List<TodoItem> todoItems;

    @FXML
    private ListView<TodoItem> todoListView;

    @FXML
    private TextArea itemDetailsTextArea;

    @FXML
    private Label deadlineLabel;

    @FXML
    private BorderPane mainBorderPane;

    @FXML
    private ContextMenu listContextMenu;

    @FXML
    private ToggleButton filterToggleButton;

    private FilteredList<TodoItem> filteredList;


    public void initialize(){
        /*
        TodoItem t1 = new TodoItem("Mail Birthday card","Buy a 30th Birthday card for John",
                LocalDate.of(2016, Month.APRIL,25));
        TodoItem t2 = new TodoItem("Finish Java Course","Finish all section in java course",
                LocalDate.of(2016, Month.MAY,23));
        TodoItem t3 = new TodoItem("Lorem impsu 1","Lorem impsu 1 Lorem impsu Lorem impsu",
                LocalDate.of(2016, Month.JANUARY,22));
        TodoItem t4 = new TodoItem("Lorem impsu 2","Lorem impsu 2 Lorem impsu Lorem impsu",
                LocalDate.of(2016, Month.MARCH,23));
        TodoItem t5 = new TodoItem("Lorem impsu 3","Lorem impsu 3 Lorem impsu Lorem impsu",
                LocalDate.of(2016, Month.JUNE,20));

        todoItems = new ArrayList<TodoItem>();
        todoItems.add(t1);
        todoItems.add(t2);
        todoItems.add(t3);
        todoItems.add(t4);
        todoItems.add(t5);

        TodoData.getInstance().setTodoItems(todoItems);
         */

        //menu for delete item
        listContextMenu = new ContextMenu();
        MenuItem deleteMenuItem = new MenuItem("Delete");
        deleteMenuItem.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                TodoItem item = todoListView.getSelectionModel().getSelectedItem();
                deleteItem(item);
            }
        });

        listContextMenu.getItems().addAll(deleteMenuItem);

        todoListView.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<TodoItem>() {
            @Override
            public void changed(ObservableValue<? extends TodoItem> observable, TodoItem oldValue, TodoItem newValue) {
                if(newValue != null) {
                    TodoItem item = (TodoItem) todoListView.getSelectionModel().getSelectedItem();
                    itemDetailsTextArea.setText(item.getDetails());
                    DateTimeFormatter df = DateTimeFormatter.ofPattern("MMMM d, yyyy"); // "d M yy");
                    deadlineLabel.setText(df.format(item.getDeadline()));
                }
            }
        });

        //todoListView.getItems().setAll(TodoData.getInstance().getTodoItems());
        //todoListView.setItems(TodoData.getInstance().getTodoItems());

        //filter list
        filteredList = new FilteredList<TodoItem>(TodoData.getInstance().getTodoItems(),
                new Predicate<TodoItem>() {
                    @Override
                    public boolean test(TodoItem todoItem) {
                        return true;
                    }
        });

        //sorted item
        SortedList<TodoItem> sortedList = new SortedList<TodoItem>(filteredList,
                new Comparator<TodoItem>() {
                    @Override
                    public int compare(TodoItem o1, TodoItem o2) {
                        return o1.getDeadline().compareTo(o2.getDeadline());
                    }
        });



        todoListView.setItems(sortedList);
        todoListView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        todoListView.getSelectionModel().selectFirst();

        todoListView.setCellFactory(new Callback<ListView<TodoItem>, ListCell<TodoItem>>() {
            @Override
            public ListCell<TodoItem> call(ListView<TodoItem> param) {
                ListCell<TodoItem> cell = new ListCell<TodoItem>() {

                    @Override
                    protected void updateItem(TodoItem item, boolean empty) {
                        super.updateItem(item, empty);
                        if(empty) {
                            setText(null);
                        } else {
                            setText(item.getShortDescription());
                            if(item.getDeadline().isBefore(LocalDate.now().plusDays(1))) {
                                setTextFill(Color.RED);
                            } else if(item.getDeadline().equals(LocalDate.now().plusDays(1))) {
                                setTextFill(Color.BROWN);
                            }
                        }
                    }
                };

                cell.emptyProperty().addListener(
                        (obs,wasEmpty,isNowEmpty)->{
                            if(isNowEmpty){
                                cell.setContextMenu(null);
                            }else{
                                cell.setContextMenu(listContextMenu);
                            }
                        }
                );


                return cell;
            }
        });

    }

    @FXML
    public void handleClickListView(){
        TodoItem item = (TodoItem) todoListView.getSelectionModel().getSelectedItem();
        itemDetailsTextArea.setText(item.getDetails());
        DateTimeFormatter df = DateTimeFormatter.ofPattern("MMMM d, yyyy");
        deadlineLabel.setText(df.format( item.getDeadline()) );
        /*
        StringBuilder sb = new StringBuilder(item.getDetails());
        sb.append("\n\n\n\n");
        sb.append("Due: ");
        sb.append(item.getDeadline().toString());

        itemDetailsTextArea.setText(sb.toString());
        */

    }

    @FXML
    public void showNewItemDialog(){
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.initOwner(mainBorderPane.getScene().getWindow());
        dialog.setTitle("Add New Todo Item");
        dialog.setHeaderText("Use this dialog to create a new todo item");

        FXMLLoader fxmlLoader = new FXMLLoader();
        fxmlLoader.setLocation(getClass().getResource("todoItemDialog.fxml"));

        try{

            dialog.getDialogPane().setContent(fxmlLoader.load());


        }catch(IOException e){
            System.out.println("Couldn't load the dialog");
            e.printStackTrace();
            return;
        }

        dialog.getDialogPane().getButtonTypes().add(ButtonType.OK);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CANCEL);
        Optional<ButtonType> result = dialog.showAndWait();
        if(result.isPresent() && result.get() == ButtonType.OK){

            DialogController controller = fxmlLoader.getController();
            TodoItem newItem = controller.processResults();
            todoListView.getSelectionModel().select(newItem);
            System.out.println("OK pressed");

        }
    }


    @FXML
    public void deleteItem(TodoItem item){
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete Todo Item");
        alert.setHeaderText("Delete Item: "+item.getShortDescription());
        alert.setContentText("Are you sure? Press OK to confirm, or cancel to Back out.");

        Optional<ButtonType> result = alert.showAndWait();

        if(result.isPresent() && (result.get() == ButtonType.OK)){
            TodoData.getInstance().deleteTodoItem(item);
        }
    }

    //key click <- for delete
    @FXML
    public void handleKeyPressed(KeyEvent keyEvent){
        TodoItem selectedItem = todoListView.getSelectionModel().getSelectedItem();
        if(selectedItem != null){
            if (keyEvent.getCode().equals(KeyCode.BACK_SPACE)){
                deleteItem(selectedItem);
            }
        }
    }


    @FXML
    public void handleFilterButton(){
        if(filterToggleButton.isSelected()){
            filteredList.setPredicate(new Predicate<TodoItem>() {
                @Override
                public boolean test(TodoItem todoItem) {
                    return (todoItem.getDeadline().equals(LocalDate.now()));
                }
            });
        }else{
            filteredList.setPredicate(new Predicate<TodoItem>() {
                @Override
                public boolean test(TodoItem todoItem) {
                    return true;
                }
            });
        }
    }

}