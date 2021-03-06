/*----------------------------------------
功能: 用HC-SR04超声波模块测量距离
并用单片机(STC89C52RC)接收信号，计算距离，
并在LCD1602上实时显示当前距离。
距离=（高电平时间*声速）/2，
高电平时间就是超声波往返的时间。
----------------------------------------*/
#include <Ultrasonic.h>	 //调用头文件Ultrasonic.h：（其中包括一些调用头文件，初始化变量，宏定义和一些函数声明）
#include <I2C.H>

/*定义全局变量*/
uint time = 0; // 记录回声高电平持续时间
float distance = 0; // 存放距离
unsigned  int jieshou;//存放从PC端接收到的数据
unsigned  int flag=0;//发送模块的确认信息

/*定义LCD1602显示模块显示的内容（用数组形式存贮）*/
//LCD1602显示的第一行信息预留给光照模块
uchar code table0[] = {"GQ:"};		  //标注信息：光强
uchar code table4[] = {"Lux"};		  //单位:Lux勒克斯
// 变量定义
unsigned char AD_CHANNEL;
unsigned long xdata  LedOut[4]={0,0,0,0};	 //存放处理过后的光强信息
unsigned int  D[32];
unsigned char date;



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
		int i=0;		
	     /*******以下超声波距离处理*********/ 		
		uint disINT = (uint)distance;	   //强制类型转换，将浮点型数据（float）dat转换成无符号整型数据（unsigned int）
		/*先关闭蜂鸣器*/
	    fm=1;

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


	    //倒车报警模块
		//情况1：
		//当车尾距离墙壁小于500mm时蜂鸣器鸣笛报警
		if(disINT<=500)
		{
			fm=0;
		}
		//情况2：
		//当车尾距离墙壁大于500mm时汽车处于安全状态，蜂鸣器不响
		else
		{
			fm=1;
		}
		
		/********以下将距离值通过串口发送出去************/

		
		if(flag==1)        //发送数据条件，开关在串口中断函数中，当接收完PC端信息后自动触发
     {
		ES=0;  //关闭串口中断，先将需要发送给PC端的倒车距离数据，按位发送给PC端直到全部发送完毕
         for(i=0;i<4;i++)
		 {
         SBUF=dis[i];//存一条发一条，直到发完为止
         while(!TI);
         TI=0;              //软件清0，然后循环下一次字符
     	 }

		//ES=1; //开启串口中断等待下一次的PC端数据发送（也就是单片机等待接收）
	    //flag=0;
	 }


		delay(100);
		/*发送距离和光照强度的时间每隔100ms发一次*/
		/********以下AD-DA处理*************/  	
	     switch(AD_CHANNEL)
		 {
			 case 0: ISendByte(PCF8591,0x41);
			      D[0]=IRcvByte(PCF8591);  //ADC0 模数转换1      光敏电阻
				  break;  
			
			 case 1: ISendByte(PCF8591,0x42);
			      D[1]=IRcvByte(PCF8591);  //ADC1  模数转换2	  热敏电阻
				  break;  
			
			 case 2: ISendByte(PCF8591,0x43);
			      D[2]=IRcvByte(PCF8591);  //ADC2	模数转换3	   悬空
				  break;  
			
			 case 3: ISendByte(PCF8591,0x40);
			      D[3]=IRcvByte(PCF8591);  //ADC3   模数转换4	   可调0-5v
				  break;  
			
			 case 4: DACconversion(PCF8591,0x40, D[4]); //DAC	  数模转换
			      break;
		 
		 }

       D[4]=D[3];  //把模拟输入采样的信号 通过数模转换输出
	   	   	
	   if(++AD_CHANNEL>4) AD_CHANNEL=0;
	
	   //计算AD值，然后将值传给串口并通过display（）显示到LCD显示器上，我们为了提高灵敏度，将数据大小扩大了10倍	 	
       LedOut[0] = D[3]/100%10;
       LedOut[1] = D[3]/10%10;
       LedOut[2] = D[3]/1%10;
	   LedOut[3] = D[3]*10%10;	

		//车库补光模块
	   	//情况1：
		//当车库亮度小于等于1290勒克斯,三个小灯均不亮
		if(D[3]*10<=1290)
		{
			l2=1;
			l3=1;
			l4=1;
		}

		//情况2：
		//当车库亮度大于等于1300勒克斯，小于1310勒克斯时，小灯P22亮灯
		else if((D[3]*10>=1300)&&(D[3]*10<1310))
		{
			l2=0;
			l3=1;
			l4=1;
		}

		//情况3：
		//当车库亮度大于等于1310勒克斯，小于1320勒克斯时，小灯P22亮灯,小灯P23亮灯
		else if((D[3]*10>=1310)&&(D[3]*10<1320))
		{
		    l2=0;
			l3=0;
			l4=1;
		}

		//情况4：
		//当车库亮却笥诘扔?1320勒克斯时，小灯P22亮灯,小灯P23亮灯,小灯P24亮灯
		else if(D[3]*10>=1320)
		{
			l2=0;
			l3=0;
			l4=0;
		}
	   

	    
	   /********以下将AD的值通过串口发送出去*************/
	   
	   if(flag==1)        //发送数据条件，开关在串口中断函数中，当接收完PC端信息后自动触发
     {
		//ES=0;  //关闭串口中断，先将需要发送给PC端的光照强度数据，按位发送给PC端直到全部发送完毕
       	 for(i=0;i<4;i++)
		 {
         SBUF=LedOut[i];//存一条发一条，直到发完为止
         while(!TI);
         TI=0;              //软件清0，然后循环下一次字符
   		 }

		ES=1; //开启串口中断等待下一次的PC端数据发送（也就是单片机等待接收）
	    
	 }
	 		
		
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
	TMOD = 0x21; // 定时器1 定时模式 工作方式2 8位自动重装载
			   	// 定时器0 定时模式 工作方式1 非门控启动
	TH0 = 0x00; //设置定时时间
	TL0 = 0x00;	//设置定时时间
	TL1=0XFa;  //初值为250，波特率为：4800
    TH1=0XFa;  //初值为250，波特率为：4800
	TR1=1;  //启动定时器T1！
	SM0=0;     //设定串口工作方式
    SM1=1;     //方式1，SM0=0，SM1=1
    REN=1;     //准许串口接收，现在先设定为只发送信息到PC端
	ET0 = 1; // 开timer0溢出中断
	EA = 1; // 开全局中断
    ES=1;   //开串口中断
	
}

/*原定用：定时器1进行计时让单片机每1s钟测试一次距离和光照强度，
在定时器1的中断函数中同时办测距和测光强两件事！！*/
/*
void timer1Service() interrupt 3  //定时器T1  3.5
{
	TH1=(65536-50000)/256; //设置定时时间
	TL1=(65536-50000)%256; //设置定时时间 	
	nn++;
	if(nn==20)//20*20ms=1s钟计时一次，让定时器进入两个测量部分；
	{

		//超声波测距离部分：
		echo = 0; // 接收回音端置0
		startUltra(); // 启动超声测距
		while(!echo); // 等待回音
		TR0 = 1; // 开始计时
		while(echo);
		TR0 = 0; // 停止计时
		computeDis(); // 计算距离
		display(distance); // 显示距离（单位mm毫米）

		//光敏电阻测光照强度部分：
		//等待一手菜狗田建邦！

		//对定时器1s进行清零
		nn=0;	
	}
}
*/


/*接收PC端数据模块，串口中断，在这里是用来接收PC端的一个确认信息，接收到则打开main函数中的发送数据模块*/

void ser() interrupt 4    //当接收到PC发送的数据时，自动进入中断程序
{
  
  jieshou=SBUF;//将接收到的数据存到a中,这里要求PC端发送一个0，也就是低电平，当PC端发送0时则单片机蜂鸣器响1s钟代表接收到PC端确认信息
  fm=jieshou;
  delay(1000);//蜂鸣器响一秒钟，表示接收到了PC端信息
  fm=1;
  flag=1;  //flag是main函数中，单片机发送数据的条件
  RI=0;    //将R1清0等待下一次接收数据
  //ES=0;    //接收到一次pc端的请求发送LCD信息后，则关闭串口中断，直接将LCD显示的测得数据发送给PC端
  REN=0;
    
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
	for(i = 0; i < 4; i++){
		writeCommand(0x80 + 0x00 + strlen(table0) + i);
		writeDataLCD(table2[LedOut[i]]);//光照强度
		delay(5);
	}
	for(i = 0; i < strlen(table4); i++){
		writeCommand(0x80 + 0x00 + strlen(table0) + 4 + i);
		writeDataLCD(table4[i]);    //单位：Lux
		delay(5);
	}


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
		for(j = 125; j > 0; j--);
}

/*光敏模块ad转换：*/
/*******************************************************************
DAC 变换, 转化函数               
*******************************************************************/
bit DACconversion(unsigned char sla,unsigned char c,  unsigned char Val)
{
   Start_I2c();              //启动总线
   SendByte(sla);            //发送器件地址
   if(ack==0)return(0);
   SendByte(c);              //发送控制字节
   if(ack==0)return(0);
   SendByte(Val);            //发送DAC的数值  
   if(ack==0)return(0);
   Stop_I2c();               //结束总线
   return(1);
}

/*******************************************************************
ADC发送字节[命令]数据函数               
*******************************************************************/
bit ISendByte(unsigned char sla,unsigned char c)
{
   Start_I2c();              //启动总线
   SendByte(sla);            //发送器件地址
   if(ack==0)return(0);
   SendByte(c);              //发送数据
   if(ack==0)return(0);
   Stop_I2c();               //结束总线
   return(1);
}

/*******************************************************************
ADC读字节数据函数               
*******************************************************************/
unsigned char IRcvByte(unsigned char sla)
{  unsigned char c;

   Start_I2c();          //启动总线
   SendByte(sla+1);      //发送器件地址
   if(ack==0)return(0);
   c=RcvByte();          //读取数据0

   Ack_I2c(1);           //发送非就答位
   Stop_I2c();           //结束总线
   return(c);
}



