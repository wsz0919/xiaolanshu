package com.wsz.xiaolanshu.note.biz.service;

import com.wsz.framework.common.response.Response;
import com.wsz.xiaolanshu.note.biz.domain.dataobject.CoverTemplateDO;

import java.util.List;

/**
 * Description
 *
 * @Author wangshaozhe
 * @Date 2026-02-28 22:08
 * @Company:
 */
public interface CoverGeneratorService {

    Response<String> generateAndUpload(Long templateId, String title);

    List<CoverTemplateDO> getTemplateList();
}
