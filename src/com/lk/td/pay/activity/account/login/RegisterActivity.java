package com.lk.td.pay.activity.account.login;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout.LayoutParams;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.lk.td.pay.activity.base.BaseActivity;
import com.lk.td.pay.beans.BasicResponse;
import com.lk.td.pay.golbal.Urls;
import com.lk.td.pay.tool.Logger;
import com.lk.td.pay.tool.MyHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.td.app.pay.hx.R;

import org.apache.http.Header;
import org.json.JSONException;

import java.util.HashMap;

public class RegisterActivity extends BaseActivity implements OnClickListener {

    private EditText etUserPwd, etUserPwdAgain, etRefer;
    private TextView etprovince, etcity;
    private String mobile;

    // public LocationClient mLocationClient = null;
    // public BDLocationListener myListener = new MyLocationListener();
    private String city;
    private Context ctx;
    private TextView tv_title;
    private Button btn_back;
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.register);
        ctx = this;
        mobile = getIntent().getExtras().getString("mobile");
        initView();
        // loadData("", "");
        // mLocationClient = new LocationClient(getApplicationContext()); //
        // 声明LocationClient类
        //
        // mLocationClient.setDebug(true);
        // LocationClientOption option = new LocationClientOption();
        // option.setLocationMode(LocationMode.Battery_Saving);// 设置定位模式
        // option.setCoorType("gcj02");// 返回的定位结果是百度经纬度,默认值gcj02
        // option.setScanSpan(5000);// 设置发起定位请求的间隔时间为5000ms
        // option.setIsNeedAddress(true);// 返回的定位结果包含地址信息
        // option.setNeedDeviceDirect(false);// 返回的定位结果包含手机机头的方向
        // option.setAddrType("all");
        // mLocationClient.setLocOption(option);
        // mLocationClient.registerLocationListener(myListener); // 注册监听函数
        // mLocationClient.start();
    }

    // class MyLocationListener implements BDLocationListener {
    //
    // @Override
    // public void onReceiveLocation(BDLocation arg0) {
    //
    // if (arg0 != null) {
    // System.out.println(arg0.getAltitude());
    // System.out.println(arg0.getLongitude());
    // System.out.println(arg0.getLatitude());
    // System.out.println(arg0.getLocType());
    // if (arg0.getCity() != null) {
    // L.d("register", arg0.getAddrStr());
    // System.out.println(arg0.getCity());
    // // User.ulocation = arg0.getCity();
    // // User.longtitude = arg0.getLongitude();
    // // User.latitude = arg0.getLatitude();
    // city = arg0.getCity();
    // System.out.println("province=" + arg0.getProvince());
    // mLocationClient.stop();
    // T.showCustomeShort(RegisterActivity.this, arg0.getCity());
    // }
    // L.d("register", arg0.getCity());
    // System.out.println("addr" + arg0.getAddrStr());
    // } else {
    // System.out.println("定位失败....");
    // L.d("register", "自动定位失败---");
    // }
    //
    // }
    //
    // }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // try {
        // if (mLocationClient.isStarted()) {
        // mLocationClient.stop();
        // }
        // } catch (Exception e) {
        // }
    }

    private void initView() {

        etUserPwd = (EditText) findViewById(R.id.et_register_user_pwd);
        etUserPwdAgain = (EditText) findViewById(R.id.et_register_user_pwd_again);
        findViewById(R.id.btn_register_confirm).setOnClickListener(
                new OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        register();
                    }
                });
        tv_title = (TextView) findViewById(R.id.tv_title);
        tv_title.setText("注册");
        btn_back = (Button) findViewById(R.id.btn_back);
        btn_back.setVisibility(View.VISIBLE);
        btn_back.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        //etRefer = (EditText) findViewById(R.id.et_register_referrer);
        findViewById(R.id.iv_register_contact).setOnClickListener(
                new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(Intent.ACTION_PICK,
                                ContactsContract.Contacts.CONTENT_URI);
                        RegisterActivity.this.startActivityForResult(intent, 1);
                    }
                });
    }

    @Override
    protected void onActivityResult(int arg0, int resultCode, Intent data) {
        super.onActivityResult(arg0, resultCode, data);

        if (resultCode == Activity.RESULT_OK) {
            Uri contactData = data.getData();

            Cursor c = managedQuery(contactData, null, null, null, null);
            c.moveToFirst();

            String phoneNum = getContactPhone(this, c);
            //etRefer.setText(phoneNum);

        }
    }

    private void register() {
        // String name = etRealName.getText().toString().trim();
        // if (name == null || (name != null && name.equals(""))) {
        // Toast.makeText(this, "请输入用户姓名", Toast.LENGTH_SHORT).show();
        // return;
        // } else if (!Pattern.matches("[\\u4e00-\\u9FA5\\uF900-\\uFA2D]+",
        // name)) {
        // Toast.makeText(this, "用户姓名必须全为中文", Toast.LENGTH_SHORT).show();
        // return;
        // }
        // String identifyNo = etIdentityCardNo.getText().toString().trim();
        // if (identifyNo == null || (identifyNo != null &&
        // identifyNo.equals(""))) {
        // Toast.makeText(this, "请输入身份证号", Toast.LENGTH_SHORT).show();
        // return;
        // }
        // String identifyID = null;
        // try {
        // identifyID = Checkingroutine.IDCard.IDCardValidate(identifyNo);
        // } catch (ParseException e) {
        //
        // e.printStackTrace();
        // }
        // if (!"".equals(identifyID)) {
        // Toast.makeText(this, identifyID, Toast.LENGTH_SHORT).show();
        // return;
        // }

        // String email = "";

        // if (etprovince.getText().equals("省份")) {
        // T.showCustomeShort(ctx, "请选择省份");
        // return;
        // }
        String userPasswd = etUserPwd.getText().toString().trim();
        if (userPasswd == null || (userPasswd != null && userPasswd.equals(""))) {
            Toast.makeText(this, "请输入登录密码", Toast.LENGTH_SHORT).show();
            return;
        }
        String userPasswdAgain = etUserPwdAgain.getText().toString().trim();
        if (!userPasswd.equals(userPasswdAgain)) {
            Toast.makeText(this, "登录密码二次输入不一致", Toast.LENGTH_SHORT).show();
            return;
        }
         /*String refer=etRefer.getText().toString();
         if(TextUtils.isEmpty(refer)){
        	 T.ss("请输入推荐码");
        	 return;
         }*/
//         if(!ExpresssoinValidateUtil.isMobilePhone(refer)){
//        	 T.ss("手机号码不正确");
//        	 return;
//         }
        // String payPasswd = etPayPwd.getText().toString().trim();
        // if (payPasswd == null || (payPasswd != null && payPasswd.equals("")))
        // {
        // Toast.makeText(this, "请输入支付密码", Toast.LENGTH_SHORT).show();
        // return;
        // }
        // String payPasswdAgain = etPayPwdAgain.getText().toString().trim();
        // if (!payPasswd.equals(payPasswdAgain)) {
        // Toast.makeText(this, "支付密码二次输入不一致", Toast.LENGTH_SHORT).show();
        // return;
        // }
        HashMap<String, String> params = new HashMap<String, String>();
        params.put("custPwd", userPasswd);
        // params.put("custName", name);
        // params.put("certificateNo", identifyNo);
        params.put("custMobile", mobile);

        //params.put("referrer", refer);
        MyHttpClient.postWithoutDefault(this, Urls.REGISTER, params,
                new AsyncHttpResponseHandler() {

                    @Override
                    public void onSuccess(int statusCode, Header[] headers,
                                          byte[] responseBody) {
                        Logger.json(new String(responseBody));
                        try {
                            BasicResponse response = new BasicResponse(
                                    responseBody).getResult();
                            if (response.isSuccess()) {
                                showToast(response.getMsg());
                                startActivity(new Intent(ctx,
                                        LoginActivity.class));
                            } else {
                                showDialog(response.getMsg());
//								T.sl(response.getMsg());
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
                        showLoadingDialogCannotCancle(null);
                    }

                    @Override
                    public void onFinish() {
                        super.onFinish();
                        dismissLoadingDialog();
                    }
                });

    }

    public void sh(Context ctx, String msg) {
        LayoutInflater inflater = LayoutInflater.from(ctx);
        View view = inflater.inflate(R.layout.network_error_popwindow, null);
        final PopupWindow pop = new PopupWindow(view,
                LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, false);
        Button btn = (Button) view.findViewById(R.id.btn_retry_pop);
        if (!TextUtils.isEmpty(msg)) {
            ((TextView) (view.findViewById(R.id.tv_error_pop))).setText(msg);
        }
        pop.setBackgroundDrawable(new BitmapDrawable());
        pop.setOutsideTouchable(true);
        pop.setFocusable(true);
        btn.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {

                pop.showAsDropDown(v);

            }
        });
    }

    private void gotoLogin() {
        Intent it = new Intent(this, LoginActivity.class);
        startActivity(it);
    }

    // private String provinceId, cityId;
    // private String[] bankNames, bankIds, provinceIds, provinceNames, cityIds,
    // cityNames;
    //
    // private void loadData(final String type, String proId) {
    // HashMap<String, Object> map = new HashMap<String, Object>();
    // map.put("USRMP", Infos.uAccount);
    // map.put("TYPE", type);
    // map.put("PROVINCE_NO", proId);
    // showLoadingDialog();
    // HttpRequest request = new HttpRequest(
    // URLs.QUERY_BANK_PROVINCE_CITY_WITHOUT_USERNAME, map,
    // new ResponseCallback() {
    //
    // @Override
    // public void onSuccess(Map<String, Object> response,
    // int netStatus) {
    // if (Entity.STATE_OK.equals(response.get(Entity.RSPCOD))) {
    //
    // List<Map<String, Object>> list;
    // if (response.get("NODE") instanceof List) {
    // list = (List<Map<String, Object>>) (response
    // .get("NODE"));
    // } else if (response.get("NODE") instanceof Map) {
    // list = new ArrayList<Map<String, Object>>();
    // Map<String, Object> map = (Map<String, Object>) response
    // .get("NODE");
    // list.add(map);
    // } else {
    // list = new ArrayList<Map<String, Object>>();
    // }
    //
    // int length = list.size();
    // if ("1".equals(type)) {
    // provinceIds = new String[length];
    // provinceNames = new String[length];
    //
    // Collections.sort(list,
    // new Comparator<Map<String, Object>>() {
    //
    // @Override
    // public int compare(
    // Map<String, Object> arg0,
    // Map<String, Object> arg1) {
    // String name0 = "";
    // String name1 = "";
    // if (null != arg0.get("Name")) {
    // name0 = arg0.get("Name")
    // .toString();
    // }
    // if (null != arg1.get("Name")) {
    // name1 = arg1.get("Name")
    // .toString();
    // }
    //
    // Collator c = Collator
    // .getInstance(Locale.CHINA);
    // return c.compare(name0, name1);
    // }
    // });
    // for (int i = 0; i < length; i++) {
    // provinceIds[i] = list.get(i).get("ID")
    // .toString();
    // provinceNames[i] = list.get(i).get("NAME")
    // .toString();
    // }
    // Dialog dialog = new AlertDialog.Builder(ctx)
    // .setTitle("请选择开户行省份")
    // .setItems(
    // provinceNames,
    // new DialogInterface.OnClickListener() {
    //
    // @Override
    // public void onClick(
    // DialogInterface dialog,
    // int which) {
    // etprovince
    // .setText(provinceNames[which]);
    // provinceId = provinceIds[which];
    // cityNames = new String[0];
    // cityId = "";
    // loadData("2",
    // provinceId);
    // }
    // }).create();
    // dialog.show();
    // } else {
    //
    // cityIds = new String[length];
    // cityNames = new String[length];
    // for (int i = 0; i < length; i++) {
    // cityIds[i] = list.get(i).get("ID")
    // .toString();
    // cityNames[i] = list.get(i).get("NAME")
    // .toString();
    // }
    //
    // etcity.setText(cityNames[0]);
    // cityId = cityIds[0];
    // }
    // } else if (Entity.STATE_OUT_TIME.equals(response
    // .get(Entity.RSPCOD))) {
    // checkLogin();
    // } else {
    // Toast.makeText(ctx,
    // response.get(Entity.RSPMSG).toString(),
    // Toast.LENGTH_SHORT).show();
    //
    // }
    // dismissLoadingDialog();
    // }
    //
    // @Override
    // public void onFailure(int netStatus) {
    // Toast.makeText(ctx, "网络异常", Toast.LENGTH_SHORT).show();
    // dismissLoadingDialog();
    // }
    // });
    //
    // channel.request(request);
    // }

    @Override
    public void onClick(View v) {
        // switch (v.getId()) {
        // case R.id.et_register_city:
        // Dialog dialog = new AlertDialog.Builder(ctx).setTitle("请选择城市")
        // .setItems(cityNames, new DialogInterface.OnClickListener() {
        //
        // @Override
        // public void onClick(DialogInterface arg0, int position) {
        // etcity.setText(cityNames[position]);
        // cityId = cityIds[position];
        // }
        // }).create();
        //
        // dialog.show();
        // break;
        // case R.id.et_register_province:
        // System.out.println("onclick...");
        // // loadData("1", "");
        // break;
        // }

    }

    /**
     * 获取联系人电话号码
     *
     * @param mContext
     * @param cursor
     * @return
     */
    public static String getContactPhone(Context mContext, Cursor cursor) {

        int phoneColumn = cursor
                .getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER);
        int phoneNum = cursor.getInt(phoneColumn);
        String phoneResult = "";

        if (phoneNum > 0) {
            // 获得联系人的ID号
            int idColumn = cursor.getColumnIndex(ContactsContract.Contacts._ID);
            String contactId = cursor.getString(idColumn);
            // 获得联系人的电话号码的cursor;
            Cursor phones = mContext.getContentResolver().query(
                    ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                    null,
                    ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = "
                            + contactId, null, null);

            if (phones.moveToFirst()) {
                // 遍历所有的电话号码
                for (; !phones.isAfterLast(); phones.moveToNext()) {
                    int index = phones
                            .getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
                    int typeindex = phones
                            .getColumnIndex(ContactsContract.CommonDataKinds.Phone.TYPE);
                    int phone_type = phones.getInt(typeindex);
                    String phoneNumber = phones.getString(index);

                    // 当手机号码为空的或者为空字段 跳过当前循环
                    if (TextUtils.isEmpty(phoneNumber))
                        continue;

                    switch (phone_type) {
                        case 2:
                            phoneResult = phoneNumber;
                            break;
                    }

                }
                if (!phones.isClosed()) {
                    phones.close();
                }
            }
        }
        return phoneResult;
    }
}