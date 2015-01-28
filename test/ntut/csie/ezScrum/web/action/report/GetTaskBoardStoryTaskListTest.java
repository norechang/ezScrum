package ntut.csie.ezScrum.web.action.report;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

import ntut.csie.ezScrum.issue.core.IIssue;
import ntut.csie.ezScrum.issue.core.ITSEnum;
import ntut.csie.ezScrum.issue.sql.service.core.Configuration;
import ntut.csie.ezScrum.refactoring.manager.ProjectManager;
import ntut.csie.ezScrum.test.CreateData.AddStoryToSprint;
import ntut.csie.ezScrum.test.CreateData.AddTaskToStory;
import ntut.csie.ezScrum.test.CreateData.AddUserToRole;
import ntut.csie.ezScrum.test.CreateData.CreateAccount;
import ntut.csie.ezScrum.test.CreateData.CreateProject;
import ntut.csie.ezScrum.test.CreateData.CreateSprint;
import ntut.csie.ezScrum.test.CreateData.InitialSQL;
import ntut.csie.ezScrum.web.dataInfo.AttachFileInfo;
import ntut.csie.ezScrum.web.dataObject.AttachFileObject;
import ntut.csie.ezScrum.web.helper.ProductBacklogHelper;
import ntut.csie.ezScrum.web.logic.SprintBacklogLogic;
import ntut.csie.jcis.core.util.FileUtil;
import ntut.csie.jcis.resource.core.IPath;
import ntut.csie.jcis.resource.core.IProject;
import servletunit.struts.MockStrutsTestCase;

import com.google.gson.Gson;
import com.google.gson.internal.LinkedHashTreeMap;

public class GetTaskBoardStoryTaskListTest extends MockStrutsTestCase {
	private CreateProject mCreateProject;
	private CreateSprint mCreateSprint;
	private Configuration mConfig;
	private IProject mProject;

	private Gson gson;
	public GetTaskBoardStoryTaskListTest(String testMethod) {
		super(testMethod);
	}

	protected void setUp() throws Exception {
		mConfig = new Configuration();
		mConfig.setTestMode(true);
		mConfig.save();
		
		// 初始化 SQL
		InitialSQL ini = new InitialSQL(mConfig);
		ini.exe();

		// 新增一測試專案
		mCreateProject = new CreateProject(1);
		mCreateProject.exeCreate();
		mProject = mCreateProject.getProjectList().get(0);

		// 新增1筆 Sprint Plan
		mCreateSprint = new CreateSprint(1, mCreateProject);
		mCreateSprint.exe();

		gson = new Gson();
		super.setUp();

		setContextDirectory(new File(mConfig.getBaseDirPath() + "/WebContent"));	// 設定讀取的struts-config檔案路徑
		setServletConfigFile("/WEB-INF/struts-config.xml");
		setRequestPathInfo("/getTaskBoardStoryTaskList");

		// ============= release ==============
		ini = null;
	}

	protected void tearDown() throws IOException, Exception {
	 	// 初始化 SQL
		InitialSQL ini = new InitialSQL(mConfig);
		ini.exe();

		// 刪除外部檔案
		ProjectManager projectManager = new ProjectManager();
		projectManager.deleteAllProject();
		projectManager.initialRoleBase(mConfig.getDataPath());
		
		mConfig.setTestMode(false);
		mConfig.save();

		// ============= release ==============
		ini = null;
		mCreateProject = null;
		mCreateSprint = null;
		gson = null;
		mConfig = null;
		
		super.tearDown();
	}

	/**
	 * 1個 story，沒有 task
	 * filter 條件
	 * - 第1個 sprint
	 * - 所有人 ALL
	 */
	public void testGetTaskBoardStoryTaskList_1() throws Exception {
		// Sprint 加入1個 Story
		int storyCount = 1;
		AddStoryToSprint addStoryToSprint = new AddStoryToSprint(storyCount, 1, mCreateSprint, mCreateProject, "EST");
		addStoryToSprint.exe();
		IIssue story = addStoryToSprint.getStories().get(0);
		long stroyId = story.getIssueID();

		// ================ set request info ========================
		String projectName = mProject.getName();
		request.setHeader("Referer", "?PID=" + projectName);
		addRequestParameter("UserID", "ALL");
		addRequestParameter("sprintID", "1");

		// ================ set session info ========================
		request.getSession().setAttribute("UserSession", mConfig.getUserSession());

		// ================ 執行 action ======================
		actionPerform();

		// ================ assert ======================
		verifyNoActionErrors();
		verifyNoActionMessages();
		
		String result = response.getWriterBuffer().toString();
		StringBuilder expectedResponseText = new StringBuilder();
		expectedResponseText
		        .append("{")
		        .append("\"Stories\":[{")
		        .append("\"Id\":\"").append(stroyId).append("\",")
		        .append("\"Name\":\"").append(story.getSummary()).append("\",")
		        .append("\"Value\":\"").append(story.getValue()).append("\",")
		        .append("\"Estimate\":\"").append(story.getEstimated()).append("\",")
		        .append("\"Importance\":\"").append(story.getImportance()).append("\",")
		        .append("\"Tag\":\"\",")
		        .append("\"Status\":\"").append(story.getStatus()).append("\",")
		        .append("\"Notes\":\"").append(story.getNotes()).append("\",")
		        .append("\"HowToDemo\":\"").append(story.getHowToDemo()).append("\",")
		        .append("\"Link\":\"/ezScrum/showIssueInformation.do?issueID=").append(stroyId).append("\",")
		        .append("\"Release\":\"").append(story.getReleaseID()).append("\",")
		        .append("\"Sprint\":\"").append(mCreateSprint.getSprintIDList().get(0)).append("\",")
		        .append("\"Attach\":false,")
		        .append("\"AttachFileList\":[],")
		        .append("\"Tasks\":[]}],")
		        .append("\"success\":true,\"Total\":1}");
		assertEquals(expectedResponseText.toString(), result);
	}

	/**
	 * 10個 story，沒有 task
	 * filter 條件
	 * - 第1個 sprint
	 * - 所有人 ALL
	 */
	public void testGetTaskBoardStoryTaskList_2() throws Exception {
		// Sprint 加入10個 Story
		int storyCount = 10;
		AddStoryToSprint addStoryToSprint = new AddStoryToSprint(storyCount, 1, mCreateSprint, mCreateProject, "EST");
		addStoryToSprint.exe();

		// ================ set request info ========================
		String projectName = mProject.getName();
		request.setHeader("Referer", "?PID=" + projectName);
		addRequestParameter("UserID", "ALL");
		addRequestParameter("sprintID", "1");

		// ================ set session info ========================
		request.getSession().setAttribute("UserSession", mConfig.getUserSession());

		// ================ 執行 action ======================
		actionPerform();

		// ================ assert ======================
		verifyNoActionErrors();
		verifyNoActionMessages();
		
		String result = response.getWriterBuffer().toString();
		HashMap<String, Object> resultMap = gson.fromJson(result, HashMap.class);
		ArrayList<LinkedHashTreeMap<String, Object>> storyList = (ArrayList<LinkedHashTreeMap<String, Object>>) resultMap.get("Stories");
		Number total = (Number) resultMap.get("Total");
		List<IIssue> expectedStories = addStoryToSprint.getStories();
		IIssue expectedStory;

		assertEquals(true, resultMap.get("success"));
		assertEquals(storyCount, total.intValue());
		for (int i = 0; i < storyCount; i++) {
			expectedStory = expectedStories.get(i);
			assertEquals(String.valueOf(expectedStory.getIssueID()), storyList.get(i).get("Id"));
			assertEquals(expectedStory.getSummary(), storyList.get(i).get("Name"));
			assertEquals(expectedStory.getValue(), storyList.get(i).get("Value"));
			assertEquals(expectedStory.getEstimated(), storyList.get(i).get("Estimate"));
			assertEquals(expectedStory.getImportance(), storyList.get(i).get("Importance"));
			assertEquals("", storyList.get(i).get("Tag"));
			assertEquals(expectedStory.getStatus(), storyList.get(i).get("Status"));
			assertEquals(expectedStory.getNotes(), storyList.get(i).get("Notes"));
			assertEquals(expectedStory.getHowToDemo(), storyList.get(i).get("HowToDemo"));
			assertEquals(expectedStory.getIssueLink(), storyList.get(i).get("Link"));
			assertEquals(expectedStory.getReleaseID(), storyList.get(i).get("Release"));
			assertEquals(mCreateSprint.getSprintIDList().get(0), storyList.get(i).get("Sprint"));
			assertEquals(false, storyList.get(i).get("Attach"));
			assertEquals(expectedStory.getAttachFiles(), storyList.get(i).get("AttachFileList"));
			assertEquals(new ArrayList<LinkedHashTreeMap>(), storyList.get(i).get("Tasks"));
		}
	}

	/**
	 * 1個 story，1個 task
	 * filter 條件
	 * - 第1個 sprint
	 * - 所有人 ALL
	 */
	public void testGetTaskBoardStoryTaskList_3() throws Exception {
		int storyCount = 1;
		int taskCount = 1;
		// Sprint 加入1個 Story
		AddStoryToSprint addStoryToSprint = new AddStoryToSprint(storyCount, 1, mCreateSprint, mCreateProject, "EST");
		addStoryToSprint.exe();
		
		IIssue story = addStoryToSprint.getStories().get(0);
		long stroyId = story.getIssueID();

		// 每個 Story 加入1個 task
		AddTaskToStory addTaskToStory = new AddTaskToStory(taskCount, 1, addStoryToSprint, mCreateProject);
		addTaskToStory.exe();
		
		IIssue task = addTaskToStory.getTaskList().get(0);
		long taskId = task.getIssueID();

		// ================ set request info ========================
		String projectName = mProject.getName();
		request.setHeader("Referer", "?PID=" + projectName);
		addRequestParameter("UserID", "ALL");
		addRequestParameter("sprintID", "");

		// ================ set session info ========================
		request.getSession().setAttribute("UserSession", mConfig.getUserSession());

		// ================ 執行 action ======================
		actionPerform();

		// ================ assert ======================
		verifyNoActionErrors();
		verifyNoActionMessages();
		
		String result = response.getWriterBuffer().toString();
		StringBuilder expectedResponseText = new StringBuilder();
		expectedResponseText
		        .append("{")
		        .append("\"Stories\":[{")
		        .append("\"Id\":\"").append(stroyId).append("\",")
		        .append("\"Name\":\"").append(story.getSummary()).append("\",")
		        .append("\"Value\":\"").append(story.getValue()).append("\",")
		        .append("\"Estimate\":\"").append(story.getEstimated()).append("\",")
		        .append("\"Importance\":\"").append(story.getImportance()).append("\",")
		        .append("\"Tag\":\"\",")
		        .append("\"Status\":\"").append(story.getStatus()).append("\",")
		        .append("\"Notes\":\"").append(story.getNotes()).append("\",")
		        .append("\"HowToDemo\":\"").append(story.getHowToDemo()).append("\",")
		        .append("\"Link\":\"/ezScrum/showIssueInformation.do?issueID=").append(stroyId).append("\",")
		        .append("\"Release\":\"").append(story.getReleaseID()).append("\",")
		        .append("\"Sprint\":\"").append(mCreateSprint.getSprintIDList().get(0)).append("\",")
		        .append("\"Attach\":false,")
		        .append("\"AttachFileList\":[],")
		        .append("\"Tasks\":[{")
		        .append("\"Id\":\"").append(taskId).append("\",")
		        .append("\"Name\":\"").append(task.getSummary()).append("\",")
		        .append("\"Estimate\":\"").append(task.getEstimated()).append("\",")
		        .append("\"RemainHours\":\"").append(task.getRemains()).append("\",")
		        .append("\"Handler\":\"\",")
		        .append("\"Notes\":\"").append(task.getNotes()).append("\",")
		        .append("\"AttachFileList\":[],")
		        .append("\"Attach\":false,")
		        .append("\"Status\":\"").append(task.getStatus()).append("\",")
		        .append("\"Partners\":\"\",")
		        .append("\"Link\":\"/ezScrum/showIssueInformation.do?issueID=").append(taskId).append("\",")
		        .append("\"Actual\":\"").append(task.getActualHour()).append("\"")
		        .append("}]}],")
		        .append("\"success\":true,\"Total\":1}");
		assertEquals(expectedResponseText.toString(), result);
	}

	/**
	 * 5個 story，1個 task
	 * filter 條件
	 * - 第1個 sprint
	 * - TEST_ACCOUNT_ID_1
	 */
	public void testGetTaskBoardStoryTaskList_4() throws Exception {
		int storyCount = 5;
		int taskCount = 1;
		// Sprint 加入5個 Story
		AddStoryToSprint addStoryToSprint = new AddStoryToSprint(storyCount, 1, mCreateSprint, mCreateProject, "EST");
		addStoryToSprint.exe();

		// 每個 Story 加入1個 task
		AddTaskToStory addTaskToStory = new AddTaskToStory(taskCount, 1, addStoryToSprint, mCreateProject);
		addTaskToStory.exe();

		// 新建一個帳號並給於專案權限
		CreateAccount createAccount = new CreateAccount(1);
		createAccount.exe();
		
		AddUserToRole addUserToRole = new AddUserToRole(mCreateProject, createAccount);
		addUserToRole.exe_ST();

		SprintBacklogLogic sprintBacklogLogic = new SprintBacklogLogic(mProject, mConfig.getUserSession(), null);
		// 將第一個 story 跟 task 全都拉到 done, 用 TEST_ACCOUNT_ID_1 checkout task
		List<IIssue> tasks = addTaskToStory.getTaskList();
		List<IIssue> stories = addStoryToSprint.getStories();
		sprintBacklogLogic.checkOutTask(tasks.get(0).getIssueID(), tasks.get(0).getSummary(), createAccount.getAccount_ID(1), "", tasks.get(0).getNotes(), "");
		Thread.sleep(1000);
		sprintBacklogLogic.doneIssue(tasks.get(0).getIssueID(), tasks.get(0).getSummary(), tasks.get(0).getNotes(), null, null);
		sprintBacklogLogic.doneIssue(stories.get(0).getIssueID(), stories.get(0).getSummary(), stories.get(0).getNotes(), null, null);
		// 將第三個 task check out，用 TEST_ACCOUNT_ID_1 checkout task
		sprintBacklogLogic.checkOutTask(tasks.get(2).getIssueID(), tasks.get(2).getSummary(), createAccount.getAccount_ID(1), "", tasks.get(2).getNotes(), "");

		// ================ set request info ========================
		String projectName = mProject.getName();
		request.setHeader("Referer", "?PID=" + projectName);
		addRequestParameter("UserID", createAccount.getAccount_ID(1));
		addRequestParameter("sprintID", "1");

		// ================ set session info ========================
		request.getSession().setAttribute("UserSession", mConfig.getUserSession());

		// ================ 執行 action ======================
		actionPerform();

		// ================ assert ======================
		verifyNoActionErrors();
		verifyNoActionMessages();
		
		String result = response.getWriterBuffer().toString();
		HashMap<String, Object> resultMap = gson.fromJson(result, HashMap.class);
		ArrayList<LinkedHashTreeMap<String, Object>> storyList = (ArrayList<LinkedHashTreeMap<String, Object>>) resultMap.get("Stories");
		ArrayList<LinkedHashTreeMap<String, Object>> taskList;
		Number total = (Number) resultMap.get("Total");
		List<IIssue> expectedStories = addStoryToSprint.getStories();
		List<IIssue> expectedTasks = addTaskToStory.getTaskList();
		IIssue expectedStory, expectedTask;

		/**
		 * 一個 Story and task 被 TEST_ACCOUNT_ID_1 拉到 Done
		 * 一個 task 被 TEST_ACCOUNT_ID_1 拉到 Done
		 * 所以有兩個 Story 會被 Filter 出來(與其底下的 task)
		 */
		assertEquals(true, resultMap.get("success"));
		assertEquals(2, total.intValue());
		
		for (int i = 0; i < storyList.size(); i++) {
			expectedStory = expectedStories.get(i * 2);
			expectedTask = expectedTasks.get(i * 2);
			taskList = (ArrayList<LinkedHashTreeMap<String, Object>>) storyList.get(i).get("Tasks");
			assertEquals(String.valueOf(expectedStory.getIssueID()), storyList.get(i).get("Id"));
			assertEquals(expectedStory.getSummary(), storyList.get(i).get("Name"));
			assertEquals(expectedStory.getValue(), storyList.get(i).get("Value"));
			assertEquals(expectedStory.getEstimated(), storyList.get(i).get("Estimate"));
			assertEquals(expectedStory.getImportance(), storyList.get(i).get("Importance"));
			assertEquals("", storyList.get(i).get("Tag"));
			if (i == 0) {
				assertEquals(ITSEnum.CLOSED, storyList.get(i).get("Status"));
			} else {
				assertEquals(ITSEnum.S_NEW_STATUS, storyList.get(i).get("Status"));
			}
			assertEquals(expectedStory.getNotes(), storyList.get(i).get("Notes"));
			assertEquals(expectedStory.getHowToDemo(), storyList.get(i).get("HowToDemo"));
			assertEquals(expectedStory.getIssueLink(), storyList.get(i).get("Link"));
			assertEquals(expectedStory.getReleaseID(), storyList.get(i).get("Release"));
			assertEquals(mCreateSprint.getSprintIDList().get(0), storyList.get(i).get("Sprint"));
			assertEquals(false, storyList.get(i).get("Attach"));
			assertEquals(expectedStory.getAttachFiles(), storyList.get(i).get("AttachFileList"));
			for (int j = 0; j < taskList.size(); j++) {
				assertEquals(String.valueOf(expectedTask.getIssueID()), taskList.get(j).get("Id"));
				assertEquals(expectedTask.getSummary(), taskList.get(j).get("Name"));
				assertEquals(expectedTask.getEstimated(), taskList.get(j).get("Estimate"));
				assertEquals(createAccount.getAccount_ID(1), taskList.get(j).get("Handler"));
				assertEquals(expectedTask.getNotes(), taskList.get(j).get("Notes"));
				assertEquals(expectedTask.getAttachFiles(), taskList.get(j).get("AttachFileList"));
				assertEquals(false, taskList.get(j).get("Attach"));
				if (i == 0) {
					assertEquals(ITSEnum.CLOSED, taskList.get(j).get("Status"));
					assertEquals("0", taskList.get(j).get("RemainHours"));
				} else {
					assertEquals(ITSEnum.ASSIGNED, taskList.get(j).get("Status"));
					assertEquals(expectedTask.getRemains(), taskList.get(j).get("RemainHours"));
				}
				assertEquals(expectedTask.getPartners(), taskList.get(j).get("Partners"));
				assertEquals(expectedTask.getIssueLink(), taskList.get(j).get("Link"));
				assertEquals(expectedTask.getActualHour(), taskList.get(j).get("Actual"));
			}
		}
	}
	
	/**
	 * 1個 story attach 1個 file
	 * filter 條件
	 * - 第1個 sprint
	 * - TEST_ACCOUNT_ID_1
	 */
	public void testGetTaskBoardStoryTaskList_AttachFile_Story() throws Exception {
		// Sprint 加入1個 Story
		int storyCount = 1;
		AddStoryToSprint addStoryToSprint = new AddStoryToSprint(storyCount, 1, mCreateSprint, mCreateProject, "EST");
		addStoryToSprint.exe();

		IIssue story = addStoryToSprint.getStories().get(0);
		long storyId = story.getIssueID();

		// attach file
		final String FILE_NAME = "ezScrumTestFile";
		IPath fullPath = mProject.getFullPath();
		String targetPath = fullPath.getPathString() + File.separator + FILE_NAME;
		File file = new File(targetPath);
        BufferedWriter output = new BufferedWriter(new FileWriter(file));
        output.write("hello i am test");
        output.close();
        
        AttachFileInfo attachFileInfo = new AttachFileInfo();
        attachFileInfo.issueId = storyId;
        attachFileInfo.issueType = AttachFileObject.TYPE_STORY;
        attachFileInfo.name = FILE_NAME;
        attachFileInfo.contentType = "text/plain";
        attachFileInfo.projectName = mCreateProject.getProjectList().get(0).getName();
        
		ProductBacklogHelper pbHelper = new ProductBacklogHelper(mConfig.getUserSession(), mProject);
		pbHelper.addAttachFile(attachFileInfo, file);
		IIssue expectedStory = pbHelper.getIssue(storyId);

		try {
			FileUtil.delete(targetPath);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		// ================ set request info ========================
		String projectName = mProject.getName();
		request.setHeader("Referer", "?PID=" + projectName);
		addRequestParameter("UserID", "ALL");
		addRequestParameter("sprintID", "1");

		// ================ set session info ========================
		request.getSession().setAttribute("UserSession", mConfig.getUserSession());

		// ================ 執行 action ======================
		actionPerform();

		// ================ assert ======================
		verifyNoActionErrors();
		verifyNoActionMessages();
		String result = response.getWriterBuffer().toString();
		LinkedHashMap<String, Object> resultMap = gson.fromJson(result, LinkedHashMap.class);
		ArrayList<LinkedHashTreeMap<String, Object>> storyList = (ArrayList<LinkedHashTreeMap<String, Object>>) resultMap.get("Stories");
		assertEquals(String.valueOf(expectedStory.getIssueID()), storyList.get(0).get("Id"));
		assertEquals(expectedStory.getSummary(), storyList.get(0).get("Name"));
		assertEquals(expectedStory.getValue(), storyList.get(0).get("Value"));
		assertEquals(expectedStory.getEstimated(), storyList.get(0).get("Estimate"));
		assertEquals(expectedStory.getImportance(), storyList.get(0).get("Importance"));
		assertEquals("", storyList.get(0).get("Tag"));
		assertEquals(expectedStory.getStatus(), storyList.get(0).get("Status"));
		assertEquals(expectedStory.getNotes(), storyList.get(0).get("Notes"));
		assertEquals(expectedStory.getHowToDemo(), storyList.get(0).get("HowToDemo"));
		assertEquals(expectedStory.getIssueLink(), storyList.get(0).get("Link"));
		assertEquals(expectedStory.getReleaseID(), storyList.get(0).get("Release"));
		assertEquals(mCreateSprint.getSprintIDList().get(0), storyList.get(0).get("Sprint"));
		assertEquals(!expectedStory.getAttachFiles().isEmpty(), storyList.get(0).get("Attach"));
		LinkedHashTreeMap attachFile = ((List<LinkedHashTreeMap>) storyList.get(0).get("AttachFileList")).get(0);
		assertEquals(expectedStory.getAttachFiles().get(0).getId(), ((Double) attachFile.get("FileId")).longValue());
		assertEquals(expectedStory.getAttachFiles().get(0).getName(), attachFile.get("FileName"));
	}
	
	/**
	 * 1個task attach 1個file
	 * filter條件
	 * - 第1個sprint
	 * - TEST_ACCOUNT_ID_1
	 */
	public void testGetTaskBoardStoryTaskList_AttachFile_Task() throws Exception {
		// Sprint 加入1個 Story
		int storyCount = 1;
		int taskCount = 1;
		
		AddStoryToSprint addStoryToSprint = new AddStoryToSprint(storyCount, 1, mCreateSprint, mCreateProject, "EST");
		addStoryToSprint.exe();
		IIssue story = addStoryToSprint.getStories().get(0);
		long stroyId = story.getIssueID();
		
		// 每個 Story 加入1個 task
		AddTaskToStory addTaskToStory = new AddTaskToStory(taskCount, 1, addStoryToSprint, mCreateProject);
		addTaskToStory.exe();
		
		IIssue task = addTaskToStory.getTaskList().get(0);
		long taskId = task.getIssueID();
		
		// attach file
		final String FILE_NAME = "ezScrumTestFile";
		IPath fullPath = mProject.getFullPath();
		String targetPath = fullPath.getPathString() + File.separator + "ezScrumTestFile";
		File file = new File(targetPath);
		BufferedWriter output = new BufferedWriter(new FileWriter(file));
		output.write("hello i am test");
		output.close();
		
		AttachFileInfo attachFileInfo = new AttachFileInfo();
        attachFileInfo.issueId = taskId;
        attachFileInfo.issueType = AttachFileObject.TYPE_TASK;
        attachFileInfo.name = FILE_NAME;
        attachFileInfo.contentType = "text/plain";
        attachFileInfo.projectName = mCreateProject.getProjectList().get(0).getName();
		
		ProductBacklogHelper pbHelper = new ProductBacklogHelper(mConfig.getUserSession(), mProject);
		pbHelper.addAttachFile(attachFileInfo, file);
		
		IIssue expectedStory = pbHelper.getIssue(stroyId);
		IIssue expectedTask = pbHelper.getIssue(taskId);
		
		try {
			FileUtil.delete(targetPath);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		// ================ set request info ========================
		String projectName = mProject.getName();
		request.setHeader("Referer", "?PID=" + projectName);
		addRequestParameter("UserID", "ALL");
		addRequestParameter("sprintID", "1");
		
		// ================ set session info ========================
		request.getSession().setAttribute("UserSession", mConfig.getUserSession());
		
		// ================ 執行 action ======================
		actionPerform();
		
		// ================ assert ======================
		verifyNoActionErrors();
		verifyNoActionMessages();
		String result = response.getWriterBuffer().toString();
		LinkedHashMap<String, Object> resultMap = gson.fromJson(result, LinkedHashMap.class);
		ArrayList<LinkedHashTreeMap<String, Object>> storyList = (ArrayList<LinkedHashTreeMap<String, Object>>) resultMap.get("Stories");
		assertEquals(String.valueOf(expectedStory.getIssueID()), storyList.get(0).get("Id"));
		assertEquals(expectedStory.getSummary(), storyList.get(0).get("Name"));
		assertEquals(expectedStory.getValue(), storyList.get(0).get("Value"));
		assertEquals(expectedStory.getEstimated(), storyList.get(0).get("Estimate"));
		assertEquals(expectedStory.getImportance(), storyList.get(0).get("Importance"));
		assertEquals("", storyList.get(0).get("Tag"));
		assertEquals(expectedStory.getStatus(), storyList.get(0).get("Status"));
		assertEquals(expectedStory.getNotes(), storyList.get(0).get("Notes"));
		assertEquals(expectedStory.getHowToDemo(), storyList.get(0).get("HowToDemo"));
		assertEquals(expectedStory.getIssueLink(), storyList.get(0).get("Link"));
		assertEquals(expectedStory.getReleaseID(), storyList.get(0).get("Release"));
		assertEquals(mCreateSprint.getSprintIDList().get(0), storyList.get(0).get("Sprint"));
		assertEquals(!expectedStory.getAttachFiles().isEmpty(), storyList.get(0).get("Attach"));
		assertEquals(expectedStory.getAttachFiles(), storyList.get(0).get("AttachFileList"));
		
		ArrayList<LinkedHashTreeMap<String, Object>> taskList = (ArrayList<LinkedHashTreeMap<String, Object>>) storyList.get(0).get("Tasks");
		assertEquals(String.valueOf(expectedTask.getIssueID()), taskList.get(0).get("Id"));
		assertEquals(expectedTask.getSummary(), taskList.get(0).get("Name"));
		assertEquals(expectedTask.getEstimated(), taskList.get(0).get("Estimate"));
		assertEquals(expectedTask.getNotes(), taskList.get(0).get("Notes"));
		assertEquals(expectedTask.getStatus(), taskList.get(0).get("Status"));
		assertEquals(expectedTask.getRemains(), taskList.get(0).get("RemainHours"));
		assertEquals(expectedTask.getPartners(), taskList.get(0).get("Partners"));
		assertEquals(expectedTask.getIssueLink(), taskList.get(0).get("Link"));
		assertEquals(expectedTask.getActualHour(), taskList.get(0).get("Actual"));
		assertEquals(!expectedTask.getAttachFiles().isEmpty(), taskList.get(0).get("Attach"));
		
		LinkedHashTreeMap attachFile = ((List<LinkedHashTreeMap>) taskList.get(0).get("AttachFileList")).get(0);
		assertEquals(expectedTask.getAttachFiles().get(0).getId(), ((Double) attachFile.get("FileId")).longValue());
		assertEquals(expectedTask.getAttachFiles().get(0).getName(), attachFile.get("FileName"));
	}
}
