package serialCommunication;

import Exceptions.SerialCommunicationExceptions.PicaxeConnectionErrorException;
import Exceptions.SerialCommunicationExceptions.PortNotFoundException;
import com.fazecast.jSerialComm.SerialPort;
import com.fazecast.jSerialComm.SerialPortDataListener;
import com.fazecast.jSerialComm.SerialPortEvent;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;
import settings.Settings;

public class StepperMotor {

    private SerialPort serialPort = null;
    private final int impulseTime = 5; // cas jedneho impulzu
    private final int pauseBetweenImpulses = 10; // aby sa neroztocil prilis rychlo

    public void stepForward() throws InterruptedException, PicaxeConnectionErrorException {

        if (!checkPicaxeConnection())
            throw new PicaxeConnectionErrorException("Picaxe connection error");

        byte[] data = new byte['+'];
        /*
        for (int i = 0; i < Settings.getStepSize(); i++) {
            Thread.sleep(impulseTime + pauseBetweenImpulses);
            serialPort.writeBytes(data, 1);
        }
        */
        Timeline tmp = new Timeline(new KeyFrame(Duration.millis(impulseTime + pauseBetweenImpulses), e -> {
            serialPort.writeBytes(data, 1);
        }));
        tmp.setCycleCount(Settings.getStepSize());
        tmp.play();
    }

    public void stepBackwards() throws InterruptedException, PicaxeConnectionErrorException {

        if (!checkPicaxeConnection())
            throw new PicaxeConnectionErrorException("Picaxe connection error");

        byte[] data = new byte['-'];
        /*
        for (int i = 0; i < Settings.getStepSize(); i++) {
            Thread.sleep(impulseTime + pauseBetweenImpulses);
            serialPort.writeBytes(data, 1);
        }
        */
        Timeline tmp = new Timeline(new KeyFrame(Duration.millis(impulseTime + pauseBetweenImpulses), e -> {
            serialPort.writeBytes(data, 1);
        }));
        tmp.setCycleCount(Settings.getStepSize());
        tmp.play();
    }

    public Double moveToAngle(Double angle) {

        return 0.0; // return kam sa pohol ( neda sa vzdy ist presne na uhol )
    }

    public Integer stepsNeededToMove(Double startAngle, Double endAngle) {

        Integer stepsToDo = 0;
        Double angleDiff = Math.abs(startAngle - endAngle);
        Integer stepCount1 = (int) Math.floor((angleDiff) / getStepToAngleRatio()); // idem pred alebo na koncovy uhol
        Integer stepCount2 = (int) Math.ceil((angleDiff) / getStepToAngleRatio()); // idem za alebo na koncovy uhol
        Double diff1 = Math.abs(endAngle - stepCount1 * getStepToAngleRatio());
        Double diff2 = Math.abs(endAngle - stepCount2 * getStepToAngleRatio());

        stepsToDo = diff1 < diff2 ? stepCount1 : stepCount2;    // ktore je blizsie k uhlu kam chceme ist
        return stepsToDo;
    }

    public void findPicaxe() throws PortNotFoundException {

        SerialPort[] serialPorts = SerialPort.getCommPorts();

        if (serialPorts.length == 0)
            throw new PortNotFoundException("Port not found");

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
                    if ((char) data[0] == '@')
                        serialPort = port;
                }
            });
        }
    }

    public boolean checkPicaxeConnection() {

        if (serialPort == null)
            return false;
        return serialPort.isOpen();
        // return serialPort.openPort( (int)sleepTime );
    }

    public void sendPingToPicaxe() throws PicaxeConnectionErrorException { // mozno nepotrebujeme

        if (!checkPicaxeConnection())
            throw new PicaxeConnectionErrorException("Picaxe connection error");

        byte[] data = new byte['!'];
        serialPort.writeBytes(data, 1);
    }

    public void sendPingToPort(SerialPort port) { // mozno nepotrebujeme

        byte[] data = new byte['!'];
        port.writeBytes(data, 1);
    }

    public StepperMotor() throws PortNotFoundException {

        findPicaxe();
    }

    public double getImpulseTime() {

        return impulseTime;
    }

    public double getStepTime() {

        return Settings.getStepSize() * impulseTime + (Settings.getStepSize() - 1) * pauseBetweenImpulses;
        // return Settings.getStepSize() * impulseTime + (Settings.getStepSize()) * pauseBetweenImpulses;
    }

    public double getStepToAngleRatio() {

        return Settings.getStepSize() * Settings.getPulseToAngleRatio();
    }
}
