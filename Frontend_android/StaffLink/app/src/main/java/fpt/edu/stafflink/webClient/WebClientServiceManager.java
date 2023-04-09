package fpt.edu.stafflink.webClient;

import fpt.edu.stafflink.webClient.services.ProjectService;
import fpt.edu.stafflink.webClient.services.TaskService;
import fpt.edu.stafflink.webClient.services.impls.ProjectServiceImpl;
import fpt.edu.stafflink.webClient.services.impls.TaskServiceImpl;

public class WebClientServiceManager {

    private static ProjectService projectServiceInstance;
    private static TaskService taskServiceInstance;

    public static ProjectService getProjectServiceInstance() {
        if (projectServiceInstance == null) {
            projectServiceInstance = new ProjectServiceImpl();
        }
        return projectServiceInstance;
    }

    public static TaskService getTaskServiceInstance() {
        if (taskServiceInstance == null) {
            taskServiceInstance = new TaskServiceImpl();
        }
        return taskServiceInstance;
    }
}
