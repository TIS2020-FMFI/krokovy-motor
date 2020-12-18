package gui;

import Interfaces.Observer;
import javafx.scene.control.Label;
import serialCommunication.StepperMotor;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class CurrentAngleObserver implements Observer {

    private Label currentAngleLabel;
    private Label thetaAngleLabel;
    private StepperMotor subject;

    /**
     * @param subject the observed instance
     * @param currentAngleLabel label for showing current angle
     * @param thetaAngleLabel label for showing theta angle
     */
    public CurrentAngleObserver(StepperMotor subject, Label currentAngleLabel, Label thetaAngleLabel) {

        this.currentAngleLabel = currentAngleLabel;
        this.thetaAngleLabel = thetaAngleLabel;
        this.subject = subject;
    }

    /**
     * updates the current angle and theta angle values
     */
    @Override
    public void update() {

        if (subject.currentAngle == null) {
            currentAngleLabel.setText("-");
            thetaAngleLabel.setText("-");
        } else {
            double currentAngle = subject.currentAngle;
            currentAngleLabel.setText(String.valueOf(round(currentAngle, 2)));
            thetaAngleLabel.setText(String.valueOf(round(computeTheta(currentAngle), 2)));
        }

    }

    private double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();

        BigDecimal bd = new BigDecimal(Double.toString(value));
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }

    private double computeTheta(Double currentAngle){
        return (180d - currentAngle)/2;
    }

}
