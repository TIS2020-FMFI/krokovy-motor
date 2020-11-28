package serialCommunication;

import Exceptions.SerialCommunicationExceptions.PicaxeConnectionErrorException;
import Exceptions.SerialCommunicationExceptions.PortNotFoundException;
import Exceptions.SerialCommunicationExceptions.UnknownCurrentAngleException;
import com.fazecast.jSerialComm.SerialPort;
import com.fazecast.jSerialComm.SerialPortDataListener;
import com.fazecast.jSerialComm.SerialPortEvent;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;
import settings.Settings;

public class StepperMotor {

    public Double currentAngle = null;
    private SerialPort serialPort = null;
    private final int impulseTime = 50; //zahrna aj pauzu medzi impulzmi
    Timeline timeline;
    byte[] forwardSign = new byte['+'];
    byte[] backwardsSign = new byte['-'];

    public StepperMotor() { }

    public void moveOnePulseForward(){
        serialPort.writeBytes(forwardSign, 1);
        currentAngle += Settings.getPulseToAngleRatio();
    }

    public void moveOnePulseBackwards(){
        serialPort.writeBytes(backwardsSign, 1);
        currentAngle -= Settings.getPulseToAngleRatio();
    }

    public void stepForward(){
        /*if (!checkPicaxeConnection()) {
            throw new PicaxeConnectionErrorException("Picaxe connection error");
        }*/

        timeline = new Timeline(new KeyFrame(Duration.millis(impulseTime), e -> {
            moveOnePulseForward();
        }));
        timeline.setCycleCount(Settings.getStepSize());
        timeline.play();
    }

    public void stepBackwards() {
        /*if (!checkPicaxeConnection()) {
            throw new PicaxeConnectionErrorException("Picaxe connection error");
        }*/

        timeline = new Timeline(new KeyFrame(Duration.millis(impulseTime), e -> {
            moveOnePulseBackwards();
        }));
        timeline.setCycleCount(Settings.getStepSize());
        timeline.play();
    }

    public void moveToAngle(double angle) throws UnknownCurrentAngleException {
        if (currentAngle == null) {
            throw new UnknownCurrentAngleException("Current angle is unknown");
        }

        if  (currentAngle < angle) {
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
        double pulseToAngleRatio = Settings.getPulseToAngleRatio();

        double angleDiff = Math.abs(currentAngle - endAngle);
        int pulseCount1 = (int) Math.floor((angleDiff) / pulseToAngleRatio); // idem pred alebo na koncovy uhol
        int pulseCount2 = (int) Math.ceil((angleDiff) / pulseToAngleRatio); // idem za alebo na koncovy uhol
        double diff1 = Math.abs(endAngle - pulseCount1 * pulseToAngleRatio);
        double diff2 = Math.abs(endAngle - pulseCount2 * pulseToAngleRatio);

        return diff1 < diff2 ? pulseCount1 : pulseCount2;    // ktore je blizsie k uhlu kam chceme ist
    }

    public Integer stepsNeededToMove(double endAngle) {
        double angleDiff = Math.abs(currentAngle - endAngle);
        int stepCount1 = (int) Math.floor((angleDiff) / getStepToAngleRatio()); // idem pred alebo na koncovy uhol
        int stepCount2 = (int) Math.ceil((angleDiff) / getStepToAngleRatio()); // idem za alebo na koncovy uhol
        double diff1 = Math.abs(endAngle - stepCount1 * getStepToAngleRatio());
        double diff2 = Math.abs(endAngle - stepCount2 * getStepToAngleRatio());

        return diff1 < diff2 ? stepCount1 : stepCount2;    // ktore je blizsie k uhlu kam chceme ist
    }

    public void findPicaxe() throws PortNotFoundException {
        SerialPort[] serialPorts = SerialPort.getCommPorts();

        if (serialPorts.length == 0) {
            throw new PortNotFoundException("Port not found");
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

        Timeline tmp = new Timeline(new KeyFrame(Duration.millis(2000), e -> { }));
        tmp.setCycleCount(1);
        tmp.setOnFinished(e -> {
            if (checkPicaxeConnection()) {
                // Picaxe bol najdeny
            } else {
                // Picaxe nebol najdeny
            }
        });
        tmp.play();
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
        // return Settings.getStepSize() * impulseTime + (Settings.getStepSize() - 1) * pauseBetweenImpulses;
    }

    public double getStepToAngleRatio() {
        return Settings.getStepSize() * Settings.getPulseToAngleRatio();
    }
}
