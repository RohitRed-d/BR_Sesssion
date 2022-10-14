package com.gv.csc.service;

import com.gv.csc.exceptions.CLOSETException;
import com.gv.csc.helper.CLOSETHelper;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * CLOSETService - Service class containing the service logic for speaking with CLOSET APIs
 */
@Service
public class CLOSETService {
	
    @Autowired
    private CLOSETHelper closetHelper;

    /**
     * closetLogin - Service function used to login to CLOSET and get response
     *
     * @param username String username
     * @param password String password
     * @param groupToken Boolean groupToken
     * @return JSON Object with connection details
     * @throws CLOSETException exception
     */
    public JSONObject closetLogin(String username, String password, Boolean groupToken) throws CLOSETException {
        return closetHelper.prepareLoginResponse(username, password, groupToken);
    }

    /**
     * searchClosetStyle - Service function used to Search Styles in CLOSET and get response
     * @param searchTerm String searchTerm
     * @param filter int filter
     * @param headers Map<String, String> headers
     * @return JSON Object with connection details
     * @throws CLOSETException exception
     */
    public JSONObject searchClosetStyle(String searchTerm, int filter, Map<String, String> headers) throws CLOSETException {
        return closetHelper.prepareSearchStyleResponse(searchTerm, filter, headers);
    }

    /**
     * getClosetStyle - Service function used to getStyle from CLOSET and response
     *
     * @param styleId String styleId
     * @param httpHeaders Map<String, String> httpHeaders
     * @return JSON Object with connection details
     * @throws CLOSETException exception
     */
    public JSONObject getClosetStyle(String styleId, int version, Map<String, String> httpHeaders) throws CLOSETException {
        return closetHelper.prepareGetStyleResponse(styleId, version, httpHeaders);
    }

    /**
     * getClosetGroups - Service function used to getGroups from CLOSET and response
     * @param headers Map<String, String> headers
     * @return JSON Object with connection details
     * @throws CLOSETException exception
     */
    public JSONObject getClosetGroups(Map<String, String> headers) throws CLOSETException {
        return closetHelper.prepareGetGroupResponse(headers);
    }

    /**
     * getRenders - Service function used to getRenders from CLOSET and response
     *
     * @param styleId String styleId
     * @param version int version
     * @param httpHeaders Map httpHeaders
     * @return JSON Object with connection details
     * @throws CLOSETException exception
     */
    public JSONObject getRenders(String styleId, int version, Map<String, String> httpHeaders) throws CLOSETException {
        return closetHelper.prepareGetRenderResponse(styleId,version, httpHeaders);
    }

    /**
     * getColorways - Service function used to getColorways from CLOSET and response
     *
     * @return JSON Object with connection details
     * @throws CLOSETException exception
     */
    public JSONObject getColorways(String styleId, int version, Map<String, String> headers) throws CLOSETException {
        return closetHelper.prepareGetColorwaysResponse(styleId, version, headers);
    }

    /**
     * getClosetViewConfig - Service function used to get response of getClosetViewConfig
     * @param headers
     * @return JSON Object with connection details
     * @throws CLOSETException
     */
    public JSONObject getClosetViewConfig(HttpHeaders headers) throws CLOSETException {
        return closetHelper.prepareGetClosetViewResponse(headers);
    }

    /**
     * getClosetResultConfig - Service function used to get response of getClosetResultConfig
     * @param headers
     * @return JSON Object with connection details
     * @throws CLOSETException
     */
    public JSONObject getClosetResultConfig(HttpHeaders headers) throws CLOSETException{
        return closetHelper.prepareGetClosetResultResponse(headers);
    }

    /**
     * getZprjFile - Service function used to getStyle from CLOSET and response
     *
     * @param styleId String styleId
     * @param httpHeaders Map<String, String> httpHeaders
     * @return JSON Object with connection details
     * @throws CLOSETException exception
     */
    public JSONObject getZprjFile(String styleId, int version, Map<String, String> httpHeaders) throws CLOSETException {
        return closetHelper.prepareGetZprjFileResponse(styleId, version, httpHeaders);
    }

    /**
     * getZprjFile - Service function used to getStyle from CLOSET and response
     *
     * @param styleId String styleId
     * @param httpHeaders Map<String, String> httpHeaders
     * @return JSON Object with connection details
     * @throws CLOSETException exception
     */
    public JSONObject getRenderZipFile(String styleId, int renderSeq, Map<String, String> httpHeaders) throws CLOSETException {
        return closetHelper.prepareGetRenderZipFileResponse(styleId, renderSeq, httpHeaders);
    }
}
