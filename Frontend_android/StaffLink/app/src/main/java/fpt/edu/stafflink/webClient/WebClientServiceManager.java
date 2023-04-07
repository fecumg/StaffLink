package fpt.edu.stafflink.webClient;

import fpt.edu.stafflink.webClient.services.ProjectService;
import fpt.edu.stafflink.webClient.services.impls.ProjectServiceImpl;

public class WebClientServiceManager {

    private static ProjectService projectServiceInstance;

    public static ProjectService getProjectServiceInstance() {
        if (projectServiceInstance == null) {
            projectServiceInstance = new ProjectServiceImpl();
        }
        return projectServiceInstance;
    }
}
