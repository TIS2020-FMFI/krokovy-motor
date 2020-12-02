package gui;

import Interfaces.Observer;
import javafx.scene.control.Label;
import measurement.MeasurementManager;

public class RemainingStepsObserver implements Observer {

    private Label label;
    private MeasurementManager subject;

    public RemainingStepsObserver(MeasurementManager subject, Label label) {

        this.label = label;
        this.subject = subject;
    }

    @Override
    public void update() {

        if (subject.remainingSteps == null)
            label.setText("-");
        else
            label.setText(String.valueOf(subject.remainingSteps));
    }
}
