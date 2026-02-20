package com.wsz.xiaolanshu.distributed.id.generator.biz.core;

import com.wsz.xiaolanshu.distributed.id.generator.biz.core.common.Result;

public interface IDGen {
    Result get(String key);
    boolean init();
}
