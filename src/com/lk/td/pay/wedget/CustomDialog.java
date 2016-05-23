package com.lk.td.pay.wedget;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.view.Display;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.view.ViewGroup.LayoutParams;

import com.lk.td.pay.beans.CardBean;
import com.td.app.pay.hx.R;

import java.util.List;

/**
 * Created by wsq on 2016/5/6.
 */
public class CustomDialog extends Dialog{

    public CustomDialog(Context context) {
        super(context);
    }

    public CustomDialog(Context context, int theme) {
        super(context, theme);
    }

    public static class Builder {
        private Context context; //上下文对象
        private String title; //对话框标题
        /*按钮坚挺事件*/
        private AdapterView.OnItemClickListener itemClickListener;
        private DialogInterface.OnClickListener okListener, cancelListener;


        private ListView listView;
        public List<CardBean> mData;
        private boolean isShowBtn = true;
        private String ok, cancel, message;


        public Builder(Context context) {
            this.context = context;
        }

        public Builder setTitle(String title) {
            this.title = title;
            return this;
        }
        public Builder setMessage(String message) {
            this.message = message;
            return this;
        }

        public Builder setListView(List<CardBean> list, AdapterView.OnItemClickListener listener) {
            this.mData  = list;
            this.itemClickListener = listener;
            isShowBtn = false;
            return this;
        }

        public Builder setOkBtn(String okStr, DialogInterface.OnClickListener listener){
                this.ok = okStr;
                this.okListener = listener;
            return this;
        }
        public Builder setCancelBtn(String cancelStr, DialogInterface.OnClickListener listener){
            this.cancel = cancelStr;
            this.cancelListener = listener;
            return this;
        }

        public CustomDialog create() {
            LayoutInflater inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            // instantiate the dialog with the custom Theme
            final CustomDialog dialog = new CustomDialog(context, R.style.mystyle);
            View layout = inflater.inflate(R.layout.customdialog, null);
            dialog.addContentView(layout, new LayoutParams(
                    LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
            // set the dialog title
            ((TextView) layout.findViewById(R.id.title)).setText(title);
            ((TextView) layout.findViewById(R.id.title)).getPaint().setFakeBoldText(true);
            LinearLayout dialog_btn_layout = (LinearLayout) layout.findViewById(R.id.dialog_btn_layout);
            TextView dialog_message = (TextView) layout.findViewById(R.id.dialog_message);
            View dialig_view = layout.findViewById(R.id.dialig_view);
            // set the content message
            if (isShowBtn){
                dialog_btn_layout.setVisibility(View.VISIBLE);
                dialog_message.setVisibility(View.VISIBLE);
                Button dialog_ok = (Button)layout.findViewById(R.id.dialog_ok);
                Button dialog_cancel = (Button)layout.findViewById(R.id.dialog_cancel);
                dialog_message.setText(message);
                dialog_ok.setText(ok);
                if (okListener!= null){
                    dialog_ok.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            okListener.onClick(dialog, DialogInterface.BUTTON_POSITIVE);
                        }
                    });
                }
                dialog_cancel.setText(cancel);
                if (cancelListener!=null){
                    dialog_cancel.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            cancelListener.onClick(dialog, DialogInterface.BUTTON_NEGATIVE);
                        }
                    });
                }
                if (okListener ==null && cancelListener!=null){
                    dialog_cancel.setBackgroundResource(R.drawable.shape_dialog_buttom);
                    dialog_ok.setVisibility(View.GONE);
                    dialig_view.setVisibility(View.GONE);
                }else if (okListener !=null && cancelListener==null){
                    dialog_ok.setBackgroundResource(R.drawable.shape_dialog_buttom);
                    dialog_cancel.setVisibility(View.GONE);
                    dialig_view.setVisibility(View.GONE);
                }

            }else{
                ListView listView = (ListView) layout.findViewById(R.id.listView);
                listView.setVisibility(View.VISIBLE);
                listView.setOnItemClickListener(itemClickListener);
                listView.setAdapter(new DialogAdapter());
                dialog_btn_layout.setVisibility(View.GONE);
                dialog_message.setVisibility(View.GONE);
            }
            Window dialogWindow = dialog.getWindow();
            float widthPixels = context.getResources().getDisplayMetrics().widthPixels;
            WindowManager.LayoutParams p = dialogWindow.getAttributes(); // 获取对话框当前的参数值
           // p.height = (int) (d.getHeight() * 0.6); // 高度设置为屏幕的0.6
            p.width = (int) (widthPixels * 0.8); // 宽度设置为屏幕的0.65
            dialogWindow.setAttributes(p);
            dialog.setContentView(layout);

            return dialog;
        }

        public class DialogAdapter extends BaseAdapter{

            @Override
            public int getCount() {
                return mData.size();
            }

            @Override
            public Object getItem(int i) {
                return mData.get(i);
            }

            @Override
            public long getItemId(int i) {
                return i;
            }

            @Override
            public View getView(int position, View convertView, ViewGroup viewGroup) {

                TextView text = new TextView(context);
                AbsListView.LayoutParams params = new AbsListView.LayoutParams(LayoutParams.MATCH_PARENT, dip2px(40, context.getResources().getDisplayMetrics().density));
                text.setLayoutParams(params);
                text.setTextSize(20);
                text.setBackgroundColor(Color.parseColor("#FFFFFF"));
                text.setGravity(Gravity.CENTER);

                text.setText(mData.get(position).getCardName());

                return text;
            }

            public  int dip2px(float dipValue, float scale) {
                return (int) (dipValue * scale + 0.5f);
            }

        }


    }

}
