package com.editor.imageeditor

import javafx.animation.KeyFrame
import javafx.animation.Timeline
import javafx.embed.swing.SwingFXUtils
import javafx.event.EventHandler
import java.net.URL
import java.util.ResourceBundle
import javafx.fxml.FXML
import javafx.fxml.FXMLLoader
import javafx.scene.control.Button
import javafx.scene.control.ScrollPane
import javafx.scene.control.SplitPane
import javafx.scene.image.Image
import javafx.scene.image.ImageView
import javafx.scene.layout.AnchorPane
import javafx.scene.layout.HBox
import javafx.scene.layout.VBox
import javafx.scene.shape.Line
import javafx.stage.FileChooser
import javafx.stage.Stage
import javafx.util.Duration
import java.io.File
import javax.imageio.ImageIO
import kotlinx.coroutines.*


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
    private lateinit var saveImageButton: Button

    @FXML
    private lateinit var saveSchemeButton: Button

    @FXML
    private lateinit var loadSchemeButton: Button

    @FXML
    private lateinit var outputArea: ScrollPane

    @FXML
    private lateinit var outputImage: ImageView

    @FXML
    private lateinit var splitPane: SplitPane


    @FXML
    fun initialize(){

        global_output = outputImage
        effectsArea.children.add(OutputNode())
        effectsArea.children.add(ImageSourceNode())

        imageSource.onAction = EventHandler { effectsArea.children.add(ImageSourceNode()) }
        saveImageButton.onAction = EventHandler { save_image() }
        saveSchemeButton.onAction = EventHandler { serialize() }
        loadSchemeButton.onAction = EventHandler { deserialize() }
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

    fun save_image(){
        if (global_output!!.image != null){
            val fileChooser = FileChooser()
            val imageFilter = FileChooser.ExtensionFilter("Image Files", "*.png")
            fileChooser.extensionFilters.add(imageFilter)
            val file: File? = fileChooser.showSaveDialog(Stage())
            ImageIO.write(SwingFXUtils.fromFXImage(global_output!!.image, null), "png", file)
        }
    }

    fun serialize (){
        var string = nodes.size.toString() + " " + connections.size.toString()
        for (i in nodes){
            string += "\n"
            string += i.layoutX.toString() + " " + i.layoutY.toString() + " " + i.id.toString() + " " +
                    i.updated_info.int.toString() + " " + i.updated_info.float.toString() + " " +
                    i.updated_info.string + " " + i.updated_info.from + " " + i.path_to_image.toString() + " " + i.javaClass.toString()
        }
        for (i in connections){
            string += "\n"
            string += i.first.id.toString() + " " + i.first.linkParent.id.toString() + " " +
                    i.second.id.toString() + " " + i.second.linkParent.id.toString() + " " + i.first.serial_number.toString() + " " +
                    i.first.lbx.toString() + " " + i.first.lby.toString() + " " +
                    i.first.lex.toString() + " " + i.first.ley.toString()

//            string += ((i.first.localToScene(i.first.boundsInLocal).centerX) - 115).toString() + " " +
//                    ((i.first.localToScene(i.first.boundsInLocal).centerY) - 10).toString() + " " +
//                    i.first.id.toString() + " " + i.first.linkParent.id.toString() + " " + i.first.outputEnd.toString() + " " +
//                    i.first.type + " " + if (i.first.axis.toString() != " ") i.first.axis.toString() else "'" + " " +
//                    i.first.linkEndLabel.text
//            string += "\n"
//            string += ((i.second.localToScene(i.second.boundsInLocal).centerX) - 115).toString() + " " +
//                    ((i.second.localToScene(i.second.boundsInLocal).centerY) - 10).toString() + " " +
//                    i.second.id.toString() + " " + i.second.linkParent.id.toString() + " " + i.second.outputEnd.toString() + " " +
//                    i.second.type + " " + if (i.second.axis.toString() != " ") i.second.axis.toString() else "'" + " " +
//                    i.second.linkEndLabel.text
        }
        val fileChooser = FileChooser()
        val imageFilter = FileChooser.ExtensionFilter("Image Files", "*.txt")
        fileChooser.extensionFilters.add(imageFilter)
        val file: File? = fileChooser.showSaveDialog(Stage())
        file?.writeText(string)
    }

    fun deserialize() {
        val fileChooser = FileChooser()
        val imageFilter = FileChooser.ExtensionFilter("Image Files", "*.txt")
        fileChooser.extensionFilters.add(imageFilter)
        val file: File? = fileChooser.showOpenDialog(Stage())
        if (file != null){
            nodes.clear()
            connections.clear()
            effectsArea.children.clear()
            val reader = file.readText().split("\n")
            val numbers = arrayListOf<Int>()
            var values = arrayListOf<String>()
            numbers.addAll(listOf(reader[0].split(" ")[0].toInt(), reader[0].split(" ")[1].toInt()))
            println(numbers)
            for (i in 1 until numbers[0] + 1){
                values = reader[i].split(" ") as ArrayList<String>
                println(values)
                runBlocking {
                    when (values[9]) {
                        "com.editor.imageeditor.OutputNode" -> effectsArea.children.add(OutputNode())
                        "com.editor.imageeditor.ImageSourceNode" -> effectsArea.children.add(ImageSourceNode())
                        "com.editor.imageeditor.IntNode" -> effectsArea.children.add(IntNode())
                        "com.editor.imageeditor.StringNode" -> effectsArea.children.add(StringNode())
                        "com.editor.imageeditor.FloatNode" -> effectsArea.children.add(FloatNode())
                        "com.editor.imageeditor.AddStringNode" -> effectsArea.children.add(AddStringNode())
                        "com.editor.imageeditor.FilterGreyNode" -> effectsArea.children.add(FilterGreyNode())
                        "com.editor.imageeditor.FilterBrightnessNode" -> effectsArea.children.add(FilterBrightnessNode())
                        "com.editor.imageeditor.FilterSepiaNode" -> effectsArea.children.add(FilterSepiaNode())
                        "com.editor.imageeditor.FilterInvertNode" -> effectsArea.children.add(FilterInvertNode())
                        "com.editor.imageeditor.FilterBlurNode" -> effectsArea.children.add(FilterBlurNode())
                        "com.editor.imageeditor.TransformMoveNode" -> effectsArea.children.add(TransformMoveNode())
                        "com.editor.imageeditor.TransformScaleNode" -> effectsArea.children.add(TransformScaleNode())
                        "com.editor.imageeditor.TransformRotateNode" -> effectsArea.children.add(TransformRotateNode())
                        else -> return@runBlocking
                    }
                }
                nodes.last().layoutX = values[0].toDouble()
                nodes.last().layoutY = values[1].toDouble()
                nodes.last().id = values[2]
                nodes.last().updated_info.int = if (values[3] == "null") null else values[3].toInt()
                nodes.last().updated_info.float = if (values[4] == "null") null else values[4].toFloat()
                nodes.last().updated_info.string = if (values[5] == "null") null else values[4]
                nodes.last().updated_info.from = values[6]
                nodes.last().path_to_image = File(values[7])
                if (nodes.last().javaClass.toString() == "class com.editor.imageeditor.ImageSourceNode"){
                    try {
                        nodes.last().image.image = Image(values[7])
                        nodes.last().updated_info.image = nodes.last().image.image
                    } catch (e: Exception){
                        nodes.last().path_to_image = null
                    }
                }
                values.clear()
            }

            for (i in 0 until numbers[1]){
                values = reader[1 + numbers[0] + i].split(" ") as ArrayList<String>
                println(values)
                var linkBegin: LinkController? = null
                var linkEnd: LinkController? = null
                for (node in nodes){
                    if (node.id == values[3]){
                        (node.rightBox.children[0] as LinkController).linked = true
                        (node.rightBox.children[0] as LinkController).id = values[2]
                        (node.rightBox.children[0] as LinkController).lbx = values[5].toDouble()
                        (node.rightBox.children[0] as LinkController).lby = values[6].toDouble()
                        (node.rightBox.children[0] as LinkController).lex = values[7].toDouble()
                        (node.rightBox.children[0] as LinkController).ley = values[8].toDouble()
                        linkBegin = node.rightBox.children[0] as LinkController
                    }
                }
                for (node in nodes){
                    if (node.id == values[1]){
                        (node.leftBox.children[values[4].toInt()] as LinkController).linked = true
                        (node.leftBox.children[values[4].toInt()] as LinkController).id = values[0]
                        linkEnd = node.leftBox.children[values[4].toInt()] as LinkController
                    }
                }
                if (linkBegin != null && linkEnd != null) {
                    linkBegin.on_drag_dropped(linkBegin, linkEnd, false)
                }
            }
            for (node in nodes){
                if (node.javaClass.toString() == "com.editor.imageeditor.IntNode"){
                    node.label.text = node.updated_info.int.toString()
                }
                else if (node.javaClass.toString() ==  "com.editor.imageeditor.FloatNode"){
                    node.label.text = node.updated_info.float.toString()
                }
                else if (node.javaClass.toString() == "com.editor.imageeditor.StringNode"){
                    node.label.text = node.updated_info.string
                }
            }
            for (node in nodes){
                if (node.javaClass.toString() == "com.editor.imageeditor.ImageSourceNode"){
                    node.update(node.updated_info)
                    break
                }
            }
        }
    }
}