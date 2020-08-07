package kr.ac.pknu.perdu.itemlist;

import android.os.Parcel;
import android.os.Parcelable;

public class ListItem implements Parcelable {
    // 리스트 아이템 객체를 생성하기 위한 클래스
    private String itemName;
    private int itemID;
    private int itemIcon;
    private int mode;

    public ListItem(String name, int code, int icon, int selectedMode) {
        itemName = name;
        itemID = code;
        itemIcon = icon;
        mode = selectedMode;
    }

    protected ListItem(Parcel in) {
        itemName = in.readString();
        itemID = in.readInt();
        itemIcon = in.readInt();
        mode = in.readInt();
    }

    public static final Creator<ListItem> CREATOR = new Creator<ListItem>() {
        @Override
        public ListItem createFromParcel(Parcel in) {
            return new ListItem(in);
        }

        @Override
        public ListItem[] newArray(int size) {
            return new ListItem[size];
        }
    };

    public String getItemName() {
        return itemName;
    }

    public int getItemID() {
        return itemID;
    }

    public int getItemIcon() {
        return itemIcon;
    }

    public int getMode() {
        return mode;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(itemName);
        dest.writeInt(itemID);
        dest.writeInt(itemIcon);
        dest.writeInt(mode);
    }
}
