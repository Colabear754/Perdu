package kr.ac.pknu.perdu.itemlist;

public class ListItem {
    // 리스트 아이템 객체를 생성하기 위한 클래스
    private String itemName;
    private int itemID;
    private int itemIcon;

    public ListItem(String name, int code, int icon) {
        itemName = name;
        itemID = code;
        itemIcon = icon;
    }

    public String getItemName() {
        return itemName;
    }

    public int getItemID() {
        return itemID;
    }

    public int getItemIcon() {
        return itemIcon;
    }
}
