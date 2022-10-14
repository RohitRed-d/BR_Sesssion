package com.gv.csc.helper;

import com.gv.csc.entity.LastLoginTime;
import com.gv.csc.exceptions.CLOSETException;
import com.gv.csc.service.LastLoginTimeService;
import com.gv.csc.util.CLOSETConnectorConstants;
import com.gv.csc.util.CLOSETConstants;
import com.gv.csc.util.PLMConstants;
import com.gv.csc.util.Utility;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Map;

/**
 * CLOSETHelper - Helper class containing the helper logic for speaking with CLOSET APIs and preparing response
 */
public class CLOSETHelper {

    @Autowired
    private RestService restService;

    @Autowired
    private LastLoginTimeService lastLoginTimeService;
    
    static Logger logger = LoggerFactory.getLogger(CLOSETHelper.class);

    /**
     * prepareLoginResponse - Function calls CLOSET Rest API and prepares response
     *
     * @param username String username
     * @param password String password
     * @param groupToken Boolean groupToken
     * @return JSONObject with Rest response
     * @throws CLOSETException exception
     */
    public JSONObject prepareLoginResponse(String username, String password, Boolean groupToken) throws CLOSETException {
        logger.info("INFO::CLOSETHelper: prepareLoginResponse() started.");
        JSONObject outJson = new JSONObject();
        JSONArray urlArray = new JSONArray();
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
        headers.add(CLOSETConstants.API_VERSION, CLOSETConstants.VERSION_2_0);
        headers.add(CLOSETConstants.X_USER_EMAIL, username);
        try {

            if (groupToken) {
                headers.add(HttpHeaders.AUTHORIZATION, CLOSETConstants.BEARER_PREFIX + password);
                ResponseEntity<String> groupsResponse = restService.makeGetOrPostCall(CLOSETConstants.CLOSET_BASE_URL + CLOSETConstants.CLOSET_GROUPS_END_POINT,
                        HttpMethod.GET,
                        headers,
                        new JSONObject());
                logger.debug("groupsResponse :"+groupsResponse.getBody());
                HttpStatus statusCode = groupsResponse.getStatusCode();
                logger.debug("DEBUG::CLOSETHelper: prepareLoginResponse() statusCode - " + statusCode);

                if (groupsResponse.getStatusCodeValue() != 200) {
                    String errorJson = groupsResponse.getBody();
                    logger.error("errorJson :"+errorJson);
                    throw new CLOSETException(errorJson, statusCode);
                }

                outJson.put(CLOSETConnectorConstants.CC_EXPIRY_JSON_KEY, CLOSETConnectorConstants.TOKEN_EXPIRY_TIME);
                outJson.put(CLOSETConnectorConstants.CC_EMAIL_JSON_KEY, username);
                outJson.put(CLOSETConnectorConstants.CC_USERID_JSON_KEY, username);
                outJson.put(CLOSETConnectorConstants.CC_TOKEN_JSON_KEY, CLOSETConstants.BEARER_PREFIX + password);
                outJson.put(CLOSETConnectorConstants.CC_URLS_JSON_KEY,urlArray);
            } else {
                String uri = CLOSETConstants.CLOSET_BASE_URL + CLOSETConstants.CLOSET_LOGIN_END_POINT + CLOSETConnectorConstants.QUESTION_MARK + CLOSETConstants.CLOSET_EMAIL_PARAM_KEY + CLOSETConnectorConstants.ASSIGN + username + CLOSETConnectorConstants.AMPERSAND + CLOSETConstants.CLOSET_PASSWORD_PARAM_KEY + CLOSETConnectorConstants.ASSIGN + password;
                logger.debug("DEBUG::CLOSETHelper: prepareLoginResponse() uri - " + uri);
                ResponseEntity<String> loginResponseEntity = restService.makeGetOrPostCall(uri, HttpMethod.GET, headers, new JSONObject());

                String responseToken = loginResponseEntity.getBody();
                HttpStatus statusCode = loginResponseEntity.getStatusCode();

                logger.debug("DEBUG::CLOSETHelper: prepareLoginResponse() statusCode - " + statusCode);
                logger.debug("DEBUG::CLOSETHelper: prepareLoginResponse() responseToken - " + responseToken);

                if (loginResponseEntity.getStatusCodeValue() != 200) {
                    String errorJson = loginResponseEntity.getBody();
                    throw new CLOSETException(errorJson, statusCode);

                }

                outJson.put(CLOSETConnectorConstants.CC_EXPIRY_JSON_KEY, 1800000);
                outJson.put(CLOSETConnectorConstants.CC_EMAIL_JSON_KEY, username);
                outJson.put(CLOSETConnectorConstants.CC_USERID_JSON_KEY, username);
                outJson.put(CLOSETConnectorConstants.CC_TOKEN_JSON_KEY, CLOSETConstants.BEARER_PREFIX + responseToken);
                outJson.put(CLOSETConnectorConstants.CC_URLS_JSON_KEY,urlArray);
            }
        } catch (HttpClientErrorException restExc) {
            String excMessage = readErrorResponseMessage(restExc.getResponseBodyAsString());
            logger.error("ERROR::CLOSETHelper: prepareLoginResponse() excMessage - " + excMessage);
            throw new CLOSETException(excMessage, restExc.getStatusCode());
        } catch (CLOSETException closetExc) {
            closetExc.printStackTrace();
            throw closetExc;
        } catch (Exception exe) {
            exe.printStackTrace();
            throw new CLOSETException(exe.getLocalizedMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
        logger.info("INFO::CLOSETHelper: prepareLoginResponse() outJson - " + outJson);
        logger.info("INFO::CLOSETHelper: prepareLoginResponse() end.");
        return outJson;
    }

    /**
     * prepareErrorResponse - Function used to prepare Error JSON
     *
     * @param exception CLOSETException object
     * @return JSONObject prepares Error JSON consuming exception
     */
    public static JSONObject prepareErrorResponse(CLOSETException exception) {
        JSONObject errorJSON = new JSONObject();
        errorJSON.put(CLOSETConnectorConstants.CC_STATUS_CODE_JSON_KEY, exception.getStatusCode());
        errorJSON.put(CLOSETConnectorConstants.CC_STATUS_JSON_KEY, CLOSETConnectorConstants.CC_FAILED_STATUS_JSON_VALUE);
        String message = exception.getLocalizedMessage();
        errorJSON.put(CLOSETConnectorConstants.CC_MESSAGE_JSON_KEY, message);

        return errorJSON;
    }
    public static String readErrorResponseMessage(String excMessage) {
        String message = excMessage;
        JSONObject errorJson;
        if(Utility.isJSONArray(excMessage)) {
            errorJson = new JSONArray(excMessage).getJSONObject(0);
            message = readErrorResponseMessage(errorJson);
        } else if(Utility.isJSONObject(excMessage)) {
            errorJson = new JSONObject(excMessage);
            message = readErrorResponseMessage(errorJson);
        }

        logger.debug("DEBUG::CLOSETHelper: readErrorResponseMessage() uri message - " + message);
        return message;
    }
    public static String readErrorResponseMessage(JSONObject errorJson) {
        logger.error("errorJson  :"+errorJson);
        String message;
        if(errorJson.has(CLOSETConstants.PLM_CODE_JSON_KEY)) {
            message = errorJson.getInt(CLOSETConstants.PLM_CODE_JSON_KEY) + CLOSETConnectorConstants.COLON + CLOSETConnectorConstants.SPACE + errorJson.getString(CLOSETConstants.PLM_MESSAGE_JSON_KEY);
        } else {
            if(errorJson.equals(CLOSETConstants.PLM_MESSAGE_JSON_KEY)) {
                message = errorJson.getString(CLOSETConstants.PLM_MESSAGE_JSON_KEY);
            }else{
                message = errorJson.getString(CLOSETConstants.MESSAGE_JSON_KEY);
            }
        }
        logger.debug("DEBUG::CLOSETHelper: readErrorResponseMessage() uri message - " + message);
        return message;
    }

    /**
     * prepareSearchStyleResponse - Function calls CLOSET Rest API and prepares response
     * @param searchTerm String searchTerm
     * @param filter int filter
     * @param headers Map<String, String> headers
     * @return JSONObject with Rest response
     * @throws CLOSETException exception
     */
    public JSONObject prepareSearchStyleResponse(String searchTerm, int filter, Map<String, String> headers) throws CLOSETException {
        logger.debug("INFO::CLOSETHelper: prepareSearchStyleResponse() start.");
        JSONObject outJson = new JSONObject();
        try {
            HttpHeaders restHeaders = prepareClosetHeaders(headers);
            ResponseEntity<String> groupsResponse = restService.makeGetOrPostCall(CLOSETConstants.CLOSET_BASE_URL + CLOSETConstants.CLOSET_GROUPS_END_POINT, HttpMethod.GET, restHeaders, new JSONObject());

            HttpStatus statusCode = groupsResponse.getStatusCode();
            logger.debug("DEBUG::CLOSETHelper: prepareSearchStyleResponse() statusCode - " + statusCode);

            if (groupsResponse.getStatusCodeValue() != 200) {
                String errorJson = groupsResponse.getBody();
                throw new CLOSETException(errorJson, statusCode);
            }

            String responseGroupList = groupsResponse.getBody();
            logger.debug("DEBUG::CLOSETHelper: prepareSearchStyleResponse() responseGroupList - "+responseGroupList);

            if(!Utility.isJSONArray(responseGroupList)) {
                throw new CLOSETException("Error in parsing search results, please contact your System Administrator", HttpStatus.INTERNAL_SERVER_ERROR);
            }

            JSONArray groupListJSONArray = new JSONArray(responseGroupList);
            String groupId;
            String url;
            JSONArray stylesList = new JSONArray();
            JSONArray stylesListArray;
            for (int i = 0; i < groupListJSONArray.length(); i++) {
                groupId = new JSONArray(responseGroupList).getJSONObject(i).get(CLOSETConstants.CLOSET_GROUP_ID_JSON_KEY).toString();
                url = CLOSETConstants.CLOSET_BASE_URL + CLOSETConstants.CLOSET_STYLE_SEARCH_END_POINT + CLOSETConnectorConstants.QUESTION_MARK
                        + CLOSETConstants.CLOSET_GROUP_ID_PARAM_KEY + CLOSETConnectorConstants.ASSIGN + groupId + CLOSETConnectorConstants.AMPERSAND
                        + CLOSETConstants.CLOSET_PAGE_SIZE_PARAM_KEY + CLOSETConnectorConstants.ASSIGN + 100 + CLOSETConnectorConstants.AMPERSAND
                        + CLOSETConstants.CLOSET_KEYWORD_PARAM_KEY + CLOSETConnectorConstants.ASSIGN + searchTerm + CLOSETConnectorConstants.AMPERSAND
                        + CLOSETConstants.CLOSET_SEARCH_FILTER_PARAM_KEY + CLOSETConnectorConstants.ASSIGN + filter;
                        //"?groupId=" + groupId + "&pageSize=100" + "&keyword=" + searchTerm + "&searchFilter=" + filter;
                logger.debug("DEBUG::CLOSETHelper: prepareSearchStyleResponse() url - " + url);
                ResponseEntity<String> stylesResponseEntity = restService.makeGetOrPostCall(url, HttpMethod.GET, restHeaders, new JSONObject());

                statusCode = stylesResponseEntity.getStatusCode();
                logger.debug("DEBUG::CLOSETHelper: prepareSearchStyleResponse() statusCode - " + statusCode);

                if (stylesResponseEntity.getStatusCodeValue() != 200) {
                    String errorJson = stylesResponseEntity.getBody();
                    throw new CLOSETException(errorJson, statusCode);
                }

                String responseStyleList = stylesResponseEntity.getBody();

                if(!Utility.isJSONArray(responseStyleList)) {
                    throw new CLOSETException("Error in parsing search results, please contact your System Administrator", HttpStatus.INTERNAL_SERVER_ERROR);
                }

                stylesListArray = new JSONArray(responseStyleList);
                for (int j = 0; j < stylesListArray.length(); j++) {
                    stylesList.put(stylesListArray.getJSONObject(j));
                }
                if(stylesList.isEmpty()){
                    throw new CLOSETException("No Records Found. Try modifying the search term", HttpStatus.INTERNAL_SERVER_ERROR);
                }
                logger.debug("DEBUG::CLOSETHelper: prepareSearchStyleResponse() responseStyleList - "+responseStyleList);
            }
            outJson.put(CLOSETConnectorConstants.CC_STYLES_JSON_KEY, stylesList);
            outJson.put(CLOSETConnectorConstants.CC_COUNT_JSON_KEY, stylesList.length());

        } catch(HttpClientErrorException restExc) {
            String excMessage = readErrorResponseMessage(restExc.getResponseBodyAsString());
            logger.error("ERROR::CLOSETHelper: prepareSearchStyleResponse() excMessage - " + excMessage);
            throw new CLOSETException(excMessage, restExc.getStatusCode());
        }
        logger.debug("DEBUG::CLOSETHelper: prepareSearchStyleResponse() end.");
        return  outJson;
    }

    /**
     *
     * @param styleId String styleId
     * @param version int version
     * @param headers Map<String, String> headers
     * @return JSONObject with Style Rest response
     * @throws CLOSETException exception
     */
    public JSONObject prepareGetStyleResponse(String styleId, int version, Map<String, String> headers) throws CLOSETException {
        JSONObject outJson = new JSONObject();

        HttpHeaders restHeaders = prepareClosetHeaders(headers);

        String url = CLOSETConstants.CLOSET_BASE_URL + CLOSETConstants.CLOSET_GET_STYLE_END_POINT + styleId + CLOSETConnectorConstants.FORWARD_SLASH + CLOSETConstants.CLOSET_VERSIONS_PARAM_KEY
                + CLOSETConnectorConstants.FORWARD_SLASH + version;
        logger.debug("DEBUG::CLOSETHelper: prepareGetStyleResponse() url - " + url);
        try {
            ResponseEntity<String> styleResponseEntity = restService.makeGetOrPostCall(url, HttpMethod.GET, restHeaders, new JSONObject());

            HttpStatus statusCode = styleResponseEntity.getStatusCode();
            logger.debug("DEBUG::CLOSETHelper: prepareGetStyleResponse() statusCode - " + statusCode);

            JSONObject stylesResponseJSON = new JSONObject(styleResponseEntity.getBody());
            logger.info("stylesResponseJSON :" + stylesResponseJSON);

            if (styleResponseEntity.getStatusCodeValue() != 200) {
                String errorJson = styleResponseEntity.getBody();
                throw new CLOSETException(errorJson, statusCode);
            }

            String responseStyleList = styleResponseEntity.getBody();
            logger.debug("DEBUG::CLOSETHelper: prepareGetStyleResponse() responseStyleList - " + responseStyleList);

            if (!Utility.isJSONObject(responseStyleList)) {
                throw new CLOSETException("Error in parsing searched style, please contact your System Administrator", HttpStatus.INTERNAL_SERVER_ERROR);
            }
            JSONObject styleJson = addFileDetails(styleId, new JSONObject(responseStyleList), headers);
            outJson.put(CLOSETConnectorConstants.CC_STYLE_JSON_KEY, styleJson);
        } catch(HttpClientErrorException restExc) {
            String excMessage = readErrorResponseMessage(restExc.getResponseBodyAsString());
            logger.error("ERROR::CLOSETHelper: prepareGetStyleResponse() excMessage - " + excMessage);
            throw new CLOSETException(excMessage, restExc.getStatusCode());
        }

        logger.debug("DEBUG::CLOSETHelper: prepareGetStyleResponse() outJson - " + outJson);
        return  outJson;
    }

    private JSONObject addFileDetails(String styleId, JSONObject styleDataJson, Map<String, String> headers) throws CLOSETException {
        int version = styleDataJson.getInt(CLOSETConstants.CLOSET_VERSION_PARAM_KEY);
        JSONArray versions = styleDataJson.getJSONArray(CLOSETConstants.CLOSET_VERSIONS_PARAM_KEY);
        styleDataJson.put(CLOSETConstants.RESET_EXTERNAL_STYLE_ID, false);

        if (!styleDataJson.has(CLOSETConstants.EXTERNAL_STYLE_ID)) {
            JSONObject closetTechSpec = getTechSpec(styleId, version, headers);
            JSONObject apiMetaDataJon = closetTechSpec.getJSONObject(CLOSETConstants.CLOSET_TECHPACK_API_META_DATA_KEY);

            if (apiMetaDataJon.has(CLOSETConstants.EXTERNAL_STYLE_ID)) {
                Object externalStyleIdObj = apiMetaDataJon.get(CLOSETConstants.EXTERNAL_STYLE_ID);
                if(externalStyleIdObj != null) {
                    logger.info("externalStyleId techSpec:" + externalStyleIdObj);
                    styleDataJson.put(CLOSETConstants.EXTERNAL_STYLE_ID, externalStyleIdObj);
                    styleDataJson.put(CLOSETConstants.RESET_EXTERNAL_STYLE_ID, true);
                }
            }
        } else if(styleDataJson.has(CLOSETConstants.EXTERNAL_STYLE_ID) && styleDataJson.isNull(CLOSETConstants.EXTERNAL_STYLE_ID)) {
            JSONObject closetTechSpec = getTechSpec(styleId, version, headers);

            if(closetTechSpec.has(CLOSETConstants.CLOSET_TECHPACK_API_META_DATA_KEY)) {
                JSONObject apiMetaDataJon = closetTechSpec.getJSONObject(CLOSETConstants.CLOSET_TECHPACK_API_META_DATA_KEY);
                if (apiMetaDataJon.has(CLOSETConstants.EXTERNAL_STYLE_ID)) {
                    Object externalStyleIdObj = apiMetaDataJon.get(CLOSETConstants.EXTERNAL_STYLE_ID);
                    if(externalStyleIdObj != null) {
                        logger.info("externalStyleId techSpec:" + externalStyleIdObj);
                        styleDataJson.put(CLOSETConstants.EXTERNAL_STYLE_ID, externalStyleIdObj);
                        styleDataJson.put(CLOSETConstants.RESET_EXTERNAL_STYLE_ID, true);
                    }
            }
            }
        }

        for(int i = 0; i < versions.length(); i++) {
            JSONObject versionJson = versions.getJSONObject(i);
            int tempVersion = versionJson.getInt(CLOSETConstants.CLOSET_VERSION_PARAM_KEY);
            if(version == tempVersion) {
                String fileName = versionJson.getString(PLMConstants.FILENAME);
                styleDataJson.put(CLOSETConnectorConstants.CC_ZPRJ_FILE_NAME, fileName);
                break;
            }
        }
        JSONObject zprjJson = prepareGetZprjFileResponse(styleId, version, headers);
        String zprjUrl = zprjJson.getString(CLOSETConnectorConstants.CC_URL_JSON_KEY);
        styleDataJson.put(CLOSETConnectorConstants.CC_ZPRJ_FILE_URL, zprjUrl);

        String rendersFileName = styleDataJson.getString(CLOSETConnectorConstants.CC_ZPRJ_FILE_NAME);
        rendersFileName = rendersFileName.substring(0, rendersFileName.lastIndexOf(CLOSETConnectorConstants.PERIOD)) + CLOSETConnectorConstants.UNDERSCORE + "RENDERS" + CLOSETConnectorConstants.PERIOD + CLOSETConnectorConstants.ZIP_EXTENSION;
        String thumbnailFileName = styleDataJson.getString(CLOSETConnectorConstants.CC_ZPRJ_FILE_NAME);
        thumbnailFileName = thumbnailFileName.substring(0, thumbnailFileName.lastIndexOf(CLOSETConnectorConstants.PERIOD)) + CLOSETConnectorConstants.UNDERSCORE + "Thumbnail" + CLOSETConnectorConstants.PERIOD + CLOSETConnectorConstants.PNG_EXTENSION;
        styleDataJson.put(CLOSETConnectorConstants.CC_RENDER_FILE_NAME, rendersFileName);
        styleDataJson.put(CLOSETConnectorConstants.CC_THUMBNAIL_FILE_NAME, thumbnailFileName);
        styleDataJson.put(CLOSETConnectorConstants.CC_RENDER_FILE_URL, "");
        return styleDataJson;
    }
    /**
     * prepareClosetHeaders - Prepares HTTP headers using collection of headers
     * @param httpHeaders Map<String, String> httpHeaders
     * @return HttpHeaders closetHeaders
     */
    private HttpHeaders prepareClosetHeaders(Map<String, String> httpHeaders) {
        HttpHeaders closetHeaders = new HttpHeaders();
        closetHeaders.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
        closetHeaders.add(CLOSETConstants.API_VERSION, CLOSETConstants.VERSION_2_0);
        closetHeaders.add(HttpHeaders.AUTHORIZATION, httpHeaders.get(CLOSETConstants.CLOSET_AUTH_TOKEN));
        closetHeaders.add(CLOSETConstants.X_USER_EMAIL, httpHeaders.get(CLOSETConstants.CLO_SET_EMAIL));

        return closetHeaders;
    }

    /**
     * prepareGetGroupResponse - Function calls CLOSET Rest API and prepares response
     * @param headers Map<String, String> headers
     * @return JSONObject with Style Rest response
     * @throws CLOSETException exception
     */
    public JSONObject prepareGetGroupResponse(Map<String, String> headers) throws CLOSETException {
        JSONObject outJson = new JSONObject();
        try {
            HttpHeaders restHeaders = prepareClosetHeaders(headers);
            ResponseEntity<String> groupsResponse = restService.makeGetOrPostCall(CLOSETConstants.CLOSET_BASE_URL + CLOSETConstants.CLOSET_GROUPS_END_POINT,
                    HttpMethod.GET,
                    restHeaders,
                    new JSONObject());
            HttpStatus statusCode = groupsResponse.getStatusCode();
            logger.debug("DEBUG::CLOSETHelper: prepareGetGroupResponse() statusCode - " + statusCode);

            if (groupsResponse.getStatusCodeValue() != 200) {
                String errorJson = groupsResponse.getBody();
                throw new CLOSETException(errorJson, statusCode);
            }
            String groupsResponseStr = groupsResponse.getBody();
            if (!Utility.isJSONArray(groupsResponseStr)) {
                throw new CLOSETException("Error in parsing groups, please contact your System Administrator", HttpStatus.INTERNAL_SERVER_ERROR);
            }

            JSONArray groupsList = new JSONArray(groupsResponseStr);
            outJson.put(CLOSETConnectorConstants.CC_GROUPS_JSON_KEY, groupsList);
            outJson.put(CLOSETConnectorConstants.CC_COUNT_JSON_KEY, groupsList.length());
        } catch(HttpClientErrorException restExc) {
            String excMessage = readErrorResponseMessage(restExc.getResponseBodyAsString());
            logger.error("ERROR::CLOSETHelper: prepareGetGroupResponse() excMessage - " + excMessage);
            throw new CLOSETException(excMessage, restExc.getStatusCode());
        }
        return outJson;
    }

    /**
     *  prepareGetRenderResponse - Function calls CLOSET Rest API and prepares response
     * @param styleId String styleId
     * @param version int version
     * @param headers Map headers
     * @return JSONObject with Style Rest response
     * @throws CLOSETException exception
     */
    public JSONObject prepareGetRenderResponse(String styleId, int version, Map<String, String> headers) throws CLOSETException {
        JSONObject outJson = new JSONObject();
        try {
            HttpHeaders restHeaders = prepareClosetHeaders(headers);

            String url = CLOSETConstants.CLOSET_BASE_URL + CLOSETConstants.CLOSET_GET_STYLE_RENDERS_END_POINT + CLOSETConnectorConstants.QUESTION_MARK
                    + CLOSETConstants.CLOSET_STYLE_ID_PARAM_KEY + CLOSETConnectorConstants.ASSIGN + styleId + CLOSETConnectorConstants.AMPERSAND
                    + CLOSETConstants.CLOSET_VERSION_PARAM_KEY + CLOSETConnectorConstants.ASSIGN + version;

            ResponseEntity<String> rendersResponse = restService.makeGetOrPostCall(url, HttpMethod.GET, restHeaders, new JSONObject());

            HttpStatus statusCode = rendersResponse.getStatusCode();
            logger.debug("DEBUG::CLOSETHelper: prepareGetRenderResponse() statusCode - " + statusCode);

            if (rendersResponse.getStatusCodeValue() != 200) {
                String errorJson = rendersResponse.getBody();
                throw new CLOSETException(errorJson, statusCode);
            }
            String rendersResponseStr = rendersResponse.getBody();
            if (!Utility.isJSONArray(rendersResponseStr)) {
                throw new CLOSETException("Error in parsing renders, please contact your System Administrator", HttpStatus.INTERNAL_SERVER_ERROR);
            }

            JSONArray rendersList = new JSONArray(rendersResponseStr);
            outJson.put(CLOSETConnectorConstants.CC_RENDERS_JSON_KEY, rendersList);
            outJson.put(CLOSETConnectorConstants.CC_COUNT_JSON_KEY, rendersList.length());
        } catch(HttpClientErrorException restExc) {
                String excMessage = readErrorResponseMessage(restExc.getResponseBodyAsString());
                logger.error("ERROR::CLOSETHelper: prepareGetGroupResponse() excMessage - " + excMessage);
                throw new CLOSETException(excMessage, restExc.getStatusCode());
        }
        return outJson;
    }

    /**
     * prepareGetColorwaysResponse - Function calls CLOSET Rest API and prepares response
     * @param styleId String styleId
     * @param version int version
     * @param headers Map<String, String> headers
     * @return JSONObject with Style Rest response
     * @throws CLOSETException exception
     */
    public JSONObject prepareGetColorwaysResponse(String styleId, int version, Map<String, String> headers) throws CLOSETException {
        JSONObject outJson = new JSONObject();
        try {
            HttpHeaders restHeaders = prepareClosetHeaders(headers);
            String url = CLOSETConstants.CLOSET_BASE_URL + CLOSETConstants.CLOSET_GET_TECHPACK_END_POINT+ styleId + CLOSETConnectorConstants.FORWARD_SLASH + CLOSETConstants.CLOSET_VERSIONS_PARAM_KEY
                    + CLOSETConnectorConstants.FORWARD_SLASH + version + CLOSETConnectorConstants.FORWARD_SLASH + CLOSETConstants.CLOSET_GET_TECHPACK_END_POINT_SUFFIX
                    + CLOSETConnectorConstants.QUESTION_MARK + CLOSETConstants.CLOSET_CONVERT_RES_URL_PARAM_KEY + CLOSETConnectorConstants.ASSIGN + true;
            ResponseEntity<String> techpackResponse = restService.makeGetOrPostCall(url, HttpMethod.GET, restHeaders, new JSONObject());

            HttpStatus statusCode = techpackResponse.getStatusCode();
            logger.debug("DEBUG::CLOSETHelper: prepareGetColorwaysResponse() statusCode - " + statusCode);

            if (techpackResponse.getStatusCodeValue() != 200) {
                String errorJson = techpackResponse.getBody();
                throw new CLOSETException(errorJson, statusCode);
            }
            String techpackResponseStr = techpackResponse.getBody();
            if (!Utility.isJSONObject(techpackResponseStr)) {
                throw new CLOSETException("Error in parsing techpack, please contact your System Administrator", HttpStatus.INTERNAL_SERVER_ERROR);
            }

            JSONObject techpack = new JSONObject(techpackResponseStr);
            JSONArray colorwayList = readCLOSETColorways(techpack);

            outJson.put(CLOSETConnectorConstants.CC_COLORWAYS_JSON_KEY, colorwayList);
            outJson.put(CLOSETConnectorConstants.CC_COUNT_JSON_KEY, colorwayList.length());
        } catch(HttpClientErrorException restExc) {
            String excMessage = readErrorResponseMessage(restExc.getResponseBodyAsString());
            logger.error("ERROR::CLOSETHelper: prepareGetColorwaysResponse() excMessage - " + excMessage);
            throw new CLOSETException(excMessage, restExc.getStatusCode());
        }

        return outJson;
    }

    /**
     * readCLOSETColorways - Read Colorways from techpack json
     * @param techpack JSONObject techpack
     * @return JSONArray outColorwayList
     * @throws CLOSETException exception
     */
    public JSONArray readCLOSETColorways(JSONObject techpack) throws CLOSETException {
        JSONArray outColorwayList = new JSONArray();
        try {
            if (!techpack.has(CLOSETConstants.CLOSET_TECHPACK_COLORWAYS_KEY)) {
                return outColorwayList;
            }
            outColorwayList = techpack.getJSONArray(CLOSETConstants.CLOSET_TECHPACK_COLORWAYS_KEY);

        } catch(JSONException jsonExc) {
            throw new CLOSETException(jsonExc.getLocalizedMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return outColorwayList;
    }

    /**
     *
     * @param headers
     * @return CLOSETViewFields
     * @throws CLOSETException
     */
    public JSONObject prepareGetClosetViewResponse(HttpHeaders headers) throws CLOSETException{
        headers.add(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
        String filename = "/" + "config" + "/" + "CLOSET" + "/" + "CLOSETViewFields.json";
        JSONObject json = new JSONObject();
        try (InputStream inputStream = getClass().getResourceAsStream(filename)) {
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            BufferedReader bufferedReader = new BufferedReader(reader);
            JSONTokener tokener = new JSONTokener(bufferedReader);
            json = new JSONObject(tokener);
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(0);
        }
        return json;
    }

    /**
     *
     * @param headers
     * @return CLOSETResultsFields
     * @throws CLOSETException
     */
    public JSONObject prepareGetClosetResultResponse(HttpHeaders headers) throws CLOSETException {
        headers.add(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
        String filename = "/" + "config" + "/" + "CLOSET" + "/" + "CLOSETResultsFields.json";
        JSONObject json = new JSONObject();
        try (InputStream inputStream = getClass().getResourceAsStream(filename)) {
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            BufferedReader bufferedReader = new BufferedReader(reader);
            JSONTokener tokener = new JSONTokener(bufferedReader);
            json = new JSONObject(tokener);
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(0);
        }
        return json;
    }

    /**
     * prepareGetZprjFileResponse - Function calls CLOSET Rest API and prepares response
     * @param styleId String styleId
     * @param version int version
     * @param headers Map<String, String> headers
     * @return JSONObject with Style Rest response
     * @throws CLOSETException exception
     */
    public JSONObject prepareGetZprjFileResponse(String styleId, int version, Map<String, String> headers) throws CLOSETException {
        JSONObject outJson = new JSONObject();
        try {
            HttpHeaders restHeaders = prepareClosetHeaders(headers);
            //https://www.clo-set.com/api/styles/55eeb58403b5401e81b6deb55a8c5f73/versions/0/downloadUrl
            String url = CLOSETConstants.CLOSET_BASE_URL + CLOSETConstants.CLOSET_GET_STYLE_END_POINT + styleId + CLOSETConnectorConstants.FORWARD_SLASH + CLOSETConstants.CLOSET_VERSIONS_PARAM_KEY
                    + CLOSETConnectorConstants.FORWARD_SLASH + version + CLOSETConstants.CLOSET_GET_ZPRJ_FILE_END_POINT;
            ResponseEntity<String> rendersResponse = restService.makeGetOrPostCall(url, HttpMethod.GET, restHeaders, new JSONObject());
            outJson.put("url",rendersResponse.getBody());
        } catch(HttpClientErrorException restExc) {
            String excMessage = readErrorResponseMessage(restExc.getResponseBodyAsString());
            logger.error("ERROR::CLOSETHelper: prepareGetZprjFileResponse() excMessage - " + excMessage);
            throw new CLOSETException(excMessage, restExc.getStatusCode());
        }
        return outJson;
    }
    /**
     * prepareGetRenderZipResponse - Function calls CLOSET Rest API and prepares response
     * @param styleId String styleId
     * @param renderSeq int version
     * @param headers Map<String, String> headers
     * @return JSONObject with Style Rest response
     * @throws CLOSETException exception
     */
    public JSONObject prepareGetRenderZipFileResponse(String styleId, int renderSeq, Map<String, String> headers) throws CLOSETException {
        JSONObject outJson = new JSONObject();
        try {
            HttpHeaders restHeaders = prepareClosetHeaders(headers);

            String url = CLOSETConstants.CLOSET_BASE_URL + CLOSETConstants.CLOSET_GET_ZIP_FILE_END_POINT + CLOSETConnectorConstants.QUESTION_MARK
                    + CLOSETConstants.CLOSET_STYLE_ID_PARAM_KEY + CLOSETConnectorConstants.ASSIGN + styleId + CLOSETConnectorConstants.AMPERSAND
                    + CLOSETConstants.CLOSET_RENDERS_PARAM_KEY + CLOSETConnectorConstants.ASSIGN + renderSeq;
            ResponseEntity<String> rendersResponse = restService.makeGetOrPostCall(url, HttpMethod.GET, restHeaders, new JSONObject());
            outJson.put("url",rendersResponse.getBody());
        } catch(HttpClientErrorException restExc) {
            String excMessage = readErrorResponseMessage(restExc.getResponseBodyAsString());
            logger.error("ERROR::CLOSETHelper: prepareGetRenderZipFileResponse() excMessage - " + excMessage);
            throw new CLOSETException(excMessage, restExc.getStatusCode());
        }
        return outJson;
    }

    /**
     *
     * @param styleId
     * @param styleVersion
     * @param httpHeaders
     * @return TechSpec
     * @throws CLOSETException
     */
    public JSONObject getTechSpec(String styleId, int styleVersion, Map<String, String> httpHeaders) throws CLOSETException {
        try {
            HttpHeaders CLOSETHeaders = prepareClosetHeaders(httpHeaders);
            //String techSpecURI = "https://www.clo-set.com/api" + "/styles/" + CLOSETStyleId + "/versions/" + styleVersion + "/techpackJson?convertResourceUrl=true";
            String techSpecURI = CLOSETConstants.CLOSET_BASE_URL + CLOSETConstants.CLOSET_GET_TECHPACK_END_POINT+ styleId + CLOSETConnectorConstants.FORWARD_SLASH + CLOSETConstants.CLOSET_VERSIONS_PARAM_KEY
                    + CLOSETConnectorConstants.FORWARD_SLASH + styleVersion + CLOSETConstants.CLOSET_GET_TECHPACK_END_POINT_SUFFIX
                    + CLOSETConnectorConstants.QUESTION_MARK + CLOSETConstants.CLOSET_CONVERT_RES_URL_PARAM_KEY + CLOSETConnectorConstants.ASSIGN + true;
            ResponseEntity<String> responseEntity = restService.makeGetOrPostCall(techSpecURI, HttpMethod.GET, CLOSETHeaders, new JSONObject());
            String responseTechSpec = responseEntity.getBody();
            return new JSONObject(responseTechSpec);
        } catch(HttpClientErrorException restExc) {
            restExc.printStackTrace();
            logger.error("ERROR::CLOSETHelper: getTechSpec() excMessage - " + restExc);
            throw new CLOSETException(restExc.getMessage(), restExc.getStatusCode());
        }
    }

    /**
     *
     * @param styleId
     * @param styleVersion
     * @param renderSeq
     * @param headers
     * @return Render url
     * @throws CLOSETException
     */
    public String getRenderURL(String styleId, int styleVersion, int renderSeq, Map<String, String> headers) throws CLOSETException {
        try {
            HttpHeaders closetHeaders = prepareClosetHeaders(headers);

            String url = CLOSETConstants.CLOSET_BASE_URL + CLOSETConstants.CLOSET_GET_STYLE_RENDER_URL_END_POINT + styleId + CLOSETConnectorConstants.FORWARD_SLASH + CLOSETConstants.CLOSET_RENDERS_PARAM_KEY
                    + CLOSETConnectorConstants.FORWARD_SLASH + renderSeq + CLOSETConnectorConstants.FORWARD_SLASH + "downloadurl";

            //this.BASE_URL + "/styles/" + closetStyleId + "/renders/" + renderSeq + "/downloadurl"
            ResponseEntity<String> response = restService.makeGetOrPostCall(url, HttpMethod.GET, closetHeaders, new JSONObject());
            String renderResponse = response.getBody();

            return renderResponse;

        } catch(HttpClientErrorException restExc) {
            restExc.printStackTrace();
            logger.error("ERROR::CLOSETHelper: getRenderURL() excMessage - " + restExc);
            throw new CLOSETException(restExc.getMessage(), restExc.getStatusCode());
        }
    }
    /**
     * Update the external Style Id
     *
     * @param CLOSETStyleID   CLO-SET Style Id
     * @param externalStyleID External Style Id to be updated
     * @param httpHeaders     headers
     * @return updated Style Id
     * @throws CLOSETException exceptions, related to network, authorization, resources not found, etc
     */
    public String publishExternalId(String CLOSETStyleID,
                                    String externalStyleID,
                                    Map<String, String> httpHeaders) throws CLOSETException {
        try {
            HttpHeaders CLOSETHeaders = prepareClosetHeaders(httpHeaders);
            return  this.restService.makePatchCall(CLOSETConstants.CLOSET_BASE_URL + "/styles/" + CLOSETStyleID + "/externalId?externalStyleId=" + externalStyleID,
                    CLOSETHeaders,
                    new JSONObject());
        } catch (HttpClientErrorException |
                HttpServerErrorException e) {
            e.printStackTrace();
            if (e.getStatusCode() == HttpStatus.BAD_REQUEST) {
                logger.error("ERROR::CLOSETHelper: publishExternalId() excMessage - " + HttpStatus.CONFLICT);
                throw new CLOSETException("The CLOSET Style is already linked with other PLM Style Id", HttpStatus.CONFLICT);
            }
            throw new CLOSETException(e.getMessage(), e.getStatusCode());
        }
    }

}
