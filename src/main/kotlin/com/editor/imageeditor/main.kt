package com.editor.imageeditor

import javafx.application.Application
import javafx.scene.Scene
import javafx.stage.Stage

class ImageEditor : Application() {
    override fun start(stage: Stage) {
        System.load("D:\\ProgramData\\opencv\\build\\java\\x64\\opencv_java455.dll")
        val width = 1200.0
        val height = 600.0

//        val fxmlLoader = FXMLLoader(MainWindow::class.java.getResource("mainWindow.fxml"))
        val fxmlLoader = MainWindow()
        val scene = Scene(fxmlLoader, width, height)
        stage.title = "GraphEditor"
        stage.scene = scene
        stage.show()
    }
}

fun main() {
    Application.launch(ImageEditor::class.java)
}