import Exceptions.SerialCommunicationExceptions.PortNotFoundException;
import com.fazecast.jSerialComm.SerialPort;

import java.io.IOException;
import java.util.Scanner;

public class TestMotora {
    static SerialPort serialPort;
    static byte[] forwardSign = new byte['+'];

    public static void findPicaxe() throws PortNotFoundException {

        SerialPort[] serialPorts = SerialPort.getCommPorts();


        if (serialPorts.length == 0) {
            throw new PortNotFoundException("Ports not found");
        }

        for (SerialPort port : serialPorts) {
            String portName = port.getDescriptivePortName().toUpperCase();
            System.out.println(portName);
            if (portName.contains("COM1")) {
                serialPort = port;
                serialPort.openPort(500);
                serialPort.setBaudRate(9600);
                serialPort.setNumDataBits(8);
                serialPort.setNumStopBits(1);
                serialPort.setParity(0);
            }

        }
    }


    public static void moveOnePulseForward() {
        System.out.println("pohyb dopredu");
        try {
            serialPort.getOutputStream().write('+');
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
//        serialPort.writeBytes(forwardSign, 1);
    }

    public static void moveOnePulseBackwards() {
        System.out.println("pohyb dozadu");
        try {
            serialPort.getOutputStream().write('-');
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
//        serialPort.writeBytes(new byte['-'], 1);
    }

    public static void sendPing() {
        System.out.println("ping");
        serialPort.writeBytes(new byte['!'], 1);
    }

    public static void main(String[] args) {

        try {
            findPicaxe();
        } catch (PortNotFoundException e) {
            e.printStackTrace();
        }

        Scanner scanner = new Scanner(System.in);
        while (true) {
            String s = scanner.next();
            if (s.equals("+")) {
                System.out.println("program precital +");
                moveOnePulseForward();
            }
            if (s.equals("-")) {
                System.out.println("program precital -");
                moveOnePulseBackwards();
            }
            if (s.equals("!")) {
                System.out.println("program precital !");
                sendPing();
            }
        }


    }
}
