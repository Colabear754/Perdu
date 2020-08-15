package kr.ac.pknu.perdu.adapter;

import android.annotation.SuppressLint;
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

    @SuppressLint({"ViewHolder", "InflateParams"})
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        convertView = inflater.inflate(R.layout.spinner_layout, null);
        ImageView icon = convertView.findViewById(R.id.flashSpinnerImage);

        if (position == 0)
            icon.setBackgroundColor(0xFF000000);
        else
            icon.setBackgroundColor(0x00000000);

        icon.setImageResource(spinnerImages[position]);
        return icon;
    }
}
