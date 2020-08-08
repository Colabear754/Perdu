package kr.ac.pknu.perdu;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
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
    ItemAdapter emotionAdapter, poseAdapter;
    private int selectedMode = 100001;
    private static final int EMOTION = 100001;
    private static final int POSE = 200001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        Display display = ((WindowManager) getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        screenWidth = display.getWidth();
        screenHeight = display.getHeight();

        itemList = findViewById(R.id.itemList);
        emotionAdapter = new ItemAdapter();
        poseAdapter = new ItemAdapter();

        // 샘플 코드
        emotionAdapter.addItem(new ListItem("표정1", 101, R.drawable.default_icon, EMOTION));
        emotionAdapter.addItem(new ListItem("표정2", 102, R.drawable.default_icon, EMOTION));
        emotionAdapter.addItem(new ListItem("표정3", 103, R.drawable.default_icon, EMOTION));
        emotionAdapter.addItem(new ListItem("표정4", 104, R.drawable.default_icon, EMOTION));
        emotionAdapter.addItem(new ListItem("표정5", 105, R.drawable.default_icon, EMOTION));
        emotionAdapter.addItem(new ListItem("표정6", 106, R.drawable.default_icon, EMOTION));
        emotionAdapter.addItem(new ListItem("표정7", 107, R.drawable.default_icon, EMOTION));
        emotionAdapter.addItem(new ListItem("표정8", 108, R.drawable.default_icon, EMOTION));

        switch (selectedMode) {
            // selectedMode 값에 따라 아이템리스트 어댑터를 변경
            case EMOTION:
                itemList.setAdapter(emotionAdapter);
                break;
            case POSE:
                itemList.setAdapter(poseAdapter);
                break;
        }

        itemList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent();
                // 아이템을 선택하면 해당 아이템 객체를 메인 액티비티로 전달하고 설정 액티비티 종료
                switch (selectedMode) {
                    // selectedMode 값에 따라 표정 또는 자세의 객체를 메인 액티비티에 전송
                    case EMOTION:
                        intent.putExtra("item", emotionAdapter.getItem(position));
                        break;
                    case POSE:
                        intent.putExtra("item", poseAdapter.getItem(position));
                        break;
                }
                setResult(RESULT_OK, intent);
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
        selectedMode = EMOTION;
    }

    public void onPoseButton(View v) {
        selectedMode = POSE;
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