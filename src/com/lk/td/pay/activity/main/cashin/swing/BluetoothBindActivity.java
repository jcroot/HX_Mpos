package com.lk.td.pay.activity.main.cashin.swing;

import java.util.HashMap;

import org.apache.http.Header;
import org.json.JSONException;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.landicorp.android.mpos.reader.LandiMPos;
import com.landicorp.mpos.reader.BasicReaderListeners.GetDeviceInfoListener;
import com.landicorp.mpos.reader.BasicReaderListeners.OpenDeviceListener;
import com.landicorp.mpos.reader.model.MPosDeviceInfo;
import com.landicorp.robert.comm.api.CommunicationManagerBase.CommunicationMode;
import com.landicorp.robert.comm.api.CommunicationManagerBase.DeviceSearchListener;
import com.landicorp.robert.comm.api.DeviceInfo;
import com.lk.td.pay.activity.base.BaseActivity;
import com.lk.td.pay.adapter.BluetoothAdapter;
import com.lk.td.pay.beans.BasicResponse;
import com.lk.td.pay.beans.Order;
import com.lk.td.pay.golbal.Urls;
import com.lk.td.pay.tool.Logger;
import com.lk.td.pay.tool.MyHttpClient;
import com.lk.td.pay.tool.T;
import com.lk.td.pay.wedget.CustomDialog;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.td.app.pay.hx.R;

public class BluetoothBindActivity extends BaseActivity implements
		OnItemClickListener, DeviceSearchListener {

	private TextView currentText;
	private ListView listview;
	private BluetoothAdapter adapter;
	private LandiMPos reader;
	private ProgressBar progressBar;
	private DeviceInfo deviceInfo;
	private Context mContext;
	private String posType = "";
	public static final int ACTION_SIGN = 0;
	public static final int ACTION_BOUND = 1;
	private int action;
	private String pinkey;
	private Order order = new Order();
	private Button btn_back;
	private TextView tv_title;

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.bluetooth_bind);
		// posType = getIntent().getStringExtra("POSTYPE");
		mContext = this;
		initView();
		action = getIntent().getIntExtra("action", -1);
	}

	private void initView() {
		btn_back = (Button) findViewById(R.id.btn_back);
		tv_title = (TextView) findViewById(R.id.tv_title);
		btn_back.setVisibility(View.VISIBLE);
		tv_title.setText("绑定刷卡器");
		btn_back.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				finish();
			}
		});
		currentText = (TextView) findViewById(R.id.equ_man_current_text);
		currentText.setText("无");
		listview = (ListView) findViewById(R.id.equ_man_listview);
		progressBar = (ProgressBar) findViewById(R.id.equ_man_progressbar);
		adapter = new BluetoothAdapter(this);
		listview.setAdapter(adapter);
		listview.setOnItemClickListener(this);
		progressBar.setVisibility(View.VISIBLE);
		reader = LandiMPos.getInstance(getApplicationContext());
		reader.startSearchDev(this, true, true, 60000);
		findViewById(R.id.equ_man_stop_search_btn).setOnClickListener(
				new OnClickListener() {

					@Override
					public void onClick(View v) {
						reader.stopSearchDev();

					}
				});
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		deviceInfo = adapter.getDeviceInfo(position);
		bindEqu(position);

	}

	private void bindEqu(final int position) {
		CustomDialog.Builder builder = new CustomDialog.Builder(this);
		builder.setTitle(getResources().getString(R.string.app_name));
		builder.setMessage("您确定要绑定此设备?");
		builder.setOkBtn("确定", new DialogInterface.OnClickListener() {

			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
				if (deviceInfo.getName() != null) {

					reader.stopSearchDev();
					
					// if (deviceInfo.getName() != null) {
					openDevice();
					// } else {
					// Toast.makeText(mContext, "未知设备，请重新选择",
					// Toast.LENGTH_LONG).show();
					// }
					// openDevice();

				} else {
					// showToast("未知设备，请重新选择");
					Toast.makeText(mContext, "未知设备，请重新选择", Toast.LENGTH_LONG)
							.show();
				}
			}
		});
		builder.setCancelBtn("取消", new DialogInterface.OnClickListener() {

			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();

			}
		});
		builder.create().show();
	}

	CustomDialog.Builder alert;

	private void showMsg(String mag) {
		try {
			alert = new CustomDialog.Builder(this)
					.setTitle(getString(R.string.app_name))
					.setMessage(mag)
					.setOkBtn(getString(R.string.confirm),
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									dialog.cancel();
									finish();
								}
							});
			alert.create().show();
		} catch (Exception e) {
			finish();
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		dismissLoadingDialog();

	}

	@Override
	public void discoverComplete() {
		progressBar.setVisibility(View.INVISIBLE);

	}

	@Override
	public void discoverOneDevice(DeviceInfo arg0) {
		if (arg0.getName() != null) {
			adapter.addDevice(arg0);
		}

	}

	@Override
	public boolean dispatchKeyEvent(KeyEvent event) {
		if (event.getAction() == KeyEvent.ACTION_DOWN
				&& event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
			reader.cancleTrade();
		}
		return super.dispatchKeyEvent(event);
	}

	private void openDevice() {
		showLoadingDialog();
		reader.openDevice(CommunicationMode.MODE_DUPLEX, deviceInfo,
				new OpenDeviceListener() {
					@Override
					public void openSucc() {

						reader.getDeviceInfo(new GetDeviceInfoListener() {

							@Override
							public void onError(int arg0, String arg1) {
								// dismissLoadingDialog();
								showMsg("获取设备信息失败" + arg1);

							}

							@Override
							public void onGetDeviceInfoSucc(MPosDeviceInfo arg0) {
								goCheck(arg0.deviceSN);
							}
						});
					}

					@Override
					public void openFail() {
						dismissLoadingDialog();
						showMsg("打开刷卡器设备失败");
					}

				});

	}

	private void goCheck(String deviceSN) {
		HashMap<String, String> params = new HashMap<String, String>();
		params.put("termNo", deviceSN);
		MyHttpClient.post(mContext, Urls.POS_BING, params,
				new AsyncHttpResponseHandler() {

					@Override
					public void onSuccess(int statusCode, Header[] headers,
							byte[] responseBody) {
						Logger.json(new String(responseBody));
						try {
							BasicResponse response = new BasicResponse(
									responseBody).getResult();
							if (response.isSuccess()) {
								T.showCustomeOk(mContext, "绑定成功！");
								
								finish();

							} else {
								T.ss(response.getMsg());
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
		// Logger.d("bond");
		// MApplication.mSharedPref.putDeviceInfo(deviceInfo);
		// Intent it = new Intent(this, BoundMobilePos.class);
		// it.putExtra("TRMMODNO", deviceSN);
		// it.putExtra("POSTYPE", posType);
		// startActivity(it);
		// finish();
	}

}
