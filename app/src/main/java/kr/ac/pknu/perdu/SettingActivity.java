package kr.ac.pknu.perdu;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;

import java.util.ArrayList;

import kr.ac.pknu.perdu.itemlist.ItemViewer;
import kr.ac.pknu.perdu.itemlist.ListItem;

public class SettingActivity extends AppCompatActivity {
    private int screenWidth, screenHeight;
    private int emotionID, poseID;
    GridView itemList;
    ItemAdapter itemAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        Display display = ((WindowManager) getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        screenWidth = display.getWidth();
        screenHeight = display.getHeight();

        itemList = findViewById(R.id.itemList);
        itemAdapter = new ItemAdapter();

        // 샘플 코드
        itemAdapter.addItem(new ListItem("표정1", 10001, R.drawable.default_icon));
        itemAdapter.addItem(new ListItem("표정2", 10002, R.drawable.default_icon));
        itemAdapter.addItem(new ListItem("표정3", 10003, R.drawable.default_icon));
        itemAdapter.addItem(new ListItem("표정4", 10004, R.drawable.default_icon));
        itemAdapter.addItem(new ListItem("표정5", 10005, R.drawable.default_icon));
        itemAdapter.addItem(new ListItem("표정6", 10006, R.drawable.default_icon));
        itemAdapter.addItem(new ListItem("표정7", 10007, R.drawable.default_icon));
        itemAdapter.addItem(new ListItem("표정8", 10008, R.drawable.default_icon));

        itemList.setAdapter(itemAdapter);

        itemList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // 아이템을 선택하면 해당 아이템의 ID를 변수에 저장하고 메인 액티비티에 결과를 전송한 후 설정 액티비티 종료
                emotionID = itemAdapter.getItem(position).getItemID();
                setResult(emotionID);
                finish();
            }
        });
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int x, y;
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            x = (int) event.getX();
            y = (int) event.getY();

            Bitmap bitmap = Bitmap.createBitmap(screenWidth, screenHeight, Bitmap.Config.ARGB_8888);    // 터치한 부분의 색상코드를 추출하기 위한 비트맵

            if (x < 0 || y < 0)
                return false;

            int ARGB = bitmap.getPixel(x, y);

            if (Color.alpha(ARGB) != 255) {     // 터치한 공간의 알파값이 255가 아니면 액티비티를 종료
                setResult(RESULT_CANCELED);
                finish();
            }
            return true;
        }
        return false;
    }

    public void onEmotionButton(View v) {

    }

    public void onPoseButton(View v) {

    }

    class ItemAdapter extends BaseAdapter {
        // 표정, 자세 아이템을 표시하기 위한 어댑터
        ArrayList<ListItem> items = new ArrayList<ListItem>();

        @Override
        public int getCount() {
            return items.size();
        }

        public void addItem(ListItem item) {
            items.add(item);
        }

        @Override
        public ListItem getItem(int position) {
            return items.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ItemViewer itemViewer = new ItemViewer(getApplicationContext());
            itemViewer.setItem(items.get(position));
            return itemViewer;
        }
    }

}