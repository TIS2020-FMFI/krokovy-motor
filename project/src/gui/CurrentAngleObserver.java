package gui;

import Interfaces.Observer;
import javafx.scene.control.Label;
import serialCommunication.StepperMotor;

public class CurrentAngleObserver implements Observer {

    private Label currentAngleLabel;
    private Label thetaAngleLabel;
    private StepperMotor subject;

    public CurrentAngleObserver(StepperMotor subject, Label currentAngleLabel, Label thetaAngleLabel) {

        this.currentAngleLabel = currentAngleLabel;
        this.thetaAngleLabel = thetaAngleLabel;
        this.subject = subject;
    }

    @Override
    public void update() {

        if (subject.currentAngle == null) {
            currentAngleLabel.setText("-");
            thetaAngleLabel.setText("-");
        } else {
            double currentAngle = subject.currentAngle;
            currentAngleLabel.setText(String.valueOf(currentAngle));
            thetaAngleLabel.setText(String.valueOf(computeTheta(currentAngle)));
        }

    }

    private double computeTheta(Double currentAngle){
        return (180d - currentAngle)/2;
    }
}
