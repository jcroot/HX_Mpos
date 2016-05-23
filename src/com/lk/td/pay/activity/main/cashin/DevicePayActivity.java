package com.lk.td.pay.activity.main.cashin;

import java.util.HashMap;

import org.apache.http.Header;
import org.json.JSONException;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.lk.td.pay.activity.base.BaseActivity;
import com.lk.td.pay.activity.main.cashin.swing.SwingHXCardActivity;
import com.lk.td.pay.beans.BasicResponse;
import com.lk.td.pay.beans.BindDeviceInfo;
import com.lk.td.pay.beans.PosData;
import com.lk.td.pay.golbal.Actions;
import com.lk.td.pay.golbal.Urls;
import com.lk.td.pay.tool.Logger;
import com.lk.td.pay.tool.MyHttpClient;
import com.lk.td.pay.utils.AmountUtils;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.td.app.pay.hx.R;

public class DevicePayActivity extends BaseActivity {

	private TextView tv_title;
	private Button btn_back;
	TextView tv_type,tv_amt,tv_name;

	BindDeviceInfo info;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.device_pay_layout);
		tv_title = find(R.id.tv_title);
		tv_title.setText("设备押金信息");
		btn_back = (Button) findViewById(R.id.btn_back);
		btn_back.setVisibility(View.VISIBLE);
		btn_back.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				finish();
			}
		});
        tv_type=find(R.id.tv_type);
        tv_amt=find(R.id.tv_amt);
        tv_name=find(R.id.tv_target);
        info=(BindDeviceInfo) getIntent().getSerializableExtra("data");
        tv_type.setText(info.getType());
        tv_amt.setText(AmountUtils.changeFen2Yuan(info.getTermPayAmt())+" 元");
        tv_name.setText(info.getTermRecipient());
        findViewById(R.id.btn_buy_device).setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				createOrder();		
			}
		});
	}

	
	private void createOrder(){
		HashMap<String, String> params=new HashMap<String, String>();
		params.put("prdordType", "02");
		params.put("bizType", "800001");
		params.put("prdordAmt", info.getTermPayAmt());
		params.put("prdName", "刷卡头"
				);
		params.put("price", info.getTermPayAmt());
		MyHttpClient.post(this, Urls.CREATE_ORDER, params, new AsyncHttpResponseHandler() {
			
			@Override
			public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
				Logger.json("[CreateOrder]",responseBody);
				try {
					BasicResponse r=new BasicResponse(responseBody).getResult();
					if(r.isSuccess()){
						String ord=r.getJsonBody().getString("prdordNo");
						
						 PosData.getPosData().setPrdordNo(r.getJsonBody().getString("prdordNo"));
                         PosData.getPosData().setPayAmt(info.getTermPayAmt());
                         PosData.getPosData().setRate("1");
                         startActivity(new Intent(DevicePayActivity.this,SwingHXCardActivity.class).setAction(Actions.ACTION_CASHIN));
                 		finish();
					}else{
						showDialog(r.getMsg());
					}
				} catch (JSONException e) {
					e.printStackTrace();
				}
				
				
			}
			
			@Override
			public void onFailure(int statusCode, Header[] headers,
					byte[] responseBody, Throwable error) {
				networkError(statusCode, error);
			}
			@Override
			public void onStart() {
				super.onStart();
				showLoadingDialog();
			}
			@Override
			public void onFinish() {
				super.onFinish();
				dismissLoadingDialog();
			}
		});
		
	}
	private <T extends View> T find(int id) {
		return (T) findViewById(id);
	}
}
