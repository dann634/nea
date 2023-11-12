module com.jackson.nea {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.media;


    opens com.jackson.main to javafx.fxml;
    exports com.jackson.main;
}