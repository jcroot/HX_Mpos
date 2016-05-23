package com.lk.td.pay.activity.main.cashin.swing;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.lk.td.pay.activity.base.BaseActivity;
import com.lk.td.pay.activity.main.cashin.ShowMsgActivity;
import com.lk.td.pay.activity.main.cashin.scan.BankScanCamera;
import com.lk.td.pay.beans.BasicResponse;
import com.lk.td.pay.beans.PosData;
import com.lk.td.pay.golbal.Urls;
import com.lk.td.pay.wedget.signature.SignaturePad;
import com.lk.td.pay.tool.Logger;
import com.lk.td.pay.tool.MyHttpClient;
import com.lk.td.pay.utils.FileUtil;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.td.app.pay.hx.R;

import org.apache.http.Header;
import org.json.JSONException;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;

public class SignaturePadActivity extends BaseActivity {
    private SignaturePad mSignaturePad;
    private Button mClearButton, mSaveButton;
    private String signPath;
    private TextView showText;
    private Context mContext;
    public static final  int TYPE_QUICK_PAY = 1;
    public static final String  TYPE= "TYPE";
    private int type=-1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.signature_pad);
        mContext = this;

        type =getIntent().getIntExtra(TYPE, -1);

        showText = (TextView) findViewById(R.id.signature_showText);
        mSignaturePad = (SignaturePad) findViewById(R.id.signature_pad);
        mSignaturePad.setOnSignedListener(new SignaturePad.OnSignedListener() {
            @Override
            public void onSigned() {
                mSaveButton.setEnabled(true);
                mClearButton.setEnabled(true);
                mSaveButton.setBackgroundResource(R.drawable.selector_next_normal);
                mClearButton.setBackgroundResource(R.drawable.selector_next_normal);
                showText.setVisibility(View.GONE);
            }

            @Override
            public void onClear() {
                mSaveButton.setEnabled(false);
                mClearButton.setEnabled(false);
                mSaveButton.setBackgroundResource(R.drawable.selector_nextstep);
                mClearButton.setBackgroundResource(R.drawable.selector_nextstep);
                showText.setVisibility(View.VISIBLE);
            }
        });

        mSaveButton = (Button) findViewById(R.id.save_button);
        mSaveButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                //uploadSignature();
                Bitmap signatureBitmap = mSignaturePad.getSignatureBitmap();
                if (addSignatureToGallery(signatureBitmap)) {
                    upload();
                } else {
                    Toast.makeText(mContext, "保存电子签名失败!", Toast.LENGTH_SHORT).show();
                }
            }
        });
        mClearButton = (Button) findViewById(R.id.clear_button);
        mClearButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                mSignaturePad.clear();
            }
        });
    }

    private void upload() {
        HashMap<String, String> params = new HashMap<>();
        params.put("prdordNo", PosData.getPosData().getPrdordNo());
        //params.put("content", data);
        String url = "";
        if (type==TYPE_QUICK_PAY){

        }else{
            url = Urls.UPLOAD_SIGNTURE;
        }
        MyHttpClient.post(SignaturePadActivity.this,
                url, params, signPath,
                new AsyncHttpResponseHandler() {

                    @Override
                    public void onSuccess(int statusCode,
                                          Header[] headers, byte[] responseBody) {
                        Logger.json("[上传电子签名]", responseBody);
                        try {
                            BasicResponse r = new BasicResponse( responseBody).getResult();
                            if (r.isSuccess()) {
                                if (type==TYPE_QUICK_PAY){

                                }else{
                                    Intent intentTack = new Intent(SignaturePadActivity.this, BankScanCamera.class);
                                    intentTack.putExtra("BankAPP", 10011);
                                    intentTack.putExtra("TYPE","1");//1 支付
                                    startActivity(intentTack);
                                }
                                finish();
                            } else {
                                Intent it = new Intent(mContext,
                                        ShowMsgActivity.class);
                                it.setAction("ACTION_CASH_IN");
                                it.putExtra("code", r.isSuccess());
                                it.putExtra("msg", r.getMsg());
                                startActivity(it);
                                finish();
                            }
                        } catch (JSONException e) {
                            // TODO 自动生成的 catch 块
                            e.printStackTrace();
                        }

                    }

                    @Override
                    public void onFailure(int statusCode,
                                          Header[] headers, byte[] responseBody,
                                          Throwable error) {
                        networkError(statusCode, error);
                    }

                    @Override
                    public void onStart() {
                        // TODO Auto-generated method stub
                        super.onStart();
                        showLoadingDialog("正在上传电子签名...");
                    }

                    public void onFinish() {
                        super.onFinish();
                        dismissLoadingDialog();
                    }

                    ;
                });
    }

    public void saveBitmapToJPG(Bitmap bitmap, File photo) throws IOException {
        Bitmap newBitmap = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(newBitmap);
        canvas.drawColor(Color.WHITE);
        canvas.drawBitmap(bitmap, 0, 0, null);
        OutputStream stream = new FileOutputStream(photo);
        newBitmap.compress(Bitmap.CompressFormat.JPEG, 80, stream);
        stream.close();
    }

    public boolean addSignatureToGallery(Bitmap signature) {
        boolean result = false;
        try {
            File photo = new File(FileUtil.getTdPath(mContext) + String.format("Signature_%d.jpg", System.currentTimeMillis()));
            File temp = new File(FileUtil.getTdPath(mContext));
            if (!temp.exists()) {
                temp.mkdir();
            }
            saveBitmapToJPG(signature, photo);

            Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
            Uri contentUri = Uri.fromFile(photo);
            mediaScanIntent.setData(contentUri);
            this.sendBroadcast(mediaScanIntent);
            signPath = photo.getAbsolutePath();
            result = true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }
}
