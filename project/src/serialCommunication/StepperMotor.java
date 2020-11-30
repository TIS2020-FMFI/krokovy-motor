package serialCommunication;

import Exceptions.FilesAndFoldersExcetpions.WrongParameterException;
import Exceptions.SerialCommunicationExceptions.PicaxeConnectionErrorException;
import Exceptions.SerialCommunicationExceptions.PortNotFoundException;
import Exceptions.SerialCommunicationExceptions.UnknownCurrentAngleException;
import com.fazecast.jSerialComm.SerialPort;
import com.fazecast.jSerialComm.SerialPortDataListener;
import com.fazecast.jSerialComm.SerialPortEvent;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.control.Label;
import javafx.util.Duration;
import settings.Settings;

public class StepperMotor {

    public Double currentAngle = null;
    private SerialPort serialPort = null;
    private final int impulseTime = 50; //zahrna aj pauzu medzi impulzmi
    Timeline timeline;
    byte[] forwardSign = new byte['+'];
    byte[] backwardsSign = new byte['-'];

    public StepperMotor() {
    }

    public void moveOnePulseForward(Label currentAngleLabel) {
        serialPort.writeBytes(forwardSign, 1);
        if (Settings.isCalibrationSet()) {
            currentAngle += Settings.getPulseToAngleRatio();
            currentAngleLabel.setText(String.valueOf(currentAngle));
        }
    }

    public void moveOnePulseBackwards(Label currentAngleLabel) {
        serialPort.writeBytes(backwardsSign, 1);
        if (Settings.isCalibrationSet()) {
            currentAngle -= Settings.getPulseToAngleRatio();
            currentAngleLabel.setText(String.valueOf(currentAngle));
        }
    }

    public void stepForward(Label currentAngleLabel) {
        /*if (!checkPicaxeConnection()) {
            throw new PicaxeConnectionErrorException("Picaxe connection error");
        }*/

        timeline = new Timeline(new KeyFrame(Duration.millis(impulseTime), e -> {
            moveOnePulseForward(currentAngleLabel);
        }));
        timeline.setCycleCount(Settings.getStepSize());
        timeline.play();
    }

    public void stepBackwards(Label currentAngleLabel) {
        /*if (!checkPicaxeConnection()) {
            throw new PicaxeConnectionErrorException("Picaxe connection error");
        }*/

        timeline = new Timeline(new KeyFrame(Duration.millis(impulseTime), e -> {
            moveOnePulseBackwards(currentAngleLabel);
        }));
        timeline.setCycleCount(Settings.getStepSize());
        timeline.play();
    }

    public void moveToAngle(String angleValue, Label currentAngleLabel) throws UnknownCurrentAngleException, WrongParameterException {

        double angle;

        if (angleValue == null) {
            throw new WrongParameterException("value for move to angle is not set");
        }

        try {
            angle = Double.parseDouble(angleValue);
        } catch (NumberFormatException e) {
            throw new WrongParameterException("value for move to angle is in wrong format");
        }

        if (currentAngle == null) {
            throw new UnknownCurrentAngleException("Current angle is unknown");
        }

        if (currentAngle < angle) {
            timeline = new Timeline(new KeyFrame(Duration.millis(impulseTime), e -> {
                moveOnePulseForward(currentAngleLabel);
            }));
        } else {
            timeline = new Timeline(new KeyFrame(Duration.millis(impulseTime), e -> {
                moveOnePulseBackwards(currentAngleLabel);
            }));
        }
        timeline.setCycleCount(pulsesNeededToMove(angle));
        timeline.play();
    }

    public Integer pulsesNeededToMove(double endAngle) {
        Double pulseToAngleRatio = Settings.getPulseToAngleRatio();

        double angleDiff = Math.abs(currentAngle - endAngle);
        int pulseCount1 = (int) Math.floor((angleDiff) / pulseToAngleRatio); // idem pred alebo na koncovy uhol
        int pulseCount2 = (int) Math.ceil((angleDiff) / pulseToAngleRatio); // idem za alebo na koncovy uhol
        double diff1 = Math.abs(endAngle - (currentAngle + pulseCount1 * pulseToAngleRatio));
        double diff2 = Math.abs(endAngle - (currentAngle + pulseCount2 * pulseToAngleRatio));

        return diff1 < diff2 ? pulseCount1 : pulseCount2;    // ktore je blizsie k uhlu kam chceme ist
    }

    public Integer stepsNeededToMove(double endAngle) {
        Double stepToAngleRatio = getStepToAngleRatio();

        double angleDiff = Math.abs(currentAngle - endAngle);
        int stepCount1 = (int) Math.floor((angleDiff) / stepToAngleRatio); // idem pred alebo na koncovy uhol
        int stepCount2 = (int) Math.ceil((angleDiff) / stepToAngleRatio); // idem za alebo na koncovy uhol
        double diff1 = Math.abs(endAngle - (currentAngle + stepCount1 * stepToAngleRatio));
        double diff2 = Math.abs(endAngle - (currentAngle + stepCount2 * stepToAngleRatio));

        return diff1 < diff2 ? stepCount1 : stepCount2;    // ktore je blizsie k uhlu kam chceme ist
    }

    public void findPicaxe() throws PortNotFoundException {
        SerialPort[] serialPorts = SerialPort.getCommPorts();

        if (serialPorts.length == 0) {
            throw new PortNotFoundException("Ports not found");
        }

        for (SerialPort port : serialPorts) {
            port.openPort();
            port.addDataListener(new SerialPortDataListener() {
                @Override
                public int getListeningEvents() {

                    return SerialPort.LISTENING_EVENT_DATA_RECEIVED;
                }

                @Override
                public void serialEvent(SerialPortEvent event) {

                    byte[] data = event.getReceivedData();
                    if ((char) data[0] == '@') {
                        serialPort = port;
                        try {
                            sendPingToPicaxe('+');
                        } catch (PicaxeConnectionErrorException e) {
                            e.printStackTrace();
                        }
                    }
                }
            });
        }

//        Timeline tmp = new Timeline(new KeyFrame(Duration.millis(2000), e -> {
//        }));
//        tmp.setCycleCount(1);
//        tmp.setOnFinished(e -> {
//            if (checkPicaxeConnection()) {
//                // Picaxe bol najdeny
//            } else {
//                // Picaxe nebol najdeny
//            }
//        });
//        tmp.play();
    }

    public boolean checkPicaxeConnection() {
        if (serialPort == null) {
            return false;
        }
        return serialPort.isOpen();
        // return serialPort.openPort( (int)sleepTime );
    }

    public void sendPingToPicaxe(char ping) throws PicaxeConnectionErrorException {
        if (!checkPicaxeConnection()) {
            throw new PicaxeConnectionErrorException("Picaxe connection error");
        }
        byte[] data = new byte[ping];
        serialPort.writeBytes(data, 1);
    }

    public double getImpulseTime() {
        return impulseTime;
    }

    public double getStepTime() {
        return Settings.getStepSize() * impulseTime;
    }

    public double getStepToAngleRatio() {
        return Settings.getStepSize() * Settings.getPulseToAngleRatio();
    }
}
