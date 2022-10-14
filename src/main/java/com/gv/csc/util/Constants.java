package com.gv.csc.util;

import java.io.File;

public class Constants {
	// Configuration Files
	public static String ANGULAR_CONFIG = "AngularConfig.json";
	public static String CLOSET_CONFIG = "CLOSETConfig.json";
	public static String PLM_CONFIG = "Config.json";

	// CLO-SET Exception messages
	public static String CLOSET_UNAUTHORIZED_ACCESS = "CLOSET_UNAUTHORIZED_ACCESS";
	public static String CLOSET_FORBIDDEN_ACCESS = "CLOSET_FORBIDDEN_ACCESS";
	public static String CLOSET_RESOURCE_NOT_FOUND = "CLOSET_RESOURCE_NOT_FOUND";
	public static String CLOSET_SERVER_DOWN = "CLOSET_SERVER_DOWN";
	public static String CLOSET_CONFLICT = "CLOSET_CONFLICT";
	public static String CLOSET_INTERNAL_ERROR = "CLOSET_INTERNAL_ERROR";

	// PLM Exception messages
	public static String PLM_UNAUTHORIZED_ACCESS = "PLM_UNAUTHORIZED_ACCESS";
	public static String PLM_FORBIDDEN_ACCESS = "PLM_FORBIDDEN_ACCESS";
	public static String PLM_RESOURCE_NOT_FOUND = "PLM_RESOURCE_NOT_FOUND";
	public static String PLM_SERVER_DOWN = "PLM_SERVER_DOWN";
	public static String PLM_CONFLICT = "PLM_CONFLICT";
	public static String PLM_INTERNAL_ERROR = "PLM_INTERNAL_ERROR";

	public static String CLOSET_AUTH_TOKEN = "closet-auth-token";
	public static String PLM_AUTH_TOKEN = "plm-auth-token";
	public static String CLO_SET_EMAIL = "clo-set-email";
	public static String X_USER_EMAIL = "X-User-Email";
	public static String API_VERSION = "api-version";
	public static String VERSION_2_0 = "2.0";
	
	//License
	public static final String LICENSE_NOT_VALID_MSG = "You are not authorized to access CLO-SET Connector. Please contact your system administrator.";
	public static final String USER_ALREADY_ACTIVE = "User is already active. Please log out of the CLO-Vise on other devices.";
	public static final String LICENSE_EXPIRED = "The license expiration date has passed.";
	public static final String LICENSE_LIMIT_EXCEEDED = "License limit exceeded. Please log out of the CLO-Vise on other devices. "
			+ "If license is currently not active on other devices, please try again in ten minutes. "
			+ "If you continue to have issues or cannot log out of other devices, please contact The CLO Support Team.";
	public static final String LICENSE_ERROR_CODE_101_MSG = "Company name is missing. Please verify PLM settings or contact your system administrator.";
	public static final String LICENSE_ERROR_CODE_102_MSG = "Company Api Key is missing. Please verify PLM settings or contact your system administrator.";
	public static final String RESULT_CODE = "result_code";
	public static final String PUBLIC_IP = "publicIp";
	public static final String COMPANY_NAME = "companyName";
	public static final String PRIVATE_IP = "privateIp";
	public static final String MACHINE_NAME = "machineName";
	public static final String USERID = "userId";
	public static final String LOGOUT_UNSUCCESSFUL_MSG = "You don't have active session.";
	public static final String LICENSE_LOGOUT_MSG = "User logged out successfully.";
	
	public final static String FILE_STORAGE_PATH = new File("").getAbsolutePath() + "/src";

	// common constants
	public final static String RESULT_TABLE_KEY = "";
	public final static String RESULT_TABLE_THUMBNAIL = "imagePath";

	public final static String staticFileStorage = new File("").getAbsolutePath() + "/src/main/resources/static/images";
	public final static String HI_RES_RENDERED_IMAGES = "hi-res-rendered-images";

	public final static String STATIC_ASSETS_CONTROLLER = "img";

	/**
	 * Storage related constants
	 */
	public final static String STORAGE_DIRECTORY = new File("").getAbsolutePath() + File.separator + "src"
			+ File.separator + "main" + File.separator + "resources" + File.separator + "static" + File.separator
			+ "images" + File.separator + "storageDirectory" + File.separator;

	public final static String THUMBNAIL = "thumbnail";
	public final static String RENDERED_IMAGES = "rendered-images";
	public final static String ZPRJ_FILE = "zprj-file";
	public final static String COLORWAY_IMAGES = "colorway-images";
	public final static String CLO_IMAGE_SEQ_PREFIX = "CLOIMAGE_";
}
