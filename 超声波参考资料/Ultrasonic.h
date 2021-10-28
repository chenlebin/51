#include <reg52.h>
#include <string.h>
#include <intrins.h>
#define uchar unsigned char//�궨�嶨���޷���char��Ϊuchar
#define uint unsigned int//�궨�嶨���޷���int��Ϊuint
#define FOSC 11.0592 // ϵͳ����Ƶ��11.0592MHz
#define FACTOR 0.1845 // (�����������)[(12 / FOSC) * 10^-6] * 340 / 2
#define  PCF8591 0x90    //PCF8591 ��ַ

/* ������I/O�� */
sbit fm = P1^0;   // ������I/O��,���͵�ƽʱ��������

	
/*HC-SR04������ģ����������*/
sbit trig = P2^1; // ��ƽ������������
sbit echo = P2^0; // �����������


/*LCD1602��ʾģ��˿ڶ���*/
sbit lcdrs = P2^5;//LCD  RS����
sbit lcdrw = P2^6;//LCD  RW����
sbit lcden = P2^7;//LCD  E����


/*��ģ�麯������*/
void startUltra();
void timerInit();
void delayUs(uint xus);
void computeDis();

/*LCD1602��ʾģ�麯������*/
void delay(uint xms);
void writeCommand(uchar command);
void writeDataLCD(uchar dat);
void LCDInit();
void display();


