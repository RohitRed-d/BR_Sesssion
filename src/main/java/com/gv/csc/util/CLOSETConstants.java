package com.gv.csc.util;

import java.io.File;

public class CLOSETConstants {
	/****** CLOSET Configuration start ******/
	public final static String CLOSET_CONFIG = "CLOSETConfig.json";
	/****** CLOSET Configuration end ******/


	/****** CLOSET Rest constants start ******/
	public final static String CLOSET_AUTH_TOKEN = "closet-auth-token";
	public final static String CLO_SET_EMAIL = "clo-set-email";
	public final static String PLM_USER_NAME = "plm-user-name";
	public final static String X_USER_EMAIL = "X-User-Email";
	public final static String API_VERSION = "api-version";
	public final static String HI_RES_RENDERED_IMAGES = "hi-res-rendered-images";
	public final static String VERSION_2_0 = "2.0";
	public final static String CLOSET_EMAIL_PARAM_KEY = "email";
	public final static String CLOSET_PASSWORD_PARAM_KEY = "password";
	public final static String PLM_MESSAGE_JSON_KEY = "message";
	public final static String MESSAGE_JSON_KEY = "Message";
	public final static String PLM_CODE_JSON_KEY = "code";
	public final static String BEARER_PREFIX = "Bearer ";
	public final static String CLOSET_GROUP_ID_PARAM_KEY = "groupId";
	public final static String CLOSET_PAGE_SIZE_PARAM_KEY = "pageSize";
	public final static String CLOSET_KEYWORD_PARAM_KEY = "keyword";
	public final static String CLOSET_SEARCH_FILTER_PARAM_KEY = "searchFilter";
	public final static String CLOSET_VERSIONS_PARAM_KEY = "versions";
	public final static String CLOSET_RENDERS_PARAM_KEY = "renders";
	public final static String CLOSET_VERSION_PARAM_KEY = "version";
	public final static String CLOSET_RENDER_SEQUENCE_KEY = "renderSeq";
	public final static String CLOSET_STYLE_ID_PARAM_KEY = "styleId";
	public final static String CLOSET_CONVERT_RES_URL_PARAM_KEY = "convertResourceUrl";

	public final static String CLOSET_GROUP_ID_JSON_KEY = "groupId";
	public final static String CLOSET_TECHPACK_COLORWAYS_KEY = "colorwayList";
	public static final String RESET_EXTERNAL_STYLE_ID = "resetExternalStyleId";

	public final static String CLOSET_TECHPACK_FABRIC_LIST_KEY = "fabricList";
	public final static String CLOSET_TECHPACK_MATERIAL_FRONT_KEY = "materialFront";
	public final static String CLOSET_TECHPACK_BASE_COLOR_NAME_KEY = "baseColorName";
	public final static String CLOSET_TECHPACK_CLOVISE_COLOR_ID_KEY = "CLOVISE_COLOR_ID";
	public final static String CLOSET_TECHPACK_NAME_KEY = "name";
	public final static String CLOSET_TECHPACK_API_META_DATA_KEY = "apiMetaData";
	public static final String EXTERNAL_STYLE_ID = "externalStyleId";

	/****** CLOSET Rest constants end ******/

	/****** CLOSET Rest Endpoints start ******/
	public static final String CLOSET_BASE_URL = "https://www.clo-set.com/api";
	public final static String CLOSET_GROUPS_END_POINT = "/groups";
	public final static String CLOSET_LOGIN_END_POINT = "/auth/token";
	public final static String CLOSET_STYLE_SEARCH_END_POINT = "/styles";
	public final static String CLOSET_GET_STYLE_END_POINT = "/styles/";
	public final static String CLOSET_GET_TECHPACK_END_POINT = "/styles/";
	public final static String CLOSET_GET_TECHPACK_END_POINT_SUFFIX = "/techpackJson";
	public final static String CLOSET_GET_STYLE_RENDER_URL_END_POINT = "/styles/";
	public final static String CLOSET_GET_STYLE_RENDERS_END_POINT = "/styles/styleId/versions/version/renders";
	public final static String CLOSET_GET_ZPRJ_FILE_END_POINT = "/downloadUrl";
	public final static String CLOSET_GET_ZIP_FILE_END_POINT = "/styles/styleId/renders/renderSeq/downloadUrl";


	/****** CLOSET Rest Endpoints end ******/

}
