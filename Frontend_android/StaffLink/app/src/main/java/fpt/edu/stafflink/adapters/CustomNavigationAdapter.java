package fpt.edu.stafflink.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.RecyclerView;

import org.apache.commons.lang3.StringUtils;

import java.util.List;

import fpt.edu.stafflink.R;
import fpt.edu.stafflink.models.responseDtos.FunctionResponse;
import fpt.edu.stafflink.utilities.ActivityUtils;
import fpt.edu.stafflink.utilities.DimenUtils;

public class CustomNavigationAdapter extends BaseAdapter<FunctionResponse, CustomNavigationAdapter.ViewHolder>{
    private static final int PADDING_STEP_IN_DP = 20;

    private String uri;

    DrawerLayout drawerLayout;

    public CustomNavigationAdapter(List<FunctionResponse> functions) {
        super(functions);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_navigation, parent, false);
        return new CustomNavigationAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        if (super.getItemCount() == 0) {
            return;
        }
        FunctionResponse function = objects.get(position);

        int paddingStepInPx = DimenUtils.dpToPx(holder.itemView.getContext(), PADDING_STEP_IN_DP);
        holder.itemNavigationMainLayout.setPadding(paddingStepInPx * this.getFunctionLevel(function, 0), 0, 0, 0);

        holder.itemNavigationMainElement.setText(function.getName());

        Context context = holder.itemNavigationMainElement.getContext();
        if (StringUtils.isNotEmpty(function.getUri().trim())) {
            holder.itemNavigationMainElement.setOnClickListener(view -> this.onClickItem(view, function));
        } else {
            holder.itemNavigationMainElement.setTextColor(ContextCompat.getColor(context, R.color.secondary));
        }

        if (StringUtils.isNotEmpty(this.uri) && this.uri.equals(function.getUri())) {
            holder.itemNavigationMainElement.setBackgroundColor(ContextCompat.getColor(context, R.color.transparent_shadow_40));
        }
    }

    private void onClickItem(View view, FunctionResponse function) {
        if (drawerLayout != null) {
            drawerLayout.closeDrawers();
        }

        if (function != null && StringUtils.isNotEmpty(function.getUri())) {
            ActivityUtils.goTo(view.getContext(), function.getUri());
        }
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    private int getFunctionLevel(FunctionResponse function, int initialLevel) {
        FunctionResponse parent = function.getParent();
        if (parent != null) {
            return getFunctionLevel(parent, ++ initialLevel);
        } else {
            return initialLevel;
        }
    }

    public void setDrawerLayout(DrawerLayout drawerLayout) {
        this.drawerLayout = drawerLayout;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        LinearLayout itemNavigationMainLayout;
        TextView itemNavigationMainElement;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            itemNavigationMainLayout = itemView.findViewById(R.id.itemNavigationMainLayout);
            itemNavigationMainElement = itemView.findViewById(R.id.itemNavigationMainElement);
        }
    }
}
