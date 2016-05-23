package com.lk.td.pay.utils;

import java.util.HashMap;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

public class DataParse {
	private static DataParse dataParse = null;
	private static Gson gson = null;
	public static DataParse getInstance(){
		if(dataParse == null){
			dataParse = new DataParse();
		}
		return dataParse;
	}
	
	private DataParse(){
		gson = new Gson();
	}
	
	public HashMap<String, Object> parse(String content){
		HashMap<String, HashMap<String, Object>> retMap = gson.fromJson(content,  
                new TypeToken<HashMap<String, HashMap<String, Object>>>() {}.getType());
		return retMap.get("REP_BODY");
	}
}
