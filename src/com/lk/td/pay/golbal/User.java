package com.lk.td.pay.golbal;

import android.util.Log;

import com.lk.td.pay.beans.BankCardItem;
import com.lk.td.pay.beans.BindDeviceInfo;
import com.lk.td.pay.beans.SaveUserState;

import java.util.List;

public class User {

    public static boolean login = false;
    public static String uAccount;
    public static String uId;
    public static String uName;
    public static String sign;
    public static String cache_card;
    /**
     * 认证状态（0未认证，1审核中，2审核通过，3审核不通过）
     */
    public static int uStatus;
    /**
     * 银行卡数量
     */
    public static int cardNum;
    /**
     * 已绑定数量
     */
    public static int termNum;


    /**
     * pos绑定状态
     * 0-未绑定
     * 1-已绑定
     */
    public static int bindStatus;

    /**
     * 已绑定的设备列表
     * 登陆后初始化
     */
    public static List<BindDeviceInfo> bindDevices;
    /**
     * 即时到账余额
     */
    public static String amtT0;
    /**
     * 未到账余额
     */
    public static String amtT1;

    /**
     * 隔天到账余额
     */
    public static String amtT1y;
    /**
     * 账户总余额
     */
    public static String totalAmt;

    /**
     *未审核金额
     */
    public static String acT1UNA;

    /**
     * 已审核金额
     */
    public static String acT1AP;

    /**
     * 未审核金额
     */
    public static String acT1AUNP;


    /**
     * 已绑定的银行卡信息
     * 被动赋值
     */
    public static BankCardItem cardInfo;

    public static int cardBundingStatus;
    public static String ideCardAuthError;
    public static String bankCardAuthError;
    public static String macAddress;

    public static SaveUserState createUserState() {
        Log.i("[异常重启]", "保存用户状态");
        SaveUserState temp = new SaveUserState();
        temp.setAmtT0(User.amtT0);
        temp.setAmtT1(User.amtT1);
        temp.setAmtT1y(User.amtT1y);
        temp.setBindStatus(User.bindStatus);
        temp.setCardNum(User.cardNum);
        temp.setLogin(User.login);
        temp.setSign(User.sign);
        temp.setTermNum(User.termNum);
        temp.setTotalAmt(User.totalAmt);
        temp.setuAccount(User.uAccount);
        temp.setuId(User.uId);
        temp.setuName(User.uName);
        temp.setuStatus(User.uStatus);
        Log.i("[异常重启]", "保存用户状态--succ");
        return temp;
    }

    public static void reStoreUserState(SaveUserState user) {
        Log.i("[异常重启]", "恢复用户状态");
        User.amtT0 = user.getAmtT0();
        User.amtT1 = user.getAmtT1();
        User.amtT1y = user.getAmtT1y();
        User.bindStatus = user.getBindStatus();
        User.cardNum = user.getCardNum();
        User.login = user.isLogin();
        User.termNum = user.getTermNum();
        User.totalAmt = user.getTotalAmt();
        User.uAccount = user.getuAccount();
        User.uId = user.getuId();
        User.uName = user.getuName();
        User.uStatus = user.getuStatus();
        Log.i("[异常重启]", "恢复用户状态-succ");
    }


}
