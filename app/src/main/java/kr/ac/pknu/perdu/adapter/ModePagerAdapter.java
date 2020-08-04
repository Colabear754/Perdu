package kr.ac.pknu.perdu.adapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

import java.util.ArrayList;
// 모드 변경 페이저를 위한 어댑터
public class ModePagerAdapter extends FragmentStatePagerAdapter {
    ArrayList<Fragment> modes = new ArrayList<Fragment>();
    public ModePagerAdapter(FragmentManager fragmentManager) {
        super(fragmentManager);
    }

    public void addItem(Fragment item) {
        modes.add(item);
    }

    @NonNull
    @Override
    public Fragment getItem(int position) {
        return modes.get(position);
    }

    @Override
    public int getCount() {
        return modes.size();
    }
}
