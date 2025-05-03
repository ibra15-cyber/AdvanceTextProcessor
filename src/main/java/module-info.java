module com.ibra.advancedtextprocessor {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;

    opens com.ibra.advancedtextprocessor to javafx.fxml;
    exports com.ibra.advancedtextprocessor;
}