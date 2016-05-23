package com.lk.td.pay.utils;

public class AmountUtil {
	/**        
	* 将每三个数字加上逗号处理（通常使用金额方面的编辑）
	* 
	* @param str 无逗号的数字
	* @return 加上逗号的数字
	*/
	public static String addComma(String str) {
		// 将传进数字反转
		String reverseStr = new StringBuilder(str).reverse().toString();
		String strTemp = "";
		for (int i=0; i<reverseStr.length(); i++) { 
			if (i*3+3 > reverseStr.length()){ 
				strTemp += reverseStr.substring(i*3,reverseStr.length()); 
				break; 
			} 
			strTemp += reverseStr.substring(i*3, i*3+3)+","; 
			}
			// 将[789,456,] 中最后一个[,]去除
			if (strTemp.endsWith(",")) { 
				strTemp = strTemp.substring(0, strTemp.length()-1); 
			}
			// 将数字重新反转
			String resultStr = new StringBuilder(strTemp).reverse().toString();        
			return resultStr;
		}
	}
