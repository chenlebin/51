package serialPort;

import java.awt.Color;
import java.awt.Font;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.TooManyListenersException;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.border.TitledBorder;

import gnu.io.NoSuchPortException;
import gnu.io.PortInUseException;
import gnu.io.SerialPort;
import gnu.io.SerialPortEvent;
import gnu.io.SerialPortEventListener;
import gnu.io.UnsupportedCommOperationException;

/**
 * 监测数据显示类
 * @author Zhong
 *
 */
public class SerialView extends JFrame {

	/**
	 */
	private static final long serialVersionUID = 1L;

	//设置window的icon
	Toolkit toolKit = getToolkit();
	Image icon = toolKit.getImage(SerialView.class.getResource("computer.png"));
	DateTimeFormatter df= DateTimeFormatter.ofPattern("YYYY-MM-dd HH:mm:ss.SSS");

	private JComboBox<String> commChoice;
	private JComboBox<String> bpsChoice;
	private JButton openSerialButton;
	private JButton sendButton;
	private JTextArea sendArea;
	private JTextArea receiveArea;
	private JButton closeSerialButton;

	private List<String> commList = null;	//保存可用端口号
	private SerialPort serialPort = null;	//保存串口对象

	/**类的构造方法
	 * @param client
	 */
	public SerialView() {

		init();
		TimerTask task = new TimerTask() {  
			@Override  
			public void run() {  
				commList = SerialTool.findPort(); //程序初始化时就扫描一次有效串口
				//检查是否有可用串口，有则加入选项中
				if (commList == null || commList.size()<1) {
					JOptionPane.showMessageDialog(null, "没有搜索到有效串口！", "错误", JOptionPane.INFORMATION_MESSAGE);
				}else{
					commChoice.removeAllItems();
					for (String s : commList) {
						commChoice.addItem(s);
					}
				}
			}
		};
		Timer timer = new Timer();  
		timer.scheduleAtFixedRate(task, 0, 10000);
		listen();

	}
	/**
	 */
	private void listen(){

		//打开串口连接
		openSerialButton.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				//获取串口名称
				String commName = (String) commChoice.getSelectedItem();			
				//获取波特率
				String bpsStr = (String) bpsChoice.getSelectedItem();
				//检查串口名称是否获取正确
				if (commName == null || commName.equals("")) {
					JOptionPane.showMessageDialog(null, "没有搜索到有效串口！", "错误", JOptionPane.INFORMATION_MESSAGE);			
				}else {
					//检查波特率是否获取正确
					if (bpsStr == null || bpsStr.equals("")) {
						JOptionPane.showMessageDialog(null, "波特率获取错误！", "错误", JOptionPane.INFORMATION_MESSAGE);
					}else {
						//串口名、波特率均获取正确时
						int bps = Integer.parseInt(bpsStr);
						try {
							//获取指定端口名及波特率的串口对象
							serialPort = SerialTool.openPort(commName, bps);
							SerialTool.addListener(serialPort, new SerialListener());
							if(serialPort==null) return;
							//在该串口对象上添加监听器
							closeSerialButton.setEnabled(true);
							sendButton.setEnabled(true);
							openSerialButton.setEnabled(false);
							String time=df.format(LocalDateTime.ofInstant(Instant.ofEpochMilli(System.currentTimeMillis()),ZoneId.of("Asia/Shanghai")));
							receiveArea.append(time+" ["+serialPort.getName().split("/")[3]+"] : "+" 连接成功..."+"\r\n");
							receiveArea.setCaretPosition(receiveArea.getText().length());  
						} catch (UnsupportedCommOperationException | PortInUseException | NoSuchPortException | TooManyListenersException e1) {
							e1.printStackTrace();
						}
					}
				}
			}
		});
		//发送数据
		sendButton.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if(!sendButton.isEnabled())return;
				String message= sendArea.getText();
				//"FE0400030001D5C5"
				try {
					SerialTool.sendToPort(serialPort, hex2byte(message));
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
		});
		//关闭串口连接
		closeSerialButton.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if(!closeSerialButton.isEnabled())return;
				SerialTool.closePort(serialPort);
				String time=df.format(LocalDateTime.ofInstant(Instant.ofEpochMilli(System.currentTimeMillis()),ZoneId.of("Asia/Shanghai")));
				receiveArea.append(time+" ["+serialPort.getName().split("/")[3]+"] : "+" 断开连接"+"\r\n");
				receiveArea.setCaretPosition(receiveArea.getText().length());  
				openSerialButton.setEnabled(true);
				closeSerialButton.setEnabled(false);
				sendButton.setEnabled(false);
			}
		});
	}
	/**
	 * 主菜单窗口显示；
	 * 添加JLabel、按钮、下拉条及相关事件监听；
	 */
	private void init() {

		this.setBounds(WellcomView.LOC_X, WellcomView.LOC_Y, WellcomView.WIDTH, WellcomView.HEIGHT);
		this.setTitle("串口调试");
		this.setIconImage(icon);
		this.setBackground(Color.gray);
		this.setLayout(null);

		Font font =new Font("微软雅黑", Font.BOLD, 16);

		receiveArea=new JTextArea(18, 30);
		receiveArea.setEditable(false);
		JScrollPane receiveScroll = new JScrollPane(receiveArea);
		receiveScroll.setBorder(new TitledBorder("接收区"));
		//滚动条自动出现 FE0400030001D5C5
		receiveScroll.setHorizontalScrollBarPolicy( 
				JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED); 
		receiveScroll.setVerticalScrollBarPolicy( 
				JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED); 
		receiveScroll.setBounds(52, 20, 680,340);
		this.add(receiveScroll);

		JLabel chuankou=new JLabel(" 串口选择： ");
		chuankou.setFont(font);
		chuankou.setBounds(50, 380, 100,50);
		this.add(chuankou);

		JLabel botelv=new JLabel(" 波 特 率： ");
		botelv.setFont(font);
		botelv.setBounds(290, 380, 100,50);
		this.add(botelv);

		//添加串口选择选项
		commChoice = new JComboBox<String>();	//串口选择（下拉框）
		commChoice.setBounds(145, 390, 100, 30);
		this.add(commChoice);

		//添加波特率选项
		bpsChoice = new JComboBox<String>();	//波特率选择
		bpsChoice.setBounds(380, 390, 100, 30);
		bpsChoice.addItem("1500");
		bpsChoice.addItem("2400");
		bpsChoice.addItem("4800");
		bpsChoice.addItem("9600");
		bpsChoice.addItem("14400");
		bpsChoice.addItem("19500");
		bpsChoice.addItem("115500");
		this.add(bpsChoice);

		//添加打开串口按钮
		openSerialButton = new JButton("连接");
		openSerialButton.setBounds(540, 390, 80, 30);
		openSerialButton.setFont(font);
		openSerialButton.setForeground(Color.darkGray);
		this.add(openSerialButton);

		//添加关闭串口按钮
		closeSerialButton = new JButton("关闭");
		closeSerialButton.setEnabled(false);
		closeSerialButton.setBounds(650, 390, 80, 30);
		closeSerialButton.setFont(font);
		closeSerialButton.setForeground(Color.darkGray);
		this.add(closeSerialButton);

		sendArea=new JTextArea(30,20);
		JScrollPane sendScroll = new JScrollPane(sendArea);
		sendScroll.setBorder(new TitledBorder("发送区"));
		//滚动条自动出现
		sendScroll.setHorizontalScrollBarPolicy( 
				JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS); 
		sendScroll.setVerticalScrollBarPolicy( 
				JScrollPane.VERTICAL_SCROLLBAR_ALWAYS); 
		sendScroll.setBounds(52, 450, 500,100);
		this.add(sendScroll);

		sendButton = new JButton("发 送");
		sendButton.setBounds(610, 520, 120, 30);
		sendButton.setFont(font);
		sendButton.setForeground(Color.darkGray);
		sendButton.setEnabled(false);
		this.add(sendButton);

		this.setResizable(false);	//窗口大小不可更改
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setVisible(true);
	}

	/**字符串转16进制
	 * @param hex
	 * @return
	 */
	private byte[] hex2byte(String hex) {
		
		String digital = "0123456789ABCDEF";
		String hex1 = hex.replace(" ", "");
		char[] hex2char = hex1.toCharArray();
		byte[] bytes = new byte[hex1.length() / 2];
		byte temp;
		for (int p = 0; p < bytes.length; p++) {
			temp = (byte) (digital.indexOf(hex2char[2 * p]) * 16);
			temp += digital.indexOf(hex2char[2 * p + 1]);
			bytes[p] = (byte) (temp & 0xff);
		}
		return bytes;
	}
	/**字节数组转16进制
	 * @param b
	 * @return
	 */
	private String printHexString(byte[] b) {

		StringBuffer sbf=new StringBuffer();
		for (int i = 0; i < b.length; i++) { 
			String hex = Integer.toHexString(b[i] & 0xFF); 
			if (hex.length() == 1) { 
				hex = '0' + hex; 
			} 
			sbf.append(hex.toUpperCase()+"  ");
		}
		return sbf.toString().trim();
	}
	/**
	 * 以内部类形式创建一个串口监听类
	 * @author zhong
	 */
	class SerialListener implements SerialPortEventListener {

		/**
		 * 处理监控到的串口事件
		 */
		public void serialEvent(SerialPortEvent serialPortEvent) {

			switch (serialPortEvent.getEventType()) {
			case SerialPortEvent.BI: // 10 通讯中断
				JOptionPane.showMessageDialog(null, "与串口设备通讯中断", "错误", JOptionPane.INFORMATION_MESSAGE);
				break;
			case SerialPortEvent.OE: // 7 溢位（溢出）错误
				break;
			case SerialPortEvent.FE: // 9 帧错误
				break;
			case SerialPortEvent.PE: // 8 奇偶校验错误
				break;
			case SerialPortEvent.CD: // 6 载波检测
				break;
			case SerialPortEvent.CTS: // 3 清除待发送数据
				break;
			case SerialPortEvent.DSR: // 4 待发送数据准备好了
				break;
			case SerialPortEvent.RI: // 5 振铃指示
				break;
			case SerialPortEvent.OUTPUT_BUFFER_EMPTY: // 2 输出缓冲区已清空
				break;
			case SerialPortEvent.DATA_AVAILABLE: // 1 串口存在可用数据
				String time=df.format(LocalDateTime.ofInstant(Instant.ofEpochMilli(System.currentTimeMillis()),ZoneId.of("Asia/Shanghai")));
				byte[] data;//FE0400030001D5C5
				try {
					data = SerialTool.readFromPort(serialPort);
					receiveArea.append(time+" ["+serialPort.getName().split("/")[3]+"] : "+ printHexString(data)+"\r\n");
					receiveArea.setCaretPosition(receiveArea.getText().length());  
				} catch (IOException e) {
					e.printStackTrace();
				}
				break;
			default:
				break;
			}
		}
	}
}
