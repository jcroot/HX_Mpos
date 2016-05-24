package com.lk.td.pay.wedget;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.android.annotation.event.OnClick;
import com.lk.td.pay.beans.PopupItem;
import com.lk.td.pay.utils.DensityUtil;
import com.td.app.pay.hx.R;

import java.util.List;

/**
 * Created by wsq on 2016/5/9.
 */
public class CustomPopupWindow extends PopupWindow{
    private Activity context;
    private List<PopupItem> mData;
    private ListView listView;
    private View popupView;
    private PopupAdapter mAdapter;

    public CustomPopupWindow(Activity context, View view, String text, OnClickListener onClickListener){
        this.context = context;
        popupView = LayoutInflater.from(context).inflate(R.layout.layout_popup, null);
        listView = (ListView) popupView.findViewById(R.id.popup_list);
        listView.setVisibility(View.GONE);
        LinearLayout layout = (LinearLayout) popupView.findViewById(R.id.wheelview_layout);

        layout.addView(view);

        TextView cancel = (TextView) popupView.findViewById(R.id.popup_cancel);
        cancel.setText(text);
        cancel.setOnClickListener(onClickListener);

        initPopup();
    }
    public CustomPopupWindow(Activity context, List<PopupItem> list, AdapterView.OnItemClickListener itemClick){
        this.context = context;
        this.mData = list;

        popupView = LayoutInflater.from(context).inflate(R.layout.layout_popup, null);
        listView = (ListView) popupView.findViewById(R.id.popup_list);
        LinearLayout layout = (LinearLayout) popupView.findViewById(R.id.wheelview_layout);
        layout.setVisibility(View.GONE);
        popupView.findViewById(R.id.popup_cancel).setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				dismiss();
			}
		});
        mAdapter = new PopupAdapter();
        listView.setAdapter(mAdapter);

        listView.setOnItemClickListener(itemClick);

        initPopup();
    }

    public void initPopup(){

        int w = context.getResources().getDisplayMetrics().widthPixels;
        int h = context.getResources().getDisplayMetrics().heightPixels;
        // 设置按钮监听
        // 设置SelectPicPopupWindow的View
        this.setContentView(popupView);
        // 设置SelectPicPopupWindow弹出窗体的宽
        this.setWidth((int)(w*0.9));
        //
        // 设置SelectPicPopupWindow弹出窗体的高
        int height = 0;
        if (null != mData) {
            if (h / 2 <= DensityUtil.dp2px(context, (mData.size() * 50) + 60)) {
                height = h / 2;
            } else {
                height = DensityUtil.dp2px(context, (mData.size() * 40) + 60);
            }
        }else{
            height = h/2;
        }
        this.setHeight(height);
        // 设置SelectPicPopupWindow弹出窗体可点击
        this.setFocusable(true);
        // 设置SelectPicPopupWindow弹出窗体动画效果
        this.setAnimationStyle(R.style.style_pop);
        // 实例化一个ColorDrawable颜色为半透明
        ColorDrawable dw = new ColorDrawable(0x00000000);
        // 设置SelectPicPopupWindow弹出窗体的背景
        this.setBackgroundDrawable(new BitmapDrawable());
        this.setOutsideTouchable(true);

        this.setOnDismissListener(new PoponDismissListener());

        popupView.setOnTouchListener(new OnTouchListener() {
			
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				// TODO Auto-generated method stub
				
					dismiss();
					
				return false;
			}
		});
    }

    @Override
    public void showAtLocation(View parent, int gravity, int x, int y) {
        super.showAtLocation(parent, gravity, x, y);
        context.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        backgroundAlpha(0.4f);

    }

    @Override
    public void dismiss() {
        super.dismiss();
//        backgroundAlpha(1f);
    }

    /**
     * 添加新笔记时弹出的popWin关闭的事件，主要是为了将背景透明度改回来
     * @author cg
     *
     */
    class PoponDismissListener implements PopupWindow.OnDismissListener{

        @Override
        public void onDismiss() {
            // TODO Auto-generated method stub
            backgroundAlpha(1f);
        }

    }
    private void backgroundAlpha(float bgAlpha) {
        WindowManager.LayoutParams lp = context.getWindow().getAttributes();
            lp.alpha = bgAlpha; //0.0-1.0
            context.getWindow().setAttributes(lp);
    }

    public class PopupAdapter extends BaseAdapter{

        @Override
        public int getCount() {
            return mData.size();
        }

        @Override
        public Object getItem(int position) {
            return mData.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup viewGroup) {

            ViewHodler hodler = null;
            if (convertView == null){
                convertView = LayoutInflater.from(context).inflate(R.layout.layout_popup_item, null);
                hodler = new ViewHodler();
                hodler.popup_Name = (TextView) convertView.findViewById(R.id.popup_name);
                hodler.popup_state = (CheckBox) convertView.findViewById(R.id.popup_state);
                convertView.setTag(hodler);
            }else {
                hodler = (ViewHodler) convertView.getTag();
            }

            hodler.popup_Name.setText(mData.get(position).getName());
            hodler.popup_state.setChecked(mData.get(position).isState());
            return convertView;
        }

        public class ViewHodler{
            TextView popup_Name;
            CheckBox popup_state;
        }
    }
}
