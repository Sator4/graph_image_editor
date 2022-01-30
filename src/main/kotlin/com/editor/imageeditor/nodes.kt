package com.editor.imageeditor

import javafx.embed.swing.SwingFXUtils.fromFXImage
import javafx.embed.swing.SwingFXUtils.toFXImage
import javafx.event.EventHandler
import javafx.fxml.FXML
import javafx.fxml.FXMLLoader
import javafx.geometry.Point2D
import javafx.scene.SnapshotParameters
import javafx.scene.canvas.Canvas
import javafx.scene.control.Button
import javafx.scene.control.Label
import javafx.scene.control.TextField
import javafx.scene.image.Image
import javafx.scene.image.ImageView
import javafx.scene.input.ClipboardContent
import javafx.scene.input.TransferMode
import javafx.scene.layout.AnchorPane
import javafx.scene.layout.BorderPane
import javafx.scene.layout.HBox
import javafx.scene.layout.VBox
import javafx.scene.paint.Color
import javafx.scene.shape.Line
import javafx.stage.FileChooser
import javafx.stage.Stage
import org.opencv.core.CvType
import org.opencv.core.Mat
import org.opencv.core.Size
import org.opencv.imgproc.Imgproc
import java.awt.image.BufferedImage
import java.awt.image.DataBufferByte
import java.io.File
import java.net.URL
import java.util.*
import kotlin.math.absoluteValue
import kotlin.math.roundToInt


class Passinfo {
    var int: Int? = null
    var float: Float? = null
    var string: String? = null
    var image: Image? = null
    var from: String? = null
}


open class DraggableNode : BorderPane() {
    @FXML
    protected lateinit var resources: ResourceBundle

    @FXML
    protected lateinit var location: URL

    @FXML
    protected lateinit var VBox: VBox

    @FXML
    protected lateinit var buttonExit: Button

    @FXML
    protected lateinit var buttonMain: Button
    
    @FXML
    protected lateinit var globalBox: BorderPane

    @FXML
    lateinit var image: ImageView

    @FXML
    protected lateinit var input: TextField

    @FXML
    protected lateinit var label: Label

    @FXML
    protected lateinit var leftBox: VBox

    @FXML
    protected lateinit var rightBox: VBox

    @FXML
    protected lateinit var topBox: HBox

    var nextNode: DraggableNode? = null
    val updated_info = Passinfo()


    @FXML
    fun initialize() {
        id = UUID.randomUUID().toString()
        nodes.add(this)
        buttonExit.onAction = EventHandler { delete_this_node() }

        globalBox.onDragDetected = EventHandler { mouseEvent ->
//            println("nc-dr-b")
            val coords = Point2D(mouseEvent.sceneX - globalBox.layoutX, mouseEvent.sceneY - globalBox.layoutY)
            globalBox.parent.onDragOver = EventHandler { dragEvent ->
//                println("nc-dr-ov")
                globalBox.layoutX = dragEvent.sceneX - coords.x
                globalBox.layoutY = dragEvent.sceneY - coords.y
                dragEvent.acceptTransferModes(*TransferMode.ANY)
                dragEvent.consume()
            }
            globalBox.parent.onDragDropped = EventHandler { dragEvent ->
                globalBox.parent.onDragOver = null
                globalBox.parent.onDragDropped = null
                dragEvent.isDropCompleted = true
                dragEvent.consume()
            }
            val content = ClipboardContent()
            content.putString("qwer")
            globalBox.startDragAndDrop(*TransferMode.ANY).setContent(content)
            mouseEvent.consume()
        }
        globalBox.onDragDone = EventHandler { dragEvent ->
            globalBox.parent.onDragOver = null
            globalBox.parent.onDragDropped = null
            dragEvent.consume()
        }
    }

    init {
        val fxmlLoader = FXMLLoader(javaClass.getResource("draggableNode.fxml"))
        fxmlLoader.setRoot(this)
        fxmlLoader.setController(this)
        fxmlLoader.load<Any>()
    }

    open fun update(new_info: Passinfo){
        if (nextNode != null){
            nextNode!!.update(new_info)
        }
    }

    open fun get_number(updated_info: Passinfo, axis: Char){}

    open fun get_another_image(updated_info: Passinfo){}

    fun delete_this_node(){
        var type = imgType
        var offset = 0
        for (i in 0..connections.size){
            if (i - offset >= connections.size){
                break
            }
//            println(connections[i - offset].first.linkParent)
//            println(connections[i - offset].second.linkParent)
//            println()
            if (connections[i - offset].first.linkParent == this || connections[i - offset].second.linkParent == this){
                if (connections[i - offset].first.outputEnd && connections[i - offset].first.linkParent == this){
                    type = connections[i - offset].first.type
                } else if (connections[i - offset].second.outputEnd && connections[i - offset].second.linkParent == this){
                    type = connections[i - offset].second.type
                }
                connections[i - offset].first.linked = false
                connections[i - offset].first.link = Line()
                connections[i - offset].second.linked = false
                connections[i - offset].second.link = Line()
//                println((this.parent as AnchorPane).children)
                (this.parent as AnchorPane).children.remove(connections[i - offset].third)
//                println((this.parent as AnchorPane).children)
//                println()
                connections.remove(connections[i - offset])
                offset += 1
            }
        }
        if (this.nextNode != null){
            val updated_ifo_on_delete = Passinfo()
            updated_ifo_on_delete.from = type
            this.nextNode!!.update(updated_ifo_on_delete)
        }
        (this.parent as AnchorPane).children.remove(this)
    }
}


class OutputNode : DraggableNode() {
    init {
        val link = LinkController(this)
        link.onDragDetected = null
        this.leftBox.children.add(link)

        this.topBox.children.remove(buttonExit)
        this.VBox.children.remove(this.image)
        this.VBox.children.remove(this.input)
        this.VBox.children.remove(this.buttonMain)

        this.prefWidth = 100.0
        this.prefHeight = 50.0
        this.label.text = "Output"
        this.layoutX = 300.0
    }

    override fun update(new_info: Passinfo) {
        if (new_info.image == null){
            global_output!!.image = null
        }
        else {
            global_output!!.image = new_info.image
        }
    }
} // update done

class ImageSourceNode : DraggableNode() {
    init {
        this.VBox.spacing = 5.0
        this.VBox.children.remove(this.input)
//        this.topBox.children.remove(buttonExit)
        val update_btn = Button()
        update_btn.text = "Update"
        this.VBox.children.add(update_btn)
//        println(this.children)
        this.label.text = "Input"
        this.buttonMain.text = "Pick image"

        val link = LinkController(this)
        link.outputEnd = true
        this.rightBox.children.add(link)
        updated_info.from = imgType

        buttonMain.onAction = EventHandler {
            val fileChooser = FileChooser()
            val imageFilter = FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.bmp")
            fileChooser.extensionFilters.add(imageFilter)
            val file: File? = fileChooser.showOpenDialog(Stage())
            if (file == null){
                this.image.image = null
            }
            else {
                val preview = Image(file.toString())
                this.image.image = preview
            }
            updated_info.image = this.image.image
            update(updated_info)
        }

        update_btn.onAction = EventHandler { update(updated_info) }
    }
} // update done

class IntNode : DraggableNode() {
    var number: Int? = null
    init {
        this.layoutY = 150.0
        this.label.text = "0"

        val link = LinkController(this)
        link.outputEnd = true
        link.type = intType
        this.rightBox.children.add(link)
        updated_info.from = intType

        this.buttonMain.onAction = EventHandler {
            if (this.input.getText() == null){
                number = null
            }
            else {
                try {
                    number = this.input.getText().toInt()
                    this.label.text = this.input.getText()
                } finally {}
            }
            updated_info.int = number
            update(updated_info)
        }
    }
} // update done

class StringNode : DraggableNode() {
    var string = ""
    init {
        this.layoutY = 150.0
        this.label.text = ""

        val link = LinkController(this)
        link.outputEnd = true
        link.type = strType
        this.rightBox.children.add(link)
        updated_info.from = strType

        this.buttonMain.onAction = EventHandler {
            try {
                string = this.input.getText()
                this.label.text = string
            } finally {}
            updated_info.string = string
            if (nextNode != null) {
                nextNode!!.update(updated_info)
            }
        }
    }
}  // update done

class FloatNode : DraggableNode() {
    var number: Float? = null
    init {
        this.layoutY = 150.0
        this.label.text = "0.0"

        val link = LinkController(this)
        link.outputEnd = true
        link.type = floatType
        this.rightBox.children.add(link)
        updated_info.from = floatType

        this.buttonMain.onAction = EventHandler {
            if (this.input.getText() == null){
                number = null
            }
            else {
                try {
                    number = this.input.getText().toFloat()
                    this.label.text = this.input.getText()
                } finally {}
            }
            updated_info.float = number
            update(updated_info)
        }
    }
} // update done

class AddStringNode : DraggableNode() {
    var string: String? = null
    var pos_x = 0
    var pos_y = 0
    init {
        this.layoutY = 150.0
        this.label.text = "Add string"
        this.VBox.children.remove(this.input)
        this.VBox.children.remove(this.buttonMain)

        val linkImage = LinkController(this)
        val linkString = LinkController(this)
        val linkX = LinkController(this)
        val linkY = LinkController(this)
        val linkOut = LinkController(this)
        linkImage.linkEndLabel.text = "Img"
        linkImage.onDragDetected = null
        linkString.linkEndLabel.text = "Str"
        linkString.type = strType
        linkString.onDragDetected = null
        linkX.linkEndLabel.text = "X"
        linkX.type = intType
        linkX.onDragDetected = null
        linkY.linkEndLabel.text = "Y"
        linkY.type = intType
        linkY.onDragDetected = null
        linkOut.outputEnd = true
        this.leftBox.children.addAll(linkImage, linkString, linkX, linkY)
        this.rightBox.children.add(linkOut)
        updated_info.from = imgType

    }

    override fun update(new_info: Passinfo) {
        updated_info.image = new_info.image
        if (new_info.from == imgType) {
            this.image.image = new_info.image
        }
        if (new_info.from == strType) {
            this.string = new_info.string
        }


        if (this.image.image != null) {
            val width = this.image.image!!.width
            val height = this.image.image!!.height
            val t = Canvas(width, height)
            val t2 = t.graphicsContext2D
            t2.drawImage(this.image.image, 0.0, 0.0)
            println("${string}, ${pos_x}, ${pos_y}")
            t2.fillText(string, pos_x.toDouble(), pos_y.toDouble())
            val snapParams = SnapshotParameters()
            snapParams.fill = Color.TRANSPARENT
            updated_info.image = t.snapshot(snapParams, null)
        }
        this.image.image = updated_info.image
        if (nextNode != null){
            nextNode!!.update(updated_info)
        }
    }

    override fun get_number(updated_info: Passinfo, axis: Char){
        println(updated_info)
        println(axis)
        if (axis == 'X'){
            if (updated_info.int == null){
                pos_x = 0
            }
            else {
                pos_x = updated_info.int!!.toInt()
            }
        }
        else if (axis == 'Y'){
            if (updated_info.int == null){
                pos_y = 0
            }
            else {
                pos_y = updated_info.int!!.toInt()
            }
        }
        update(updated_info)
    }
} // update done

class FilterGreyNode : DraggableNode() {
    init {
        this.layoutY = 150.0
        this.label.text = "Grey filter"
        this.VBox.children.remove(this.input)
        this.VBox.children.remove(this.buttonMain)

        val linkIn = LinkController(this)
        val linkOut = LinkController(this)
        linkOut.outputEnd = true
        linkIn.onDragDetected = null

        this.leftBox.children.add(linkIn)
        this.rightBox.children.add(linkOut)
    }

    override fun update(new_info: Passinfo) {
        this.image.image = new_info.image
        if (this.image.image != null) {
            val t = imgToMat(this.image.image!!)
            Imgproc.cvtColor(t, t, Imgproc.COLOR_BGR2GRAY)
            this.image.image = matToImage(t)
        }
        updated_info.image = this.image.image
        if (nextNode != null){
            nextNode!!.update(updated_info)
        }
    }
} // done

class FilterBrightnessNode : DraggableNode() {
    var brightness: Double? = 1.0
    init {
        this.layoutY = 150.0
        this.label.text = "Brightness filter"
        this.VBox.children.remove(this.input)
        this.VBox.children.remove(this.buttonMain)

        val linkInImage = LinkController(this)
        val linkInFloat = LinkController(this)
        val linkOut = LinkController(this)
        linkOut.outputEnd = true
        linkInImage.onDragDetected = null
        linkInImage.linkEndLabel.text = "Img"
        linkInFloat.onDragDetected = null
        linkInFloat.linkEndLabel.text = "Float"
        linkInFloat.type = floatType

        this.leftBox.children.add(linkInImage)
        this.leftBox.children.add(linkInFloat)
        this.rightBox.children.add(linkOut)
    }

    override fun update(new_info: Passinfo) {
        if (new_info.from == imgType){
            this.image.image = new_info.image
        }
        if (new_info.from == floatType){
            if (new_info.float != null) {
                if (new_info.float!! < 0.0){
                    this.brightness = 0.0
                } else {
                    this.brightness = new_info.float!!.toDouble()
                }
            } else {
                this.brightness = 1.0
            }
        }

        if (this.image.image != null){
            val t = imgToMat(this.image.image)
            t.convertTo(t, -1, this.brightness!!)
            this.image.image = matToImage(t)
        }
        updated_info.image = this.image.image
        if (nextNode != null){
            nextNode!!.update(updated_info)
        }
    }
} // done

class FilterSepiaNode : DraggableNode() {
    init {
        this.layoutY = 150.0
        this.label.text = "Sepia filter"
        this.VBox.children.remove(this.input)
        this.VBox.children.remove(this.buttonMain)

        val linkIn = LinkController(this)
        val linkOut = LinkController(this)
        linkOut.outputEnd = true
        linkIn.onDragDetected = null

        this.leftBox.children.add(linkIn)
        this.rightBox.children.add(linkOut)
    }

    override fun update(new_info: Passinfo) {
        this.image.image = new_info.image
        if (new_info.image != null) {
            val img = fromFXImage(this.image.image!!, null)
            for(i in 0 until img.height){
                for (j in 0 until img.width){
                    val pixel: Int = img.getRGB(j, i)
                    var color = java.awt.Color(pixel, true)
                    var red = (color.red * 0.393 + color.green * 0.769 + color.blue * 0.189).roundToInt()
                    var green = (color.red * 0.349 + color.green * 0.686 + color.blue * 0.168).roundToInt()
                    var blue = (color.red * 0.272 + color.green * 0.534 + color.blue * 0.131).roundToInt()
                    if (red > 255) red = 255
                    if (green > 255) green = 255
                    if (blue > 255) blue = 255
                    color = java.awt.Color(red, green, blue)
                    img.setRGB(j, i, color.rgb)
                }
            }
            this.image.image = toFXImage(img, null)
        }
        updated_info.image = this.image.image
        if (nextNode != null){
            nextNode!!.update(updated_info)
        }
    }
} // done

class FilterInvertNode : DraggableNode() {
    init {
        this.layoutY = 150.0
        this.label.text = "Invert filter"
        this.VBox.children.remove(this.input)
        this.VBox.children.remove(this.buttonMain)

        val linkIn = LinkController(this)
        val linkOut = LinkController(this)
        linkOut.outputEnd = true
        linkIn.onDragDetected = null

        this.leftBox.children.add(linkIn)
        this.rightBox.children.add(linkOut)
    }

    override fun update(new_info: Passinfo) {
        this.image.image = new_info.image
        if (new_info.image != null) {
            val img = fromFXImage(this.image.image!!, null)
            for(i in 0 until img.height){
                for (j in 0 until img.width){
                    val pixel: Int = img.getRGB(j, i)
                    var color = java.awt.Color(pixel, true)
                    color = java.awt.Color(255 - color.red, 255 - color.green, 255 - color.blue)
                    img.setRGB(j, i, color.rgb)
                }
            }
            this.image.image = toFXImage(img, null)
        }
        updated_info.image = this.image.image
        if (nextNode != null){
            nextNode!!.update(updated_info)
        }
    }
} // done

class FilterBlurNode : DraggableNode() {
    var kernelSize: Int = 1
    init {
        this.layoutY = 150.0
        this.label.text = "Blur filter"
        this.VBox.children.remove(this.input)
        this.VBox.children.remove(this.buttonMain)

        val linkInImage = LinkController(this)
        val linkInInt = LinkController(this)
        val linkOut = LinkController(this)
        linkInImage.linkEndLabel.text = "Img"
        linkInImage.onDragDetected = null
        linkInInt.type = intType
        linkInInt.linkEndLabel.text = "Int"
        linkInInt.onDragDetected = null
        linkOut.outputEnd = true

        this.leftBox.children.add(linkInImage)
        this.leftBox.children.add(linkInInt)
        this.rightBox.children.add(linkOut)
    }

    override fun update(new_info: Passinfo) {
        if (new_info.from == intType){
            println(this.kernelSize)
            println(this.image)
            if (new_info.int != null){
                this.kernelSize = new_info.int!!
                if (this.kernelSize % 2 == 0){
                    this.kernelSize -= 1
                }
            } else {
                this.kernelSize = 1
            }
        }
        if (new_info.from == imgType){
            this.image.image = new_info.image
        }
        if (this.image.image != null) {
            val t = imgToMat(this.image.image!!)
            Imgproc.GaussianBlur(t, t, Size(kernelSize.toDouble(), kernelSize.toDouble()), 0.0)
            this.image.image = matToImage(t)
        }
        updated_info.image = this.image.image
        if (nextNode != null){
            nextNode!!.update(updated_info)
        }
    }
} // done

class TransformMoveNode : DraggableNode() {
    var pos_x = 0.0
    var pos_y = 0.0
    init {
        this.layoutY = 150.0
        this.label.text = "Move"
        this.VBox.children.remove(this.input)
        this.VBox.children.remove(this.buttonMain)

        val linkImage = LinkController(this)
        val linkX = LinkController(this)
        val linkY = LinkController(this)
        val linkOut = LinkController(this)
        linkImage.linkEndLabel.text = "Img"
        linkImage.onDragDetected = null
        linkX.linkEndLabel.text = "X"
        linkX.type = floatType
        linkX.onDragDetected = null
        linkY.linkEndLabel.text = "Y"
        linkY.type = floatType
        linkY.onDragDetected = null
        linkOut.outputEnd = true
        this.leftBox.children.addAll(linkImage, linkX, linkY)
        this.rightBox.children.add(linkOut)
        updated_info.from = imgType
    }

    override fun update(new_info: Passinfo) {
        if (new_info.from == imgType){
            this.image.image = new_info.image
        }
        if (this.image.image != null){
            val t = Canvas(this.image.image!!.width + pos_x, this.image.image!!.height + pos_y)
            t.graphicsContext2D.drawImage(this.image.image, pos_x, pos_y)
            this.image.image = t.snapshot(SnapshotParameters(), null)
        }
        updated_info.image = this.image.image
        if (nextNode != null){
            nextNode!!.update(updated_info)
        }
    }

    override fun get_number(updated_info: Passinfo, axis: Char) {
        if (axis == 'X'){
            if (updated_info.float == null){
                pos_x = 0.0
            }
            else {
                pos_x = updated_info.float!!.toDouble()
            }
        }
        else if (axis == 'Y'){
            if (updated_info.float == null){
                pos_y = 0.0
            }
            else {
                pos_y = updated_info.float!!.toDouble()
            }
        }
        update(updated_info)
    }
} // done

class TransformScaleNode : DraggableNode() {
    var scale_x = 1.0
    var scale_y = 1.0
    init {
        this.layoutY = 150.0
        this.label.text = "Scale"
        this.VBox.children.remove(this.input)
        this.VBox.children.remove(this.buttonMain)

        val linkImage = LinkController(this)
        val linkX = LinkController(this)
        val linkY = LinkController(this)
        val linkOut = LinkController(this)
        linkImage.linkEndLabel.text = "Img"
        linkImage.onDragDetected = null
        linkX.linkEndLabel.text = "X"
        linkX.type = floatType
        linkX.onDragDetected = null
        linkY.linkEndLabel.text = "Y"
        linkY.type = floatType
        linkY.onDragDetected = null
        linkOut.outputEnd = true
        this.leftBox.children.addAll(linkImage, linkX, linkY)
        this.rightBox.children.add(linkOut)
        updated_info.from = imgType
    }

    override fun update(new_info: Passinfo) {
        if (new_info.from == imgType){
            this.image.image = new_info.image
        }
        if (this.image.image != null){
            val t = imgToMat(this.image.image!!)
            val t2 = Mat()
            Imgproc.resize(t, t2, Size(0.0, 0.0), scale_x, scale_y)
            this.image.image = matToImage(t2)
        }
        updated_info.image = this.image.image
        if (nextNode != null){
            nextNode!!.update(updated_info)
        }
    }

    override fun get_number(updated_info: Passinfo, axis: Char) {
        if (axis == 'X'){
            if (updated_info.float == null){
                scale_x = 1.0
            }
            else {
                try {
                    scale_x = 1 / (updated_info.float!!.toDouble().absoluteValue)
                    println(scale_x)
                } catch (e: Exception){
                    scale_x = 1.0
                }
            }
        }
        else if (axis == 'Y'){
            if (updated_info.float == null){
                scale_y = 1.0
            }
            else {
                try {
                    scale_y = 1 / (updated_info.float!!.toDouble().absoluteValue)
                    println(scale_y)
                } catch (e: Exception){
                    scale_y = 1.0
                }
            }
        }
        update(updated_info)
    }
}

class TransformRotateNode : DraggableNode() {
    var angle = 0.0
    init {
        this.layoutY = 150.0
        this.label.text = "Rotate"
        this.VBox.children.remove(this.input)
        this.VBox.children.remove(this.buttonMain)

        val linkImage = LinkController(this)
        val linkRad = LinkController(this)
        val linkOut = LinkController(this)
        linkImage.linkEndLabel.text = "Img"
        linkImage.onDragDetected = null
        linkRad.linkEndLabel.text = "Rad"
        linkRad.type = floatType
        linkRad.onDragDetected = null
        linkOut.outputEnd = true
        this.leftBox.children.addAll(linkImage, linkRad)
        this.rightBox.children.add(linkOut)
        updated_info.from = imgType
    }
    override fun update(new_info: Passinfo) {
        if (new_info.from == imgType){
            this.image.image = new_info.image
        }
        if (new_info.from == floatType){
            this.angle = new_info.float!!.toDouble()
        }
        if (this.image.image != null){
            val t = Canvas(this.image.image!!.width, this.image.image!!.height)
            t.rotate = angle * 180 / Math.PI
            t.graphicsContext2D.drawImage(this.image.image, 0.0, 0.0)
            this.image.image = t.snapshot(SnapshotParameters(), null)
        }
        updated_info.image = this.image.image
        if (nextNode != null){
            nextNode!!.update(updated_info)
        }
    }
} // done

class AddImageNode : DraggableNode() {
    var image2: Image? = null
    var pos_x = 0
    var pos_y = 0
    init {
        this.layoutY = 150.0
        this.label.text = "Add image"
        this.VBox.children.remove(this.input)
        this.VBox.children.remove(this.buttonMain)

        val linkImage = LinkController(this)
        val linkImage2 = LinkController(this)
        val linkX = LinkController(this)
        val linkY = LinkController(this)
        val linkOut = LinkController(this)
        linkImage.linkEndLabel.text = "Img"
        linkImage.onDragDetected = null
        linkImage2.linkEndLabel.text = "Img2"
        linkImage2.type = imgType
        linkImage2.onDragDetected = null
        linkX.linkEndLabel.text = "X"
        linkX.type = intType
        linkX.onDragDetected = null
        linkY.linkEndLabel.text = "Y"
        linkY.type = intType
        linkY.onDragDetected = null
        linkOut.outputEnd = true
        this.leftBox.children.addAll(linkImage, linkImage2, linkX, linkY)
        this.rightBox.children.add(linkOut)
        updated_info.from = imgType

    }

    override fun update(new_info: Passinfo) {
        println(image2)
        println(this.image.image)
        if (new_info.from == imgType) {
            this.image.image = new_info.image
        }

        if (this.image.image != null && this.image2 != null) {
            val width = this.image.image!!.width
            val height = this.image.image!!.height
            val t = Canvas(width, height)
            val t2 = t.graphicsContext2D
            t2.drawImage(this.image.image, 0.0, 0.0)
            t2.drawImage(this.image2, pos_x.toDouble(), pos_y.toDouble())
            this.image.image = t.snapshot(SnapshotParameters(), null)
        }
        updated_info.image = this.image.image
        if (nextNode != null){
            nextNode!!.update(updated_info)
        }
    }

    override fun get_number(updated_info: Passinfo, axis: Char){
        if (axis == 'X'){
            if (updated_info.int == null){
                pos_x = 0
            }
            else {
                pos_x = updated_info.int!!.toInt()
            }
        }
        else if (axis == 'Y'){
            if (updated_info.int == null){
                pos_y = 0
            }
            else {
                pos_y = updated_info.int!!.toInt()
            }
        }
        update(updated_info)
    }

    override fun get_another_image(updated_info: Passinfo) {
        image2 = updated_info.image
        updated_info.image = this.image.image
        update(updated_info)
    }
}


fun imgToMat(img: Image): Mat {
    val width = img.width.toInt()
    val height = img.height.toInt()
    val bufImg = fromFXImage(img, null)
    val convertedImage = BufferedImage(width, height, BufferedImage.TYPE_3BYTE_BGR)
    convertedImage.graphics.drawImage(bufImg, 0, 0, null)
    val mat = Mat(height, width, CvType.CV_8UC3)
    mat.put(0, 0, (convertedImage.raster.dataBuffer as DataBufferByte).data)
    return mat
}

fun matToImage(frame: Mat): Image? {
    return try {
        toFXImage(matToBufferedImage(frame), null)
    } catch (e: java.lang.Exception) {
        System.err.println("Cannot convert the Mat obejct: $e")
        null
    }
}

private fun matToBufferedImage(original: Mat): BufferedImage {
    // init
    var image: BufferedImage? = null
    val width = original.width()
    val height = original.height()
    val channels = original.channels()
    val sourcePixels = ByteArray(width * height * channels)
    original[0, 0, sourcePixels]
    image = if (original.channels() > 1) {
        BufferedImage(width, height, BufferedImage.TYPE_3BYTE_BGR)
    } else {
        BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY)
    }
    val targetPixels = (image.raster.dataBuffer as DataBufferByte).data
    System.arraycopy(sourcePixels, 0, targetPixels, 0, sourcePixels.size)
    return image
}