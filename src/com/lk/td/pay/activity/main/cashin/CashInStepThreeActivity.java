package com.lk.td.pay.activity.main.cashin;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.lk.td.pay.activity.base.BaseFragmentActivity;
import com.lk.td.pay.wedget.signature.SignaturePad;
import com.td.app.pay.hx.R;

public class CashInStepThreeActivity extends BaseFragmentActivity implements OnClickListener{
	
	private SignaturePad mSignaturePad;
    private Button mClearButton;
    private Button mSaveButton;
    private TextView hintText;
    private String usrId, termphyNo, amount, expireData, mCardType, panSerial, cardTrack2, cardTrack3, pwdData, icCardData;
    
	@Override
	protected void onCreate(Bundle arg0) {
		// TODO Auto-generated method stub
		super.onCreate(arg0);
		setContentView(R.layout.cashin_step_three);
		
		init();
		
    }

    private void init() {
    	usrId = getIntent().getStringExtra("usrId");
    	termphyNo = getIntent().getStringExtra("termphyNo");
    	amount = getIntent().getStringExtra("amount");
    	expireData = getIntent().getStringExtra("expireData");
    	mCardType = getIntent().getStringExtra("mCardType");
    	panSerial = getIntent().getStringExtra("panSerial");
    	cardTrack2 = getIntent().getStringExtra("cardTrack2");
    	cardTrack3 = getIntent().getStringExtra("cardTrack3");
    	pwdData = getIntent().getStringExtra("pwdData");
    	icCardData = getIntent().getStringExtra("icCardData");
    	
    	hintText = (TextView) findViewById(R.id.signature_pad_hint);
    	mSignaturePad = (SignaturePad) findViewById(R.id.signature_pad);
        mSignaturePad.setOnSignedListener(new SignaturePad.OnSignedListener() {
            @Override
            public void onSigned() {
                mSaveButton.setEnabled(true);
                mClearButton.setEnabled(true);
                mSaveButton.setBackgroundResource(R.drawable.selector_next_normal);
                mClearButton.setBackgroundResource(R.drawable.selector_next_normal);
                hintText.setText("");
            }

            @Override
            public void onClear() {
                mSaveButton.setEnabled(false);
                mClearButton.setEnabled(false);
                mSaveButton.setBackgroundResource(R.drawable.selector_nextstep);
                mClearButton.setBackgroundResource(R.drawable.selector_nextstep);
                hintText.setText("消费者请在此签名");
            }
        });

        mClearButton = (Button) findViewById(R.id.clear_button);
        mClearButton.setOnClickListener(this);
        mSaveButton = (Button) findViewById(R.id.save_button);
        mSaveButton.setOnClickListener(this);
	}

	public File getAlbumStorageDir(String albumName) {
        // Get the directory for the user's public pictures directory.
        File file = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), albumName);
        if (!file.mkdirs()) {
            Log.e("SignaturePad", "Directory not created");
        }
        return file;
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
            File photo = new File(getAlbumStorageDir("SignaturePad"), String.format("Signature_%d.jpg", System.currentTimeMillis()));
            saveBitmapToJPG(signature, photo);
            Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
            Uri contentUri = Uri.fromFile(photo);
            mediaScanIntent.setData(contentUri);
            CashInStepThreeActivity.this.sendBroadcast(mediaScanIntent);
            result = true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.clear_button:
			mSignaturePad.clear();
			break;
		case R.id.save_button:
			Bitmap signatureBitmap = mSignaturePad.getSignatureBitmap();
            if(addSignatureToGallery(signatureBitmap)) {
                Toast.makeText(CashInStepThreeActivity.this, "Signature saved into the Gallery", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(CashInStepThreeActivity.this, "Unable to store the signature", Toast.LENGTH_SHORT).show();
            }
			break;

		default:
			break;
		}
		
	}
}
