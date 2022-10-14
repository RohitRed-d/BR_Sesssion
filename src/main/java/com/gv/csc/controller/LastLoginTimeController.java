package com.gv.csc.controller;

import java.util.List;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.gv.csc.dao.LastLoginTimeDao;
import com.gv.csc.entity.LastLoginTime;
import com.gv.csc.service.LastLoginTimeService;


@RestController
@RequestMapping("/closetandplm")
public class LastLoginTimeController {
	
	@Autowired
	private LastLoginTimeService lastLoginTimeService;
	
	@Autowired
	private LastLoginTimeDao lastLoginTimeDao;
	private ObjectMapper objectMapper = new ObjectMapper();
	private Gson gson = new Gson();
    Logger logger = LoggerFactory.getLogger(StyleController.class);
	
    /**
    *
    * @param id
    * @return response based on id
    */
   @GetMapping(value = "/fetchId/{id}")
   public ResponseEntity<String> getLastLoginTimeByid(@PathVariable long id) {
       logger.info("INFO::LastLoginTimeController: getLastLoginTimeByid() started.");
       JSONObject jsonObject = new JSONObject();
       try {
           LastLoginTime lastLoginTime = lastLoginTimeService.getLastLoginTimeById(id);
           jsonObject.put("lastLoginTime", gson.toJson(lastLoginTime));
       } catch(Exception e) {
           jsonObject.put("message", e.getMessage());
           return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(jsonObject.toString());
       }
       logger.info("INFO::LastLoginTimeController: getLastLoginTimeByid() ended.");
       return ResponseEntity.ok(jsonObject.toString());
   }
   
   /**
   *
   * @return all database rows
   */
  @GetMapping(value ="/fetchAll")
  public ResponseEntity<String> findAllLastLoginTime() {
      logger.info("INFO::LastLoginTimeController: findAllLastLoginTime() started.");
      JSONObject jsonObject = new JSONObject();
      try {
          List list = lastLoginTimeService.getAllLastLoginTimes();
          jsonObject.put("lastLoginTime", list);
      } catch(Exception e) {
          jsonObject.put("message", e.getMessage());
          return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(jsonObject.toString());
      }
      logger.info("INFO::LastLoginTimeController: findAllLastLoginTime() ended.");
      return ResponseEntity.ok(jsonObject.toString());
  }
	
  /**
  *
  * @return all database rows
  */
 @GetMapping(value ="/lastLoginTime")
 public ResponseEntity<String> findFirstLastLoginTime() {
     logger.info("INFO::LastLoginTimeController: findAllLastLoginTime() started.");
     JSONObject jsonObject = new JSONObject();
     String lastLoginTimejson;
     try {
         List<LastLoginTime> lastLoginTime = lastLoginTimeDao.findFirst2ByOrderByLoginTimeDesc();
         lastLoginTimejson = gson.toJson(lastLoginTime);
     } catch(Exception e) {
         jsonObject.put("message", e.getMessage());
         return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(jsonObject.toString());
     }
     logger.info("INFO::LastLoginTimeController: findAllLastLoginTime() ended.");
     return ResponseEntity.ok(lastLoginTimejson);
 }
}
