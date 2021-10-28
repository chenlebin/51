package serialPort;

import java.awt.Color;
import java.awt.Font;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import javax.swing.JFrame;
import javax.swing.JLabel;


/**
 * @author bh
 * ������й������׳� java.lang.UnsatisfiedLinkError ����
 * �뽫rxtx��ѹ���е� rxtxParallel.dll��rxtxSerial.dll �������ļ����Ƶ� C:\Windows\System32 Ŀ¼�¼��ɽ���ô���
 */
public class WellcomView {

	/** ���������*/
	public static final int WIDTH = 800;
	/** �������߶�*/
	public static final int HEIGHT = 620;
	/** ����������λ�ã������꣩ */
	public static final int LOC_X = 200;
	/** ����������λ�ã������꣩*/
	public static final int LOC_Y = 70;
	
	private JFrame jFrame;

	/**������
	 * @param args	//
	 */
	public static void main(String[] args) {
		
		new WellcomView();
	}
	public WellcomView() {
		
		init();
		listen();
	}
	/**
	 */
	private void listen() {
		
		//��Ӽ��̼�����
		jFrame.addKeyListener(new KeyAdapter() {
			public void keyReleased(KeyEvent e) {
				int keyCode = e.getKeyCode();
				if (keyCode == KeyEvent.VK_ENTER) {	//���������û��û�����enter����ִ������Ĳ���
					jFrame.setVisible(false);	//��ȥ��ӭ����
					new SerialView();	//�������ࣨ��ʾ�����������壩
				}
			}
		});	
	}
	/**
	 * ��ʾ������
	 */
	private void init() {

		jFrame=new JFrame("���ڵ���");
		jFrame.setBounds(LOC_X, LOC_Y, WIDTH, HEIGHT);	//�趨������������ֵ�λ��
		jFrame.setLayout(null);
		//����window��icon���������Զ�����һ��Windows���ڵ�iconͼ�꣬��Ϊʵ�ھ����ĸ�С����ͼ�겻�ÿ� = =��
		Toolkit toolKit = jFrame.getToolkit();
		Image icon = toolKit.getImage(WellcomView.class.getResource("computer.png"));
		jFrame.setIconImage(icon); 
		jFrame.setBackground(Color.white);	//���ñ���ɫ
		
		JLabel huanyin=new JLabel("��ӭʹ�ô��ڵ��Թ���");
		huanyin.setBounds(170, 80,600,50);
		huanyin.setFont(new Font("΢���ź�", Font.BOLD, 40));
		jFrame.add(huanyin);
		
		JLabel banben=new JLabel("Version��1.0   Powered By��cyq");
		banben.setBounds(180, 390,500,50);
		banben.setFont(new Font("΢���ź�", Font.ITALIC, 26));
		jFrame.add(banben);
		
		JLabel enter=new JLabel("�����������Enter�����������桪������");
		enter.setBounds(100, 480,600,50);
		enter.setFont(new Font("΢���ź�", Font.BOLD, 30));
		jFrame.add(enter);
		
		jFrame.setResizable(false);	//���ڴ�С���ɸ���
		jFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		jFrame.setVisible(true);	//��ʾ����
	}
}
