package com.bruce.telephone;

import java.util.ArrayList;

import com.bruce.crypt.DESCrypt;
import com.bruce.listener.GenerateKeyListener;
import com.bruce.observer.SmsObserver;


import android.app.Activity;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Base64;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.telephony.SmsManager;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class ContactsActivity extends Activity {

	/**协商密钥按钮**/
	Button keyBtn=null;
	/** 显示信息 **/
	TextView tv = null;
	/** 发送按钮 **/
	Button button = null;

	/** 收件人电话 **/
	EditText mNumber = null;

	/** 编辑信息 **/
	EditText mMessage = null;

	/** 发送与接收的广播 **/
	String SENT_SMS_ACTION = "SENT_SMS_ACTION";
	String DELIVERED_SMS_ACTION = "DELIVERED_SMS_ACTION";

	Context mContext = null;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.message);

		keyBtn=(Button)this.findViewById(R.id.keyBtn);
		
		mNumber = (EditText) findViewById(R.id.number);
		mMessage = (EditText) findViewById(R.id.message);

		tv = (TextView) this.findViewById(R.id.keyText);

		keyBtn.setOnClickListener(new GenerateKeyListener(tv,mNumber,this));
		button = (Button) findViewById(R.id.button02);

		mContext = this;
		button.setOnClickListener(new OnClickListener() {

			public void onClick(View view) {

				/** 拿到输入的手机号码 **/
				String number = mNumber.getText().toString();
				/** 拿到输入的短信内容 **/
				String text = mMessage.getText().toString();

				/** 手机号码 与输入内容 必需不为空 **/
				if (!TextUtils.isEmpty(number) && !TextUtils.isEmpty(text)) {
					sendSMS(number, text);
				}
			}
		});

		// 注册广播 发送消息
		registerReceiver(sendMessage, new IntentFilter(SENT_SMS_ACTION));
		registerReceiver(receiver, new IntentFilter(DELIVERED_SMS_ACTION));

		NotificationManager notifyM;
		notifyM = (NotificationManager) this
				.getSystemService(NOTIFICATION_SERVICE);
		ContentResolver cr = this.getContentResolver();
		cr.registerContentObserver(Uri.parse("content://sms/"), true,
				new SmsObserver(tv, this, notifyM, cr, new Handler()));
	}

	private BroadcastReceiver sendMessage = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			// 判断短信是否发送成功
			switch (getResultCode()) {
			case Activity.RESULT_OK:
				Toast.makeText(context, "短信发送成功", Toast.LENGTH_SHORT).show();
				break;
			default:
				Toast.makeText(mContext, "发送失败", Toast.LENGTH_LONG).show();
				break;
			}
		}
	};

	private BroadcastReceiver receiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			// 表示对方成功收到短信
			Toast.makeText(mContext, "对方接收成功", Toast.LENGTH_LONG).show();
		}
	};

	/**
	 * 参数说明 destinationAddress:收信人的手机号码 scAddress:发信人的手机号码 text:发送信息的内容
	 * sentIntent:发送是否成功的回执，用于监听短信是否发送成功。
	 * DeliveryIntent:接收是否成功的回执，用于监听短信对方是否接收成功。
	 */
	private void sendSMS(String phoneNumber, String message) {
		// ---sends an SMS message to another device---
		try {

			SmsManager sms = SmsManager.getDefault();

			// create the sentIntent parameter
			Intent sentIntent = new Intent(SENT_SMS_ACTION);
			PendingIntent sentPI = PendingIntent.getBroadcast(this, 0,
					sentIntent, 0);

			// create the deilverIntent parameter
			Intent deliverIntent = new Intent(DELIVERED_SMS_ACTION);
			PendingIntent deliverPI = PendingIntent.getBroadcast(this, 0,
					deliverIntent, 0);
            String key=SmsObserver.secretKey.substring(0,12 );
            byte[] cipher=DESCrypt.crypt(message.getBytes(), key);
			String cipherText=new String(Base64.encode(cipher, Base64.DEFAULT));
			// 如果短信内容超过70个字符 将这条短信拆成多条短信发送出去
			if (cipherText.length() > 70) {
				ArrayList<String> msgs = sms.divideMessage(cipherText);
				for (String msg : msgs) {
					sms.sendTextMessage(phoneNumber, null, msg, sentPI,
							deliverPI);
				}
			} else {
				sms.sendTextMessage(phoneNumber, null, cipherText, sentPI,
						deliverPI);
			}
			tv.append("\n发给:"+phoneNumber+"\n"+message+"->"+cipherText);
		} catch (Exception exc) {
			Toast.makeText(this, "error", Toast.LENGTH_LONG);
		}
	}

}