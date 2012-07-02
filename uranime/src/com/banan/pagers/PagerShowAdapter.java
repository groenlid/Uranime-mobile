package com.banan.pagers;

import com.banan.fragments.AnimeFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

public class PagerShowAdapter extends FragmentPagerAdapter{

	public PagerShowAdapter(FragmentManager fm) {
		super(fm);
	}

	protected static final String[] CONTENT = new String[]{ "Summary", "Episodes"};
	
	@Override
	public Fragment getItem(int position) {
		return AnimeFragment.newInstance(PagerShowAdapter.CONTENT[position % PagerShowAdapter.CONTENT.length]);
	}

	@Override
	public int getCount() {
		return PagerShowAdapter.CONTENT.length;
	}
	
	/*public void setCount(int count){
		if(count > 0 && count <= 10){
			mCount = count;
			notifyDataSetChanged();
		}
	}*/
	
	public CharSequence getPageTitle(int position) {
		return PagerShowAdapter.CONTENT[position % PagerShowAdapter.CONTENT.length].toUpperCase();
	}
	
}
