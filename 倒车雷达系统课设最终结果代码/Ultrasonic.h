#include <reg52.h>
#include <string.h>
#include <intrins.h>
#define uchar unsigned char//宏定义定义无符号char型为uchar
#define uint unsigned int//宏定义定义无符号int型为uint
#define FOSC 11.0592 // 系统晶振频率11.0592MHz
#define FACTOR 0.1845 // (距离计算因子)[(12 / FOSC) * 10^-6] * 340 / 2
#define  PCF8591 0x90    //PCF8591 地址

/* 蜂鸣器I/O口 */
sbit fm = P1^0;   // 蜂鸣器I/O口,给低电平时蜂鸣器响

/*车尾补光模块（三个小灯）*/
sbit l2=P2^2;
sbit l3=P2^3;
sbit l4=P2^4;

	
/*HC-SR04超声波模块两个引脚*/
sbit trig = P2^1; // 电平触发输入引脚
sbit echo = P2^0; // 回声输出引脚


/*LCD1602显示模块端口定义*/
sbit lcdrs = P2^5;//LCD  RS引脚
sbit lcdrw = P2^6;//LCD  RW引脚
sbit lcden = P2^7;//LCD  E引脚




/*主模块函数声明*/
void startUltra();
void timerInit();
void delayUs(uint xus);
void computeDis();

/*LCD1602显示模块函数声明*/
void delay(uint xms);
void writeCommand(uchar command);
void writeDataLCD(uchar dat);
void LCDInit();
void display();


/*光敏电阻函数声明*/
bit DACconversion(unsigned char sla,unsigned char c,  unsigned char Val);
bit ISendByte(unsigned char sla,unsigned char c);
unsigned char IRcvByte(unsigned char sla);
void Initial_com(void);
