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
 * ���������ʾ��
 * @author Zhong
 *
 */
public class SerialView extends JFrame {

	/**
	 */
	private static final long serialVersionUID = 1L;

	//����window��icon
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

	private List<String> commList = null;	//������ö˿ں�
	private SerialPort serialPort = null;	//���洮�ڶ���

	/**��Ĺ��췽��
	 * @param client
	 */
	public SerialView() {

		init();
		TimerTask task = new TimerTask() {  
			@Override  
			public void run() {  
				commList = SerialTool.findPort(); //�����ʼ��ʱ��ɨ��һ����Ч����
				//����Ƿ��п��ô��ڣ��������ѡ����
				if (commList == null || commList.size()<1) {
					JOptionPane.showMessageDialog(null, "û����������Ч���ڣ�", "����", JOptionPane.INFORMATION_MESSAGE);
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

		//�򿪴�������
		openSerialButton.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				//��ȡ��������
				String commName = (String) commChoice.getSelectedItem();			
				//��ȡ������
				String bpsStr = (String) bpsChoice.getSelectedItem();
				//��鴮�������Ƿ��ȡ��ȷ
				if (commName == null || commName.equals("")) {
					JOptionPane.showMessageDialog(null, "û����������Ч���ڣ�", "����", JOptionPane.INFORMATION_MESSAGE);			
				}else {
					//��鲨�����Ƿ��ȡ��ȷ
					if (bpsStr == null || bpsStr.equals("")) {
						JOptionPane.showMessageDialog(null, "�����ʻ�ȡ����", "����", JOptionPane.INFORMATION_MESSAGE);
					}else {
						//�������������ʾ���ȡ��ȷʱ
						int bps = Integer.parseInt(bpsStr);
						try {
							//��ȡָ���˿����������ʵĴ��ڶ���
							serialPort = SerialTool.openPort(commName, bps);
							SerialTool.addListener(serialPort, new SerialListener());
							if(serialPort==null) return;
							//�ڸô��ڶ�������Ӽ�����
							closeSerialButton.setEnabled(true);
							sendButton.setEnabled(true);
							openSerialButton.setEnabled(false);
							String time=df.format(LocalDateTime.ofInstant(Instant.ofEpochMilli(System.currentTimeMillis()),ZoneId.of("Asia/Shanghai")));
							receiveArea.append(time+" ["+serialPort.getName().split("/")[3]+"] : "+" ���ӳɹ�..."+"\r\n");
							receiveArea.setCaretPosition(receiveArea.getText().length());  
						} catch (UnsupportedCommOperationException | PortInUseException | NoSuchPortException | TooManyListenersException e1) {
							e1.printStackTrace();
						}
					}
				}
			}
		});
		//��������
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
		//�رմ�������
		closeSerialButton.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if(!closeSerialButton.isEnabled())return;
				SerialTool.closePort(serialPort);
				String time=df.format(LocalDateTime.ofInstant(Instant.ofEpochMilli(System.currentTimeMillis()),ZoneId.of("Asia/Shanghai")));
				receiveArea.append(time+" ["+serialPort.getName().split("/")[3]+"] : "+" �Ͽ�����"+"\r\n");
				receiveArea.setCaretPosition(receiveArea.getText().length());  
				openSerialButton.setEnabled(true);
				closeSerialButton.setEnabled(false);
				sendButton.setEnabled(false);
			}
		});
	}
	/**
	 * ���˵�������ʾ��
	 * ���JLabel����ť��������������¼�������
	 */
	private void init() {

		this.setBounds(WellcomView.LOC_X, WellcomView.LOC_Y, WellcomView.WIDTH, WellcomView.HEIGHT);
		this.setTitle("���ڵ���");
		this.setIconImage(icon);
		this.setBackground(Color.gray);
		this.setLayout(null);

		Font font =new Font("΢���ź�", Font.BOLD, 16);

		receiveArea=new JTextArea(18, 30);
		receiveArea.setEditable(false);
		JScrollPane receiveScroll = new JScrollPane(receiveArea);
		receiveScroll.setBorder(new TitledBorder("������"));
		//�������Զ����� FE0400030001D5C5
		receiveScroll.setHorizontalScrollBarPolicy( 
				JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED); 
		receiveScroll.setVerticalScrollBarPolicy( 
				JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED); 
		receiveScroll.setBounds(52, 20, 680,340);
		this.add(receiveScroll);

		JLabel chuankou=new JLabel(" ����ѡ�� ");
		chuankou.setFont(font);
		chuankou.setBounds(50, 380, 100,50);
		this.add(chuankou);

		JLabel botelv=new JLabel(" �� �� �ʣ� ");
		botelv.setFont(font);
		botelv.setBounds(290, 380, 100,50);
		this.add(botelv);

		//��Ӵ���ѡ��ѡ��
		commChoice = new JComboBox<String>();	//����ѡ��������
		commChoice.setBounds(145, 390, 100, 30);
		this.add(commChoice);

		//��Ӳ�����ѡ��
		bpsChoice = new JComboBox<String>();	//������ѡ��
		bpsChoice.setBounds(380, 390, 100, 30);
		bpsChoice.addItem("1500");
		bpsChoice.addItem("2400");
		bpsChoice.addItem("4800");
		bpsChoice.addItem("9600");
		bpsChoice.addItem("14400");
		bpsChoice.addItem("19500");
		bpsChoice.addItem("115500");
		this.add(bpsChoice);

		//��Ӵ򿪴��ڰ�ť
		openSerialButton = new JButton("����");
		openSerialButton.setBounds(540, 390, 80, 30);
		openSerialButton.setFont(font);
		openSerialButton.setForeground(Color.darkGray);
		this.add(openSerialButton);

		//��ӹرմ��ڰ�ť
		closeSerialButton = new JButton("�ر�");
		closeSerialButton.setEnabled(false);
		closeSerialButton.setBounds(650, 390, 80, 30);
		closeSerialButton.setFont(font);
		closeSerialButton.setForeground(Color.darkGray);
		this.add(closeSerialButton);

		sendArea=new JTextArea(30,20);
		JScrollPane sendScroll = new JScrollPane(sendArea);
		sendScroll.setBorder(new TitledBorder("������"));
		//�������Զ�����
		sendScroll.setHorizontalScrollBarPolicy( 
				JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS); 
		sendScroll.setVerticalScrollBarPolicy( 
				JScrollPane.VERTICAL_SCROLLBAR_ALWAYS); 
		sendScroll.setBounds(52, 450, 500,100);
		this.add(sendScroll);

		sendButton = new JButton("�� ��");
		sendButton.setBounds(610, 520, 120, 30);
		sendButton.setFont(font);
		sendButton.setForeground(Color.darkGray);
		sendButton.setEnabled(false);
		this.add(sendButton);

		this.setResizable(false);	//���ڴ�С���ɸ���
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setVisible(true);
	}

	/**�ַ���ת16����
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
	/**�ֽ�����ת16����
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
	 * ���ڲ�����ʽ����һ�����ڼ�����
	 * @author zhong
	 */
	class SerialListener implements SerialPortEventListener {

		/**
		 * �����ص��Ĵ����¼�
		 */
		public void serialEvent(SerialPortEvent serialPortEvent) {

			switch (serialPortEvent.getEventType()) {
			case SerialPortEvent.BI: // 10 ͨѶ�ж�
				JOptionPane.showMessageDialog(null, "�봮���豸ͨѶ�ж�", "����", JOptionPane.INFORMATION_MESSAGE);
				break;
			case SerialPortEvent.OE: // 7 ��λ�����������
				break;
			case SerialPortEvent.FE: // 9 ֡����
				break;
			case SerialPortEvent.PE: // 8 ��żУ�����
				break;
			case SerialPortEvent.CD: // 6 �ز����
				break;
			case SerialPortEvent.CTS: // 3 �������������
				break;
			case SerialPortEvent.DSR: // 4 ����������׼������
				break;
			case SerialPortEvent.RI: // 5 ����ָʾ
				break;
			case SerialPortEvent.OUTPUT_BUFFER_EMPTY: // 2 ��������������
				break;
			case SerialPortEvent.DATA_AVAILABLE: // 1 ���ڴ��ڿ�������
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
