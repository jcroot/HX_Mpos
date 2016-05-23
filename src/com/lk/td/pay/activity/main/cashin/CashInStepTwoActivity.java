package com.lk.td.pay.activity.main.cashin;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.KeyEvent;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.landicorp.android.mpos.reader.LandiMPos;
import com.landicorp.android.mpos.reader.PBOCStartListener;
import com.landicorp.android.mpos.reader.PBOCStopListener;
import com.landicorp.android.mpos.reader.model.InputPinParameter;
import com.landicorp.android.mpos.reader.model.StartPBOCParam;
import com.landicorp.android.mpos.reader.model.StartPBOCResult;
import com.landicorp.mpos.reader.BasicReaderListeners.CardType;
import com.landicorp.mpos.reader.BasicReaderListeners.EMVProcessListener;
import com.landicorp.mpos.reader.BasicReaderListeners.GetDeviceInfoListener;
import com.landicorp.mpos.reader.BasicReaderListeners.GetTrackDataPlainListener;
import com.landicorp.mpos.reader.BasicReaderListeners.InputPinListener;
import com.landicorp.mpos.reader.BasicReaderListeners.OpenDeviceListener;
import com.landicorp.mpos.reader.BasicReaderListeners.WaitCardType;
import com.landicorp.mpos.reader.BasicReaderListeners.WaitingCardListener;
import com.landicorp.mpos.reader.model.MPosDeviceInfo;
import com.landicorp.mpos.reader.model.MPosEMVProcessResult;
import com.landicorp.robert.comm.api.DeviceInfo;
import com.lk.td.pay.activity.account.bind.equip.EquipmentManagementActivity;
import com.lk.td.pay.activity.base.BaseFragmentActivity;
import com.lk.td.pay.beans.CashInResult;
import com.lk.td.pay.golbal.MApplication;
import com.lk.td.pay.tool.sharedpref.SharedPrefConstant;
import com.lk.td.pay.utils.AmountUtils;
import com.lk.td.pay.utils.ByteArrayUtils;
import com.lk.td.pay.utils.Utils;
import com.lk.td.pay.wedget.CustomDialog;
import com.td.app.pay.hx.R;


public class CashInStepTwoActivity extends BaseFragmentActivity {
	
	private static final int GET_TRACKDATA_PLAIN = 0;
	private static final int START_PBOC = 1;
	private static final int INPUT_PIN = 2;
	private static final int PBOC_STOP = 3;
	private static final int WAIT_SWING = 4;
	//private static final int GET_PAN_PLAIN = 4;
	private LinearLayout cashLayout;
	private TextView showText;
	private TextView accountHintText;
	private TextView accountText;
	private TextView bankNameText;
	private TextView cardNumText;
	private TextView contentText;
	private ImageView contentImv;
	private LandiMPos reader;
	private String amount;          //收款金额
	private String expireData;    //失效日期
	private String panSerial;       //Pan序列号
	private String cardTrack2;             //二磁信息
	private String cardTrack3;             //三磁信息
	private String pwdData;      //pinblock
	private String icCardData;  //ic卡55域数据
	private String usrId;
	private String termphyNo;  //机具SN号码
	private String acNo;             //卡号
	private CardType mCardType;
	private String onlineDataProcessResult;
	private CashInResult result;
	private byte emvTradeType;
	private String mposHint;
	private String custName;
	private String cseqNo; //交易凭证号
	
	
	Handler mHandler = new Handler(){
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case WAIT_SWING:
				
				waitingcard();
				break;
			case GET_TRACKDATA_PLAIN:
				reader.getTrackDataPlain(new GetTrackDataPlainListener() {

					@Override
					public void onError(int errCode, String errDesc) {
						contentText.setText("获取磁道失败" + errDesc);
						
					}

					@Override
					public void onGetTrackDataPlainSucc(String track1,
							String track2, String track3) {
						cardTrack2 = track2;
						cardTrack3 = cardTrack3;
						if(cardTrack2.contains("=")){
							acNo = cardTrack2.split("=")[0];
						} else {
							acNo = cardTrack2.split("D")[0];
						}
						showText.setText("请输入密码，并确认...");
						contentImv.setImageResource(R.drawable.bg_input_pwd);
						mHandler.sendEmptyMessage(INPUT_PIN);
					}
				});
				break;	
			case INPUT_PIN:
				InputPinParameter inputPinParameter = new InputPinParameter();
				inputPinParameter.setCardNO(acNo);
				inputPinParameter.setTimeout((byte) 60000);
				inputPinParameter.setAmount(AmountUtils.change12(amount));
				reader.inputPin(inputPinParameter, new InputPinListener() {
					@Override
					public void onError(int errCode, String errDesc) {
						//showMsg("读取PIN密钥失败" + errDesc);
						contentText.setText("读取PIN密钥失败" + errDesc);
					
					}

					@Override
					public void onInputPinSucc(byte[] pinblock) {
						pwdData = ByteArrayUtils.byteArray2HexString(pinblock);
						cashIn();
					}
				});
				break;	
			case START_PBOC:
				final StartPBOCParam startPBOCParam = new StartPBOCParam();
//				byte emvTradeType = 0x00;
				startPBOCParam.setTransactionType(emvTradeType);
				startPBOCParam.setAuthorizedAmount(AmountUtils.change12(amount));
				startPBOCParam.setOtherAmount("000000000000");
				startPBOCParam.setDate(Utils.getCurrentDate("yyMMdd"));
				startPBOCParam.setTime(Utils.getCurrentDate("HHmmss")); // "pos_time":
				startPBOCParam.setForbidContactCard(false);
				startPBOCParam.setForceOnline(true);
				startPBOCParam.setForbidMagicCard(false);
				startPBOCParam.setForbidContactlessCard(false);
				reader.startPBOC(startPBOCParam,
						new EMVProcessListener() {
							@Override
							public void onError(int errCode, String errDesc) {
								contentText.setText("读取二磁道失败" + errDesc);
							}

							@Override
							public void onEMVProcessSucc(
									MPosEMVProcessResult result) {
								expireData = result.getExpireData();
								panSerial = result.getPanSerial();
								cardTrack2 = result.getTrack2();								
								acNo = result.getCredentialNo();
								showText.setText("请输入密码，并确认...");
								contentImv.setImageResource(R.drawable.bg_input_pwd);
							}
						}, new PBOCStartListener() {
							@Override
							public void onError(int errCode, String errDesc) {
								contentText.setText("交易失败" + errDesc);
							}

					@Override
					public void onPBOCStartSuccess(StartPBOCResult result) {
						pwdData = ByteArrayUtils.byteArray2HexString(result.getPwdData());
						icCardData = ByteArrayUtils.byteArray2HexString(result.getICCardData());
						mHandler.sendEmptyMessage(PBOC_STOP);
						cashIn();
						
					}
				});
				break;
			
			case PBOC_STOP:
				reader.PBOCStop(new PBOCStopListener() {

					@Override
					public void onError(int errCode, String errDesc) {
						//showMsg("交易失败" + errDesc);
						//showSucc(result);
					}

					@Override
					public void onPBOCStopSuccess() {
						//contentText.setText("交易成功");
						//showSucc(result);
					}
				});
				break;
			
			default:
				break;
			}
		};
	};
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.cashin_step_two);
		reader = LandiMPos.getInstance(getApplicationContext());
	
		init();
		initView();
	}

	private void init(){
		amount = getIntent().getStringExtra("account");
		//termphyNo = getIntent().getStringExtra("termphyNo");

		usrId = MApplication.mSharedPref.getSharePrefString(SharedPrefConstant.USER_ACCOUNT);
		custName = MApplication.mSharedPref.getSharePrefString(SharedPrefConstant.USERNAME);
	}
	
	private void initView() {
		cashLayout = (LinearLayout) findViewById(R.id.cashin_step_two_layout);
		showText = (TextView) findViewById(R.id.cashin_show_msg_text);
		accountHintText = (TextView) findViewById(R.id.cashin_content_hint_text);
		accountText = (TextView) findViewById(R.id.cashin_account_text);
		emvTradeType = 0x00;
		mposHint = "收款";
		accountHintText.setText("收款金额");
		accountText.setText(amount + "元");
		
		bankNameText = (TextView) findViewById(R.id.cashin_bank_name_text);
		cardNumText = (TextView) findViewById(R.id.cashin_card_num_text);
		contentText = (TextView) findViewById(R.id.cashin_content_text);
		contentImv = (ImageView) findViewById(R.id.cashin_content_img);
		
		bankNameText.setText(MApplication.mSharedPref.getSharePrefString(SharedPrefConstant.USER_OPNBNK));
		cardNumText.setText(MApplication.mSharedPref.getSharePrefString(SharedPrefConstant.USER_ACTNO));
		//waitingcard();
		openDevice();
	}

	private void waitingcard() {
		//showLoadingDialog("正在交易。。。");
		showText.setText("请用刷卡器刷卡/插卡...");
		reader.waitingCard(WaitCardType.MAGNETIC_IC_CARD_RFCARD, AmountUtils.change12(amount),mposHint, 60000,
				new WaitingCardListener() {
					@Override
					public void onError(int errCode, String errDesc) {

						contentText.setText("交易失败" + errDesc);
					}

					@Override
					public void onWaitingCardSucc(CardType cardType) {
						mCardType = cardType;
						//contentText.setText("卡片类型：" + mCardType.name());
						if (mCardType.equals(CardType.IC_CARD)) {							
							//contentText.setText("暂不支持IC卡");
							mHandler.sendEmptyMessage(START_PBOC);
						} else {
							mHandler.sendEmptyMessage(GET_TRACKDATA_PLAIN);
						}
						
					}
				});
	}
	
	private void showMsg(String mag){
		new CustomDialog.Builder(this)
		.setTitle(getString(R.string.app_name))
		.setMessage(mag)
		.setOkBtn(getString(R.string.ok),
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog,
							int which) {
						dialog.cancel();
						finish();
					}
				}).create().show();
	}
	
	private void cashIn(){
		Intent it = new Intent(this, CashInStepThreeActivity.class);
		it.putExtra("usrId", usrId);
		it.putExtra("termphyNo", termphyNo);
		it.putExtra("amount", amount);
		it.putExtra("expireData", expireData);
		it.putExtra("mCardType", mCardType.name());
		it.putExtra("panSerial", panSerial);
		it.putExtra("cardTrack2", cardTrack2);
		it.putExtra("cardTrack3", cardTrack3);
		it.putExtra("pwdData", pwdData);
		it.putExtra("icCardData", icCardData);
		startActivity(it);
	}
	
	private void openDevice() {
		showLoadingDialog();
		DeviceInfo deviceInfo = MApplication.mSharedPref.getDeviceInfo();
		if(null == deviceInfo){
			selectEqu();
			
		} else {
			reader.openDevice(deviceInfo, new OpenDeviceListener() {
				@Override
				public void openSucc() {

					reader.getDeviceInfo(new GetDeviceInfoListener() {
						
						@Override
						public void onError(int arg0, String arg1) {
							dismissLoadingDialog();
							showMsg("获取设备信息失败"+arg1);
							
						}
						
						@Override
						public void onGetDeviceInfoSucc(MPosDeviceInfo arg0) {
							termphyNo = arg0.deviceSN;
							dismissLoadingDialog();
							mHandler.sendEmptyMessage(WAIT_SWING);
							
						}
					});
				}

				@Override
				public void openFail() {				
					dismissLoadingDialog();
					selectEqu();
					
				}

			});
		}
		
	}
	
	private void selectEqu(){
		new CustomDialog.Builder(this)
		.setTitle(getString(R.string.app_name))
		.setMessage("当前未绑定设备，前往设备签到？")
		.setOkBtn(getString(R.string.ok),
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog,
							int which) {
						dismissLoadingDialog();
						goEquMan();
					}
				})
		.setCancelBtn(getString(R.string.cancel),
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog,
							int which) {
						dismissLoadingDialog();
						dialog.cancel();
					}
		}).create().show();
		
	}
	
	private void goEquMan() {
		Intent it = new Intent(this, EquipmentManagementActivity.class);
		startActivity(it);
	}
	
	@Override
	public boolean dispatchKeyEvent(KeyEvent event) {
		if (event.getAction() == KeyEvent.ACTION_DOWN
				&& event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
			reader.cancleTrade();			
		}
		return super.dispatchKeyEvent(event);
	}
	
}
