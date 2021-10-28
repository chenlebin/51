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
 * 如果运行过程中抛出 java.lang.UnsatisfiedLinkError 错误，
 * 请将rxtx解压包中的 rxtxParallel.dll，rxtxSerial.dll 这两个文件复制到 C:\Windows\System32 目录下即可解决该错误。
 */
public class WellcomView {

	/** 程序界面宽度*/
	public static final int WIDTH = 800;
	/** 程序界面高度*/
	public static final int HEIGHT = 620;
	/** 程序界面出现位置（横坐标） */
	public static final int LOC_X = 200;
	/** 程序界面出现位置（纵坐标）*/
	public static final int LOC_Y = 70;
	
	private JFrame jFrame;

	/**主方法
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
		
		//添加键盘监听器
		jFrame.addKeyListener(new KeyAdapter() {
			public void keyReleased(KeyEvent e) {
				int keyCode = e.getKeyCode();
				if (keyCode == KeyEvent.VK_ENTER) {	//当监听到用户敲击键盘enter键后执行下面的操作
					jFrame.setVisible(false);	//隐去欢迎界面
					new SerialView();	//主界面类（显示监控数据主面板）
				}
			}
		});	
	}
	/**
	 * 显示主界面
	 */
	private void init() {

		jFrame=new JFrame("串口调试");
		jFrame.setBounds(LOC_X, LOC_Y, WIDTH, HEIGHT);	//设定程序在桌面出现的位置
		jFrame.setLayout(null);
		//设置window的icon（这里我自定义了一下Windows窗口的icon图标，因为实在觉得哪个小咖啡图标不好看 = =）
		Toolkit toolKit = jFrame.getToolkit();
		Image icon = toolKit.getImage(WellcomView.class.getResource("computer.png"));
		jFrame.setIconImage(icon); 
		jFrame.setBackground(Color.white);	//设置背景色
		
		JLabel huanyin=new JLabel("欢迎使用串口调试工具");
		huanyin.setBounds(170, 80,600,50);
		huanyin.setFont(new Font("微软雅黑", Font.BOLD, 40));
		jFrame.add(huanyin);
		
		JLabel banben=new JLabel("Version：1.0   Powered By：cyq");
		banben.setBounds(180, 390,500,50);
		banben.setFont(new Font("微软雅黑", Font.ITALIC, 26));
		jFrame.add(banben);
		
		JLabel enter=new JLabel("————点击Enter键进入主界面————");
		enter.setBounds(100, 480,600,50);
		enter.setFont(new Font("微软雅黑", Font.BOLD, 30));
		jFrame.add(enter);
		
		jFrame.setResizable(false);	//窗口大小不可更改
		jFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		jFrame.setVisible(true);	//显示窗口
	}
}
