/*
 * Copyright 2021 ccy.All Rights Reserved
 */
package com.runningcode.noadapter.adpater.adpater;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import androidx.recyclerview.widget.RecyclerView;

/**
 * 列表 recycleView BaseAdapter
 */
public class NoAdapter
        extends RecyclerView.Adapter<BaseVH<?>> {
    private static final String TAG = "NoAdapter";
    protected List dataList;
    private OnItemClickListener listener;

    public NoAdapter(List dataList) {
        this.dataList = dataList;
    }

    public void setData(List dataList) {
        this.dataList = dataList;
    }

    public void setListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    @Override
    public BaseVH<?> onCreateViewHolder(ViewGroup parent, int viewType) {
        BaseVH<?> viewHolder = null;
        String err;

        try {
            Class<?> vhClass = ViewHolderRegistry.getVHClass(viewType);
            Log.d(TAG, "getItemViewType -->" + viewType + "：" + vhClass);
            err = " get vh class by " + viewType + " is null. please check ViewHolderRegistry.";
            if (vhClass == null) {
                throw new IllegalArgumentException("create viewHolder failed." + err);
            }

            Constructor<?> constructor = vhClass.getConstructor(ViewGroup.class);
            viewHolder = (BaseVH<?>) constructor.newInstance(parent);
        } catch (NoSuchMethodException e) {
            err = e.getLocalizedMessage();
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            err = e.getLocalizedMessage();
            e.printStackTrace();
        } catch (InstantiationException e) {
            err = e.getLocalizedMessage();
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            err = e.getLocalizedMessage();
            e.printStackTrace();
        }

        if (viewHolder == null) {
            throw new IllegalArgumentException("create viewHolder failed." + err);
        }

        if (listener != null) {
            viewHolder.setListener(listener);
        }
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(BaseVH holder, int position) {
        final Object data = dataList.get(position);

        holder.bindData(data);

        if (listener != null) {
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onItemClick(data);
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        if (dataList == null) {
            return 0;
        } else {
            return dataList.size();
        }
    }

    @Override
    public int getItemViewType(int position) {
        return ViewHolderRegistry.getItemViewType(dataList.get(position));
    }

}
