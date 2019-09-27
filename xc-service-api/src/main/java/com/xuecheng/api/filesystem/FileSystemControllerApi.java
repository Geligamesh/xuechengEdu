package com.xuecheng.api.filesystem;

import com.xuecheng.framework.domain.filesystem.response.UploadFileResult;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.multipart.MultipartFile;

@Api(value = "文件管理接口",description = "文件的管理接口，管理文件的上传、下载")
public interface FileSystemControllerApi {

    //上传文件
    @ApiOperation("上传文件")
    UploadFileResult upload(MultipartFile multipartFile,String filetag,String businesskey,String metadata);
}
