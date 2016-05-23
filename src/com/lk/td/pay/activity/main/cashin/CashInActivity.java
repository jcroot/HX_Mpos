package com.lk.td.pay.activity.main.cashin;

import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Vibrator;
import android.text.InputType;
import android.text.TextUtils;
import android.text.method.NumberKeyListener;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.landicorp.android.mpos.reader.LandiMPos;
import com.landicorp.mpos.reader.BasicReaderListeners.GetDeviceInfoListener;
import com.landicorp.mpos.reader.BasicReaderListeners.OpenDeviceListener;
import com.landicorp.mpos.reader.model.MPosDeviceInfo;
import com.landicorp.robert.comm.api.DeviceInfo;
import com.lk.td.pay.activity.account.bind.equip.EquipmentManagementActivity;
import com.lk.td.pay.activity.base.BaseActivity;
import com.lk.td.pay.activity.main.cashin.swing.ty.bluetooth.CheckTYActivity;
import com.lk.td.pay.activity.main.cashin.swing.xdl.SwingCardByXDLAudioActivity;
import com.lk.td.pay.beans.BasicResponse;
import com.lk.td.pay.beans.CardBean;
import com.lk.td.pay.beans.PosData;
import com.lk.td.pay.golbal.Actions;
import com.lk.td.pay.golbal.MApplication;
import com.lk.td.pay.golbal.Urls;
import com.lk.td.pay.tool.Logger;
import com.lk.td.pay.tool.MyHttpClient;
import com.lk.td.pay.tool.T;
import com.lk.td.pay.utils.AmountUtils;
import com.lk.td.pay.wedget.CustomDialog;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.td.app.pay.hx.R;

import org.apache.http.Header;
import org.json.JSONException;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.List;

public class CashInActivity extends BaseActivity implements OnClickListener {

    private ImageButton btnDel;
    private Button btn00, btn0, btn1, btn2, btn3, btn4, btn5, btn6, btn7, btn8,
            btn9, btnPoint, btnPay;
    private String str00, str0, str1, str2, str3, str4, str5, str6, str7, str8,
            str9, strPoint;
    private EditText amountEdit;
    private StringBuilder sb;
    private boolean isDian = false;
    private Vibrator vibrator;
    private DecimalFormat nf;
    private LandiMPos reader;
    public static final String ACTION_T0 = "0";
    public static final String ACTION_T1 = "1";

    private Button btn_back;
    private TextView tv_title;
    private Context ctx;

    private CustomDialog dialog = null;
    private List<CardBean> listType = null;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_cashin);

        ctx = CashInActivity.this;
        initView();
        reader = LandiMPos.getInstance(this);
        vibrator = (Vibrator) getSystemService(Service.VIBRATOR_SERVICE);
        nf = new DecimalFormat("0.00"); // 保留几位小数
        listType = MApplication.getInstance().getCardType();
    }

    private void initView() {
        tv_title = (TextView) findViewById(R.id.tv_title);
        tv_title.setVisibility(View.GONE);
        btn_back = (Button) findViewById(R.id.btn_back);
        btn_back.setVisibility(View.VISIBLE);
        btn_back.setText("收款");
        if (getIntent().getAction().equals(ACTION_T0)) {
            btn_back.setText("即时到账");
            PosData.getPosData().setCtype("00");
        } else {
            PosData.getPosData().setCtype("01");
        }
        amountEdit = (EditText) this.findViewById(R.id.cashin_amount_edit);
        btn00 = (Button) this.findViewById(R.id.btn00);
        btn0 = (Button) this.findViewById(R.id.btn0);
        btn1 = (Button) this.findViewById(R.id.btn1);
        btn2 = (Button) this.findViewById(R.id.btn2);
        btn3 = (Button) this.findViewById(R.id.btn3);
        btn4 = (Button) this.findViewById(R.id.btn4);
        btn5 = (Button) this.findViewById(R.id.btn5);
        btn6 = (Button) this.findViewById(R.id.btn6);
        btn7 = (Button) this.findViewById(R.id.btn7);
        btn8 = (Button) this.findViewById(R.id.btn8);
        btn9 = (Button) this.findViewById(R.id.btn9);
        btnPoint = (Button) this.findViewById(R.id.btn_point);
        btnDel = (ImageButton) this.findViewById(R.id.btn_del);
        btnPay = (Button) this.findViewById(R.id.btn_pay);

        btn00.setOnClickListener(this);
        btn0.setOnClickListener(this);
        btn1.setOnClickListener(this);
        btn2.setOnClickListener(this);
        btn3.setOnClickListener(this);
        btn4.setOnClickListener(this);
        btn5.setOnClickListener(this);
        btn6.setOnClickListener(this);
        btn7.setOnClickListener(this);
        btn8.setOnClickListener(this);
        btn9.setOnClickListener(this);
        btnPoint.setOnClickListener(this);
        btnDel.setOnClickListener(this);
        btnPay.setOnClickListener(this);
        btn_back.setOnClickListener(
                new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        finish();
                    }
                });
    }

    @Override
    public void onResume() {
        super.onResume();
        sb = new StringBuilder();
        amountEdit.setKeyListener(new MNumberKeyListener());
        amountEdit.setFocusable(true);
    }

    private void insert(Button btn) {
        String str = btn.getText().toString();
        if (sb.length() == 0 && str.equals("00")) {
            return;
        }
        if (sb.length() == 6 && str.equals("00")) {
            return;
        }
        if (isDian) {
            if (sb.toString().contains(".")) {
                String sbStr = new StringBuilder(sb.toString()).reverse()
                        .toString();

                if (sbStr.indexOf(".") == 1) {
                    sb.append(str);
                }
            } else {
                if (sb.length() == 0) {
                    sb.append("0." + str);
                } else {
                    sb.append("." + str);
                }
            }
        } else {
            sb.append(str);
        }
        String amount = nf.format(Double.parseDouble(sb.toString()));
        if (amount.length() < 11) {
            amountEdit.setText(amount);
        }

    }

    @Override
    public void onClick(View v) {
        vibrator.vibrate(new long[]{0, 15}, -1);
        switch (v.getId()) {
            case R.id.btn00:
                insert(btn00);
                break;
            case R.id.btn0:
                insert(btn0);
                break;
            case R.id.btn1:
                insert(btn1);
                break;
            case R.id.btn2:
                insert(btn2);
                break;
            case R.id.btn3:
                insert(btn3);
                break;
            case R.id.btn4:
                insert(btn4);
                break;
            case R.id.btn5:
                insert(btn5);
                break;
            case R.id.btn6:
                insert(btn6);
                break;
            case R.id.btn7:
                insert(btn7);
                break;
            case R.id.btn8:
                insert(btn8);
                break;
            case R.id.btn9:
                insert(btn9);
                break;
            case R.id.btn_point:
                isDian = true;

                break;
            case R.id.btn_del:
                isDian = false;
                sb.delete(0, sb.length());
                amountEdit.setText("");
                break;
            case R.id.btn_pay:
                goBrush();
                break;
            default:
                break;
        }

    }

    public class MNumberKeyListener extends NumberKeyListener {
        @Override
        protected char[] getAcceptedChars() {
            char[] numberChars = {'.', '0', '1', '2', '3', '4', '5', '6', '7',
                    '8', '9'};
            return numberChars;
        }

        @Override
        public int getInputType() {
            // return InputType.TYPE_NUMBER_FLAG_DECIMAL;
            return InputType.TYPE_DATETIME_VARIATION_NORMAL;
        }

    }

    private void goBrush() {

        String amount = amountEdit.getText().toString().trim();
        amount = AmountUtils.changeY2F(amount);
        if (TextUtils.isEmpty(amount)) {
            Toast.makeText(ctx, "金额格式不正确", Toast.LENGTH_SHORT).show();
        } else {
            createOrder(amount);
        }
    }

    private void openDevice(final String account) {
        showLoadingDialog();
        DeviceInfo deviceInfo = MApplication.mSharedPref.getDeviceInfo();
        if (null == deviceInfo) {
            showDialog();

        } else {
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
                            dismissLoadingDialog();
                            // goStepTwo(account, arg0.deviceSN);

                        }
                    });
                }

                @Override
                public void openFail() {
                    dismissLoadingDialog();
                    showDialog();

                }

            });
        }

    }

    private void showDialog() {
        new CustomDialog.Builder(ctx)
                .setTitle(getString(R.string.app_name))
                .setMessage("当前未绑定设备，前往设备连接管理？")
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

    private void showMsg(String mag) {
        new CustomDialog.Builder(ctx)
                .setTitle(getString(R.string.app_name))
                .setMessage(mag)
                .setOkBtn(getString(R.string.ok),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog,
                                                int which) {
                                dialog.cancel();
                            }
                        }).create().show();
    }

    private void goEquMan() {
        Intent it = new Intent(ctx, EquipmentManagementActivity.class);
        startActivity(it);
    }

    private void goStepTwo() {
        CustomDialog.Builder alert = new CustomDialog.Builder(ctx);
        alert.setListView(listType, new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                CardBean card = listType.get(i);
                Intent intent = new Intent();
                switch (card.getId()){
                    case 1:  //新大陆音频
                        intent.setClass(CashInActivity.this, SwingCardByXDLAudioActivity.class);
                        intent.setAction(Actions.ACTION_CASHIN);
                        break;
                    case 2:  //新大陆蓝牙

                    case 4:  //新大陆蓝牙（键盘）
                        intent.setClass(CashInActivity.this, CheckActivity.class);
                        intent.putExtra(CheckActivity.TYPE,CheckActivity.TYPE_CONN);
                        MApplication.getInstance().setKeymap(card.getId()==2 ? false :true);
                        PosData.getPosData().setActtext("ACTION_QUERY");
                        break;
                    case 3: //天谕蓝牙
                        intent.setClass(CashInActivity.this, CheckTYActivity.class);
                        intent.putExtra(CheckTYActivity.TYPE, CheckTYActivity.TYPE_CONN);
                        PosData.getPosData().setActtext("ACTION_QUERY");
                        break;
                    default:
                        break;
                }
                startActivity(intent);
                dialog.dismiss();
                finish();
            }
        });

        alert.setTitle("请选择刷卡器类型");
        dialog = alert.create();
        dialog.show();


    }

    /**
     * 创建商品订单
     * @param amount
     */
    private void createOrder(final String amount) {
        HashMap<String, String> params = new HashMap<String, String>();
        params.put("prdordType", "01");
        params.put("prdordAmt", amount);
        MyHttpClient.post(this, Urls.CREATE_ORDER, params,
                new AsyncHttpResponseHandler() {

                    @Override
                    public void onSuccess(int statusCode, Header[] headers,
                                          byte[] responseBody) {
                        Logger.json("[CreateOrder]", responseBody);
                        try {
                            BasicResponse r = new BasicResponse(responseBody).getResult();
                            if (r.isSuccess()) {
                                PosData.getPosData().setPrdordNo(
                                        r.getJsonBody().getString("prdordNo"));
                                PosData.getPosData().setPayAmt(amount);

                                goStepTwo();
                            } else {
                                T.ss(r.getMsg());
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
                        showLoadingDialog();
                    }

                    @Override
                    public void onFinish() {
                        super.onFinish();
                        dismissLoadingDialog();
                    }
                });

    }

}
