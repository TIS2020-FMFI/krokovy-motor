package serialCommunication;
import Exceptions.SerialCommunicationExceptions.PicaxeConnectionErrorException;
import com.fazecast.jSerialComm.SerialPort;
import com.fazecast.jSerialComm.SerialPortDataListener;import com.fazecast.jSerialComm.SerialPortEvent;

import settings.Settings;

public class StepperMotor {

    private final int impulseTime = 5; //cas jedneho impulzu
    private final int pauseBetweenImpulses = 10; //aby sa neroztocil prilis rychlo

    private SerialPort serialPort = null;

    public void stepForward() throws InterruptedException, PicaxeConnectionErrorException {

        if (!checkPicaxeConnection())
            throw new PicaxeConnectionErrorException("Picaxe connection error");

        byte[] data = new byte['+'];
        for ( int i = 0; i < Settings.getStepSize(); i++ ) {
            Thread.sleep(impulseTime + pauseBetweenImpulses);
            serialPort.writeBytes(data, 1);
        }
    }

    public void stepBackwards() throws InterruptedException, PicaxeConnectionErrorException {

        if (!checkPicaxeConnection())
            throw new PicaxeConnectionErrorException("Picaxe connection error");

        byte[] data = new byte['+'];
        for ( int i = 0; i < Settings.getStepSize(); i++ ) {
            Thread.sleep(impulseTime + pauseBetweenImpulses);
            serialPort.writeBytes(data, 1);
        }
    }

    public Double moveToAngle(Double angle) { //TODO

        return 0.0; //return kam sa pohol (neda sa vzdy ist presne na uhol)
    }

    public Integer stepsNeededToMove(Double startAngle, Double endAngle){
        Integer stepsToDo = 0;
        Double angleDiff = Math.abs(startAngle - endAngle);
        Integer stepCount1 = (int) Math.floor((angleDiff) / getStepToAngleRatio()); //idem pred alebo na koncovy uhol
        Integer stepCount2 = (int) Math.ceil((angleDiff) / getStepToAngleRatio()); //idem za alebo na koncovy uhol
        Double diff1 = Math.abs(endAngle - stepCount1 * getStepToAngleRatio());
        Double diff2 = Math.abs(endAngle - stepCount2 * getStepToAngleRatio());
        stepsToDo = diff1 < diff2 ? stepCount1 : stepCount2;    //ktore je blizsie k uhlu kam chceme ist
        return stepsToDo;
    }

    public void findPicaxe(){

        SerialPort[] serialPorts = SerialPort.getCommPorts();

        for (SerialPort port : serialPorts) {
            port.openPort();
            port.addDataListener(new SerialPortDataListener() {
                @Override
                public int getListeningEvents() { return SerialPort.LISTENING_EVENT_DATA_RECEIVED; }
                @Override
                public void serialEvent(SerialPortEvent event)
                {
                    byte[] data = event.getReceivedData();
                    if ( (char)data[0] == '@' )
                        serialPort = port;
                }
            });
        }
    }

    public boolean checkPicaxeConnection() {

        if ( serialPort == null )
            return false;
        return serialPort.isOpen();
        //return serialPort.openPort( (int)sleepTime );
    }

    public boolean sendPingToPort( SerialPort port ) { // mozno nepotrebujeme
        throw new UnsupportedOperationException("Not implemented yet.");
    }

    public StepperMotor() {
        findPicaxe();
    }

    public double getImpulseTime() {
        return impulseTime;
    }

    public double getStepTime(){
        return Settings.getStepSize() * impulseTime + (Settings.getStepSize() /* - 1 */) * pauseBetweenImpulses;
    }

    public double getStepToAngleRatio(){
        return Settings.getStepSize() * Settings.getPulseToAngleRatio();
    }
}
