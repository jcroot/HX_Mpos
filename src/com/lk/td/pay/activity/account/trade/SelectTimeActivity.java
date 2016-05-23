package com.lk.td.pay.activity.account.trade;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.github.jjobes.slidedatetimepicker.SlideDateTimeListener;
import com.github.jjobes.slidedatetimepicker.SlideDateTimePicker;
import com.lk.td.pay.activity.base.BaseActivity;
import com.td.app.pay.hx.R;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by zhenglibin on 16/3/7.
 */
public class SelectTimeActivity extends BaseActivity implements View.OnClickListener, View.OnTouchListener {

    private Button btn_back;
    private TextView tv_title;
    private EditText et_select_start_time,
            et_select_end_time;
    private Button
            btn_confirm,
            btn_cancel;
    private TextView tv_start_time,
            tv_end_time;

    private SimpleDateFormat mFormatter = new SimpleDateFormat("yyyyMMddHHmmss");

    private String startTime;
    private String endTime;

    private Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_time);
        mContext = this;
        initView();
        setListener();
    }

    private void initView() {
        tv_start_time = (TextView) this.findViewById(R.id.tv_start_time);
        tv_end_time = (TextView) this.findViewById(R.id.tv_end_time);
        et_select_start_time = (EditText) this.findViewById(R.id.et_select_start_time);
        et_select_end_time = (EditText) this.findViewById(R.id.et_select_end_time);
        btn_confirm = (Button) this.findViewById(R.id.btn_confirm);
        btn_cancel = (Button) this.findViewById(R.id.btn_cancel);
        initTitle();
    }

    private void initTitle() {
        btn_back = (Button) findViewById(R.id.btn_back);
        tv_title = (TextView) findViewById(R.id.tv_title);
        tv_title.setText("选择时间");
        btn_back.setVisibility(View.VISIBLE);
        btn_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    private void setListener() {
        btn_confirm.setOnClickListener(this);
        btn_cancel.setOnClickListener(this);

        et_select_start_time.setOnTouchListener(this);
        et_select_end_time.setOnTouchListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {

            case R.id.btn_confirm:
                if (TextUtils.isEmpty(startTime)){
                    Toast.makeText(mContext,"请选择起始时间",Toast.LENGTH_SHORT).show();
                    break;
                }

                if (TextUtils.isEmpty(endTime)){
                    Toast.makeText(mContext,"请选择结束时间",Toast.LENGTH_SHORT).show();
                    break;
                }

                BigDecimal startDecimal = new BigDecimal(startTime);
                BigDecimal endDecimal = new BigDecimal(endTime);

                if (startDecimal.compareTo(endDecimal) == -1) {
                    Intent intent = new Intent();
                    intent.putExtra("START_TIME", startTime);
                    intent.putExtra("END_TIME", endTime);
                    setResult(RESULT_OK, intent);
                    finish();
                } else {
                    Toast.makeText(mContext,"开始时间需小于结束时间",Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.btn_cancel:
                finish();
                break;
            default:
                break;
        }
    }

    private SlideDateTimeListener startListener = new SlideDateTimeListener() {
        @Override
        public void onDateTimeSet(Date date) {
            startTime = mFormatter.format(date);
            et_select_start_time.setText(mFormatter.format(date));
        }

        @Override
        public void onDateTimeCancel() {

        }
    };

    private SlideDateTimeListener endListener = new SlideDateTimeListener() {
        @Override
        public void onDateTimeSet(Date date) {
            endTime = mFormatter.format(date);
            et_select_end_time.setText(mFormatter.format(date));
        }

        @Override
        public void onDateTimeCancel() {

        }
    };

    @Override
    public boolean onTouch(View view, MotionEvent e) {
        switch (e.getAction()){
            case MotionEvent.ACTION_DOWN:

                if (view.getId() ==et_select_start_time.getId()){
                    new SlideDateTimePicker.Builder(getSupportFragmentManager())
                            .setListener(startListener)
                            .setInitialDate(new Date())
                            .build()
                            .show();
                }else if(view.getId() == et_select_end_time.getId()){
                new SlideDateTimePicker.Builder(getSupportFragmentManager())
                        .setListener(endListener)
                        .setInitialDate(new Date())
                        .build()
                        .show();
            }
                break;
        }
        return false;
    }
}
