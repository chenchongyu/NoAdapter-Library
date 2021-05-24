/*
 * Copyright 2021 ccy.All Rights Reserved
 */
package com.runningcode.noadapter.adpater.adpater;

import java.util.ArrayList;
import java.util.List;

import com.runningcode.noadapter.annotation.IVHRegistry;

import android.text.TextUtils;
import android.util.Log;

public class ViewHolderRegistry {
    private static final String TAG = ViewHolderRegistry.class.getSimpleName();
    private static List<IVHRegistry> list = new ArrayList<>();

    static {
//        add("com.baidu.adu.noadapter.compiler.ViewHolderRegistry$module");
//        add("com.baidu.adu.noadapter.compiler.ViewHolderRegistry$app");
//        add("com.baidu.adu.noadapter.compiler.ViewHolderRegistry$test");
    }

    public static void add(String className) {
        Log.i(TAG, "registe by plugin");
        if (!TextUtils.isEmpty(className)) {
            try {
                Class<?> clazz = Class.forName(className);
                Object obj = clazz.getConstructor().newInstance();
                if (obj instanceof IVHRegistry) {
                    add((IVHRegistry) obj);
                } else {
                    Log.i(TAG, "register failed, class name: " + className
                            + " should implements IVHRegistry");
                }
            } catch (Exception e) {
                Log.e(TAG, "register class error:" + className + " --> " + e.getMessage());
            }
        }
    }

    public static void add(IVHRegistry ivhRegistry) {
        list.add(ivhRegistry);
    }

    public static int getItemViewType(Object data) {
        for (IVHRegistry ivhRegistry : list) {
            int itemViewType = ivhRegistry.getItemViewType(data);
            if (itemViewType > 0) {
                return itemViewType;
            }
        }

        System.err.println("ViewHolderRegistry find type value by " + data.getClass() + " is "
                + "error.");
        return 0;
    }

    public static Class getVHClass(int type) {
        for (IVHRegistry ivhRegistry : list) {
            Class vhClass = ivhRegistry.getVHClass(type);
            if (vhClass != null) {
                return vhClass;
            }
        }

        return null;
    }
}
