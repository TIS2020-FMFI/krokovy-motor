package gui;

import Interfaces.Observer;
import javafx.scene.control.Label;

public class CurrentAngleObserver implements Observer {

    private Label label;

    public CurrentAngleObserver(Label label) {

        this.label = label;
    }

    @Override
    public void update(Object object) {

        // if (object instanceof Double || object instanceof Integer) { }
        label.setText(String.valueOf(object));
    }
}
