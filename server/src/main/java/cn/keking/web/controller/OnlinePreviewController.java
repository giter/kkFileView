package cn.keking.web.controller;

import cn.keking.model.FileAttribute;
import cn.keking.model.FileType;
import cn.keking.model.ReturnResponse;
import cn.keking.service.FilePreview;
import cn.keking.service.FilePreviewFactory;

import cn.keking.service.cache.CacheService;
import cn.keking.service.impl.OtherFilePreviewImpl;
import cn.keking.service.FileHandlerService;
import cn.keking.utils.DownloadUtils;
import cn.keking.utils.WebUtils;
import fr.opensagres.xdocreport.core.io.IOUtils;
import io.mola.galimatias.GalimatiasParseException;
import jodd.io.NetUtil;
import org.apache.commons.codec.binary.Base64;
import org.apache.tika.Tika;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

import static cn.keking.service.FilePreview.*;

/**
 * @author yudian-it
 */
@Controller
public class OnlinePreviewController {

    public static final String BASE64_DECODE_ERROR_MSG = "Base64解码失败，请检查你的 %s 是否采用 Base64 + urlEncode 双重编码了！";
    private final Logger logger = LoggerFactory.getLogger(OnlinePreviewController.class);

    private final FilePreviewFactory previewFactory;
    private final CacheService cacheService;
    private final FileHandlerService fileHandlerService;
    private final OtherFilePreviewImpl otherFilePreview;

    public OnlinePreviewController(FilePreviewFactory filePreviewFactory, FileHandlerService fileHandlerService, CacheService cacheService, OtherFilePreviewImpl otherFilePreview) {
        this.previewFactory = filePreviewFactory;
        this.fileHandlerService = fileHandlerService;
        this.cacheService = cacheService;
        this.otherFilePreview = otherFilePreview;
    }

    @RequestMapping(value = "/onlinePreview")
    public String onlinePreview(String url, Model model, HttpServletRequest req) throws IOException {
        String fileUrl;
        try {
            fileUrl = new String(Base64.decodeBase64(url), StandardCharsets.UTF_8);
        } catch (Exception ex) {
            String errorMsg = String.format(BASE64_DECODE_ERROR_MSG, "url");
            return otherFilePreview.notSupportedFile(model, errorMsg);
        }


        FileAttribute fileAttribute = fileHandlerService.getFileAttribute(fileUrl, req);
        model.addAttribute("file", fileAttribute);

        FilePreview filePreview = null;

        if(fileAttribute.getUrl().startsWith("http")){

            ReturnResponse<String> response = DownloadUtils.downLoad(fileAttribute, fileAttribute.getName());

            if (response.getCode() == 0) {

                Tika tika = new Tika();
                String type = tika.detect(new File(response.getContent()));

                String previewType = null;

                if ("application/pdf".equals(type)) {

                    filePreview = previewFactory.get(FileType.PDF);
                    previewType = "PDF";
                    fileAttribute.setOfficePreviewType(PDF_FILE_PREVIEW_PAGE);

                } else if (type.contains("excel") ||
                        type.contains("powerpoint") ||
                        type.contains("openxmlformats") ||
                        type.contains("msword") ||
                        type.contains("ms-word")
                ) {
                    filePreview = previewFactory.get(FileType.OFFICE);
                    previewType = "OFFICE";
                    fileAttribute.setOfficePreviewType(OFFICE_PICTURE_FILE_PREVIEW_PAGE);
                }

                if (filePreview != null) {

                    logger.info("预览文件url1：{}，previewType：{}", fileUrl, previewType);
                    return filePreview.filePreviewHandle(fileUrl, model, fileAttribute);
                }
            }
        }


        filePreview = previewFactory.get(fileAttribute);
        logger.info("预览文件url2：{}，previewType：{}", fileUrl, fileAttribute.getType());

        return filePreview.filePreviewHandle(fileUrl, model, fileAttribute);
    }


    /**
     * 根据url获取文件内容
     * 当pdfjs读取存在跨域问题的文件时将通过此接口读取
     *
     * @param urlPath  url
     * @param response response
     */
    @RequestMapping(value = "/getCorsFile", method = RequestMethod.GET)
    public void getCorsFile(String urlPath, HttpServletResponse response) {
        logger.info("下载跨域pdf文件url：{}", urlPath);
        try {
            URL url = WebUtils.normalizedURL(urlPath);
            byte[] bytes = NetUtil.downloadBytes(url.toString());
            IOUtils.write(bytes, response.getOutputStream());
        } catch (IOException | GalimatiasParseException e) {
            logger.error("下载跨域pdf文件异常，url：{}", urlPath, e);
        }
    }

    /**
     * 通过api接口入队
     *
     * @param url 请编码后在入队
     */
    @RequestMapping("/addTask")
    @ResponseBody
    public String addQueueTask(String url) {
        logger.info("添加转码队列url：{}", url);
        cacheService.addQueueTask(url);
        return "success";
    }

}
