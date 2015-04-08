package com.bruce.crypt;

import java.security.*;

import javax.crypto.*;
import javax.crypto.spec.DESKeySpec;

import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;
public class DESCrypt {

	public static byte[] crypt(byte[] plainText,String key)
	{
		try{
			SecureRandom random=new SecureRandom();
			DESKeySpec desKey=new DESKeySpec(key.getBytes());
			//创建一个密钥工厂,然后用它把DESKeySpec转换成加密密钥
			SecretKeyFactory keyFactory=SecretKeyFactory.getInstance("DES");
			SecretKey secureKey=keyFactory.generateSecret(desKey);
			//用Cipher对象完成加密操作
			Cipher cipher=Cipher.getInstance("DES");
			//用密钥初始化Cipher对象
			cipher.init(Cipher.ENCRYPT_MODE, secureKey,random);
			//加密数据
			return cipher.doFinal(plainText);
		}catch(Exception exc){
			exc.printStackTrace();
		}
		return null;
	}
	
	
	public static byte[] decrypt(byte[] cipherText,String key){
		try{
			SecureRandom random=new SecureRandom();
			DESKeySpec desKey=new DESKeySpec(key.getBytes());
			//创建一个密钥工厂,然后用它把DESKeySpec转换成加密密钥
			SecretKeyFactory keyFactory=SecretKeyFactory.getInstance("DES");
			SecretKey secureKey=keyFactory.generateSecret(desKey);
			//用Cipher对象完成加密操作
			Cipher cipher=Cipher.getInstance("DES");
			//用密钥初始化Cipher对象
			cipher.init(Cipher.DECRYPT_MODE, secureKey,random);
			//加密数据
			return cipher.doFinal(cipherText);
		}catch(Exception exc){
			exc.printStackTrace();
		}	
		
		return null;
		
	}
	
/*	public static void main(String[] args){
		String str="Hello!!!";
		String key="12345678";
		byte[] cipher=crypt(str.getBytes(),key);
		System.out.println("原文: "+str);
					
		System.out.println("密文: "+new String(cipher));
		
		byte[] plain=decrypt(cipher,key);
		System.out.println("明文: "+new String(plain));
	}
	*/
	public static void main(String[]args){
		String str="Hello!!!";
		String key="12345678";
		byte[] cipher=crypt(str.getBytes(),key);
		System.out.println("原文: "+str);
		//--------------------------------------------------------
		BASE64Encoder enc=new BASE64Encoder();
        String baseStr=enc.encode(cipher);		
		//--------------------------------------------------------
		System.out.println("密文: "+new String(cipher));
		System.out.println("密文: "+baseStr);
	//-----------------------------------------------------------
		BASE64Decoder dec=new BASE64Decoder();
		byte[] baseCipher=null;
		try{
			baseCipher=dec.decodeBuffer(baseStr);
		}catch(Exception exc){
			exc.printStackTrace();
		}
	//------------------------------------------------------------	
		byte[] plain=decrypt(baseCipher,key);
		System.out.println("明文: "+new String(plain));
	}
}
