/*----------------------------------------
����: ��HC-SR04������ģ���������
���õ�Ƭ��(STC89C52RC)�����źţ�������룬
����LCD1602��ʵʱ��ʾ��ǰ���롣
����=���ߵ�ƽʱ��*���٣�/2��
�ߵ�ƽʱ����ǳ�����������ʱ�䡣
----------------------------------------*/
#include "Ultrasonic.h"	 //����ͷ�ļ�Ultrasonic.h�������а���һЩ����ͷ�ļ�����ʼ���������궨���һЩ����������


/*����ȫ�ֱ���*/
uint time = 0; // ��¼�����ߵ�ƽ����ʱ��
float distance = 0.0; // ��ž���

/*����LCD1602��ʾģ����ʾ�����ݣ���������ʽ������*/
uchar code table0[] = {"weight:0034kg"};		 


//LCD1602��ʾ�ĵڶ�����Ϣ����ʾ���������ģ�飺
uchar code table1[] = {"Distance:"};  //��ע��Ϣ
uchar code table2[] = {"0123456789"}; //����
uchar code table3[] = {"mm"};		  //��λ:mm
// ��������
uchar num = 0;
uint nn=0;
uchar dis[4];//��Ŵ������ľ�����Ϣ


/*������*/
void main(){
    
	LCDInit();//LCD��ʾ��ʼ������
	timerInit();//��ʱ��0��1��ʼ������
	while(1){
	     /*******���³��������봦��*********/ 
		uint disINT = (uint)distance;	   //ǿ������ת���������������ݣ�float��datת�����޷����������ݣ�unsigned int��
		int i=0;
		echo = 0; // ���ջ�������0
		startUltra(); // �����������
		while(!echo); // �ȴ�����
		TR0 = 1; // ��ʼ��ʱ
		while(echo);
		TR0 = 0; // ֹͣ��ʱ
		computeDis(); // �������

		//�������ֵ��Ȼ��ֵ�������ڲ�ͨ��display������ʾ��LCD��ʾ����		
     	dis[0] = disINT / 1000;        //ȡ��ǧλ
    	dis[1] = disINT % 1000 / 100;  //ȡ����λ
	    dis[2] = disINT % 100 / 10;	   //ȡ��ʮλ
	    dis[3] = disINT % 10;          //ȡ����λ
	 
		display(); // ��LCD��ʾ������ʾ����ǿ�ȣ���һ�У��͵������루�ڶ��У�
	}
}

/*==============================================
��������������ΪHC-SR04������ģ������ã���������
===============================================*/
/*�������������*/
void startUltra(){ // ����һ������10us�ĸߵ�ƽ��HC-SR04������ģ�������������
	trig = 1;
	delayUs(15);
	trig = 0;
}

/*��ʱ��0��1��ʼ������*/
void timerInit(){
	TMOD = 0x01; // ��ʱ��1 ��ʱģʽ ������ʽ2 8λ�Զ���װ��
			   	// ��ʱ��0 ��ʱģʽ ������ʽ1 ���ſ�����
	TH0 = 0x00; //���ö�ʱʱ��
	TL0 = 0x00;	//���ö�ʱʱ��  
	ET0 = 1; // ��timer0����ж�
	EA = 1; // ��ȫ���ж�

}



/*��ʱ��0����жϷ������*/
void timer0Service() interrupt 1{ // �������˵��������෶Χ
	echo = 0; // ǿ�ƹرջ���
}

/*��ʱx * 1 us*/
void delayUs(uint xus){
	uint i;
	for(i = xus; i > 0; i--){
		_nop_();			//�պ���_nop()_���൱��һ��������ʱ��1us
	}
}

/*�������*/
void computeDis(){
	time = TH0 * 256 + TL0;    //ʱ��ת��Ϊtime
	distance = FACTOR * time;  //FACTOR�궨���Ϊ��0.1845
	TH0 = 0x00; // ��װ��ֵ
	TL0 = 0x00;
}

/*==============================================
����������������ΪLCD1602����ʾ���ã�������������
===============================================*/

/*��ʼ��LCD1602������*/
void LCDInit(){
	uchar i;
	lcden = 0; // ����ʹ�ܶˣ�׼������ʹ�ܸ������ź�
	writeCommand(0x38); // ��ʾģʽ����(16x2�� 5x7����8λ���ݽӿ�)
	writeCommand(0x0c); // ����ʾ������ʾ���
	writeCommand(0x06); // дһ���ַ����ַָ���Զ���1
	writeCommand(0x01); // ��ʾ���㣬����ָ������
	
	/*LCD�ϵ����*/	 

	//��һ�е����ã�������ʾ����ǿ�ȿ��ƿ��ص�
	writeCommand(0x80); // ������ָ�붨λ����һ����
	for(i = 0; i < strlen(table0); i++){
		writeDataLCD(table0[i]);
		delay(5);
	}	  
	
	
	//�ڶ��е����ã�������ʾ����ǿ�ȿ���С��
	writeCommand(0x80 + 0x40); // ������ָ�붨λ���ڶ�����
	for(i = 0; i < strlen(table1); i++){
		writeDataLCD(table1[i]);  //��ע��Ϣ:Distance
		delay(5);
	}
}

/*LCD��ʾ����*/
void display(){
	/*�������ǿ���ֵ�һ����ʾ*/	
	uchar  i;


	/*��������ಿ�ֵڶ�����ʾ*/		
	for(i = 0; i < 4; i++){
		writeCommand(0x80 + 0x40 + strlen(table1) + i);
		writeDataLCD(table2[dis[i]]);//����
		delay(5);
	}
	for(i = 0; i < strlen(table3); i++){
		writeCommand(0x80 + 0x40 + strlen(table1) + 4 + i);
		writeDataLCD(table3[i]);    //��λ��mm
		delay(5);
	}
}

/*дָ���*/
void writeCommand(uchar command){
	lcdrs = 0; // ����ѡ��
	lcdrw = 0;
	P0 = command;
	delay(5);
	
	lcden = 1; // ����һ��������ʹ���ź�
	delay(5);
	lcden = 0;
}

/*д���ݺ���*/
void writeDataLCD(uchar dat){
	lcdrs = 1; // ����ѡ��
	lcdrw = 0;
	P0 = dat;
	delay(5);
	
	lcden = 1;
	delay(5);
	lcden = 0;
}

/*��ʱx* 1 ms����*/
void delay(uint xms){
	uint i, j;
	for(i = xms; i > 0; i--)
		for(j = 110; j > 0; j--);
}
