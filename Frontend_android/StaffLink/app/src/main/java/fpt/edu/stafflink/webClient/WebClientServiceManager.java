package fpt.edu.stafflink.webClient;

import fpt.edu.stafflink.webClient.services.AttachmentService;
import fpt.edu.stafflink.webClient.services.CheckItemService;
import fpt.edu.stafflink.webClient.services.CommentService;
import fpt.edu.stafflink.webClient.services.ProjectService;
import fpt.edu.stafflink.webClient.services.TaskService;
import fpt.edu.stafflink.webClient.services.impls.AttachmentServiceImpl;
import fpt.edu.stafflink.webClient.services.impls.CheckItemServiceImpl;
import fpt.edu.stafflink.webClient.services.impls.CommentServiceImpl;
import fpt.edu.stafflink.webClient.services.impls.ProjectServiceImpl;
import fpt.edu.stafflink.webClient.services.impls.TaskServiceImpl;

public class WebClientServiceManager {

    private static ProjectService projectService;
    private static TaskService taskService;
    private static AttachmentService attachmentService;
    private static CheckItemService checkItemService;
    private static CommentService commentService;

    public static ProjectService getProjectService() {
        if (projectService == null) {
            projectService = new ProjectServiceImpl();
        }
        return projectService;
    }

    public static TaskService getTaskService() {
        if (taskService == null) {
            taskService = new TaskServiceImpl();
        }
        return taskService;
    }

    public static AttachmentService getAttachmentServiceInstance() {
        if (attachmentService == null) {
            attachmentService = new AttachmentServiceImpl();
        }
        return attachmentService;
    }

    public static CheckItemService getCheckItemServiceInstance() {
        if (checkItemService == null) {
            checkItemService = new CheckItemServiceImpl();
        }
        return checkItemService;
    }

    public static CommentService getCommentServiceInstance() {
        if (commentService == null) {
            commentService = new CommentServiceImpl();
        }
        return commentService;
    }
}
