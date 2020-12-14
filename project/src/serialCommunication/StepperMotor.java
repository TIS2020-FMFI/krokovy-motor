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
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;
import settings.Settings;
import java.io.IOException;
import java.util.ArrayList;

public class StepperMotor implements Subject {

    private final int IMPULSE_TIME = 100; //zahrna aj pauzu medzi impulzmi
    private final int BAUD_RATE = 9600;
    private final int NUM_DATA_BITS = 8;
    private final int NUM_STOP_BITS = 1;
    private final int PARITY = 0;
    private final char MOTOR_CHECK_PING = '!';
    private final char MOTOR_STOP = 'n';


    private SerialPort serialPort = null;
    private Timeline timeline;
    private Timeline stopTimeline;
    private ArrayList<Observer> observers = new ArrayList();

    public Double currentAngle = null;

    public StepperMotor() { }

    private void sendCharToPicaxe(char ping) {

        try {
            serialPort.getOutputStream().write(ping);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void moveOnePulseForward() {

        sendCharToPicaxe('+');
        if (Settings.getInstance().isCalibrationSet()) {
            currentAngle += Settings.getInstance().getPulseToAngleRatio();
            notifyObservers(); // currentAngleLabel.setText(String.valueOf(currentAngle));
        }
    }

    public void moveOnePulseBackwards() {

        sendCharToPicaxe('-');
        if (Settings.getInstance().isCalibrationSet()) {
            currentAngle -= Settings.getInstance().getPulseToAngleRatio();
            notifyObservers(); // currentAngleLabel.setText(String.valueOf(currentAngle));
        }
    }

    public void stepForward() {

        timeline = new Timeline(new KeyFrame(Duration.millis(IMPULSE_TIME), e -> {
            moveOnePulseForward();
        }));
        timeline.setCycleCount(Settings.getInstance().getStepSize());
        timeline.setOnFinished(finish ->{
            stopTimeline = new Timeline(new KeyFrame(Duration.millis(IMPULSE_TIME+20), e -> {
                stopMotor();
            }));
            stopTimeline.play();
        });
        timeline.play();
    }

    public void stepBackwards() {

        timeline = new Timeline(new KeyFrame(Duration.millis(IMPULSE_TIME), e -> {
            moveOnePulseBackwards();
        }));
        timeline.setCycleCount(Settings.getInstance().getStepSize());
        timeline.setOnFinished(finish ->{
            stopTimeline = new Timeline(new KeyFrame(Duration.millis(IMPULSE_TIME+20), e -> {
                stopMotor();
            }));
            stopTimeline.play();
        });
        timeline.play();
    }

    private void checkAngleRange(double angle) throws WrongParameterException {

        if (angle < 0) {
            throw new WrongParameterException("Value for move to angle should be >= 0");
        }
        Settings settings = Settings.getInstance();
        if (settings.getAngleUnits().equals("gradians") && angle > settings.GRADIANS_MAX) {
            throw new WrongParameterException("Value for move to angle should be <= " + settings.GRADIANS_MAX);
        }
        if (settings.getAngleUnits().equals("degrees") && angle > settings.DEGREES_MAX) {
            throw new WrongParameterException("Value for move to angle should be <= " + settings.DEGREES_MAX);
        }
    }

    public void moveToAngle(String angleValue) throws UnknownCurrentAngleException, WrongParameterException {

        double angle;
        if (angleValue == null) {
            throw new WrongParameterException("Value for move to angle cannot be null");
        }
        try {
            angle = Double.parseDouble(angleValue);
        } catch (NumberFormatException e) {
            throw new WrongParameterException("Value for move to angle is in wrong format");
        }
        checkAngleRange(angle);
        if (currentAngle == null) {
            throw new UnknownCurrentAngleException("Current angle is unknown");
        }
        if (currentAngle < angle) {
            timeline = new Timeline(new KeyFrame(Duration.millis(IMPULSE_TIME), e -> {
                moveOnePulseForward();
            }));
        } else {
            timeline = new Timeline(new KeyFrame(Duration.millis(IMPULSE_TIME), e -> {
                moveOnePulseBackwards();
            }));
        }
        timeline.setCycleCount(pulsesNeededToMove(angle));
        timeline.setOnFinished(finish ->{
            stopTimeline = new Timeline(new KeyFrame(Duration.millis(IMPULSE_TIME+20), e -> {
                stopMotor();
            }));
            stopTimeline.play();
        });
        timeline.play();
    }

    public void stopMotor(){
        try {
            serialPort.getOutputStream().write(MOTOR_STOP);
        } catch (IOException ioException) {
            System.out.println(ioException.getMessage());
        }
    }

    public Integer pulsesNeededToMove(double endAngle) {

        double pulseToAngleRatio = Settings.getInstance().getPulseToAngleRatio();
        double angleDiff = Math.abs(currentAngle - endAngle);
        int pulseCount1 = (int) Math.floor((angleDiff) / pulseToAngleRatio); // idem pred alebo na koncovy uhol
        int pulseCount2 = (int) Math.ceil((angleDiff) / pulseToAngleRatio); // idem za alebo na koncovy uhol
        double diff1 = Math.abs(endAngle - (currentAngle + pulseCount1 * pulseToAngleRatio));
        double diff2 = Math.abs(endAngle - (currentAngle + pulseCount2 * pulseToAngleRatio));
        return diff1 < diff2 ? pulseCount1 : pulseCount2;    // ktore je blizsie k uhlu kam chceme ist
    }

    public Integer stepsNeededToMove(double endAngle) {

        double stepToAngleRatio = getStepToAngleRatio();
        double angleDiff = Math.abs(currentAngle - endAngle);
        int stepCount1 = (int) Math.floor((angleDiff) / stepToAngleRatio); // idem pred alebo na koncovy uhol
        int stepCount2 = (int) Math.ceil((angleDiff) / stepToAngleRatio); // idem za alebo na koncovy uhol
        double diff1 = Math.abs(endAngle - (currentAngle + stepCount1 * stepToAngleRatio));
        double diff2 = Math.abs(endAngle - (currentAngle + stepCount2 * stepToAngleRatio));
        return diff1 < diff2 ? stepCount1 : stepCount2;    // ktore je blizsie k uhlu kam chceme ist
    }

    public void findPicaxe(String portName) throws PortNotFoundException {

        SerialPort[] serialPorts = SerialPort.getCommPorts();
        if (serialPorts.length == 0) {
            throw new PortNotFoundException("Ports not found");
        }
        if (serialPort != null){
            serialPort.closePort();
            serialPort = null;
        }
        for (SerialPort port : serialPorts) {
            if (port.getDescriptivePortName().contains(portName)) {
                port.openPort();
                setPortConfiguration(port);
                setPortDataListener(port);
                timeline = new Timeline(new KeyFrame(Duration.millis(1000), e -> {
                    try {
                        port.getOutputStream().write(MOTOR_CHECK_PING);
                    } catch (IOException ioException) {
                        ioException.printStackTrace();
                    }
                }));
                timeline.setCycleCount(1);
                timeline.play();
                /*
                byte[] data = new byte[MOTOR_CHECK_PING];
                port.writeBytes(data, 1);
                */
            }
        }
    }

    private void setPortDataListener(SerialPort port) {

        port.addDataListener(new SerialPortDataListener() {
            @Override
            public int getListeningEvents() { return SerialPort.LISTENING_EVENT_DATA_RECEIVED; }

            @Override
            public void serialEvent(SerialPortEvent event) {

                byte[] data = event.getReceivedData();
                if ((char) data[0] == '@') serialPort = port;
                /*
                try { // ak pride znak '@'
                    if ((char) port.getInputStream().read() == '@') serialPort = port;
                } catch (IOException e) {
                    System.out.println(e.getMessage());
                }
                */
            }
        });
    }

    private void setPortConfiguration(SerialPort port) {

        port.setBaudRate(BAUD_RATE);
        port.setNumDataBits(NUM_DATA_BITS);
        port.setNumStopBits(NUM_STOP_BITS);
        port.setParity(PARITY);
    }

    public boolean checkPicaxeConnection() {

        if (serialPort == null) {
            return false;
        }
        return serialPort.isOpen();
    }

    public double getImpulseTime() { return IMPULSE_TIME; }

    public double getStepTime() {

        return Settings.getInstance().getStepSize() * IMPULSE_TIME;
    }

    public double getStepToAngleRatio() {

        return Settings.getInstance().getStepSize() * Settings.getInstance().getPulseToAngleRatio();
    }

    @Override
    public void attach(Observer observer) { observers.add(observer); }

    @Override
    public void notifyObservers() {

        for (Observer observer : observers) {
            observer.update();
        }
    }

    @Override
    public void detach(Observer observer) { observers.remove(observer); }
}
