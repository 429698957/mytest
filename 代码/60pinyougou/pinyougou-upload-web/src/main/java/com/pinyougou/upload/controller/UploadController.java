package com.pinyougou.upload.controller;

import com.pinyougou.common.util.FastDFSClient;
import entity.Result;
import net.sf.jsqlparser.schema.MultiPartName;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/upload")
public class UploadController {

    @RequestMapping("/uploadFile")
    @CrossOrigin(origins = {"http://localhost:9102","http://localhost:9101"},allowCredentials = "true")
    public Result uploadFile(MultipartFile file) {

        try {

            //1.获取源文件的扩展名                                                               +1的意思是截取源文件.jpg的后缀名 不要.
             String extName = file.getOriginalFilename().substring(file.getOriginalFilename().lastIndexOf(".")+1);

            //2.获取源文件的字节数组
            byte[] bytes=file.getBytes();

            //3.调用fastdfsclient的api上传图片
            FastDFSClient fastDFSClient = new FastDFSClient("classpath:fdfs_client.conf");
            String path = fastDFSClient.uploadFile(bytes,extName);
            String realPath = "http://192.168.25.133/"+path;
            //4.返回result 带图片的路径
            return new Result(true,realPath);
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false,"上传失败");
        }
    }
}
