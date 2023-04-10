package fpt.edu.stafflink.components;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import fpt.edu.stafflink.R;
import fpt.edu.stafflink.adapters.CustomNavigationAdapter;
import fpt.edu.stafflink.models.responseDtos.FunctionResponse;

public class CustomNavigationComponent extends LinearLayout {

    RecyclerView customNavigationComponentMainElement;
    TextView customNavigationComponentError;
    CustomNavigationAdapter adapter;

    private CharSequence error;

    public CustomNavigationComponent(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        this.initView(context);
    }

    private void initView(Context context) {
        View view = inflate(context, R.layout.component_navigation_custom, this);
        customNavigationComponentMainElement = view.findViewById(R.id.customNavigationComponentMainElement);
        customNavigationComponentError = view.findViewById(R.id.customNavigationComponentError);

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        customNavigationComponentMainElement.setLayoutManager(layoutManager);

        adapter = new CustomNavigationAdapter(new ArrayList<>());
        customNavigationComponentMainElement.setAdapter(adapter);
    }

    public void setFunctions(List<FunctionResponse> functions) {
        this.adapter.setObjects(this.rearrangeFunctions(functions, null));
    }

    public void setFunctions(List<FunctionResponse> functions, String uri) {
        this.adapter.setUri(uri);
        this.adapter.setObjects(this.rearrangeFunctions(functions, null));
    }

    public void setError(CharSequence error) {
        this.error = error;
        customNavigationComponentError.setText(error);
    }

    public CharSequence getError() {
        return this.error;
    }

    public void setDrawerLayout(DrawerLayout drawerLayout) {
        this.adapter.setDrawerLayout(drawerLayout);
    }

    private List<FunctionResponse> rearrangeFunctions(List<FunctionResponse> functions, FunctionResponse parent) {
        List<FunctionResponse> rearrangedFunctions = new ArrayList<>();
        functions.forEach(function -> {
            if (
                    function.isDisplayed() &&
                            ((function.getParent() == null && parent == null) || (function.getParent() != null && parent != null && function.getParent().getId() == parent.getId()))
                    ) {
                rearrangedFunctions.add(function);
                rearrangedFunctions.addAll(rearrangeFunctions(functions, function));
            }
        });
        return rearrangedFunctions;
    }
}
