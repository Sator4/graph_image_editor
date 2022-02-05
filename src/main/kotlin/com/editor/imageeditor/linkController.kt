package com.editor.imageeditor

import javafx.beans.binding.Bindings
import javafx.event.EventHandler
import java.net.URL
import javafx.fxml.FXML
import javafx.fxml.FXMLLoader
import javafx.scene.control.Label
import javafx.scene.input.ClipboardContent
import javafx.scene.input.TransferMode
import javafx.scene.layout.AnchorPane
import javafx.scene.layout.VBox
import javafx.scene.shape.Line
import java.util.*

class LinkController(val linkParent: DraggableNode) : VBox() {

    @FXML
    private lateinit var resources: ResourceBundle

    @FXML
    private lateinit var location: URL

    @FXML
    private lateinit var linkEnd: VBox

    @FXML
    lateinit var linkEndLabel: Label


    var effectsArea: AnchorPane? = null
    var templink = Line()
    var link = Line()
    var outputEnd = false
    var type = imgType
    var linked = false
    var axis = ' '
    var serial_number = 0
    var lbx: Double = 0.0
    var lby: Double = 0.0
    var lex: Double = 0.0
    var ley: Double = 0.0



    @FXML
    fun initialize() {
        id = UUID.randomUUID().toString()


        linkEnd.onDragDetected = EventHandler { mouseEvent ->

//            println("next begin ${linkParent}, ${linkParent.nextNode}")
            if (linkParent.nextNode != null){
                for (connection in connections){
                    if (connection.second.parent.parent == linkParent){
                        if (connection.first.linkEndLabel.text == "Y" || connection.second.linkEndLabel.text == "Y"){
                            axis = 'Y'
                        } else if (connection.first.linkEndLabel.text == "X" || connection.second.linkEndLabel.text == "X") {
                            axis = 'X'
                        } else if (connection.first.linkEndLabel.text == "Img2" || connection.second.linkEndLabel.text == "Img2"){
                            axis = 'I'
                        }
                        effectsArea!!.children.remove(connection.third)
                        println(connection.first.id)
                        println(connection.second.id)
                        connection.first.linked = false
                        connection.first.link = Line()
                        connection.second.linked = false
                        connection.second.link = Line()
                        connections.remove(connection)
                        break
                    }
                }

                val updated_info_on_drop = Passinfo()
                updated_info_on_drop.from = linkParent.updated_info.from
                if (axis != ' '){
                    if (type == intType || type == floatType) {
                        linkParent.nextNode!!.get_number(updated_info_on_drop, axis)
                    } else if (type == imgType){
                        linkParent.nextNode!!.get_another_image(updated_info_on_drop)
                    }
                }
                else {
                    linkParent.nextNode!!.update(updated_info_on_drop)
                }
                linkParent.nextNode = null
                println("lc-sltn-f")
            }
            effectsArea = linkParent.parent as AnchorPane
//            println("lc-1")
            templink.startX = this.localToScene(this.boundsInLocal).centerX - 120
            templink.startY = this.localToScene(this.boundsInLocal).centerY - 10
            effectsArea!!.children.add(templink)

            effectsArea!!.onDragOver = EventHandler { dragEvent ->
//                println("lc-2")
                templink.endX = dragEvent.x
                templink.endY = dragEvent.y
                dragEvent.acceptTransferModes(*TransferMode.ANY)
                dragEvent.consume()
            }

            effectsArea!!.onDragDropped = EventHandler { dragEvent ->
//                println("lc-3")
                effectsArea!!.onDragOver = null
                effectsArea!!.onDragDropped = null
                effectsArea!!.children.remove(templink)
                dragEvent.consume()
            }
            val content = ClipboardContent()
            content[linkType] = type
            content[parentId] = linkParent.id
            linkEnd.startDragAndDrop(*TransferMode.ANY).setContent(content)
            mouseEvent.consume()
        }

        linkEnd.onDragOver = EventHandler { dragEvent ->
//            println(id)
//            println(linked)
            if (!outputEnd && dragEvent.dragboard.getContent(parentId) != linkParent.id &&
                !linked &&
                dragEvent.dragboard.getContent(linkType) == type){
//                println("lc-ov-gra")
                dragEvent.acceptTransferModes(*TransferMode.ANY)
            }
            dragEvent.consume()
        }
        linkEnd.onDragDropped = EventHandler { dragEvent ->
            on_drag_dropped(dragEvent.gestureSource as LinkController, dragEvent.gestureTarget as LinkController, true)

//            for (i in connections) println(i)
            dragEvent.consume()
            println("lc-dr-f")
        }
        linkEnd.onDragDone = EventHandler { dragEvent ->
            effectsArea!!.onDragOver = null
            effectsArea!!.onDragDropped = null
            effectsArea!!.children.remove(templink)
            dragEvent.consume()
            println("lc-f")
            println()
        }
    }

    fun on_drag_dropped(link_begin: LinkController, link_end: LinkController, from_event: Boolean){
        if (effectsArea == null){
            effectsArea = linkParent.parent as AnchorPane
        }
//            println("lc-dr")

        (link_begin.parent.parent as DraggableNode).nextNode = link_end.parent.parent as DraggableNode
//            println("lc-dr-1")



//            node_link!!.startXProperty().bind(Bindings.add(source1.layoutXProperty(), source1.width/2.0))
//            node_link!!.startYProperty().bind(Bindings.add(source1.layoutYProperty(), source1.height/2.0))

        if (from_event) {
            link.startX = link_begin.localToScene(link_begin.boundsInLocal).centerX - 120
            link.startY = link_begin.localToScene(link_begin.boundsInLocal).centerY - 10
            link.endX = link_end.localToScene(link_end.boundsInLocal).centerX - 120
            link.endY = link_end.localToScene(link_end.boundsInLocal).centerY - 10
            lbx = link.startX
            lby = link.startY
            lex = link.endX
            ley = link.endY
        }
        else {
            link = Line()
            link.startX = lbx
            link.startY = lby
            link.endX = lex
            link.endY = ley
        }
        link.startXProperty().bind(Bindings.add(
            link_begin.linkParent.layoutXProperty(),
            link.startX - link_begin.linkParent.layoutX))
        link.startYProperty().bind(Bindings.add(
            link_begin.linkParent.layoutYProperty(),
            link.startY - link_begin.linkParent.layoutY))
        link.endXProperty().bind(Bindings.add(
            link_end.linkParent.layoutXProperty(),
            link.endX - link_end.linkParent.layoutX))
        link.endYProperty().bind(Bindings.add(
            link_end.linkParent.layoutYProperty(),
            link.endY - link_end.linkParent.layoutY))
//            println(this.linkParent)
        effectsArea!!.children.add(link)
        connections.add(Triple(link_end, link_begin, link))
        link_end.linked = true
        link_begin.linked = true

        if (link_end.linkEndLabel.text == "X"){
            axis = 'X'
        } else if (link_end.linkEndLabel.text == "Y"){
            axis = 'Y'
        } else if (link_end.linkEndLabel.text == "Img2"){
            axis = 'I'
        }
        else {
            axis = ' '
        }
        if (axis != ' ') {
            if (type == intType || type == floatType) {
                linkParent.get_number((link_begin.parent.parent as DraggableNode).updated_info, axis)
            } else if (type == imgType){
                linkParent.get_another_image((link_begin.parent.parent as DraggableNode).updated_info)
            }
        }
        else {
            linkParent.update((link_begin.parent.parent as DraggableNode).updated_info)
        }
    }

    init {
        val fxmlLoader = FXMLLoader(javaClass.getResource("link.fxml"))
        fxmlLoader.setRoot(this)
        fxmlLoader.setController(this)
        fxmlLoader.load<Any>()
    }


}