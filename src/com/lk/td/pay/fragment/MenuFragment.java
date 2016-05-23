package com.lk.td.pay.fragment;

import com.td.app.pay.hx.R;
import com.lk.td.pay.wedget.ImageCycleView;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;

public class MenuFragment extends BaseFragment{
	
	private View layoutView;
	private ImageCycleView bannerView;
	
	public static BaseFragment newInstance() {
		BaseFragment fragment = new MenuFragment();
		return fragment;
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		layoutView = inflater.inflate(R.layout.fragment_menu, container, false);
		bannerView = (ImageCycleView) layoutView.findViewById(R.id.menu_banner_view);
		initView();
		
		return layoutView;
	}

	private void initView() {
		LayoutParams params1 = bannerView.getLayoutParams();
		params1.width = getDisplayMetricsWidth();
		params1.height = (int) (params1.width*1.0/484*200);
		
		bannerView.setLayoutParams(params1);
		
	}
	
	//获取屏幕宽度
	public int getDisplayMetricsWidth() {
		int i = getActivity().getWindowManager().getDefaultDisplay().getWidth();
		int j = getActivity().getWindowManager().getDefaultDisplay().getHeight();
		return Math.min(i, j);
	}
}
