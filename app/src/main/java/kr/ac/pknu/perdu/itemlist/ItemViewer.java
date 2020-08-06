package kr.ac.pknu.perdu.itemlist;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;

import kr.ac.pknu.perdu.R;

public class ItemViewer extends LinearLayout {
    // 리스트의 아이템을 표시하기 위한 클래스
    TextView itemName;
    ImageView itemIcon;
    int itemID;

    public ItemViewer(Context context) {
        super(context);
        init(context);
    }

    public ItemViewer(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public void init(Context context) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.list_item, this, true);

        itemName = findViewById(R.id.itemName);
        itemIcon = findViewById(R.id.itemIcon);
    }

    public void setItem(ListItem item) {
        itemName.setText(item.getItemName());
        itemIcon.setImageResource(item.getItemIcon());
        itemID = item.getItemID();
    }
}
