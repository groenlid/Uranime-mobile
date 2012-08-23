package com.banan.providers;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.ActionBar.Tab;

public class TabListener<T extends Fragment> implements ActionBar.TabListener {
	
	private final FragmentActivity mActivity;
    private final String mTag;
    private final Class<T> mClass;
    private final Bundle mArgs;
    private Fragment mFragment;
    
    public TabListener(FragmentActivity activity, String tag, Class<T> clz, Bundle args) {
        mActivity = activity;
        mTag = tag;
        mClass = clz;
        mArgs = args;
        FragmentTransaction ft = mActivity.getSupportFragmentManager().beginTransaction();


        // Check to see if we already have a fragment for this tab, probably
        // from a previously saved state.  If so, deactivate it, because our
        // initial state is that a tab isn't shown.
        mFragment = mActivity.getSupportFragmentManager().findFragmentByTag(mTag);
        if (mFragment != null && !mFragment.isDetached()) {
            ft.detach(mFragment);
        }
    }
    
    
	@Override
	public void onTabSelected(Tab tab, FragmentTransaction ft) {
		//SherlockFragment preInitializedFragment = (SherlockFragment) mActivity.getSupportFragmentManager().findFragmentByTag(mTag);
		 //FragmentTransaction 
		ft = mActivity.getSupportFragmentManager().beginTransaction();

		if(mFragment != null){
            ft.attach(mFragment);
            ft.commit();
        } else {
            mFragment = Fragment.instantiate(mActivity, mClass.getName(), mArgs);
            ft.add(android.R.id.content, mFragment, mTag);
            ft.commit();
        }
	}

	@Override
	public void onTabUnselected(Tab tab, FragmentTransaction ft) {
		 //FragmentTransaction 
		ft = mActivity.getSupportFragmentManager().beginTransaction();

	        if (mFragment != null) {
	            ft.detach(mFragment);
	            ft.commitAllowingStateLoss();
	        }           
		
	}

	@Override
	public void onTabReselected(Tab tab, FragmentTransaction ft) {
		
	}
	
}