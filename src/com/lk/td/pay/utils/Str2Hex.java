package com.lk.td.pay.utils;

import java.io.ByteArrayOutputStream;

public class Str2Hex {
	private static String hexString = "0123456789ABCDEF";

	

	public static String GetEncodeStr(String str) {
		// ���Ĭ�ϱ����ȡ�ֽ�����
		byte[] bytes = null;
		try {
			bytes = str.getBytes("UTF-8");
		} catch (Exception e) {
			e.printStackTrace();
		}
		StringBuilder sb = new StringBuilder(bytes.length * 2);
		// ���ֽ�������ÿ���ֽڲ���2λ16��������
		for (int i = 0; i < bytes.length; i++) {
			sb.append(hexString.charAt((bytes[i] & 0xf0) >> 4));
			sb.append(hexString.charAt((bytes[i] & 0x0f) >> 0));
		}
		return sb.toString();
	}

	public static String GetDecodeStr(String bytes) {
		String sRes = "";
		ByteArrayOutputStream baos = new ByteArrayOutputStream(
				bytes.length() / 2);
		// ��ÿ2λ16����������װ��һ���ֽ�
		for (int i = 0; i < bytes.length(); i += 2)
			baos.write((hexString.indexOf(bytes.charAt(i)) << 4 | hexString
					.indexOf(bytes.charAt(i + 1))));

		try {
			sRes = new String(baos.toByteArray(), "UTF-8");
		} catch (Exception e) {
			e.printStackTrace();
		}
		return sRes;
	}
}
