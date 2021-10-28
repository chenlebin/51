/*--------------------------------------------------------------------------
//请自行更改通信定义引脚
//众明工作室
//淘宝链接: https://item.taobao.com/item.htm?spm=a2oq0.12575281.0.0.15d11deb3ow36J&ft=t&id=642125636062
//  版 本 号   : v1.0
//  作    者   : 众明工作室
//  联系  QQ   : 1795190213
//    众明交流群 :1041406448
// 众明售前咨询群:150709982
// 众明技术售后群:1080227324（订单号入群）
//  生成日期   : 2021-04-15
//  最近修改   : 
//  功能描述   : PCF8591四路采集串口显示测试程序
//      说明   : 

--------------------------------------------------------------------------*/

#include<reg52.h>    //包含单片机寄存器的头文件
#include <I2C.H>

#include <string.h>
#include <intrins.h>


#define uchar unsigned char//宏定义定义无符号char型为uchar
#define uint unsigned int//宏定义定义无符号int型为uint
#define FOSC 11.0592 // 系统晶振频率11.0592MHz
/*LCD1602显示模块端口定义*/
sbit lcdrs = P2^5;//LCD  RS引脚
sbit lcdrw = P2^6;//LCD  RW引脚
sbit lcden = P2^7;//LCD  E引脚
uchar code table0[] = {"GQ:"};		  //标注信息：光强
uchar code table1[] = {"Distance:"};  //标注信息：距离
uchar code table2[] = {"0123456789"}; //距离
uchar code table3[] = {"Lux"};		  //单位:Lux勒克斯


#define uchar unsigned char
#define  PCF8591 0x90    //PCF8591 地址



// 变量定义
unsigned char AD_CHANNEL;
unsigned long xdata  LedOut[4];
unsigned int  D[32];
// sbit P2_0 = P2^0;
// sbit P2_1 = P2^1;
// sbit P2_2 = P2^2;
// sbit P2_3 = P2^3;


					
unsigned char date;

//uchar code table[]={0xc0,0xf9,0xa4,0xb0,0x99,0x92,0x82,0xf8,0x80,0x90};


  // 函数申明 

void display();
void delay(uchar i);
bit DACconversion(unsigned char sla,unsigned char c,  unsigned char Val);
bit ISendByte(unsigned char sla,unsigned char c);
unsigned char IRcvByte(unsigned char sla);
void Initial_com(void);

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


//******************************************************************/
//void Timer0Init()
//{
//	TMOD|=0X01;//选择为定时器0模式，工作方式1，仅用TR0打开启动。

//	TH0=0XFC;	//给定时器赋初值，定时1ms
//	TL0=0X66;	
//	ET0=1;//打开定时器0中断允许
//	EA=1;//打开总中断
//	TR0=1;//打开定时器			
//}
void DigDisplay()
{
	//显示第一行的光照强度数据信息：	
	uchar  i;
	for(i = 0; i < 4; i++){
		writeCommand(0x80 + 0x00 + strlen(table0) + i);
		writeDataLCD(table2[LedOut[i]]);//光照强度
		delay(5);
	}
	for(i = 0; i < strlen(table3); i++){
		writeCommand(0x80 + 0x00 + strlen(table0) + 4 + i);
		writeDataLCD(table3[i]);    //单位：Lux
		delay(5);
	}
}

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
main()
{  

	//Timer0Init();
	LCDInit();//调用LCD显示模块初始化
	while(1)
	{

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
	
	   /********以下将AD的值通过串口发送出去*************/
	 //delay(200);	
	 // SBUF=D[0];		   	   
	 //delay(200);		  
	 //SBUF=D[1];	
     LedOut[0] = D[3]/100%10;
     LedOut[1] = D[3]/10%10;
     LedOut[2] = D[3]/1%10;
	 LedOut[3] = D[3]*10%10;	 

		DigDisplay();
		P2=0xff;
	 //delay(200);	
	 // SBUF=D[2];
	 // delay(200);
	 // SBUF=D[3];
	 // delay(200);
	 
   }
}


void delay(uchar i)
{
  uchar j,k; 
  for(j=i;j>0;j--)
    for(k=125;k>0;k--);
}

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



//void Timer0() interrupt 1
//{
//	unsigned char i;
//	TH0=0XFC;	//给定时器赋初值，定时1ms
//	TL0=0X66;	
//	i++;
//	if(i==5)
//	{	
//		DigDisplay();
//	}
////  if(i>=20)
////	{	
////		i=0;
////	  P0=0x00;//消隐
////	}
//}
