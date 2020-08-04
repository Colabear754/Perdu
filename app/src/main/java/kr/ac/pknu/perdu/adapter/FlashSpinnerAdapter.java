package kr.ac.pknu.perdu.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import kr.ac.pknu.perdu.R;

public class FlashSpinnerAdapter extends BaseAdapter {
    Context context;
    LayoutInflater inflater;
    int[] spinnerImages;

    public FlashSpinnerAdapter(Context context, int[] images) {
        this.context = context;
        this.spinnerImages = images;
        inflater = (LayoutInflater.from(context));
    }

    @Override
    public int getCount() {
        return spinnerImages.length;
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        convertView = inflater.inflate(R.layout.spinner_layout, null);
        ImageView icon = convertView.findViewById(R.id.flashSpinnerImage);
        icon.setImageResource(spinnerImages[position]);
        return icon;
    }
}