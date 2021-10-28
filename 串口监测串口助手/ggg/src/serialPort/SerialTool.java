package serialPort;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.TooManyListenersException;

import gnu.io.CommPort;
import gnu.io.CommPortIdentifier;
import gnu.io.NoSuchPortException;
import gnu.io.PortInUseException;
import gnu.io.SerialPort;
import gnu.io.SerialPortEventListener;
import gnu.io.UnsupportedCommOperationException;

/**���ڷ����࣬�ṩ�򿪡��رմ��ڣ���ȡ�����ʹ������ݵȷ���
 */
public class SerialTool {

	private static SerialTool serialTool = null;

	static {
		//�ڸ��౻ClassLoader����ʱ�ͳ�ʼ��һ��SerialTool����
		if (serialTool == null) {
			serialTool = new SerialTool();
		}
	}

	//˽�л�SerialTool��Ĺ��췽��������������������SerialTool����
	private SerialTool() {}	
	/**
	 * ��ȡ�ṩ�����SerialTool����
	 * @return serialTool
	 */
	public static SerialTool getSerialTool() {

		if (serialTool == null) {
			serialTool = new SerialTool();
		}
		return serialTool;
	}
	/**
	 * �������п��ö˿�
	 * @return ���ö˿������б�
	 */
	public static final List<String> findPort() {

		//��õ�ǰ���п��ô���
		@SuppressWarnings("unchecked")
		Enumeration<CommPortIdentifier> portList = CommPortIdentifier.getPortIdentifiers();	
		List<String> portNameList = new ArrayList<>();
		//�����ô�������ӵ�List�����ظ�List
		while (portList.hasMoreElements()) {
			String portName = portList.nextElement().getName();
			portNameList.add(portName);
		}
		return portNameList;
	}
	/**
	 * �򿪴���
	 * @param portName �˿�����
	 * @param baudrate ������
	 * @return ���ڶ���
	 * @throws UnsupportedCommOperationException
	 * @throws PortInUseException
	 * @throws NoSuchPortException
	 */
	public static final SerialPort openPort(String portName, int baudrate) throws UnsupportedCommOperationException, PortInUseException, NoSuchPortException {

		//ͨ���˿���ʶ��˿�
		CommPortIdentifier portIdentifier = CommPortIdentifier.getPortIdentifier(portName);
		//�򿪶˿ڣ������˿����ֺ�һ��timeout���򿪲����ĳ�ʱʱ�䣩
		CommPort commPort = portIdentifier.open(portName, 2000);
		//�ж��ǲ��Ǵ���
		if (commPort instanceof SerialPort) {
			SerialPort serialPort = (SerialPort) commPort;
			//����һ�´��ڵĲ����ʵȲ���
			serialPort.setSerialPortParams(baudrate, SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);                              
			return serialPort;
		}
		return null;
	}
	/**
	 * �رմ���
	 * @param serialport ���رյĴ��ڶ���
	 */
	public static void closePort(SerialPort serialPort) {

		if (serialPort != null) {
			serialPort.close();
			serialPort = null;
		}
	}
	/**
	 * �����ڷ�������
	 * @param serialPort ���ڶ���
	 * @param order	����������
	 * @throws IOException 
	 */
	public static void sendToPort(SerialPort serialPort, byte[] order) throws IOException  {

		OutputStream out = null;
		out = serialPort.getOutputStream();
		out.write(order);
		out.flush();
		out.close();
	}
	/**
	 * �Ӵ��ڶ�ȡ����
	 * @param serialPort ��ǰ�ѽ������ӵ�SerialPort����
	 * @return ��ȡ��������
	 * @throws IOException 
	 */
	public static byte[] readFromPort(SerialPort serialPort) throws IOException {

		InputStream in = null;
		byte[] bytes = null;
		try {
			in = serialPort.getInputStream();
			int bufflenth = in.available();	//��ȡbuffer������ݳ���
			while (bufflenth != 0) {                             
				bytes = new byte[bufflenth]; //��ʼ��byte����Ϊbuffer�����ݵĳ���
				in.read(bytes);
				bufflenth = in.available();
			} 
		} catch (IOException e) {
			throw e;
		} finally {
			if (in != null) {
				in.close();
				in = null;
			}
		}
		return bytes;
	}
	/**��Ӽ�����
	 * @param port     ���ڶ���
	 * @param listener ���ڼ�����
	 * @throws TooManyListenersException 
	 */
	public static void addListener(SerialPort port, SerialPortEventListener listener) throws TooManyListenersException {

		//��������Ӽ�����
		port.addEventListener(listener);
		//���õ������ݵ���ʱ���Ѽ��������߳�
		port.notifyOnDataAvailable(true);
		//���õ�ͨ���ж�ʱ�����ж��߳�
		port.notifyOnBreakInterrupt(true);
	}
}
