package com.gv.csc.entity;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "LastLogin")
@Getter @Setter @NoArgsConstructor
public class LastLoginTime {

	@Id
    @GeneratedValue(strategy= GenerationType.AUTO)
    public long id;
    
    @Column(name = "login_time")
    public Date loginTime;
    
    @Column(name = "closet_user")
    public String closetUser;

    @Column(name = "plm_user")
    public String plmUser;
    
}
