package com.lk.td.pay.activity.main.cashin.swing;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
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
import com.landicorp.mpos.reader.BasicReaderListeners.GetPANListener;
import com.landicorp.mpos.reader.BasicReaderListeners.GetTrackDataCipherListener;
import com.landicorp.mpos.reader.BasicReaderListeners.InputPinListener;
import com.landicorp.mpos.reader.BasicReaderListeners.OpenDeviceListener;
import com.landicorp.mpos.reader.BasicReaderListeners.WaitCardType;
import com.landicorp.mpos.reader.BasicReaderListeners.WaitingCardListener;
import com.landicorp.mpos.reader.model.MPosDeviceInfo;
import com.landicorp.mpos.reader.model.MPosEMVProcessResult;
import com.landicorp.robert.comm.api.CommunicationManagerBase.CommunicationMode;
import com.landicorp.robert.comm.api.DeviceInfo;
import com.lk.td.pay.activity.base.BaseActivity;
import com.lk.td.pay.beans.Order;
import com.lk.td.pay.utils.AmountUtils;
import com.lk.td.pay.utils.ByteArrayUtils;
import com.lk.td.pay.utils.Utils;
import com.lk.td.pay.wedget.CustomDialog;
import com.td.app.pay.hx.R;

public class SwingLDBTCardActivity extends BaseActivity {

	private static final int GET_TRACKDATA_PLAIN = 0;
	private static final int START_PBOC = 1;
	private static final int INPUT_PIN = 2;
	private static final int PBOC_STOP = 3;
	private static final int GET_PAN_PLAIN = 4;
	private static final int WAIT_SWING = 5;
	private LinearLayout cashLayout;
	private TextView showText;
	private TextView accountHintText;
	private TextView accountText;
	private TextView contentText;
	private ImageView contentImv;
	private LandiMPos reader;
	private String amount; // 收款金额
	private String expireData; // 失效日期
	private String panSerial; // Pan序列号
	private String cardTrack2; // 二磁信息
	private String cardTrack3; // 三磁信息
	private String pwdData; // pinblock
	private String icCardData; // ic卡55域数据
	private String posId = ""; // 机具SN号码
	private String rateType = "";
	private String acNo; // 卡号
	private CardType mCardType;
	private String onlineDataProcessResult;
	private byte emvTradeType;
	private String mposHint;
	private String action;
	private String paytType, tratyp;
	private DeviceInfo deviceInfo;
	private Order order = new Order();
	private TextView tv_title;
	private Button btn_back;

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.swing_card_bluetooth);
		reader = LandiMPos.getInstance(getApplicationContext());
		init();
		initView();
	}

	private void init() {
		action = getIntent().getAction();
		amount = getIntent().getStringExtra("TXAMT");
		posId = getIntent().getStringExtra("POSTYPE");
		rateType = getIntent().getStringExtra("rateType");
		tratyp = getIntent().getStringExtra("tratyp");
		deviceInfo = null;
		order.setTratyp(tratyp);
		order.setTXAMT(amount);
		order.setRateType(rateType);
		// order.setTrmmodno(deviceInfo.getIdentifier());
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

		contentText = (TextView) findViewById(R.id.cashin_content_text);
		contentImv = (ImageView) findViewById(R.id.cashin_content_img);
		tv_title = (TextView) findViewById(R.id.tv_title);
		tv_title.setText("刷卡支付");
		btn_back = (Button) findViewById(R.id.btn_back);
		btn_back.setVisibility(View.VISIBLE);
		btn_back.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				finish();
			}
		});
		if(null!=action){
			if(action.equals("query")){
				cashLayout.setVisibility(View.GONE);
				tv_title.setText("余额查询");
			}
		}
		openDevice();
		// waitingcard();
	}

	private void openDevice() {
		System.out.println("----开始连接设备------");
		if (null == deviceInfo) {
			System.out.println("-------设备号丢失，请重新绑定刷卡器------------");
			contentText.setText("设备号丢失，请重新绑定刷卡器。");
//			SweetAlertDialog dia = new SweetAlertDialog(
//					SwingLDCardActivity.this, SweetAlertDialog.WARNING_TYPE);
//			dia.setTitleText("操作提示").setContentText("设备号丢失，请重新获取后再尝试!");
//			dia.setConfirmClickListener(new OnSweetClickListener() {
//
//				@Override
//				public void onClick(SweetAlertDialog sweetAlertDialog) {
//					sweetAlertDialog.dismissWithAnimation();
//					Intent getinfo = new Intent(SwingLDCardActivity.this,
//							GetDeviceInfo.class);
//					startActivity(getinfo);
//					finish();
//				}
//			});
//			dia.show();

		} else {

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
								public void onGetDeviceInfoSucc(
										MPosDeviceInfo arg0) {
									System.out
											.println("-------获取设备信息成功------------");
									// 如果返回的posSN不为空
//									if (!TextUtils.isEmpty(Infos.posSn)) {
//										if (!Infos.posSn.equals(arg0.deviceSN)) {// 选取的设备sn与服务器不匹配
//											reader.closeDevice(null);
//											SweetAlertDialog dia = new SweetAlertDialog(
//													SwingLDCardActivity.this,
//													SweetAlertDialog.WARNING_TYPE)
//													.setTitleText(
//															"连接的设备和绑定的设备不符，请重新选择设备")
//													.setConfirmClickListener(
//															new OnSweetClickListener() {
//
//																@Override
//																public void onClick(
//																		SweetAlertDialog sweetAlertDialog) {
//																	sweetAlertDialog
//																			.dismissWithAnimation();
//																	Intent rechoose = new Intent(
//																			SwingLDCardActivity.this,
//																			GetDeviceInfo.class);
//																	rechoose.setAction("rechoose");
//																	startActivity(rechoose);
//																	finish();
//																}
//															})
//													.setConfirmText("重新选择");
//											dia.setCancelClickListener(new OnSweetClickListener() {
//
//												@Override
//												public void onClick(
//														SweetAlertDialog sweetAlertDialog) {
//													sweetAlertDialog
//															.dismissWithAnimation();
//
//													finish();
//
//												}
//											});
//											return;
//										}
//									}
//									order.setTrmmodno(arg0.deviceSN);
//									mHandler.sendEmptyMessage(WAIT_SWING);
								}

							});
						}

						@Override
						public void openFail() {
							// dismissLoadingDialog();
							System.out
									.println("------------打开设备失败-------------------");
							showMsg("打开刷卡器设备失败");
						}

					});
		}
	}

	private void waitingcard() {
		showText.setText("请用刷卡器刷卡/插卡...");
		reader.waitingCard(WaitCardType.MAGNETIC_IC_CARD_RFCARD,
				AmountUtils.change12(amount), mposHint, 60000,
				new WaitingCardListener() {
					@Override
					public void onError(int errCode, String errDesc) {

						contentText.setText("交易失败" + errDesc);
					}

					@Override
					public void onWaitingCardSucc(CardType cardType) {
						mCardType = cardType;
						if (mCardType.equals(CardType.IC_CARD)) {
							// contentText.setText("暂不支持IC卡");
							System.out.println("[IC_Card]");
							paytType = "01";
							mHandler.sendEmptyMessage(START_PBOC);
							// mHandler.sendEmptyMessage(GET_PAN_PLAIN);
						} else {
							System.out.println("[磁条卡]");
							paytType = "02";
							mHandler.sendEmptyMessage(GET_PAN_PLAIN);
							System.out.println("[GET_PAN_PLAIN]");
						}

					}
				});
	}

	private void showMsg(String mag) {
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
	}

	private void cashIn() {
		
		Intent intent = new Intent(this, null);
		intent.putExtra("PINBLK", pwdData);
		intent.putExtra("action", action);
		intent.putExtra("track2", cardTrack2);
		intent.putExtra("track3", cardTrack3);
		intent.putExtra("PAY_TYPE", paytType);// 刷卡类型 磁道：01 芯片：02
		intent.putExtra("TXAMT", amount);
		intent.putExtra("tratyp", tratyp);
		intent.putExtra("DCdata", icCardData);
		intent.putExtra("CardNo", acNo);
		intent.putExtra("rateType", rateType);
		intent.putExtra("posId", posId);
		intent.putExtra("PERIOD", expireData);
		intent.putExtra("ICnumber", panSerial);
		order.setPINBLK(pwdData);
		order.setAction(action);
		order.setTrack2(cardTrack2);
		order.setTrack3(cardTrack3);
		order.setPAY_TYPE(paytType);
		order.setCardNo(acNo);
		order.setDCdata(icCardData);
		order.setICnumber(panSerial);
		order.setPERIOD(expireData);
		order.setPosId(posId);
		order.setRateType(rateType);
		intent.putExtra("data", order);
		startActivity(intent);

		/****************************************************/
	}

	@Override
	public boolean dispatchKeyEvent(KeyEvent event) {
		if (event.getAction() == KeyEvent.ACTION_DOWN
				&& event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
			reader.cancleTrade();
		}
		return super.dispatchKeyEvent(event);
	}

	Handler mHandler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case WAIT_SWING:
				waitingcard();
				break;
			case GET_PAN_PLAIN:
				reader.getPANPlain(new GetPANListener() {

					@Override
					public void onError(int arg0, String arg1) {
						System.out.println("[获取卡号失败]" + arg1);
						contentText.setText("交易失败" + arg1);

					}

					@Override
					public void onGetPANSucc(String arg0) {
						acNo = arg0;
						System.out.println("???????????????????");
						System.out.println("获取卡号成功---" + acNo);
						order.setCardNo(arg0);
						mHandler.sendEmptyMessage(GET_TRACKDATA_PLAIN);
					}
				});
				break;
			case GET_TRACKDATA_PLAIN:
				reader.getTrackDataCipher(new GetTrackDataCipherListener() {

					@Override
					public void onError(int arg0, String arg1) {
						contentText.setText("获取磁道失败" + arg1);
					}

					@Override
					public void onGetTrackDataCipherSucc(String arg0,
							String arg1, String arg2, String arg3) {
						cardTrack2 = arg1;
						cardTrack3 = arg2;
						showText.setText("请输入密码，并确认...");
						contentImv.setImageResource(R.drawable.bg_input_pwd);
						mHandler.sendEmptyMessage(INPUT_PIN);
					}
				});
				/*
				 * reader.getTrackDataCipher(new GetTrackDataCipheListener() {
				 * 
				 * @Override public void onError(int errCode, String errDesc) {
				 * contentText.setText("获取磁道失败" + errDesc);
				 * 
				 * }
				 * 
				 * @Override public void onGetTrackDataPlainSucc(String track1,
				 * String track2, String track3) { cardTrack2 = track2;
				 * cardTrack3 = track3; showText.setText("请输入密码，并确认...");
				 * contentImv.setImageResource(R.drawable.bg_input_pwd);
				 * mHandler.sendEmptyMessage(INPUT_PIN); } });
				 */
				break;
			case INPUT_PIN:
				InputPinParameter inputPinParameter = new InputPinParameter();
				inputPinParameter.setCardNO(acNo);
				inputPinParameter.setTimeout((byte) 60000);
				inputPinParameter.setAmount(AmountUtils.change12(amount));
				reader.inputPin(inputPinParameter, new InputPinListener() {
					@Override
					public void onError(int errCode, String errDesc) {
						// showMsg("读取PIN密钥失败" + errDesc);
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
				// byte emvTradeType = 0x00;
				startPBOCParam.setTransactionType(emvTradeType);
				startPBOCParam
						.setAuthorizedAmount(AmountUtils.change12(amount));
				startPBOCParam.setOtherAmount("000000000000");
				startPBOCParam.setDate(Utils.getCurrentDate("yyMMdd"));
				startPBOCParam.setTime(Utils.getCurrentDate("HHmmss")); // "pos_time":
				startPBOCParam.setForbidContactCard(false);
				startPBOCParam.setForceOnline(true);
				startPBOCParam.setForbidMagicCard(false);
				startPBOCParam.setForbidContactlessCard(false);

				reader.startPBOC(startPBOCParam, new EMVProcessListener() {
					@Override
					public void onError(int errCode, String errDesc) {
						contentText.setText("读取二磁道失败" + errDesc);
					}

					@Override
					public void onEMVProcessSucc(MPosEMVProcessResult result) {
						expireData = result.getExpireData();
						panSerial = result.getPanSerial();
						cardTrack2 = result.getTrack2();
						System.out.println("?????????????????????????/=="+cardTrack2);
						acNo = result.getCredentialNo();
						System.out.println("[getCredentialNo==]"
								+ result.getCredentialNo());
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
						pwdData = ByteArrayUtils.byteArray2HexString(result
								.getPwdData());
						icCardData = ByteArrayUtils.byteArray2HexString(result
								.getICCardData());
						System.out.println("icCardData==="+icCardData);
						// System.out.println(icCardData.length() +
						// "icCardData-->" + icCardData);
						mHandler.sendEmptyMessage(PBOC_STOP);
						cashIn();

					}
				});
				break;

			case PBOC_STOP:
				reader.PBOCStop(new PBOCStopListener() {

					@Override
					public void onError(int errCode, String errDesc) {
						// showMsg("交易失败" + errDesc);
						// showSucc(result);
					}

					@Override
					public void onPBOCStopSuccess() {
						// contentText.setText("交易成功");
						// showSucc(result);
					}
				});
				break;

			default:
				break;
			}
		};
	};

	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

	};
}
