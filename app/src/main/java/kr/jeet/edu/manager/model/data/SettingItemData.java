package kr.jeet.edu.manager.model.data;

import androidx.annotation.NonNull;

public class SettingItemData implements Cloneable{
    public int seq;     //seq
    public int memberSeq;   //memberSeq
    public String settingsType; //settingsType
    public String settingItemName;
    public int value;
    public String insertDate;   //insertDate
    public String updateDate;   //updateDate

    @NonNull
    @Override
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }
}
