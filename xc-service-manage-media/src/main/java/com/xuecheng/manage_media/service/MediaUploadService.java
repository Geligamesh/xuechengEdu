package com.xuecheng.manage_media.service;

import com.alibaba.fastjson.JSON;
import com.xuecheng.framework.domain.media.MediaFile;
import com.xuecheng.framework.domain.media.response.CheckChunkResult;
import com.xuecheng.framework.domain.media.response.MediaCode;
import com.xuecheng.framework.exception.ExceptionCast;
import com.xuecheng.framework.model.response.CommonCode;
import com.xuecheng.framework.model.response.ResponseResult;
import com.xuecheng.manage_media.config.RabbitMQConfig;
import com.xuecheng.manage_media.dao.MediaFileRepository;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.IOUtils;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.*;
import java.util.*;

@Service
public class MediaUploadService {

    @Autowired
    private MediaFileRepository mediaFileRepository;

    @Value("${xc-service-manage-media.upload-location}")
    private String upload_location;

    @Value("${xc-service-manage-media.mq.routingkey-media-video}")
    private String routingkey_media_video;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    //得到文件所属目录的路径
    private String getFileFolderPath(String fileMd5) {
        return upload_location + fileMd5.substring(0, 1) + "/" + fileMd5.substring(1, 2) + "/" + fileMd5 + "/";
    }

    private String getFilePath(String fileMd5,String fileExt) {
        return upload_location + fileMd5.substring(0, 1) + "/" + fileMd5.substring(1, 2) + "/" + fileMd5 + "/" + fileMd5 + "." + fileExt;
    }

    private String getChunkFileFolder(String fileMd5) {
        return upload_location + fileMd5.substring(0, 1) + "/" + fileMd5.substring(1, 2) + "/" + fileMd5 + "/chunk/";
    }

    /**
     * 文件上传前的注册，检查文件是否存在
     * 根据文件md5得到文件路径
     * 规则：
     * 一级目录：md5的第一个字符
     * 二级目录：md5的第二个字符
     * 三级目录：md5
     * 文件名：md5+文件扩展名
     * @param fileMd5 文件md5值
     * @param fileExt 文件扩展名
     * @return 文件路径
     */
    //文件上传前的注册
    public ResponseResult register(String fileMd5, String fileName, Long fileSize, String mimetype, String fileExt) {
        //检查文件在磁盘上是否存在
        //文件所属目录的路径
        String fileFolderPath = this.getFileFolderPath(fileMd5);
        //文件的路径
        String filePath = this.getFilePath(fileMd5, fileExt);
        //文件是否存在
        File file = new File(filePath);
        boolean exists = file.exists();
        //检查文件信息再mongodb中是否存在
        Optional<MediaFile> optional = mediaFileRepository.findById(fileMd5);
        if (exists && optional.isPresent()) {
            ExceptionCast.cast(MediaCode.UPLOAD_FILE_REGISTER_FAIL);
        }
        //如果文件不存在是做一些准备工作，检查文件所在目录是否存在，如果不存在则创建
        File fileFolder = new File(fileFolderPath);
        if (!fileFolder.exists()) {
            fileFolder.mkdirs();
        }
        return new ResponseResult(CommonCode.SUCCESS);
    }

    /**
     *
     * @param fileMd5 文件MD5
     * @param chunk 块的下标
     * @param chunkSize 块的大小
     * @return
     */
    //分块检查
    public CheckChunkResult checkChunk(String fileMd5, Integer chunk, Integer chunkSize) {
        //检查分块文件是否存在
        //得到分块文件所属的文件目录
        String chunkFileFolder = this.getChunkFileFolder(fileMd5);
        File chunkFile = new File(chunkFileFolder + chunk);
        //文件存在
        if (chunkFile.exists()) {
            return new CheckChunkResult(CommonCode.SUCCESS,true);
        }
        return new CheckChunkResult(CommonCode.SUCCESS, false);
    }

    //分块上传
    public ResponseResult uploadChunk(MultipartFile file, String fileMd5, Integer chunk) {
        //检查分块目录是否存在,如果不存在则自动创建
        String chunkFileFolderPath = this.getChunkFileFolder(fileMd5);
        String chunkFilePath = chunkFileFolderPath + chunk;
        //得到分块文件的目录
        File chunkFileFolder = new File(chunkFileFolderPath);
        //分块文件目录是否存在
        if (!chunkFileFolder.exists()) {
            chunkFileFolder.mkdirs();
        }
        //得到分块文件的输入流
        InputStream inputStream = null;
        OutputStream outputStream = null;
        try {
            inputStream = file.getInputStream();
            outputStream = new FileOutputStream(new File(chunkFilePath));
            IOUtils.copy(inputStream, outputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            try {
                outputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                inputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return new ResponseResult(CommonCode.SUCCESS);
    }

    //合并文件
    public ResponseResult mergeChunks(String fileMd5, String fileName, Long fileSize, String mimetype, String fileExt) {
        //合并所有的文件
        //得到分块分块文件的所属目录
        String chunkFileFolderPath = this.getChunkFileFolder(fileMd5);
        File chunkFileFolder = new File(chunkFileFolderPath);
        //分块文件列表
        File[] files = chunkFileFolder.listFiles();
        List<File> fileList = Arrays.asList(files);
        //创建一个合并文件
        String filePath = this.getFilePath(fileMd5, fileExt);
        File mergeFile = new File(filePath);
        //执行合并文件
        mergeFile = this.mergeFile(fileList, mergeFile);
        //文件为空则返回异常
        if (mergeFile == null) {
            ExceptionCast.cast(MediaCode.MERGE_FILE_FAIL);
        }
        //校验合并文件MD5与前端传来的文件MD5参数是否一致
        boolean checkFileMd5 = this.checkFileMd5(mergeFile, fileMd5);
        if (!checkFileMd5) {
            ExceptionCast.cast(MediaCode.MERGE_FILE_CHECKFAIL);
        }
        //校验成功则将文件数据保存到MongoDB中
        MediaFile mediaFile = new MediaFile();
        mediaFile.setFileId(fileMd5);
        mediaFile.setFileOriginalName(fileName);
        mediaFile.setFileName(fileMd5 + "." +fileExt);
        //文件路径保存相对路径
        String filePath1 = fileMd5.substring(0,1) + "/" + fileMd5.substring(1,2) + "/" + fileMd5 + "/";
        mediaFile.setFilePath(filePath1);
        mediaFile.setFileSize(fileSize);
        mediaFile.setUploadTime(new Date());
        mediaFile.setMimeType(mimetype);
        mediaFile.setFileType(fileExt);
        //状态为上传成功
        mediaFile.setFileStatus("301002");
        mediaFileRepository.save(mediaFile);
        //向rabbitMQ发送视频处理消息
        sendProcessVideoMsg(mediaFile.getFileId());

        return new ResponseResult(CommonCode.SUCCESS);
    }

    //向MQ发送视频处理消息
    private ResponseResult sendProcessVideoMsg(String mediaId) {
        //检查数据库中是否有数据
        Optional<MediaFile> optional = mediaFileRepository.findById(mediaId);
        if (!optional.isPresent()) {
            ExceptionCast.cast(CommonCode.FAIL);
        }
        //构建消息内容
        Map<String,String> map = new HashMap<>();
        map.put("mediaId", mediaId);
        String jsonString = JSON.toJSONString(map);
        //向MQ发送消息
        try {
            rabbitTemplate.convertAndSend(RabbitMQConfig.EX_MEDIA_PROCESSTASK, routingkey_media_video, jsonString);
        } catch (AmqpException e) {
            e.printStackTrace();
            return new ResponseResult(CommonCode.FAIL);
        }
        return new ResponseResult(CommonCode.SUCCESS);
    }

    //检验文件
    private boolean checkFileMd5(File mergeFile, String fileMd5) {
        try {
            //得到文件的输入流
            InputStream inputStream = new FileInputStream(mergeFile);
            //得到文件的MD5值
            String md5Hex = DigestUtils.md5Hex(inputStream);
            //校验合并文件MD5与前端传来的文件MD5参数是否一致
            if (fileMd5.equalsIgnoreCase(md5Hex)) {
                return true;
            }
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return false;
    }

    private File mergeFile(List<File> fileList,File mergeFile) {
        //如果合并文件已经存在则删除,不存在则新建文件
        try {
            if (mergeFile.mkdirs()) {
                mergeFile.delete();
            }else {
                mergeFile.createNewFile();
            }
            //对文件的顺序进行升序排序
            Collections.sort(fileList, (file1, file2) -> {
                if (Integer.parseInt(file1.getName()) > Integer.parseInt(file2.getName())) {
                    return 1;
                }
                return -1;
            });
            //创建一个写文件
            RandomAccessFile raf_write = new RandomAccessFile(mergeFile, "rw");
            byte[] arr = new byte[1024];
            int len;
            for (File file : fileList) {
                //为每一个块文件创建一个读文件
                RandomAccessFile raf_read = new RandomAccessFile(file, "r");
                while ((len = raf_read.read(arr)) != -1) {
                    raf_write.write(arr, 0, len);
                }
                raf_read.close();
            }
            raf_write.close();
        }catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        //返回合并后的文件
        return mergeFile;
    }
}
