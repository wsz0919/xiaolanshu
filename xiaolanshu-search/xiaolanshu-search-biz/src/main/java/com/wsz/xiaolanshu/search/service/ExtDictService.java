package com.wsz.xiaolanshu.search.service;

import org.springframework.http.ResponseEntity;

/**
 * Description
 *
 * @Author wangshaozhe
 * @Date 2026-02-04 15:43
 * @Company:
 */
public interface ExtDictService {

    /**
     * 获取热更新词典
     * @return
     */
    ResponseEntity<String> getHotUpdateExtDict();
}
