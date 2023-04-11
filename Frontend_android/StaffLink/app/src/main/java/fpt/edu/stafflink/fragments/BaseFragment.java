package fpt.edu.stafflink.fragments;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import fpt.edu.stafflink.BaseActivity;

public class BaseFragment extends Fragment {
    private BaseActivity baseActivity;
    private Context context;

    protected BaseActivity getBaseActivity() {
        return baseActivity;
    }
    protected Context retrieveContext() {
        return this.context;
    }

    @Override
    public void onAttach (@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof BaseActivity){
            baseActivity = (BaseActivity) context;
        }
        this.context = context;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
}
