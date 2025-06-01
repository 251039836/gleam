package gleam.config.define;

/**
 * 地区<br>
 * 1个地区可能包含多种语言<br>
 * 地区实际对应的是某个发行渠道 某个独立运营的后台+网关+跨服体系<br>
 * 
 * 
 * @author hdh
 *
 */
public enum Regions {
    /**
     * 中国大陆
     */
    CHINESE_MAINLAND(1, Languages.SIMPLIFIED_CHINESE),
    /**
     * 新加坡和马来西亚
     */
    SINGAPORE_AND_MALAYSIA(2, Languages.SIMPLIFIED_CHINESE),
    /**
     * (一星)越南
     */
    VIETNAME(3, Languages.VIETNAMESE),

    ;
    public static Regions valueOf(int id) {
        for (Regions region : values()) {
            if (region.id == id) {
                return region;
            }
        }
        return null;
    }

    /**
     * 地区id
     */
    private final int id;
    /**
     * 主要语言
     */
    private final Languages mainLanguage;

    /**
     * 使用的所有语言<br>
     * 含主要语言
     */
    private final Languages[] languages;

    private Regions(int id, Languages mainLanguage) {
        this.id = id;
        this.mainLanguage = mainLanguage;
        this.languages = new Languages[] { mainLanguage };
    }

    private Regions(int id, Languages mainLanguage, Languages... languages) {
        this.id = id;
        this.mainLanguage = mainLanguage;
        this.languages = new Languages[languages.length + 1];
        this.languages[0] = mainLanguage;
        for (int i = 0; i < languages.length; i++) {
            this.languages[i + 1] = languages[i];
        }
    }

    public int getId() {
        return id;
    }

    public int[] getLanguageIds() {
        int[] languageIds = new int[languages.length];
        for (int i = 0; i < languageIds.length; i++) {
            languageIds[i] = languages[i].getId();
        }
        return languageIds;
    }

    public Languages[] getLanguages() {
        return languages;
    }

    public Languages getMainLanguage() {
        return mainLanguage;
    }

    public int getMainLanguageId() {
        return mainLanguage.getId();
    }

    /**
     * 该地区是否包含了该语言
     * 
     * @param languageId
     * @return
     */
    public boolean isIncludeLanguage(int languageId) {
        for (Languages language : languages) {
            if (language.getId() == languageId) {
                return true;
            }
        }
        return false;
    }

}
