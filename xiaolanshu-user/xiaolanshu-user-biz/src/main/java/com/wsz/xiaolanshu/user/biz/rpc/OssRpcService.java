package com.wsz.xiaolanshu.user.biz.rpc;

import com.wsz.framework.common.response.Response;
import com.wsz.xiaolanshu.oss.api.FileFeignApi;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

/**
 * Description
 *
 * @Author wangshaozhe
 * @Date 2025-12-08 22:34
 * @Company:
 */
@Component
public class OssRpcService {

    @Resource
    private FileFeignApi fileFeignApi;

    public String uploadFile(MultipartFile file) {
        // 调用对象存储服务上传文件
        Response<?> response = fileFeignApi.uploadFile(file);

        if (!response.isSuccess()) {
            return null;
        }

        // 返回图片访问链接
        return (String) response.getData();
    }
}
