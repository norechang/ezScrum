package ntut.csie.ezScrum.web.support;

import ntut.csie.ezScrum.pic.core.ScrumRole;

public class AccessPermissionUI {
	public boolean AccessProductBacklog = false;
	public boolean AccessReleasePlan = false;
	public boolean AccessSprintPlan = false;
	public boolean AccessSprintBacklog = false;
	public boolean AccessTaskboard = false;
	public boolean AccessRetrospective = false;
	public boolean AccessUnplan = false;
	public boolean AccessReport = false;
	public boolean AccessEditProject = false;
	
	public AccessPermissionUI() {
		
	}
	
	public AccessPermissionUI(ScrumRole sr) {
		if (sr != null) {
			this.AccessProductBacklog = sr.getAccessProductBacklog();
			this.AccessReleasePlan = sr.getAccessReleasePlan();
			this.AccessSprintPlan = sr.getAccessSprintPlan();
			this.AccessSprintBacklog = sr.getAccessSprintBacklog();
			this.AccessTaskboard = sr.getAccessTaskBoard();
			this.AccessRetrospective = sr.getAccessRetrospective();
			this.AccessUnplan = sr.getAccessUnplanItem();
			this.AccessReport = sr.getAccessReport();
			this.AccessEditProject = sr.getAccessEditProject();
		}
	}
	
	public void parseJsonObjString(String JsonInfo) {
//		System.out.println(" J = = " + JsonInfo.toString());
	}
}
