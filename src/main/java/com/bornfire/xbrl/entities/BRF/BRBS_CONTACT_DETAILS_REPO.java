package com.bornfire.xbrl.entities.BRF;

import java.math.BigDecimal;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
@Repository
public interface BRBS_CONTACT_DETAILS_REPO extends JpaRepository <BRBS_CONTACT_DETAILS_ENTITY,BigDecimal>{

}
