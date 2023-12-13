package kr.jeet.edu.manager.common;

import android.util.ArrayMap;

import java.util.ArrayList;
import java.util.List;

import kr.jeet.edu.manager.model.data.ACAData;
import kr.jeet.edu.manager.model.data.BoardAttributeData;
import kr.jeet.edu.manager.model.data.LTCData;
import kr.jeet.edu.manager.model.data.RecipientData;
import kr.jeet.edu.manager.model.data.SchoolData;
import kr.jeet.edu.manager.model.data.SettingItemData;
import kr.jeet.edu.manager.model.data.StudentGradeData;

/**
 * 앱에서 사용하는 JEET 관련 정보들을 저장하는 클래스
 */
public class DataManager {
    private static final String TAG = "dataMgr";
    
    public static DataManager getInstance() {
        return DataManager.LazyHolder.INSTANCE;
    }
    private static class LazyHolder {
        private static final DataManager INSTANCE = new DataManager();
    }
    // 공지사항
    //서버에서 주는 항목
    public static final String BOARD_NOTICE = "notice";    //공지사항
    public static final String BOARD_PT = "pt";    //설명회
    public static final String BOARD_SYSTEM_NOTICE = "systemNotice";   //알림
    public static final String BOARD_LEVELTEST = "levelTest";  //테스트예약
    public static final String BOARD_REPORT = "report";    //성적표
    public static final String BOARD_QNA = "qna";  //Q&A
    ////서버에서 주지 않는 항목
    public static final String BOARD_STUDENT_INFO = "studentinfo"; //원생정보
    public static final String BOARD_BUS = "bus";  //차량정보
    public static final String BOARD_SCHEDULE = "schedule";
    //Dot size
    public float DOT_SIZE = 10f;
    // 캠퍼스 리스트
//    private List<ACAData> ACAList = new ArrayList<>();
    private ArrayMap<String, ACAData> ACAListMap = new ArrayMap<>();
    //지역별 캠퍼스 리스트
    private ArrayMap<String, ACAData> LocalACAListMap = new ArrayMap<>();
    //지역캠퍼스 등급 리스트
    private ArrayMap<String, StudentGradeData> LocalGradeListMap = new ArrayMap<>();
    //레벨테스트용 캠퍼스 리스트
    private ArrayMap<String, LTCData> LTCListMap = new ArrayMap<>();
    //학교 리스트
    private ArrayMap<Integer, SchoolData> SchoolListMap = new ArrayMap<>();
    // 게시판 정보 리스트
    private ArrayMap<String, BoardAttributeData> BoardListMap = new ArrayMap<>();
    //수신인 리스트 (등록 후 삭제됨)
    private ArrayList<RecipientData> RecipientList = new ArrayList<>();
    //Setting 리스트
    private ArrayMap<String, SettingItemData> SettingListMap = new ArrayMap<>();

    public ArrayMap<String, ACAData> getACAListMap() {
        return ACAListMap;
    }
    public void setACAListMap(ArrayMap<String, ACAData> map) {
        this.ACAListMap =  map;
    }
    public boolean initACAListMap(List<ACAData> list)
    {
        if(list == null) return false;
        if(!ACAListMap.isEmpty()) ACAListMap.clear();
        for(ACAData item : list) {
            String key = item.acaCode;
            if (!ACAListMap.containsKey(key)) {
                ACAListMap.put(key, item);
            }
        }
        return true;
    }
    public ACAData getACAData(String acaCode) {
        if(ACAListMap.containsKey(acaCode)) {
            return ACAListMap.get(acaCode);
        }
        return null;
    }
    public boolean setACAData(ACAData data) {
        if(data == null) return false;
        String key = data.acaCode;
        if(!ACAListMap.containsKey(key)) {
            ACAListMap.put(key, data);
            return true;
        }
        return false;
    }

    public ArrayMap<String, ACAData> getLocalACAListMap() {
        return LocalACAListMap;
    }
    public void setLocalACAListMap(ArrayMap<String, ACAData> map) {
        this.LocalACAListMap =  map;
    }
    public boolean initLocalACAListMap(List<ACAData> list)
    {
        if(list == null) return false;
        if(!LocalACAListMap.isEmpty()) LocalACAListMap.clear();
        for(ACAData item : list) {
            String key = item.acaCode;
            if (!LocalACAListMap.containsKey(key)) {
                LocalACAListMap.put(key, item);
            }
        }
        return true;
    }
    public ACAData getLocalACAData(String acaCode) {
        if(LocalACAListMap.containsKey(acaCode)) {
            return LocalACAListMap.get(acaCode);
        }
        return null;
    }
    public boolean setLocalACAData(ACAData data) {
        if(data == null) return false;
        String key = data.acaCode;
        if(!LocalACAListMap.containsKey(key)) {
            LocalACAListMap.put(key, data);
            return true;
        }
        return false;
    }

    public ArrayMap<String, LTCData> getLTCListMap() {
        return LTCListMap;
    }
    public void setLTCListMap(ArrayMap<String, LTCData> map) {
        this.LTCListMap =  map;
    }
    public boolean initLTCListMap(List<LTCData> list)
    {
        if(list == null) return false;
        if(!LTCListMap.isEmpty()) LTCListMap.clear();
        for(LTCData item : list) {
            String key = item.ltcCode;
            if (!LTCListMap.containsKey(key)) {
                LTCListMap.put(key, item);
            }
        }
        return true;
    }
    public LTCData getLTCData(String ltcCode) {
        if(LTCListMap.containsKey(ltcCode)) {
            return LTCListMap.get(ltcCode);
        }
        return null;
    }
    public boolean setLTCData(LTCData data) {
        if(data == null) return false;
        String key = data.ltcCode;
        if(!LTCListMap.containsKey(key)) {
            LTCListMap.put(key, data);
            return true;
        }
        return false;
    }

    public ArrayMap<Integer, SchoolData> getSchoolListMap() {
        return SchoolListMap;
    }
    public void setSchoolListMap(ArrayMap<Integer, SchoolData> map) {
        this.SchoolListMap =  map;
    }
    public boolean initSchoolListMap(List<SchoolData> list)
    {
        if(list == null) return false;
        if(!SchoolListMap.isEmpty()) SchoolListMap.clear();
        for(SchoolData item : list) {
            int key = item.scCode;
            if (!SchoolListMap.containsKey(key)) {
                SchoolListMap.put(key, item);            }
        }
        return true;
    }
    public SchoolData getSchoolData(int scCode) {
        if(SchoolListMap.containsKey(scCode)) {
            return SchoolListMap.get(scCode);
        }
        return null;
    }
    public boolean setSchoolData(SchoolData data) {
        if(data == null) return false;
        int key = data.scCode;
        if(!SchoolListMap.containsKey(key)) {
            SchoolListMap.put(key, data);
            return true;
        }
        return false;
    }

    public ArrayMap<String, BoardAttributeData> getBoardInfoArrayMap() {
        return BoardListMap;
    }
    public void setBoardInfoMap(ArrayMap<String, BoardAttributeData> map) {
        this.BoardListMap =  map;
    }
    public BoardAttributeData getBoardInfo(String boardType) {
        if(BoardListMap.containsKey(boardType)) {
            return BoardListMap.get(boardType);
        }
        return null;
    }
    public boolean setBoardInfo(BoardAttributeData data) {
        if(data == null) return false;
        String key = data.boardType;
        if(!BoardListMap.containsKey(key)) {
            BoardListMap.put(key, data);
            return true;
        }
        return false;
    }
    //settings
    public ArrayMap<String, SettingItemData> getSettingItemList() {
        return SettingListMap;
    }
    public void setSettingListMap(ArrayMap<String, SettingItemData> map) {
        this.SettingListMap =  map;
    }
    public boolean initSettingListMap(List<SettingItemData> list)
    {
        if(list == null) return false;
        if(!SettingListMap.isEmpty()) SettingListMap.clear();
        for(SettingItemData item : list) {
            item.settingItemName = item.settingsType.replaceAll("[0-9]", "");
            String key = item.settingItemName;
            if (!SettingListMap.containsKey(key)) {
                SettingListMap.put(key, item);
            }
        }
        return true;
    }
    public SettingItemData getSettingItemData(String itemType) {
        if(SettingListMap.containsKey(itemType)) {
            return SettingListMap.get(itemType);
        }
        return null;
    }
    public boolean setSettingItemData(SettingItemData data) {
        if(data == null) return false;
        data.settingItemName = data.settingsType.replaceAll("[^0-9]", "");
        String key = data.settingItemName;
        if(!SettingListMap.containsKey(key)) {
            SettingListMap.put(key, data);
            return true;
        }
        return false;
    }

    public ArrayList<RecipientData> getRecipientList() {
        return RecipientList;
    }
}
