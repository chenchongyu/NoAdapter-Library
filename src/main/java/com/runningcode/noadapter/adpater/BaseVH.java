/*
 * Copyright (C) 2021 Baidu, Inc. All Rights Reserved.
 */
package com.runningcode.noadapter.adpater;

import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;

public abstract class BaseVH<T> extends RecyclerView.ViewHolder {
    private OnItemClickListener listener;

    public BaseVH(@NonNull ViewGroup parent, int layoutId) {
        super(LayoutInflater.from(parent.getContext()).inflate(layoutId, parent, false));
    }

    public OnItemClickListener getListener() {
        return listener;
    }

    public void setListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public abstract void bindData(T data);
}
