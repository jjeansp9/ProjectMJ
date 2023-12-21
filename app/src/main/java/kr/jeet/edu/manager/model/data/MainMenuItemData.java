package kr.jeet.edu.manager.model.data;

public class MainMenuItemData {
    private int imgRes;
    private int titleRes;   //기본적으로 Resource 의 String을 보여줌
    private String type;
    private String title;   //서버에서 받은 board Attribute 내 해당 Type의 이름을 update 함
//    private boolean isMemberOnly = true;
    private boolean isNew;
    private Class<?> targetClass;

    public MainMenuItemData(String type, int img_res, int title_res, boolean isNew,  Class<?> cls) {
        this.type = type;
        this.imgRes = img_res;
        this.titleRes = title_res;
        this.isNew = isNew;
        this.targetClass = cls;
//        this.isMemberOnly = only_member;
    }

    public String getType() {
        return type;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getImgRes() {
        return imgRes;
    }

    public int getTitleRes() {
        return titleRes;
    }

//    public boolean isMemberOnly() {
//        return isMemberOnly;
//    }
    public void setIsNew(boolean isNew) {this.isNew = isNew;}
    public boolean getIsNew() {return isNew;}

    public Class<?> getTargetClass() {
        return targetClass;
    }
}
