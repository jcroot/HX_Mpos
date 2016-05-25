package com.lk.td.pay.activity.account.info;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.lk.td.pay.activity.main.cashin.ShowMsgActivity;
import com.lk.td.pay.activity.base.BaseActivity;
import com.lk.td.pay.beans.BankCardItem;
import com.lk.td.pay.beans.BasicResponse;
import com.lk.td.pay.golbal.Urls;
import com.lk.td.pay.golbal.User;
import com.lk.td.pay.tool.Logger;
import com.lk.td.pay.tool.MyHttpClient;
import com.lk.td.pay.tool.T;
import com.lk.td.pay.utils.AmountUtils;
import com.lk.td.pay.utils.MD5Util;
import com.lk.td.pay.utils.Utils;
import com.lk.td.pay.wedget.password.CustomPasswordDialog;
import com.lk.td.pay.wedget.password.GridPasswordView;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.td.app.pay.hx.R;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class AccountWithdrawActivity extends BaseActivity implements
        OnClickListener {

    EditText etWithdrawMoney, etPaypwd, etAccountName, etExplain;
    private TextView tvBankCardNo, tv_balance, tv_cash, tv_t0, tv_t1, tv_fee,
            tv_detail;
    private String userName, bankCardNo, bankcode, cname;
    private boolean done = false;
    private String clear, noclear;
    private String poundage, tfee;
    private Button btn_back;
    private TextView tv_title;

    private CustomPasswordDialog passwordDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.account_withdraw);
        initView();
        queryBalance();
        if (TextUtils.isEmpty(User.cache_card)) {
            getBankCardStatus();
        } else {
            etAccountName.setText(User.cache_card + "");
        }
    }

    private String cardNo;
    private List<BankCardItem> bankList = new ArrayList<BankCardItem>();

    private void getBankCardStatus() {
        HashMap<String, String> requestMap = new HashMap<String, String>();
        MyHttpClient.post(this, Urls.GET_BANKCARD_LIST, requestMap,
                new AsyncHttpResponseHandler() {

                    @Override
                    public void onSuccess(int statusCode, Header[] headers,
                                          byte[] responseBody) {
                        Logger.json("[BankCardList]", responseBody);
                        try {
                            BasicResponse r = new BasicResponse(responseBody)
                                    .getResult();
                            if (r.isSuccess()) {
                                JSONArray array = r.getJsonBody().optJSONArray(
                                        "bankCardList");

                                for (int i = 0; i < array.length(); i++) {
                                    JSONObject temp = array.getJSONObject(i);
                                    BankCardItem item = new BankCardItem();
                                    item.setCardName(temp.optString("issnam"));
                                    item.setCardNo(temp.optString("cardNo"));
                                    bankList.add(item);
                                }
                                if (bankList.size() > 0) {
                                    String temp = Utils.hiddenCardNo(bankList.get(0).getCardNo());
                                    etAccountName.setText(temp);
                                    User.cache_card = temp;
                                } else {
                                    etAccountName.setText("--");
                                }

                            } else {
                                T.ss(r.getMsg());
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
//							dismissLoadingDialog();
                    }
                });

    }

    private String toS(Object str) {
        if (null == str)
            return "";
        else
            return str.toString();
    }

    private void initView() {
        tv_title = (TextView) findViewById(R.id.tv_title);
        tv_title.setText("账户提现");
        btn_back = (Button) findViewById(R.id.btn_back);
        btn_back.setVisibility(View.VISIBLE);
        btn_back.setText("返回");
        btn_back.setOnClickListener(this);
        tv_balance = (TextView) findViewById(R.id.tv_balance_total);
        tv_detail = (TextView) findViewById(R.id.tv_balance_detail);
        // tvBankCardNo = (TextView)
        // findViewById(R.id.tv_account_withdraw_bank_card_no);
        // tvBankCardNo.setText(CardUtil.getCardNo(bankCardNo));
        etAccountName = (EditText)
                findViewById(R.id.et_account_withdraw_name);
        etWithdrawMoney = (EditText) findViewById(R.id.et_account_withdraw_money);
        etPaypwd = (EditText) findViewById(R.id.et_account_withdraw_pay_pwd);
        // etExplain = (EditText)
        // findViewById(R.id.et_account_withdraw_explain);

        // findViewById(R.id.btn_back).setOnClickListener(this);
        findViewById(R.id.btn_account_withdraw_confirm)
                .setOnClickListener(this);
        findViewById(R.id.tv_loan_money).setOnClickListener(this);
        tv_fee = (TextView) findViewById(R.id.tv_loan_money);
        findViewById(R.id.btn_wd_calfee).setOnClickListener(this);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    public void onClick(View v) {
        String amt =  etWithdrawMoney.getText().toString();
        switch (v.getId()) {

            case R.id.btn_account_withdraw_confirm:
                if (!TextUtils.isEmpty(amt) && Double.parseDouble(amt) >= 1.00){
                    getPassword(AmountUtils.changeY2Y(amt));
                }else {
                    Toast.makeText(AccountWithdrawActivity.this, "请输入正确的提现金额,至少1元",
                            Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.tv_loan_money:
                if (TextUtils.isEmpty(amt) || Double.parseDouble(amt) < 1.00){
                    Toast.makeText(AccountWithdrawActivity.this, "请输入正确的提现金额,至少1元",
                            Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.btn_wd_calfee:
                calcFee();
                break;
            case  R.id.btn_back:
                finish();
                break;
            default:
                break;
        }
    }

    private boolean checkAmt() {
        String temp = etWithdrawMoney.getText().toString().trim();
        if (TextUtils.isEmpty(temp)) {
            T.ss("请输入提现金额");
            return false;
        }
        if (User.totalAmt.equals("0")) {
            T.ss("账户无余额");
            return false;
        }
//        if (!ExpresssoinValidateUtil.isNumberStartWithOneOrNot(temp))
        if (Double.parseDouble(temp) < 1.00){
            Toast.makeText(AccountWithdrawActivity.this, "请输入正确的提现金额,至少1元",
                    Toast.LENGTH_SHORT).show();
            return false;
        }


        return true;
    }

    private void confirmWithdraw(String psw) {
        if (!checkAmt()) {
            return;
        }
        if (psw.trim().length() < 6) {
            T.ss("支付密码最短为6位");
            return;
        }
        double temp = 0;
        try {
            temp = Double.parseDouble(etWithdrawMoney.getText().toString());
            if (temp <= 0) {
                T.ss("请输入正确的提现金额");
                return;
            }
            if (temp > Double.parseDouble(User.totalAmt)) {
                T.ss("提现金额需小于账户余额");
                return;
            }
        } catch (NumberFormatException e) {
            T.ss("金额输入有误");
            return;
        }
        String amt = etWithdrawMoney.getText().toString();
        HashMap<String, String> params = new HashMap<String, String>();
        params.put("txamt", AmountUtils.changeY2F(amt));
        // params.put("cardNo", "");
        params.put("casType", "00");
        params.put("payPwd", MD5Util.generatePassword(psw));
        MyHttpClient.post(this, Urls.WITHFRAW, params,
                new AsyncHttpResponseHandler() {

                    @Override
                    public void onSuccess(int statusCode, Header[] headers,
                                          byte[] responseBody) {
                        Logger.json("[提现]", responseBody);
                        try {
                            BasicResponse r = new BasicResponse(responseBody)
                                    .getResult();
                            if (r.isSuccess()) {
                                T.showCustomeOk(AccountWithdrawActivity.this,
                                        "已提交审核", 3500);
                                finish();
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

    private void showMsg(String code, String msg) {
        Intent it = new Intent(this, ShowMsgActivity.class);
        it.putExtra("code", code);
        it.putExtra("msg", msg);
        startActivity(it);
        finish();
    }

    private void queryBalance() {

        MyHttpClient.post(this, Urls.QUERY_BALANCE,
                new HashMap<String, String>(), new AsyncHttpResponseHandler() {

                    @Override
                    public void onSuccess(int statusCode, Header[] headers,
                                          byte[] responseBody) {
                        Logger.json("[余额查询]", responseBody);
                        try {
                            BasicResponse r = new BasicResponse(responseBody)
                                    .getResult();
                            if (r.isSuccess()) {
                                JSONObject obj = r.getJsonBody();
                                User.amtT0 = AmountUtils.changeFen2Yuan(obj
                                        .optString("acT0"));
                                User.amtT1 = AmountUtils.changeFen2Yuan(obj
                                        .optString("acT1"));
                                User.amtT1y = AmountUtils.changeFen2Yuan(obj
                                        .optString("acT1Y"));
                                User.totalAmt = AmountUtils.changeFen2Yuan(obj
                                        .optString("acBal"));
                                User.acT1AP = AmountUtils.changeFen2Yuan(
                                        AmountUtils.deletePoint(obj.optString("acT1AP")));

//                                tv_detail.setText("已结算金额: " + User.amtT1y
//                                        + "元 | 未结算金额: " + User.amtT0 + "元");
                                tv_balance.setText(User.acT1AP + "元");
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

    private void calcFee() {
        final String withdrawMoney = etWithdrawMoney.getText().toString();
        if (withdrawMoney == null
                || (withdrawMoney != null && withdrawMoney.equals(""))) {
            Toast.makeText(this, "请输入提现金额", Toast.LENGTH_SHORT).show();
            return;
        }

        double temp = 0;
        try {
            temp = Double.parseDouble(withdrawMoney);
            if (temp <= 0) {
                T.ss("请输入正确的提现金额");
                return;
            }
            if (temp > Double.parseDouble(User.totalAmt)) {
                T.ss("提现金额需小于账户余额");
                return;
            }
        } catch (NumberFormatException e) {
            T.ss("金额输入有误");
            return;
        }

        String amt = etWithdrawMoney.getText().toString().trim();
//        if (!ExpresssoinValidateUtil.isNumberInPositiveWhichHasTwolengthAfterPoint(amt)) {
        if (Double.parseDouble(amt) < 1.00){
            Toast.makeText(AccountWithdrawActivity.this, "请输入正确的提现金额,至少1元",
                    Toast.LENGTH_SHORT).show();
            return;
        }


        HashMap<String, String> params = new HashMap<String, String>();
        params.put("txamt", AmountUtils.changeY2F(amt));
        params.put("casType", "3");
        MyHttpClient.post(this, Urls.CALC_FEE, params,
                new AsyncHttpResponseHandler() {

                    @Override
                    public void onSuccess(int statusCode, Header[] headers,
                                          byte[] responseBody) {
                        Logger.json("[计算手续费]", responseBody);
                        try {
                            BasicResponse r = new BasicResponse(responseBody).getResult();
                            if (r.isSuccess()) {
                                int fee = r.getJsonBody().optInt("fee");
                                int service = r.getJsonBody().optInt("serviceFee");

                                tv_fee.setText("￥" + AmountUtils.changeFen2Yuan((fee + service) + ""));
                            } else {
                                tv_fee.setText("计算失败...");
                                T.ss(r.getMsg());
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers,
                                          byte[] responseBody, Throwable error) {
                        networkError(statusCode, error);
                        tv_fee.setText("计算失败...");
                    }

                    @Override
                    public void onStart() {
                        super.onStart();
                        tv_fee.setText("计算中...");
                    }
                });
    }

    public void getPassword(String amt){
        CustomPasswordDialog.Builder builder = new CustomPasswordDialog.Builder(this);
        View passwordView = LayoutInflater.from(this).inflate(R.layout.layout_password_popup,  null);
        builder.setTitle("输入密码")
                .setShowBtn(false)
                .setMessage(passwordView);
        passwordDialog = builder.create();
        passwordDialog.show();

       final GridPasswordView gridPasswordView = (GridPasswordView) passwordView.findViewById(R.id.gridViewPassword);
        TextView text_amt = (TextView) passwordView.findViewById(R.id.text_amt);
        text_amt.setText(amt);
        gridPasswordView.setFocusable(true);

        gridPasswordView.setOnPasswordChangedListener(new GridPasswordView.OnPasswordChangedListener() {
            @Override
            public void onTextChanged(String psw) {

            }

            @Override
            public void onInputFinish(String psw) {
                confirmWithdraw(psw); // 提现
                gridPasswordView.clearPassword();
                passwordDialog.dismiss();
            }
        });

    }


    OnClickListener calcFee = new OnClickListener() {

        @Override
        public void onClick(View v) {
            calcFee();
        }
    };

    private void doWithdraw() {

    }
}
