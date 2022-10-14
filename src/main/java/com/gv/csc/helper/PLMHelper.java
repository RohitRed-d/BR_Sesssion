package com.gv.csc.helper;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gv.csc.entity.LastLoginTime;
import com.gv.csc.entity.Style;
import com.gv.csc.exceptions.CLOSETException;
import com.gv.csc.exceptions.PLMException;
import com.gv.csc.service.LastLoginTimeService;
import com.gv.csc.service.StyleService;
import com.gv.csc.util.*;
import okhttp3.Response;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.http.*;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.net.InetAddress;

/**
 * PLMHelper - Helper class containing the helper logic for speaking with PLM APIs and preparing response
 */
public class PLMHelper {

    @Autowired
    private RestService restService;

    @Autowired
    private CLOSETHelper closetHelper;

    @Autowired
     private StyleService styleService;
    private ObjectMapper objectMapper = new ObjectMapper();
    
    @Autowired
    private Environment environment;
    
    @Autowired
    private LastLoginTimeService lastLoginTimeService;

    Logger logger = LoggerFactory.getLogger(PLMHelper.class);

    public PLMHelper() {
    }

    @Value( "${csc.plm.companyName}" )
    private String companyName;
    
    /**
     * prepareLoginResponse - Function calls PLM Rest API and prepares response
     * @param userid String userid
     * @param password String password
     * @return JSONObject with response
     * @throws PLMException exception
     * @throws CLOSETException 
     * @throws UnknownHostException 
     */
    public JSONObject prepareLoginResponse(String userid, String password, String plmUrl, Map<String, String> headers) throws PLMException{
        logger.info("INFO::PLMHelper: prepareLoginResponse() started.");
        validateLicense(userid);
        String cloUserName = headers.get("closet-user-name");
        JSONObject outJson = new JSONObject();
        String url = plmUrl + PLMConstants.LOGIN_URI + CLOSETConnectorConstants.QUESTION_MARK
                + PLMConstants.PLM_LOGIN_CS_PARAM_KEY + CLOSETConnectorConstants.ASSIGN + PLMConstants.PLM_LOGIN_CS_PARAM_VALUE
                + CLOSETConnectorConstants.AMPERSAND + PLMConstants.PLM_LOGIN_EXPOFFSET_PARAM_KEY + CLOSETConnectorConstants.ASSIGN
                + CLOSETConnectorConstants.TOKEN_EXPIRY_TIME;

        HttpHeaders restheaders = new HttpHeaders();
        restheaders.add(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
        restheaders.add(PLMConstants.API_HEADER_USER_ID_KEY, userid);
        restheaders.add(PLMConstants.API_HEADER_PASSWORD_ID_KEY, password);
        restheaders.add(PLMConstants.API_HEADER_VERSION_KEY, PLMConstants.API_HEADER_VERSION_VALUE);

        logger.debug("DEBUG::CLOSETHelper: prepareLoginResponse() uri - " + url);
        ResponseEntity<String> responseEntity = restService.makeGetOrPostCall(url, HttpMethod.GET, restheaders, new JSONObject());

        HttpStatus statusCode = responseEntity.getStatusCode();
        logger.debug("DEBUG::PLMHelper: prepareLoginResponse() statusCode - "+statusCode);

        if (statusCode.value() != 200) {
            String errorJson = responseEntity.getBody();
            throw new PLMException(errorJson, statusCode);
        }
        
        JSONObject loginResponseJSON = new JSONObject(responseEntity.getBody());

        JSONObject documentJSONObject = loginResponseJSON.getJSONObject(PLMConstants.PLM_DOCUMENT_JSON_KEY);
        JSONArray messageJSONArray = documentJSONObject.getJSONArray(PLMConstants.PLM_MESSAGE_JSON_KEY);
        JSONObject messageJSONObject = messageJSONArray.getJSONObject(0);
        String loginStatus = messageJSONObject.getString(PLMConstants.PLM_STATUS_JSON_KEY);

        if(!PLMConstants.PLM_SUCCESS_STATUS_VALUE.equalsIgnoreCase(loginStatus)) {
            String messageId = messageJSONObject.getString(PLMConstants.PLM_MESSAGE_ID_JSON_KEY);
            String messageDesc = messageJSONObject.getString(PLMConstants.PLM_MESSAGE_DESC_JSON_KEY);
            throw new PLMException(messageId + CLOSETConnectorConstants.COLON + CLOSETConnectorConstants.SPACE + messageDesc, HttpStatus.UNAUTHORIZED);
        }
        // Storing time and userId in database
        Date dNow = new Date( );
        LastLoginTime lastLoginTime = lastLoginTimeService.getLastLoginTimeByClosetUserAndPlmUserAndLoginTime(cloUserName, userid, dNow);
        if(lastLoginTime == null) {
        	lastLoginTime = new LastLoginTime();
        }
        lastLoginTime.setClosetUser(cloUserName);
        lastLoginTime.setLoginTime(dNow);
        lastLoginTime.setPlmUser(userid);
        lastLoginTimeService.saveLastLoginTime(lastLoginTime);

        String tokenValue = messageJSONObject.getString(PLMConstants.PLM_TOKEN_JSON_KEY);
        outJson.put(CLOSETConnectorConstants.CC_EXPIRY_JSON_KEY, CLOSETConnectorConstants.TOKEN_EXPIRY_TIME);
        outJson.put(CLOSETConnectorConstants.CC_EMAIL_JSON_KEY, "");
        outJson.put(CLOSETConnectorConstants.CC_USERID_JSON_KEY, userid);
        outJson.put(CLOSETConnectorConstants.CC_TOKEN_JSON_KEY, tokenValue);

        JSONArray userJSONArray = documentJSONObject.getJSONArray(PLMConstants.PLM_USER_JSON_KEY);
        JSONObject userJSONObject = userJSONArray.getJSONObject(0);
        outJson.put(CLOSETConnectorConstants.CC_USER_NAME_JSON_KEY, userJSONObject.getString(PLMConstants.PLM_USER_NAME_JSON_KEY));
        
        logger.info("INFO::PLMHelper: prepareLoginResponse() outJson - "+outJson);
        logger.info("INFO::PLMHelper: prepareLoginResponse() end.");
        return outJson;
    }

    /**
     * prepareErrorResponse - Function used to prepare Error JSON
     * @param exception PLMException exception
     * @return JSONObject prepares Error JSON consuming exception
     */
    public static JSONObject prepareErrorResponse(PLMException exception) {
        JSONObject errorJSON = new JSONObject();
        errorJSON.put(CLOSETConnectorConstants.CC_STATUS_CODE_JSON_KEY, exception.getStatusCode());
        errorJSON.put(CLOSETConnectorConstants.CC_STATUS_JSON_KEY, CLOSETConnectorConstants.CC_FAILED_STATUS_JSON_VALUE);
        errorJSON.put(CLOSETConnectorConstants.CC_MESSAGE_JSON_KEY, exception.getLocalizedMessage());

        return errorJSON;
    }
    /**
     * prepareSearchStyleResponse - Function calls PLM Rest API and prepares response
     * @param searchTerm String searchTerm
     * @param headers Map<String, String> headers)
     * @return JSONObject with Search styles response
     * @throws PLMException exception
     */
    public JSONObject prepareSearchStyleResponse(String searchTerm, Map<String, String> headers, String plmUrl) throws PLMException {
        logger.info("INFO::PLMHelper: prepareSearchStyleResponse() start.");
        //String plmurl = headers.get("plm_env_url");
        JSONObject outJson = new JSONObject();

        HttpHeaders restHeaders = preparePlmHeaders(headers);

        String url = plmUrl + PLMConstants.SEARCH_STYLE_URI + CLOSETConnectorConstants.QUESTION_MARK
                + PLMConstants.CLOSET_SEARCH_TERM_PARAM_KEY + CLOSETConnectorConstants.ASSIGN + searchTerm;
        System.out.println("url prepareSearchStyleResponse  :"+url);
        ResponseEntity<String> stylesResponse = restService.makeGetOrPostCall(url, HttpMethod.GET, restHeaders, new JSONObject());

        HttpStatus statusCode = stylesResponse.getStatusCode();
        logger.debug("DEBUG::PLMHelper: prepareSearchStyleResponse() statusCode - " + statusCode);

        if (stylesResponse.getStatusCodeValue() != 200) {
            String errorJson = stylesResponse.getBody();
            throw new PLMException(errorJson, statusCode);
        }

        JSONObject stylesResponseJSON = new JSONObject(stylesResponse.getBody());
        logger.info("stylesResponseJSON :" + stylesResponseJSON);
        JSONObject documentJSONObject = stylesResponseJSON.getJSONObject(PLMConstants.PLM_DOCUMENT_JSON_KEY);
        JSONArray messageJSONArray = documentJSONObject.getJSONArray(PLMConstants.PLM_MESSAGE_JSON_KEY);
        JSONObject messageJSONObject = messageJSONArray.getJSONObject(0);
        String loginStatus = messageJSONObject.getString(PLMConstants.PLM_STATUS_JSON_KEY);



        if (!PLMConstants.PLM_SUCCESS_STATUS_VALUE.equalsIgnoreCase(loginStatus)) {
            String messageId = messageJSONObject.getString(PLMConstants.PLM_MESSAGE_ID_JSON_KEY);
            String messageDesc = messageJSONObject.getString(PLMConstants.PLM_MESSAGE_DESC_JSON_KEY);
            throw new PLMException(messageId + CLOSETConnectorConstants.COLON + CLOSETConnectorConstants.SPACE + messageDesc, HttpStatus.INTERNAL_SERVER_ERROR);
        }

        if (!documentJSONObject.has(PLMConstants.PLM_TECH_SPEC_JSON_KEY)) {
            String messageId = messageJSONObject.getString(PLMConstants.PLM_MESSAGE_ID_JSON_KEY);
            String messageDesc = messageJSONObject.getString(PLMConstants.PLM_MESSAGE_DESC_JSON_KEY);
            throw new PLMException(messageId + CLOSETConnectorConstants.COLON + CLOSETConnectorConstants.SPACE + messageDesc, HttpStatus.INTERNAL_SERVER_ERROR);
        }

        JSONArray tempStylesList = documentJSONObject.getJSONArray(PLMConstants.PLM_TECH_SPEC_JSON_KEY);
        tempStylesList = setDisplayValues(tempStylesList, restHeaders, plmUrl);

        JSONArray stylesList = new JSONArray();
        JSONObject style;
        JSONObject tempStyle;
        JSONArray tempAttachments;
        JSONObject tempAttachment;
        String attachmentName;
        String associateId;
        String imagePath;
        for (int i = 0; i < tempStylesList.length(); i++) {
            imagePath = "";
            tempStyle = tempStylesList.getJSONObject(i);
            style = tempStyle;
            if (tempStyle.has(PLMConstants.PLM_ATTACHMENT_JSON_KEY)) {
                tempAttachments = tempStyle.getJSONArray(PLMConstants.PLM_ATTACHMENT_JSON_KEY);
                for (int j = 0; j < tempAttachments.length(); j++) {
                    tempAttachment = tempAttachments.getJSONObject(j);
                    String attachmentNo = tempAttachment.getString(PLMConstants.PLM_ATTACHMENT_NO_JSON_KEY);
                    if (PLMConstants.PLM_ATTACHMENT_NO_FOR_THUMNAIL.equalsIgnoreCase(attachmentNo)) {
                        attachmentName = tempAttachment.getString(PLMConstants.PLM_LOCATION_JSON_KEY);
                        associateId = tempAttachment.getString(PLMConstants.PLM_ASSOC_ID_JSON_KEY);
                        imagePath = plmUrl + PLMConstants.DOWNLOAD_ATTACHMENT_URI + attachmentName + CLOSETConnectorConstants.QUESTION_MARK
                                + PLMConstants.API_HEADER_TOKEN_KEY + CLOSETConnectorConstants.ASSIGN + headers.get(PLMConstants.PLM_AUTH_TOKEN)
                                + CLOSETConnectorConstants.AMPERSAND + PLMConstants.CLOSET_ASSOC_ID_PARAM_KEY + CLOSETConnectorConstants.ASSIGN + associateId;
                        break;
                    }
                }
                style.put(CLOSETConnectorConstants.CC_THUMBNAIL_JSON_KEY, imagePath);
            } else {
                style.put(CLOSETConnectorConstants.CC_THUMBNAIL_JSON_KEY, "");
            }
            stylesList.put(style);
        }

        logger.info("stylesList :" + stylesList);

        outJson.put(CLOSETConnectorConstants.CC_STYLES_JSON_KEY, stylesList);
        outJson.put(CLOSETConnectorConstants.CC_COUNT_JSON_KEY, stylesList.length());

        String messageId = messageJSONObject.getString(PLMConstants.PLM_MESSAGE_ID_JSON_KEY);
        String messageDesc = messageJSONObject.getString(PLMConstants.PLM_MESSAGE_DESC_JSON_KEY);
        String msg = messageId + ":" + messageDesc;

        outJson.put(PLMConstants.PLM_MESSAGE_JSON_KEY, msg);

        logger.info("INFO::CLOSETHelper: prepareSearchStyleResponse() end.");
        return outJson;
    }

    /**
     * prepareGetStyleResponse - Function calls PLM Rest API and prepares style response
     * @param requestNo String requestNo
     * @param owner String owner
     * @param headers Map<String, String> headers
     * @return JSONObject with style response
     * @throws PLMException exception
     */
    public JSONObject prepareGetStyleResponse(String requestNo, String owner, Map<String, String> headers, String plmUrl) throws PLMException  {
        logger.info("INFO::PLMHelper: prepareGetStyleResponse() start.");
        JSONObject outJson = new JSONObject();
        //String plmurl = headers.get("plm_env_url");
        HttpHeaders restHeaders = preparePlmHeaders(headers);

        String url = plmUrl + PLMConstants.GET_STYLE_URI + CLOSETConnectorConstants.QUESTION_MARK
                + PLMConstants.CLOSET_OWNER_PARAM_KEY + CLOSETConnectorConstants.ASSIGN + owner + CLOSETConnectorConstants.AMPERSAND
                + PLMConstants.CLOSET_REQUEST_NO_PARAM_KEY + CLOSETConnectorConstants.ASSIGN + requestNo;
        ResponseEntity<String> styleResponse = restService.makeGetOrPostCall(url, HttpMethod.GET, restHeaders, new JSONObject());

        HttpStatus statusCode = styleResponse.getStatusCode();
        logger.debug("DEBUG::PLMHelper: prepareGetStyleResponse() statusCode - " + statusCode);
        logger.debug("DEBUG::PLMHelper: prepareGetStyleResponse() styleResponse.getBody() - " + styleResponse.getBody());
        if (styleResponse.getStatusCodeValue() != 200) {
            String errorJson = styleResponse.getBody();
            throw new PLMException(errorJson, statusCode);
        }

        JSONObject styleResponseJSON = new JSONObject(styleResponse.getBody());

        JSONObject documentJSONObject = styleResponseJSON.getJSONObject(PLMConstants.PLM_DOCUMENT_JSON_KEY);
        JSONArray messageJSONArray = documentJSONObject.getJSONArray(PLMConstants.PLM_MESSAGE_JSON_KEY);
        JSONObject messageJSONObject = messageJSONArray.getJSONObject(0);
        String loginStatus = messageJSONObject.getString(PLMConstants.PLM_STATUS_JSON_KEY);

        if(!PLMConstants.PLM_SUCCESS_STATUS_VALUE.equalsIgnoreCase(loginStatus)) {
            String messageId = messageJSONObject.getString(PLMConstants.PLM_MESSAGE_ID_JSON_KEY);
            String messageDesc = messageJSONObject.getString(PLMConstants.PLM_MESSAGE_DESC_JSON_KEY);
            throw new PLMException(messageId + CLOSETConnectorConstants.COLON + CLOSETConnectorConstants.SPACE + messageDesc, HttpStatus.INTERNAL_SERVER_ERROR);
        }

        if(!documentJSONObject.has(PLMConstants.PLM_TECH_SPEC_JSON_KEY)) {
            String messageId = messageJSONObject.getString(PLMConstants.PLM_MESSAGE_ID_JSON_KEY);
            String messageDesc = messageJSONObject.getString(PLMConstants.PLM_MESSAGE_DESC_JSON_KEY);
            throw new PLMException(messageId + CLOSETConnectorConstants.COLON + CLOSETConnectorConstants.SPACE + messageDesc, HttpStatus.INTERNAL_SERVER_ERROR);

        }

        JSONArray outStylesList = documentJSONObject.getJSONArray(PLMConstants.PLM_TECH_SPEC_JSON_KEY);

        if(outStylesList.length() > 1) {
            throw new PLMException("More than one record found for the combination of Owner and Request Number, please contact your System Administrator", HttpStatus.INTERNAL_SERVER_ERROR);
        }
        outStylesList = setDisplayValues(outStylesList, restHeaders, plmUrl);
        JSONObject tempStyle = outStylesList.getJSONObject(0);

        JSONArray tempAttachments;
        JSONObject tempAttachment;
        String attachmentName;
        String associateId;
        String imagePath;
        ArrayList<Integer> rendersSeq = new ArrayList<Integer>();
        String tempSeq;
        Integer tempSeqNo;
        if (tempStyle.has(PLMConstants.PLM_ATTACHMENT_JSON_KEY)) {
            tempAttachments = tempStyle.getJSONArray(PLMConstants.PLM_ATTACHMENT_JSON_KEY);
            for (int j = 0; j < tempAttachments.length(); j++) {
                tempAttachment = tempAttachments.getJSONObject(j);
                String attachmentNo = tempAttachment.getString(PLMConstants.PLM_ATTACHMENT_NO_JSON_KEY);
                if (PLMConstants.PLM_ATTACHMENT_NO_FOR_THUMNAIL.equalsIgnoreCase(attachmentNo)) {
                    attachmentName = tempAttachment.getString(PLMConstants.PLM_LOCATION_JSON_KEY);
                    associateId = tempAttachment.getString(PLMConstants.PLM_ASSOC_ID_JSON_KEY);
                    imagePath = plmUrl + PLMConstants.DOWNLOAD_ATTACHMENT_URI + attachmentName + CLOSETConnectorConstants.QUESTION_MARK
                            + PLMConstants.API_HEADER_TOKEN_KEY + CLOSETConnectorConstants.ASSIGN + headers.get(PLMConstants.PLM_AUTH_TOKEN)
                            + CLOSETConnectorConstants.AMPERSAND + PLMConstants.CLOSET_ASSOC_ID_PARAM_KEY + CLOSETConnectorConstants.ASSIGN + associateId;

                    tempStyle.put(CLOSETConnectorConstants.CC_THUMBNAIL_JSON_KEY, imagePath);
                } else if (attachmentNo.contains(PLMConstants.PLM_RENDERS_PREFIX)) {
                    tempSeq = attachmentNo.substring(attachmentNo.indexOf(PLMConstants.PLM_RENDERS_PREFIX) + PLMConstants.PLM_RENDERS_PREFIX.length());
                    logger.info("tempSeq - "+tempSeq);
                    if(Utility.hasContent(tempSeq)) {
                        try {
                            tempSeqNo = Integer.parseInt(tempSeq);
                            rendersSeq.add(tempSeqNo);
                        } catch(NumberFormatException nfe) {
                            logger.info("Not a valid Render attachment Number");
                        }
                    }
                }
            }
            logger.info("rendersSeq - "+rendersSeq);
            if(rendersSeq.size() > 0) {
                Collections.sort(rendersSeq);
                logger.info("rendersSeq last - "+rendersSeq.get(rendersSeq.size() - 1));
                tempStyle.put(CLOSETConnectorConstants.CC_LAST_RENDER_SEQ_NO_JSON_KEY, rendersSeq.get(rendersSeq.size() - 1));
            } else {
                tempStyle.put(CLOSETConnectorConstants.CC_LAST_RENDER_SEQ_NO_JSON_KEY, 0);
            }


        } else {
            tempStyle.put(CLOSETConnectorConstants.CC_THUMBNAIL_JSON_KEY, "");
            tempStyle.put(CLOSETConnectorConstants.CC_LAST_RENDER_SEQ_NO_JSON_KEY, 0);
        }

        JSONArray tempColorways;
        JSONObject tempColorway;
        if (tempStyle.has(PLMConstants.PLM_COLORWAYS_JSON_KEY)) {
            JSONArray finalColorways = new JSONArray();
            tempColorways = tempStyle.getJSONArray(PLMConstants.PLM_COLORWAYS_JSON_KEY);
            for (int i = 0; i < tempColorways.length(); i++) {
                tempColorway = tempColorways.getJSONObject(i);
                if (tempColorway.has(PLMConstants.PLM_ATTACHMENT_JSON_KEY)) {
                    tempAttachments = tempColorway.getJSONArray(PLMConstants.PLM_ATTACHMENT_JSON_KEY);
                    for (int j = 0; j < tempAttachments.length(); j++) {
                        tempAttachment = tempAttachments.getJSONObject(j);
                        String attachmentNo = tempAttachment.getString(PLMConstants.PLM_ATTACHMENT_NO_JSON_KEY);
                        if (PLMConstants.PLM_ATTACHMENT_NO_FOR_COLORWAY_THUMNAIL.equalsIgnoreCase(attachmentNo)) {
                            attachmentName = tempAttachment.getString(PLMConstants.PLM_LOCATION_JSON_KEY);
                            associateId = tempAttachment.getString(PLMConstants.PLM_ASSOC_ID_JSON_KEY);
                            imagePath = plmUrl + PLMConstants.DOWNLOAD_ATTACHMENT_URI + attachmentName + CLOSETConnectorConstants.QUESTION_MARK
                                    + PLMConstants.API_HEADER_TOKEN_KEY + CLOSETConnectorConstants.ASSIGN + headers.get(PLMConstants.PLM_AUTH_TOKEN)
                                    + CLOSETConnectorConstants.AMPERSAND + PLMConstants.CLOSET_ASSOC_ID_PARAM_KEY + CLOSETConnectorConstants.ASSIGN + associateId;

                            tempColorway.put(CLOSETConnectorConstants.CC_THUMBNAIL_JSON_KEY, imagePath);
                            break;
                        }
                    }
                }
                finalColorways.put(tempColorway);
            }
            tempStyle.put(PLMConstants.PLM_COLORWAYS_JSON_KEY, finalColorways);
        }
        outJson.put(CLOSETConnectorConstants.CC_STYLE_JSON_KEY, tempStyle);

        logger.info("INFO::PLMHelper: prepareSearchStyleResponse() end.");
        return  outJson;
    }

    private JSONArray setDisplayValues(JSONArray inStylesList, HttpHeaders restHeaders, String plmurl) {
        logger.info("setDisplayValues inStylesList :" + inStylesList);
        String deptCodeValue = "";
        String depNumtValue = "";
        String keys;
        String deptValue = "";
        HashMap<String, HashMap<String, String>> plmLOVs = PLMLOVs.getPLM_LOV_LIST();
        JSONObject tempStylesListJson = null;
        JSONArray outStylesList = new JSONArray();
        for (int i = 0; i < inStylesList.length(); i++) {
            tempStylesListJson = inStylesList.getJSONObject(i);
            for (int j = 0; j < PLMConstants.PLM_ATT_KEYS.size(); j++) {
                deptCodeValue = "";
                keys = PLMConstants.PLM_ATT_KEYS.get(j);
                if (tempStylesListJson.has(keys)) {
                    deptCodeValue = tempStylesListJson.getString(keys);
                }
                if (plmLOVs.isEmpty()) {
                    if(keys.equals(PLMConstants.DEPT)) {
                        deptValue = getDeptDisplayValue(deptCodeValue, restHeaders, plmurl);
                    }
                    if(keys.equals(PLMConstants.BRAND)) {
                        deptValue = getBrandDisplayValue(deptCodeValue, restHeaders, plmurl);
                    }
                    if(keys.equals(PLMConstants.DIVISION)) {
                        deptValue = getDivisionDisplayValue(deptCodeValue, restHeaders, plmurl);
                    }
                    PLMLOVs.setPLM_LOV_LIST(keys, deptCodeValue, deptValue);
                    plmLOVs = PLMLOVs.getPLM_LOV_LIST();
                    depNumtValue = plmLOVs.get(keys).get(deptCodeValue);
                    tempStylesListJson.put(keys, depNumtValue);
                } else {
                    if (plmLOVs.containsKey(keys)) {
                        depNumtValue = plmLOVs.get(keys).get(deptCodeValue);
                        if(depNumtValue == null){
                            if(keys.equals(PLMConstants.DEPT)) {
                                deptValue = getDeptDisplayValue(deptCodeValue, restHeaders, plmurl);
                            }
                            if(keys.equals(PLMConstants.BRAND)) {
                                deptValue = getBrandDisplayValue(deptCodeValue, restHeaders, plmurl);
                            }
                            if(keys.equals(PLMConstants.DIVISION)) {
                                deptValue = getDivisionDisplayValue(deptCodeValue, restHeaders, plmurl);
                            }
                            PLMLOVs.setPLM_LOV_LIST(keys, deptCodeValue, deptValue);
                            plmLOVs = PLMLOVs.getPLM_LOV_LIST();
                            depNumtValue = plmLOVs.get(keys).get(deptCodeValue);
                        }
                        tempStylesListJson.put(keys, depNumtValue);
                    } else {
                        if(keys.equals(PLMConstants.DEPT)) {
                            deptValue = getDeptDisplayValue(deptCodeValue, restHeaders, plmurl);
                        }
                        if(keys.equals(PLMConstants.BRAND)) {
                            deptValue = getBrandDisplayValue(deptCodeValue, restHeaders, plmurl);
                        }
                        if(keys.equals(PLMConstants.DIVISION)) {
                            deptValue = getDivisionDisplayValue(deptCodeValue, restHeaders, plmurl);
                        }
                        PLMLOVs.setPLM_LOV_LIST(keys, deptCodeValue, deptValue);
                        plmLOVs = PLMLOVs.getPLM_LOV_LIST();
                        depNumtValue = plmLOVs.get(keys).get(deptCodeValue);
                        tempStylesListJson.put(keys, depNumtValue);
                    }
                }
            }
            outStylesList.put(tempStylesListJson);
        }
        logger.debug("Debug :: setDisplayValues() outStylesList:"+outStylesList);
        return outStylesList;
    }

    private HttpHeaders preparePlmHeaders(Map<String, String> headers) {
        HttpHeaders brHeaders = new HttpHeaders();
        brHeaders.add(PLMConstants.API_HEADER_TOKEN_KEY, headers.get(PLMConstants.PLM_AUTH_TOKEN));
        //brHeaders.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
        brHeaders.add(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
        brHeaders.add(PLMConstants.API_HEADER_VERSION_KEY, PLMConstants.API_HEADER_VERSION_VALUE);

        return brHeaders;
    }

    /**
     * preparePublishToPLM - Function calls PLM Rest API and prepares publish response
     * @param multiPartRequest MultipartHttpServletRequest multiPartRequest
     * @param headers Map<String, String> headers
     * @return JSONObject with publish response
     * @throws PLMException exception
     */
    public JSONObject preparePublishToPLM(MultipartHttpServletRequest multiPartRequest, Map<String, String> headers, String plmUrl) throws PLMException {
        JSONObject outJson = new JSONObject();
        logger.info("INFO::PLMHelper: preparePublishToPLM() start.");
        //String plmurl = headers.get("plm_env_url");
        try {
            String publishData = multiPartRequest.getParameter(PLMConstants.PLM_PUBLISH_DATA_KEY);
            if(!Utility.isJSONObject(publishData)) {
                throw new PLMException("Error in parsing the Publishing data, please contact your System Administrator.", HttpStatus.INTERNAL_SERVER_ERROR);
            }

            String nanoTime = Long.toString(System.nanoTime());
            String userId = "";

            String outerPath = CLOSETConnectorConstants.STORAGE_DIRECTORY + userId + CLOSETConnectorConstants.UNDERSCORE + nanoTime + File.separator;
            outerPath = Utility.formatOSFolderLocation(outerPath);

            logger.debug("DEBUG::PLMHelper: preparePublishToPLM() -> outerPath :: "+outerPath);

            File outerFile = new File(outerPath);
            if(outerFile != null && outerFile.exists())
                FileUtils.forceDelete(outerFile);

            outerFile = new File(outerPath);

            if(outerFile != null && !outerFile.exists()) {
                outerFile.mkdir();
                logger.info("outerFile is dir - "+outerFile.isFile());
            }

            JSONObject publishDataJson = new JSONObject(publishData);
            HashMap<String, File> uploadFiles = getUploadFiles(multiPartRequest, outerPath);
            List<File> files = uploadFiles.values().stream().collect(Collectors.toList());
            JSONArray uploadAttachmentsArray =  uploadAttachments(files, headers, plmUrl);
            JSONArray attachmentsArray = preparePublishAttachmentsArray(publishDataJson, uploadAttachmentsArray);
            JSONArray colorwaysArray = prepareColorwayArray(publishDataJson);
            //Renders
            JSONObject publishPayLoad = preparePayLoadForBRStylePublish(publishDataJson, attachmentsArray, colorwaysArray);
            HttpHeaders restHeaders = preparePlmHeaders(headers);
            String token = String.valueOf(restHeaders.get(PLMConstants.API_HEADER_TOKEN_KEY));

            Response publishResponse = restService.okmakeGetOrPostCall(plmUrl + PLMConstants.POST_STYLE_URI, publishPayLoad, token);

            String responseData = publishResponse.body().string();
            logger.info("responseData -:"+responseData);

            JSONObject responseDataJSON = new JSONObject(responseData);
            JSONObject publishResponseJSON = responseDataJSON.getJSONObject(PLMConstants.PLM_DOCUMENT_JSON_KEY);
            JSONArray publishResponseTECHJSONArray = publishResponseJSON.getJSONArray(PLMConstants.PLM_TECH_SPEC_JSON_KEY);
            JSONObject publishResponseTECHJSON = publishResponseTECHJSONArray.getJSONObject(0);
            JSONArray messageJSONArray= publishResponseTECHJSON.getJSONArray(PLMConstants.PLM_MESSAGE_JSON_KEY);
            JSONObject messageJSONObject  = messageJSONArray.getJSONObject(0);

            String status = messageJSONObject.getString(PLMConstants.PLM_STATUS_JSON_KEY);

            if("SUCCESS".equalsIgnoreCase(status)) {
                logger.info("Successfully published to PLM.");
                outJson.put(PLMConstants.PLM_MESSAGE_JSON_KEY,status);
                return outJson;
            } else {
                throw new PLMException("Error while publishing data to PLM. Please try again.", HttpStatus.INTERNAL_SERVER_ERROR);
            }

        } catch (PLMException plmExc) {
            throw plmExc;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return  outJson;
    }

    /**
     * preparePublishToPLM - Function calls PLM Rest API and prepares publish response
     * @param publishData MultipartHttpServletRequest multiPartRequest
     * @param headers Map<String, String> headers
     * @return JSONObject with publish response
     * @throws PLMException exception
     */
    public JSONObject preparePublishToPLM(JSONObject publishData, Map<String, String> headers, String plmUrl) throws PLMException {
        JSONObject outJson = new JSONObject();
        logger.info("INFO::PLMHelper: preparePublishToPLM() start."+publishData);
        //String plmurl = headers.get("plm_env_url");
        String outerPath = "";
        File outerFile = null;
        try {
            JSONObject plmStyleDetails = publishData.getJSONObject(PLMConstants.PLM_STYLE_DETAILS);
            String externalStyleId = plmStyleDetails.getString(CLOSETConstants.EXTERNAL_STYLE_ID);
            logger.info("externalStyleId :"+externalStyleId);
            String owner = plmStyleDetails.getString(PLMConstants.CLOSET_OWNER_PARAM_KEY);
            String requestNo = plmStyleDetails.getString(PLMConstants.REQUEST_NO);
            JSONObject closetStyleDetails = publishData.getJSONObject(PLMConstants.CLOSET_STYLE_DETAILS);
            String closetStyleId = closetStyleDetails.getString(CLOSETConstants.CLOSET_STYLE_ID_PARAM_KEY);
            int closetStyleVersion = closetStyleDetails.getInt(CLOSETConstants.CLOSET_VERSION_PARAM_KEY);

            if(!Utility.hasContent(externalStyleId)){
                externalStyleId = owner+"-:-"+requestNo;
            }
            if(plmStyleDetails.has(CLOSETConstants.RESET_EXTERNAL_STYLE_ID) && plmStyleDetails.getBoolean(CLOSETConstants.RESET_EXTERNAL_STYLE_ID) ) {
                closetHelper.publishExternalId(closetStyleId, externalStyleId, headers);
            }

            String nanoTime = Long.toString(System.nanoTime());
            String userId = headers.get(CLOSETConstants.PLM_USER_NAME);

            if(!Utility.hasContent(userId)) {
                userId = PLMConstants.TEMP;
            }

            outerPath = CLOSETConnectorConstants.STORAGE_DIRECTORY + userId + CLOSETConnectorConstants.UNDERSCORE + nanoTime + File.separator;
            outerPath = Utility.formatOSFolderLocation(outerPath);

            logger.debug("DEBUG::PLMHelper: preparePublishToPLM() -> outerPath :: "+outerPath);

            outerFile = new File(outerPath);
            if(outerFile != null && outerFile.exists())
                FileUtils.forceDelete(outerFile);

            outerFile = new File(outerPath);

            if(outerFile != null && !outerFile.exists()) {
                outerFile.mkdir();
                logger.info("outerFile is dir - "+outerFile.isFile());
            }

            JSONObject closetAssets = closetStyleDetails.getJSONObject(PLMConstants.CLOSET_ASSETS);
            HashMap<String, File> uploadFiles = getUploadFilesFromURLs(closetAssets, outerPath);
            JSONArray renderDetails = addMissingRendersURL(closetAssets, closetStyleId, closetStyleVersion, headers);
            HashMap<String, File> renderUploadFiles = getRenderUploadFilesFromURLs(renderDetails, outerPath);
            logger.info("uploadFiles - "+uploadFiles);
            logger.info("renderUploadFiles - "+renderUploadFiles);
            if(uploadFiles.isEmpty()) {
                throw new PLMException("Error in uploading assets to PLM, no assets found. Please contact your System Administrator.", HttpStatus.INTERNAL_SERVER_ERROR);
            }
            List<File> files = uploadFiles.values().stream().collect(Collectors.toList());
            List<File> rendersFiles = new ArrayList<File>();
            if(!renderUploadFiles.isEmpty()) {
                rendersFiles = renderUploadFiles.values().stream().collect(Collectors.toList());
            }
            logger.info("files - "+files);
            logger.info("rendersFiles - "+rendersFiles);
            JSONArray uploadAttachmentsArray =  uploadAttachments(files, headers, plmUrl);
            logger.info("rendersFiles 1 - "+rendersFiles);
            JSONArray renderPublishAttachmentsArray = new JSONArray();
            if(!rendersFiles.isEmpty()) {
                rendersFiles = getUnzippedRenders(rendersFiles, outerPath);
                logger.info("rendersFiles 2 - "+rendersFiles);
                JSONArray rendersAttachmentsArray =  uploadAttachments(rendersFiles, headers, plmUrl);
                int lastRenderSeq;
                if(renderDetails.getJSONObject(0).has(CLOSETConnectorConstants.CC_LAST_RENDER_SEQ_NO_JSON_KEY) && !renderDetails.getJSONObject(0).isNull(CLOSETConnectorConstants.CC_LAST_RENDER_SEQ_NO_JSON_KEY)) {
                    lastRenderSeq = renderDetails.getJSONObject(0).getInt(CLOSETConnectorConstants.CC_LAST_RENDER_SEQ_NO_JSON_KEY);
                } else {
                    lastRenderSeq = 0;
                }

                renderPublishAttachmentsArray = prepareRendersAttachmentsArray(rendersAttachmentsArray, lastRenderSeq);
            }

            JSONArray attachmentsArray = preparePublishAttachmentsArray(publishData, uploadAttachmentsArray);

            for(int i = 0; i < renderPublishAttachmentsArray.length(); i++) {
                attachmentsArray.put(renderPublishAttachmentsArray.getJSONObject(i));
            }

            JSONObject closetTechSpec = closetHelper.getTechSpec(closetStyleId, closetStyleVersion, headers);

//            JSONObject apiMetaDataJon = closetTechSpec.getJSONObject("apiMetaData");
//            String externalStyleId = apiMetaDataJon.getString("externalStyleId");

            JSONArray colorwaysArray = filterAndAddMissingDetailsInMappedColorways(publishData, closetTechSpec);

            if(!colorwaysArray.isEmpty()) {
                colorwaysArray = prepareColorwayArray(colorwaysArray);
            }

            //Renders
            JSONObject publishPayLoad = preparePayLoadForBRStylePublish(publishData, attachmentsArray, colorwaysArray);
            HttpHeaders restHeaders = preparePlmHeaders(headers);
            String token = String.valueOf(restHeaders.get(PLMConstants.API_HEADER_TOKEN_KEY));

            Response publishResponse = restService.okmakeGetOrPostCall(plmUrl + PLMConstants.POST_STYLE_URI, publishPayLoad, token);

            String responseData = publishResponse.body().string();
            logger.info("responseData -:"+responseData);

            JSONObject responseDataJSON = new JSONObject(responseData);
            JSONObject publishResponseJSON = responseDataJSON.getJSONObject(PLMConstants.PLM_DOCUMENT_JSON_KEY);
            JSONArray publishResponseTECHJSONArray = publishResponseJSON.getJSONArray(PLMConstants.PLM_TECH_SPEC_JSON_KEY);
            JSONObject publishResponseTECHJSON = publishResponseTECHJSONArray.getJSONObject(0);
            JSONArray messageJSONArray= publishResponseTECHJSON.getJSONArray(PLMConstants.PLM_MESSAGE_JSON_KEY);
            JSONObject messageJSONObject  = messageJSONArray.getJSONObject(0);

            String status = messageJSONObject.getString(PLMConstants.PLM_STATUS_JSON_KEY);

            if(!PLMConstants.PLM_SUCCESS_STATUS_VALUE.equalsIgnoreCase(status)) {
                String messageId = messageJSONObject.getString(PLMConstants.PLM_MESSAGE_ID_JSON_KEY);
                String messageDesc = messageJSONObject.getString(PLMConstants.PLM_MESSAGE_DESC_JSON_KEY);
                throw new PLMException(messageId + CLOSETConnectorConstants.COLON + CLOSETConnectorConstants.SPACE + messageDesc, HttpStatus.INTERNAL_SERVER_ERROR);
            } else {
                //Style style = new Style();
                Style style = styleService.findByClosetStyleIdAndPlmStyleId(closetStyleId, externalStyleId);
                if(style==null){
                    style = new Style();
                }
                style.setClosetStyleId(closetStyleId);
                style.setPlmStyleId(externalStyleId);
                Date time = new Date();
                style.setCreateTimeStamp(time);
                style.setModifyTimeStamp(time);
                style.setClosetUser(headers.get("closet-user-name"));
                style.setPlmUser(headers.get("plm-user-name"));
                styleService.saveStyle(style);
                logger.info("Successfully published to PLM.");
                outJson.put(PLMConstants.PLM_MESSAGE_JSON_KEY, status);
            }

            if(outerFile != null && outerFile.exists()) {
                FileUtils.forceDelete(outerFile);
            }

            return outJson;
        } catch (PLMException plmExc) {
            if(outerFile != null && outerFile.exists()) {
                try {
                    FileUtils.forceDelete(outerFile);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
            throw plmExc;
        } catch (IOException io) {
            if(outerFile != null && outerFile.exists()) {
                try {
                    FileUtils.forceDelete(outerFile);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
            throw new PLMException(io.getLocalizedMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (CLOSETException closetExc) {
            if(outerFile != null && outerFile.exists()) {
                try {
                    FileUtils.forceDelete(outerFile);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
            throw new PLMException(closetExc.getLocalizedMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (Exception e) {
            if(outerFile != null && outerFile.exists()) {
                try {
                    FileUtils.forceDelete(outerFile);
                } catch (IOException io) {
                    throw new RuntimeException(io);
                }
            }
            throw new PLMException(e.getLocalizedMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
        //return  outJson;
    }

    /**
     *
     * @param closetTechSpec
     * @return closetColorwayList
     * @throws PLMException
     */
    public JSONArray getClosetColorwayList(JSONObject closetTechSpec) throws PLMException {
        JSONArray closetFabricList = new JSONArray();
        JSONArray closetColorwayList = new JSONArray();

        if(closetTechSpec.has(CLOSETConstants.CLOSET_TECHPACK_FABRIC_LIST_KEY)) {
            try {
                closetFabricList = closetTechSpec.getJSONArray(CLOSETConstants.CLOSET_TECHPACK_FABRIC_LIST_KEY);
            } catch (JSONException e) {
                closetFabricList = new JSONArray();
            }
        }

        if(closetFabricList.isEmpty()) {
            throw new PLMException("Not found main material/color information to publish Colorways.", HttpStatus.INTERNAL_SERVER_ERROR);
        }

        JSONObject closetFabric = new JSONObject();
        JSONObject firstClosetFabric = new JSONObject();
        for(int k = 0; k < closetFabricList.length(); k++) {
            JSONObject tempCLOSETFabric = closetFabricList.getJSONObject(k);
            firstClosetFabric = tempCLOSETFabric;
            String tempCLOSETFabricStr = tempCLOSETFabric.toString();
            if(tempCLOSETFabricStr.contains(CLOSETConstants.CLOSET_TECHPACK_CLOVISE_COLOR_ID_KEY)) {
                closetFabric = tempCLOSETFabric;
                break;
            }
        }

        if(closetFabric.isEmpty()) {
            closetFabric = firstClosetFabric;
        }

        if(closetFabric.has(CLOSETConstants.CLOSET_TECHPACK_COLORWAYS_KEY)) {
            closetColorwayList = closetFabric.getJSONArray(CLOSETConstants.CLOSET_TECHPACK_COLORWAYS_KEY);
        }

        if(closetColorwayList.isEmpty()) {
            throw new PLMException("Not found main material/color information to publish Colorways.", HttpStatus.INTERNAL_SERVER_ERROR);
        }

        return closetColorwayList;
    }

    /**
     *
     * @param data
     * @param closetTechSpec
     * @return outMappedColorwayDetails
     * @throws PLMException
     */
    public JSONArray filterAndAddMissingDetailsInMappedColorways(JSONObject data, JSONObject closetTechSpec) throws PLMException {
        JSONArray outMappedColorwayDetails = new JSONArray();

        JSONObject closetStyleDetails = data.getJSONObject(PLMConstants.CLOSET_STYLE_DETAILS);
        JSONObject closetAssets = closetStyleDetails.getJSONObject(PLMConstants.CLOSET_ASSETS);
        JSONArray mappedColorwayDetails = closetAssets.getJSONArray(PLMConstants.COLORWAY_DETAILS);
        //JSONArray outMappedColorwayDetails = new JSONArray();
        if(mappedColorwayDetails.isEmpty()) {
            logger.info("INFO::PLMHelper: filterAndAddMissingDetailsInMappedColorways() : No Colorway are mapped, skipping Colorway publish");
            return  outMappedColorwayDetails;
        }

        JSONArray closetFabricColorwayList;
        JSONArray closetColorwayList = new JSONArray();
        try {
            closetFabricColorwayList = getClosetColorwayList(closetTechSpec);
            if(closetTechSpec.has(CLOSETConstants.CLOSET_TECHPACK_COLORWAYS_KEY))
                closetColorwayList = closetTechSpec.getJSONArray(CLOSETConstants.CLOSET_TECHPACK_COLORWAYS_KEY);
        } catch (PLMException e) {
            throw new PLMException(e.getLocalizedMessage(), e.getStatusCode());
        }

        JSONObject mappedClosetColorway;
        JSONObject mappedColorwayDetail;
        JSONObject mappedPlmColorway;
        //String mappedPlmColorwayName;
        String mappedClosetColorwayName;
        JSONObject closetFabricColorway;
        JSONObject frontFabricJSON;
        JSONObject closetColorway;
        String closetColorwayName;
        String baseColorName;
        for(int i = 0; i < mappedColorwayDetails.length(); i++) {
            mappedColorwayDetail = mappedColorwayDetails.getJSONObject(i);
            mappedPlmColorway = mappedColorwayDetail.getJSONObject(PLMConstants.PLM_COLORWAY);
            mappedClosetColorway = mappedColorwayDetail.getJSONObject(PLMConstants.CLOSET_COLORWAY);
            //mappedPlmColorwayName = mappedPlmColorway.getString(PLMConstants.COLORWAY_NAME);
            mappedClosetColorwayName = mappedClosetColorway.getString(PLMConstants.COLORWAY_NAME);

            if(mappedClosetColorwayName.equalsIgnoreCase("Drop here")) {
                continue;
            }

            for(int j = 0; j < closetFabricColorwayList.length(); j++) {
                closetFabricColorway = closetFabricColorwayList.getJSONObject(j);
                frontFabricJSON = closetFabricColorway.getJSONObject(CLOSETConstants.CLOSET_TECHPACK_MATERIAL_FRONT_KEY);
                baseColorName = frontFabricJSON.getString(CLOSETConstants.CLOSET_TECHPACK_BASE_COLOR_NAME_KEY);
                //JSONObject closetFabricColorway = closetFabricColorwayList.getJSONObject(j);
                closetColorway = closetColorwayList.getJSONObject(j);
                closetColorwayName = closetColorway.getString(CLOSETConstants.CLOSET_TECHPACK_NAME_KEY);

                if(mappedClosetColorwayName.equals(closetColorwayName)) {
                    String colorNo = "";
                    if(frontFabricJSON.has(CLOSETConstants.CLOSET_TECHPACK_API_META_DATA_KEY)) {
                        JSONObject apiMetaData = frontFabricJSON.getJSONObject(CLOSETConstants.CLOSET_TECHPACK_API_META_DATA_KEY);
                        if(apiMetaData.has(CLOSETConstants.CLOSET_TECHPACK_CLOVISE_COLOR_ID_KEY)) {
                            colorNo = apiMetaData.getString(CLOSETConstants.CLOSET_TECHPACK_CLOVISE_COLOR_ID_KEY);
                        }
                    }
                    mappedClosetColorway.put(PLMConstants.COLOR_ID, colorNo);
                    mappedClosetColorway.put(PLMConstants.COLOR_NAME, baseColorName);
                    mappedPlmColorway.put(PLMConstants.COLOR_ID, colorNo);
                    mappedPlmColorway.put(PLMConstants.COLOR_NAME, baseColorName);

                    mappedColorwayDetail.put(PLMConstants.PLM_COLORWAY, mappedPlmColorway);
                    mappedColorwayDetail.put(PLMConstants.CLOSET_COLORWAY, mappedClosetColorway);

                    outMappedColorwayDetails.put(mappedColorwayDetail);
                    break;
                }
            }
        }

        return outMappedColorwayDetails;
    }

    /**
     *
     * @param files
     * @param outerPath
     * @return destFiles
     * @throws PLMException
     */
    public List<File> getUnzippedRenders(List<File> files, String outerPath) throws PLMException {
        logger.info("INFO::PLMHelper: getUnzippedRenders() -> started");
        List<File> destFiles = new ArrayList<File>();
        ZipInputStream zipIn = null;
        File destDir = null;
        try{
            for(File file: files) {
                String zipName = file.getName();
                int lastIndex = zipName.lastIndexOf(".");
                zipName = zipName.substring(0,lastIndex);

                destDir = new File(outerPath + File.separator + zipName);

                if (!destDir.exists()) {
                    destDir.mkdir();
                }

                zipIn = new ZipInputStream(new FileInputStream(file));
                ZipEntry entry = zipIn.getNextEntry();

                while (entry != null) {
                    String filePathNew = FilenameUtils.getPath(entry.getName());

                    logger.debug("DEBUG::PLMHelper: unzip() -> filePathNew :"+filePathNew);
                    if(Utility.hasContent(filePathNew)) {
                        File fileNew = new File(outerPath + filePathNew);
                        if(!fileNew.exists()) {
                            fileNew.mkdirs();
                        }
                    }

                    logger.info("unzip() entry.getName() : "+entry.getName());

                    String filePath = destDir + File.separator + entry.getName();

                    logger.debug("DEBUG::PLMHelper: unzip() -> filePath: "+filePath);
                    if (!entry.isDirectory()) {
                        Utility.extractFile(zipIn, filePath);
                    } else {
                        File dir = new File(filePath);
                        dir.mkdirs();
                    }
                    zipIn.closeEntry();

                    File[] images = destDir.listFiles();
                    for(int i = 0; i < images.length; i++) {
                        File newFile = images[i];
                        if (newFile != null) {
                            destFiles.add(newFile);
                        }
                    }
                    entry = zipIn.getNextEntry();
                }

            }
        }
        catch(Exception e) {
            e.printStackTrace();
            throw new PLMException(e.getLocalizedMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
        finally{
            try {
                zipIn.close();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        //logger.debug("DEBUG::CV3DModelAndVisualsLogic: unzip() -> images size :"+images.length);
        logger.info("INFO::PLMHelper: unzip() -> end");
        return destFiles;
    }

    /**
     *
     * @param multiPartRequest
     * @param outerFile
     * @return uploadFileNames
     * @throws PLMException
     */
    public HashMap<String, File> getUploadFiles(MultipartHttpServletRequest multiPartRequest, String outerFile) throws PLMException {
        HashMap<String, File> uploadFileNames = new HashMap<String, File>();
        try {
            String fileName;
            String filePath;
            File uploadFile;
            Iterator<String> fileNamesItr = multiPartRequest.getFileNames();
            while (fileNamesItr.hasNext()) {
                fileName = fileNamesItr.next();
                logger.debug("DEBUG::PLMHelper: preparePublishToPLM() fileName - " + fileName);
                MultipartFile file = multiPartRequest.getFile(fileName);
                logger.debug("DEBUG::PLMHelper: preparePublishToPLM() file - " + file.getName());
                filePath = outerFile + fileName;
                uploadFile = new File(filePath);
                file.transferTo(new File(filePath));
                uploadFileNames.put(fileName, uploadFile);
                //Method to upload files to BR
                //Input HashMap of fileName, file
            }
        } catch (IOException ioExc) {
            throw new PLMException(ioExc.getLocalizedMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return uploadFileNames;
    }

    /**
     *
     * @param closetAssets
     * @param outerPath
     * @return uploadFileNames
     * @throws PLMException
     */
	public HashMap<String, File> getUploadFilesFromURLs(JSONObject closetAssets, String outerPath) throws PLMException {
        HashMap<String, File> uploadFileNames = new HashMap<String, File>();

        JSONObject projectFileDetails = closetAssets.getJSONObject(PLMConstants.PROJECT_FILE_DETAILS);
        String projectFileUrl = projectFileDetails.getString(PLMConstants.FILE_URL);
        String projectFileName = projectFileDetails.getString(PLMConstants.FILENAME);

        JSONObject thumbnailDetails = closetAssets.getJSONObject(PLMConstants.THUMBNAIL_DETAILS);
        String thumbnailUrl = thumbnailDetails.getString(PLMConstants.FILE_URL);
        String thumbnailFileName = thumbnailDetails.getString(PLMConstants.FILENAME);
        try {
            String filePath;
            URL url;
            File uploadFile;

            if(Utility.hasContent(projectFileUrl)) {
                filePath = outerPath + projectFileName;
                uploadFile = new File(filePath);

                url = new URL(projectFileUrl);
                URLConnection conn = url.openConnection();

                conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:31.0) Gecko/20100101 Firefox/31.0");
                conn.connect();

                FileUtils.copyInputStreamToFile(conn.getInputStream(), uploadFile);


                uploadFileNames.put(uploadFile.getName(), uploadFile);
            }

            if(Utility.hasContent(thumbnailUrl)) {
                filePath = outerPath + thumbnailFileName;
                uploadFile = new File(filePath);

                url = new URL(thumbnailUrl);
                URLConnection conn = url.openConnection();

                conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:31.0) Gecko/20100101 Firefox/31.0");
                conn.connect();

                FileUtils.copyInputStreamToFile(conn.getInputStream(), uploadFile);

                uploadFileNames.put(uploadFile.getName(), uploadFile);
            }
        } catch (IOException ioExc) {
            logger.info("ioExc - "+ioExc.getLocalizedMessage());
            throw new PLMException(ioExc.getLocalizedMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return uploadFileNames;
    }

    /**
     *
     * @param closetAssets
     * @param closetStyleId
     * @param closetStyleVersion
     * @param headers
     * @return outRendersDetails
     * @throws PLMException
     */
    public JSONArray addMissingRendersURL(JSONObject closetAssets, String closetStyleId, int closetStyleVersion, Map<String, String> headers) throws PLMException {
        JSONArray rendersDetails = closetAssets.getJSONArray(PLMConstants.RENDERS_DETAILS);
        JSONArray outRendersDetails = new JSONArray();
        if (rendersDetails.length() > 0) {
            JSONObject rendersDetail = rendersDetails.getJSONObject(0);
            if(!rendersDetail.has(PLMConstants.RENDER_SEQ_NO_JSON_KEY)) {
                return outRendersDetails;
            }

            int renderSeq = rendersDetail.getInt(PLMConstants.RENDER_SEQ_NO_JSON_KEY);
            if(!Utility.hasContent(String.valueOf(renderSeq))) {
                logger.info("No Render info found, Skipping Renders publish.");
                return outRendersDetails;
            }
            try {
                String rendersURL = closetHelper.getRenderURL(closetStyleId, closetStyleVersion, renderSeq, headers);
                rendersDetail.put(PLMConstants.FILE_URL, rendersURL);
                outRendersDetails.put(rendersDetail);
            } catch (CLOSETException e) {
                throw new PLMException(e.getLocalizedMessage(), e.getStatusCode());
            }
        }
        return outRendersDetails;
    }

    /**
     *
     * @param renderDetails
     * @param outerPath
     * @return uploadFileNames
     * @throws PLMException
     */
    public HashMap<String, File> getRenderUploadFilesFromURLs(JSONArray renderDetails, String outerPath) throws PLMException {
        HashMap<String, File> uploadFileNames = new HashMap<String, File>();

        //JSONArray rendersDetails = closetAssets.getJSONArray(PLMConstants.RENDERS_DETAILS);

        try {
            if (renderDetails.length() > 0) {
                String rendersUrl = renderDetails.getJSONObject(0).getString(PLMConstants.FILE_URL);
                String rendersFileName = renderDetails.getJSONObject(0).getString(PLMConstants.FILENAME);
                String filePath;
                URL url;
                File uploadFile;

                if (Utility.hasContent(rendersUrl)) {
                    filePath = outerPath + rendersFileName;
                    uploadFile = new File(filePath);

                    url = new URL(rendersUrl);
                    URLConnection conn = url.openConnection();

                    conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:31.0) Gecko/20100101 Firefox/31.0");
                    conn.connect();

                    FileUtils.copyInputStreamToFile(conn.getInputStream(), uploadFile);

                    uploadFileNames.put(uploadFile.getName(), uploadFile);
                }
            }
        } catch (IOException ioExc) {
            logger.error("ioExc - " + ioExc.getLocalizedMessage());
            throw new PLMException(ioExc.getLocalizedMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return uploadFileNames;
    }

    /**
     *
     * @param files
     * @param headers
     * @return locationsJSON
     * @throws PLMException
     */
    public JSONArray uploadAttachments(List<File> files, Map<String, String> headers, String plmurl) throws PLMException {
        HttpHeaders httpHeaders = preparePlmHeaders(headers);
        httpHeaders.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);

        MultiValueMap<String, Object> multiPartsBody = Utility.prepareMultipartFilesBody(files);
        ResponseEntity<String> responseEntity = restService.uploadMultipartFormData(multiPartsBody, plmurl + PLMConstants.UPLOAD_ATTACHMENT_URI, httpHeaders);
        JSONObject responseJSON = new JSONObject(responseEntity.getBody());
        JSONObject documentJSON = responseJSON.getJSONObject(PLMConstants.PLM_DOCUMENT_JSON_KEY);
        JSONArray locationsJSON = documentJSON.getJSONArray(PLMConstants.PLM_LOCATION_KEY);
        logger.info("locationsJSON - "+locationsJSON);
        return locationsJSON;
    }

    /**
     *
     * @param inputAttachmentsArray
     * @param lastRenderSeq
     * @return outputAttachmentsArray
     * @throws PLMException
     */
    public JSONArray prepareRendersAttachmentsArray(JSONArray inputAttachmentsArray, int lastRenderSeq) throws PLMException {
        JSONArray outputAttachmentsArray = new JSONArray();

        JSONObject inputAttachmentJson;
        JSONObject outputAttachmentJson;
        String inputAttachmentName;
        String attachmentNo;
        int attachmentNoSuffix;
        for (int j = 0; j < inputAttachmentsArray.length(); j++) {
            outputAttachmentJson = new JSONObject();
            attachmentNoSuffix = lastRenderSeq + j + 1;
            inputAttachmentJson = inputAttachmentsArray.getJSONObject(j);
            if(inputAttachmentJson.isEmpty()) {
                continue;
            }
            attachmentNo = "CLOIMAGE_" + attachmentNoSuffix;
            JSONObject messagesJson = inputAttachmentJson.getJSONObject(PLMConstants.PLM_MESSAGES_JSON_KEY);
            String status = messagesJson.getString(PLMConstants.PLM_STATUS_JSON_KEY);
            if(!PLMConstants.PLM_SUCCESS_STATUS_VALUE.equalsIgnoreCase(status)) {
                JSONArray messageArray = messagesJson.getJSONArray(PLMConstants.PLM_MESSAGE_JSON_KEY);
                String messageId = "";
                String messageDesc = "";
                for (int i = 0; i<messageArray.length(); i++){
                    JSONObject messageJson = messageArray.getJSONObject(i);
                    messageId = messageJson.getString(PLMConstants.PLM_MESSAGE_ID_JSON_KEY);
                    messageDesc = messageJson.getString(PLMConstants.PLM_MESSAGE_DESC_JSON_KEY);
                }
                throw new PLMException(messageId + CLOSETConnectorConstants.COLON + CLOSETConnectorConstants.SPACE + messageDesc, HttpStatus.INTERNAL_SERVER_ERROR);

            }

            outputAttachmentJson.put(PLMConstants.PLM_LOCATION_JSON_KEY, inputAttachmentJson.getString(PLMConstants.PLM_NEW_NAME_JSON_KEY));
            outputAttachmentJson.put(PLMConstants.PLM_ATTACHMENT_NO_JSON_KEY, attachmentNo);
            outputAttachmentsArray.put(outputAttachmentJson);
        }
        return outputAttachmentsArray;
    }

    /**
     *
     * @param publishJson
     * @param inputAttachmentsArray
     * @return outputAttachmentsArray
     * @throws PLMException
     */
    public JSONArray preparePublishAttachmentsArray(JSONObject publishJson, JSONArray inputAttachmentsArray) throws PLMException {
        JSONArray outputAttachmentsArray = new JSONArray();

        JSONObject closetStyleDetails = publishJson.getJSONObject(PLMConstants.CLOSET_STYLE_DETAILS);
        JSONObject closetAssets = closetStyleDetails.getJSONObject(PLMConstants.CLOSET_ASSETS);
        JSONObject projectFileDetails = closetAssets.getJSONObject(PLMConstants.PROJECT_FILE_DETAILS);
        JSONObject thumbnailDetails = closetAssets.getJSONObject(PLMConstants.THUMBNAIL_DETAILS);
        String zprjFileName = projectFileDetails.getString(PLMConstants.FILENAME);
        String thumbnailFileName = thumbnailDetails.getString(PLMConstants.FILENAME);

        JSONObject inputAttachmentJson;
        JSONObject outputAttachmentJson;
        String inputAttachmentName;
        String attachmentNo;
        for (int j = 0; j < inputAttachmentsArray.length(); j++) {
            outputAttachmentJson = new JSONObject();
            inputAttachmentJson = inputAttachmentsArray.getJSONObject(j);
            if(inputAttachmentJson.isEmpty()) {
                continue;
            }

            JSONObject messagesJson = inputAttachmentJson.getJSONObject(PLMConstants.PLM_MESSAGES_JSON_KEY);
            logger.info("messageJson :"+messagesJson);
            String status = messagesJson.getString(PLMConstants.PLM_STATUS_JSON_KEY);
            logger.info("status :"+status);
            if(!PLMConstants.PLM_SUCCESS_STATUS_VALUE.equalsIgnoreCase(status)) {
                JSONArray messageArray = messagesJson.getJSONArray(PLMConstants.PLM_MESSAGE_JSON_KEY);
                String messageId = "";
                String messageDesc = "";
                for (int i = 0; i<messageArray.length(); i++){
                    JSONObject messageJson = messageArray.getJSONObject(i);
                    messageId = messageJson.getString(PLMConstants.PLM_MESSAGE_ID_JSON_KEY);
                    messageDesc = messageJson.getString(PLMConstants.PLM_MESSAGE_DESC_JSON_KEY);
                }
                throw new PLMException(messageId + CLOSETConnectorConstants.COLON + CLOSETConnectorConstants.SPACE + messageDesc, HttpStatus.INTERNAL_SERVER_ERROR);
            }

            inputAttachmentName = inputAttachmentJson.getString(PLMConstants.PLM_OLD_NAME_JSON_KEY);
            logger.info("inputAttachmentName :"+inputAttachmentName);

            if(zprjFileName.equals(inputAttachmentName)) {
                attachmentNo = PLMConstants.PLM_ATTACHMENT_NO_FOR_ZPRJ;
            } else if(thumbnailFileName.equals(inputAttachmentName)) {
                attachmentNo = PLMConstants.PLM_ATTACHMENT_NO_FOR_THUMNAIL;
            } else {
                continue;
            }
            logger.info("inputAttachmentJson :"+inputAttachmentJson);

            outputAttachmentJson.put(PLMConstants.PLM_LOCATION_JSON_KEY, inputAttachmentJson.getString(PLMConstants.PLM_NEW_NAME_JSON_KEY));
            outputAttachmentJson.put(PLMConstants.PLM_ATTACHMENT_NO_JSON_KEY, attachmentNo);
            outputAttachmentsArray.put(outputAttachmentJson);
        }
        return outputAttachmentsArray;
    }

    /**
     *
     * @param publishJson
     * @return prepareColorwayArray
     * @throws PLMException
     */
    public JSONArray prepareColorwayArray(JSONObject publishJson) throws PLMException {
        JSONObject closetStyleDetails = publishJson.getJSONObject(PLMConstants.CLOSET_STYLE_DETAILS);
        JSONObject closetAssets = closetStyleDetails.getJSONObject(PLMConstants.CLOSET_ASSETS);
        JSONArray colorwayDetails = closetAssets.getJSONArray(PLMConstants.COLORWAY_DETAILS);

        return prepareColorwayArray(colorwayDetails);
    }

    /**
     *
     * @param inputMappedColorways
     * @return outputColorwaysArray
     * @throws PLMException
     */
    public JSONArray prepareColorwayArray(JSONArray inputMappedColorways) throws PLMException {
        logger.info("prepareColorwayArray - inputMappedColorways - "+inputMappedColorways);
        JSONArray outputColorwaysArray = new JSONArray();

        JSONObject colorwayDetail;
        JSONObject closetColorway;
        JSONObject plmColorway;
        JSONObject colorwayJson;
        String colorNo;
        String colorName;
        String colorwayName;
        String assocId;
        for(int i = 0; i < inputMappedColorways.length(); i++) {
            colorwayJson = new JSONObject();
            colorNo = "";
            assocId = "";
            colorwayDetail = inputMappedColorways.getJSONObject(i);
            closetColorway = colorwayDetail.getJSONObject(PLMConstants.CLOSET_COLORWAY);
            plmColorway = colorwayDetail.getJSONObject(PLMConstants.PLM_COLORWAY);

            if(closetColorway.has(PLMConstants.COLOR_ID))
                colorNo = closetColorway.getString(PLMConstants.COLOR_ID);

            colorName = closetColorway.getString(PLMConstants.COLOR_NAME);
            colorwayName = closetColorway.getString(PLMConstants.COLORWAY_NAME);

            if(plmColorway.has(PLMConstants.ASSOC_ID))
                assocId = plmColorway.getString(PLMConstants.ASSOC_ID);

            if(Utility.hasContent(assocId))
                colorwayName = plmColorway.getString(PLMConstants.COLORWAY_NAME);

            colorwayJson.put(PLMConstants.PLM_COLOR_NO_KEY, colorNo);
            colorwayJson.put(PLMConstants.PLM_COLOR_NAME_KEY, colorName);
            colorwayJson.put(PLMConstants.PLM_COLORWAY_NAME_KEY, colorwayName);
            colorwayJson.put(PLMConstants.PLM_ASSOC_ID_JSON_KEY, assocId);
            outputColorwaysArray.put(colorwayJson);
        }

        return outputColorwaysArray;
    }

    /**
     *
     * @param publishJson
     * @param attachmentsArray
     * @param mappedColorways
     * @return payLoadJsonObject
     */
    public JSONObject preparePayLoadForBRStylePublish(JSONObject publishJson, JSONArray attachmentsArray, JSONArray mappedColorways) {
        JSONObject documentJsonObject = new JSONObject();
        JSONObject payLoadJsonObject = new JSONObject();
        JSONObject techspecJsonObject = new JSONObject();
        OrderedJSONObject quoteJsonObject = new OrderedJSONObject();
        JSONArray quoteJsonArray = new JSONArray();
        JSONArray techspecJsonArray = new JSONArray();

        JSONObject plmStylesDetails = publishJson.getJSONObject(PLMConstants.PLM_STYLE_DETAILS);
        //quoteJSONObject.put("@owner", publishJSON.getString("@owner"));//TRADESTONE
        quoteJsonObject.put(PLMConstants.PLM_REQUEST_NO_KEY, plmStylesDetails.getString(PLMConstants.REQUEST_NO));//BLUE030521
        //quoteJSONObject.put("@season", publishJSON.getString("@season"));//FALL 2018
        quoteJsonObject.put(PLMConstants.PLM_ATTACHMENT_JSON_KEY, attachmentsArray);
        quoteJsonObject.put(PLMConstants.COLORWAYS, mappedColorways);
        quoteJsonArray.put(quoteJsonObject);
        techspecJsonObject.put(PLMConstants.QUOTE, quoteJsonArray);
        techspecJsonArray.put(techspecJsonObject);

        documentJsonObject.put(PLMConstants.PLM_TECH_SPEC_JSON_KEY, techspecJsonArray);
        payLoadJsonObject.put(PLMConstants.PLM_DOCUMENT_JSON_KEY, documentJsonObject);

        return payLoadJsonObject;
    }

    /**
     *
     * @param headers
     * @return PLMViewFields JSON
     * @throws PLMException
     */
    public JSONObject prepareGetPLMViewResponse(HttpHeaders headers) throws PLMException {
        headers.add(HttpHeaders.ACCEPT,MediaType.APPLICATION_JSON_VALUE);
        String filename = "/" + "config" + "/" + "BR" + "/" + "PLMViewFields.json";

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
     * @return PLMResultsFields JSON
     * @throws PLMException
     */
    public JSONObject prepareGetPLMResultResponse(HttpHeaders headers) throws PLMException {
        headers.add(HttpHeaders.ACCEPT,MediaType.APPLICATION_JSON_VALUE);
        String filename = "/" + "config" + "/" + "BR" + "/" + "PLMResultsFields.json";
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
     * @param deptCodeValue
     * @param headers
     * @return Dept display value
     */
    private String getDeptDisplayValue(String deptCodeValue, HttpHeaders headers, String plmurl) {
        String url = plmurl + PLMConstants.DEPARTMENT_SEARCH_RESULTS_API;
        url = url + CLOSETConnectorConstants.QUESTION_MARK + PLMConstants.PLM_CODE_PARAM_KEY + CLOSETConnectorConstants.ASSIGN + deptCodeValue;
        ResponseEntity<String> responseEntity1 = restService.makeGetOrPostCall(url, HttpMethod.GET, headers, new JSONObject());
        String responseData1 = responseEntity1.getBody();
        JSONObject responseDataJSON1 = new JSONObject(responseData1);

        JSONObject docJSON = responseDataJSON1.getJSONObject(PLMConstants.PLM_DOCUMENT_JSON_KEY);
        JSONArray deptCodesArray = docJSON.getJSONArray(PLMConstants.PLM_DEPARTMENT_CODES_JSON_KEY);
        // JSONObject brandDescJson = brandCodesArray.getJSONObject(0);
        int index = 0;
        String deptValue = null;
        for (int j = 0; j < deptCodesArray.length(); j++) {
            JSONObject rec = deptCodesArray.getJSONObject(j);
            String deptCodeJson = rec.getString(PLMConstants.PLM_CODE_JSON_KEY);
            index = j;
            if (deptCodeJson.trim().equals(deptCodeValue.trim())) {
                JSONObject deptDescJson = deptCodesArray.getJSONObject(index);
                deptValue = deptDescJson.getString(PLMConstants.PLM_DESCRIPTION_JSON_KEY);
                break;
            }
        }

        if(!Utility.hasContent(deptValue))
            deptValue = deptCodeValue;

        return deptValue;
    }

    /**
     *
     * @param brandCodeValue
     * @param headers
     * @return Brand disply value
     */
    private String getBrandDisplayValue(String brandCodeValue, HttpHeaders headers, String plmurl) {
        String url2 = plmurl + PLMConstants.BRAND_SEARCH_RESULTS_API;
        url2 = url2 + CLOSETConnectorConstants.QUESTION_MARK + PLMConstants.PLM_CODE_PARAM_KEY + CLOSETConnectorConstants.ASSIGN + brandCodeValue;

        ResponseEntity<String> responseEntity1 = restService.makeGetOrPostCall(url2, HttpMethod.GET, headers, new JSONObject());
        String responseData1 = responseEntity1.getBody();
        JSONObject responseDataJSON1 = new JSONObject(responseData1);

        JSONObject docJSON = responseDataJSON1.getJSONObject(PLMConstants.PLM_DOCUMENT_JSON_KEY);
        JSONArray brandCodesArray = docJSON.getJSONArray(PLMConstants.PLM_BRAND_CODES_JSON_KEY);
        // JSONObject brandDescJson = brandCodesArray.getJSONObject(0);
        int index = 0;
        String brandValue = null;
        for (int j = 0; j < brandCodesArray.length(); j++) {
            JSONObject rec = brandCodesArray.getJSONObject(j);
            String brandDCodeJson = rec.getString(PLMConstants.PLM_CODE_JSON_KEY);
            index = j;
            if(brandDCodeJson.trim().equals(brandCodeValue.trim())){
                JSONObject brandDescJson = brandCodesArray.getJSONObject(index);
                brandValue = brandDescJson.getString(PLMConstants.PLM_DESCRIPTION_JSON_KEY);
                break;
            }
        }

        if(!Utility.hasContent(brandValue))
            brandValue = brandCodeValue;

        return brandValue;
    }

    /**
     *
     * @param divisionCodeValue
     * @param headers
     * @return Division value
     */
    private String getDivisionDisplayValue(String divisionCodeValue, HttpHeaders headers, String plmurl) {
        String url = plmurl + PLMConstants.DIVISION_SEARCH_RESULTS_API;
        url = url + CLOSETConnectorConstants.QUESTION_MARK + PLMConstants.PLM_CODE_PARAM_KEY + CLOSETConnectorConstants.ASSIGN + divisionCodeValue;
        ResponseEntity<String> responseEntity1 = restService.makeGetOrPostCall(url, HttpMethod.GET, headers, new JSONObject());
        String responseData1 = responseEntity1.getBody();
        JSONObject responseDataJSON1 = new JSONObject(responseData1);

        JSONObject docJSON = responseDataJSON1.getJSONObject(PLMConstants.PLM_DOCUMENT_JSON_KEY);
        JSONArray divisionCodesArray = docJSON.getJSONArray(PLMConstants.PLM_DIVISION_CODES_JSON_KEY);
        // JSONObject brandDescJson = brandCodesArray.getJSONObject(0);
        int index = 0;
        String divisionValue = null;
        for (int j = 0; j < divisionCodesArray.length(); j++) {
            JSONObject rec = divisionCodesArray.getJSONObject(j);
            String divisionCodeJson = rec.getString(PLMConstants.PLM_CODE_JSON_KEY);
            index = j;
            if (divisionCodeJson.trim().equals(divisionCodeValue.trim())) {
                JSONObject divisionDescJson = divisionCodesArray.getJSONObject(index);
                divisionValue = divisionDescJson.getString(PLMConstants.PLM_DESCRIPTION_JSON_KEY);
                break;
            }
        }

        if(!Utility.hasContent(divisionValue))
            divisionValue = divisionCodeValue;

        return divisionValue;
    }
    
    /**
     * This method is used to check license is valid or not
     * @throws CLOSETException
     * @throws PLMException 
     * @throws UnknownHostException 
     */
    public void validateLicense(String userid) throws PLMException {
        logger.info("INFO::CLOSETService: validateLicense() -> Started");
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE);
        headers.add(HttpHeaders.AUTHORIZATION, "Basic QmFtYm9vUm9zZTowTmtHME9wbXNRc2ZGU1QydnIvanpneGRIdnJpTWxUTDQ1S1dSR3VEaUlzPQ==");
        headers.add("X-User-Product", "clovise");
//        System.out.println(environment.getProperty("java.rmi.server.hostname"));
//        System.out.println(environment.getProperty("local.server.port"));
//        System.out.println(InetAddress.getLocalHost().getHostAddress());
//        String ip = InetAddress.getLocalHost().getHostAddress();
//        System.out.println("ip :"+ip);
		/*
		 * JSONObject requestBody = new JSONObject();
		 * requestBody.put(Constants.MACHINE_NAME, "GOVISE-ASSET");
		 * requestBody.put(Constants.PRIVATE_IP, "192.168.0,1");
		 * requestBody.put(Constants.COMPANY_NAME, "BambooRose");
		 * requestBody.put(Constants.USERID, "CLO");
		 * requestBody.put(Constants.PUBLIC_IP, "192.168.0,1");
		 * System.out.println("headers :"+headers);
		 * System.out.println("requestBody :"+requestBody);
		 */
        
        StringBuilder builder = new StringBuilder();
        builder.append(Constants.MACHINE_NAME+"=GOVISE-ASSET");
        builder.append("&");
        builder.append(Constants.PRIVATE_IP+"=192.168.0.1");
        builder.append("&");
        builder.append(Constants.COMPANY_NAME+"="+companyName);
        builder.append("&");
        builder.append(Constants.USERID+"="+userid);
        builder.append("&");
        builder.append(Constants.PUBLIC_IP+"=");
        
        ResponseEntity<String> response = restService.doGetOrPostCall("https://api.clo3d.com/plugin/govise/login", HttpMethod.POST, headers, builder.toString());
        JSONObject loginResponseJSON = new JSONObject(response.getBody());
        int resultCode = loginResponseJSON.getInt(Constants.RESULT_CODE);
        if (resultCode == 0 || resultCode == 3) {
            throw new PLMException(Constants.LICENSE_NOT_VALID_MSG, HttpStatus.INTERNAL_SERVER_ERROR);
        }
        else if (resultCode == 1) {
            System.out.println("Success.");
        }
        else if (resultCode == 2) {
            throw new PLMException(Constants.USER_ALREADY_ACTIVE, HttpStatus.INTERNAL_SERVER_ERROR);
        }
        else if (resultCode == 4) {
            throw new PLMException(Constants.LICENSE_EXPIRED, HttpStatus.INTERNAL_SERVER_ERROR);
        }
        else if (resultCode == 5) {
            throw new PLMException(Constants.LICENSE_LIMIT_EXCEEDED, HttpStatus.INTERNAL_SERVER_ERROR);
        }
        else if (resultCode == 101) {
            throw new PLMException(Constants.LICENSE_ERROR_CODE_101_MSG, HttpStatus.INTERNAL_SERVER_ERROR);
        }
        else if (resultCode == 102) {
            throw new PLMException(Constants.LICENSE_ERROR_CODE_102_MSG, HttpStatus.INTERNAL_SERVER_ERROR);
        }
        logger.info("INFO::CLOSETService: validateLicense() -> end");
    }
    
    public JSONObject licenseLogout(String userId) throws PLMException {
        logger.info("INFO::CLOSETService: validateLicense() -> Started");
        JSONObject outJson = new JSONObject();
        HttpHeaders headers = new HttpHeaders();
        //headers.add(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
        headers.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE);
        headers.add(HttpHeaders.AUTHORIZATION, "Basic QmFtYm9vUm9zZTowTmtHME9wbXNRc2ZGU1QydnIvanpneGRIdnJpTWxUTDQ1S1dSR3VEaUlzPQ==");
        headers.add("X-User-Product", "clovise");
        boolean loggedOut = false;
        StringBuilder builder = new StringBuilder();
        builder.append(Constants.MACHINE_NAME+"=GOVISE-ASSET");
        builder.append("&");
        builder.append(Constants.PRIVATE_IP+"=192.168.0.1");
        builder.append("&");
        builder.append(Constants.COMPANY_NAME+"="+companyName);
        builder.append("&");
        builder.append(Constants.USERID+"="+userId);
        builder.append("&");
        builder.append(Constants.PUBLIC_IP+"=");
        ResponseEntity<String> response = restService.doGetOrPostCall("https://api.clo3d.com/plugin/govise/logout", HttpMethod.POST, headers, builder.toString());
        JSONObject loginResponseJSON = new JSONObject(response.getBody());
        int resultCode = loginResponseJSON.getInt(Constants.RESULT_CODE);
    	if (resultCode == 0) {
    		throw new PLMException(Constants.LOGOUT_UNSUCCESSFUL_MSG, HttpStatus.INTERNAL_SERVER_ERROR);
    	}
    	else if (resultCode == 1) {
    		loggedOut = true;
    	}
    	else if (resultCode == 101) {
    		throw new PLMException(Constants.LICENSE_ERROR_CODE_101_MSG, HttpStatus.INTERNAL_SERVER_ERROR);
    	}
    	else if (resultCode == 102) {
    		throw new PLMException(Constants.LICENSE_ERROR_CODE_102_MSG, HttpStatus.INTERNAL_SERVER_ERROR);
    	}
        logger.info("INFO::CLOSETService: validateLicense() -> end");
        outJson.put("loggedOut", loggedOut);
        return outJson;
    }
    
    /**
     * 
     * @param headers
     * @return PLMSetting Json
     */
	public JSONObject prepareGetPLMSettingResponse(HttpHeaders headers) throws PLMException {
		 headers.add(HttpHeaders.ACCEPT,MediaType.APPLICATION_JSON_VALUE);
	        String filename = "/" + "config" + "/" + "BR" + "/" + "PLMSettings.json";

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

}
