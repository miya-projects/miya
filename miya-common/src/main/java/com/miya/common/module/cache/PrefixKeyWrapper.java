package com.miya.common.module.cache;

/**
 * key的一个前缀包装器，简单将前缀和key连接起来
 */
public interface PrefixKeyWrapper {

    /**
     * 返回前缀
     * @return
     */
    String prefix();

    /**
     * 返回真正的key
     * @param originKey    原本的key
     * @return 真正使用到的key
     */
    default String toKey(String originKey){
        return this.prefix() + originKey;
    }
}
