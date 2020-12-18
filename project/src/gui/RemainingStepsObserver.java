package gui;

import Interfaces.Observer;
import javafx.scene.control.Label;
import measurement.MeasurementManager;
import measurement.SeriesOfMeasurements;

public class RemainingStepsObserver implements Observer {

    private Label label;
    private SeriesOfMeasurements subject;

    /**
     * @param subject the observed instance
     * @param label label for showing remaining steps
     */
    public RemainingStepsObserver(SeriesOfMeasurements subject, Label label) {

        this.label = label;
        this.subject = subject;
    }

    /**
     * updates the remaining steps value
     */
    @Override
    public void update() {

        if (subject.remainingSteps == 0)
            label.setText("-");
        else
            label.setText(String.valueOf(subject.remainingSteps));
    }
}
