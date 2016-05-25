package com.lk.td.pay.activity.main.quickpay;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.annotation.ContentView;
import com.android.annotation.ViewInject;
import com.android.annotation.event.OnClick;
import com.android.annotation.event.OnTouch;
import com.lk.td.pay.activity.base.BaseActivity;
import com.lk.td.pay.beans.BasicResponse;
import com.lk.td.pay.beans.PopupItem;
import com.lk.td.pay.request.BasicRequest;
import com.lk.td.pay.request.ParamsUtils;
import com.lk.td.pay.tool.T;
import com.lk.td.pay.utils.BankCardValidate;
import com.lk.td.pay.utils.ViewUtils;
import com.lk.td.pay.wedget.CustomDialog;
import com.lk.td.pay.wedget.CustomPopupWindow;
import com.lk.td.pay.wedget.wheelview.view.DateSelectorUtils;
import com.td.app.pay.hx.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.util.Date;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * Created by wsq on 2016/5/13.
 */
@ContentView(R.layout.layout_bank_select)
public class BankSelectActivity extends BaseActivity{

    @ViewInject(R.id.btn_back) Button btn_back;
    @ViewInject(R.id.tv_title)TextView tv_title;
    @ViewInject(R.id.et_cradNo)EditText et_cradNo;
    @ViewInject(R.id.select_bank)LinearLayout select_bank;
    @ViewInject(R.id.et_period)EditText et_period;
    @ViewInject(R.id.et_cvv)EditText et_cvv;
    @ViewInject(R.id.txt_bank)TextView txt_bank;

    private static final int BANK_CARD_MIN = 13;
    private static final int BANK_CARD_MAX = 19;

    private CustomDialog dialog;
    private String cradNo, cvv, period;
    private List<PopupItem> mData;

    private CustomPopupWindow bankPopup;
    private CustomPopupWindow dateSelector;
    private BankSelectActivity mContext;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ViewUtils.inject(this);
        init();
    }

    public void init(){
        tv_title.setText(getString(R.string.quick_pay));
        btn_back.setVisibility(View.VISIBLE);

        mData = new ArrayList<PopupItem>();

        mContext = this;

        getBankList();


    }

    @OnClick({  R.id.btn_back,                  //返回按钮
                R.id.btn_next,                  //下一步
                R.id.txt_bank                   //选择银行卡
            })
    public void onclickListener(View view ){
        Intent intent = new Intent();
        switch (view.getId()){
            case R.id.btn_back:
                finish();
                break;
            case R.id.txt_bank:
                bankPopup = new CustomPopupWindow(mContext, mData,"请选择银行类型", new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                        txt_bank.setText(mData.get(position).getName());
                        bankPopup.dismiss();
                    }
                });
                bankPopup.showAtLocation(select_bank, Gravity.BOTTOM|Gravity.CENTER_HORIZONTAL, 0, 0);
                break;
            case R.id.btn_next:
                verifyContent();
                break;
        }
    }
    @OnTouch({R.id.et_period})
    public boolean onTouch(View view, MotionEvent event){
        if (view.getId() == R.id.et_period){

            switch (event.getAction()){
                case MotionEvent.ACTION_DOWN:
                    hideKeyMap();
                    showPopup();
                    break;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
//                    et_period.setEnabled(true);
//                    et_period.setClickable(true);
                    break;

            }
        }

        return true;
    }

    public void verifyContent(){

    	String text_bank = txt_bank.getText().toString();
    	if (TextUtils.isEmpty(text_bank)) {
			T.sl("请选择银行卡");
    		return;
		}
    	
    	
        //输入的卡号
        cradNo =et_cradNo.getText().toString();

        //银行卡号验证
        if (TextUtils.isEmpty(cradNo)){
            T.sl("银行卡号不能为空！");
            return;
        }
        int length = cradNo.length();
        if (length < BANK_CARD_MIN || length > BANK_CARD_MAX){
            T.sl("银行卡号位数错误！");
            return ;
        }
        if (!BankCardValidate.validateCard(cradNo)){
            T.sl("请输入正确的银行卡号！");
            return ;
        }
        //输入的cvv
        cvv = et_cvv.getText().toString();
        if (TextUtils.isEmpty(cvv)){
            T.sl("CVV码不能为空！");
            return;
        }
        //输入的有效期
        period = et_period.getText().toString();

        if (TextUtils.isEmpty(period)){
            T.sl("有效期限不能为空！");
            return ;
        }
        Calendar calendar = Calendar.getInstance();

        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM");
        Date date = null;
        try {
            date = format.parse(period);
        } catch (ParseException e) {
            e.printStackTrace();
            T.sl("日期格式错误!");
            return ;
        }

        Calendar c = Calendar.getInstance();
        c.setTime(date);

        //获取当前年月
        int curYear = calendar.get(Calendar.YEAR);
        int curMonth = calendar.get(Calendar.MONTH)+1;
        //获取选择的年月
        int selYear = c.get(Calendar.YEAR);
        int selMonth = c.get(Calendar.MONTH)+1;
        if (curYear > selYear){
            T.sl("银行卡已失效");
            return;
        }else if(curYear == selYear){

            if (curMonth >= selMonth){
                T.sl("银行卡已失效");
                return;
            }
        }


        showDialog();

    }

    public void showDialog(){
        CustomDialog.Builder builder = new CustomDialog.Builder(this);
        builder.setTitle("提示")
                .setMessage("您在本应用中还未做过快捷支付， 请先审核协议")
                .setOkBtn("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int i) {
                        Intent intent = new Intent(mContext, BankInfoActivity.class);
                        intent.putExtra(ParamsUtils.CARD_NO, cradNo);
                        intent.putExtra("cvv", cvv);
                        intent.putExtra(ParamsUtils.PERIOD, period);
                        intent.putExtra("isAuth",false);
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

        try {
            InputStream is = getResources().getAssets().open("data/banklist");

            BufferedReader reader = new BufferedReader(new InputStreamReader(is));

            String content = "";
            String line = "";

            while ((line = reader.readLine())!=null){
                content += line;
            }
            BasicResponse response = new BasicResponse(content.getBytes()).getResult();

            JSONArray jsona = response.getJsonBody().getJSONArray("BANKLIST");

            for (int i = 0, len = jsona.length(); i <len ; i++) {
                PopupItem item = new PopupItem();
                JSONObject json = jsona.optJSONObject(i);
                item.setId(i);
                item.setName(json.optString("bankName"));
                mData.add(item);
            }

        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * 隐藏键盘
     */
    public void hideKeyMap(){
//        et_period.setEnabled(false);
        et_period.setFocusable(false);
        et_period.setFocusableInTouchMode(false);
//        et_period.setClickable(false);
        View view = getWindow().peekDecorView();
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    /**
     * 显示日期选择器
     */
    public void showPopup(){

        Log.d("","========您点击了====");
        if (dateSelector == null) {
            dateSelector = new CustomPopupWindow(mContext,
                    DateSelectorUtils.showDateSelector(mContext),
                    "确定","",
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {

                            et_period.setText(DateSelectorUtils.SELECTOR_YEAR+"-"+DateSelectorUtils.SELECTOR_MONTH);
                            dateSelector.dismiss();
                        }
                    });
        }

        dateSelector.showAtLocation(select_bank, Gravity.BOTTOM|Gravity.CENTER_HORIZONTAL, 0, 0);
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
