package fpt.edu.stafflink.fragments;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import fpt.edu.stafflink.BaseActivity;

public class BaseFragment extends Fragment {
    BaseActivity baseActivity;

    protected BaseActivity getBaseActivity() {
        return baseActivity;
    }

    @Override
    public void onAttach (Context context) {
        super.onAttach(context);
        if (context instanceof BaseActivity){
            baseActivity = (BaseActivity) context;
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
}
