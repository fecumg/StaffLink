package fpt.edu.stafflink.fragments;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import fpt.edu.stafflink.BaseActivity;
import io.reactivex.disposables.CompositeDisposable;
import reactor.core.Disposables;

public class BaseFragment extends Fragment {
    private BaseActivity baseActivity;
    private Context context;

    public CompositeDisposable compositeDisposable;
    public reactor.core.Disposable.Composite reactorCompositeDisposable;

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

        compositeDisposable = new CompositeDisposable();
        reactorCompositeDisposable = Disposables.composite();
    }

    @Override
    public void onResume() {
        super.onResume();

        if (compositeDisposable == null || compositeDisposable.isDisposed()) {
            compositeDisposable = new CompositeDisposable();
        }
        if (reactorCompositeDisposable == null || reactorCompositeDisposable.isDisposed()) {
            reactorCompositeDisposable = Disposables.composite();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (compositeDisposable != null) {
            compositeDisposable.dispose();
        }
        if (reactorCompositeDisposable != null) {
            reactorCompositeDisposable.dispose();
        }
    }
}
