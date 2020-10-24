import com.fazecast.jSerialComm.SerialPort;
import com.fazecast.jSerialComm.SerialPortDataListener;
import com.fazecast.jSerialComm.SerialPortEvent;

public class jSerialComm {

    public static String devicePortName = "USB-SERIAL CH340"; //nazov portuPreZariadenie
    public static SerialPort serialPort = null;               //otvoreny port
    public static byte[] byteBuffer = new byte['#'];          //posielany char
    public static byte[] newData = null;

    public static void main(String[] args) {
        SerialPort[] serialPorts = SerialPort.getCommPorts();

        for (int i = 0; i < serialPorts.length; i++) {
            String portName = serialPorts[i].getDescriptivePortName();

            if (portName.contains(devicePortName)) {
                serialPort = serialPorts[i];
                serialPort.openPort();
                System.out.println("connected to: " + portName);
                break;
            }
            else System.out.println("connecting");
        }
        if(serialPort != null) {
            serialPort.addDataListener(new SerialPortDataListener() {
                @Override
                public int getListeningEvents() {
                    return SerialPort.LISTENING_EVENT_DATA_RECEIVED;
                }

                @Override
                public void serialEvent(SerialPortEvent event) {
                    newData = event.getReceivedData();
                    System.out.println((char) newData[0]);
                    //TOTO JE LEN NA TEST
                    if((char)newData[0] == '#') {
                        byteBuffer[0] = '!';
                        serialPort.writeBytes(byteBuffer, 1);
                        byteBuffer[0] = '+';
                        serialPort.writeBytes(byteBuffer, 1);
                        byteBuffer[0] = '!';
                        serialPort.writeBytes(byteBuffer, 1);
                    }
                    //TO JE LEN NA TEST
                }
            });
        }
        else System.out.println("device is not connected");
    }
}
