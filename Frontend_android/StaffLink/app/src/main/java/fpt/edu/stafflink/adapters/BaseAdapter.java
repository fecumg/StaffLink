package fpt.edu.stafflink.adapters;

import static fpt.edu.stafflink.constants.AdapterActionParam.PARAM_ID;
import static fpt.edu.stafflink.constants.AdapterActionParam.PARAM_POSITION;

import android.content.Intent;
import android.view.View;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.RecyclerView;

import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

import fpt.edu.stafflink.utilities.GenericUtils;


public abstract class BaseAdapter<T, TViewHolder extends RecyclerView.ViewHolder> extends RecyclerView.Adapter<TViewHolder> {
    protected List<T> objects;
    protected String action;

    public BaseAdapter(List<T> objects) {
        this.objects = objects;
    }

    public BaseAdapter(List<T> objects, String action) {
        this.objects = objects;
        this.action = action;
    }

    @Override
    public int getItemCount() {
        return objects == null ? 0 : objects.size();
    }

    protected void onClickItem(View view, int id, int position) {
        if (StringUtils.isNotEmpty(action)) {
            Intent intent = new Intent(action);
            intent.putExtra(PARAM_ID, id);
            intent.putExtra(PARAM_POSITION, position);
            LocalBroadcastManager.getInstance(view.getContext()).sendBroadcast(intent);
        }
    }

    public void setObjects(List<T> objects) {
        int formerItemCount = getItemCount();
        this.objects = objects;
        notifyItemRangeChanged(0, formerItemCount > getItemCount() ? formerItemCount : getItemCount());
    }

    public void addNewItem(T object) {
        this.objects.add(object);
        notifyItemInserted(this.objects.size());
    }

    public void addNewItems(List<T> objects) {
        int positionStart = this.objects.size();
        this.objects.addAll(objects);
        notifyItemRangeInserted(positionStart, this.objects.size());
    }

    public void modifyItem(int position, T object) {
        this.objects.set(position, object);
        notifyItemChanged(position);
    }

    public void removeItem(int position) {
        this.objects.remove(position);
        notifyItemRemoved(position);
        notifyItemRangeChanged(0, getItemCount());
    }

    public void removeAll() {
        int formerItemCount = getItemCount();
        this.objects.removeAll(this.objects);
        notifyItemRangeRemoved(0, formerItemCount);
    }

    public List<T> getObjects() {
        return objects;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getAction() {
        return action;
    }

    protected int getObjectId(T object) {
        try {
            if (object == null) {
                return 0;
            }
            Method method = object.getClass().getMethod("getId");
            Object idObject = method.invoke(object);
            if (idObject instanceof Integer) {
                return (int) idObject;
            }
            return 0;
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
            return 0;
        }
    }

    protected int getIndexOf(T object) {
        return GenericUtils.getIndexOf(object, this.objects);
    }
}
