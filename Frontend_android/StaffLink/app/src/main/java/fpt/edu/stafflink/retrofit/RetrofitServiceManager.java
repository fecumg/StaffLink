package fpt.edu.stafflink.retrofit;

import android.content.Context;

import fpt.edu.stafflink.retrofit.services.AttachmentService;
import fpt.edu.stafflink.retrofit.services.AuthenticationService;
import fpt.edu.stafflink.retrofit.services.CheckItemService;
import fpt.edu.stafflink.retrofit.services.CommentService;
import fpt.edu.stafflink.retrofit.services.FunctionService;
import fpt.edu.stafflink.retrofit.services.ProjectService;
import fpt.edu.stafflink.retrofit.services.RoleService;
import fpt.edu.stafflink.retrofit.services.TaskService;
import fpt.edu.stafflink.retrofit.services.UserService;

public class RetrofitServiceManager {
    public static AuthenticationService getAuthenticationService(Context context) {
        return RetrofitManager
                .getRetrofitInstance(context)
                .create(AuthenticationService.class);
    }

    public static UserService getUserService(Context context) {
        return RetrofitManager
                .getRetrofitInstance(context)
                .create(UserService.class);
    }

    public static RoleService getRoleService(Context context) {
        return RetrofitManager
                .getRetrofitInstance(context)
                .create(RoleService.class);
    }

    public static FunctionService getFunctionService(Context context) {
        return RetrofitManager
                .getRetrofitInstance(context)
                .create(FunctionService.class);
    }

    public static ProjectService getProjectService(Context context) {
        return RetrofitManager
                .getRetrofitInstance(context)
                .create(ProjectService.class);
    }

    public static TaskService getTaskService(Context context) {
        return RetrofitManager
                .getRetrofitInstance(context)
                .create(TaskService.class);
    }

    public static AttachmentService getAttachmentService(Context context) {
        return RetrofitManager
                .getRetrofitInstance(context)
                .create(AttachmentService.class);
    }

    public static CheckItemService getCheckItemService(Context context) {
        return RetrofitManager
                .getRetrofitInstance(context)
                .create(CheckItemService.class);
    }

    public static CommentService getCommentService(Context context) {
        return RetrofitManager
                .getRetrofitInstance(context)
                .create(CommentService.class);
    }
}
