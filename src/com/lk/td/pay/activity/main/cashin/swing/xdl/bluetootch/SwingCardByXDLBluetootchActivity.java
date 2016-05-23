package com.lk.td.pay.activity.main.cashin.swing.xdl.bluetootch;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.text.Html;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.lk.td.pay.activity.base.BaseActivity;
import com.lk.td.pay.activity.main.cashin.CheckActivity;
import com.lk.td.pay.activity.main.cashin.swing.CardBalanceConfirmActivity;
import com.lk.td.pay.activity.main.cashin.swing.SignaturePadActivity;
import com.lk.td.pay.activity.main.cashin.swing.xdl.bluetootch.Const.MessageTag;
import com.lk.td.pay.activity.main.cashin.swing.xdl.bluetootch.device.DeviceControllerInterface;
import com.lk.td.pay.activity.main.cashin.swing.xdl.bluetootch.device.DeviceControllerInterfaceImpl;
import com.lk.td.pay.activity.main.cashin.swing.xdl.bluetootch.pinInput.ME3xDeviceDriver;
import com.lk.td.pay.beans.BasicResponse;
import com.lk.td.pay.beans.PosData;
import com.lk.td.pay.golbal.MApplication;
import com.lk.td.pay.golbal.Urls;
import com.lk.td.pay.golbal.User;
import com.lk.td.pay.tool.Logger;
import com.lk.td.pay.tool.MyHttpClient;
import com.lk.td.pay.tool.T;
import com.lk.td.pay.utils.AmountUtils;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.newland.me.ConnUtils;
import com.newland.me.DeviceManager;
import com.newland.me.DeviceManager.DeviceConnState;
import com.newland.mtype.ConnectionCloseEvent;
import com.newland.mtype.DeviceInfo;
import com.newland.mtype.conn.DeviceConnParams;
import com.newland.mtype.event.DeviceEventListener;
import com.newland.mtype.module.common.cardreader.CardRule;
import com.newland.mtype.module.common.cardreader.OpenCardType;
import com.newland.mtypex.bluetooth.BlueToothV100ConnParams;
import com.td.app.pay.hx.R;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

public class SwingCardByXDLBluetootchActivity extends BaseActivity {
    private static final String ME3X_DRIVER_NAME = "com.newland.me.ME3xDriver";
    private SwingCardByXDLBluetootchActivity swingcardbyxdlbluetootchactivity;
    private String deviceInteraction = "", newstring;
    private AbstractDevice connected_device;

    private BuletootchController controller = BuletootchControllerImpl.getInstance();
    private TextView cashin_account_text;// 充值钱数
    private TextView cashin_show_msg_text;// 刷卡状态
    private String amount = "";// 充值数量
    private TextView  titl;// 重置
    private LinearLayout backBtn, cashin_step_two_layout;// 返回
    private Boolean processing = false;
    //private String ksn;
    private String csn;
    private SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
    private long mTime = 0;
    // private boolean service_flag=false;//服务请求标识
    // private boolean termList_flag=false;//终端列表标识
    private JSONArray jsonTermListArray = null;
    private boolean sign_flag = false;// 签到标识
    private String termTdk;//密文密钥
    private String termTdkCv;
    private static DeviceManager deviceManager = ConnUtils.getDeviceManager();
    private Boolean isFrist = false;
    private String SDCardPath;
    private Button btn_back, btn_reset;
    private TextView tv_title;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        setContentView(R.layout.swing_card_audio);
        swingcardbyxdlbluetootchactivity = SwingCardByXDLBluetootchActivity.this;

        PosData.getPosData().getActtext();
        initUI();
        String blueTootchAddress = User.macAddress;
        btnStateToWaitingInitFinished();
        initMe3xDeviceController(ME3X_DRIVER_NAME, new BlueToothV100ConnParams(blueTootchAddress));
        getDeviceInfo();

    }


    private void initUI() {
        cashin_account_text = (TextView) findViewById(R.id.cashin_account_text);
        cashin_step_two_layout = (LinearLayout) findViewById(R.id.cashin_step_two_layout);
        btn_back = (Button) findViewById(R.id.btn_back);
        btn_reset = (Button) findViewById(R.id.btn_reset);
        btn_back.setVisibility(View.VISIBLE);
        tv_title = (TextView) findViewById(R.id.tv_title);
        tv_title.setText("刷卡支付");
        if (PosData.getPosData().getActtext().equals("ACTION_QUERY_BALANCE")) {
            amount = "1";
            cashin_step_two_layout.setVisibility(View.GONE);
            tv_title.setText("余额查询");
        } else {
            amount = PosData.getPosData().getPayAmt();

        }

        cashin_account_text.setText(AmountUtils.changeFen2Yuan(amount) + "元");
        cashin_show_msg_text = (TextView) findViewById(R.id.cashin_show_msg_text);
        btn_reset.setVisibility(View.VISIBLE);
        btn_reset.setText("重置");
        btn_reset.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if(!isFrist){
					String blueTootchAddress = User.macAddress;
					btnStateToWaitingInitFinished();
					initMe3xDeviceController(ME3X_DRIVER_NAME,new BlueToothV100ConnParams(blueTootchAddress));
					System.out.println("控制器已初始化!");
					getDeviceInfo();
				}else{
					onSwipCard();
				}
			}
		});
        btn_back.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                if (null!=controller){
                    controller.destroy();
                }
                finish();
            }
        });

    }

    @Override
    public void onBackPressed() {
        finish();
    }

    public BuletootchController initMe3xDeviceController(String driverPath, DeviceConnParams params) {

        ME3xDeviceDriver me3xDeviceController = new ME3xDeviceDriver(SwingCardByXDLBluetootchActivity.this);
        me3xDeviceController.initMe3xDeviceController(driverPath, params);

        controller.init(SwingCardByXDLBluetootchActivity.this, driverPath, params, new DeviceEventListener<ConnectionCloseEvent>() {
            @Override
            public void onEvent(ConnectionCloseEvent event, Handler handler) {
                if (event.isSuccess()) {
                    appendInteractiveInfoAndShow("设备被客户主动断开！");
                }
                if (event.isFailed()) {
                    appendInteractiveInfoAndShow("设备链接异常断开！" + event.getException().getMessage());
                }

            }

            @Override
            public Handler getUIHandler() {
                return null;
            }
        });

        System.out.println("驱动版本号：" + controller.getCurrentDriverVersion());
        return controller;
    }

    private void appendInteractiveInfoAndShow(final String string) {

        runOnUiThread(new Runnable() {
            @Override
            public void run() {

                cashin_show_msg_text.setText(string);
            }
        });
    }

    /**
     * 设置成处理中状态
     *
     * @since ver1.0
     */
    private void btnStateToProcessing() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                // btnConnect.setEnabled(false);
                // btnDisconnect.setEnabled(false);
                // listView.setOnTouchListener(new OnTouchListener() {
                // @Override
                // public boolean onTouch(View v, MotionEvent event) {
                // return false;
                // }
                // });
                processing = true;
            }
        });
    }

    /**
     * 设置成设备销毁状态
     *
     * @since ver1.0
     */
    private void btnStateDestroyed() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                // btnConnect.setEnabled(true);
                // btnDisconnect.setEnabled(false);
                // listView.setOnTouchListener(new OnTouchListener() {
                // @Override
                // public boolean onTouch(View v, MotionEvent event) {
                // return true;
                // }
                // });
                processing = true;
            }
        });
    }

    public void connectDevice() {
        appendInteractiveInfoAndShow("设备连接中...");
        try {
            controller.connect();
            isFrist = true;
            appendInteractiveInfoAndShow("设备连接成功...");
        } catch (Exception e1) {
            e1.printStackTrace();
            appendInteractiveInfoAndShow("设备链接异常断开...");
            //throw new RuntimeException(e1.getMessage(), e1);
        }
    }

    /**
     * 获取设备信息
     */
    private void getDeviceInfo() {

        new Thread(new Runnable() {
            @Override
            public void run() {
                connectDevice();
                try {
                    processingLock();
                    DeviceInfo deviceInfo = controller.getDeviceInfo();
                    csn = deviceInfo.getCSN();
                } catch (Exception e) {
                    appendInteractiveInfoAndShow("获取设备信息失败，确保刷卡头已连接！");
                    return;
                } finally {
                    processingUnLock();

                }
                onSwipCard();// 插卡、刷卡（密文）
            }
        }).start();
    }

    public void processingUnLock() {
        SharedPreferences setting = getSharedPreferences("setting", 0);
        SharedPreferences.Editor editor = setting.edit();
        editor.putBoolean("PBOC_LOCK", false);
        editor.commit();
        return;
    }

    public void processingLock() {
        SharedPreferences setting = getSharedPreferences("setting", 0);
        SharedPreferences.Editor editor = setting.edit();
        editor.putBoolean("PBOC_LOCK", true);
        editor.commit();
    }

    /**
     *
     */
    public void DisplayDialog() {

        runOnUiThread(new Runnable() {

            @Override
            public void run() {
                // TODO Auto-generated method stub

                Toast.makeText(SwingCardByXDLBluetootchActivity.this, "此设备没有进行绑定，请先到绑定界面进行绑定！", Toast.LENGTH_SHORT).show();
            }
        });

    }

    /**
     * 刷卡
     *
     * @param whereType
     */
    public void onSwipCard() {

        processingisLocked();
        appendInteractiveInfoAndShow("请刷卡或插入IC卡...");
        System.err.println("========s=================================================");
        try {
            DecimalFormat df = new DecimalFormat("#.00");
            BigDecimal amt = new BigDecimal(amount);
            Double strAmt = Double.parseDouble(df.format(amt));
            controller.startTransfer(swingcardbyxdlbluetootchactivity,
                    new OpenCardType[]{OpenCardType.SWIPER, OpenCardType.ICCARD, OpenCardType.NCCARD},
                    "交易金额为:" + (strAmt / 100) + " 元，\n请刷卡或者插入IC卡", amt, 60,
                    TimeUnit.SECONDS, CardRule.ALLOW_LOWER,
                    new SimpleTransferListener(connected_device, swingcardbyxdlbluetootchactivity));
            Log.d("=========","csn : " + csn);
            PosData.getPosData().setPayAmt(amount);   //交易金额
            PosData.getPosData().setTermNo(csn);  //设备号
            entryOtherActivity();
        } catch (Exception e1) {
            appendInteractiveInfoAndShow(PosData.getPosData().getErr());
            btnStateInitFinished();
            return;
        }
    }


    /**
     * 进入签名的Activity
     */
    private void entryOtherActivity() {

        runOnUiThread(new Runnable() {

            @Override
            public void run() {
                // TODO Auto-generated method stub
                if (PosData.getPosData().getCardNo().equals("")) {
                    appendInteractiveInfoAndShow("获取磁道信息失败！");
                    return;
                } else {
                    if (PosData.getPosData().getActtext().equals("ACTION_QUERY_BALANCE")) {//余额查询
                        startActivity(new Intent(SwingCardByXDLBluetootchActivity.this, CardBalanceConfirmActivity.class));
                        finish();
                    } else if (PosData.getPosData().getActtext().equals("ACTION_QUERY")) {  //交易签名
                        startActivity(new Intent(SwingCardByXDLBluetootchActivity.this, SignaturePadActivity.class));
                        finish();
                    }
                }

            }
        });

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        destroyDeviceController();
    }

    /**
     * 销毁设备控制器
     *
     * @since ver1.0
     */
    private void destroyDeviceController() {
        if (!MApplication.getInstance().isKeymap()){
            controller.destroy(); // 终端后的相关状态处理会通过事件完成,此处不需要处理
        }
        btnStateDestroyed();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        if (event.getAction()== KeyEvent.KEYCODE_BACK){

            if (null!=controller){
                controller.destroy();
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    /**
     * 取消当前的操作
     */
    private boolean onCancel() {

        boolean cancel_flag = false;// 取消指令
        if (DeviceConnState.CONNECTED == controller.getDeviceConnState()) {
            try {
                controller.reset();
                cancel_flag = true;
            } catch (Exception e) {
                appendInteractiveInfoAndShow("撤消指令执行失败!请点击重置按钮.");
            }
        } else {
            appendInteractiveInfoAndShow("设备未连接!");
        }

        return cancel_flag;
    }

    /**
     * 获取终端列表
     */
    public void onGetTermList() {

        jsonTermListArray = null;
        HashMap<String, String> params = new HashMap<String, String>();
        MyHttpClient.post(SwingCardByXDLBluetootchActivity.this,
                Urls.BIND_DEVICE_LIST, params, new AsyncHttpResponseHandler() {

                    @Override
                    public void onSuccess(int statusCode, Header[] headers,
                                          byte[] responseBody) {
                        Logger.json(responseBody);
                        try {

                            BasicResponse result = new BasicResponse(
                                    responseBody).getResult();
                            if (result.isSuccess()) {

                                JSONObject jsonOneBody = result.getJsonBody();
                                jsonTermListArray = jsonOneBody.getJSONArray("termList");
                                if (jsonTermListArray.length() > 0) {

                                    getDeviceInfo();// 初始化设备

                                } else {


                                    cashin_show_msg_text.setText("请先去设备列表界面进行设备绑定！");

                                }
                            } else {

                                T.ss(result.getRSPMSG());

                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers,
                                          byte[] responseBody, Throwable error) {

                        Logger.d(error.toString());
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


    /**
     * @param cardNo         银行卡号
     * @param secondTrackStr 二磁
     * @param thirdTrackStr  三磁
     * @param period         有效期
     * @param icNumber       卡序列号
     * @param icdata         第五域
     * @param mediaType      刷卡方式
     */
    private void nextStep(String cardNo, String secondTrackStr, String thirdTrackStr, String period, String icNumber, String icdata, String mediaType) {

        PosData.getPosData().setPayAmt(amount);
        PosData.getPosData().setTermNo(csn);
        entryOtherActivity();
        PosData.getPosData().setCardNo(cardNo);
        PosData.getPosData().setPayType("02");
        PosData.getPosData().setRate("1");
        PosData.getPosData().setTermType("02");
        PosData.getPosData().setTrack(secondTrackStr + "|" + thirdTrackStr);
        PosData.getPosData().setRandom("000000");
        PosData.getPosData().setMediaType(mediaType);//磁卡类型01 磁条卡 02 IC卡
        PosData.getPosData().setPeriod(period);
        PosData.getPosData().setCrdnum(icNumber);
        PosData.getPosData().setIcdata(icdata);


    }

    public void appendInteractiveInfoAndShow(final String string, final int messageTag) {

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                switch (messageTag) {
                    case MessageTag.NORMAL:
                        newstring = "<font color='black'>" + string + "</font>";
                        break;
                    case MessageTag.ERROR:
                        newstring = "<font color='red'>" + string + "</font>";
                        break;
                    case MessageTag.TIP:
                        newstring = "<font color='blue'>" + string + "</font>";
                        break;
                    case MessageTag.DATA:
                        String arr[] = string.split(":");
                        newstring = "<font color='green'>" + arr[0] + "</font>" + ":" + arr[1];
                        break;
                    default:
                        break;
                }
                deviceInteraction = newstring + "<br>" + deviceInteraction;
                System.err.println(Html.fromHtml(deviceInteraction));
            }
        });
    }

    /**
     * 设置成初始化结束状态
     *
     * @since ver1.0
     */
    public void btnStateInitFinished() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                processing = false;
            }
        });

    }

    public void doPinInputShower(final boolean isNext) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (isNext) {
                    deviceInteraction = "*" + deviceInteraction;
                    System.err.println(Html.fromHtml(deviceInteraction));
                } else {
                    deviceInteraction = deviceInteraction.substring(1, deviceInteraction.length());
                    System.err.println(Html.fromHtml(deviceInteraction));
                }

            }
        });
    }

    public boolean processingisLocked() {
        SharedPreferences setting = getSharedPreferences("setting", 0);
        if (setting.getBoolean("PBOC_LOCK", true)) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * 设置成等待初始化结束状态
     *
     * @since ver1.0
     */
    private void btnStateToWaitingInitFinished() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                // btnConnect.setEnabled(false);
                // btnDisconnect.setEnabled(false);
                // listView.setOnTouchListener(new OnTouchListener() {
                // @Override
                // public boolean onTouch(View v, MotionEvent event) {
                // return true;
                // }
                // });
                processing = true;
            }
        });
    }


}
