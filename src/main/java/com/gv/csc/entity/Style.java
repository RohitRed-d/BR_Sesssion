package com.gv.csc.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "Style")
@Getter @Setter @NoArgsConstructor
public class Style {

    @Id
    @GeneratedValue(strategy= GenerationType.AUTO)
    public long id;

    @Column(name = "closet_style_id")
    public String closetStyleId;

    @Column(name = "plm_style_id")
    public String plmStyleId;

    @Column(name = "create_time_stamp")
    public Date createTimeStamp;

    @Column(name = "modify_time_stamp")
    public Date modifyTimeStamp;

    @Column(name = "closet_user")
    public String closetUser;

    @Column(name = "plm_user")
    public String plmUser;

}
