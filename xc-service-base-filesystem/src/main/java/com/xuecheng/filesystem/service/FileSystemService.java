package com.xuecheng.filesystem.service;

import com.alibaba.fastjson.JSON;
import com.xuecheng.filesystem.dao.FileSystemRepository;
import com.xuecheng.framework.domain.filesystem.FileSystem;
import com.xuecheng.framework.domain.filesystem.response.FileSystemCode;
import com.xuecheng.framework.domain.filesystem.response.UploadFileResult;
import com.xuecheng.framework.exception.ExceptionCast;
import com.xuecheng.framework.model.response.CommonCode;
import org.apache.commons.lang3.StringUtils;
import org.csource.fastdfs.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.util.Map;

@Service
public class FileSystemService {

    @Autowired
    private FileSystemRepository fileSystemRepository;
    @Value("${xuecheng.fastdfs.tracker_servers}")
    private String tracker_servers;
    @Value("${xuecheng.fastdfs.connect_timeout_in_seconds}")
    private int connect_timeout_in_seconds;
    @Value("${xuecheng.fastdfs.network_timeout_in_seconds}")
    private int network_timeout_in_seconds;
    @Value("${xuecheng.fastdfs.charset}")
    private String charset;
    //上传文件
    public UploadFileResult upload(MultipartFile multipartFile,
                                   String filetag,
                                   String businesskey,
                                   String metadata) {
        //将文件上传到fastDFS中，得到一个文件id
        String fileId = upload_file(multipartFile);
        if (StringUtils.isEmpty(fileId)) {
            ExceptionCast.cast(FileSystemCode.FS_UPLOADFILE_SERVERFAIL);
        }
        //将文件id以及文件信息存储到MongoDB中
        FileSystem fileSystem = new FileSystem();
        fileSystem.setFileId(fileId);
        fileSystem.setFilePath(fileId);
        fileSystem.setFileSize(multipartFile.getSize());
        fileSystem.setFileType(multipartFile.getContentType());
        fileSystem.setFileName(multipartFile.getOriginalFilename());
        fileSystem.setFiletag(filetag);
        fileSystem.setBusinesskey(businesskey);
        if (StringUtils.isNotEmpty(metadata)) {
            Map map = JSON.parseObject(metadata, Map.class);
            fileSystem.setMetadata(map);
        }
        fileSystemRepository.save(fileSystem);
        return new UploadFileResult(CommonCode.SUCCESS, fileSystem);
    }

    /**
     * 上传文件到fastDFS
     * @param multipartFile 文件对象
     * @return 文件id
     */
    public String upload_file(MultipartFile multipartFile) {
        //初始化fastDFS服务器
        initFastDFSConfig();
        //创建trackerClient
        TrackerClient trackerClient = new TrackerClient();
        try {
            TrackerServer trackerServer = trackerClient.getConnection();
            //得到StorageServer服务器
            StorageServer storageServer = trackerClient.getStoreStorage(trackerServer);
            StorageClient1 storageClient1 = new StorageClient1(trackerServer, storageServer);
            //上传文件
            //得到文件的原始名称
            String originalFilename = multipartFile.getOriginalFilename();
            //得到文件的扩展名
            String ext = originalFilename.substring(originalFilename.lastIndexOf(".") + 1);
            //得到文件字节
            byte[] bytes = multipartFile.getBytes();
            //上传文件成功后返回的文件id
            String fileId = storageClient1.upload_file1(bytes, ext, null);
            return fileId;
        } catch (Exception e) {
            e.printStackTrace();
        }
        //上传失败则返回null
        return null;
    }

    /**
     * 初始化fastDFS的环境
     */
    private void initFastDFSConfig() {
        try {
            //初始化fastDFS的trackerServer
            ClientGlobal.initByTrackers(tracker_servers);
            ClientGlobal.setG_charset(charset);
            ClientGlobal.setG_connect_timeout(connect_timeout_in_seconds);
            ClientGlobal.setG_network_timeout(network_timeout_in_seconds);
        } catch (Exception e) {
            e.printStackTrace();
            //若产生异常则抛出fastDFS初始化异常
            ExceptionCast.cast(FileSystemCode.FS_DELETEFILE_DBFAIL);
        }

    }
}
