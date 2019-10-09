package com.xuecheng.manage_media.test;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@SpringBootTest
@RunWith(SpringRunner.class)
public class TestFile {

    //测试文件分块
    @Test
    public void testChunk() throws IOException {
        //源文件
        File sourceFile = new File("E:/ffmpeg_test/lucene.avi");
        //块文件目录
        String chunkFileFolder = "E:/ffmpeg_test/chunks/";
        //块文件大小
        long chunkFileSize = 1 *1024 * 1024;
        //块文件个数
        long chunkFileNum = (long) Math.ceil(sourceFile.length() * 1.0 / chunkFileSize);
        //创建读文件的对象
        RandomAccessFile raf_read = new RandomAccessFile(sourceFile, "r");

        byte[] arr = new byte[1024];
        int len;

        for (int i = 0; i < chunkFileNum; i++) {
            //创建一个块文件
            File chunkFile = new File(chunkFileFolder + i);
            //创建一个写文件的对象
            RandomAccessFile raf_write = new RandomAccessFile(chunkFile, "rw");
            while ((len = raf_read.read(arr)) != -1) {
                raf_write.write(arr,0,len);
                if (chunkFile.length() >= chunkFileSize) {
                    break;
                }
            }
            raf_write.close();
        }
        raf_read.close();
    }

    @Test
    //测试文件合并
    public void testMergeFile() throws IOException {
        //块文件目录
        String chunkFileFolderPath = "E:/ffmpeg_test/chunks/";
        File  chunkFileFolder = new File(chunkFileFolderPath);

        File[] files = chunkFileFolder.listFiles();
        List<File> fileList = Arrays.asList(files);

        Collections.sort(fileList, (file1, file2) -> {
            if (Integer.parseInt(file1.getName()) > Integer.parseInt(file2.getName())) {
                return 1;
            }
            return -1;
        });

        File mergeFile = new File("E:/ffmpeg_test/merge_lucene.avi");
        mergeFile.createNewFile();

        byte[] arr = new byte[1024];
        int len;
        RandomAccessFile raf_write = new RandomAccessFile(mergeFile, "rw");

        for (File chunkFile : fileList) {
            RandomAccessFile raf_read = new RandomAccessFile(chunkFile,"r");
            while ((len = raf_read.read(arr)) != -1) {
                raf_write.write(arr, 0, len);
            }
            raf_read.close();
        }
        raf_write.close();
    }
}
