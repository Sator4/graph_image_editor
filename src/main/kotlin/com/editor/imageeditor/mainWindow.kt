package com.editor.imageeditor

import javafx.event.EventHandler
import java.net.URL
import java.util.ResourceBundle
import javafx.fxml.FXML
import javafx.fxml.FXMLLoader
import javafx.scene.control.Button
import javafx.scene.control.ScrollPane
import javafx.scene.control.SplitPane
import javafx.scene.image.ImageView
import javafx.scene.layout.AnchorPane
import javafx.scene.layout.HBox
import javafx.scene.layout.VBox
import javafx.scene.shape.Line


var connections = arrayListOf<Triple<LinkController, LinkController, Line>>()
var nodes = arrayListOf<DraggableNode>()
var global_output: ImageView? = null

class MainWindow : HBox() {

    @FXML
    private lateinit var resources: ResourceBundle

    @FXML
    private lateinit var location: URL

    @FXML
    private lateinit var buttonBox: VBox

    @FXML
    private lateinit var effectsArea: AnchorPane

    @FXML
    private lateinit var globalBox: HBox

    @FXML
    private lateinit var imageSource: Button

    @FXML
    private lateinit var integerButton: Button

    @FXML
    private lateinit var floatButton: Button

    @FXML
    private lateinit var stringButton: Button

    @FXML
    private lateinit var addStringButton: Button

    @FXML
    private lateinit var greyFilterButton: Button

    @FXML
    private lateinit var brightnessFilterButton: Button

    @FXML
    private lateinit var sepiaFilterButton: Button

    @FXML
    private lateinit var invertFilterButton: Button

    @FXML
    private lateinit var blurFilterButton: Button

    @FXML
    private lateinit var moveTransformButton: Button

    @FXML
    private lateinit var scaleTransformButton: Button

    @FXML
    private lateinit var rotateTransformButton: Button

    @FXML
    private lateinit var addImageButton: Button

    @FXML
    private lateinit var outputArea: ScrollPane

    @FXML
    private lateinit var outputImage: ImageView

    @FXML
    private lateinit var splitPane: SplitPane


    @FXML
    fun initialize() {

        global_output = outputImage
        effectsArea.children.add(OutputNode())
        effectsArea.children.add(ImageSourceNode())

        imageSource.onAction = EventHandler { effectsArea.children.add(ImageSourceNode()) }
        integerButton.onAction = EventHandler { effectsArea.children.add(IntNode()) }
        stringButton.onAction = EventHandler { effectsArea.children.add(StringNode()) }
        floatButton.onAction = EventHandler { effectsArea.children.add(FloatNode()) }
        addStringButton.onAction = EventHandler { effectsArea.children.add(AddStringNode()) }
        greyFilterButton.onAction = EventHandler { effectsArea.children.add(FilterGreyNode()) }
        brightnessFilterButton.onAction = EventHandler { effectsArea.children.add(FilterBrightnessNode()) }
        sepiaFilterButton.onAction = EventHandler { effectsArea.children.add(FilterSepiaNode()) }
        invertFilterButton.onAction = EventHandler { effectsArea.children.add(FilterInvertNode()) }
        blurFilterButton.onAction = EventHandler { effectsArea.children.add(FilterBlurNode()) }
        moveTransformButton.onAction = EventHandler { effectsArea.children.add(TransformMoveNode()) }
        scaleTransformButton.onAction = EventHandler { effectsArea.children.add(TransformScaleNode()) }
        rotateTransformButton.onAction = EventHandler { effectsArea.children.add(TransformRotateNode()) }
        addImageButton.onAction = EventHandler { effectsArea.children.add(AddImageNode()) }






    }

    init {
        val fxmlLoader = FXMLLoader(javaClass.getResource("mainWindow.fxml"))
        fxmlLoader.setRoot(this)
        fxmlLoader.setController(this)
        fxmlLoader.load<Any>()
    }
}