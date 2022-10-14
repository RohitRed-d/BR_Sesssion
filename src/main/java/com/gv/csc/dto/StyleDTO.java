package com.gv.csc.dto;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Data
public class StyleDTO{

    @Id
    @GeneratedValue(strategy= GenerationType.AUTO)
    public long id;

    public String closetStyleId;
}
