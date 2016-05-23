package com.lk.td.pay.activity.main.quickpay;

import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.Button;

import com.android.annotation.ContentView;
import com.android.annotation.ViewInject;
import com.android.annotation.event.OnClick;
import com.lk.td.pay.activity.base.BaseActivity;
import com.lk.td.pay.utils.ViewUtils;
import com.td.app.pay.hx.R;

/**
 * Created by wsq on 2016/5/20.
 */
@ContentView(R.layout.layout_quick_pay)
public class QuickPayActvity extends BaseActivity{

    @ViewInject(R.id.btn_back)Button btn_back;
    @ViewInject(R.id.btn_GetVerify)Button btn_GetVerify;
    public static final String PHONE_NUM = "PHONE_NUM";
    private String phoneNum;
    private SmsCodeCount smsCodeCount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ViewUtils.inject(this);
        init();
    }

    public void init(){
        //获取手机号码
        phoneNum = getIntent().getStringExtra(PHONE_NUM);
        btn_back.setVisibility(View.VISIBLE);
        smsCodeCount = new SmsCodeCount(60*1000, 1000);

    }

    @OnClick({R.id.btn_back})
    public void onClickListner(View view){
        switch (view.getId()){
            case R.id.btn_back:
                finish();
                break;

            default:

                break;

        }
    }


    /**
     * 获取短信验证之后的倒计时显示
     */
    class SmsCodeCount extends CountDownTimer{

        public SmsCodeCount(long millisInFuture, long countDownInterval){
            super(millisInFuture, countDownInterval);
        }
        @Override
        public void onTick(long millisUntilFinished) {

            btn_GetVerify.setText((millisUntilFinished/1000) + "秒");
            btn_GetVerify.setEnabled(false);
        }

        @Override
        public void onFinish() {
            btn_GetVerify.setText("再次获取");
            btn_GetVerify.setEnabled(true);
        }
    }

    /**
     * 在销毁界面的时候，取消倒计时显示的线程
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (null != smsCodeCount){
            smsCodeCount.cancel();
        }
    }
}
