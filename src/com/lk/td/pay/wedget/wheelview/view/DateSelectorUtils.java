package com.lk.td.pay.wedget.wheelview.view;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;

import com.lk.td.pay.request.BasicRequest;
import com.lk.td.pay.utils.DensityUtil;
import com.lk.td.pay.wedget.CustomPopupWindow;
import com.lk.td.pay.wedget.wheelview.NumericWheelAdapter;
import com.lk.td.pay.wedget.wheelview.OnWheelChangedListener;
import com.lk.td.pay.wedget.wheelview.WheelView;
import com.td.app.pay.hx.R;

import java.sql.Date;
import java.util.Calendar;

/**
 * Created by wsq on 2016/5/24.
 */
public class DateSelectorUtils {

    private static int START_YEAR = 1700,END_YEAR=2700;
    private static int START_MONTH = 1, END_MONTH =12;
    public static int SELECTOR_YEAR = 0;
    public static int SELECTOR_MONTH = 0;

    private static final int WHEELVIEW_YEAR_ID = 0x010001;
    private static final int WHEELVIEW_MONTH_ID = 0x010002;
    private static final int WHEELVIEW_DAY_ID = 0x010003;
    private static final int WHEELVIEW_HOUR_ID = 0x010004;
    private static Date date ;
    // 添加大小月月份并将其转换为list,方便之后的判断
//    String[] months_big = { "1", "3", "5", "7", "8", "10", "12" };
//    String[] months_little = { "4", "6", "9", "11" };


    public static View  showDateSelector(Activity context){
        Calendar calendar =Calendar.getInstance();
        date = new Date(System.currentTimeMillis());
        View view  = LayoutInflater.from(context).inflate(R.layout.layout_date_seletor, null);
        //设置年份
        WheelView wy = (WheelView) view.findViewById(R.id.wheel_year);

        wy.setAdapter(new NumericWheelAdapter(START_YEAR, END_YEAR));
        wy.setCyclic(true);
        wy.setVisibleItems(5);
        wy.setLabel("年");
        wy.setId(WHEELVIEW_YEAR_ID);

        SELECTOR_YEAR = calendar.get(Calendar.YEAR);
        wy.setCurrentItem(calendar.get(Calendar.YEAR)-START_YEAR);
        wy.TEXT_SIZE = DensityUtil.sp2px(context, 25);



        //设置月份
        WheelView wm = (WheelView) view.findViewById(R.id.wheel_month);
        wm.setAdapter(new NumericWheelAdapter(START_MONTH, END_MONTH));
        wm.setCyclic(true);
        wm.setVisibleItems(5);
        wm.setId(WHEELVIEW_MONTH_ID);
        wm.setLabel("月");
        SELECTOR_MONTH = calendar.get(Calendar.MONTH)+1;
        wm.setCurrentItem(calendar.get(Calendar.MONTH)+1);
        wm.TEXT_SIZE = DensityUtil.sp2px(context, 25);


        //年份的监听
        wy.addChangingListener(wheel_listener);
        wm.addChangingListener(wheel_listener);

        //月份的监听

        return view;
    }


    static OnWheelChangedListener wheel_listener = new OnWheelChangedListener() {
        @Override
        public void onChanged(WheelView wheel, int oldValue, int newValue) {

            switch (wheel.getId()){

                case WHEELVIEW_YEAR_ID:
                    SELECTOR_YEAR = newValue + START_YEAR;
                    break;
                case WHEELVIEW_MONTH_ID:
                    SELECTOR_MONTH = newValue + 1;
                    break;
            }
        }
    };
}
