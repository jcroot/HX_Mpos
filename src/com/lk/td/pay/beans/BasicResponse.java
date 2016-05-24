package com.lk.td.pay.beans;

import java.io.Serializable;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;

import com.lk.td.pay.activity.account.login.LoginActivity;
import com.lk.td.pay.golbal.MApplication;
import com.lk.td.pay.request.ParamsUtils;
import com.lk.td.pay.wedget.CustomDialog;
import com.lk.td.pay.wedget.MyDialogActivity;

public class BasicResponse implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 7314798688932428397L;
	protected String RSPCOD;
	protected String RSPMSG;
	private boolean isSuccess = false;
	private String msg;
	private byte[] response;
	private JSONObject jsonBody;
	public static final String LOGIN_EXPIRE = "888888";
	CustomDialog mDialog = null;
	// private JSONArray jsonArray;
	public BasicResponse(byte[] response) {
		this.response = response;
	}

	public BasicResponse getResult() throws JSONException {
		if (response != null) {
			JSONObject obj = new JSONObject(new String(response))
					.getJSONObject(ParamsUtils.RESULT_REP_BODY);
			this.jsonBody = obj;
			this.msg = obj.optString(ParamsUtils.RESULT_RSPMSG);
			if (obj.optString(ParamsUtils.RESULT_RSPCOD).equals("000000")) {

				isSuccess = true;
			} else if (obj.optString(ParamsUtils.RESULT_RSPCOD).equals("888889")) {
				isSuccess = false;
				// 长时间未操作超时
				// MApplication.getInstance().getApplicationContext();
				final Context ctx = MApplication.getInstance().getMainHomeContext();
				if (ctx == null) {
					MApplication
							.getInstance()
							.getApplicationContext()
							.startActivity(
									new Intent(MApplication.getInstance()
											.getApplicationContext(),
											LoginActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP));
				} else {
					ctx.startActivity(new Intent(ctx, MyDialogActivity.class)
							.putExtra("title", "账户安全提示").putExtra("msg",
									"该账户已在其它设备登录！"));

//					showDialog(ctx, "账户安全提示", "该账户已在其它设备登录！", new DialogInterface.OnClickListener() {
//						@Override
//						public void onClick(DialogInterface dialog, int i) {
//
//							ctx.startActivity(new Intent(ctx,
//									LoginActivity.class)
//									.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK));
//							dialog.dismiss();
//						}
//					});
				}

			}else if (obj.optString("RSPCOD").equals("888888")){
				isSuccess = false;
				// 长时间未操作超时
				// MApplication.getInstance().getApplicationContext();
				final Context ctx = MApplication.getInstance().getMainHomeContext();
				if (ctx == null) {
					MApplication
							.getInstance()
							.getApplicationContext()
							.startActivity(
									new Intent(MApplication.getInstance()
											.getApplicationContext(),
											LoginActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
											.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP));
				} else {
					ctx.startActivity(new Intent(ctx, MyDialogActivity.class)
							.putExtra("title", "账户安全提示").putExtra("msg",
									"账户长时间未操作,请重新登录."));

//					showDialog(ctx, "账户安全提示", "账户长时间未操作,请重新登录.", new DialogInterface.OnClickListener() {
//						@Override
//						public void onClick(DialogInterface dialog, int i) {
//
//							ctx.startActivity(new Intent(ctx,
//									LoginActivity.class)
//									.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK));
//							dialog.dismiss();
//						}
//					});
				}
			} else if (obj.optString(ParamsUtils.RESULT_RSPCOD).equals("900001")){
				isSuccess = false;
				// MApplication.getInstance().getApplicationContext();
				final Context ctx = MApplication.getInstance().getMainHomeContext();
				if (ctx == null) {
					MApplication
							.getInstance()
							.getApplicationContext()
							.startActivity(
									new Intent(MApplication.getInstance()
											.getApplicationContext(),
											LoginActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP));
				} else {
					ctx.startActivity(new Intent(ctx, MyDialogActivity.class)
							.putExtra("title", "账户安全提示").putExtra("msg",
									"服务器未响应,请重新登录."));

//					showDialog(ctx, "账户安全提示", "服务器未响应,请重新登录.", new DialogInterface.OnClickListener() {
//						@Override
//						public void onClick(DialogInterface dialog, int i) {
//
//							ctx.startActivity(new Intent(ctx,
//									LoginActivity.class)
//									.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK));
//							dialog.dismiss();
//						}
//					});
				}
			}
			return this;
		}
		return null;
	}


	public String getRSPCOD() {
		return RSPCOD;
	}

	public void setRSPCOD(String rSPCOD) {
		RSPCOD = rSPCOD;
	}

	public String getRSPMSG() {
		return RSPMSG;
	}

	public void setRSPMSG(String rSPMSG) {
		RSPMSG = rSPMSG;
	}

	public boolean isSuccess() {
		return isSuccess;
	}

	public String getMsg() {
		return msg;
	}

	public JSONObject getJsonBody() {
		return jsonBody;
	}

	public void setJsonBody(JSONObject jsonBody) {
		this.jsonBody = jsonBody;
	}

	// public JSONArray getJsonArray() {
	// return jsonArray;
	// }
	//
	// public void setJsonArray(JSONArray jsonArray) {
	// this.jsonArray = jsonArray;
	// }

}
