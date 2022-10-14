package com.gv.csc.dao;

import com.gv.csc.entity.Style;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
public interface StyleDAO extends JpaRepository<Style,Long> {
    public Style findById(long id);
    public Style findByClosetStyleId(String closetStyleId);
    public Style findByClosetStyleIdAndPlmStyleId(String closetStyleId, String plmStyleId);
    public List<Style> findByClosetUserAndPlmUser(String closetUser, String plmUser);
    public List<Style> findFirst10ByOrderByCreateTimeStampDesc();
}
