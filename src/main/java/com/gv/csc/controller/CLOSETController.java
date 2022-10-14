package com.gv.csc.controller;

import com.gv.csc.exceptions.CLOSETException;
import com.gv.csc.helper.CLOSETHelper;
import com.gv.csc.service.CLOSETService;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;


/**
 * CLOSETController - CLOSET Controller holds all the End points to serve CLOSET APIs
 */
@RestController
@RequestMapping("/closet")
public class CLOSETController {
	
    @Autowired
    private CLOSETService closetService;

    Logger logger = LoggerFactory.getLogger(CLOSETController.class);

    /**
     * login - End point to be used to connect to CLOSET
     *
     * @param username   String username
     * @param password   String password
     * @param groupToken Boolean groupToken
     * @return ResponseEntity holding the details on login
     */
    @GetMapping(value = "/login", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> login(@RequestParam String username, @RequestParam String password, @RequestParam(defaultValue = "false", required = false) Boolean groupToken) {
        logger.info("INFO::CLOSETController: login() started.");
        JSONObject outJson;
        try {
            //Calling service method used to get the login details
            outJson = closetService.closetLogin(username, password, groupToken);
        } catch (CLOSETException exe) {
            exe.printStackTrace();
            //Prepare Error JSON response with the exception message and code
            outJson = CLOSETHelper.prepareErrorResponse(exe);
            logger.error("ERROR::CLOSETController: login() -> outJson : " + outJson);
            return ResponseEntity.status(exe.getStatusCode()).body(outJson.toString());
        }
        logger.debug("DEBUG::CLOSETController: login() outJson) - " + outJson);
        logger.info("INFO::CLOSETController: login() end.");
        return ResponseEntity.ok(outJson.toString());
    }

    /**
     * searchStyle - Get CLOSET styles based on search criteria and only show the fields specified in config file
     *
     * @param searchTerm  String searchTerm
     * @param filter      int filter
     * @param httpHeaders Map<String, String> httpHeaders
     * @return ResponseEntity with styles
     */
    @GetMapping(value = "/search", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> searchStyle(@RequestParam String searchTerm, @RequestParam(defaultValue = "0", required = false) int filter,
                                         @RequestHeader Map<String, String> httpHeaders) {
        logger.info("INFO::CLOSETController: searchStyle() started.");
        JSONObject outJson;
        try {
            //Calling service method used to get the search details
            outJson = closetService.searchClosetStyle(searchTerm, filter, httpHeaders);
        } catch (CLOSETException exe) {
            exe.printStackTrace();
            //Prepare Error JSON response with the exception message and code
            outJson = CLOSETHelper.prepareErrorResponse(exe);
            logger.error("ERROR::CLOSETController: searchStyle() -> outJson : " + outJson);
            return ResponseEntity.status(exe.getStatusCode()).body(outJson.toString());
        }
        logger.debug("DEBUG::CLOSETController: searchStyle() outJson - " + outJson);
        logger.info("INFO::CLOSETController: searchStyle() end.");
        return ResponseEntity.ok(outJson.toString());
    }

    /**
     * getStyle - Get CLOSET style based on styleID
     *
     * @param styleId     String styleId
     * @param version     int version
     * @param httpHeaders Map<String, String> httpHeaders
     * @return Response entity of Style response
     */
    @GetMapping(value = "/style", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getStyle(@RequestParam String styleId, @RequestParam(defaultValue = "0", required = false) int version,
                                      @RequestHeader Map<String, String> httpHeaders) {
        logger.info("INFO::CLOSETController: getStyle() started.");
        JSONObject outJson;
        try {
            //Calling service method used to get the style details
            outJson = closetService.getClosetStyle(styleId, version, httpHeaders);
        } catch (CLOSETException exe) {
            exe.printStackTrace();
            //Prepare Error JSON response with the exception message and code
            outJson = CLOSETHelper.prepareErrorResponse(exe);
            logger.error("ERROR::CLOSETController: getStyle() -> outJson : " + outJson);
            return ResponseEntity.status(exe.getStatusCode()).body(outJson.toString());
        }
        logger.debug("DEBUG::CLOSETController: getStyle() outJson) - " + outJson);
        logger.info("INFO::CLOSETController: getStyle() end.");
        return ResponseEntity.ok(outJson.toString());
    }
    
    @GetMapping(value = "/test/webhook")
    public String testWebHook() {
    	return "response";
    }

    /**
     * getGroups - Get CLOSET groups
     * @param httpHeaders Map<String, String> httpHeaders
     * @return Response entity of Style response
     */
    @GetMapping(value = "/groups", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getGroups(@RequestHeader Map<String, String> httpHeaders) {
        logger.info("INFO::CLOSETController: getGroups() started.");
        JSONObject outJson;
        try {
            //Calling service method used to get the groups details
            outJson = closetService.getClosetGroups(httpHeaders);
        } catch (CLOSETException exe) {
            exe.printStackTrace();
            //Prepare Error JSON response with the exception message and code
            outJson = CLOSETHelper.prepareErrorResponse(exe);
            logger.error("ERROR::CLOSETController: getGroups() -> outJson : " + outJson);
            return ResponseEntity.status(exe.getStatusCode()).body(outJson.toString());
        }
        logger.debug("DEBUG::CLOSETController: getGroups() outJson) - " + outJson);
        logger.info("INFO::CLOSETController: getGroups() end.");
        return ResponseEntity.ok(outJson.toString());
    }

    /**
     * getRenders - Get CLOSET renders
     *
     * @param styleId String styleId
     * @param version int version
     * @param httpHeaders Map httpHeaders
     * @return Response entity of Style response
     */
    @GetMapping(value = "/rendersinfo", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getRenders(@RequestParam String styleId, @RequestParam(defaultValue = "0", required = false) int version,
                                        @RequestHeader Map<String, String> httpHeaders) {
        logger.info("INFO::CLOSETController: getRenders() started.");
        JSONObject outJson;
        try {
            //Calling service method used to get the render details
            outJson = closetService.getRenders(styleId, version, httpHeaders);
        } catch (CLOSETException exe) {
            exe.printStackTrace();
            //Prepare Error JSON response with the exception message and code
            outJson = CLOSETHelper.prepareErrorResponse(exe);
            logger.error("ERROR::CLOSETController: getRenders() -> outJson : " + outJson);
            return ResponseEntity.status(exe.getStatusCode()).body(outJson.toString());
        }
        logger.debug("DEBUG::CLOSETController: getRenders() outJson) - " + outJson);
        logger.info("INFO::CLOSETController: getRenders() end.");
        return ResponseEntity.ok(outJson.toString());
    }

    /**
     * getColorways - Get CLOSET Colorways
     * @param styleId String styleId
     * @param version int version
     * @param httpHeaders Map<String, String> httpHeaders
     * @return Response entity of Colorways of Style
     */
    @GetMapping(value = "/colorways", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getColorways(@RequestParam String styleId, @RequestParam(defaultValue = "0", required = false) int version,
                                          @RequestHeader Map<String, String> httpHeaders) {
        logger.info("INFO::CLOSETController: getColorways() started.");
        JSONObject outJson;
        try {
            //Calling service method used to get the getColorways details
            outJson = closetService.getColorways(styleId, version, httpHeaders);
        } catch (CLOSETException exe) {
            exe.printStackTrace();
            //Prepare Error JSON response with the exception message and code
            outJson = CLOSETHelper.prepareErrorResponse(exe);
            logger.error("ERROR::CLOSETController: getColorways() -> outJson : " + outJson);
            return ResponseEntity.status(exe.getStatusCode()).body(outJson.toString());
        }
        logger.debug("DEBUG::CLOSETController: getColorways() outJson) - " + outJson);
        logger.info("INFO::CLOSETController: getColorways() end.");
        return ResponseEntity.ok(outJson.toString());
    }

    /**
     * getClosetViewConfig - Get CLOSET views
     * @param headers HttpHeaders headers
     * @return Response entity of Colorways of Style
     */
    @GetMapping(value = "/closetviewconfig", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getClosetViewConfig(@RequestHeader HttpHeaders headers) {
        logger.info("INFO::CLOSETController: getClosetViewConfig() started.");
        JSONObject outJson;
        try {
            //Calling service method used to get the getClosetViewConfig details
            outJson = closetService.getClosetViewConfig(headers);
        } catch (CLOSETException exe) {
            exe.printStackTrace();
            //Prepare Error JSON response with the exception message and code
            outJson = CLOSETHelper.prepareErrorResponse(exe);
            logger.error("ERROR::CLOSETController: getClosetViewConfig() -> outJson : " + outJson);
            return ResponseEntity.status(exe.getStatusCode()).body(outJson.toString());
        }
        logger.debug("DEBUG::CLOSETController: getClosetViewConfig() outJson) - " + outJson);
        logger.info("INFO::CLOSETController: getClosetViewConfig() end.");
        return ResponseEntity.ok(outJson.toString());
    }

    /**
     * getClosetView - Get CLOSET views
     * @param headers HttpHeaders headers
     * @return Response entity of Colorways of Style
     */
    @GetMapping(value = "/closetresultconfig", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getClosetResultConfig(@RequestHeader HttpHeaders headers) {
        logger.info("INFO::CLOSETController: getClosetView() started.");
        JSONObject outJson;
        try {
            //Calling service method used to get the getClosetResultConfig details
            outJson = closetService.getClosetResultConfig(headers);
        } catch (CLOSETException exe) {
            exe.printStackTrace();
            //Prepare Error JSON response with the exception message and code
            outJson = CLOSETHelper.prepareErrorResponse(exe);
            logger.error("ERROR::CLOSETController: getClosetView() -> outJson : " + outJson);
            return ResponseEntity.status(exe.getStatusCode()).body(outJson.toString());
        }
        logger.debug("DEBUG::CLOSETController: getClosetView() outJson) - " + outJson);
        logger.info("INFO::CLOSETController: getClosetView() end.");
        return ResponseEntity.ok(outJson.toString());
    }

    /**
     * getZprjFile - Get CLOSET style based on styleID
     *
     * @param styleId     String styleId
     * @param version     int version
     * @param httpHeaders Map<String, String> httpHeaders
     * @return Response entity of Style response
     */
    @GetMapping(value = "/style/zprj/download/url", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getZprjFile(@RequestParam String styleId, @RequestParam(defaultValue = "0", required = false) int version,
                                      @RequestHeader Map<String, String> httpHeaders) {
        logger.info("INFO::CLOSETController: getZprjFile() started.");
        JSONObject outJson;
        try {
            //Calling service method used to get the zprj details
            outJson = closetService.getZprjFile(styleId, version, httpHeaders);
        } catch (CLOSETException exe) {
            exe.printStackTrace();
            //Prepare Error JSON response with the exception message and code
            outJson = CLOSETHelper.prepareErrorResponse(exe);
            logger.error("ERROR::CLOSETController: getZprjFile() -> outJson : " + outJson);
            return ResponseEntity.status(exe.getStatusCode()).body(outJson.toString());
        }
        logger.debug("DEBUG::CLOSETController: getZprjFile() outJson) - " + outJson);
        logger.info("INFO::CLOSETController: getZprjFile() end.");
        return ResponseEntity.ok(outJson.toString());
    }
    /**
     * getRenderZipFile - Get CLOSET style based on styleID
     *
     * @param styleId     String styleId
     * @param renderSeq     int version
     * @param httpHeaders Map<String, String> httpHeaders
     * @return Response entity of Style response
     */
    @GetMapping(value = "/style/zip/download/url", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getRenderZipFile(@RequestParam String styleId, @RequestParam int renderSeq,
                                         @RequestHeader Map<String, String> httpHeaders) {
        logger.info("INFO::CLOSETController: getRenderZipFile() started.");
        JSONObject outJson;
        try {
            //Calling service method used to get the zip details
            outJson = closetService.getRenderZipFile(styleId, renderSeq , httpHeaders);
        } catch (CLOSETException exe) {
            exe.printStackTrace();
            //Prepare Error JSON response with the exception message and code
            outJson = CLOSETHelper.prepareErrorResponse(exe);
            logger.error("ERROR::CLOSETController: getRenderZipFile() -> outJson : " + outJson);
            return ResponseEntity.status(exe.getStatusCode()).body(outJson.toString());
        }
        logger.debug("DEBUG::CLOSETController: getRenderZipFile() outJson) - " + outJson);
        logger.info("INFO::CLOSETController: getRenderZipFile() end.");
        return ResponseEntity.ok(outJson.toString());
    }
}
