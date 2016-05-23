package com.lk.td.pay.activity.account.info;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import com.lk.td.pay.activity.base.BaseActivity;
import com.lk.td.pay.beans.BankCardItem;
import com.lk.td.pay.beans.BasicResponse;
import com.lk.td.pay.golbal.Urls;
import com.lk.td.pay.golbal.User;
import com.lk.td.pay.tool.Logger;
import com.lk.td.pay.tool.MyHttpClient;
import com.lk.td.pay.tool.T;

import com.lk.td.pay.utils.AmountUtils;
import com.lk.td.pay.utils.Utils;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.td.app.pay.hx.R;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class AccountInfoActivity extends BaseActivity {

    private Button mBtn_back;
    private TextView mTv_info_account;
    private TextView mTv_info_name;
    private TextView bankNameText;
    private TextView mTv_info_bcno;
    private TextView tv_balance_1,tv_balance_2,tv_balance_3, tv_detail,tv_detail_1;
    private List<BankCardItem> bankList = new ArrayList<BankCardItem>();
    private Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.account_info);
        bindViews();
    }

    private void bindViews() {
        mContext = this;
        tv_balance_1 = (TextView) findViewById(R.id.tv_balance_total_1);
        tv_balance_2 = (TextView) findViewById(R.id.tv_balance_total_2);
        tv_balance_3 = (TextView) findViewById(R.id.tv_balance_total_3);
        tv_detail = (TextView) findViewById(R.id.tv_balance_detail);
        tv_detail_1 = (TextView) findViewById(R.id.tv_balance_detail_1);
        mBtn_back = (Button) findViewById(R.id.btn_back);
        mBtn_back.setText("我的账户");
        mBtn_back.setVisibility(View.VISIBLE);
        findViewById(R.id.tv_title).setVisibility(View.GONE);
        mTv_info_account = (TextView) findViewById(R.id.tv_info_account);
        mTv_info_name = (TextView) findViewById(R.id.tv_info_name);
        bankNameText = (TextView) findViewById(R.id.tv_info_bank_name);
        mTv_info_bcno = (TextView) findViewById(R.id.tv_info_bcno);
        mTv_info_account.setText(Utils.hiddenMobile(User.uAccount));

        mTv_info_name.setText(toS(User.uName));
        mBtn_back.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                finish();
            }
        });
        findViewById(R.id.accnount_info_confirm_btn).setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                finish();

            }
        });
        getBankCardStatus();
        queryBalance();
    }

    private void getBankCardStatus() {
        HashMap<String, String> requestMap = new HashMap<String, String>();
        MyHttpClient.post(AccountInfoActivity.this, Urls.GET_BANKCARD_LIST, requestMap,
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
                                    bankNameText.setText(bankList.get(0).getCardName());
                                    mTv_info_bcno.setText(Utils.hiddenCardNo(bankList.get(0).getCardNo()));
                                } else {
                                    bankNameText.setText("--");
                                    mTv_info_bcno.setText("--");
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
//						dismissLoadingDialog();
                    }
                });

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
                                User.acT1UNA = AmountUtils.changeFen2Yuan(AmountUtils.deletePoint(obj.optString("acT1UNA")));
                                User.acT1AUNP = AmountUtils.changeFen2Yuan(AmountUtils.deletePoint(obj.optString("acT1AUNP")));
                                User.acT1AP = AmountUtils.changeFen2Yuan(AmountUtils.deletePoint(obj.optString("acT1AP")));

                                tv_detail.setText( User.amtT1y + "元");
                                tv_detail_1.setText(User.amtT0 + "元");
                                tv_balance_1.setText(User.acT1UNA + "元" );
                                tv_balance_2.setText(User.acT1AP + "元");
                                tv_balance_3.setText(User.acT1AUNP + "元");
//                                tv_balance_3.setText();
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
                        //showLoadingDialog();
                    }

                    @Override
                    public void onFinish() {
                        super.onFinish();
                        dismissLoadingDialog();
                    }

                });

    }

    private String sub(String s) {
        if (null == s)
            return "";
        if (s.length() > 4) {
            return s.substring(s.length() - 4, s.length());
        } else {
            return "";
        }
    }

    private String toS(String s) {
        if (null == s)
            return "";
        return s;
    }
}
