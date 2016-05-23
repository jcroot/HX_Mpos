package com.lk.td.pay.activity.main.quickpay;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.annotation.ContentView;
import com.android.annotation.ViewInject;
import com.android.annotation.event.OnClick;
import com.lk.td.pay.activity.base.BaseActivity;
import com.lk.td.pay.activity.main.cashin.swing.SignaturePadActivity;
import com.lk.td.pay.request.BasicRequest;
import com.lk.td.pay.request.ParamsUtils;
import com.lk.td.pay.tool.MyHttpClient;
import com.lk.td.pay.utils.ViewUtils;
import com.lk.td.pay.wedget.CustomDialog;
import com.lk.td.pay.wedget.CustomPopupWindow;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.td.app.pay.hx.R;

import org.apache.http.Header;

import java.util.HashMap;

/**
 * Created by wsq on 2016/5/13.
 */
@ContentView(R.layout.layout_bank_select)
public class BankSelectActivity extends BaseActivity{

    @ViewInject(R.id.btn_back) Button btn_back;
    @ViewInject(R.id.tv_title)TextView tv_title;
    @ViewInject(R.id.et_cradNo)EditText et_cradNo;
    @ViewInject(R.id.et_period)EditText et_period;
    @ViewInject(R.id.et_cvv)EditText et_cvv;

    private CustomDialog dialog;
    private String cradNo, cvv, period;

    private CustomPopupWindow bankPopup;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ViewUtils.inject(this);
        init();
    }

    public void init(){
        tv_title.setText(getString(R.string.quick_pay));
        btn_back.setVisibility(View.VISIBLE);

    }

    @OnClick({  R.id.btn_back,                  //返回按钮
                R.id.btn_next,                  //下一步
            })
    public void onclickListener(View view ){
        Intent intent = new Intent();
        switch (view.getId()){
            case R.id.btn_back:
                finish();
                break;
            case R.id.btn_next:
                verifyContent();
                break;

        }
    }
    public void verifyContent(){

        //输入的卡号
        cradNo =et_cradNo.getText().toString();

        //输入的cvv
        cvv = et_cvv.getText().toString();

        //输入的有效期
        period = et_period.getText().toString();

        showDialog();

    }

    public void showDialog(){
        CustomDialog.Builder builder = new CustomDialog.Builder(this);
        builder.setTitle("提示")
                .setMessage("您在本应用中还未做过快捷支付， 请先审核协议")
                .setOkBtn("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int i) {
                        Intent intent = new Intent(BankSelectActivity.this, BankInfoActivity.class);
                        intent.putExtra(ParamsUtils.CARD_NO, cradNo);
                        intent.putExtra("cvv", cvv);
                        intent.putExtra(ParamsUtils.PERIOD, period);
                        startActivity(intent);
                        dialog.dismiss();
                        finish();
                    }
                }).setCancelBtn("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int i) {
                dialog.dismiss();
                finish();
            }
        });
        dialog = builder.create();

        dialog.show();

    }

    /**
     * 获取银行列表
     */
    public void getBankList(){

        Intent intent = new Intent(this, BankInfoActivity.class);
        startActivity(intent);
    }

    Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case BasicRequest.FLAG_REQUEST_SUCCESS:

                    break;
                case BasicRequest.FLAG_REQUEST_UNSUCCESS:

                    break;
                case BasicRequest.FLAG_REQUEST_FAIL:

                    break;
            }
        }
    };

}
