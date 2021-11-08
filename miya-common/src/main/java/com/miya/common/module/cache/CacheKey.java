package com.miya.common.module.cache;

import lombok.AllArgsConstructor;

/**
 * 用于redis或其他kv存储的地方用到的key
 * 目的是为各种不同用处的key做不同的包装，使得不会重复，也集中在一个地方存储，方便查找，也做为类型区别，避免直接使用String
 * 比如，存储用户信息可能是 user:id, 存储订单信息可能是order:id
 */
@AllArgsConstructor(staticName = "of")
public class CacheKey {

    /**
     * key的包装器
     */
    private final PrefixKeyWrapper prefixKeyWrapper;
    /**
     * key的具象内容
     */
    private final String keyContent;

    /**
     * 返回真正存储使用的key
     */
    public String toKey(){
        return this.prefixKeyWrapper.toKey(this.keyContent);
    }
}
