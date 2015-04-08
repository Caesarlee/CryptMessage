package com.bruce.observer;

//import javax.crypto.SecretKey;
import java.math.BigInteger;
import java.util.ArrayList;

import com.bruce.crypt.DESCrypt;
import com.bruce.diffhell.SelfDefineDH;
import com.bruce.listener.GenerateKeyListener;


import android.app.Activity;
import android.app.NotificationManager;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.ContentObserver;
import android.database.Cursor;
import android.util.Base64;
import android.net.Uri;
import android.os.Handler;
import android.telephony.SmsManager;
import android.widget.TextView;

public class SmsObserver extends ContentObserver {

	TextView tv;
	private ContentResolver mResolver;

	public static String key = null;
	public static String secretKey;

	
	

	public SmsObserver(TextView tv, Activity activity,
			NotificationManager notify, ContentResolver mResolver,
			Handler handler) {
		super(handler);
		this.mResolver = mResolver;

		this.tv = tv;
		// TODO Auto-generated constructor stub
	}

	public void onChange(boolean selfChange) {

		// 获取接受到的短信
		String[] projection = new String[] { "_id", "thread_id", "address",
				"person", "date", "read", "status", "type", "body", };
		// String where = " type= 1 AND date > "
		// + (System.currentTimeMillis() - 60 * 1000);
		Cursor cursor = null;
		cursor = mResolver.query(Uri.parse("content://sms/inbox"), projection,
				null, null, "date desc");

		int idColumn = cursor.getColumnIndex("_id");
	//	int thread_idColumn = cursor.getColumnIndex("thread_id");

		int phoneNumberColumn = cursor.getColumnIndex("address");
	//	int nameColumn = cursor.getColumnIndex("person");
	//	int dateColumn = cursor.getColumnIndex("date");
		int readColumn = cursor.getColumnIndex("read");
	//	int statusColumn = cursor.getColumnIndex("status");
	//	int typeColumn = cursor.getColumnIndex("type");
		int smsbodyColumn = cursor.getColumnIndex("body");

		if (cursor != null) {
			if (cursor.moveToNext()) {
				String phoneNumber;
				String smsBody;
				if (cursor.getInt(readColumn) == 0) {
					phoneNumber = cursor.getString(phoneNumberColumn);
					smsBody = cursor.getString(smsbodyColumn);
					// tv.append("\n_id:" + cursor.getString(idColumn));
					// tv.append("\nthread_id" +
					// cursor.getString(thread_idColumn));
					//tv.append("\n收到\naddress:" + phoneNumber);
					// tv.append("\nperson:" + cursor.getString(nameColumn));
					// tv.append("\ndate:" + cursor.getString(dateColumn));
					// tv.append("\nread:" + cursor.getString(readColumn));
					// tv.append("\nstatus:" + cursor.getString(statusColumn));
					// tv.append("\ntype:" + cursor.getString(typeColumn));
					//tv.append("\nbody:" + smsBody);

					ContentValues value = new ContentValues();
					try {
						value.put("read", 1);
						this.mResolver.update(Uri.parse("content://sms/"),
								value, "_id=?",
								new String[] { cursor.getString(idColumn) });
					} catch (Exception exc) {
						tv.append("\n\\O_O/");
					}
					// value.put("_id", cursor.getString(idColumn));

					int ret = this.checkTag(smsBody);
					if (ret == 1) {
						tv.append("\n收到\naddress:" + phoneNumber);
						tv.append("\nbody:" + smsBody);
						this.generateKeyPair(smsBody, phoneNumber);
					} else {
						if (ret == 2) {
							tv.append("\n收到\naddress:" + phoneNumber);
							tv.append("\nbody:" + smsBody);
							secretKey=this.generateKey2(smsBody);
							tv.append("\nsecret key:"+secretKey+"\n");
						} else {
							tv.append("\n收到\naddress:" + phoneNumber);
							this.decryptSMS(smsBody);
						}
					}

				}

			}
			cursor.close();
		}
		// ------------------------------------------------------------

		super.onChange(selfChange);
	}

	private int checkTag(String message) {
		String[] receive = new String[4];
		receive = message.split("&");
		if (receive[0].equals("rk"))
			return 1;
		else if ("tk".equals(receive[0]))
			return 2;
		else
			return 0;
	}

	// 接受协商密钥的短信，并完成本端的密钥协商工作
	private void generateKeyPair(String message, String number) {
		String[] receive = new String[4];
		receive = message.trim().split("&");
		BigInteger[] bgArr = new BigInteger[2];
		bgArr[0] = new BigInteger(receive[1]);
		bgArr[1] = new BigInteger(receive[2]);
		BigInteger[] Bxy = new BigInteger[2];
		Bxy = SelfDefineDH.generateXY_B(bgArr);

		tv.append("Xb:" + Bxy[0].toString() + "\n");
		tv.append("Yb:" + Bxy[1].toString() + "\n");
		String send = Bxy[1].toString();
		//

		this.sendKeySpec(send, number);
		secretKey = this.generateKey(receive[3], Bxy[0].toString(), receive[1]);
		tv.append("密钥:" + secretKey + "\n");

	}

	private void sendKeySpec(String pubKeyEnc, String number) {

		SmsManager sms = SmsManager.getDefault();

		pubKeyEnc = "tk&" + pubKeyEnc;
		if (pubKeyEnc.length() > 70) {
			ArrayList<String> msgs = sms.divideMessage(pubKeyEnc);
			for (String msg : msgs) {
				sms.sendTextMessage(number, null, msg, null, null);
			}
		} else {
			sms.sendTextMessage(number, null, pubKeyEnc, null, null);
		}

	}

	private String generateKey(String y, String x, String p) {

		BigInteger key = BigInteger.ONE;
		key = new BigInteger(y).modPow(new BigInteger(x), new BigInteger(p));
		return key.toString();
	}

	// 接受协商密钥的短信，并完成本端的密钥协商工作
	private String generateKey2(String message) {

		String[] receive = new String[2];
		receive = message.split("&");
		BigInteger Yb = new BigInteger(receive[1]);
		BigInteger key = Yb.modPow(GenerateKeyListener.Axy[0],
				GenerateKeyListener.bgArr[0]);
		return key.toString();

	}
	//解密加密密文
	private void decryptSMS(String message){
		byte[] cipher=Base64.decode(message.getBytes(),Base64.DEFAULT);
		byte[] plain=DESCrypt.decrypt(cipher, SmsObserver.secretKey.substring(0, 12));
		tv.append("\n内容:"+message+"->"+new String(plain)+"\n");
	}
}
