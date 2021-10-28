/*
例4：PC机通信向单片机发送一组16进制数，点亮LED小灯；
接收到数据后，向PC端发送“I get ” 。
*/
#include <reg52.h>
unsigned char code seg7[]={0xc0,0xf9,0xa4,0xb0,0x99,0x92,0x82,0xf8,
                            0x80,0x90,0x88,0x83,0xc6,0xa1,0x86,0x8e};//共阳
unsigned char  table[]="i get" ;

unsigned  int i,j,k,flag;
unsigned  int a;//存放接收数据

void delay10ms(k)	                  	 //有参延时函数
{
	for(i=0;i<k;i++)			//10ms*k
	    {
	          for(j=0;j<1000;j++);		//每条for语句大约用时10us
					//1000*10=10ms
	    }
}
void init()
{
   TMOD=0X20;                 //GATE=0,ct!=0,M1=1,M0=0:定时器1,工作方式2,8位自动重装载
   TL1=0XFa;                  //初值为250，波特率为：4800
   TH1=0XFa;                  //初值为250，波特率为：4800
   TR1=1;                     //启动定时器T1
   SM0=0;                     //设定串口工作方式
   SM1=1;                     //方式1，SM0=0，SM1=1
   REN=1;                     //准许串口接收
   EA=1;                      //开总中断
   ES=1;                      //开串口中断

}

main()
{
  init();    //调用初始化
  while(1)
  {
     if(flag==1)        //发送数据条件，开关在串口中断函数中，当接收完PC端信息后自动触发
     {
       ES=0;              //关闭串口中断，先将需要发送给PC端的数据（I get），按位发送给PC端直到全部发送完毕
       for(i=0;i<5;i++)
       {
        SBUF=table[i];//存一条发一条，直到发完为止
        while(!TI) ;
        TI=0;              //软件清0，然后循环下一次字符
       }
       SBUF=a;          //将接收到的16进制数（0X？？）再次发送给PC端确认单片机真的接收到了这个数据
	   while(!TI);
	   TI=0;   
       /*
        以上为方式1发送数据大致流程 
       */                     
      
       ES=1; //开启串口中断等待下一次的PC端数据发送（也就是单片机等待接收）
       flag=0;//将flag单片机发送数据的开关关闭，直到下一次接收到PC端信息，
                  //进入串口中断函数后最后一行flag=1开启main函数发送数据为止！！ 
     
     }                                           
  }

}
void ser() interrupt 4    //当接收到PC发送的数据时，自动进入中断程序
{
 
  
  a=SBUF;//将接收到的数据存到a中
  P2=a;    //让a中存储的16进制数以小灯的形式显示出来
  flag=1;  //flag是main函数中，单片机发送数据的条件
  RI=0;    //将R1清0等待下一次接收数据
  


}