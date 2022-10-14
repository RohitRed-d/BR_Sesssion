package com.gv.csc.dao;

import java.util.Date;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.gv.csc.entity.LastLoginTime;
import com.gv.csc.entity.Style;

@Repository
public interface LastLoginTimeDao extends JpaRepository<LastLoginTime,Long>{
	public LastLoginTime findById(long id);
	public LastLoginTime findByLoginTime(Date loginTime);
	public List<LastLoginTime> findFirst2ByOrderByLoginTimeDesc();
	public LastLoginTime findByClosetUser(String cloUser);
	public LastLoginTime findByPlmUser(String plmUser);
	public LastLoginTime findByClosetUserAndPlmUserAndLoginTime(String cloUser, String plmUser, Date loginTime);

}
