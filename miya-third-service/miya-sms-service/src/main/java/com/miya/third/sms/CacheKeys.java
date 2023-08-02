package com.miya.third.sms;

import com.miya.common.module.cache.CacheKey;
import com.miya.common.module.cache.PrefixKeyWrapper;
import lombok.AllArgsConstructor;

/**
 * 存放所有类型的key
 */
@AllArgsConstructor
public enum CacheKeys implements PrefixKeyWrapper {

    /**
     * 手机验证码使用
     */
    PHONE_VERIFY("phone:");

    private final String prefix;

    @Override
    public String prefix() {
        return this.prefix;
    }

    public CacheKey toCacheKey(String keyContent){
        return CacheKey.of(this, keyContent);
    }

}
