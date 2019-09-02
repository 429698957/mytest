package com.pinyougou.shop.test;

import com.pinyougou.common.util.FastDFSClient;
import org.csource.fastdfs.ClientGlobal;
import org.csource.fastdfs.StorageClient;
import org.csource.fastdfs.TrackerClient;
import org.csource.fastdfs.TrackerServer;
import org.junit.Test;


public class test {
     //上传
    @Test
    public void upload() throws Exception{
    //1.创建一个配置文件，用来存储服务器的ip和端口
        //2.加载配置文件
        ClientGlobal.init("D:\\ideaEncoding\\pinyougou\\代码\\60pinyougou\\pinyougou-shop-web\\src\\main\\resources\\fdfs_client.conf");

        //3.创建trackerclient
        TrackerClient trackerClient = new TrackerClient();
        //4.获取trackerserver
        TrackerServer connection = trackerClient.getConnection();
        //5.创建storageclient
        StorageClient storageClient = new StorageClient(connection,null);
        //6.上传图片
        String[] jpgs = storageClient.upload_file("C:\\Users\\42969\\Pictures\\timg.jpg", "jpg", null);
        for (String jpg : jpgs) {
            System.out.println(jpg);
        }

    }

    @Test
    public void fastdfsclient() throws Exception{
        FastDFSClient fastDFSClient = new FastDFSClient("D:\\ideaEncoding\\pinyougou\\代码\\60pinyougou\\pinyougou-shop-web\\src\\main\\resources\\fdfs_client.conf");
        String s = fastDFSClient.uploadFile("D:\\abc.jpg", "jpg");
        System.out.println(s);
    }
}
