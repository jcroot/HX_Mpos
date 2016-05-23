package com.lk.td.pay.activity.account.auth;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.TextView;

import com.lk.td.pay.activity.base.BaseActivity;
import com.lk.td.pay.beans.CardBean;
import com.lk.td.pay.wedget.CustomDialog;
import com.td.app.pay.hx.R;
import com.lk.td.pay.activity.main.cashin.swing.BindAudioDeviceActivity;
import com.lk.td.pay.beans.BasicResponse;
import com.lk.td.pay.golbal.Urls;
import com.lk.td.pay.golbal.User;
import com.lk.td.pay.tool.Logger;
import com.lk.td.pay.tool.MyHttpClient;
import com.lk.td.pay.tool.T;
import com.loopj.android.http.AsyncHttpResponseHandler;

public class AuthenticationActivity extends BaseActivity implements
		OnClickListener {

	private String realNameStatus, boundPosStatus, checkDetail;
	private TextView tvDetail;
	private String userMp;
	private TextView tv_namestatus, tv_posstatus;
	private Context ctx;
	private String[] typeItem=new String[]{"蓝牙刷卡器","音频刷卡器"};

	private TextView tv_title;
	private Button btn_back;
	CustomDialog dialog;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		ctx = this;
		setContentView(R.layout.authentication);
		getUserInfo();
		init();
	}

	private void init() {
		tv_title = (TextView) findViewById(R.id.tv_title);
		btn_back = (Button)findViewById(R.id.btn_back);
		btn_back.setVisibility(View.VISIBLE);
		tv_title.setText("信息认证");
		btn_back.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				finish();
			}
		});
		tv_namestatus = (TextView) findViewById(R.id.tv_realname_status);
		tv_posstatus = (TextView) findViewById(R.id.tv_posbind_status);
		TextView tvName = (TextView) findViewById(R.id.authentication_name);

		findViewById(R.id.panel_posbind).setOnClickListener(this);
		findViewById(R.id.panel_realname).setOnClickListener(this);
		
		TextView tvBoundMessage = (TextView) findViewById(R.id.tv_bound_pos);

		if ("-1".equals(checkDetail)) {
			tvBoundMessage.setText("刷卡器未绑定");
		} else if ("1".equals(checkDetail)) {
			tvBoundMessage.setText("刷卡器已绑定");
		}
	}

	@Override
	public void onClick(View view) {
		int status = User.uStatus;
		switch (view.getId()) {
		
		case R.id.panel_posbind:
//			if(TextUtils.isEmpty(Infos.pos_status)){
//				if(Infos.uStatus!=2){
//					T.showCustomeShort(ctx, "请实名认证通过后再绑定");
//					return;
//				}
//				Intent real=new Intent(ctx,BluetoothBindActivity.class);
//				startActivity(real);
//				finish();
//			}
			startActivity(new Intent(ctx,BindAudioDeviceActivity.class));
			break;
			
		case R.id.panel_realname:
			if(User.uStatus==2||User.uStatus==1){
				return;
			}
//			Intent real=new Intent(ctx,RealNameAuthenticationActivity.class);
//			startActivity(real);
//			finish();
			break;
		default:
			break;
		}
	}

	private void selectMobilePos() {
		CustomDialog.Builder builder = new CustomDialog.Builder(this);
		builder.setTitle("请选择刷卡器的类型");
		List<CardBean> list = new ArrayList<CardBean>();
		for (int i=0, len = typeItem.length; i<len ; i++){
			CardBean c = new CardBean();
			c.setCardName(typeItem[i]);
			c.setId(i);
			list.add(c);
		}
		builder.setListView(list, new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
				// 根据选择的刷卡器类型决定跳转到哪个页面
				switch (position) {
					case 0:

						break;
					case 1:

						break;
					default:
						break;
				}
				if (dialog!=null){
					dialog.dismiss();
				}
			}
		});
		dialog = builder.create();
		dialog.show();
	}


	private void getUserInfo() {
		HashMap<String, String> map = new HashMap<String, String>();
		map.put("custMobile", User.uAccount);
		MyHttpClient.post(ctx, Urls.GET_USER_INFO,map,
				new AsyncHttpResponseHandler() {

					@Override
					public void onSuccess(int statusCode, Header[] headers,
							byte[] responseBody) {
						System.out.println("-----获取用户信息成功-------");
						if(responseBody!=null){
//						Logger.json(new String(responseBody));
						 try {
							JSONObject json=new JSONObject(new String(responseBody)).getJSONObject("REP_BODY");
							User.cardNum=json.optInt("cardNum");
							User.termNum=json.optInt("termNum");
							User.uStatus=json.optInt("custStatus");
							if(User.uStatus==0){
								tv_namestatus.setText("未认证");
							}else if(User.uStatus==1){
								tv_namestatus.setText("审核中");
							}else if(User.uStatus==2){
								tv_namestatus.setText("已通过");
							}else{
								tv_namestatus.setText("未通过");
							}
						} catch (JSONException e) {
							// TODO 自动生成的 catch 块
							e.printStackTrace();
						}
						 
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

					
					
				});
		MyHttpClient.post(ctx, Urls.BIND_DEVICE_LIST, map, new AsyncHttpResponseHandler() {
			
			@Override
			public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
    				Logger.json("[终端列表]",responseBody);
    				try {
						BasicResponse res=new BasicResponse(responseBody).getResult();
						if(res.isSuccess()){
							JSONArray array=res.getJsonBody().getJSONArray("termList");
							for (int i = 0; i < array.length(); i++) {
                                  								
							}
							if(array.length()==0){
								tv_posstatus.setText("未绑定");
							}else{
								tv_posstatus.setText("已绑定:"+array.length());
							}
						}else{
							T.ss(res.getMsg());
						}
					} catch (JSONException e) {
						// TODO 自动生成的 catch 块
						e.printStackTrace();
					}
    				
			}
			
			@Override
			public void onFailure(int statusCode, Header[] headers,
					byte[] responseBody, Throwable error) {
				networkError(statusCode, error);
			}
			@Override
			public void onFinish() {
				super.onFinish();
				dismissLoadingDialog();
			}
		});
	}
	@Override
	protected void onDestroy() {
		super.onDestroy();
		
		MyHttpClient.cancleRequest(ctx);
	}
	

	private String toS(Object obj) {
		if (null == obj)
			return "";
		return obj.toString();
	}

}
