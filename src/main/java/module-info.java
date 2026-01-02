module com.example.tournament_master 
{
   requires javafx.controls;
   requires javafx.fxml;
   opens app to javafx.fxml;
   opens controllers to javafx.fxml;
   exports app;
}
