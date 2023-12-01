package kr.jeet.edu.manager.model.data;

public class MainMenuItemData {
    private int imgRes;
    private int titleRes;
    private String type;
    private String title;
//    private boolean isMemberOnly = true;
    private Class<?> targetClass;

    public MainMenuItemData(String type, int img_res, int title_res, Class<?> cls) {
        this.type = type;
        this.imgRes = img_res;
        this.titleRes = title_res;
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

    public Class<?> getTargetClass() {
        return targetClass;
    }
}
