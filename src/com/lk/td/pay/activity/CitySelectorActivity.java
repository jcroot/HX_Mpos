package com.lk.td.pay.activity;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.widget.SimpleCursorAdapter;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.lk.td.pay.activity.base.BaseActivity;
import com.lk.td.pay.beans.BasicResponse;
import com.lk.td.pay.golbal.Urls;
import com.lk.td.pay.tool.LSharePreference;
import com.lk.td.pay.tool.MyHttpClient;
import com.td.app.pay.hx.R;

import org.apache.http.Header;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * 城市选择器
 *
 * @author Ding
 */
public class CitySelectorActivity extends BaseActivity {

    EditText search;
    ListView listview;
    GridView gridview;
    LSharePreference share;

    // DbUtils db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.chioose_city_activity);
        share = LSharePreference.getInstance(this);
        init();
    }

    SimpleCursorAdapter adapter;
    Cursor cu;
    ArrayAdapter<String> cityadapter;
    List<String> values;
    LinearLayout panel;

    private void init() {
        values = new ArrayList<>();
        values.add("上海");
        values.add("北京");
        values.add("广州");
        values.add("深圳");
        values.add("武汉");
        values.add("南京");
        values.add("西安");
        values.add("杭州");
        values.add("郑州");
        values.add("长沙");
        values.add("厦门");
        values.add("天津");
        values.add("香港");
        values.add("澳门");
        values.add("台北");
        values.add("昆明");
        values.add("合肥");
        values.add("青岛");
        values.add("沈阳");
        values.add("石家庄");
        values.add("成都");

        search = (EditText) findViewById(R.id.search_view_city);
        panel = (LinearLayout) findViewById(R.id.city_choose_panel);
        listview = (ListView) findViewById(R.id.lisstview_search);
        gridview = (GridView) findViewById(R.id.gridview_city);
        cityadapter = new ArrayAdapter<>(this,
                R.layout.gridview_adapter, R.id.gridview_adapter_tv1, values);
        gridview.setAdapter(cityadapter);
        search.addTextChangedListener(textwatch);
        gridview.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {

                Intent intent = new Intent();
                intent.putExtra("data", values.get(position));
//					ChooseCityActivity.this.setResult(101, intent);
//					ChooseCityActivity.this.finish();
            }
        });

        listview.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {

//					try {
//						View v = view.findViewById(R.id.tv_choosecity_city);
//						String temp = ((TextView) v).getText().toString();
//						Intent intent = new Intent();
//						intent.putExtra("data", temp);
//						ChooseCityActivity.this.setResult(101, intent);
//						ChooseCityActivity.this.finish();
//					} catch (Exception e) {
//						e.printStackTrace();
//					}

            }
        });
        boolean init = share.getBoolean("dbinit", false);
        if (!init) {
            try {


                share.setBoolean("dbinit", true);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void initCity() {
        MyHttpClient.post(this, Urls.PROVINCE, new HashMap<String, String>(),
                new com.loopj.android.http.AsyncHttpResponseHandler() {

                    @Override
                    public void onSuccess(int statusCode, Header[] headers,
                                          byte[] responseBody) {
                        try {
                            BasicResponse r = new BasicResponse(responseBody).getResult();


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

                });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (cu != null)
            cu.close();
    }

    TextWatcher textwatch = new TextWatcher() {

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count,
                                      int after) {

        }

        @Override
        public void afterTextChanged(Editable s) {
//				String newText=s.toString().trim();
//				if (newText.trim().length() == 0) {
//					panel.setVisibility(View.VISIBLE);
//					listview.setVisibility(View.GONE);
//				} else {
//					panel.setVisibility(View.GONE);
//					listview.setVisibility(View.VISIBLE);
//				}
//				cu = getContentResolver().query(CityTableMetaData.CONTENT_URI,
//						null, null, new String[] { newText }, null);
//				if (null == adapter) {
//					adapter = new SimpleCursorAdapter(ChooseCityActivity.this,
//							R.layout.listview_items_choosecity, cu,
//							new String[] { "name", "province" }, new int[] {
//									R.id.tv_choosecity_city, R.id.tv_choosecity_province });
//					listview.setAdapter(adapter);
//				}
//				adapter.swapCursor(cu);
        }
    };


}
