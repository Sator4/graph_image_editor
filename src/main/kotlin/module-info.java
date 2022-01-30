module com.editor.imageeditor {
    requires javafx.controls;
    requires javafx.fxml;
    requires kotlin.stdlib;
    requires opencv;
    requires java.desktop;
    requires javafx.swing;


    opens com.editor.imageeditor to javafx.fxml;
    exports com.editor.imageeditor;
}