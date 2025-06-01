package gleam.config.define;

import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Map;

import com.google.common.collect.Maps;

import gleam.util.time.DateFormatUtils;

/**
 * 语言<br>
 * 一个地区可能有多种语言
 * 
 * @author hdh
 *
 */
public enum Languages {
    /**
     * 简体中文
     */
    SIMPLIFIED_CHINESE(0),
    /**
     * 繁体中文
     */
    TRADITIONAL_CHINESE(1),
    /**
     * 英文
     */
    ENGLISH(2),
    /**
     * 越南文
     */
    VIETNAMESE(3, DateFormatUtils.VIETNAME_DATE_FORMATTER),;

    private static final Map<Integer, Languages> languageMap = Maps.uniqueIndex(Arrays.asList(Languages.values()), t -> {
        return t.getId();
    });

    public static Languages valueOf(int type) {
        return languageMap.get(type);
    }

    private final int id;

    /**
     * 日期格式<br>
     * 只包含年月日 不含时分秒
     */
    private final DateTimeFormatter dateFormatter;

    private Languages(int id) {
        this(id, DateFormatUtils.DATE_FORMATTER);
    }

    private Languages(int id, DateTimeFormatter dateFormatter) {
        this.id = id;
        this.dateFormatter = dateFormatter;
    }

    public DateTimeFormatter getDateFormatter() {
        return dateFormatter;
    }

    public int getId() {
        return id;
    }

}
