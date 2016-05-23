package com.lk.td.pay.utils;
import java.security.Key;
import java.security.Security;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import com.sun.crypto.provider.SunJCE;
public class CipherUtil {
	public static String deskey = "{PONY}"; //Ĭ�ϸ�ֵ
	
	public static String encrypt(String strIn)throws Exception{
	    Security.addProvider(new SunJCE());
	    Key key = getKey(deskey);
	    
	    Cipher encryptCipher = Cipher.getInstance("DES");
	    encryptCipher.init(1, key);
	
	    return byteArr2HexStr(encryptCipher.doFinal(strIn.getBytes()));
	}
	
	private static Key getKey(String strKey) throws Exception{
	    byte[] arrBTmp = strKey.getBytes();
	    byte[] arrB = new byte[8];
	    for (int i = 0; (i < arrBTmp.length) && (i < arrB.length); ++i) {
	      arrB[i] = arrBTmp[i];
	    }
	    Key key = new SecretKeySpec(arrB, "DES");
	    return key;
	}
	
	private static String byteArr2HexStr(byte[] arrB) throws Exception {
	    int iLen = arrB.length;
	    StringBuffer sb = new StringBuffer(iLen * 2);
	    for (int i = 0; i < iLen; ++i) {
	    	int intTmp = arrB[i];
	      	while (intTmp < 0) {
	    	  	intTmp += 256;
	      	}
	      	if (intTmp < 16) {
	      		sb.append("0");
	      	}
	      	sb.append(Integer.toString(intTmp, 16));
	    }
	    return sb.toString();
	}
}
