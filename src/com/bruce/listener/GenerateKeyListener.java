package com.bruce.listener;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;

import java.math.BigInteger;
import java.util.ArrayList;

import com.bruce.diffhell.SelfDefineDH;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.telephony.SmsManager;
import android.widget.EditText;
import android.widget.TextView;

public class GenerateKeyListener implements OnClickListener {

	public  static BigInteger[] bgArr=null;
	public  static BigInteger[] Axy=null;
	public  BigInteger[] Bxy=null;
	private TextView tv;
	private EditText editText;// 手机号输入栏
	private Activity activity;// 协商密钥按钮所在的activity

	/** 发送与接收的广播 **/
	String SENT_SMS_ACTION = "SENT_SMS_ACTION";
	String DELIVERED_SMS_ACTION = "DELIVERED_SMS_ACTION";
	

	public GenerateKeyListener(TextView tv,EditText ev, Activity activity) {
		this.tv=tv;
		this.editText = ev;
		this.activity = activity;

	}

	public void onClick(View v) {
	
		/**
		 * 利用DH协商密钥 明天2014-3-27编写
		 * 利用Diffie-Hellman密钥协商算法协商密钥
		 */
	 
		//First Step生成大素数和它的本原根
	    bgArr=new BigInteger[2];
	
		bgArr=SelfDefineDH.SecretKeySwap();
		Log.i("XXOO1", bgArr[0].toString());
		Log.i("XXOO1", bgArr[1].toString());
		tv.append("大素数P:"+bgArr[0].toString()+"\n");
		tv.append("本原根g:"+bgArr[1].toString()+"\n");
		
		//己方开始生成自己的X,Y
		Axy=new BigInteger[2];
		Axy=SelfDefineDH.generateXY_A(bgArr);
		tv.append("Xa:"+Axy[0].toString()+"\n");
		tv.append("Ya:"+Axy[1].toString()+"\n");
		//生成需要发送给B的数据结构
		String[]  sendDS=new String[3];
		sendDS[0]=bgArr[0].toString();
		sendDS[1]=bgArr[1].toString();
		sendDS[2]=Axy[1].toString();
		
		String send=sendDS[0]+"&"+sendDS[1]+"&"+sendDS[2];
		/** 拿到输入的手机号码 **/
		
		String number = editText.getText().toString();

		this.sendKeySpec(send, number);

	}

	
	
	private void sendKeySpec(String pubKeyEnc, String number) {

		SmsManager sms = SmsManager.getDefault();

		// create the sentIntent parameter
		Intent sentIntent = new Intent(SENT_SMS_ACTION);
		PendingIntent sentPI = PendingIntent.getBroadcast(this.activity, 0,
				sentIntent, 0);

		// create the deilverIntent parameter
		Intent deliverIntent = new Intent(DELIVERED_SMS_ACTION);
		PendingIntent deliverPI = PendingIntent.getBroadcast(this.activity, 0,
				deliverIntent, 0);

		pubKeyEnc = "rk&" + pubKeyEnc;
		tv.append("\nsend:"+pubKeyEnc);
		//this.editText.setText(""+pubKeyEnc.length());
		if (pubKeyEnc.length() > 70) {
			ArrayList<String> msgs = sms.divideMessage(pubKeyEnc);
			for (String msg : msgs) {
				sms.sendTextMessage(number, null, msg, sentPI, deliverPI);
			}
		} else {
			sms.sendTextMessage(number, null, pubKeyEnc, sentPI, deliverPI);
		}

	}
}
