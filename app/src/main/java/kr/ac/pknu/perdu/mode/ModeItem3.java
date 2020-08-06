package kr.ac.pknu.perdu.mode;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import kr.ac.pknu.perdu.R;

public class ModeItem3 extends Fragment {
    TextView modeItem3;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.mode_item3, container, false);
        modeItem3 = view.findViewById(R.id.modeItem3);
        return view;
    }

    public void setTextView(String str, int textColor) {
        modeItem3.setText(str);
        modeItem3.setTextColor(textColor);
    }
}
