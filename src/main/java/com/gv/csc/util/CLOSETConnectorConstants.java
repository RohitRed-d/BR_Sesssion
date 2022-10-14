package com.gv.csc.util;

import java.io.File;

public class CLOSETConnectorConstants {

    /****** CLOSET Connector Rest constants start ******/
    public final static String CC_EXPIRY_JSON_KEY = "expiry";
    public final static String CC_EMAIL_JSON_KEY = "email";
    public final static String CC_USERID_JSON_KEY = "userId";
    public final static String CC_USER_NAME_JSON_KEY = "username";
    public final static String CC_TOKEN_JSON_KEY = "token";
    public final static String CC_STATUS_CODE_JSON_KEY = "statusCode";
    public final static String CC_STATUS_JSON_KEY = "status";
    public final static String CC_MESSAGE_JSON_KEY = "message";
    public final static String CC_STYLES_JSON_KEY = "styles";
    public final static String CC_GROUPS_JSON_KEY = "groups";
    public final static String CC_RENDERS_JSON_KEY = "renders";
    public final static String CC_COLORWAYS_JSON_KEY = "colorways";
    public final static String CC_STYLE_JSON_KEY = "style";
    public final static String CC_THUMBNAIL_JSON_KEY = "thumbnail";
    public final static String CC_LAST_RENDER_SEQ_NO_JSON_KEY = "lastRenderSeqNo";
    public final static String CC_ZPRJ_FILE_NAME = "zprjFileName";
    public final static String CC_ZPRJ_FILE_URL = "zprjFileUrl";
    public final static String CC_RENDER_FILE_NAME = "renderFileName";
    public final static String CC_THUMBNAIL_FILE_NAME = "thumbnailFileName";
    public final static String CC_RENDER_FILE_URL = "renderFileUrl";
    public final static String CC_COUNT_JSON_KEY = "count";
    public final static String CC_URL_JSON_KEY = "url";
    public final static String CC_FAILED_STATUS_JSON_VALUE = "Failed";
    public final static String CC_SUCCESS_STATUS_JSON_VALUE = "Success";
    /****** CLOSET Connector Rest constants end ******/

    /****** CLOSET Connector constants start ******/
    public final static String AMPERSAND = "&";
    public final static String QUESTION_MARK = "?";
    public final static String ASSIGN = "=";
    public final static String COLON = ":";
    public final static String SPACE = " ";
    public final static String UNDERSCORE = "_";
    public final static String FORWARD_SLASH = "/";
    public final static String ZIP_EXTENSION = "zip";
    public final static String PNG_EXTENSION = "png";
    public final static String PERIOD = ".";

    public final static String STORAGE_DIRECTORY = new File("").getAbsolutePath() + File.separator + "src"
            + File.separator + "main" + File.separator + "resources" + File.separator + "static" + File.separator
            + "images" + File.separator + "storageDirectory" + File.separator;
    /****** CLOSET Connector constants end ******/

    public final static String STATIC_ASSETS_CONTROLLER = "img";

    public final static long TOKEN_EXPIRY_TIME = 21600000;
    public static final String CC_URLS_JSON_KEY = "plmUrls";
}
