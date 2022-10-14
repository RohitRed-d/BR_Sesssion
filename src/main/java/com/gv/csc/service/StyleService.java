package com.gv.csc.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gv.csc.dao.StyleDAO;
import com.gv.csc.entity.Style;
import com.gv.csc.exceptions.CLOSETException;
import com.gv.csc.exceptions.PLMException;
import com.gv.csc.helper.CLOSETHelper;
import com.gv.csc.helper.PLMHelper;
import com.gv.csc.helper.RestService;
import com.gv.csc.util.Utility;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpSession;

@Service
public class StyleService {

    @Autowired
    private StyleDAO styleDAO;

    @Autowired
    private RestService restService;

    @Autowired
    private CLOSETHelper closetHelper;

    @Autowired
    private PLMHelper plmHelper;

    Logger logger = LoggerFactory.getLogger(StyleService.class);

    /**
     *
     * @return List of all styles
     */
    public List<Style> getAllStyle(){
        List list = (List<Style>) this.styleDAO.findAll();
        return list;
    }

    /**
     *
     * @param id
     * @return Style details based on id
     */
    public Style getStyleById(long id){
        Style style = null;
        try {
            style = styleDAO.findById(id);
        }catch (Exception e){
            e.printStackTrace();
        }
        return style;
    }

    /**
     *
     * @param closetStyleId
     * @return style based on closetStyleId
     */
    public Style findStyleByCLOSETStyleId(String closetStyleId) {
        Style style = styleDAO.findByClosetStyleId(closetStyleId);
        return style;
    }

    /**
     *
     * @param style
     * @return save style
     */
    public Style saveStyle(Style style){
        return styleDAO.save(style);
    }

    /**
     *
     * @param closetStyleId
     * @param plmStyleId
     * @return
     */
    public Style findByClosetStyleIdAndPlmStyleId(String closetStyleId, String plmStyleId){
        return styleDAO.findByClosetStyleIdAndPlmStyleId(closetStyleId, plmStyleId);
    }

    /**
     *
     * @return top 10 results
     */
    public List<Style> recentlyPublishedStyle(Map<String, String> headers) {
        logger.info("INFO::StyleService: recentlyPublishedStyle() started.");
        List<Style> list = styleDAO.findByClosetUserAndPlmUser(headers.get("closet-user-name"), headers.get("plm-user-name"));
        logger.info("INFO::StyleService: recentlyPublishedStyle() ended.");
        return list;
    }

    /**
     *
     * @param version
     * @param headers
     * @return Style information
     * @throws JsonProcessingException
     * @throws CLOSETException
     */
    public JSONObject recentContentDetails(int version, Map<String, String> headers, String plmUrl) throws JsonProcessingException, CLOSETException {
        logger.info("INFO::StyleService: recentContentDetails() started.");
        JSONObject outJson = new JSONObject();
        List<Style> list = recentlyPublishedStyle(headers);
        ObjectMapper objectMapper = new ObjectMapper();
        String data = objectMapper.writeValueAsString(list);
        JSONArray json = new JSONArray(data);
        JSONObject plmStyleJson = new JSONObject();
        JSONObject closetStyleJson = new JSONObject();
        JSONObject tempJson = new JSONObject();
        JSONObject tempStyleJson = new JSONObject();
        JSONArray stylesArray = new JSONArray();
        for (int i = 0; i<json.length(); i++) {
            tempStyleJson = new JSONObject();
            JSONObject jsonObject = json.getJSONObject(i);
            String closetStyleId = jsonObject.getString("closetStyleId");
            String plmStyleId = jsonObject.getString("plmStyleId");

            if(!Utility.hasContent(plmStyleId)) {
                continue;
            }
            if(!plmStyleId.contains("-:-")) {
                continue;
            }
            tempJson = closetHelper.prepareGetStyleResponse(closetStyleId, version, headers);
            closetStyleJson = tempJson.getJSONObject("style");
            tempStyleJson.put("closetStyle", closetStyleJson);


            String owner = plmStyleId.split("-:-")[0];
            String requestNo = plmStyleId.split("-:-")[1];
            try {
                tempJson = plmHelper.prepareGetStyleResponse(requestNo, owner, headers, plmUrl);
            } catch (PLMException e) {
                //throw new RuntimeException(e);
                continue;
            }
            plmStyleJson = tempJson.getJSONObject("style");
            tempStyleJson.put("plmStyle", plmStyleJson);

            stylesArray.put(tempStyleJson);
        }
        outJson.put("recentStyles", stylesArray);
        logger.info("INFO::StyleService: recentContentDetails() ended.");

        return outJson;
    }

}
