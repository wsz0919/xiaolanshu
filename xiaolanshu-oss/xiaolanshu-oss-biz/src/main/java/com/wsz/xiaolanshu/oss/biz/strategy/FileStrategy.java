package com.wsz.xiaolanshu.oss.biz.strategy;

import org.springframework.web.multipart.MultipartFile;

/**
 * Description
 *
 * @Author wangshaozhe
 * @Date 2025-12-08 18:54
 * @Company:
 */
public interface FileStrategy {

    /**
     * 文件上传
     *
     * @param file
     * @return
     */
    String uploadFile(MultipartFile file);
}
