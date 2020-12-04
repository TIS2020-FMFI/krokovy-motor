package gui;

import Interfaces.Observer;
import javafx.scene.control.Label;
import measurement.MeasurementManager;
import measurement.SeriesOfMeasurements;

public class RemainingStepsObserver implements Observer {

    private Label label;
    private SeriesOfMeasurements subject;

    public RemainingStepsObserver(SeriesOfMeasurements subject, Label label) {

        this.label = label;
        this.subject = subject;
    }

    @Override
    public void update() {

        if (subject.remainingSteps == 0)
            label.setText("-");
        else
            label.setText(String.valueOf(subject.remainingSteps));
    }
}
