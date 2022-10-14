package com.gv.csc.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.gv.csc.entity.Style;
import com.gv.csc.exceptions.CLOSETException;
import com.gv.csc.helper.CLOSETHelper;
import com.gv.csc.service.StyleService;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpSession;

@RestController
@RequestMapping("/style")
public class StyleController {

    @Autowired
    private StyleService styleService;

    private Gson gson = new Gson();
    private ObjectMapper objectMapper = new ObjectMapper();

    Logger logger = LoggerFactory.getLogger(StyleController.class);

    /**
     *
     * @param id
     * @return response based on id
     */
    @GetMapping(value = "/fetchId/{id}")
    public ResponseEntity<String> getStyleByid(@PathVariable long id) {
        logger.info("INFO::StyleController: getStyleByid() started.");
        JSONObject jsonObject = new JSONObject();
        try {
            Style style = styleService.getStyleById(id);
            jsonObject.put("style", gson.toJson(style));
        } catch(Exception e) {
            jsonObject.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(jsonObject.toString());
        }
        logger.info("INFO::StyleController: getStyleByid() ended.");
        return ResponseEntity.ok(jsonObject.toString());
    }

    /**
     *
     * @param closetStyleId
     * @return response based on result
     */
    @GetMapping(value = "/fetchClosetStyleId/{closetStyleId}")
    public ResponseEntity<String> findStyleByCLOSETStyleId(@PathVariable String closetStyleId) {
        logger.info("INFO::StyleController: findStyleByCLOSETStyleId() started.");
        JSONObject jsonObject = new JSONObject();
        try {
            Style style = styleService.findStyleByCLOSETStyleId(closetStyleId);
            jsonObject.put("style", gson.toJson(style));
        } catch(Exception e) {
            jsonObject.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(jsonObject.toString());
        }
        logger.info("INFO::StyleController: findStyleByCLOSETStyleId() ended.");
        return ResponseEntity.ok(jsonObject.toString());
    }

    /**
     *
     * @return all database rows
     */
    @GetMapping(value = "/fetchAll")
    public ResponseEntity<String> findAllStyle() {
        logger.info("INFO::StyleController: findAllStyle() started.");
        JSONObject jsonObject = new JSONObject();
        try {
            List list = styleService.getAllStyle();
            jsonObject.put("style", list);
        } catch(Exception e) {
            jsonObject.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(jsonObject.toString());
        }
        logger.info("INFO::StyleController: findAllStyle() ended.");
        return ResponseEntity.ok(jsonObject.toString());
    }

    /**
     *
     * @return top 10 style id's
     */
    @GetMapping(value = "/recentlypublished")
    public ResponseEntity<?> recentlyPublishedStyle(@RequestHeader Map<String, String> httpHeaders) {
        logger.info("INFO::StyleController: recentlyPublishedStyle() started.");
        String data = "";
        JSONObject jsonObject = new JSONObject();
        try {
            List<Style> result = styleService.recentlyPublishedStyle(httpHeaders);
            data = objectMapper.writeValueAsString(result);
        } catch(Exception e) {
            jsonObject.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(jsonObject.toString());
        }
        logger.debug("DEBUG::StyleController: recentlyPublishedStyle() :"+data);
        logger.info("INFO::StyleController: recentlyPublishedStyle() ended.");
        return ResponseEntity.ok(data);
    }

    /**
     *
     * @param version
     * @param httpHeaders
     * @return top 10 styles details
     */
    @GetMapping(value = "/recentcontent")
    public ResponseEntity<?> recentContentDetails(@RequestParam(defaultValue = "0", required = false) int version, @RequestHeader Map<String, String> httpHeaders, HttpSession session) {
        logger.info("INFO::StyleController: recentContentDetails() started.");
        String plmUrl = (String) session.getAttribute("plmurl");
        JSONObject outJson = null;
        try {
            outJson = styleService.recentContentDetails(version, httpHeaders, plmUrl);
        } catch (JsonProcessingException exe) {
            exe.printStackTrace();
        } catch (CLOSETException e) {
            e.printStackTrace();
            outJson = CLOSETHelper.prepareErrorResponse(e);
            return ResponseEntity.status(e.getStatusCode()).body(outJson.toString());
        }
        logger.debug("DEBUG::StyleController: recentContentDetails() :"+outJson);
        logger.info("INFO::StyleController: recentContentDetails() ended.");
        return ResponseEntity.ok(outJson.toString());
    }
}
