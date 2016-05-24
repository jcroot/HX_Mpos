package com.lk.td.pay.activity.main.quickpay;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.annotation.ContentView;
import com.android.annotation.ViewInject;
import com.android.annotation.event.OnClick;
import com.lk.td.pay.activity.base.BaseActivity;
import com.lk.td.pay.request.ParamsUtils;
import com.lk.td.pay.tool.T;
import com.lk.td.pay.utils.ExpresssoinValidateUtil;
import com.lk.td.pay.utils.IDCardValidateTool;
import com.lk.td.pay.utils.ViewUtils;
import com.td.app.pay.hx.R;

/**
 * Created by wsq on 2016/5/23.
 */
@ContentView(R.layout.layout_protocol_auth)
public class BankInfoActivity extends BaseActivity{

    @ViewInject(R.id.btn_back)Button btn_back;
    @ViewInject(R.id.tv_cardNo)TextView tv_cardNo;
    @ViewInject(R.id.tv_period)TextView tv_period;
    @ViewInject(R.id.tv_cvv)TextView tv_cvv;
    @ViewInject(R.id.et_cradNo)EditText et_cradNo;
    @ViewInject(R.id.et_Mobile)EditText et_Mobile;
    @ViewInject(R.id.auth_protocol)LinearLayout auth_protocol;
    private boolean isAuth = false; //表示是否验证

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ViewUtils.inject(this);
        init();
    }

    public void init(){

        btn_back.setVisibility(View.VISIBLE);
        Intent intent = getIntent();
        tv_cardNo.setText(intent.getStringExtra(ParamsUtils.CARD_NO));
        tv_cvv.setText(intent.getStringExtra("cvv"));
        tv_period.setText(intent.getStringExtra(ParamsUtils.PERIOD));
        isAuth = intent.getBooleanExtra("isAuth", false);
        auth_protocol.setVisibility(isAuth ? View.GONE : View.VISIBLE);

    }

    @OnClick({R.id.btn_back,
            R.id.btn_protocol_next})
    public void onClick(View view){
        switch (view.getId()){
            case R.id.btn_back:
                finish();
                break;
            case R.id.btn_protocol_next:
                verifyContent();
                break;
        }
    }

    /**
     * 验证输入的内容
     */
    public void verifyContent(){

        //验证输入的身份证号是否正确
        String cardNo =et_cradNo.getText().toString();
        if(!IDCardValidateTool.validateCard(cardNo)){
            T.sl("请输入正确的身份证号码");
            return ;
        }
        //验证输入的手机号码是否正确
        String mobile  =et_Mobile.getText().toString();
        if (!ExpresssoinValidateUtil.isMobilePhone(mobile)){
            T.sl("请输入正确的电话号码");
            return;
        }

        if (isAuth){
            startActivity(new Intent(BankInfoActivity.this, QuickPayActvity.class));
        }else{
            //在此处需要验证，生成协议信息
            //发送短信消息
        }

    }

}