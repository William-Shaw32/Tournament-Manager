module com.example.tournament_master 
{
   requires javafx.controls;
   requires javafx.fxml;
   requires javafx.graphics;
   opens app to javafx.fxml;
   opens controllers to javafx.fxml;
}
