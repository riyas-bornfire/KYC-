package com.bornfire.xbrl.services;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import javax.sql.DataSource;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.core.env.Environment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.ModelAndView;

import com.bornfire.xbrl.entities.XBRLSession;
import com.bornfire.xbrl.entities.BRF.BRF071_TABLE_ENTITY;
import com.bornfire.xbrl.entities.BRF.BRF_BHARATH_02_ENTITY;
import com.bornfire.xbrl.entities.BRF.RBSTransactionMasterEntity;
import com.bornfire.xbrl.entities.BRF.T1CurProdDetail;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.export.ooxml.JRXlsxExporter;
import net.sf.jasperreports.export.SimpleExporterInput;
import net.sf.jasperreports.export.SimpleOutputStreamExporterOutput;

@Service
@Transactional
@ConfigurationProperties("output")
public class BRF071_Service {

	private static final Logger logger = LoggerFactory.getLogger(BRF001ReportService.class);

	@Autowired
	SessionFactory sessionFactory;

	@Autowired
	DataSource srcdataSource;
	
	@Autowired
	Environment env;
	
	
    
	
	DateFormat df = new SimpleDateFormat("dd-MMM-yyyy");


	public String preCheck(String reportid, String fromdate, String todate) {
		
			String msg = "";
			Session hs = sessionFactory.getCurrentSession();
			Date dt1;
			Date dt9;
			logger.info("Report precheck : " + reportid);

			try {
				dt1 = new SimpleDateFormat("dd/MM/yyyy").parse(fromdate);
				dt9 = new SimpleDateFormat("dd/MM/yyyy").parse(todate);
				logger.info("Getting No of records in Master table :" + reportid);
				Long dtlcnt = (Long) hs.createQuery("select count(*) from BRF071_TABLE_ENTITY a where a.report_date=?1")
						.setParameter(1, dt9).getSingleResult();

				if (dtlcnt > 0) {
					logger.info("Getting No of records in Mod table :" + reportid);
					Long modcnt = (Long) hs.createQuery("select count(*) from BRF071_TABLE_ENTITY a").getSingleResult();
					if (modcnt > 0) {
						msg = "success";
					}
				} else {
				//	msg = "Data Not available for the Report. Please Contact Administrator";
					msg = "success";

				}

			} catch (Exception e) {
				logger.info(e.getMessage());
				msg = "success";
				e.printStackTrace();

			}

			return msg;

		}	
	public ModelAndView getBRF071View(String reportId, String fromdate, String todate, String currency, String dtltype,
			Pageable pageable) {
		
			ModelAndView mv = new ModelAndView();
			Session hs = sessionFactory.getCurrentSession();
			int pageSize = pageable.getPageSize();
			int currentPage = pageable.getPageNumber();
			int startItem = currentPage * pageSize;
		 
			// Query<Object[]> qr;

			List<BRF071_TABLE_ENTITY> T1Master = new ArrayList<BRF071_TABLE_ENTITY>();
			/* List<BRF73_TABLE2> T1Master1 = new ArrayList<BRF73_TABLE2>(); */

			logger.info("Inside archive" +currency);

			try {
				Date d1 = df.parse(todate);
			//	T1rep = t1CurProdServiceRepo.getT1CurProdServices(d1);

				T1Master = hs.createQuery("from  BRF071_TABLE_ENTITY a where a.report_date = ?1 ", BRF071_TABLE_ENTITY.class)
						.setParameter(1, df.parse(todate)).getResultList();

				/*
				 * T1Master1 = hs.createQuery("from BRF73_TABLE2 a where a.report_date = ?1 ",
				 * BRF73_TABLE2.class) .setParameter(1, df.parse(todate)).getResultList();
				 */

			} catch (ParseException e) {
				e.printStackTrace();
			}

			// T1rep = t1CurProdServiceRepo.getT1CurProdServices(d1);

			mv.setViewName("RR/BRF071");
			// mv.addObject("currlist", refCodeConfig.currList());
		 
			/* mv.addObject("reportsummary1", T1Master1); */
			mv.addObject("reportmaster", T1Master);
			mv.addObject("displaymode", "summary");
			mv.addObject("reportsflag", "reportsflag");
			mv.addObject("menu", reportId);
			System.out.println("scv" + mv.getViewName());

			return mv;

		}
	
	public ModelAndView getBRF071currentDtl(String reportId, String fromdate, String todate, String currency,
			String dtltype, Pageable pageable, String filter) {

		int pageSize = pageable.getPageSize();
		int currentPage = pageable.getPageNumber();
		int startItem = currentPage * pageSize;

		ModelAndView mv = new ModelAndView();

		Session hs = sessionFactory.getCurrentSession();
		List<Object> T1Dt1 = new ArrayList<Object>();
		Query<Object[]> qr;

		if (dtltype.equals("report")) {
			if (!filter.equals("null")) {
				qr = hs.createNativeQuery("select * from TRAN_MASTER_DETAIL");
				//qr.setParameter(2, filter);
			} else {
				qr = hs.createNativeQuery("select * from TRAN_MASTER_DETAIL");
			}
		} else {
			qr = hs.createNativeQuery("select * from TRAN_MASTER_DETAIL  where report_date = ?1");
		}
		List<RBSTransactionMasterEntity> T1Master = new ArrayList<RBSTransactionMasterEntity>();

		try {
			T1Master = hs.createQuery("from RBSTransactionMasterEntity a where a.report_date = ?1", RBSTransactionMasterEntity.class)
					.setParameter(1, df.parse(todate)).getResultList();
		} catch (ParseException e) {
			
			e.printStackTrace();
		}

		logger.info("Getting Report Detail for : " + reportId + "," + fromdate + "," + todate + "," + currency);
		List<Object[]> result = qr.getResultList();

		for (Object[] a : result) {
			String cust_id = (String) a[0];
			String cust_name = (String) a[1];
			String cust_type = (String) a[2];
			String cust_rating = (String) a[3];
			String acct_no = (String) a[4];
			String acct_name = (String) a[5];
			String tran_type = (String) a[6];
			String tran_sub_type = (String) a[7];
			Date tran_date = (Date) a[8];
			String tran_id = (String) a[9];
			BigDecimal part_tran_id = (BigDecimal) a[10];
			String part_tran_type = (String) a[11];
			String tran_crncy = (String) a[12];
			BigDecimal tran_amt = (BigDecimal) a[13];
			BigDecimal tran_amt_orgin = (BigDecimal) a[14];
			String tran_category = (String) a[15];
			Character qtr_flg = (Character) a[16];
			Character entity_flg = (Character) a[17];
			Character del_flg = (Character) a[18];
			Character modify_flg = (Character) a[19];
			Date entry_date = (Date) a[20];
			Date modify_date = (Date) a[21];
			Date verify_date = (Date) a[22];
			String entry_user = (String) a[23];
			String modify_user = (String) a[24];
			String verify_user = (String) a[25];
			String report_code = (String) a[26];
			String report_name = (String) a[27];
			Date report_date = (Date) a[28];
			Character arch_flg = (Character) a[29];
			String cell_mapping = (String) a[30];
			String process_owner = (String) a[31];
			String bank_id = (String) a[32];
			Date cust_rating_date = (Date) a[33];
			String tran_particular = (String) a[34];
			String tran_channel = (String) a[35];
			String cntry_res = (String) a[36];
			String cnty_incorp = (String) a[37];
			String cntry_oper = (String) a[38];
			String aml_code_1 = (String) a[39];
			String aml_code_2 = (String) a[40];
			String aml_code_3 = (String) a[41];
			String aml_code_4 = (String) a[42];
			String aml_code_5 = (String) a[43];
			String aml_code_6 = (String) a[44];
			String aml_code_7 = (String) a[45];
			String aml_code_8 = (String) a[46];
			String aml_code_9 = (String) a[47];
			String aml_code_10 = (String) a[48];
			String t1_report = (String) a[49];
			String t2_report = (String) a[50];
			String t4_report = (String) a[51];
			String t5_report = (String) a[52];
			String t6_report = (String) a[53];
			String t7_report = (String) a[54];
			String t9_report = (String) a[55];
			String t12a_report = (String) a[56];
			String t12b_report = (String) a[57];
			String t12c_report = (String) a[58];
			String t12d_report = (String) a[59];
			String t12e_report = (String) a[60];
			String t12f_report = (String) a[61];
			String t12g_report = (String) a[62];
			String t13a_report = (String) a[63];
			String t13b_report = (String) a[64];
			String t14a_report = (String) a[65];
			String t14b_report = (String) a[66];
			String t15a_report = (String) a[67];
			String t15b_report = (String) a[68];
			String t16a_report = (String) a[69];
			String t16b_report = (String) a[70];
			String t16c_report = (String) a[71];
			String t23_report = (String) a[72];
			String t26_report = (String) a[73];
			String t27_report = (String) a[74];
			String t29_report = (String) a[75];
			String t32_report = (String) a[76];
			BigDecimal srl_num = (BigDecimal) a[77];

			RBSTransactionMasterEntity py = new RBSTransactionMasterEntity(cust_id, cust_name, cust_type, cust_rating,
					acct_no, acct_name, tran_type, tran_sub_type,  tran_date, tran_id,
					part_tran_id, part_tran_type, tran_crncy, tran_amt,
					 tran_amt_orgin, tran_category, qtr_flg, entity_flg, del_flg,
					modify_flg, entry_date, modify_date,  verify_date, entry_user,
					modify_user, verify_user, report_code, report_name, report_date,
					arch_flg, cell_mapping, process_owner, bank_id,cust_rating_date,
					tran_particular, tran_channel, cntry_res, cnty_incorp, cntry_oper,
					aml_code_1, aml_code_2, aml_code_3, aml_code_4, aml_code_5,
					aml_code_6, aml_code_7, aml_code_8, aml_code_9, aml_code_10,
					t1_report, t2_report, t4_report, t5_report, t6_report, t7_report,
					t9_report, t12a_report, t12b_report, t12c_report, t12d_report,
					t12e_report, t12f_report, t12g_report, t13a_report, t13b_report,
					t14a_report, t14b_report, t15a_report, t15b_report, t16a_report,
					t16b_report, t16c_report, t23_report, t26_report, t27_report,
					t29_report, t32_report,srl_num);

			T1Dt1.add(py);

		}
		;

		List<Object> pagedlist;

		if (T1Dt1.size() < startItem) {
			pagedlist = Collections.emptyList();
		} else {
			int toIndex = Math.min(startItem + pageSize, T1Dt1.size());
			pagedlist = T1Dt1.subList(startItem, toIndex);
		}

		logger.info("Converting to Page");
		Page<Object> T1Dt1Page = new PageImpl<Object>(pagedlist, PageRequest.of(currentPage, pageSize), T1Dt1.size());

		mv.setViewName("RR"+"/"+"BRF001A::reportcontent");
		mv.addObject("reportdetails", T1Dt1Page);
		mv.addObject("reportmaster", T1Master);
		mv.addObject("singledetail", new T1CurProdDetail());
		mv.addObject("reportsflag", "reportsflag");
		mv.addObject("menu", reportId);
		return mv;
	}

	
	public File getFile(String reportId, String fromdate, String todate, String currency, String dtltype,
			String filetype) throws FileNotFoundException, JRException, SQLException {

		DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");

		String path = this.env.getProperty("output.exportpath");
		String fileName = "";
		String zipFileName = "";
		File outputFile;

		logger.info("Getting Output file :" + reportId);
		fileName = "t" + reportId + "_" + todate;
		/*try {
			SimpleDateFormat dateFormat1 = new SimpleDateFormat("dd-MM-yyyy");
			Date ConDate = dateFormat1.parse(todate);
			System.out.println(ConDate);
			SimpleDateFormat formatter1 = new SimpleDateFormat("dd-MM-yyyy");
			String strDate1 = formatter1.format(ConDate);
			fileName = "t" + reportId + "_" + strDate1;
			

		} catch (ParseException e1) {

			logger.info(e1.getMessage());
			e1.printStackTrace();
		}*/

		if (!filetype.equals("xbrl")) {

			try {
				InputStream jasperFile;
				logger.info("Getting Jasper file :" + reportId);
				if (filetype.equals("detailexcel")) {
					if (dtltype.equals("report")) {

						jasperFile = this.getClass().getResourceAsStream("/static/jasper/Details.jrxml");
					} else {
						jasperFile = this.getClass().getResourceAsStream("/static/jasper/BRF071.jrxml");
					}

				} else {
					if (dtltype.equals("report")) {
						logger.info("Inside report");
						jasperFile = this.getClass().getResourceAsStream("/static/jasper/BRF071.jrxml");
					} else {
						jasperFile = this.getClass().getResourceAsStream("/static/jasper/BRF071.jrxml");
					}
				}

				/* JasperReport jr = (JasperReport) JRLoader.loadObject(jasperFile); */
				JasperReport jr = JasperCompileManager.compileReport(jasperFile);
				HashMap<String, Object> map = new HashMap<String, Object>();

				logger.info("Assigning Parameters for Jasper");
				map.put("REPORT_DATE", todate);
			/*	try {
					SimpleDateFormat dateFormat1 = new SimpleDateFormat("dd-MM-yyyy");
					Date ConDate = dateFormat1.parse(todate);
					SimpleDateFormat formatter1 = new SimpleDateFormat("dd-MMM-yyyy");
					String strDate1 = formatter1.format(ConDate);

					String today = dateFormat.format(new SimpleDateFormat("dd-MMM-yyyy").parse(strDate1));
					
				} catch (ParseException e1) {

					logger.info(e1.getMessage());
					e1.printStackTrace();
				}
*/
				if (filetype.equals("pdf")) {
					System.out.println("PDF");
					fileName = fileName + ".pdf";
					path += fileName;
					JasperPrint jp = JasperFillManager.fillReport(jr, map, srcdataSource.getConnection());
					JasperExportManager.exportReportToPdfFile(jp, path);
					logger.info("PDF File exported");
				} else {

					System.out.println("EXCEEEEEll");
					fileName = fileName + ".xlsx";
					path += fileName;
					JasperPrint jp = JasperFillManager.fillReport(jr, map, srcdataSource.getConnection());
					JRXlsxExporter exporter = new JRXlsxExporter();
					exporter.setExporterInput(new SimpleExporterInput(jp));
					exporter.setExporterOutput(new SimpleOutputStreamExporterOutput(path));
					exporter.exportReport();
					logger.info("Excel File exported");
				}

			} catch (Exception e) {
				e.printStackTrace();
			}

		}
		outputFile = new File(path);

		return outputFile;
	}
	
	

	public String detailChanges(RBSTransactionMasterEntity detail, Character changeType, String userid,Date todate) {

		String msg = "";
		
		


		try {
			
			Session hs = sessionFactory.getCurrentSession();

			if (changeType.equals('A')) {
				
				
				detail.setTran_date(new Date());
				detail.setDel_flg('N'); 
				//Date d="31-MAR-23";

				detail.setReport_date(todate);
				hs.save(detail);
				
				logger.info("Added Record");
				msg = "Added Successfully";
			} else if (changeType.equals('E')) {
				
				//detail.setCust_id(userid);
				//detail.setTran_date(new Date());
				
				hs.update(detail);
				logger.info("Edited Record");
				msg = "Edited Successfully";
				
			} else if (changeType.equals('D')) {
				
				//detail.setCust_id(userid);
				//detail.setTran_date(new Date());
				detail.setDel_flg('Y'); 
				hs.delete(detail);
				logger.info("Deleted Record");
				msg = "Deleted Successfully";
			}

		} catch (Exception e) {

			msg = "error occured. Please contact Administrator";
			e.printStackTrace();
		}

		return msg;
	}
	
	
		public void SessionLogging(String menuname, String menuid, String sessionid, String userid, String ip,
			String status) {
		Session hs = sessionFactory.getCurrentSession();

		try {

			if (menuname.equals("LOGOUT")) {

				hs.createQuery("update XBRLSession set status='IN-ACTIVE' where session_id = ?1")
						.setParameter(1, sessionid).executeUpdate();

			} else {
				
				hs.save(new XBRLSession(menuname, menuid, sessionid, userid, ip, new Date(), status));
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

	}
	
}
