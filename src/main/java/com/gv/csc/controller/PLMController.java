package com.gv.csc.controller;

import com.gv.csc.exceptions.CLOSETException;
import com.gv.csc.exceptions.PLMException;
import com.gv.csc.helper.PLMHelper;
import com.gv.csc.service.PLMService;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import javax.servlet.http.HttpSession;

import java.net.UnknownHostException;
import java.util.Map;

/**
 * PLMController - PLM Controller holds all the End points to serve PLM APIs
 */
@RestController
@RequestMapping("/plm")
public class PLMController {
    @Autowired
    private PLMService plmService;

    @Autowired
    private PLMHelper plmHelper;

    Logger logger = LoggerFactory.getLogger(PLMController.class);

    /**
     * login - End point to be used to connect to PLM
     *
     * @param userid   String userid
     * @param password String password
     * @return ResponseEntity holding the details on login
     * @throws UnknownHostException 
     */
    @GetMapping(value = "/login", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> login(@RequestParam String userid, @RequestParam String password, @RequestParam String plmUrl,@RequestHeader Map<String, String> headers, HttpSession session ) throws CLOSETException{
        logger.info("INFO::PLMController: login() started.");
        JSONObject outJson;
        try {
        	session.setAttribute("plmurl", plmUrl);
            //Calling service method used to get the login details
            outJson = plmService.plmLogin(userid, password, plmUrl, headers);
        } catch (PLMException exe) {
            exe.printStackTrace();
            //Prepare Error JSON response with the exception message and code
            outJson = PLMHelper.prepareErrorResponse(exe);
            logger.error("ERROR::PLMController: login() -> outJson : " + outJson);
            return ResponseEntity.status(exe.getStatusCode()).body(outJson.toString());
        }
        logger.debug("DEBUG::PLMController: login() outJson) - " + outJson);
        logger.info("INFO::PLMController: login() end.");
        return ResponseEntity.ok(outJson.toString());
    }

    /**
     * Get PLM styles based on search criteria and only show the fields specified in config file
     *
     * @param searchTerm search criteria
     * @param httpHeaders headers
     * @return style result list
     */
    @GetMapping(value = "/search", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> searchStyle(@RequestParam String searchTerm, @RequestHeader Map<String, String> httpHeaders, HttpSession session) {
        logger.info("INFO::PLMController: searchStyle() started.");
        JSONObject outJson;
        try {
            String plmUrl = (String) session.getAttribute("plmurl");
            //Calling service method used to get the search details
            outJson = plmService.searchPlmStyle(searchTerm, httpHeaders, plmUrl);
        } catch (PLMException exe) {
            exe.printStackTrace();
            //Prepare Error JSON response with the exception message and code
            outJson = PLMHelper.prepareErrorResponse(exe);
            logger.error("ERROR::CLOSETController: searchStyle() -> outJson : " + outJson);
            return ResponseEntity.status(exe.getStatusCode()).body(outJson.toString());
        }
        logger.debug("DEBUG::PLMController: searchStyle() outJson - " + outJson);
        logger.info("INFO::PLMController: searchStyle() end.");
        return ResponseEntity.ok(outJson.toString());
    }

    /**
     * Get PLM style based on styleID
     */
    @GetMapping(value = "/style")
    public ResponseEntity<?> getStyle(@RequestParam String requestNo, @RequestParam String owner, @RequestHeader Map<String, String> httpHeaders, HttpSession session) {
        logger.info("INFO::PLMController: getStyle() started.");
        JSONObject outJson;
        try {
        	String plmUrl = (String) session.getAttribute("plmurl");
        //Calling service method used to get the search details
        outJson = plmService.getPlmStyle(requestNo, owner, httpHeaders, plmUrl);
        } catch (PLMException exe) {
        exe.printStackTrace();
        //Prepare Error JSON response with the exception message and code
        outJson = PLMHelper.prepareErrorResponse(exe);
        logger.error("ERROR::CLOSETController: getStyle() -> outJson : " + outJson);
        return ResponseEntity.status(exe.getStatusCode()).body(outJson.toString());
        }
        logger.debug("DEBUG::PLMController: getStyle() outJson) - " + outJson);
        logger.debug("INFO::PLMController: getStyle() end.");
        return ResponseEntity.ok(outJson.toString());
    }

    /**
     * publish - End point to publish data to PLM
     * @param multiPartRequest MultipartHttpServletRequest request
     * @param httpHeaders Map<String, String> httpHeaders
     * @return publish data json
     */
    @PostMapping(value = "/publish", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> publish(MultipartHttpServletRequest multiPartRequest, @RequestHeader Map<String, String> httpHeaders, HttpSession session ) {
        logger.info("INFO::PLMController: publish() started.");
        JSONObject outJson;
        try {
        	String plmUrl = (String) session.getAttribute("plmurl");
            //Calling service method used to post the style details to PLM
            outJson = plmService.publishToPLM(multiPartRequest, httpHeaders, plmUrl);
        } catch (PLMException exe) {
            exe.printStackTrace();
            //Prepare Error JSON response with the exception message and code
            outJson = PLMHelper.prepareErrorResponse(exe);
            logger.error("ERROR::CLOSETController: publish() -> outJson : " + outJson);
            return ResponseEntity.status(exe.getStatusCode()).body(outJson.toString());
        }
        logger.debug("DEBUG::PLMController: publish() outJson - " + outJson);
        logger.info("INFO::PLMController: publish() end.");
        return ResponseEntity.ok(outJson.toString());
    }
    /**
     * getPLMViewConfig - Get PLM views
     * @param headers HttpHeaders headers
     * @return Response entity of plm view config of Style
     */
    @GetMapping(value = "/plmviewconfig", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getPLMViewConfig(@RequestHeader HttpHeaders headers ) {
        logger.info("INFO::PLMController: getPLMViewConfig() started.");
        JSONObject outJson;
        try {
            //Calling service method used to get the getClosetResultConfig details
            outJson = plmService.getPLMViewConfig(headers);
        } catch (PLMException exe) {
            exe.printStackTrace();
            //Prepare Error JSON response with the exception message and code
            outJson = PLMHelper.prepareErrorResponse(exe);
            logger.error("ERROR::PLMController: getPLMResultConfig() -> outJson : " + outJson);
            return ResponseEntity.status(exe.getStatusCode()).body(outJson.toString());
        }
        logger.debug("DEBUG::PLMController: getPLMViewConfig() outJson) - " + outJson);
        logger.error("INFO::PLMController: getPLMViewConfig() end.");
        return ResponseEntity.ok(outJson.toString());
    }
    /**
     * getPLMResultConfig - Get PLM results
     * @param headers HttpHeaders headers
     * @return Response entity of PLM results of Style
     */
    @GetMapping(value = "/plmresultconfig", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getPLMResultConfig(@RequestHeader HttpHeaders headers ) {
        logger.info("INFO::PLMController: getPLMResultConfig() started.");
        JSONObject outJson;
        try {
            outJson = plmService.getPLMResultConfig(headers);
        } catch (PLMException exe) {
            exe.printStackTrace();
            //Prepare Error JSON response with the exception message and code
            outJson = PLMHelper.prepareErrorResponse(exe);
            logger.error("ERROR::PLMController: getPLMResultConfig() -> outJson : " + outJson);
            return ResponseEntity.status(exe.getStatusCode()).body(outJson.toString());
        }
        logger.debug("DEBUG::PLMController: getPLMResultConfig() outJson) - " + outJson);
        logger.info("INFO::PLMController: getPLMResultConfig() end.");
        return ResponseEntity.ok(outJson.toString());
    }
	
	/**
     * publish - End point to publish data to PLM
     * @param publishData MultipartHttpServletRequest request
     * @param httpHeaders Map<String, String> httpHeaders
     * @return publish data json
     */
	@PostMapping(value = "/publishdata", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> publish(@RequestBody String publishData, @RequestHeader Map<String, String> httpHeaders, HttpSession session ) {
        logger.info("INFO::PLMController: publish() started."+publishData);
        JSONObject outJson;
        try {
        	String plmUrl = (String) session.getAttribute("plmurl");
            //Calling service method used to post the style details to PLM
            outJson = plmService.publishToPLM(new JSONObject(publishData), httpHeaders, plmUrl);
        } catch (PLMException exe) {
            exe.printStackTrace();
            //Prepare Error JSON response with the exception message and code
            outJson = PLMHelper.prepareErrorResponse(exe);
            logger.error("ERROR::CLOSETController: publish() -> outJson : " + outJson);
            return ResponseEntity.status(exe.getStatusCode()).body(outJson.toString());
        }
        logger.debug("DEBUG::PLMController: publish() outJson - " + outJson);
        logger.info("INFO::PLMController: publish() end.");
        return ResponseEntity.ok(outJson.toString());
    }
	
	@GetMapping(value = "/logout")
    public ResponseEntity<?> logout(@RequestParam String userId, HttpSession session) throws PLMException {
        logger.info("INFO::PLMController: licenseLogout() started."+userId);
        JSONObject outJson;
        //Calling service method used to licenseLogout
        session.invalidate();
		outJson = plmService.licenseLogout(userId);
        logger.debug("DEBUG::PLMController: licenseLogout() outJson - " );
        logger.info("INFO::PLMController: licenseLogout() end.");
        return ResponseEntity.ok(outJson.toString());
    }
	/**
	 * 
	 * @param headers
	 * @return
	 */
	 @GetMapping(value = "/plmsetting", produces = MediaType.APPLICATION_JSON_VALUE)
	    public ResponseEntity<?> getPLMSettingConfig(@RequestHeader HttpHeaders headers ) throws PLMException {
	        logger.info("INFO::PLMController: getPLMResultConfig() started.");
	        JSONObject outJson;
	        try {
	            outJson = plmService.getPLMSettingConfig(headers);
	        } catch (PLMException exe) {
	            exe.printStackTrace();
	            //Prepare Error JSON response with the exception message and code
	            outJson = PLMHelper.prepareErrorResponse(exe);
	            logger.error("ERROR::PLMController: getPLMResultConfig() -> outJson : " + outJson);
	            return ResponseEntity.status(exe.getStatusCode()).body(outJson.toString());
	        }
         logger.debug("DEBUG::ajadbajbhdbcjvcJVc - " + outJson);
         logger.info("abcdefghijafxghjbnm,cZvkjsdhvjksdnvkjsdnjsd njkn KM .");
         logger.debug("NMZM<cnmzncmzcnmzxnc x ksj sjk s sd  - " + outJson);
         logger.info("aklsmca,sna,s nxa,msn cacka amc ak scma cakn caksnc akc acjk kjac ");
         logger.debug("MResultConfig() outJson 1) - " + outJson);
         logger.info("ultConfig() end";);
         logger.debug(" outJson) - " + outJson);
         logger.info("INFO::ltConfig() end.");
         logger.debug("Dnfig() outJson) - " + outJson);
         logger.info("Iend.");

         return ResponseEntity.ok(outJson.toString());
	    }
}
