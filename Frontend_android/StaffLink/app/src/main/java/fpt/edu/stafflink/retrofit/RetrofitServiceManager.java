package fpt.edu.stafflink.retrofit;

import android.content.Context;

import fpt.edu.stafflink.services.AuthenticationService;
import fpt.edu.stafflink.services.FunctionService;
import fpt.edu.stafflink.services.ProjectService;
import fpt.edu.stafflink.services.RoleService;
import fpt.edu.stafflink.services.UserService;

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
}
