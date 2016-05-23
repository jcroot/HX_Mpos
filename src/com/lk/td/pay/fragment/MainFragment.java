package com.lk.td.pay.fragment;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.TextView;

import com.lk.td.pay.activity.account.info.AccountWithdrawActivity;
import com.lk.td.pay.activity.main.cashin.CashInActivity;
import com.lk.td.pay.activity.main.cashin.CheckActivity;
import com.lk.td.pay.activity.main.notice.NoticeActivity;
import com.lk.td.pay.activity.account.auth.RealNameAuthenticationActivity;
import com.lk.td.pay.activity.main.cashin.swing.ty.bluetooth.SwingCardByTYBluetootchActivity;
import com.lk.td.pay.activity.main.cashin.swing.xdl.SwingXDLCardBalanceActivity;
import com.lk.td.pay.activity.main.quickpay.BankSelectActivity;
import com.lk.td.pay.adapter.MainDataAdapter;
import com.lk.td.pay.beans.BasicResponse;
import com.lk.td.pay.beans.CardBean;
import com.lk.td.pay.beans.MainDataBean;
import com.lk.td.pay.beans.PosData;
import com.lk.td.pay.golbal.Actions;
import com.lk.td.pay.golbal.AppConfig;
import com.lk.td.pay.golbal.MApplication;
import com.lk.td.pay.golbal.Urls;
import com.lk.td.pay.golbal.User;
import com.lk.td.pay.tool.Logger;
import com.lk.td.pay.tool.MyHttpClient;
import com.lk.td.pay.tool.T;
import com.lk.td.pay.wedget.CustomDialog;
import com.lk.td.pay.wedget.flashview.EffectConstants;
import com.lk.td.pay.wedget.flashview.FlashView;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.td.app.pay.hx.R;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MainFragment extends BaseFragment implements OnClickListener,AdapterView.OnItemClickListener {


    private View view;
    private FlashView flash;
    private GridView grid_main;
    private MainDataAdapter mAdapter;
    private List<String> imgs = new ArrayList<String>();
    private final String DATA_PATH = "data/main";
    private List<MainDataBean> mData = new ArrayList<MainDataBean>();
    private final int COLUMNS = AppConfig.COLUMNS;

    CustomDialog dialog;
    private List<CardBean> listType;
    private  String[] type;
    private TextView tv_title;
    private ImageButton notice_main;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_main_layout, null);
        return  view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        loadNotice();
        loadAuth();
        getAdUrl();

        getData();
        listType = MApplication.getInstance().getCardType();
        type = new String[listType.size()];

    }

    private void initView() {
        grid_main = (GridView) view.findViewById(R.id.grid_main);
        grid_main.setNumColumns(COLUMNS);
        mAdapter = new MainDataAdapter(getActivity(), mData, COLUMNS);
        grid_main.setAdapter(mAdapter);
        grid_main.setOnItemClickListener(this);

        flash = (FlashView) view.findViewById(R.id.cycleview_main);
        flash.setImageUris(imgs);
        flash.setEffect(EffectConstants.DEFAULT_EFFECT);

        tv_title = (TextView) view.findViewById(R.id.tv_title);
        tv_title.setText(getString(R.string.app_name));
        notice_main = (ImageButton) view.findViewById(R.id.notice_main);
        notice_main.setVisibility(View.VISIBLE);
        notice_main.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        Intent intent = new Intent();
        switch (v.getId()) {
            case R.id.notice_main:   //公告
                intent.setClass(getActivity(), NoticeActivity.class);
                startActivity(intent);
                break;
        }
    }

    /**
     * 获取首页轮播图路径
     */
    private void getAdUrl() {
        HashMap<String, String> params = new HashMap<String, String>();
        params.put("custId", User.uId);
        params.put("custMobile", User.uAccount);
        MyHttpClient.post(getActivity(), Urls.MAIN_AD_IMG,
                params, new AsyncHttpResponseHandler() {

                    @Override
                    public void onSuccess(int statusCode, Header[] headers,
                                          byte[] responseBody) {
                        Logger.json(responseBody);
                        try {
                            BasicResponse json = new BasicResponse(responseBody)
                                    .getResult();
                            if (json.isSuccess()) {
                                JSONArray array = json.getJsonBody()
                                        .optJSONArray("imgList");
                                for (int i = 0; i < array.length(); i++) {
                                    String temp = array.getJSONObject(i)
                                            .optString("appimgPath");
                                    if (!TextUtils.isEmpty(temp)) {
                                        imgs.add(AppConfig.HOST + temp);
                                    }
                                }
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers,
                                          byte[] responseBody, Throwable error) {
                    }

                    @Override
                    public void onFinish() {
                        super.onFinish();
                        initView();
                    }
                });
    }
    private void bindDevice() {

        CustomDialog.Builder builder = new CustomDialog.Builder(getActivity());
        builder.setTitle(getString(R.string.select_card_type));
        builder.setListView(listType, cardItemClickLisener);
        dialog =  builder.create();
        dialog.show();
    }
    AdapterView.OnItemClickListener cardItemClickLisener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
            CardBean card = listType.get(position);
            Intent intent = new Intent();
            switch (card.getId()){
                case 1:
                    intent.setClass(getActivity(),SwingXDLCardBalanceActivity.class);
                    intent.setAction(Actions.ACTION_QUERY_BALANCE);
                    break;
                case 2:
                case 4:
                    PosData.getPosData().setActtext("ACTION_QUERY_BALANCE");
                    intent.setClass(getActivity(), CheckActivity.class);
                    intent.setAction(Actions.ACTION_QUERY_BALANCE);
                    intent.putExtra(CheckActivity.TYPE, CheckActivity.TYPE_CONN);
                    intent.putExtra(CheckActivity.SWING_TYPE,
                            card.getId()==2
                                    ?
                                    CheckActivity.SWING_XDL_BLUETOOTH
                                    :
                                    CheckActivity.SWING_XDL_KEYMAP);
                    MApplication.getInstance().setKeymap(
                            card.getId()==2
                                    ?
                              false : true);
                    break;
                case 3:

                    PosData.getPosData().setActtext("ACTION_QUERY_BALANCE");
                    intent.setClass(getActivity(),SwingCardByTYBluetootchActivity.class);
                    intent.setAction(Actions.ACTION_QUERY_BALANCE);

                    break;
                default:
                    break;
            }
            startActivity(intent);


            dialog.dismiss();
        }
    };

    /**
     * 加载首页公告通知
     */
    private void loadNotice() {
        HashMap<String, String> params = new HashMap<>();
        params.put("pageSize", 10 + "");
        params.put("start", "0");
        params.put("noticeStatus", "1");  //紧急公告
        MyHttpClient.post(context, Urls.SYSTEM_MESSAGE, params,
                new AsyncHttpResponseHandler() {

                    @Override
                    public void onSuccess(int statusCode, Header[] headers,
                                          byte[] responseBody) {
                        String response = new String(responseBody);
                        Logger.json(response);

                        try {
                            BasicResponse result = new BasicResponse(responseBody).getResult();
                            if (result.isSuccess()) {
                                JSONObject jsonOneBody = result.getJsonBody();
                                JSONArray array = jsonOneBody.getJSONArray("noticeList");
                                for (int i = 0; i < array.length(); i++) {
                                    JSONObject temp = array.getJSONObject(i);
                                    String noticeId = temp.optString("noticeId");
                                    String message = temp.optString("noticeBody");
                                    String noticeTitle = temp.optString("noticeTitle");
                                    showNoticeDialog(noticeId, noticeTitle, message);
                                }
                            } else {
                                showToast(result.getMsg());
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

    private void showNoticeDialog(final String noticeId, String noticeTitle, String message) {
        CustomDialog.Builder builder = new CustomDialog.Builder(getActivity());
        builder.setTitle(noticeTitle);
        builder.setMessage(message);
        builder.setOkBtn(getString(R.string.no_tips), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                setMessage(User.uId, noticeId);
                dialog.dismiss();
            }
        });
        builder.setCancelBtn(getString(R.string.go_to), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent();
                intent.setClass(getActivity(), NoticeActivity.class);
                startActivity(intent);
                dialog.dismiss();
            }
        });
        builder.create().show();
    }


    private void setMessage(String id, String noticeId) {
        HashMap<String, String> params = new HashMap<>();
        params.put("Id", id);
        params.put("noticeId", noticeId);
        MyHttpClient.post(context, Urls.SET_MESSAGE, params,
                new AsyncHttpResponseHandler() {

                    @Override
                    public void onSuccess(int statusCode, Header[] headers,
                                          byte[] responseBody) {
                        String response = new String(responseBody);
                        Logger.json(response);
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

    private void loadAuth(){
        if (User.uStatus == 0) {
            CustomDialog.Builder builder = new CustomDialog.Builder(getActivity());
            builder.setTitle(getString(R.string.tip));
            builder.setMessage(getString(R.string.no_authentication));
            builder.setOkBtn(getString(R.string.go_to_authentication),
                    new DialogInterface.OnClickListener() {

                        public void onClick(DialogInterface dialog,
                                            int which) {

                            getActivity().startActivity(
                                    new Intent(getActivity(),
                                            RealNameAuthenticationActivity.class));
                            dialog.cancel();
                        }
                    });
            CustomDialog dialog = builder.create();
            dialog.show();
            return;
        }
    }

    /**
     * 获取主界面显示的数据
     */
    public void getData(){

        try {
           InputStream is =  getActivity().getResources().getAssets().open(DATA_PATH);
            InputStreamReader reader = new InputStreamReader(is);
            BufferedReader buffReader = new BufferedReader(reader);
           String content = "";
            String line = "";
            while ( (line = buffReader.readLine())!=null){
                content += line;
            }

            JSONObject jsonObject =  new JSONObject(content);

            JSONArray jsona = jsonObject.getJSONArray("data");


            for (int i = 0 , len = jsona.length(); i<len ; i++){
                JSONObject json = jsona.getJSONObject(i);
                MainDataBean data = new MainDataBean();
                data.setId(json.optInt("id", 0));
                data.setName(json.getString("name"));
                data.setState(json.optBoolean("state",true));
                data.setImgPath(json.optString("url"));
                data.setEnable(json.optBoolean("enable", false));
                if (data.isState()){
                    mData.add(data);
                }
            }

            if (mData.size() %COLUMNS !=0){

                for (int j = 0, len = COLUMNS - mData.size() % COLUMNS; j < len; j++){
                    MainDataBean data = new MainDataBean();
                    data.setImgPath("assets://image/not_use.png");
                    data.setId(-1);
                    mData.add(data);
                }
            }
            if (mAdapter!=null){
                mAdapter.notifyDataSetChanged();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
        MainDataBean data = mData.get(position);
        int id = data.getId();
        Intent intent = new Intent();
        if (!data.isEnable()){
            T.ss(getString(R.string.app_close));
            return;
        }
        switch (id){
            case 1:
                if (MApplication.getInstance().chechStatus())//实名认证
                    if (User.termNum == 0) {//绑定刷卡器
                        T.ss(getString(R.string.unbind_card));
                        return;
                    } else {
                        intent.setClass(getActivity(), CashInActivity.class).setAction(CashInActivity.ACTION_T1);
                        startActivity(intent);
                    }
                else {
                    return;
                }
                break;
            case 2:
                if (User.cardBundingStatus != 2) {
                    com.lk.td.pay.tool.T.ss(getString(R.string.unbind_card_pass));
                    return;
                }
                if (MApplication.getInstance().chechStatus()) {
                    intent.setClass(getActivity(), AccountWithdrawActivity.class);
                    startActivity(intent);
                }
                break;
            case 5:
                if (MApplication.getInstance().chechStatus())//实名认证
                    if (User.termNum == 0) {//绑定刷卡器
                        T.ss(getString(R.string.unbind_card));
                        return;
                    } else {
                        bindDevice();
                    }
                else {
                    return;
                }
                break;
            case 13:

                intent.setClass(getActivity(), BankSelectActivity.class);
                startActivity(intent);
                break;

        }

    }
}
