package com.lk.td.pay.activity.main.cashin.swing;

import java.util.ArrayList;
import java.util.Hashtable;

import com.bbpos.emvswipe.EmvSwipeController.AutoConfigError;
import com.bbpos.emvswipe.EmvSwipeController.BatteryStatus;
import com.bbpos.emvswipe.EmvSwipeController.CheckCardResult;
import com.bbpos.emvswipe.EmvSwipeController.DisplayText;
import com.bbpos.emvswipe.EmvSwipeController.Error;
import com.bbpos.emvswipe.EmvSwipeController.NfcDataExchangeStatus;
import com.bbpos.emvswipe.EmvSwipeController.StartEmvResult;
import com.bbpos.emvswipe.EmvSwipeController.TerminalSettingStatus;
import com.bbpos.emvswipe.EmvSwipeController.TransactionResult;

/**
 * 设置音频刷卡器的回调接口
 * @author Administrator
 *
 */
public interface ISwingCard {

	public abstract void onWaitingForCard();
	public abstract void onReturnCheckCardResult(CheckCardResult checkCardResult, Hashtable<String, String> decodeData);
	public abstract void onReturnCancelCheckCardResult(boolean isSuccess);
	public abstract void onReturnEncryptPinResult(String epb, String ksn);
	public abstract void onReturnEncryptDataResult(String encryptedData, String ksn);
	public abstract void onReturnStartEmvResult(StartEmvResult result, String ksn);
	/**
	 * 获得设备的基本信息
	 * @param deviceInfoData
	 */
	public abstract void onReturnDeviceInfo(Hashtable<String, String> deviceInfoData);
	@Deprecated
	public abstract void onReturnTransactionResult(TransactionResult transResult);
	public abstract void onReturnTransactionResult(TransactionResult transResult, Hashtable<String, String> data);
	public abstract void onReturnBatchData(String tlv);
	public abstract void onReturnTransactionLog(String tlv);
	public abstract void onReturnReversalData(String tlv);
	public abstract void onReturnPowerOnIccResult(boolean isSuccess, String ksn, String atr, int atrLength);
	public abstract void onReturnPowerOffIccResult(boolean isSuccess);
	public abstract void onReturnApduResult(boolean isSuccess, String apdu, int apduLength);
	public abstract void onReturnApduResultWithPkcs7Padding(boolean isSuccess, String apdu);
	public abstract void onReturnExchangeApduResult(String apdu);
	public abstract void onReturnBatchExchangeApduResult(Hashtable<Integer, String> data);
	public abstract void onReturnEmvCardDataResult(boolean isSuccess, String tlv);
	public abstract void onReturnEmvCardNumber(String cardNumber);
	public abstract void onReturnPowerOnNfcResult(boolean isSuccess, String response, int responseLength);
	public abstract void onReturnPowerOffNfcResult(boolean isSuccess);
	public abstract void onReturnNfcDataResult(NfcDataExchangeStatus nfcDataExchangeStatus, String data, int dataLength);
	public abstract void onReturnKsn(Hashtable<String, String> ksntable);
	public abstract void onReturnUpdateTerminalSettingResult(TerminalSettingStatus terminalSettingStatus);
	public abstract void onReturnReadTerminalSettingResult(TerminalSettingStatus terminalSettingStatus, String value);
	public abstract void onRequestSelectApplication(ArrayList<String> appList);
	public abstract void onRequestSetAmount();
	public abstract void onRequestPinEntry();
	public abstract void onRequestVerifyID(String tlv);
	public abstract void onRequestCheckServerConnectivity();
	public abstract void onRequestOnlineProcess(String tlv);
	public abstract void onRequestTerminalTime();
	public abstract void onRequestDisplayText(DisplayText displayText);
	public abstract void onRequestClearDisplay();
	public abstract void onRequestReferProcess(String pan);
	public abstract void onRequestAdviceProcess(String tlv);
	public abstract void onRequestFinalConfirm();
	public abstract void onAutoConfigProgressUpdate(double percentage);
	public abstract void onAutoConfigCompleted(boolean isDefaultSettings, String autoConfigSettings);
	public abstract void onAutoConfigError(AutoConfigError autoConfigError);
	public abstract void onBatteryLow(BatteryStatus batteryStatus);
	public abstract void onNoDeviceDetected();
	public abstract void onDevicePlugged();
	public abstract void onDeviceUnplugged();
	public abstract void onDeviceHere(boolean isHere);
	public abstract void onError(Error errorState);
	public abstract void onPowerDown();
	
}
