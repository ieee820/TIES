package io.github.shahrukhqasim.ties.label;

import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.geometry.Point2D;
import javafx.geometry.Rectangle2D;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;

import java.util.Timer;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javafx.scene.control.ToggleButton;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;

public class Controller {
    Drawable image;
    Boxes boxesOcr;
    Boxes boxesCells;
    Connections connections;
    Drawable selectionBox;
    Drawable selectionConnection;
    InteractionManager interactionManager;
    final Object lock = new Object();
    final Lock lockDraw = new ReentrantLock();

    public ScrollPane scrollPane;
    public Canvas canvas;
    double scale = 1;
    Timer updater;
    Task task;
    public Label zoomLabel;
    public Label fileLabel;
    private IOManager ioManager;
    public ToggleButton toggleButtonConnect;
    public ToggleButton toggleButtonAutoSave;


    public Controller() {
    }

    void initialize() {
        this.ioManager = new IOManager(this);
        this.ioManager.initialize();
    }

    void redraw() {
        synchronized (lock) {
            long startTime = System.nanoTime();

            double vValue = scrollPane.getVvalue();
            double hValue = scrollPane.getHvalue();

            Rectangle2D boundingBox = image.getBoundingBox(scale);

            double oneWidth = Math.min(scrollPane.getWidth(), boundingBox.getWidth());
            double oneHeight = Math.min(scrollPane.getHeight(), boundingBox.getHeight());

            double x1 = hValue * (boundingBox.getWidth() - oneWidth);
            double y1 = vValue * (boundingBox.getHeight() - oneHeight);
            double x2 = x1 + oneWidth;
            double y2 = y1 + oneHeight;

            GraphicsContext graphics2D = canvas.getGraphicsContext2D();

            Rectangle2D visibleRect = new Rectangle2D(x1, y1, x2 - x1, y2 - y1);

            image.draw(graphics2D, visibleRect, scale);
            boxesOcr.draw(graphics2D, visibleRect, scale);
            boxesCells.draw(graphics2D, visibleRect, scale);
            selectionBox.draw(graphics2D, visibleRect, scale);
            selectionConnection.draw(graphics2D, visibleRect, scale);
            connections.draw(graphics2D, visibleRect, scale);

            long endTime = System.nanoTime();
            long duration = (endTime - startTime) / 1000000;
        }
    }


    @FXML
    void onZoomIn() {
        synchronized (lock) {
            if (scale == 2)
                return;
            scale += 0.25;
            zoomLabel.setText("Zoom: " + scale + "x");
            Rectangle2D box = image.getBoundingBox(scale);
            canvas.setWidth(box.getWidth());
            canvas.setHeight(box.getHeight());
        }
    }

    @FXML
    void onZoomOut() {
        synchronized (lock) {
            if (scale == 0.25)
                return;
            scale -= 0.25;
            zoomLabel.setText("Zoom: " + scale + "x");
            Rectangle2D box = image.getBoundingBox(scale);
            canvas.setWidth(box.getWidth());
            canvas.setHeight(box.getHeight());
        }
    }

    private Point2D startPointClick;

    @FXML
    void onCanvasMoved() {

    }
    @FXML
    void onCanvasPressed(MouseEvent event) {
        this.startPointClick = new Point2D(event.getX(), event.getY());
    }
    @FXML
    void onCanvasReleased(MouseEvent event) {
        synchronized (lock) {
            Point2D endPoint = new Point2D(event.getX(), event.getY());
            interactionManager.dragReleased(new Rectangle2D(Math.min(startPointClick.getX(), endPoint.getX()), Math.min(startPointClick.getY(), endPoint.getY()), Math.abs(endPoint.getX() - startPointClick.getX()), Math.abs(endPoint.getY() - startPointClick.getY())), scale, event.getButton());
        }

    }
    @FXML
    void onCanvasDragged(MouseEvent event) {
        synchronized (lock) {
            Point2D endPoint = new Point2D(event.getX(), event.getY());
            interactionManager.drag(startPointClick, endPoint, scale);
        }
    }

    @FXML
    void onKeyPressed(KeyEvent event) {
        synchronized (lock) {
            interactionManager.keyPressed(scale, event.getCode());
        }
    }

    @FXML
    void onOpen() {
        this.ioManager.open(false);
    }

    @FXML
    void onNext() {
        this.ioManager.next();
    }

    @FXML
    void onPrevious() {
        this.ioManager.previous();
    }

    @FXML
    void onSave() {
        this.ioManager.save();
    }

    @FXML
    void onToggleConnect() {
        this.interactionManager.onToggleConnect(toggleButtonConnect.isSelected());
    }
}
