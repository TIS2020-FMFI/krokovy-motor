package gui;

import Interfaces.Observer;
import Interfaces.Subject;
import javafx.scene.control.Label;
import serialCommunication.StepperMotor;

public class CurrentAngleObserver implements Observer {

    private Label label;
    private StepperMotor subject;

    public CurrentAngleObserver(StepperMotor subject, Label label) {

        this.label = label;
        this.subject = subject;
    }

    @Override
    public void update() {

        if (subject.currentAngle == null)
            label.setText("-");
        else
            label.setText(String.valueOf(subject.currentAngle));
    }
}
