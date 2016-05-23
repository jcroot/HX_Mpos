package com.lk.td.pay.wedget;

import java.util.List;

import android.app.Activity;
import android.inputmethodservice.Keyboard;
import android.inputmethodservice.Keyboard.Key;
import android.inputmethodservice.KeyboardView;
import android.inputmethodservice.KeyboardView.OnKeyboardActionListener;
import android.text.Editable;
import android.text.InputType;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.td.app.pay.hx.R;

/**
 * 
 * @ClassName: CustomKeyboard 
 *
 * @Description: When an activity hosts a keyboardView, this class allows several EditText's to register for it.
 *
 * @author Lucifer
 *
 * @date 2015年4月9日 上午11:17:09 
 *
 */
public class CustomKeyboard {

	private LinearLayout keyboardLayout;
    /** A link to the KeyboardView that is used to render this CustomKeyboard. */
    private KeyboardView mKeyboardView;
    /** A link to the activity that hosts the {@link #mKeyboardView}. */
    private Activity     mHostActivity;
    
    public boolean isUpper = false;// 是否大写
    
    public boolean isNum = false;// 是否数据键盘
    
    private Keyboard keyboard_all; //全键盘对象
	private Keyboard keyboard_number; //数字键盘对象

    /** The key (code) handler. */
    private OnKeyboardActionListener mOnKeyboardActionListener = new OnKeyboardActionListener() {

        public final static int CodeDelete     = -5; // Keyboard.KEYCODE_DELETE
        public final static int CodeCancel     = -3; // Keyboard.KEYCODE_CANCEL
        public final static int CodeShift      = -1; // Keyboard.KEYCODE_SHIFT
        public final static int CodeModeChange = -2; // Keyboard.KEYCODE_MODE_CHANGE
        public final static int CodePrev       = 55000;
        public final static int CodeAllLeft    = 55001;
        public final static int CodeLeft       = 55002;
        public final static int CodeRight      = 55003;
        public final static int CodeAllRight   = 55004;
        public final static int CodeNext       = 55005;
        public final static int CodeClear      = 55006;

        @Override 
     public void onKey(int primaryCode, int[] keyCodes) {
            // NOTE We can say '<Key android:codes="49,50" ... >' in the xml file; all codes come in keyCodes, the first in this list in primaryCode
            // Get the EditText and its Editable
            View focusCurrent = mHostActivity.getWindow().getCurrentFocus();
            if( focusCurrent==null || focusCurrent.getClass()!=EditText.class ) return;
            EditText edittext = (EditText) focusCurrent;
            Editable editable = edittext.getText();
            int start = edittext.getSelectionStart();
            // Apply the key to the edittext
            if( primaryCode==CodeCancel ) {
                hideCustomKeyboard();
            } else if(primaryCode==CodeShift){
            	changeKey();
            } else if(primaryCode==CodeModeChange){
            	//changeKeyTONum();
            } else if( primaryCode==CodeDelete ) {
                if( editable!=null && start>0 ) editable.delete(start - 1, start);
            } else if( primaryCode==CodeClear ) {
                if( editable!=null ) editable.clear();
            } else if( primaryCode==CodeLeft ) {
                if( start>0 ) edittext.setSelection(start - 1);
            } else if( primaryCode==CodeRight ) {
                if (start < edittext.length()) edittext.setSelection(start + 1);
            } else if( primaryCode==CodeAllLeft ) {
                edittext.setSelection(0);
            } else if( primaryCode==CodeAllRight ) {
                edittext.setSelection(edittext.length());
            } else if( primaryCode==CodePrev ) {
                View focusNew= edittext.focusSearch(View.FOCUS_BACKWARD);
                if( focusNew!=null ) focusNew.requestFocus();
            } else if( primaryCode==CodeNext ) {
                View focusNew= edittext.focusSearch(View.FOCUS_FORWARD);
                if( focusNew!=null ) focusNew.requestFocus();
            } else { // insert character
                editable.insert(start, Character.toString((char) primaryCode));
            }
        }

        @Override public void onPress(int arg0) {
        }

        @Override public void onRelease(int primaryCode) {
        }

        @Override public void onText(CharSequence text) {
        }

        @Override public void swipeDown() {
        }

        @Override public void swipeLeft() {
        }

        @Override public void swipeRight() {
        }

        @Override public void swipeUp() {
        }
    };

    /**
     * Create a custom keyboard, that uses the KeyboardView (with resource id <var>viewid</var>) of the <var>host</var> activity,
     * and load the keyboard layout from xml file <var>layoutid</var> (see {@link Keyboard} for description).
     * Note that the <var>host</var> activity must have a <var>KeyboardView</var> in its layout (typically aligned with the bottom of the activity).
     * Note that the keyboard layout xml file may include key codes for navigation; see the constants in this class for their values.
     * Note that to enable EditText's to use this custom keyboard, call the {@link #registerEditText(int)}.
     *
     * @param host The hosting activity.
     * @param viewid The id of the KeyboardView.
     * @param layoutid The id of the xml file containing the keyboard layout.
     */
    public CustomKeyboard(Activity host, boolean isNumKeybroad) {
    	this.isNum = isNumKeybroad;
        init(host);
    }
    
    public CustomKeyboard(Activity host) {
    	init(host);
    }
    
    private void init(Activity host){
    	mHostActivity= host;
        keyboardLayout = (LinearLayout) mHostActivity.findViewById(R.id.keyboardlayout);
        mKeyboardView= (KeyboardView)mHostActivity.findViewById(R.id.keyboardview);
        /**************************************************************/
        keyboard_all = new Keyboard(mHostActivity, R.xml.qwerty);
		keyboard_number = new Keyboard(mHostActivity, R.xml.symbols);
		if(isNum){
			this.mKeyboardView.setKeyboard(keyboard_number);
		} else {
			this.mKeyboardView.setKeyboard(keyboard_all);
		}
		
        /**************************************************************/
        //mKeyboardView.setKeyboard(new Keyboard(mHostActivity, layoutid));
        mKeyboardView.setPreviewEnabled(true); // NOTE Do not show the preview balloons
        mKeyboardView.setOnKeyboardActionListener(mOnKeyboardActionListener);
        // Hide the standard keyboard initially
        mHostActivity.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
    }

    /** Returns whether the CustomKeyboard is visible. */
    public boolean isCustomKeyboardVisible() {
        //return mKeyboardView.getVisibility() == View.VISIBLE;
    	return keyboardLayout.getVisibility() == View.VISIBLE;
    }

    /** Make the CustomKeyboard visible, and hide the system keyboard for view v. */
    public void showCustomKeyboard( View v ) {
    	keyboardLayout.setVisibility(View.VISIBLE);
        //mKeyboardView.setVisibility(View.VISIBLE);
        mKeyboardView.setEnabled(true);
        if( v!=null ) ((InputMethodManager)mHostActivity.getSystemService(Activity.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(v.getWindowToken(), 0);
    }

    /** Make the CustomKeyboard invisible. */
    public void hideCustomKeyboard() {
    	keyboardLayout.setVisibility(View.GONE);
        //mKeyboardView.setVisibility(View.GONE);
        mKeyboardView.setEnabled(false);
    }
    
    /**
	 * 切换键盘大小写字母
	 * 按照ascii码表可知，小写字母 = 大写字母+32;
	 */
	private void changeKey(){
		List<Key> keyList = mKeyboardView.getKeyboard().getKeys();
		if(isUpper){//如果为真表示当前为大写，需切换为小写
			isUpper = false;
			for (Key key : keyList) {
				if(key.label != null && isWord(key.label.toString())){
					key.label = key.label.toString().toLowerCase();
					key.codes[0] = key.codes[0]+32;
				}
			}
		}else{//如果为假表示当前为小写，需切换为大写
			isUpper = true;
			for (Key key : keyList) {
				if(key.label != null && isWord(key.label.toString())){
					key.label = key.label.toString().toUpperCase();
					key.codes[0] = key.codes[0]-32;
				}
			}
		}
		mKeyboardView.setKeyboard(mKeyboardView.getKeyboard());
	}

	/**判断是否为字母
	 * 
	 * @param str 需判断的字符串
	 */ 
	private boolean isWord(String str){
		String wordStr = "abcdefghijklmnopqrstuvwxyz";
		if(wordStr.indexOf(str.toLowerCase()) > -1){
			return true;
		}
		return false;
	}
	
	/**
	 * 数字键盘切换
	 */
	private void changeKeyTONum(){
		if(isNum){ //当前为数字键盘,切换为全字母键盘
			isNum = false;
			mKeyboardView.setKeyboard(keyboard_all);
		}else{//当前为全字母键盘，切换为数字键盘
			isNum = true;
			randomNumKey();
			mKeyboardView.setKeyboard(keyboard_number);
		}
	}
	
	/**
	 * 随机数字键盘,随机键盘LABEL中不能存在图片，否则在随机换位过程中会报错
	 */
	private void randomNumKey(){
		List<Key> keyList = keyboard_number.getKeys();
		int size = keyList.size();
		for (int i = 0; i < size; i++) {
			int random_a = (int)(Math.random()*(size));
			int random_b = (int)(Math.random()*(size));

			int code = keyList.get(random_a).codes[0];
			int code_b = keyList.get(random_b).codes[0];
			if(code == -3 || code == -5 || code_b == -3 || code_b == -5){
				continue;
			}
			
			CharSequence label = keyList.get(random_a).label;
			
			keyList.get(random_a).codes[0] = keyList.get(random_b).codes[0];
			keyList.get(random_a).label = keyList.get(random_b).label;
			
			keyList.get(random_b).codes[0] = code;
			keyList.get(random_b).label = label;
			
		}
	}
	
    /**
     * Register <var>EditText<var> with resource id <var>resid</var> (on the hosting activity) for using this custom keyboard.
     *
     * @param resid The resource id of the EditText that registers to the custom keyboard.
     */
    public void registerEditText(int resid) {
        // Find the EditText 'resid'
        EditText edittext= (EditText)mHostActivity.findViewById(resid);
        // Make the custom keyboard appear
        edittext.setOnFocusChangeListener(new OnFocusChangeListener() {
            // NOTE By setting the on focus listener, we can show the custom keyboard when the edit box gets focus, but also hide it when the edit box loses focus
            @Override public void onFocusChange(View v, boolean hasFocus) {
                if( hasFocus ) showCustomKeyboard(v); else hideCustomKeyboard();
            }
        });
        edittext.setOnClickListener(new OnClickListener() {
            // NOTE By setting the on click listener, we can show the custom keyboard again, by tapping on an edit box that already had focus (but that had the keyboard hidden).
            @Override public void onClick(View v) {
                showCustomKeyboard(v);
            }
        });
        // Disable standard keyboard hard way
        // NOTE There is also an easy way: 'edittext.setInputType(InputType.TYPE_NULL)' (but you will not have a cursor, and no 'edittext.setCursorVisible(true)' doesn't work )
        edittext.setOnTouchListener(new OnTouchListener() {
            @Override public boolean onTouch(View v, MotionEvent event) {
                EditText edittext = (EditText) v;
                int inType = edittext.getInputType();       // Backup the input type
                edittext.setInputType(InputType.TYPE_NULL); // Disable standard keyboard
                edittext.onTouchEvent(event);               // Call native handler
                edittext.setInputType(inType);              // Restore input type
                return true; // Consume touch event
            }
        });
        // Disable spell check (hex strings look like words to Android)
        edittext.setInputType(edittext.getInputType() | InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
    }

}
