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

/**串口服务类，提供打开、关闭串口，读取、发送串口数据等服务
 */
public class SerialTool {

	private static SerialTool serialTool = null;

	static {
		//在该类被ClassLoader加载时就初始化一个SerialTool对象
		if (serialTool == null) {
			serialTool = new SerialTool();
		}
	}

	//私有化SerialTool类的构造方法，不允许其他类生成SerialTool对象
	private SerialTool() {}	
	/**
	 * 获取提供服务的SerialTool对象
	 * @return serialTool
	 */
	public static SerialTool getSerialTool() {

		if (serialTool == null) {
			serialTool = new SerialTool();
		}
		return serialTool;
	}
	/**
	 * 查找所有可用端口
	 * @return 可用端口名称列表
	 */
	public static final List<String> findPort() {

		//获得当前所有可用串口
		@SuppressWarnings("unchecked")
		Enumeration<CommPortIdentifier> portList = CommPortIdentifier.getPortIdentifiers();	
		List<String> portNameList = new ArrayList<>();
		//将可用串口名添加到List并返回该List
		while (portList.hasMoreElements()) {
			String portName = portList.nextElement().getName();
			portNameList.add(portName);
		}
		return portNameList;
	}
	/**
	 * 打开串口
	 * @param portName 端口名称
	 * @param baudrate 波特率
	 * @return 串口对象
	 * @throws UnsupportedCommOperationException
	 * @throws PortInUseException
	 * @throws NoSuchPortException
	 */
	public static final SerialPort openPort(String portName, int baudrate) throws UnsupportedCommOperationException, PortInUseException, NoSuchPortException {

		//通过端口名识别端口
		CommPortIdentifier portIdentifier = CommPortIdentifier.getPortIdentifier(portName);
		//打开端口，并给端口名字和一个timeout（打开操作的超时时间）
		CommPort commPort = portIdentifier.open(portName, 2000);
		//判断是不是串口
		if (commPort instanceof SerialPort) {
			SerialPort serialPort = (SerialPort) commPort;
			//设置一下串口的波特率等参数
			serialPort.setSerialPortParams(baudrate, SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);                              
			return serialPort;
		}
		return null;
	}
	/**
	 * 关闭串口
	 * @param serialport 待关闭的串口对象
	 */
	public static void closePort(SerialPort serialPort) {

		if (serialPort != null) {
			serialPort.close();
			serialPort = null;
		}
	}
	/**
	 * 往串口发送数据
	 * @param serialPort 串口对象
	 * @param order	待发送数据
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
	 * 从串口读取数据
	 * @param serialPort 当前已建立连接的SerialPort对象
	 * @return 读取到的数据
	 * @throws IOException 
	 */
	public static byte[] readFromPort(SerialPort serialPort) throws IOException {

		InputStream in = null;
		byte[] bytes = null;
		try {
			in = serialPort.getInputStream();
			int bufflenth = in.available();	//获取buffer里的数据长度
			while (bufflenth != 0) {                             
				bytes = new byte[bufflenth]; //初始化byte数组为buffer中数据的长度
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
	/**添加监听器
	 * @param port     串口对象
	 * @param listener 串口监听器
	 * @throws TooManyListenersException 
	 */
	public static void addListener(SerialPort port, SerialPortEventListener listener) throws TooManyListenersException {

		//给串口添加监听器
		port.addEventListener(listener);
		//设置当有数据到达时唤醒监听接收线程
		port.notifyOnDataAvailable(true);
		//设置当通信中断时唤醒中断线程
		port.notifyOnBreakInterrupt(true);
	}
}
