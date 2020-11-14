package serialCommunication;
//import com.fazecast.jSerialComm.SerialPort;
//import com.fazecast.jSerialComm.SerialPortDataListener;
//import com.fazecast.jSerialComm.SerialPortEvent;

import settings.Settings;

public class StepperMotor {

    private final double impulseTime = 5; //cas jedneho impulzu
    private final double pauseBetweenImpulses = 10; //aby sa neroztocil prilis rychlo

    public void stepForward(){}

    public void stepBackwards(){}

    public Double moveToAngle(Double angle){ //TODO

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

    public boolean findPicaxe(){
        throw new UnsupportedOperationException("Not implemented yet.");
    }

    public boolean sendPingToPicaxe(){
        throw new UnsupportedOperationException("Not implemented yet.");
    }

    public StepperMotor() {
    }

    public double getImpulseTime() {
        return impulseTime;
    }

    public double getStepTime(){
        return Settings.getStepSize() * impulseTime + (Settings.getStepSize() - 1) * pauseBetweenImpulses;
    }

    public double getStepToAngleRatio(){
        return Settings.getStepSize() * Settings.getPulseToAngleRatio();
    }
}
