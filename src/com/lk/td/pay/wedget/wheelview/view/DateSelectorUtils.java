package com.lk.td.pay.wedget.wheelview.view;

import android.app.Activity;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;

import com.td.app.pay.hx.R;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * Created by wsq on 2016/5/24.
 */
public class DateSelectorUtils {

    private static int START_YEAR = 1700,END_YEAR=2500;
    public static String SELECTOR_YEAR = "0";
    public static String SELECTOR_MONTH = "0";

    private static List<String> yearDate = new ArrayList<String>();
    private static List<String> monthDate = new ArrayList<String>();





    public static View showDateSelector(Activity context){

        addDate();

        Calendar calendar =Calendar.getInstance();
        View view  = LayoutInflater.from(context).inflate(R.layout.layout_date_seletor, null);

        CycleWheelView cycle_year = (CycleWheelView) view.findViewById(R.id.cycle_year);

        CycleWheelView cycle_month = (CycleWheelView) view.findViewById(R.id.cycle_month);

        cycle_year.setLabels(yearDate);
        setParams(cycle_year, "年");
        int year = calendar.get(Calendar.YEAR);
        Log.d("","=====年份显示的位置===="+ (year-START_YEAR)+"");
        cycle_year.setSelection(year-START_YEAR);
        cycle_year.setOnWheelItemSelectedListener(yearListener);


        cycle_month.setLabels(monthDate);
        setParams(cycle_month, "月");
        int month = calendar.get(Calendar.MONTH)+1;
        cycle_month.setSelection(month-1);
        Log.d("","=====月份显示的位置===="+ (month-1)+"");
        cycle_month.setOnWheelItemSelectedListener(monthListener);


        return view;
    }


    private static void addDate(){


        for (int i=0; i< END_YEAR - START_YEAR; i++){

            yearDate.add((START_YEAR+i)+"");
            if (i<12){
                monthDate.add((i+1)+"");
            }
        }
    }


    private static void setParams(CycleWheelView view, String lable){

        view.setCycleEnable(true);

        //设置字体颜色
        view.setLabelColor(Color.parseColor("#5381F7"));
        //设置选中时的字体颜色
        view.setLabelSelectColor(Color.RED);

        view.setSelectorTextSize(25);

        view.setSelectText(lable);

        try {
            view.setWheelSize(3);
        } catch (CycleWheelView.CycleWheelViewException e) {
            e.printStackTrace();
        }

    }


   static CycleWheelView.WheelItemSelectedListener yearListener = new CycleWheelView.WheelItemSelectedListener() {
        @Override
        public void onItemSelected(int position, String label) {
            SELECTOR_YEAR = label;
        }
    };

    static CycleWheelView.WheelItemSelectedListener monthListener = new CycleWheelView.WheelItemSelectedListener() {
        @Override
        public void onItemSelected(int position, String label) {
            SELECTOR_MONTH = label;
        }
    };

}
