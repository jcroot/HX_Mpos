package com.lk.td.pay.activity.main.cashin;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.lk.td.pay.activity.MainTabActivity;
import com.lk.td.pay.activity.base.BaseActivity;
import com.lk.td.pay.activity.main.cashin.swing.xdl.bluetootch.BuletootchController;
import com.lk.td.pay.activity.main.cashin.swing.xdl.bluetootch.BuletootchControllerImpl;
import com.lk.td.pay.activity.main.cashin.swing.xdl.bluetootch.Const;
import com.lk.td.pay.activity.main.cashin.swing.xdl.bluetootch.pinInput.PinInputInterfaceImpl;
import com.lk.td.pay.beans.BasicResponse;
import com.lk.td.pay.beans.PosData;
import com.lk.td.pay.golbal.MApplication;
import com.lk.td.pay.golbal.Urls;
import com.lk.td.pay.tool.sharedpref.SharedPrefConstant;
import com.lk.td.pay.tool.Logger;
import com.lk.td.pay.tool.MyHttpClient;
import com.lk.td.pay.tool.T;
import com.lk.td.pay.utils.AmountUtils;
import com.lk.td.pay.utils.PinDes;
import com.lk.td.pay.utils.Utils;
import com.lk.td.pay.wedget.CustomDialog;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.newland.mtype.module.common.pin.AccountInputType;
import com.newland.mtype.module.common.pin.PinInputEvent;
import com.newland.mtype.module.common.pin.PinManageType;
import com.newland.mtype.module.common.pin.WorkingKey;
import com.newland.mtype.module.common.pin.WorkingKeyType;
import com.newland.mtype.module.common.swiper.SwipResult;
import com.newland.mtype.util.Dump;
import com.newland.mtype.util.ISOUtils;
import com.td.app.pay.hx.R;
import com.whty.tymposapi.DeviceApi;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;

import java.util.HashMap;
import java.util.concurrent.TimeUnit;

public class CashInConfirmActivity extends BaseActivity implements
        OnClickListener {

    /**
     * 设备购买
     */
    public static final String ACTION_BUY_DEVICE = "9";
    /**
     * 收款
     */
    public static final String ACTION_CASH_IN = "8";
    private TextView cardNoText, payAmtText, payRateText;
    private EditText cardPwdEdit;
    private String cardPwd;
    private Context mContext;
    private String[] rates;
    private LinearLayout ll_display;
    private Button btn_back;
    private String action;

    private String[] ratess = null;
    private String[] ratesDesc = null;
    private String rate = null;

    private String scancardnum;
    private String scanornot;
    private DeviceApi deviceApi;
    private PinInputInterfaceImpl pinInputInterfaceImpl;
    private SwipResult swipRslt;
    private BuletootchController buletootchController = BuletootchControllerImpl.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.cashin_confirm);
        mContext = this;
        action = getIntent().getAction() == null ? "" : getIntent().getAction();
        scancardnum = getIntent().getStringExtra("scancardnum");
        scanornot = getIntent().getStringExtra("scanornot");
        init();
        if (MApplication.getInstance().isKeymap()){
            try {
                buletootchController.isConnected();
                Logger.d("设备连接。。。。");

            }catch (Exception e){
                Logger.d("设备没有连接。。。");
                T.sl("设备连接断开，交易中断");
                finish();
            }
        }
        getBindDevice();
    }

    private void init() {
        deviceApi = new DeviceApi(getApplicationContext());
        swipRslt = PosData.getPosData().getSwipResult();

        pinInputInterfaceImpl = new PinInputInterfaceImpl();

        cardNoText = (TextView) findViewById(R.id.cashin_card_no_text);
        payAmtText = (TextView) findViewById(R.id.cashin_pay_amt_text);
        cardPwdEdit = (EditText) findViewById(R.id.cash_bank_pwd_edit);
        cardNoText.setText(Utils.hiddenCardNo(PosData.getPosData().getCardNo()));
        payAmtText.setText(AmountUtils.changeFen2Yuan(PosData.getPosData()
                .getPayAmt()) + "元");
        payRateText = (TextView) findViewById(R.id.cashin_pay_rate_text);
        payRateText.setOnClickListener(this);
        btn_back = (Button) findViewById(R.id.btn_back);
        btn_back.setText("收款");
        btn_back.setVisibility(View.VISIBLE);
        btn_back.setOnClickListener(this);
        findViewById(R.id.cashin_confirm_btn).setOnClickListener(this);
        ll_display = (LinearLayout) findViewById(R.id.ll_display);

        if (MApplication.getInstance().isKeymap()){  //表示键盘模式, 择需要获取键盘输入的加密的密码
            cardPwdEdit.setText("000000");
            cardPwdEdit.setEnabled(false);
            cardPwdEdit.setFocusable(false);
            cardPwdEdit.setFocusableInTouchMode(false);
            cardPwdEdit.setClickable(false);
            View view = getWindow().peekDecorView();
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.cashin_confirm_btn:

                if (MApplication.getInstance().isKeymap()){  //表示键盘模式, 择需要获取键盘输入的加密的密码
                    downloadPineky();
                }else {
                    submitPay();
                }
                break;
            case R.id.btn_back:
                goMainActivity();
                break;
            case R.id.cashin_pay_rate_text:
                queryRate();
                break;
            default:
                break;
        }
    }

    public void loadWkey(String pinKey, String zpincv){
        if (null != buletootchController){
            try {
                buletootchController.isConnected();
            }catch (Exception e){
                T.sl("设备连接断开，交易中断");
                finish();
            }
        }
        byte[] buff = pinInputInterfaceImpl.loadWorkingKey(WorkingKeyType.PININPUT,
                Const.MKIndexConst.DEFAULT_MK_INDEX, Const.PinWKIndexConst.DEFAULT_PIN_WK_INDEX,
                ISOUtils.hex2byte(pinKey));
        String checkValue = Dump.getHexDump(buff).replace(" ", "");
        Log.d("==checkValue =", "" + checkValue + "========" + zpincv.substring(0, 4));
        if (!Dump.getHexDump(buff).replace(" ", "").startsWith(zpincv.substring(0, 4))) {
            T.sl("PinKey 校验失败！");

            finish();
        } else {
            getInputPassword(pinKey);
        }

    }


    public void submitPay(){

        if (rate == null) {
            T.ss("请选择费率类型");
            return;
        }
        cardPwd = cardPwdEdit.getText().toString().trim();
        if (TextUtils.isEmpty(cardPwd)) {
            showDialog(getResources().getString(R.string.inputbankCardPwd));
        } else if (cardPwd.length() != 6) {
            showDialog("银行卡密码长度应为6位数");
        } else {
            if (null == MApplication.mSharedPref.getSharePrefString(
                    SharedPrefConstant.CITY_NAME) || "" == MApplication.mSharedPref.getSharePrefString(
                    SharedPrefConstant.CITY_NAME)) {
                showAlert();
            } else {
                downloadPineky();
            }
        }
    }

    /**
     * 获取键盘输入的密码
     */
    public void getInputPassword(String pinKey){

        new GetInputKeyPassword(pinKey).execute();
    }
    public class GetInputKeyPassword extends AsyncTask<Void, Void, PinInputEvent>{

        private String pinKey;
        public GetInputKeyPassword(String pinKey){
            this.pinKey = pinKey;
        }
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            showLoadingDialog("请在终端输入密码");
        }

        @Override
        protected PinInputEvent doInBackground(Void... voids) {
            try {
                PinInputEvent event = pinInputInterfaceImpl.startStandardPinInput(new WorkingKey(Const.PinWKIndexConst.DEFAULT_PIN_WK_INDEX),
                        PinManageType.MKSK, AccountInputType.USE_ACCT_HASH, swipRslt
                                .getAccount().getAcctHashId(), 6, new byte[]{'F', 'F', 'F',
                                'F', 'F', 'F', 'F', 'F'}, true, "请输入密码", 30,
                        TimeUnit.SECONDS);
                return event;
            }catch (Exception e){

                return null;
            }

        }

        @Override
        protected void onPostExecute(PinInputEvent event) {
            super.onPostExecute(event);
            dismissLoadingDialog();

            if (null == event) {
                Log.d("密码错误", "===========================");
            } else if (event.isSuccess()) {
                Log.d("KSN ", "" + Dump.getHexDump(event.getKsn()));
                Log.d("密文密码", "=====" + Dump.getHexDump(event.getEncrypPin()));
                cardPwd = Dump.getHexDump(event.getEncrypPin()).replaceAll(" ", "");
                goPay(pinKey);
            } else if (event.isUserCanceled()) {
                Log.d("密码异常", "====用户取消输入");
            }
        }
    }

    private void showAlert() {
        new CustomDialog.Builder(mContext)
                .setTitle("温馨提示")
                .setMessage("获取位置信息失败，请设置允许方位定位")
                .setOkBtn("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        finish();
                    }
                }).create().show();
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (event.getAction() == KeyEvent.ACTION_DOWN
                && event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
            goMainActivity();
            return false;
        } else {
            return super.dispatchKeyEvent(event);
        }
    }

    private void goMainActivity() {
        Intent it = new Intent(this, MainTabActivity.class);
        it.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        it.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(it);

    }

    private void goPay(String pinkey) {
        try {
            if (!MApplication.getInstance().isKeymap()) {
                cardPwd = PinDes.pinResultMak(PinDes.ZMK, pinkey, PosData.getPosData().getCardNo(), cardPwd);
            }
        } catch (Exception e) {
        }
        HashMap<String, String> params = new HashMap<String, String>();
        params.put("prdordNo", PosData.getPosData().getPrdordNo());
        params.put("payType", PosData.getPosData().getPayType());
        params.put("rateType", rate);
        params.put("termNo", PosData.getPosData().getBluetoothTermNo());
        params.put("termType", PosData.getPosData().getTermType());
        params.put("payAmt", PosData.getPosData().getPayAmt());
        params.put("track", PosData.getPosData().getTrack());
        params.put("pinblk", cardPwd);
        params.put("random", PosData.getPosData().getRandom());
        params.put("mediaType", PosData.getPosData().getMediaType());
        params.put("period", PosData.getPosData().getPeriod());
        params.put("icdata", PosData.getPosData().getIcdata());
        params.put("crdnum", PosData.getPosData().getCrdnum());
        params.put("mac", "");
        params.put("scancardnum", scancardnum);
        params.put("scanornot", scanornot);
        params.put("address", MApplication.mSharedPref.getSharePrefString(
                SharedPrefConstant.CITY_NAME));
        System.out.println("======================================>" + params.toString());

        MyHttpClient.post(mContext, Urls.TRADE_PAY,
                params, new AsyncHttpResponseHandler() {

                    @Override
                    public void onSuccess(int statusCode, Header[] headers,
                                          byte[] responseBody) {
                        Logger.json(responseBody);
                        try {
                            BasicResponse r = new BasicResponse(responseBody).getResult();
                            System.out.println("=====>" + new String(responseBody));
                            if (r.isSuccess()) {
                                PosData.getPosData().clearPosData();
                                Intent it = new Intent(mContext, ShowMsgActivity.class);
                                it.setAction("ACTION_CASH_IN");
                                it.putExtra("code", r.isSuccess());
                                it.putExtra("msg", r.getMsg());
                                startActivity(it);
                                finish();
                            } else {
                                PosData.getPosData().clearPosData();
                                Intent it = new Intent(mContext, ShowMsgActivity.class);
                                it.setAction("ACTION_CASH_IN");
                                it.putExtra("code", r.isSuccess());
                                it.putExtra("msg", r.getMsg());
                                if(null != buletootchController){
                                    buletootchController.destroy();
                                }

                                startActivity(it);
                                finish();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            showDialog("数据解析失败");
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
                        showLoadingDialog("正在支付，请稍后");
                    }

                    @Override
                    public void onFinish() {
                        super.onFinish();
                        dismissLoadingDialog();

                        if (MApplication.getInstance().isKeymap()){
                            if (null != buletootchController){
                                buletootchController.destroy();
                            }
                        }
                    }
                });
    }

    private void queryRate() {
        Dialog dialog = new AlertDialog.Builder(this)
                .setTitle("选择费率类型")
                .setSingleChoiceItems(ratesDesc, 0,
                        new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog,
                                                int which) {
                                try {
                                    rate = ratess[which];
                                    System.err.println("---------------------->>>" + rate);
                                    payRateText.setText(ratesDesc[which]);
                                    dialog.dismiss();
                                } catch (Exception e) {
                                    // TODO 自动生成的 catch 块
                                    e.printStackTrace();
                                }

                            }
                        }).create();
        dialog.show();
    }

    /**
     * 获取绑定设备的  费率
     */
    private void getBindDevice() {
        HashMap<String, String> params = new HashMap<String, String>();
        params.put("termNo", PosData.getPosData().getTermNo());
        MyHttpClient.post(this, Urls.TERM_DEVICE_LIST, params,
                new AsyncHttpResponseHandler() {

                    @Override
                    public void onSuccess(int statusCode, Header[] headers,
                                          byte[] responseBody) {
                        Logger.json(responseBody);
                        try {
                            BasicResponse res = new BasicResponse(responseBody)
                                    .getResult();
                            if (res.isSuccess()) {
                                JSONArray array = res.getJsonBody()
                                        .getJSONArray("rateList");
                                ratess = new String[array.length()];
                                ratesDesc = new String[array.length()];
                                for (int i = 0; i < array.length(); i++) {
                                    ratess[i] = array.getJSONObject(i)
                                            .optString("rateNo");
                                    ratesDesc[i] = array.getJSONObject(i)
                                            .optString("rateDesc");
                                }
                                if (ratesDesc.length != 0) {
                                    payRateText.setText(ratesDesc[0]);
                                    rate = ratess[0];
                                } else {
                                    payRateText.setText("该终端没有设置费率");
                                    payRateText.setEnabled(false);
                                }


                            } else {
                                T.ss(res.getMsg());
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
                    public void onFinish() {
                        super.onFinish();
                        dismissLoadingDialog();
                    }

                    @Override
                    public void onStart() {
                        super.onStart();
                        showLoadingDialog();
                    }
                });
    }

    private void downloadPineky() {
        System.out.println("======================================>" + rate);
        // 终端签到
        HashMap<String, String> params = new HashMap<String, String>();
        params.put("termNo", PosData.getPosData().getTermNo());
        params.put("termType", PosData.getPosData().getTermType());

        MyHttpClient.post(this, Urls.BLUETOOTH_SIGN, params,
                new AsyncHttpResponseHandler() {

                    @Override
                    public void onSuccess(int statusCode, Header[] headers,
                                          byte[] responseBody) {
                        try {
                            BasicResponse re = new BasicResponse(responseBody)
                                    .getResult();
                           
                            if (re.isSuccess()) {
                                Logger.json(re.getJsonBody().toString());
                                String pinkey = re.getJsonBody().optString("zpinkey");
                                String zpincv = re.getJsonBody().optString("zpincv");
                                Log.d("返回的pinKey   ", "======="+pinkey);
                                if (MApplication.getInstance().isKeymap()){
                                    loadWkey(pinkey, zpincv);
                                }else{
                                    goPay(pinkey);
                                }
                                

                            } else {
                                showDialog(re.getMsg());
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
                    }

                    @Override
                    public void onFinish() {
                        super.onFinish();
                    }
                });
    }


}