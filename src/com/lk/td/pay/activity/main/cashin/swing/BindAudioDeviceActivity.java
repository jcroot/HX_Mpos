package com.lk.td.pay.activity.main.cashin.swing;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.bbpos.emvswipe.CAPK;
import com.bbpos.emvswipe.EmvSwipeController;
import com.bbpos.emvswipe.EmvSwipeController.AutoConfigError;
import com.bbpos.emvswipe.EmvSwipeController.BatteryStatus;
import com.bbpos.emvswipe.EmvSwipeController.CheckCardMode;
import com.bbpos.emvswipe.EmvSwipeController.CheckCardResult;
import com.bbpos.emvswipe.EmvSwipeController.DisplayText;
import com.bbpos.emvswipe.EmvSwipeController.EmvOption;
import com.bbpos.emvswipe.EmvSwipeController.EmvSwipeControllerListener;
import com.bbpos.emvswipe.EmvSwipeController.Error;
import com.bbpos.emvswipe.EmvSwipeController.NfcDataExchangeStatus;
import com.bbpos.emvswipe.EmvSwipeController.ReferralResult;
import com.bbpos.emvswipe.EmvSwipeController.StartEmvResult;
import com.bbpos.emvswipe.EmvSwipeController.TerminalSettingStatus;
import com.bbpos.emvswipe.EmvSwipeController.TransactionResult;
import com.lk.td.pay.activity.base.BaseActivity;
import com.lk.td.pay.activity.main.cashin.swing.audio.TLV;
import com.lk.td.pay.activity.main.cashin.swing.audio.TLVParser;
import com.lk.td.pay.beans.BasicResponse;
import com.lk.td.pay.golbal.MApplication;
import com.lk.td.pay.golbal.Urls;
import com.lk.td.pay.tool.MyHttpClient;
import com.lk.td.pay.tool.T;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.td.app.pay.hx.R;

import org.apache.http.Header;
import org.json.JSONException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;

public class BindAudioDeviceActivity extends BaseActivity implements
        EmvSwipeControllerListener, OnClickListener {

    private Button checkCardButton;
    private Button getBtn;
    //private EditText amountEditText;
    private EditText statusEditText;
    private ListView appListView;
    private Dialog dialog;
    private ProgressDialog progressDialog;
    private boolean isAskingForAmount = false;
    private String amount = "";
    private String cashbackAmount = "";
    private boolean isPinCanceled = false;
    private boolean isSwitchingActivity = false;
    private EmvSwipeController emvSwipeController;
    private String termNo;
    private boolean isInsert = false;//插入装置的标志位

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        setContentView(R.layout.bind_audiodevice_layout);
        setSwingCardInit();
        initUI();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (isSwitchingActivity) {
            isSwitchingActivity = false;
        } else {
            emvSwipeController.stopAudio();
            emvSwipeController.resetEmvSwipeController();
            emvSwipeController = null;
        }
    }

//	public void promptForAmount() {
//		dismissDialog();
//		dialog = new Dialog(BindAudioDeviceActivity.this);
//		dialog.setContentView(R.layout.amount_dialog);
//		dialog.setTitle(getString(R.string.set_amount));
//
//		String[] transactionTypes = new String[] { "GOODS", "SERVICES",
//				"CASHBACK", "INQUIRY", "TRANSFER", "PAYMENT", "REFUND" };
//		((Spinner) dialog.findViewById(R.id.transactionTypeSpinner))
//				.setAdapter(new ArrayAdapter<String>(
//						BindAudioDeviceActivity.this,
//						android.R.layout.simple_spinner_item, transactionTypes));
//
//		dialog.findViewById(R.id.setButton).setOnClickListener(
//				new View.OnClickListener() {
//
//					@Override
//					public void onClick(View v) {
//						String amount = ((EditText) (dialog
//								.findViewById(R.id.amountEditText))).getText()
//								.toString();
//						String cashbackAmount = ((EditText) (dialog
//								.findViewById(R.id.cashbackAmountEditText)))
//								.getText().toString();
//						String transactionTypeString = (String) ((Spinner) dialog
//								.findViewById(R.id.transactionTypeSpinner))
//								.getSelectedItem();
//
//						TransactionType transactionType = TransactionType.GOODS;
//						if (transactionTypeString.equals("GOODS")) {
//							transactionType = TransactionType.GOODS;
//						} else if (transactionTypeString.equals("SERVICES")) {
//							transactionType = TransactionType.SERVICES;
//						} else if (transactionTypeString.equals("CASHBACK")) {
//							transactionType = TransactionType.CASHBACK;
//						} else if (transactionTypeString.equals("INQUIRY")) {
//							transactionType = TransactionType.INQUIRY;
//						} else if (transactionTypeString.equals("TRANSFER")) {
//							transactionType = TransactionType.TRANSFER;
//						} else if (transactionTypeString.equals("PAYMENT")) {
//							transactionType = TransactionType.PAYMENT;
//						} else if (transactionTypeString.equals("REFUND")) {
//							transactionType = TransactionType.REFUND;
//						}
//
//						if (emvSwipeController.setAmount(amount,
//								cashbackAmount, "840", transactionType)) {
//							amountEditText.setText("$" + amount);
//							BindAudioDeviceActivity.this.amount = amount;
//							BindAudioDeviceActivity.this.cashbackAmount = cashbackAmount;
//							dismissDialog();
//							isAskingForAmount = false;
//						} else {
//							promptForAmount();
//						}
//					}
//
//				});
//
//		dialog.findViewById(R.id.cancelButton).setOnClickListener(
//				new View.OnClickListener() {
//
//					@Override
//					public void onClick(View v) {
//						if (isAskingForAmount) {
//							emvSwipeController.cancelSetAmount();
//						}
//						isAskingForAmount = false;
//						dialog.dismiss();
//					}
//
//				});
//
//		dialog.show();
//	}

    public void dismissDialog() {
        if (dialog != null) {
            dialog.dismiss();
            dialog = null;
        }
    }

    @Override
    public void onReturnCheckCardResult(CheckCardResult checkCardResult,
                                        Hashtable<String, String> decodeData) {
        dismissDialog();
        if (checkCardResult == CheckCardResult.NONE) {
            statusEditText.setText(getString(R.string.no_card_detected));
        } else if (checkCardResult == CheckCardResult.ICC) {
            statusEditText.setText(getString(R.string.icc_card_inserted));
            emvSwipeController.startEmv(EmvOption.START);
        } else if (checkCardResult == CheckCardResult.NOT_ICC) {
            statusEditText.setText(getString(R.string.card_inserted));
        } else if (checkCardResult == CheckCardResult.BAD_SWIPE) {
            statusEditText.setText(getString(R.string.bad_swipe));
        } else if (checkCardResult == CheckCardResult.MCR) {
            String formatID = decodeData.get("formatID");
            String maskedPAN = decodeData.get("maskedPAN");
            String PAN = decodeData.get("PAN");
            String expiryDate = decodeData.get("expiryDate");
            String cardHolderName = decodeData.get("cardholderName");
            String ksn = decodeData.get("ksn");
            String serviceCode = decodeData.get("serviceCode");
            String track1Length = decodeData.get("track1Length");
            String track2Length = decodeData.get("track2Length");
            String track3Length = decodeData.get("track3Length");
            String encTracks = decodeData.get("encTracks");
            String encTrack1 = decodeData.get("encTrack1");
            String encTrack2 = decodeData.get("encTrack2");
            String encTrack3 = decodeData.get("encTrack3");
            String partialTrack = decodeData.get("partialTrack");
            String trackEncoding = decodeData.get("trackEncoding");
            String finalMessage = decodeData.get("finalMessage");
            String randomNumber = decodeData.get("randomNumber");
            String encWorkingKey = decodeData.get("encWorkingKey");

            String content = getString(R.string.card_swiped);

            content += getString(R.string.format_id) + " " + formatID + "\n";
            content += getString(R.string.masked_pan) + " " + maskedPAN + "\n";
            content += getString(R.string.pan) + " " + PAN + "\n";
            content += getString(R.string.expiry_date) + " " + expiryDate
                    + "\n";
            content += getString(R.string.cardholder_name) + " "
                    + cardHolderName + "\n";
            content += getString(R.string.ksn) + " " + ksn + "\n";
            content += getString(R.string.service_code) + " " + serviceCode
                    + "\n";
            content += getString(R.string.track_1_length) + " " + track1Length
                    + "\n";
            content += getString(R.string.track_2_length) + " " + track2Length
                    + "\n";
            content += getString(R.string.track_3_length) + " " + track3Length
                    + "\n";
            content += getString(R.string.encrypted_tracks) + " " + encTracks
                    + "\n";
            content += getString(R.string.encrypted_track_1) + " " + encTrack1
                    + "\n";
            content += getString(R.string.encrypted_track_2) + " " + encTrack2
                    + "\n";
            content += getString(R.string.encrypted_track_3) + " " + encTrack3
                    + "\n";
            content += getString(R.string.partial_track) + " " + partialTrack
                    + "\n";
            content += getString(R.string.track_encoding) + " " + trackEncoding
                    + "\n";
            content += getString(R.string.final_message) + " " + finalMessage
                    + "\n";
            content += getString(R.string.random_number) + " " + randomNumber
                    + "\n";
            content += getString(R.string.encrypted_working_key) + " "
                    + encWorkingKey + "\n";

            statusEditText.setText(content);
        } else if (checkCardResult == CheckCardResult.NO_RESPONSE) {
            statusEditText.setText(getString(R.string.card_no_response));
        } else if (checkCardResult == CheckCardResult.TRACK2_ONLY) {
            String formatID = decodeData.get("formatID");
            String maskedPAN = decodeData.get("maskedPAN");
            String PAN = decodeData.get("PAN");
            String expiryDate = decodeData.get("expiryDate");
            String cardHolderName = decodeData.get("cardholderName");
            String ksn = decodeData.get("ksn");
            String serviceCode = decodeData.get("serviceCode");
            String track1Length = decodeData.get("track1Length");
            String track2Length = decodeData.get("track2Length");
            String track3Length = decodeData.get("track3Length");
            String encTracks = decodeData.get("encTracks");
            String encTrack1 = decodeData.get("encTrack1");
            String encTrack2 = decodeData.get("encTrack2");
            String encTrack3 = decodeData.get("encTrack3");
            String partialTrack = decodeData.get("partialTrack");
            String trackEncoding = decodeData.get("trackEncoding");
            String finalMessage = decodeData.get("finalMessage");
            String randomNumber = decodeData.get("randomNumber");
            String encWorkingKey = decodeData.get("encWorkingKey");

            String content = getString(R.string.card_swiped_track2_only);
            content += getString(R.string.format_id) + " " + formatID + "\n";
            content += getString(R.string.masked_pan) + " " + maskedPAN + "\n";
            content += getString(R.string.pan) + " " + PAN + "\n";
            content += getString(R.string.expiry_date) + " " + expiryDate
                    + "\n";
            content += getString(R.string.cardholder_name) + " "
                    + cardHolderName + "\n";
            content += getString(R.string.ksn) + " " + ksn + "\n";
            content += getString(R.string.service_code) + " " + serviceCode
                    + "\n";
            content += getString(R.string.track_1_length) + " " + track1Length
                    + "\n";
            content += getString(R.string.track_2_length) + " " + track2Length
                    + "\n";
            content += getString(R.string.track_3_length) + " " + track3Length
                    + "\n";
            content += getString(R.string.encrypted_tracks) + " " + encTracks
                    + "\n";
            content += getString(R.string.encrypted_track_1) + " " + encTrack1
                    + "\n";
            content += getString(R.string.encrypted_track_2) + " " + encTrack2
                    + "\n";
            content += getString(R.string.encrypted_track_3) + " " + encTrack3
                    + "\n";
            content += getString(R.string.partial_track) + " " + partialTrack
                    + "\n";
            content += getString(R.string.track_encoding) + " " + trackEncoding
                    + "\n";
            content += getString(R.string.final_message) + " " + finalMessage
                    + "\n";
            content += getString(R.string.random_number) + " " + randomNumber
                    + "\n";
            content += getString(R.string.encrypted_working_key) + " "
                    + encWorkingKey + "\n";

            statusEditText.setText(content);

        } else if (checkCardResult == CheckCardResult.NFC_TRACK2) {
            String formatID = decodeData.get("formatID");
            String maskedPAN = decodeData.get("maskedPAN");
            String PAN = decodeData.get("PAN");
            String expiryDate = decodeData.get("expiryDate");
            String cardHolderName = decodeData.get("cardholderName");
            String ksn = decodeData.get("ksn");
            String serviceCode = decodeData.get("serviceCode");
            String track1Length = decodeData.get("track1Length");
            String track2Length = decodeData.get("track2Length");
            String track3Length = decodeData.get("track3Length");
            String encTracks = decodeData.get("encTracks");
            String encTrack1 = decodeData.get("encTrack1");
            String encTrack2 = decodeData.get("encTrack2");
            String encTrack3 = decodeData.get("encTrack3");
            String partialTrack = decodeData.get("partialTrack");
            String trackEncoding = decodeData.get("trackEncoding");
            String finalMessage = decodeData.get("finalMessage");
            String randomNumber = decodeData.get("randomNumber");
            String encWorkingKey = decodeData.get("encWorkingKey");

            String content = getString(R.string.nfc_track2);
            content += getString(R.string.format_id) + " " + formatID + "\n";
            content += getString(R.string.masked_pan) + " " + maskedPAN + "\n";
            content += getString(R.string.pan) + " " + PAN + "\n";
            content += getString(R.string.expiry_date) + " " + expiryDate
                    + "\n";
            content += getString(R.string.cardholder_name) + " "
                    + cardHolderName + "\n";
            content += getString(R.string.ksn) + " " + ksn + "\n";
            content += getString(R.string.service_code) + " " + serviceCode
                    + "\n";
            content += getString(R.string.track_1_length) + " " + track1Length
                    + "\n";
            content += getString(R.string.track_2_length) + " " + track2Length
                    + "\n";
            content += getString(R.string.track_3_length) + " " + track3Length
                    + "\n";
            content += getString(R.string.encrypted_tracks) + " " + encTracks
                    + "\n";
            content += getString(R.string.encrypted_track_1) + " " + encTrack1
                    + "\n";
            content += getString(R.string.encrypted_track_2) + " " + encTrack2
                    + "\n";
            content += getString(R.string.encrypted_track_3) + " " + encTrack3
                    + "\n";
            content += getString(R.string.partial_track) + " " + partialTrack
                    + "\n";
            content += getString(R.string.track_encoding) + " " + trackEncoding
                    + "\n";
            content += getString(R.string.final_message) + " " + finalMessage
                    + "\n";
            content += getString(R.string.random_number) + " " + randomNumber
                    + "\n";
            content += getString(R.string.encrypted_working_key) + " "
                    + encWorkingKey + "\n";
            statusEditText.setText(content);

        } else if (checkCardResult == CheckCardResult.USE_ICC_CARD) {
            statusEditText.setText(getString(R.string.use_icc_card));
        }
    }

    @Override
    public void onReturnCancelCheckCardResult(boolean isSuccess) {
        if (isSuccess) {
            statusEditText.setText(R.string.cancel_check_card_success);
        } else {
            statusEditText.setText(R.string.cancel_check_card_fail);
        }
    }

    public void onReturnEncryptDataResult(String encryptedData, String ksn) {
    }

    @Override
    public void onReturnStartEmvResult(StartEmvResult startEmvResult, String ksn) {
        if (startEmvResult == StartEmvResult.SUCCESS) {
            statusEditText.setText(getString(R.string.start_emv_success));
        } else {
            statusEditText.setText(getString(R.string.start_emv_fail));
        }
    }

    @Override
    public void onReturnDeviceInfo(Hashtable<String, String> deviceInfoData) {
        String isSupportedTrack1 = deviceInfoData.get("isSupportedTrack1") == null ? ""
                : deviceInfoData.get("isSupportedTrack1");
        String isSupportedTrack2 = deviceInfoData.get("isSupportedTrack2") == null ? ""
                : deviceInfoData.get("isSupportedTrack2");
        String isSupportedTrack3 = deviceInfoData.get("isSupportedTrack3") == null ? ""
                : deviceInfoData.get("isSupportedTrack3");
        String bootloaderVersion = deviceInfoData.get("bootloaderVersion") == null ? ""
                : deviceInfoData.get("bootloaderVersion");
        String firmwareVersion = deviceInfoData.get("firmwareVersion") == null ? ""
                : deviceInfoData.get("firmwareVersion");
        String isUsbConnected = deviceInfoData.get("isUsbConnected") == null ? ""
                : deviceInfoData.get("isUsbConnected");
        String isCharging = deviceInfoData.get("isCharging") == null ? ""
                : deviceInfoData.get("isCharging");
        String batteryLevel = deviceInfoData.get("batteryLevel") == null ? ""
                : deviceInfoData.get("batteryLevel");
        String batteryPercentage = deviceInfoData.get("batteryPercentage") == null ? ""
                : deviceInfoData.get("batteryPercentage");
        String hardwareVersion = deviceInfoData.get("hardwareVersion") == null ? ""
                : deviceInfoData.get("hardwareVersion");
        String pinKsn = deviceInfoData.get("pinKsn") == null ? ""
                : deviceInfoData.get("pinKsn");
        String trackKsn = deviceInfoData.get("trackKsn") == null ? ""
                : deviceInfoData.get("trackKsn");
        String emvKsn = deviceInfoData.get("emvKsn") == null ? ""
                : deviceInfoData.get("emvKsn");
        String uid = deviceInfoData.get("uid") == null ? "" : deviceInfoData
                .get("uid");
        String csn = deviceInfoData.get("csn") == null ? "" : deviceInfoData
                .get("csn");
        String formatID = deviceInfoData.get("formatID") == null ? ""
                : deviceInfoData.get("formatID");

        String content = "";
        content += getString(R.string.bootloader_version) + bootloaderVersion
                + "\n";
        content += getString(R.string.firmware_version) + firmwareVersion
                + "\n";
        content += getString(R.string.usb) + isUsbConnected + "\n";
        content += getString(R.string.charge) + isCharging + "\n";
        content += getString(R.string.battery_level) + batteryLevel + "\n";
        content += getString(R.string.battery_percentage) + batteryPercentage
                + "\n";
        content += getString(R.string.hardware_version) + hardwareVersion
                + "\n";
        content += getString(R.string.track_1_supported) + isSupportedTrack1
                + "\n";
        content += getString(R.string.track_2_supported) + isSupportedTrack2
                + "\n";
        content += getString(R.string.track_3_supported) + isSupportedTrack3
                + "\n";
        content += getString(R.string.pin_ksn) + pinKsn + "\n";
        content += getString(R.string.track_ksn) + trackKsn + "\n";
        content += getString(R.string.emv_ksn) + emvKsn + "\n";
        content += getString(R.string.uid) + uid + "\n";
        content += getString(R.string.csn) + csn + "\n";
        content += getString(R.string.format_id) + formatID + "\n";

        termNo = emvKsn;
        statusEditText.setText(content);
    }

    @Override
    public void onReturnTransactionResult(TransactionResult transactionResult) {
        dismissDialog();
        // statusEditText.setText("");
        dialog = new Dialog(BindAudioDeviceActivity.this);
        dialog.setContentView(R.layout.alert_dialog);
        dialog.setTitle(R.string.transaction_result);
        TextView messageTextView = (TextView) dialog
                .findViewById(R.id.messageTextView);

        if (transactionResult == TransactionResult.APPROVED) {
            String message = getString(R.string.transaction_approved) + "\n"
                    + getString(R.string.amount) + ": $" + amount + "\n";
            if (!cashbackAmount.equals("")) {
                message += getString(R.string.cashback_amount) + ": $"
                        + cashbackAmount;
            }
            messageTextView.setText(message);
        } else if (transactionResult == TransactionResult.TERMINATED) {
            messageTextView.setText(getString(R.string.transaction_terminated));
        } else if (transactionResult == TransactionResult.DECLINED) {
            messageTextView.setText(getString(R.string.transaction_declined));
        } else if (transactionResult == TransactionResult.CANCEL) {
            messageTextView.setText(getString(R.string.transaction_cancel));
        } else if (transactionResult == TransactionResult.CAPK_FAIL) {
            messageTextView.setText(getString(R.string.transaction_capk_fail));
        } else if (transactionResult == TransactionResult.NOT_ICC) {
            messageTextView.setText(getString(R.string.transaction_not_icc));
        } else if (transactionResult == TransactionResult.CARD_BLOCKED) {
            messageTextView
                    .setText(getString(R.string.transaction_card_blocked));
        } else if (transactionResult == TransactionResult.DEVICE_ERROR) {
            messageTextView
                    .setText(getString(R.string.transaction_device_error));
        } else if (transactionResult == TransactionResult.CARD_NOT_SUPPORTED) {
            messageTextView.setText(getString(R.string.card_not_supported));
        } else if (transactionResult == TransactionResult.MISSING_MANDATORY_DATA) {
            messageTextView.setText(getString(R.string.missing_mandatory_data));
        } else if (transactionResult == TransactionResult.NO_EMV_APPS) {
            messageTextView.setText(getString(R.string.no_emv_apps));
        } else if (transactionResult == TransactionResult.INVALID_ICC_DATA) {
            messageTextView.setText(getString(R.string.invalid_icc_data));
        } else if (transactionResult == TransactionResult.CONDITION_NOT_SATISFIED) {
            messageTextView
                    .setText(getString(R.string.condition_not_satisfied));
        } else if (transactionResult == TransactionResult.APPLICATION_BLOCKED) {
            messageTextView.setText(getString(R.string.application_blocked));
        } else if (transactionResult == TransactionResult.ICC_CARD_REMOVED) {
            messageTextView.setText(getString(R.string.icc_card_removed));
        }

        dialog.findViewById(R.id.confirmButton).setOnClickListener(
                new OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        dismissDialog();
                    }
                });

        dialog.show();

        amount = "";
        cashbackAmount = "";
        //amountEditText.setText("");
    }

    @Override
    public void onReturnTransactionResult(TransactionResult transactionResult,
                                          Hashtable<String, String> data) {
        dismissDialog();
        dialog = new Dialog(BindAudioDeviceActivity.this);
        dialog.setContentView(R.layout.alert_dialog);
        dialog.setTitle(R.string.transaction_result);
        TextView messageTextView = (TextView) dialog
                .findViewById(R.id.messageTextView);

        String message = "";
        if (transactionResult == TransactionResult.APPROVED) {
            message = getString(R.string.transaction_approved) + "\n"
                    + getString(R.string.amount) + ": $" + amount + "\n";
            if (!cashbackAmount.equals("")) {
                message += getString(R.string.cashback_amount) + ": $"
                        + cashbackAmount;
            }
        } else if (transactionResult == TransactionResult.TERMINATED) {
            message = getString(R.string.transaction_terminated);
        } else if (transactionResult == TransactionResult.DECLINED) {
            message = getString(R.string.transaction_declined);
        } else if (transactionResult == TransactionResult.CANCEL) {
            message = getString(R.string.transaction_cancel);
        } else if (transactionResult == TransactionResult.CAPK_FAIL) {
            message = getString(R.string.transaction_capk_fail);
        } else if (transactionResult == TransactionResult.NOT_ICC) {
            message = getString(R.string.transaction_not_icc);
        } else if (transactionResult == TransactionResult.CARD_BLOCKED) {
            message = getString(R.string.transaction_card_blocked);
        } else if (transactionResult == TransactionResult.DEVICE_ERROR) {
            message = getString(R.string.transaction_device_error);
        } else if (transactionResult == TransactionResult.CARD_NOT_SUPPORTED) {
            message = getString(R.string.card_not_supported);
        } else if (transactionResult == TransactionResult.MISSING_MANDATORY_DATA) {
            message = getString(R.string.missing_mandatory_data);
        } else if (transactionResult == TransactionResult.NO_EMV_APPS) {
            message = getString(R.string.no_emv_apps);
        } else if (transactionResult == TransactionResult.INVALID_ICC_DATA) {
            message = getString(R.string.invalid_icc_data);
        } else if (transactionResult == TransactionResult.CONDITION_NOT_SATISFIED) {
            message = getString(R.string.condition_not_satisfied);
        } else if (transactionResult == TransactionResult.APPLICATION_BLOCKED) {
            message = getString(R.string.application_blocked);
        } else if (transactionResult == TransactionResult.ICC_CARD_REMOVED) {
            message = getString(R.string.icc_card_removed);
        }

        if (data.get("receiptData") != null) {
            message += "\n" + getString(R.string.receipt_data) + ""
                    + data.get("receiptData");
        }

        messageTextView.setText(message);

        dialog.findViewById(R.id.confirmButton).setOnClickListener(
                new OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        dismissDialog();
                    }
                });

        dialog.show();

        amount = "";
        cashbackAmount = "";
        //amountEditText.setText("");
    }

    @Override
    public void onReturnBatchData(String tlv) {
        dismissDialog();
        String content = getString(R.string.batch_data);
        content += tlv;
        statusEditText.setText(content);
    }

    @Override
    public void onReturnTransactionLog(String tlv) {
        dismissDialog();
        String content = getString(R.string.transaction_log);
        content += tlv;
        statusEditText.setText(content);
    }

    @Override
    public void onReturnReversalData(String tlv) {
        dismissDialog();
        String content = getString(R.string.reversal_data);
        content += tlv;
        statusEditText.setText(content);
    }

    @Override
    public void onReturnPowerOnIccResult(boolean isSuccess, String ksn,
                                         String atr, int atrLength) {
    }

    @Override
    public void onReturnPowerOffIccResult(boolean isSuccess) {
    }

    @Override
    public void onReturnApduResult(boolean isSuccess, String apdu,
                                   int apduLength) {
    }

    @Override
    public void onReturnApduResultWithPkcs7Padding(boolean isSuccess,
                                                   String apdu) {
    }

    @Override
    public void onReturnEmvCardDataResult(boolean isSuccess, String tlv) {
    }

    @Override
    public void onReturnEmvCardNumber(String cardNumber) {
        statusEditText.setText(getString(R.string.card_number) + cardNumber);
    }

    @Override
    public void onReturnNfcDataResult(
            NfcDataExchangeStatus nfcDataExchangeStatus, String response,
            int responseLength) {
    }

    @Override
    public void onReturnPowerOffNfcResult(boolean isSuccess) {
    }

    @Override
    public void onReturnPowerOnNfcResult(boolean isSuccess, String data,
                                         int dataLength) {
    }

    @Override
    public void onReturnKsn(Hashtable<String, String> ksnTable) {
        String pinKsn = ksnTable.get("pinKsn") == null ? "" : ksnTable
                .get("pinKsn");
        String trackKsn = ksnTable.get("trackKsn") == null ? "" : ksnTable
                .get("trackKsn");
        String emvKsn = ksnTable.get("emvKsn") == null ? "" : ksnTable
                .get("emvKsn");
        String uid = ksnTable.get("uid") == null ? "" : ksnTable.get("uid");
        String csn = ksnTable.get("csn") == null ? "" : ksnTable.get("csn");

        String content = "";
        content += getString(R.string.pin_ksn) + pinKsn + "\n";
        content += getString(R.string.track_ksn) + trackKsn + "\n";
        content += getString(R.string.emv_ksn) + emvKsn + "\n";
        content += getString(R.string.uid) + uid + "\n";
        content += getString(R.string.csn) + csn + "\n";
        termNo = emvKsn;
        System.out.println("==================================>>>" + termNo);
        statusEditText.setText(content);
        gotoBindAndSign();
    }

    @Override
    public void onReturnUpdateTerminalSettingResult(
            TerminalSettingStatus terminalSettingStatus) {
    }

    @Override
    public void onReturnReadTerminalSettingResult(
            TerminalSettingStatus terminalSettingStatus, String value) {
    }

    @Override
    public void onRequestSelectApplication(ArrayList<String> appList) {
        dismissDialog();

        dialog = new Dialog(BindAudioDeviceActivity.this);
        dialog.setContentView(R.layout.application_dialog);
        dialog.setTitle(R.string.please_select_app);

        String[] appNameList = new String[appList.size()];
        for (int i = 0; i < appNameList.length; ++i) {
            appNameList[i] = appList.get(i);
        }

        appListView = (ListView) dialog.findViewById(R.id.appList);
        appListView.setAdapter(new ArrayAdapter<String>(
                BindAudioDeviceActivity.this,
                android.R.layout.simple_list_item_1, appNameList));
        appListView.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                emvSwipeController.selectApplication(position);
                dismissDialog();
            }

        });

        dialog.findViewById(R.id.cancelButton).setOnClickListener(
                new OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        emvSwipeController.cancelSelectApplication();
                        dismissDialog();
                    }
                });
        dialog.show();
    }

    @Override
    public void onRequestSetAmount() {
        isAskingForAmount = true;
        //promptForAmount();
    }

    @Override
    public void onRequestPinEntry() {
        dismissDialog();

        dialog = new Dialog(BindAudioDeviceActivity.this);
        dialog.setContentView(R.layout.pin_dialog);
        dialog.setTitle(getString(R.string.enter_pin));

        dialog.findViewById(R.id.confirmButton).setOnClickListener(
                new OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        String pin = ((EditText) dialog
                                .findViewById(R.id.pinEditText)).getText()
                                .toString();
                        emvSwipeController.sendPinEntryResult(pin);
                        dismissDialog();
                    }
                });

        dialog.findViewById(R.id.bypassButton).setOnClickListener(
                new OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        emvSwipeController.bypassPinEntry();
                        dismissDialog();
                    }
                });

        dialog.findViewById(R.id.cancelButton).setOnClickListener(
                new OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        isPinCanceled = true;
                        emvSwipeController.cancelPinEntry();
                        dismissDialog();
                    }
                });

        dialog.show();
    }

    @Override
    public void onRequestVerifyID(String tlv) {
        dismissDialog();

        dialog = new Dialog(BindAudioDeviceActivity.this);
        dialog.setContentView(R.layout.verify_id_dialog);
        dialog.setTitle(R.string.verify_id);

        String content = "";
        try {
            List<TLV> tlvList = TLVParser.parse(tlv);
            TLV cardholderCertificateTLV = TLVParser.searchTLV(tlvList, "9F61");
            TLV certificateTypeTLV = TLVParser.searchTLV(tlvList, "9F62");

            if (cardholderCertificateTLV != null) {
                content += "\n" + getString(R.string.cardholder_certificate)
                        + " " + new String(cardholderCertificateTLV.value);
            }

            if (certificateTypeTLV != null) {
                content += "\n" + getString(R.string.certificate_type) + " "
                        + certificateTypeTLV.value;
            }
        } catch (Exception e) {
        }

        ((TextView) dialog.findViewById(R.id.messageTextView)).setText(content);

        dialog.findViewById(R.id.successButton).setOnClickListener(
                new OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        emvSwipeController.sendVerifyIDResult(true);
                        dismissDialog();
                    }
                });

        dialog.findViewById(R.id.failButton).setOnClickListener(
                new OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        emvSwipeController.sendVerifyIDResult(true);
                        dismissDialog();
                    }
                });

        dialog.show();
    }

    @Override
    public void onRequestCheckServerConnectivity() {
        dismissDialog();
        dialog = new Dialog(BindAudioDeviceActivity.this);
        dialog.setContentView(R.layout.alert_dialog);
        dialog.setTitle(R.string.request_check_online_connectivity);

        ((TextView) dialog.findViewById(R.id.messageTextView))
                .setText(R.string.replied_connected);

        dialog.findViewById(R.id.confirmButton).setOnClickListener(
                new OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        emvSwipeController.sendServerConnectivity(true);
                        dismissDialog();
                    }
                });

        dialog.show();
    }

    @Override
    public void onRequestOnlineProcess(String tlv) {
        dismissDialog();

        if (!isPinCanceled) {
            dialog = new Dialog(BindAudioDeviceActivity.this);
            dialog.setContentView(R.layout.online_response_dialog);
            dialog.setTitle(R.string.select_online_response);

            String[] onlineResponses = new String[]{"8A023030",
                    "8A023030910A8600965D36A1716E3030",
                    "8A023030910A6F2BA37E41110DC93030"};
            ((Spinner) dialog.findViewById(R.id.onlineResponseSpinner))
                    .setAdapter(new ArrayAdapter<String>(
                            BindAudioDeviceActivity.this,
                            android.R.layout.simple_spinner_item,
                            onlineResponses));

            dialog.findViewById(R.id.confirmButton).setOnClickListener(
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            String response = (String) ((Spinner) dialog
                                    .findViewById(R.id.onlineResponseSpinner))
                                    .getSelectedItem();
                            emvSwipeController
                                    .sendOnlineProcessResult(response);
                            statusEditText.setText(getString(R.string.replied)
                                    + " " + response);

                            dismissDialog();
                        }
                    });

            dialog.show();
            statusEditText.setText(getString(R.string.request_online_process)
                    + ": " + tlv);
        } else {
            dialog = new Dialog(BindAudioDeviceActivity.this);
            dialog.setContentView(R.layout.alert_dialog);
            dialog.setTitle(R.string.request_online_process);

            ((TextView) dialog.findViewById(R.id.messageTextView))
                    .setText(R.string.replied_failed);

            dialog.findViewById(R.id.confirmButton).setOnClickListener(
                    new OnClickListener() {

                        @Override
                        public void onClick(View v) {
                            emvSwipeController.sendOnlineProcessResult(null);
                        }
                    });
            dialog.show();
        }
    }

    @Override
    public void onRequestTerminalTime() {
        dismissDialog();
        String terminalTime = new SimpleDateFormat("yyMMddHHmmss")
                .format(Calendar.getInstance().getTime());
        emvSwipeController.sendTerminalTime(terminalTime);
        statusEditText.setText(getString(R.string.request_terminal_time) + " "
                + terminalTime);
    }

    @Override
    public void onRequestDisplayText(DisplayText displayText) {
        dismissDialog();

        String msg = "";
        if (displayText == DisplayText.AMOUNT) {
            msg = getString(R.string.amount);
        } else if (displayText == DisplayText.AMOUNT_OK_OR_NOT) {
            msg = getString(R.string.amount_ok);
        } else if (displayText == DisplayText.APPROVED) {
            msg = getString(R.string.approved);
        } else if (displayText == DisplayText.CALL_YOUR_BANK) {
            msg = getString(R.string.call_your_bank);
        } else if (displayText == DisplayText.CANCEL_OR_ENTER) {
            msg = getString(R.string.cancel_or_enter);
        } else if (displayText == DisplayText.CARD_ERROR) {
            msg = getString(R.string.card_error);
        } else if (displayText == DisplayText.DECLINED) {
            msg = getString(R.string.decline);
        } else if (displayText == DisplayText.ENTER_AMOUNT) {
            msg = getString(R.string.enter_amount);
        } else if (displayText == DisplayText.ENTER_PIN) {
            msg = getString(R.string.enter_pin);
        } else if (displayText == DisplayText.INCORRECT_PIN) {
            msg = getString(R.string.incorrect_pin);
        } else if (displayText == DisplayText.INSERT_CARD) {
            msg = getString(R.string.insert_card);
        } else if (displayText == DisplayText.NOT_ACCEPTED) {
            msg = getString(R.string.not_accepted);
        } else if (displayText == DisplayText.PIN_OK) {
            msg = getString(R.string.pin_ok);
        } else if (displayText == DisplayText.PLEASE_WAIT) {
            msg = getString(R.string.wait);
        } else if (displayText == DisplayText.PROCESSING_ERROR) {
            msg = getString(R.string.processing_error);
        } else if (displayText == DisplayText.REMOVE_CARD) {
            msg = getString(R.string.remove_card);
        } else if (displayText == DisplayText.USE_CHIP_READER) {
            msg = getString(R.string.use_chip_reader);
        } else if (displayText == DisplayText.USE_MAG_STRIPE) {
            msg = getString(R.string.use_mag_stripe);
        } else if (displayText == DisplayText.TRY_AGAIN) {
            msg = getString(R.string.try_again);
        } else if (displayText == DisplayText.REFER_TO_YOUR_PAYMENT_DEVICE) {
            msg = getString(R.string.refer_payment_device);
        } else if (displayText == DisplayText.TRANSACTION_TERMINATED) {
            msg = getString(R.string.transaction_terminated);
        } else if (displayText == DisplayText.TRY_ANOTHER_INTERFACE) {
            msg = getString(R.string.try_another_interface);
        } else if (displayText == DisplayText.ONLINE_REQUIRED) {
            msg = getString(R.string.online_required);
        } else if (displayText == DisplayText.PROCESSING) {
            msg = getString(R.string.processing);
        } else if (displayText == DisplayText.WELCOME) {
            msg = getString(R.string.welcome);
        } else if (displayText == DisplayText.PRESENT_ONLY_ONE_CARD) {
            msg = getString(R.string.present_one_card);
        } else if (displayText == DisplayText.CAPK_LOADING_FAILED) {
            msg = getString(R.string.capk_failed);
        } else if (displayText == DisplayText.LAST_PIN_TRY) {
            msg = getString(R.string.last_pin_try);
        }

        statusEditText.setText(msg);
    }

    @Override
    public void onRequestClearDisplay() {
        dismissDialog();
        statusEditText.setText("");
    }

    @Override
    public void onRequestReferProcess(String pan) {
        dismissDialog();
        dialog = new Dialog(BindAudioDeviceActivity.this);
        dialog.setContentView(R.layout.refer_process_dialog);
        dialog.setTitle(getString(R.string.call_your_bank));

        dialog.findViewById(R.id.approvedButton).setOnClickListener(
                new OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        emvSwipeController
                                .sendReferProcessResult(ReferralResult.APPROVED);
                    }
                });

        dialog.findViewById(R.id.declinedButton).setOnClickListener(
                new OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        emvSwipeController
                                .sendReferProcessResult(ReferralResult.DECLINED);
                    }
                });

        dialog.findViewById(R.id.cancelButton).setOnClickListener(
                new OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        emvSwipeController.cancelReferProcess();
                    }
                });
    }

    @Override
    public void onRequestAdviceProcess(String tlv) {
        dismissDialog();
        statusEditText.setText(getString(R.string.advice_process));
    }

    @Override
    public void onRequestFinalConfirm() {
        dismissDialog();
        if (!isPinCanceled) {
            dialog = new Dialog(BindAudioDeviceActivity.this);
            dialog.setContentView(R.layout.confirm_dialog);
            dialog.setTitle(getString(R.string.confirm_amount));

            String message = getString(R.string.amount) + ": $" + amount;
            if (!cashbackAmount.equals("")) {
                message += "\n" + getString(R.string.cashback_amount) + ": $"
                        + cashbackAmount;
            }

            ((TextView) dialog.findViewById(R.id.messageTextView))
                    .setText(message);

            dialog.findViewById(R.id.confirmButton).setOnClickListener(
                    new OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            emvSwipeController.sendFinalConfirmResult(true);
                            dialog.dismiss();
                        }
                    });

            dialog.findViewById(R.id.cancelButton).setOnClickListener(
                    new OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            emvSwipeController.sendFinalConfirmResult(false);
                            dialog.dismiss();
                        }
                    });

            dialog.show();
        } else {
            emvSwipeController.sendFinalConfirmResult(false);
        }
    }

    @Override
    public void onAutoConfigProgressUpdate(double percentage) {
        if (progressDialog != null) {
            progressDialog.setProgress((int) percentage);
        }
    }

    @Override
    public void onAutoConfigCompleted(boolean isDefaultSettings,
                                      String autoConfigSettings) {
        if (progressDialog != null) {
            progressDialog.dismiss();
            progressDialog = null;
        }

        String outputDirectory = Environment.getExternalStorageDirectory()
                .getAbsolutePath() + "/Android/data/com.bbpos.emvswipe.ui/";
        String filename = "settings.txt";
        String content = getString(R.string.auto_config_completed);
        if (isDefaultSettings) {
            content += "\n" + getString(R.string.default_settings);
            new File(outputDirectory + filename).delete();
        } else {
            content += "\n" + getString(R.string.settings) + autoConfigSettings;

            try {
                File directory = new File(outputDirectory);
                if (!directory.isDirectory()) {
                    directory.mkdirs();
                }
                FileOutputStream fos = new FileOutputStream(outputDirectory
                        + filename, false);
                fos.write(autoConfigSettings.getBytes());
                fos.flush();
                fos.close();

                content += "\n"
                        + getString(R.string.settings_written_to_external_storage);
            } catch (Exception e) {
            }
        }
        statusEditText.setText(content);
    }

    @Override
    public void onAutoConfigError(AutoConfigError autoConfigError) {
        if (progressDialog != null) {
            progressDialog.dismiss();
            progressDialog = null;
        }

        if (autoConfigError == AutoConfigError.PHONE_NOT_SUPPORTED) {
            statusEditText
                    .setText(getString(R.string.auto_config_error_phone_not_supported));
        } else if (autoConfigError == AutoConfigError.INTERRUPTED) {
            statusEditText
                    .setText(getString(R.string.auto_config_error_interrupted));
        }
    }

    @Override
    public void onBatteryLow(BatteryStatus batteryStatus) {
        if (batteryStatus == BatteryStatus.LOW) {
            statusEditText.setText(getString(R.string.battery_low));
        } else if (batteryStatus == BatteryStatus.CRITICALLY_LOW) {
            statusEditText.setText(getString(R.string.battery_critically_low));
        }
    }

    @Override
    public void onNoDeviceDetected() {
        dismissDialog();
        statusEditText.setText(getString(R.string.no_device_detected));
    }

    @Override
    public void onDevicePlugged() {
        dismissDialog();
        statusEditText.setText(getString(R.string.device_plugged));
        isInsert = true;
    }

    @Override
    public void onDeviceUnplugged() {
        dismissDialog();
        statusEditText.setText(getString(R.string.device_unplugged));
        isInsert = false;
    }

    @Override
    public void onDeviceHere(boolean isHere) {
    }

    @Override
    public void onError(Error errorState) {
        // dismissDialog();
        //amountEditText.setText("");
        if (errorState == Error.CMD_NOT_AVAILABLE) {
            statusEditText.setText(getString(R.string.command_not_available));
        } else if (errorState == Error.TIMEOUT) {
            statusEditText.setText(getString(R.string.device_no_response));
        } else if (errorState == Error.DEVICE_RESET) {
            statusEditText.setText(getString(R.string.device_reset));
        } else if (errorState == Error.UNKNOWN) {
            statusEditText.setText(getString(R.string.unknown_error));
        } else if (errorState == Error.DEVICE_BUSY) {
            statusEditText.setText(getString(R.string.device_busy));
        } else if (errorState == Error.INPUT_OUT_OF_RANGE) {
            statusEditText.setText(getString(R.string.out_of_range));
        } else if (errorState == Error.INPUT_INVALID_FORMAT) {
            statusEditText.setText(getString(R.string.invalid_format));
            Toast.makeText(BindAudioDeviceActivity.this,
                    getString(R.string.invalid_format), Toast.LENGTH_LONG)
                    .show();
        } else if (errorState == Error.INPUT_ZERO_VALUES) {
            statusEditText.setText(getString(R.string.zero_values));
        } else if (errorState == Error.INPUT_INVALID) {
            statusEditText.setText(getString(R.string.input_invalid));
            Toast.makeText(BindAudioDeviceActivity.this,
                    getString(R.string.input_invalid), Toast.LENGTH_LONG)
                    .show();
        } else if (errorState == Error.CASHBACK_NOT_SUPPORTED) {
            statusEditText.setText(getString(R.string.cashback_not_supported));
            Toast.makeText(BindAudioDeviceActivity.this,
                    getString(R.string.cashback_not_supported),
                    Toast.LENGTH_LONG).show();
        } else if (errorState == Error.CRC_ERROR) {
            statusEditText.setText(getString(R.string.crc_error));
        } else if (errorState == Error.COMM_ERROR) {
            statusEditText.setText(getString(R.string.comm_error));
        }
    }

    @Override
    public void onPowerDown() {
        // statusEditText.setText(getString(R.string.device_off));
    }

    /**
     * 设置刷卡器初始化
     */
    private void setSwingCardInit() {

        if (emvSwipeController == null) {
            emvSwipeController = EmvSwipeController.getInstance(this,
                    BindAudioDeviceActivity.this);
            emvSwipeController.startAudio();
        }

        try {
            String filename = "settings.txt";
            String inputDirectory = Environment.getExternalStorageDirectory()
                    .getAbsolutePath() +

                    "/Android/data/com.bbpos.emvswipe.ui/";

            FileInputStream fis = new FileInputStream(inputDirectory +

                    filename);
            byte[] temp = new byte[fis.available()];
            fis.read(temp);
            fis.close();

            emvSwipeController.setAutoConfig(new String(temp));
            Toast.makeText(BindAudioDeviceActivity.this, getString

                    (R.string.setting_config), Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
        }
    }

    private void initUI() {


        checkCardButton = (Button) findViewById(R.id.checkCardButton);
        checkCardButton.setOnClickListener(this);
        //amountEditText = (EditText) findViewById(R.id.amountEditText);
        statusEditText = (EditText) findViewById(R.id.statusEditText);

    }

    @Override
    public void onBackPressed() {
        // TODO Auto-generated method stub
        finish();
        super.onBackPressed();
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.checkCardButton://获取Ksn后直接在获取设别设备信息的回调接口中绑定和签到设备
                if (!isInsert) {

                    Toast.makeText(BindAudioDeviceActivity.this, "请插入装置方可绑定设备!", Toast.LENGTH_SHORT).show();
                    return;

                }
                statusEditText.setText(R.string.getting_ksn);
                emvSwipeController.getKsn();
                break;
        }

    }

    /**
     * 终端签到和绑定
     */
    private void gotoBindAndSign() {
        //终端绑定
        HashMap<String, String> params = new HashMap<String, String>();
        params.put("termNo", termNo);
        MyHttpClient.post(this, Urls.POS_BING, params,
                new AsyncHttpResponseHandler() {

                    @Override
                    public void onSuccess(int statusCode, Header[] headers,
                                          byte[] responseBody) {
                        try {
                            BasicResponse re = new BasicResponse(responseBody)
                                    .getResult();
                            if (re.isSuccess()) {
                                T.showCustomeOk(BindAudioDeviceActivity.this, "绑定成功!");
                            } else {
                                T.ss(re.getMsg());
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


        //终端签到
        HashMap<String, String> params1 = new HashMap<String, String>();
        params1.put("termNo", termNo);
        params1.put("termType", "02");
        MyHttpClient.post(this, Urls.BLUETOOTH_SIGN, params1,
                new AsyncHttpResponseHandler() {

                    @Override
                    public void onSuccess(int statusCode, Header[] headers,
                                          byte[] responseBody) {
                        try {
                            BasicResponse re = new BasicResponse(responseBody)
                                    .getResult();
                            if (re.isSuccess()) {
                                String pinkey = re.getJsonBody().optString("zpik");
                                MApplication.mSharedPref.putSharePrefString("pinkey", pinkey);
                                T.showCustomeOk(BindAudioDeviceActivity.this, "签到成功!");
                                finish();
                            } else {
                                T.ss(re.getMsg());
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
                        showLoadingDialog();
                    }

                    @Override
                    public void onFinish() {
                        super.onFinish();
                        dismissLoadingDialog();
                    }
                });
    }

    @Override
    public void onReturnCAPKDetail(CAPK arg0) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onReturnCAPKList(List<CAPK> arg0) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onReturnCAPKLocation(String arg0) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onReturnEmvCardBalance(boolean arg0, String arg1) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onReturnEmvLoadLog(String[] arg0) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onReturnEmvReport(String arg0) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onReturnEmvReportList(Hashtable<String, String> arg0) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onReturnEmvTransactionLog(String[] arg0) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onReturnEncryptPinResult(Hashtable<String, String> arg0) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onReturnUpdateCAPKResult(boolean arg0) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onReturnViposBatchExchangeApduResult(
            Hashtable<Integer, String> arg0) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onReturnViposExchangeApduResult(String arg0) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onWaitingForCard(CheckCardMode arg0) {
        dismissDialog();
        statusEditText.setText(getString(R.string.waiting_for_card));
    }

    @Override
    public void onBatchDataDetected() {
        // TODO 自动生成的方法存根

    }

    @Override
    public void onOnlineProcessDataDetected() {
        // TODO 自动生成的方法存根

    }

    @Override
    public void onReturnEncryptDataResult(boolean arg0,
                                          Hashtable<String, String> arg1) {
        // TODO 自动生成的方法存根

    }

    @Override
    public void onReversalDataDetected() {
        // TODO 自动生成的方法存根

    }

}
