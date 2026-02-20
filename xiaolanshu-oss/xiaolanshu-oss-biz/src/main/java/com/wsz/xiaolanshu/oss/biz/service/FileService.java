package com.wsz.xiaolanshu.oss.biz.service;

import com.wsz.framework.common.response.Response;
import org.springframework.web.multipart.MultipartFile;

/**
 * Description
 *
 * @Author wangshaozhe
 * @Date 2025-12-08 18:59
 * @Company:
 */
public interface FileService {

    /**
     * 上传文件
     *
     * @param file
     * @return
     */
    Response<?> uploadFile(MultipartFile file);
}
