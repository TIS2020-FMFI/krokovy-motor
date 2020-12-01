package gui;

import Interfaces.Observer;
import javafx.scene.control.Label;

public class CurrentAngleObserver implements Observer {

    private Label actualAngleLabel;

    public CurrentAngleObserver(Label actualAngleLabel) {

        this.actualAngleLabel = actualAngleLabel;
    }

    @Override
    public void update(Object object) {

        if (object instanceof Double) {
            actualAngleLabel.setText(String.valueOf(object));
        }
    }
}
