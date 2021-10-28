/*----------------------------------------
功能: 用HC-SR04超声波模块测量距离
并用单片机(STC89C52RC)接收信号，计算距离，
并在LCD1602上实时显示当前距离。
距离=（高电平时间*声速）/2，
高电平时间就是超声波往返的时间。
----------------------------------------*/
#include "Ultrasonic.h"	 //调用头文件Ultrasonic.h：（其中包括一些调用头文件，初始化变量，宏定义和一些函数声明）


/*定义全局变量*/
uint time = 0; // 记录回声高电平持续时间
float distance = 0.0; // 存放距离

/*定义LCD1602显示模块显示的内容（用数组形式存贮）*/
uchar code table0[] = {"weight:0034kg"};		 


//LCD1602显示的第二行信息，显示超声波测距模块：
uchar code table1[] = {"Distance:"};  //标注信息
uchar code table2[] = {"0123456789"}; //距离
uchar code table3[] = {"mm"};		  //单位:mm
// 变量定义
uchar num = 0;
uint nn=0;
uchar dis[4];//存放处理过后的距离信息


/*主函数*/
void main(){
    
	LCDInit();//LCD显示初始化函数
	timerInit();//定时器0和1初始化函数
	while(1){
	     /*******以下超声波距离处理*********/ 
		uint disINT = (uint)distance;	   //强制类型转换，将浮点型数据（float）dat转换成无符号整型数据（unsigned int）
		int i=0;
		echo = 0; // 接收回音端置0
		startUltra(); // 启动超声测距
		while(!echo); // 等待回音
		TR0 = 1; // 开始计时
		while(echo);
		TR0 = 0; // 停止计时
		computeDis(); // 计算距离

		//计算距离值，然后将值传给串口并通过display（）显示到LCD显示器上		
     	dis[0] = disINT / 1000;        //取出千位
    	dis[1] = disINT % 1000 / 100;  //取出百位
	    dis[2] = disINT % 100 / 10;	   //取出十位
	    dis[3] = disINT % 10;          //取出个位
	 
		display(); // 在LCD显示屏上显示光照强度（第一行）和倒车距离（第二行）
	}
}

/*==============================================
！！！！！以下为HC-SR04超声波模块的设置：！！！！
===============================================*/
/*启动超声波测距*/
void startUltra(){ // 产生一个大于10us的高电平，HC-SR04超声波模块的启动条件！
	trig = 1;
	delayUs(15);
	trig = 0;
}

/*定时器0和1初始化函数*/
void timerInit(){
	TMOD = 0x01; // 定时器1 定时模式 工作方式2 8位自动重装载
			   	// 定时器0 定时模式 工作方式1 非门控启动
	TH0 = 0x00; //设置定时时间
	TL0 = 0x00;	//设置定时时间  
	ET0 = 1; // 开timer0溢出中断
	EA = 1; // 开全局中断

}



/*定时器0溢出中断服务程序*/
void timer0Service() interrupt 1{ // 计数溢出说明超出测距范围
	echo = 0; // 强制关闭回音
}

/*延时x * 1 us*/
void delayUs(uint xus){
	uint i;
	for(i = xus; i > 0; i--){
		_nop_();			//空函数_nop()_，相当于一个短暂延时：1us
	}
}

/*计算距离*/
void computeDis(){
	time = TH0 * 256 + TL0;    //时间转换为time
	distance = FACTOR * time;  //FACTOR宏定义过为：0.1845
	TH0 = 0x00; // 重装初值
	TL0 = 0x00;
}

/*==============================================
！！！！！！以下为LCD1602的显示设置：！！！！！！
===============================================*/

/*初始化LCD1602的设置*/
void LCDInit(){
	uchar i;
	lcden = 0; // 拉低使能端，准备产生使能高脉冲信号
	writeCommand(0x38); // 显示模式设置(16x2， 5x7点阵，8位数据接口)
	writeCommand(0x0c); // 开显示，不显示光标
	writeCommand(0x06); // 写一个字符后地址指针自动加1
	writeCommand(0x01); // 显示清零，数据指针清零
	
	/*LCD上电界面*/	 

	//第一行的设置：用来显示光照强度控制开关灯
	writeCommand(0x80); // 将数据指针定位到第一行首
	for(i = 0; i < strlen(table0); i++){
		writeDataLCD(table0[i]);
		delay(5);
	}	  
	
	
	//第二行的设置：用来显示光照强度控制小车
	writeCommand(0x80 + 0x40); // 将数据指针定位到第二行首
	for(i = 0; i < strlen(table1); i++){
		writeDataLCD(table1[i]);  //标注信息:Distance
		delay(5);
	}
}

/*LCD显示函数*/
void display(){
	/*光敏测光强部分第一行显示*/	
	uchar  i;


	/*超声波测距部分第二行显示*/		
	for(i = 0; i < 4; i++){
		writeCommand(0x80 + 0x40 + strlen(table1) + i);
		writeDataLCD(table2[dis[i]]);//距离
		delay(5);
	}
	for(i = 0; i < strlen(table3); i++){
		writeCommand(0x80 + 0x40 + strlen(table1) + 4 + i);
		writeDataLCD(table3[i]);    //单位：mm
		delay(5);
	}
}

/*写指令函数*/
void writeCommand(uchar command){
	lcdrs = 0; // 命令选择
	lcdrw = 0;
	P0 = command;
	delay(5);
	
	lcden = 1; // 产生一个正脉冲使能信号
	delay(5);
	lcden = 0;
}

/*写数据函数*/
void writeDataLCD(uchar dat){
	lcdrs = 1; // 数据选择
	lcdrw = 0;
	P0 = dat;
	delay(5);
	
	lcden = 1;
	delay(5);
	lcden = 0;
}

/*延时x* 1 ms函数*/
void delay(uint xms){
	uint i, j;
	for(i = xms; i > 0; i--)
		for(j = 110; j > 0; j--);
}
