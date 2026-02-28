package com.wsz.xiaolanshu.note.biz.service.impl;

import com.wsz.framework.common.exception.BizException;
import com.wsz.framework.common.response.Response;
import com.wsz.xiaolanshu.note.biz.domain.dataobject.CoverTemplateDO;
import com.wsz.xiaolanshu.note.biz.enums.ResponseCodeEnum;
import com.wsz.xiaolanshu.note.biz.mapper.CoverTemplateDOMapper;
import com.wsz.xiaolanshu.note.biz.service.CoverGeneratorService;
import com.wsz.xiaolanshu.oss.api.FileFeignApi;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 笔记封面生成实现类
 */
@Service
@Slf4j
public class CoverGeneratorServiceImpl implements CoverGeneratorService {

    @Resource
    private CoverTemplateDOMapper coverTemplateDOMapper;

    @Resource
    private FileFeignApi fileFeignApi;

    @Override
    public List<CoverTemplateDO> getTemplateList() {
        return coverTemplateDOMapper.selectAllEnabled();
    }

    @Override
    public Response<String> generateAndUpload(Long templateId, String title) {

        // 参数校验：title 不能为空
        if (StringUtils.isBlank(title)) {
            return Response.fail("标题不能为空");
        }

        CoverTemplateDO template = coverTemplateDOMapper.selectById(templateId);
        if (template == null) throw new BizException(ResponseCodeEnum.THE_TEMPLATE_DOES_NOT_EXIST);

        try {
            // 1. 下载并读取底图
            BufferedImage image = ImageIO.read(new URL(template.getImgUrl()));
            Graphics2D g2d = image.createGraphics();

            // 2. 开启高质量渲染
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            // 3. 准备时间数据
            LocalDateTime now = LocalDateTime.now();

            // 自定义星期缩写逻辑：mon, tues, wedn, thur, fri, sat, sun
            String weekStr = switch (now.getDayOfWeek()) {
                case MONDAY -> "Mon";
                case TUESDAY -> "Tue";
                case WEDNESDAY -> "Wed";
                case THURSDAY -> "Thu";
                case FRIDAY -> "Fri";
                case SATURDAY -> "Sat";
                case SUNDAY -> "Sun";
            };

            // 日期 (例如: 02/28)
            String dateStr = String.format("%02d/%02d", now.getMonthValue(), now.getDayOfMonth());
            Color fontColor = Color.decode(template.getFontColor());
            g2d.setColor(fontColor);

            // 4. 绘制星期 (左上角)
            g2d.setFont(new Font("Arial", Font.PLAIN, 40));
            g2d.drawString(weekStr, template.getWeekX(), template.getWeekY());

            // 5. 绘制日期 (右上角 - 动态计算宽度实现右对齐)
            FontMetrics timeMetrics = g2d.getFontMetrics();
            int dateX = (template.getDateX() == -1)
                    ? image.getWidth() - timeMetrics.stringWidth(dateStr) - 70
                    : template.getDateX();
            g2d.drawString(dateStr, dateX, template.getDateY());

            // 6. 绘制标题 (正中央 - 自动换行)
            // 注意：Linux服务器需安装字体或在 resources 下放置字体文件并加载
            Font titleFont = new Font("SimHei", Font.BOLD, template.getFontSize());
            g2d.setFont(titleFont);
            drawWrappedText(g2d, title, image.getWidth(), template.getTitleY(), image.getWidth() - 200);

            g2d.dispose();

            // 7. 将 BufferedImage 转为 MultipartFile 并上传
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            ImageIO.write(image, "jpg", os);

            // 使用下方定义的内部类进行包装
            MultipartFile file = new CommonMultipartFile(
                    "file", "cover_" + System.currentTimeMillis() + ".jpg", "image/jpeg", os.toByteArray());

            Response<?> uploadRes = fileFeignApi.uploadFile(file);
            if (uploadRes == null || !uploadRes.isSuccess()) {
                throw new BizException(ResponseCodeEnum.UPLOAD_IMAGE_ERROR);
            }
            return Response.success((String) uploadRes.getData());

        } catch (Exception e) {
            log.error("封面合成异常: ", e);
            throw new BizException(ResponseCodeEnum.THE_SYSTEM_IS_BUSY_AND_THE_COVER_GENERATION_FAILED);
        }
    }

    private void drawWrappedText(Graphics2D g2d, String text, int imgWidth, int startY, int maxWidth) {
        FontMetrics fm = g2d.getFontMetrics();
        int lineHeight = fm.getHeight();
        int curY = startY;

        StringBuilder line = new StringBuilder();
        for (char c : text.toCharArray()) {
            if (fm.stringWidth(line.toString() + c) <= maxWidth) {
                line.append(c);
            } else {
                int x = (imgWidth - fm.stringWidth(line.toString())) / 2;
                g2d.drawString(line.toString(), x, curY);
                line = new StringBuilder(String.valueOf(c));
                curY += lineHeight;
            }
        }
        if (line.length() > 0) {
            int x = (imgWidth - fm.stringWidth(line.toString())) / 2;
            g2d.drawString(line.toString(), x, curY);
        }
    }

    /**
     * 内部类：简单的 MultipartFile 实现，用于字节流上传
     */
    private static class CommonMultipartFile implements MultipartFile {
        private final String name;
        private final String originalFilename;
        private final String contentType;
        private final byte[] content;

        public CommonMultipartFile(String name, String originalFilename, String contentType, byte[] content) {
            this.name = name;
            this.originalFilename = originalFilename;
            this.contentType = contentType;
            this.content = content;
        }

        @Override
        public String getName() { return name; }
        @Override
        public String getOriginalFilename() { return originalFilename; }
        @Override
        public String getContentType() { return contentType; }
        @Override
        public boolean isEmpty() { return content == null || content.length == 0; }
        @Override
        public long getSize() { return content.length; }
        @Override
        public byte[] getBytes() throws IOException { return content; }
        @Override
        public InputStream getInputStream() throws IOException { return new ByteArrayInputStream(content); }
        @Override
        public void transferTo(File dest) throws IOException, IllegalStateException {
            try (FileOutputStream fos = new FileOutputStream(dest)) { fos.write(content); }
        }
    }
}