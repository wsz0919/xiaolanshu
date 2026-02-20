package com.wsz.xiaolanshu.search.controller;

import com.wsz.framework.biz.operationlog.aspect.ApiOperationLog;
import com.wsz.xiaolanshu.search.service.ExtDictService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Description
 *
 * @Author wangshaozhe
 * @Date 2026-02-04 15:44
 * @Company:
 */
@RestController
@RequestMapping("/search")
@Slf4j
public class ExtDictController {

    @Resource
    private ExtDictService extDictService;

    @GetMapping("/ext/dict")
    @ApiOperationLog(description = "热更新词典")
    public ResponseEntity<String> extDict() {
        return extDictService.getHotUpdateExtDict();
    }

}
