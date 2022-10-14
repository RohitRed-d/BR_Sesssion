package com.gv.csc.service;

import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.gv.csc.dao.LastLoginTimeDao;
import com.gv.csc.entity.LastLoginTime;
import com.gv.csc.entity.Style;

@Service
public class LastLoginTimeService {
	
	@Autowired
	private LastLoginTimeDao lastLoginTimeDao;

    Logger logger = LoggerFactory.getLogger(StyleService.class);
    
    /**
    *
    * @return List of all styles
    */
   public List<LastLoginTime> getAllLastLoginTimes(){
       List list = (List<LastLoginTime>) this.lastLoginTimeDao.findAll();
       return list;
   }

   /**
    *
    * @param id
    * @return Style details based on id
    */
   public LastLoginTime getLastLoginTimeById(long id){
	   LastLoginTime lastLoginTime = null;
       try {
           lastLoginTime = lastLoginTimeDao.findById(id);
       }catch (Exception e){
           e.printStackTrace();
       }
       return lastLoginTime;
   }

   /**
    * 
    * @param lastLoginTime
    * @return 
    */
   public LastLoginTime saveLastLoginTime(LastLoginTime lastLoginTime) {
	return lastLoginTimeDao.save(lastLoginTime);
   }
   
   /**
    * 
    * @param dNow
    * @return
    */
   public LastLoginTime getLastLoginTimeByLoginTime(Date dNow) {
	LastLoginTime lastLoginTime = lastLoginTimeDao.findByLoginTime(dNow);
	return lastLoginTime;
   }

   /**
    * 
    * @param cloUser
    * @return
    */
   public LastLoginTime getLastLoginTimeByClosetUser(String  cloUser) {
		LastLoginTime lastLoginTime = lastLoginTimeDao.findByClosetUser(cloUser);
		return lastLoginTime;
	   }
   /**
    * 
    * @param plmUser
    * @return
    */
   public LastLoginTime getLastLoginTimeByPlmUser(String  plmUser) {
		LastLoginTime lastLoginTime = lastLoginTimeDao.findByPlmUser(plmUser);
		return lastLoginTime;
	   }
   
   public LastLoginTime getLastLoginTimeByClosetUserAndPlmUserAndLoginTime(String cloUser, String  plmUser, Date loginTime) {
		LastLoginTime lastLoginTime = lastLoginTimeDao.findByClosetUserAndPlmUserAndLoginTime(cloUser, plmUser, loginTime);
		return lastLoginTime;
	   }

}
