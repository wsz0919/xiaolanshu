package com.wsz.xiaolanshu.note.biz.controller;

import com.wsz.framework.biz.operationlog.aspect.ApiOperationLog;
import com.wsz.framework.common.response.Response;
import com.wsz.xiaolanshu.note.biz.domain.dataobject.CoverTemplateDO;
import com.wsz.xiaolanshu.note.biz.domain.vo.GenerateCoverReqVO;
import com.wsz.xiaolanshu.note.biz.service.CoverGeneratorService;
import jakarta.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Description
 *
 * @Author wangshaozhe
 * @Date 2026-02-28 22:23
 * @Company:
 */
@RestController
@RequestMapping("/note/cover")
public class CoverController {

    @Resource
    private CoverGeneratorService coverGeneratorService;

    @GetMapping("/templates")
    @ApiOperationLog(description = "获取封面底图模板列表")
    public Response<List<CoverTemplateDO>> getTemplates() {
        return Response.success(coverGeneratorService.getTemplateList());
    }

    @PostMapping("/generate")
    @ApiOperationLog(description = "生成合成封面图")
    public Response<String> generateCover(@Validated @RequestBody GenerateCoverReqVO reqVO) {
        return coverGeneratorService.generateAndUpload(reqVO.getTemplateId(), reqVO.getTitle());
    }
}
