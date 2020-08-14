package kr.ac.pknu.perdu.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import kr.ac.pknu.perdu.R;

public class AspectRatioSpinnerAdapter extends BaseAdapter {
    Context context;
    LayoutInflater inflater;
    String[] spinnerStrings;

    public AspectRatioSpinnerAdapter(Context context, String[] str) {
        this.context = context;
        this.spinnerStrings = str;
        inflater = (LayoutInflater.from(context));
    }

    @Override
    public int getCount() {
        return spinnerStrings.length;
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
        TextView icon = convertView.findViewById(R.id.aspectRatioSpinnerImage);
        icon.setText(spinnerStrings[position]);
        return icon;
    }
}
