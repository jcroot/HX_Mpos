package com.lk.td.pay.activity.main.quickpay;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.android.annotation.ContentView;
import com.android.annotation.ViewInject;
import com.android.annotation.event.OnClick;
import com.lk.td.pay.activity.base.BaseActivity;
import com.lk.td.pay.request.ParamsUtils;
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

    }

    @OnClick({R.id.btn_back})
    public void onClick(View view){
        switch (view.getId()){
            case R.id.btn_back:
                finish();
                break;
            case R.id.btn_protocol_next:
                startActivity(new Intent(BankInfoActivity.this, QuickPayActvity.class));
                break;
        }
    }

}
