package com.wsz.xiaolanshu.oss.biz.service.impl;

import com.wsz.framework.common.response.Response;
import com.wsz.xiaolanshu.oss.biz.service.FileService;
import com.wsz.xiaolanshu.oss.biz.strategy.FileStrategy;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

/**
 * Description
 *
 * @Author wangshaozhe
 * @Date 2025-12-08 18:59
 * @Company:
 */
@Service
@Slf4j
public class FileServiceImpl implements FileService {

    @Resource
    private FileStrategy fileStrategy;

    @Override
    public Response<?> uploadFile(MultipartFile file) {

        // 上传文件到minio/oss
        String url = fileStrategy.uploadFile(file);

        return Response.success(url);
    }
}
