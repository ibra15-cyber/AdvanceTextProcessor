module com.ibra.advancedtextprocessor {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires junit;

    opens com.ibra.advancedtextprocessor to javafx.fxml;
    exports com.ibra.advancedtextprocessor;
    exports com.ibra.advancedtextprocessor.backend.test to junit;

}