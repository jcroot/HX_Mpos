package com.lk.td.pay.activity.main.cashin.swing;

import java.util.HashMap;

import org.apache.http.Header;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
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
import com.landicorp.mpos.reader.BasicReaderListeners.LoadPinKeyListener;
import com.landicorp.mpos.reader.BasicReaderListeners.OpenDeviceListener;
import com.landicorp.mpos.reader.model.MPosDeviceInfo;
import com.landicorp.robert.comm.api.CommunicationManagerBase.DeviceSearchListener;
import com.landicorp.robert.comm.api.DeviceInfo;
import com.lk.td.pay.activity.base.BaseActivity;
import com.lk.td.pay.adapter.BluetoothAdapter;
import com.lk.td.pay.golbal.MApplication;
import com.lk.td.pay.golbal.Urls;
import com.lk.td.pay.tool.Logger;
import com.lk.td.pay.tool.MyHttpClient;
import com.lk.td.pay.tool.T;
import com.lk.td.pay.utils.ByteArrayUtils;
import com.lk.td.pay.wedget.CustomDialog;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.td.app.pay.hx.R;

public class LDWriteSignActivity extends BaseActivity implements
		OnItemClickListener, DeviceSearchListener {
	private static final int LOAD_PINKEY = 0;
	private static final int LOAD_SUCCESS = 1;

	private TextView currentText;
	private ListView listview;
	private BluetoothAdapter adapter;
	private LandiMPos reader;
	private ProgressBar progressBar;
	private byte[] pinKey = null;
	byte masterKeyIndex = 0x00;
	private DeviceInfo deviceInfo;
	private String userName;

	private Button btn_back;
	private TextView tv_title;
	// private String termphyNo;

	Handler mHandler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case LOAD_PINKEY:
				loadPinKey();
				break;
			case LOAD_SUCCESS:
				dismissLoadingDialog();
				currentText.setText(deviceInfo.getName());
				showMsg("成功绑定蓝牙刷卡器");
			default:
				break;
			}
		};
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.equipment_management);
		initView();
	}

	private void initView() {
		tv_title = (TextView) findViewById(R.id.tv_title);
		tv_title.setText("设备连接管理器");
		btn_back = (Button) findViewById(R.id.btn_back);
		btn_back.setVisibility(View.VISIBLE);
		btn_back.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				finish();
			}
		});
		currentText = (TextView) findViewById(R.id.equ_man_current_text);
		progressBar = (ProgressBar) findViewById(R.id.equ_man_progressbar);

		deviceInfo = MApplication.mSharedPref.getDeviceInfo();
		reader = LandiMPos.getInstance(getApplicationContext());
		if (deviceInfo != null && reader.isConnected()) {
			openDevice();
		} else {
			reader.startSearchDev(this, true, true, 60000);
			listview = (ListView) findViewById(R.id.equ_man_listview);
			adapter = new BluetoothAdapter(this);
			listview.setAdapter(adapter);
			progressBar.setVisibility(View.VISIBLE);
			listview.setOnItemClickListener(this);
			findViewById(R.id.equ_man_stop_search_btn).setOnClickListener(
					new OnClickListener() {

						@Override
						public void onClick(View v) {
							reader.stopSearchDev();

						}
					});

		}

		// if (reader.isConnected()) {
		//
		// } else {
		// reader.startSearchDev(this, true, true, 60000);
		//
		// }
		// if (null == deviceInfo) {
		// currentText.setText("无");
		// } else {
		// currentText.setText(deviceInfo.getName());
		//
		// }
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		reader.stopSearchDev();
		deviceInfo = adapter.getDeviceInfo(position);
		// if("1".equals(dowFlg)){
		bindEqu(position);
		// } else {
		// showToast(deviceInfo.getName() + "已经绑定。");
		// }
	}

	private void bindEqu(final int position) {
		CustomDialog.Builder builder = new CustomDialog.Builder(this);
		builder.setTitle(getResources().getString(R.string.app_name));
		builder.setMessage("确定此设备签到?");
		builder.setOkBtn("确定", new DialogInterface.OnClickListener() {

			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
				if (deviceInfo.getName() != null) {
					// writeDevice(deviceInfo);
					openDevice();

				} else {
					Toast.makeText(LDWriteSignActivity.this, "未知设备，请重新选择",
							Toast.LENGTH_SHORT).show();
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

	private void loadPinKey() {
		reader.loadPinKey(masterKeyIndex, pinKey, new LoadPinKeyListener() {
			@Override
			public void onError(int errCode, String errDesc) {
				dismissLoadingDialog();
				showMsg("失败" + errDesc);
				// T.showCustomeShort(LDCheckActivity.this, "错误："+errDesc);
			}

			@Override
			public void onLoadPinKeySucc() {
				System.out.println("-------------签到成功--------------");
				mHandler.sendEmptyMessage(LOAD_SUCCESS);
			}
		});
	}

	private void showMsg(String mag) {
		try {
			new CustomDialog.Builder(this)
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
							}).create().show();
		} catch (Exception e) {
			e.printStackTrace();
		}
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
			reader.stopSearchDev();
			return super.dispatchKeyEvent(event);
		} else {
			return super.dispatchKeyEvent(event);
		}
	}

	private void openDevice() {
		// showLoadingDialog("正在签到...");
		reader.openDevice(deviceInfo, new OpenDeviceListener() {
			@Override
			public void openSucc() {

				reader.getDeviceInfo(new GetDeviceInfoListener() {

					@Override
					public void onError(int arg0, String arg1) {
						dismissLoadingDialog();
						showMsg("获取设备信息失败" + arg1);

					}

					@Override
					public void onGetDeviceInfoSucc(MPosDeviceInfo arg0) {
						downKey(arg0.deviceSN);

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

	private void downKey(String deviceSN) {
		HashMap<String, String> params = new HashMap<String, String>();
		params.put("termNo", deviceSN);
		params.put("termType", "01");
		MyHttpClient.post(this, Urls.BLUETOOTH_SIGN, params,
				new AsyncHttpResponseHandler() {

					@Override
					public void onSuccess(int statusCode, Header[] headers,
							byte[] responseBody) {
						Logger.json(new String(responseBody));
						try {
							JSONObject json = new JSONObject(new String(
									responseBody)).getJSONObject("REP_BODY");
							if(json.getString("RSPCOD").equals("000000")){
								
								pinKey = ByteArrayUtils.hexString2ByteArray(json
										.get("zpik").toString());
								// System.out.println("key="+key);
								System.out.println("two="
										+ ByteArrayUtils.hexString2ByteArray(json
												.get("zpik").toString()));
								// reader.
								loadPinKey();
							}else{
								T.ss(""+json.optString("RSPMSG"));
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
					public void onStart() {
						super.onStart();
						showLoadingDialog("正在写入密匙...");
					}

					@Override
					public void onFinish() {
						super.onFinish();
						dismissLoadingDialog();
					}
				});
	}

	// class CheckTask extends AsyncTask<String, Integer, Map<String, Object>> {
	// private ShowDialog dialog;
	//
	// @Override
	// protected void onPreExecute() {
	// try {
	// dialog = new ShowDialog(LDCheckActivity.this);
	// dialog.setCanceledOnTouchOutside(false);
	// dialog.show();
	// } catch (Exception e) {
	// }
	// super.onPreExecute();
	// }
	//
	// @Override
	// protected Map<String, Object> doInBackground(String... params) {
	// HashMap<String, Object> map = new HashMap<String, Object>();
	// map.put("USRMP", params[0]);
	// map.put("TERMID", params[1]);
	// return NetCommunicate.getData(URLs.LD_CHECK, map);
	// }
	//
	// @Override
	// protected void onPostExecute(Map<String, Object> result) {
	// if(null == LDCheckActivity.this){
	// return;
	// }
	// if (null != result) {
	// if (Entity.STATE_OK.equals(result.get(Entity.RSPCOD))) {
	// pinKey =
	// ByteArrayUtils.hexString2ByteArray(result.get("PINKEY").toString());
	// mHandler.sendEmptyMessage(LOAD_PINKEY);
	// } else {
	// dialog.cancel();
	// Toast.makeText(LDCheckActivity.this,result.get(Entity.RSPMSG).toString(),Toast.LENGTH_SHORT).show();
	// }
	// } else {
	// dialog.cancel();
	// Toast.makeText(LDCheckActivity.this, "网络异常", Toast.LENGTH_SHORT).show();
	// }
	//
	// super.onPostExecute(result);
	// }
	// }
}
