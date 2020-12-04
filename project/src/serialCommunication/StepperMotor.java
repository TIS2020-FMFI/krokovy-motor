package serialCommunication;

import Exceptions.FilesAndFoldersExcetpions.WrongParameterException;
import Exceptions.SerialCommunicationExceptions.PicaxeConnectionErrorException;
import Exceptions.SerialCommunicationExceptions.PortNotFoundException;
import Exceptions.SerialCommunicationExceptions.UnknownCurrentAngleException;
import Interfaces.Observer;
import Interfaces.Subject;
import com.fazecast.jSerialComm.SerialPort;
import com.fazecast.jSerialComm.SerialPortDataListener;
import com.fazecast.jSerialComm.SerialPortEvent;
import gui.CurrentAngleObserver;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.control.Label;
import javafx.util.Duration;
import settings.Settings;

import java.io.IOException;
import java.util.ArrayList;

public class StepperMotor implements Subject {

    public Double currentAngle = null;
    private SerialPort serialPort = null;
    private final int impulseTime = 50; //zahrna aj pauzu medzi impulzmi
    private Timeline timeline;
    private ArrayList<Observer> observers = new ArrayList();


    public StepperMotor() {
    }

    public void moveOnePulseForward() {
        System.out.println("pohyb dopredu");
        try {
            serialPort.getOutputStream().write('+');
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
        if (Settings.getInstance().isCalibrationSet()) {
            currentAngle += Settings.getInstance().getPulseToAngleRatio();
            notifyObservers(); // currentAngleLabel.setText(String.valueOf(currentAngle));
        }
    }

    public void moveOnePulseBackwards() {
        System.out.println("pohyb dozadu");
        try {
            serialPort.getOutputStream().write('-');
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
        if (Settings.getInstance().isCalibrationSet()) {
            currentAngle -= Settings.getInstance().getPulseToAngleRatio();
            notifyObservers(); // currentAngleLabel.setText(String.valueOf(currentAngle));
        }
    }

    public void stepForward() {

        /*
        if (!checkPicaxeConnection()) {
            throw new PicaxeConnectionErrorException("Picaxe connection error");
        }
        */

        timeline = new Timeline(new KeyFrame(Duration.millis(impulseTime), e -> {
            moveOnePulseForward();
        }));
        timeline.setCycleCount(Settings.getInstance().getStepSize());
        timeline.play();
    }

    public void stepBackwards() {

        /*
        if (!checkPicaxeConnection()) {
            throw new PicaxeConnectionErrorException("Picaxe connection error");
        }
        */

        timeline = new Timeline(new KeyFrame(Duration.millis(impulseTime), e -> {
            moveOnePulseBackwards();
        }));
        timeline.setCycleCount(Settings.getInstance().getStepSize());
        timeline.play();
    }

    public void moveToAngle(String angleValue) throws UnknownCurrentAngleException, WrongParameterException {

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
                moveOnePulseForward();
            }));
        } else {
            timeline = new Timeline(new KeyFrame(Duration.millis(impulseTime), e -> {
                moveOnePulseBackwards();
            }));
        }
        timeline.setCycleCount(pulsesNeededToMove(angle));
        timeline.play();
    }

    public Integer pulsesNeededToMove(double endAngle) {

        Double pulseToAngleRatio = Settings.getInstance().getPulseToAngleRatio();

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
            String portName = port.getDescriptivePortName().toUpperCase();
            System.out.println(port.getDescriptivePortName());
            if (portName.contains("COM1")) {
                serialPort = port;
                serialPort.openPort(500);
                serialPort.setBaudRate(9600);
                serialPort.setNumDataBits(8);
                serialPort.setNumStopBits(1);
                serialPort.setParity(0);
            }

        }

        /*

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

        */
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

        return Settings.getInstance().getStepSize() * impulseTime;
    }

    public double getStepToAngleRatio() {

        return Settings.getInstance().getStepSize() * Settings.getInstance().getPulseToAngleRatio();
    }

    @Override
    public void attach(Observer observer) {

        observers.add(observer);
    }

    @Override
    public void notifyObservers() {

        for (Observer observer : observers) {
            observer.update();
        }
    }

    @Override
    public void detach(Observer observer) {

        observers.remove(observer);
    }
}
