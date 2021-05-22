/*
 * Copyright 2021 ccy.All Rights Reserved
 */
package com.runningcode.noadapter.annotation;

public interface IVHRegistry {
    int getItemViewType(Object data);

    Class getVHClass(int type);
}
