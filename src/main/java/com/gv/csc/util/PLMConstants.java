package com.gv.csc.util;

import java.util.ArrayList;

public class PLMConstants {

    /****** CLOSET Configuration start ******/
    public static final String PLM_CONFIG = "Config.json";
    /****** CLOSET Configuration end ******/

    /****** CLOSET Rest constants start ******/
    public final static String API_HEADER_VERSION_KEY = "apiversion";
    public final static String API_HEADER_TOKEN_KEY = "t";
    public final static String PLM_AUTH_TOKEN = "plm-auth-token";
    public final static String API_HEADER_VERSION_VALUE = "1.0";
    public final static String API_HEADER_USER_ID_KEY = "userid";
    public final static String API_HEADER_PASSWORD_ID_KEY = "password";
    public final static String PLM_LOGIN_CS_PARAM_KEY = "cs";
    public final static String PLM_LOGIN_CS_PARAM_VALUE = "CLO3D";
    public final static String PLM_LOGIN_EXPOFFSET_PARAM_KEY = "expoffset";
    public final static String CLOSET_SEARCH_TERM_PARAM_KEY = "searchterm";
    public final static String CLOSET_OWNER_PARAM_KEY = "owner";
    public final static String CLOSET_REQUEST_NO_PARAM_KEY = "request_no";
    public final static String CLOSET_ASSOC_ID_PARAM_KEY = "ASSOC_ID";
    public final static String PLM_DOCUMENT_JSON_KEY = "document";
    public final static String PLM_MESSAGE_JSON_KEY = "message";
    public final static String PLM_MESSAGES_JSON_KEY = "messages";
    public final static String PLM_STATUS_JSON_KEY = "@status";
    public final static String PLM_MESSAGE_ID_JSON_KEY = "@message_id";
    public final static String PLM_MESSAGE_DESC_JSON_KEY = "@message_desc";
    public final static String PLM_TOKEN_JSON_KEY = "@token";
    public final static String PLM_USER_JSON_KEY = "user";
    public final static String PLM_TECH_SPEC_JSON_KEY = "techspec";
    public final static String PLM_ATTACHMENT_JSON_KEY = "attachment";
    public final static String PLM_COLORWAYS_JSON_KEY = "colorways";
    public final static String PLM_ATTACHMENT_NO_JSON_KEY = "@attachment_no";
    public final static String PLM_LOCATION_JSON_KEY = "@location";
    public final static String PLM_ASSOC_ID_JSON_KEY = "@assoc_id";
    public final static String PLM_ATTACHMENT_NO_FOR_THUMNAIL = "THUMB_1";
	public final static String PLM_RENDERS_PREFIX = "CLOIMAGE_";
    public final static String PLM_ATTACHMENT_NO_FOR_ZPRJ = "CLO_1";
    public final static String PLM_ATTACHMENT_NO_FOR_COLORWAY_THUMNAIL = "IMAGE";
    public final static String PLM_USER_NAME_JSON_KEY = "@user_name";
    public final static String PLM_DEPARTMENT_CODES_JSON_KEY = "dept_codes";
    public final static String PLM_BRAND_CODES_JSON_KEY = "brand_codes";
    public final static String PLM_DIVISION_CODES_JSON_KEY = "division_code";
    public final static String PLM_CODE_JSON_KEY = "@code";
    public final static String PLM_CODE_PARAM_KEY = "code";
    public final static String PLM_DESCRIPTION_JSON_KEY = "@description";
    public final static String PLM_SUCCESS_STATUS_VALUE = "SUCCESS";
    /****** CLOSET Rest constants end ******/
    //public static final String PLM_BASE_URL = "https://qaazure.bamboorose.com/qa2021r1";
    //public static final String PLM_BASE_URL = "https://azure-ts.bamboorose.com/qa2022r1";
    public static final String LOGIN_URI = "/csi-requesthandler/api/v2/session";
    public static final String SEARCH_STYLE_URI = "/rest/techspec/searchStyles";
    public static final String GET_STYLE_URI = "/rest/techspec/searchStyleWithKeys";
    public static final String DOWNLOAD_ATTACHMENT_URI = "/imagestore/";

    public static final String POST_STYLE_URI = "/rest/techspec/postStyle";
    public static final String UPLOAD_ATTACHMENT_URI = "/rest/attachment/upload/plugin";
    public static final String DEPARTMENT_SEARCH_RESULTS_API = "/rest/referencedata/searchDepts";
    public static final String DIVISION_SEARCH_RESULTS_API = "/rest/referencedata/searchDivisionCodes";
    public static final String BRAND_SEARCH_RESULTS_API = "/rest/referencedata/searchBrands";

    public static final String PLM_PUBLISH_DATA_KEY = "publishdata";
    public static final String PLM_LOCATION_KEY = "location";
    public static final String CLOSET_STYLE_DETAILS = "closetStyleDetails";
    public static final String CLOSET_ASSETS = "closetAssets";
    public static final String PROJECT_FILE_DETAILS = "projectFileDetails";

    public static final String THUMBNAIL_DETAILS = "thumbnailDetails";
    public static final String RENDERS_DETAILS = "rendersDetails";
    public static final String FILENAME = "fileName";
    public static final String FILE_URL = "fileUrl";
    public static final String PLM_OLD_NAME_JSON_KEY = "@oldname";
    public static final String PLM_NEW_NAME_JSON_KEY = "@newname";
    public static final String COLORWAY_DETAILS = "colorwayDetails";
    public static final String CLOSET_COLORWAY = "closetColorway";

    public static final String PLM_COLORWAY = "plmColorway";
    public static final String COLOR_ID = "colorId";
    public static final String COLOR_NAME = "colorName";
    public static final String COLORWAY_NAME = "colorwayName";

    public static final String ASSOC_ID = "assocId";
    public static final String PLM_COLOR_NO_KEY = "@color_no";
    public static final String PLM_COLOR_NAME_KEY = "@color_name";
    public static final String PLM_COLORWAY_NAME_KEY = "@colorway_name";
    public static final String PLM_STYLE_DETAILS = "plmStyleDetails";

    public static final String PLM_REQUEST_NO_KEY = "@request_no";
    public static final String COLORWAYS = "colorways";
    public static final String QUOTE = "quote";
    public static final String REQUEST_NO = "requestNo";
    public static final String RENDER_SEQ_NO_JSON_KEY = "sequenceNo";

    public static final String DEPT = "@dept";
    public static final String BRAND = "@brand";
    public static final String DIVISION = "@division";
    public static final String TEMP = "temp";

    public static final ArrayList<String> PLM_ATT_KEYS = new ArrayList<String>() {{
        add("@dept");
        add("@division");
        add("@brand");
    }};

}
