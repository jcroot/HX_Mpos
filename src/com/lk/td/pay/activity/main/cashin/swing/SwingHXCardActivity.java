package com.lk.td.pay.activity.main.cashin.swing;

import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.HashMap;

import org.apache.http.Header;
import org.json.JSONException;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.lk.td.pay.activity.base.BaseActivity;
import com.lk.td.pay.activity.account.bind.equip.ChooseDeviceActivityBaiFu;
import com.lk.td.pay.activity.account.bind.equip.EquListActivity;
import com.lk.td.pay.beans.BasicResponse;
import com.lk.td.pay.beans.PosData;
import com.lk.td.pay.golbal.Actions;
import com.lk.td.pay.golbal.Urls;
import com.lk.td.pay.tool.MyHttpClient;
import com.lk.td.pay.utils.AmountUtils;
import com.lk.td.pay.utils.CalendarUtil;
import com.lk.td.pay.utils.Utils;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.pax.commonlib.convert.Convert;
import com.pax.yumei.api.YuMeiPaxMpos;
import com.pax.yumei.listener.CheckCardListener;
import com.pax.yumei.listener.ConnectListener;
import com.pax.yumei.listener.GetDeviceInfoListener;
import com.pax.yumei.listener.LoadWorkKeyListener;
import com.pax.yumei.listener.MagProcessListener;
import com.pax.yumei.listener.StartPBOCListener;
import com.pax.yumei.mis.Enum.CardType;
import com.pax.yumei.mis.Enum.KeyType;
import com.pax.yumei.mis.MagProcessResult;
import com.pax.yumei.mis.MposDeviceInfo;
import com.pax.yumei.mis.StartPBOCParam;
import com.pax.yumei.mis.StartPBOCResult;
import com.td.app.pay.hx.R;

public class SwingHXCardActivity extends BaseActivity implements OnClickListener{
	
	private String tradeAmount = "";// 充值金额
	private Context mContext;
	private TextView showAccountText;// 充值钱数
	private TextView showMsgText;// 刷卡状态
	private ProgressBar progressBar;
	private String action;
	private LinearLayout amountLayout;
	public final static int DEVICE_SELECT_BAIFU = 2;
	
	
	
	
	//汇中百富
	private final String[] blutToothtype = { "蓝牙刷卡器" }; 
	private YuMeiPaxMpos manager;
	public  static String []type;//费率类型
	public  static String []flag;
	private ProgressDialog progressDialog;
	private String termTdkAdd="";
	private String zpinkeyAdd="";
	private String zpinkey;
	private String zpincv;
	private String termTdk;
	private String termTdkCv;
	private String macAddress="";
	private String termNo="";
	String termTypeName; //终端厂商名
	private TextView tv_title;
	private Button btn_back;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.swing_hx_card);
		init();
	}
	private void init() {
		manager = YuMeiPaxMpos.getInstance(SwingHXCardActivity.this);
		progressDialog=new ProgressDialog(this);
		mContext = this;
		action = getIntent().getAction();
		amountLayout = (LinearLayout) findViewById(R.id.cashin_amount_layout);
		tv_title = (TextView) findViewById(R.id.tv_title);
		tv_title.setText("刷卡支付");
		btn_back  = (Button) findViewById(R.id.btn_back);
		btn_back.setVisibility(View.VISIBLE);
		btn_back.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				finish();
			}
		});
		progressBar = (ProgressBar) findViewById(R.id.cashin_swing_pb);
		
		showMsgText = (TextView) findViewById(R.id.cashin_show_msg_text);	
		if(action.equals(Actions.ACTION_CASHIN)){   //收款
			tv_title.setText("刷卡支付");
			amountLayout.setVisibility(View.VISIBLE);
			tradeAmount = PosData.getPosData().getPayAmt();
			showAccountText = (TextView) findViewById(R.id.cashin_account_text);
			showAccountText.setText(AmountUtils.changeFen2Yuan(tradeAmount) + "元");
			
			initYuMeiPaxPosSDK();   //百富刷卡
			  
		}else if(action.equals(Actions.ACTION_CHECK)){
			tv_title.setText("设备绑定");
			bindDevice();
		}else if(action.equals(Actions.ACTION_QUERY_BALANCE)){  //余额查询
			tv_title.setText("余额查询");
			
			initYuMeiPaxPosSDK();   //百富刷卡
		}
		
	}
	
	private void bindDevice() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("请选择刷卡器的类型");
		builder.setItems(type, new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface arg0, int position) {
				// 根据选择的刷卡器类型决定跳转到哪个页面
				switch (position) {
				case 0:

					if(Utils.setBlueTooth(SwingHXCardActivity.this)){
						Intent it = new Intent(SwingHXCardActivity.this,EquListActivity.class);
						startActivity(it);
					}
					break;
				}
			}
		});
		builder.create().show();

	}
	
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		/**
		  * 搜索蓝牙设备,链接蓝牙 获取设备信息   获取数卡/插卡信息
		  * 
		  */
		if(resultCode!=RESULT_OK) {
			return;
		}
		    switch (requestCode) {
		    	
		    case DEVICE_SELECT_BAIFU:
		    	 termNo=data.getStringExtra("ksn");
		    	 macAddress=data.getStringExtra("macAddress");
		    	 updateResult1("设备正在初始化..."); 
				 downloadPineky(); 
		    	 connectDevice(false);
		    }
	}
	
	
	
	/***
	 * 初始化POS_SDK
	 */
	private void initYuMeiPaxPosSDK(){
		//百富链接蓝牙刷卡
		 startActivityForResult(new Intent(SwingHXCardActivity.this, ChooseDeviceActivityBaiFu.class),DEVICE_SELECT_BAIFU);
	}
	
	
	/**
	 * 终端签到
	 */
	private void downloadPineky(){
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				HashMap<String, String> params = new HashMap<String, String>();
				params.put("termNo", termNo);
				params.put("termType", "01");//01:蓝牙  02:音频
				MyHttpClient.post(SwingHXCardActivity.this, Urls.BLUETOOTH_SIGN, params,new AsyncHttpResponseHandler() {

							@Override
							public void onSuccess(int statusCode, Header[] headers,byte[] responseBody) {
								try {
									BasicResponse re = new BasicResponse(responseBody).getResult();
									if(re.isSuccess()){
										zpinkey=re.getJsonBody().optString("zpinkey");
										zpincv=re.getJsonBody().optString("zpincv");
										termTdk=re.getJsonBody().optString("termTdk");
										termTdkCv=re.getJsonBody().optString("termTdkCv");
										zpinkeyAdd=zpinkey+zpincv;
										termTdkAdd=termTdk+termTdkCv;
										
										PosData.getPosData().setPinKey(zpinkey);
										connectDevice(false);//1.true 复位  2.false 判断是否连接成功
										
									}else{
										showDialog(re.getMsg());
									}
								} catch (JSONException e) {
									
									e.printStackTrace();
								}
							}

							@Override
							public void onFailure(int statusCode, Header[] headers,byte[] responseBody, Throwable error) {
								networkError(statusCode, error);
							}
							@Override
							public void onStart() {
								showLoadingDialog();
							}
							@Override
							public void onFinish() {
								dismissLoadingDialog();
							}
					});		
			}
		});
		
	}
	
	/**
	 * 连接设备
	 */
	private void connectDevice(boolean rest_flag){
		
		runOnUiThread(new Runnable() {
			
			@Override
			public void run() {
				
				if(progressDialog!=null&&!progressDialog.isShowing()){
					progressDialog.show();
					
				}
				MyOpendeviceListener openListener = new MyOpendeviceListener();
				manager.connect(macAddress, openListener);
				
			}
		});
	}
	
	class MyOpendeviceListener implements ConnectListener {
		
		@Override
		public void onError(int errcode, String errDesc) {
			
			if(progressDialog!=null){
				
				progressDialog.dismiss();
				
			}
			updateResult1("连接失败原因: " + errDesc);
				
		}

		@Override
		public void onSucc() {
//			manager.closeDevice();
			getDeviceInfo();
		}
	
	}
	
	/**
	 * 获取设备信息
	 */
	private void getDeviceInfo(){
		myGetDeviceInfoListener listener = new myGetDeviceInfoListener();
		manager.getDeviceInfo(listener);
		
	}
	
	class myGetDeviceInfoListener implements GetDeviceInfoListener {

		@Override
		public void onError(int errCode, String errDesc) {
			progressDialog.dismiss();
			//updateResult("get device infor error:" + errDesc);
			updateResult1("获取设备信息失败 ： "+errDesc);
		}

		@Override
		public void onSucc(MposDeviceInfo devInfo) {
			 onLoadWorkKey();
			}
		
	}
	
	/**
	 * 灌注工作密钥
	 */
	private void onLoadWorkKey(){
		myLoadWorkKeyTpkListener tpklistener = new myLoadWorkKeyTpkListener();
		manager.loadWorkKey(KeyType.TPK, Convert.str2Bcd(zpinkeyAdd), tpklistener);
		tpklistener.setLoadKey("tpk");
		
	}
	
	class myLoadWorkKeyTpkListener implements LoadWorkKeyListener {

		String loadKey;
		
		public void setLoadKey(String loadKey) {
			this.loadKey = loadKey;
		}

		@Override
		public void onError(int errCode, String errDesc) {
			if(progressDialog!=null)
			progressDialog.dismiss();
			updateResult1("add working key error:" + errDesc);
		}

		@Override
		public void onSucc(KeyType keyType) {
			if(progressDialog!=null)
			 progressDialog.dismiss();
			 myLoadWorkTDKKeyListener  tdklistener = new myLoadWorkTDKKeyListener();
			 manager.loadWorkKey(KeyType.TDK, Convert.str2Bcd(termTdkAdd), tdklistener);
			 tdklistener.setLoadKey("tdk");

		}
	}
	
	
	class myLoadWorkTDKKeyListener implements LoadWorkKeyListener{
        String loadKey;
		
		public void setLoadKey(String loadKey) {
			this.loadKey = loadKey;
		}

		@Override
		public void onError(int arg0, String arg1) {
			if(progressDialog!=null)
			progressDialog.dismiss();
			updateResult1("add working key error:" + arg1);
		}

		@Override
		public void onSucc(KeyType arg0) {
			 if(loadKey.equals("tdk")){
			  updateResult1("请用刷卡器刷卡/插卡...");
			  onCheckCard();
		 }
		}
		
	}
	
	
	private void onCheckCard(){
		myCheckCardListener listener = new myCheckCardListener();
		int timeout = 60;//60s
		manager.checkCard(CardType.MAGNETIC_IC_CARD, timeout, listener);
	}
	
	class myCheckCardListener implements CheckCardListener {

		@Override
		public void onError(int errCode, String errDesc) {
			updateResult1("check card error:" + errDesc);
			if(progressDialog!=null)
			progressDialog.dismiss();
		}

		@Override
		public void onSucc(CardType cardType) {
			updateResult1("check card return " + cardType);
			
			if(cardType == CardType.MAGNETIC_CARD){//刷卡
				
				updateResult1("数据处理中...");
				
				//String amount = "000000000001";
				int timeout = 60; //60s
				myMagProcessListener listener = new myMagProcessListener();
				manager.magProcess(AmountUtils.getSupplementZero(tradeAmount), timeout, listener);
				
				
			}else if(cardType == CardType.IC_CARD){//IC卡
		
				updateResult1("数据处理中...");
			    StartPBOCParam param = new StartPBOCParam();
			   
			    param.setTransType((byte)0x00);
			    param.setDateTime(CalendarUtil.getYYMMDDHHMMSS(new Date()));
			    param.setAuthAmount(AmountUtils.getSupplementZero(tradeAmount));
			    param.setOtherAmount("000000000000");
			   
			    myStartPBOCListener listener = new myStartPBOCListener();
			    manager.startPBOC(param, listener);
			}
			if(progressDialog!=null)
			progressDialog.dismiss();
		}
	}
	
	class myMagProcessListener implements MagProcessListener {

		@Override
		public void onError(int errCode, String errDesc) {
			if(progressDialog!=null)
			progressDialog.dismiss();
			updateResult1("magCard process error:" + errDesc);
		}

		@Override
		public void onSucc(MagProcessResult result) {
			if(progressDialog!=null)
			progressDialog.dismiss();
			String secondTrackStr="";//磁道二
			String thirdTrackStr="";//磁道三
			try {
				
				if(result.getCipherTrack2() != null){
					
					secondTrackStr=new String(result.getCipherTrack2(),"UTF-8");
					
				}
				if(result.getCipherTrack3()!=null){
					thirdTrackStr=new String(result.getCipherTrack3(),"UTF-8");
				}
				
			}catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
			
			String cardNo=result.getPan();//卡号
			String icPinBlock=Convert.bcd2Str(result.getPinblock());
			nextStep(cardNo, secondTrackStr, thirdTrackStr, "", "", "", "01",icPinBlock);
		}
	}
	
	class myStartPBOCListener implements StartPBOCListener {

		@Override
		public void onError(int errCode, String errDesc) {
			updateResult1("start pboc error:" + errDesc);
			if(progressDialog!=null)
			progressDialog.dismiss();
		}

		@Override
		public void onSucc(StartPBOCResult result) {
			if(progressDialog!=null)
			progressDialog.dismiss();
			String icSecondTrackStr;
			try {
				icSecondTrackStr = new String(result.getTrack2Cipher(), "UTF-8");
			} catch (UnsupportedEncodingException e) {
				
				icSecondTrackStr="";
				e.printStackTrace();
			}
			String icThirdTrackStr="";//IC插卡用  磁道三
			String icCardNo=result.getPan();//IC插卡用   卡号
			String icNumber=result.getCardSeq();//IC插卡用   序列号
			String icPeriod=result.getExpiry();//IC插卡用   有效期
			String fRegoin=Convert.bcd2Str(result.getIccData());//IC插卡用      55域(ICData)
			String icPinBlock=Convert.bcd2Str(result.getPinBlock());
			nextStep(icCardNo, icSecondTrackStr, icThirdTrackStr, icPeriod, icNumber, fRegoin, "02",icPinBlock);
		}
	}
	
	
	
	/**
	 * @param cardNo 银行卡号
	 * @param secondTrackStr 二磁
	 * @param thirdTrackStr  三磁
	 * @param period  有效期
	 * @param icNumber  卡序列号
	 * @param icdata    第五域
	 * @param mediaType  刷卡方式
	 */
	private void nextStep(String cardNo,String secondTrackStr,String thirdTrackStr,String period,String icNumber,String icdata,String mediaType,String pinblk){
		PosData.getPosData().setPayAmt(tradeAmount);
		if(cardNo.endsWith("F")||cardNo.endsWith("f")){
			PosData.getPosData().setCardNo(cardNo.substring(0, cardNo.length()-1));
		}else{
			PosData.getPosData().setCardNo(cardNo);
		}
		PosData.getPosData().setCardNo(cardNo);
		PosData.getPosData().setPayType("01");
		PosData.getPosData().setRate("1");
		PosData.getPosData().setBluetoothTermNo(termNo);
		System.out.println(termNo+"______________________________");
		PosData.getPosData().setTermType("01");// 01蓝牙
		PosData.getPosData().setTrack(secondTrackStr + "|"+thirdTrackStr);
		PosData.getPosData().setRandom("000000");
		PosData.getPosData().setMediaType(mediaType);//磁卡类型01 磁条卡 02 IC卡
		PosData.getPosData().setPeriod(period);
		PosData.getPosData().setCrdnum(icNumber);
		PosData.getPosData().setIcdata(icdata);
		PosData.getPosData().setPinblk(pinblk);
		entryOtherActivity();
		
	}
	
	
	/**
	 * 进入签名的Activity
	 */
	private void entryOtherActivity(){
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				if(action.equals(Actions.ACTION_CASHIN)){
				startActivity(new Intent(SwingHXCardActivity.this, SignaturePadActivity.class));
				finish();
				}
				if(action.equals(Actions.ACTION_QUERY_BALANCE)){
					startActivity(new Intent(SwingHXCardActivity.this, CardBalanceConfirmActivity.class));
					finish();
				}
			}
		});
	}
	
	private void updateResult1(final String message){
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				showMsgText.setText(message);
				if(progressDialog!=null&&progressDialog.isShowing()){
					progressDialog.dismiss();
				}
				
			}
		});
		
	}
	
	
	
	
	@Override
	public void onClick(View v) {
		
	}

	
	@Override
	protected void onStart() {
		super.onStart();
	
	}
	
	@Override
	protected void onStop() {
		super.onStop();

	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();

	}
}
