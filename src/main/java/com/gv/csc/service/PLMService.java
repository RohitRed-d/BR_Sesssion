package com.gv.csc.service;

import com.gv.csc.exceptions.CLOSETException;
import com.gv.csc.exceptions.PLMException;
import com.gv.csc.helper.PLMHelper;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import java.net.UnknownHostException;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

/**
 * PLMService - Service class containing the service logic for speaking with PLM APIs
 */
@Service
public class PLMService {

    @Autowired
    private PLMHelper plmHelper;

    /**
     * plmLogin - Service function used to login to PLM and get response
     *
     * @param username String username
     * @param password String password
     * @return JSONObject
     * @throws PLMException exception
     * @throws UnknownHostException 
     */
    public JSONObject plmLogin(String username, String password, String plmUrl, @RequestHeader Map<String, String> headers) throws PLMException, CLOSETException {
        return plmHelper.prepareLoginResponse(username, password, plmUrl, headers);
    }

    /**
     * searchPlmStyle - Service Function prepares Styles Search response using necessary helpers
     * @param searchTerm String searchTerm
     * @param httpHeaders Map<String, String> httpHeaders
     * @return JSONObject of Styles
     * @throws PLMException exception
     */
    public JSONObject searchPlmStyle(String searchTerm, Map<String, String> httpHeaders, String plmUrl) throws PLMException {
        return plmHelper.prepareSearchStyleResponse(searchTerm, httpHeaders, plmUrl);
    }

    /**
     *  getPlmStyle - Service function used to get Style from PLM and get response
     * @param requestNo String requestNo
     * @param owner String owner
     * @param headers Map<String, String> headers
     * @return JSONObject
     * @throws PLMException exception
     */
    public JSONObject getPlmStyle(String requestNo, String owner, Map<String, String> headers, String plmUrl) throws PLMException {
        return plmHelper.prepareGetStyleResponse(requestNo, owner, headers, plmUrl);
    }

    /**
     * publishToPLM - Service function used to get Style from PLM and get response
     * @param multiPartRequest MultipartHttpServletRequest multiPartRequest
     * @param httpHeaders Map<String, String> httpHeaders
     * @return JSONObject
     * @throws PLMException exception
     */
    public JSONObject publishToPLM(MultipartHttpServletRequest multiPartRequest, Map<String, String> httpHeaders, String plmUrl) throws PLMException{
        return plmHelper.preparePublishToPLM(multiPartRequest, httpHeaders, plmUrl);
    }

    /**
     * getPLMViewConfig - Service function used to get response of getPLMViewConfig
     * @param headers
     * @return JSON Object with connection details
     * @throws PLMException
     */
    public JSONObject getPLMViewConfig(HttpHeaders headers) throws PLMException{
        return plmHelper.prepareGetPLMViewResponse(headers);
    }

    /**
     * getPLMResultConfig - Service function used to get response of getPLMResultConfig
     * @param headers
     * @return JSON Object with connection details
     * @throws PLMException
     */
    public JSONObject getPLMResultConfig(HttpHeaders headers) throws PLMException{
        return plmHelper.prepareGetPLMResultResponse(headers);
    }
	/** publishToPLM - Service function used to get Style from PLM and get response
     * @param publishData MultipartHttpServletRequest multiPartRequest
     * @param httpHeaders Map<String, String> httpHeaders
     * @return JSONObject
     * @throws PLMException exception
     */
    public JSONObject publishToPLM(JSONObject publishData, Map<String, String> httpHeaders, String plmUrl) throws PLMException{
        return plmHelper.preparePublishToPLM(publishData, httpHeaders, plmUrl);
    }

	public JSONObject licenseLogout(String userId) throws PLMException {
		return plmHelper.licenseLogout(userId);
	}

	public JSONObject getPLMSettingConfig(HttpHeaders headers) throws PLMException{
		return plmHelper.prepareGetPLMSettingResponse(headers);
	}
    
}
