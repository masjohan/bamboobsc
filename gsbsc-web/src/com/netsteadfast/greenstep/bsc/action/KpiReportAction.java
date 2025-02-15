/* 
 * Copyright 2012-2016 bambooCORE, greenstep of copyright Chen Xin Nien
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 * -----------------------------------------------------------------------
 * 
 * author: 	Chen Xin Nien
 * contact: chen.xin.nien@gmail.com
 * 
 */
package com.netsteadfast.greenstep.bsc.action;

import java.util.Map;

import javax.annotation.Resource;

import org.apache.struts2.ServletActionContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;

import com.netsteadfast.greenstep.base.action.BaseSupportAction;
import com.netsteadfast.greenstep.base.action.IBaseAdditionalSupportAction;
import com.netsteadfast.greenstep.base.exception.ControllerException;
import com.netsteadfast.greenstep.base.exception.ServiceException;
import com.netsteadfast.greenstep.base.model.ControllerAuthority;
import com.netsteadfast.greenstep.base.model.ControllerMethodAuthority;
import com.netsteadfast.greenstep.base.model.YesNo;
import com.netsteadfast.greenstep.base.sys.UserAccountHttpSessionSupport;
import com.netsteadfast.greenstep.bsc.model.BscMeasureDataFrequency;
import com.netsteadfast.greenstep.bsc.service.IEmployeeService;
import com.netsteadfast.greenstep.bsc.service.IOrganizationService;
import com.netsteadfast.greenstep.bsc.service.IVisionService;
import com.netsteadfast.greenstep.bsc.service.logic.IReportRoleViewLogicService;
import com.netsteadfast.greenstep.po.hbm.BbEmployee;
import com.netsteadfast.greenstep.po.hbm.BbOrganization;
import com.netsteadfast.greenstep.po.hbm.BbVision;
import com.netsteadfast.greenstep.util.MenuSupportUtils;
import com.netsteadfast.greenstep.vo.EmployeeVO;
import com.netsteadfast.greenstep.vo.OrganizationVO;
import com.netsteadfast.greenstep.vo.VisionVO;

@ControllerAuthority(check=true)
@Controller("bsc.web.controller.KpiReportAction")
@Scope
public class KpiReportAction extends BaseSupportAction implements IBaseAdditionalSupportAction {
	private static final long serialVersionUID = 1484056199465002823L;
	private IVisionService<VisionVO, BbVision, String> visionService;
	private IOrganizationService<OrganizationVO, BbOrganization, String> organizationService;
	private IEmployeeService<EmployeeVO, BbEmployee, String> employeeService;
	private IReportRoleViewLogicService reportRoleViewLogicService;
	private Map<String, String> visionMap = this.providedSelectZeroDataMap(true);
	private Map<String, String> frequencyMap = BscMeasureDataFrequency.getFrequencyMap(true);
	private Map<String, String> measureDataOrganizationMap = this.providedSelectZeroDataMap(true);
	private Map<String, String> measureDataEmployeeMap = this.providedSelectZeroDataMap(true);
	
	public KpiReportAction() {
		super();
	}
	
	public IVisionService<VisionVO, BbVision, String> getVisionService() {
		return visionService;
	}

	@Autowired
	@Required
	@Resource(name="bsc.service.VisionService")	
	public void setVisionService(
			IVisionService<VisionVO, BbVision, String> visionService) {
		this.visionService = visionService;
	}
	
	public IOrganizationService<OrganizationVO, BbOrganization, String> getOrganizationService() {
		return organizationService;
	}

	@Autowired
	@Required
	@Resource(name="bsc.service.OrganizationService")		
	public void setOrganizationService(
			IOrganizationService<OrganizationVO, BbOrganization, String> organizationService) {
		this.organizationService = organizationService;
	}

	public IEmployeeService<EmployeeVO, BbEmployee, String> getEmployeeService() {
		return employeeService;
	}

	@Autowired
	@Required
	@Resource(name="bsc.service.EmployeeService")		
	public void setEmployeeService(
			IEmployeeService<EmployeeVO, BbEmployee, String> employeeService) {
		this.employeeService = employeeService;
	}

	public IReportRoleViewLogicService getReportRoleViewLogicService() {
		return reportRoleViewLogicService;
	}

	@Autowired
	@Required
	@Resource(name="bsc.service.logic.ReportRoleViewLogicService")		
	public void setReportRoleViewLogicService(
			IReportRoleViewLogicService reportRoleViewLogicService) {
		this.reportRoleViewLogicService = reportRoleViewLogicService;
	}

	private void initData() throws ServiceException, Exception {
		this.visionMap = this.visionService.findForMap(true);		
		if ( YesNo.YES.equals(super.getIsSuperRole()) ) {
			this.measureDataOrganizationMap = this.organizationService.findForMap(true);
			this.measureDataEmployeeMap = this.employeeService.findForMap(true);
			return;
		} 
		this.measureDataOrganizationMap = this.reportRoleViewLogicService.findForOrganizationMap(
				true, this.getAccountId());
		this.measureDataEmployeeMap = this.reportRoleViewLogicService.findForEmployeeMap(
				true, this.getAccountId());
		/**
		 * 沒有資料表示,沒有限定使用者的角色,只能選取某些部門或某些員工
		 * 因為沒有限定就全部取出
		 */
		if ( this.measureDataOrganizationMap.size() <= 1 && this.measureDataEmployeeMap.size() <= 1 ) { // 第1筆是 - Please select -
			this.measureDataOrganizationMap = this.organizationService.findForMap(true);
			this.measureDataEmployeeMap = this.employeeService.findForMap(true);			
		}		
	}
	
	/**
	 * bsc.kpiReportAction.action
	 */
	@ControllerMethodAuthority(programId="BSC_PROG003D0001Q")
	public String execute() throws Exception {
		try {
			this.initData();
		} catch (ControllerException e) {
			this.setPageMessage(e.getMessage().toString());
		} catch (ServiceException e) {
			this.setPageMessage(e.getMessage().toString());
		} catch (Exception e) {
			e.printStackTrace();
			this.setPageMessage(e.getMessage().toString());
		}
		return SUCCESS;				
	}
	
	/**
	 * bsc.kpiReportOpenWindowAction.action
	 * bsc.kpiReportCoffeeChartOpenWindowAction.action
	 */
	@ControllerMethodAuthority(programId="BSC_PROG003D0001Q")	
	public String openWindow() throws Exception {
		try {
			
		} catch (ControllerException e) {
			this.setPageMessage(e.getMessage().toString());
		} catch (ServiceException e) {
			this.setPageMessage(e.getMessage().toString());
		} catch (Exception e) {
			e.printStackTrace();
			this.setPageMessage(e.getMessage().toString());
		}
		return SUCCESS;			
	}
	
	/**
	 * bsc.kpiPeriodTrendsReportAction.action
	 */
	@ControllerMethodAuthority(programId="BSC_PROG003D0007Q")
	public String periodTrends() throws Exception {
		try {
			this.initData();
		} catch (ControllerException e) {
			this.setPageMessage(e.getMessage().toString());
		} catch (ServiceException e) {
			this.setPageMessage(e.getMessage().toString());
		} catch (Exception e) {
			e.printStackTrace();
			this.setPageMessage(e.getMessage().toString());
		}
		return SUCCESS;				
	}	

	@Override
	public String getProgramName() {
		try {
			return MenuSupportUtils.getProgramName(this.getProgramId(), UserAccountHttpSessionSupport.getLang( ServletActionContext.getContext() ));
		} catch (ServiceException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "";
	}

	@Override
	public String getProgramId() {
		return super.getActionMethodProgramId();
	}

	public Map<String, String> getVisionMap() {
		return visionMap;
	}

	public Map<String, String> getFrequencyMap() {
		return frequencyMap;
	}

	public Map<String, String> getMeasureDataOrganizationMap() {
		return measureDataOrganizationMap;
	}

	public Map<String, String> getMeasureDataEmployeeMap() {
		return measureDataEmployeeMap;
	}

}
