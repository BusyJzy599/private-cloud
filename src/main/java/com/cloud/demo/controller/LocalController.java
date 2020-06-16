package com.cloud.demo.controller;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.cloud.demo.dto.ResultDTO;
import com.cloud.demo.entity.HdfsPath;
import com.cloud.demo.exception.ErrorCode;
import com.cloud.demo.service.utils.HadoopUtils;
import lombok.extern.slf4j.Slf4j;

import org.apache.commons.io.IOUtils;
import org.jodconverter.DocumentConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

@Controller
@Slf4j
public class LocalController {
    @Autowired
    private HadoopUtils hadoopUtils;
    @Value("${spring.servlet.multipart.location}")
    private String fileTempPath;
    @Value("${localFile-home}")
    private String localPath;
    @Autowired
    private DocumentConverter converter;

    public static String DEFAULT="/cloud/demo";
    private String currentPath=DEFAULT;

    /**
     * 获取信息
     * @param model
     * @param path
     * @return
     */
    @GetMapping("/local")
    public String local(Model model,
                        @RequestParam(value = "hdfs", required = false) String path){
        try {
            hadoopUtils.init();
        } catch (IOException e) {
            e.printStackTrace();
        }
        List<HdfsPath>files=null;

        if (path != null) {
            List<HdfsPath> pathList = hadoopUtils.getPaths(path);
            model.addAttribute("paths", pathList);
            files = hadoopUtils.getFilesList(path);
            currentPath=path;
        }else   {
            files = hadoopUtils.getFilesList(DEFAULT);
            currentPath=DEFAULT;
        }
        hadoopUtils.clearTmp();
        model.addAttribute("files", files);

        return "local";
    }

    /**
     * 删除单个文件
     * @param pathName
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "/local/delete", method = RequestMethod.POST)
    public Object delete(@RequestBody JSONObject pathName) {
        System.out.println(pathName.get("pathName"));
        return hadoopUtils.delete(pathName.get("pathName").toString())? ResultDTO.success():ResultDTO.error(ErrorCode.FAILED_DELETE);
    }

    /**
     * 删除多个文件
     * @param pathName
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "/local/deleteFiles", method = RequestMethod.POST)
    public Object deleteFiles(@RequestBody JSONObject pathName) {
        JSONArray pathName1 = pathName.getJSONArray("pathName");
        pathName1.forEach((v)->{
            hadoopUtils.delete(v.toString());
        });
        return true;
    }

    /**
     * 修改文件名
     * @param change
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "/local/change", method = RequestMethod.POST)
    public Object change(@RequestBody JSONObject change) {

        String oldName = change.get("oldName").toString();
        String newName = change.get("newName").toString();
        if(newName.trim().equals(""))
            return ResultDTO.error(ErrorCode.FAILED_CHANGE);
        return hadoopUtils.changeName(oldName,newName)? ResultDTO.success():ResultDTO.error(ErrorCode.ERROR_UNDEFINED);
    }

    /**
     * 本地上传
     * @param file
     * @return
     */
    @ResponseBody
    @PostMapping(value = "/local/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Object local(@RequestParam(name = "lefile") MultipartFile file) {
        if (file.isEmpty()) {
            return ResultDTO.error(ErrorCode.ERROR_UNDEFINED);
        }
        log.info("上传路径路径" + currentPath);
        String fileName = file.getOriginalFilename();
        String rawFileName = StrUtil.subBefore(fileName, ".", true);
        String fileType = StrUtil.subAfter(fileName, ".", true);
        String localFilePath = StrUtil.appendIfMissing(fileTempPath, "/") + rawFileName  + "." + fileType;
        try {
            file.transferTo(new File(localFilePath));
        } catch (IOException e) {
            log.error("【文件上传至本地】失败，绝对路径：{}", localFilePath);
            return  ResultDTO.error(ErrorCode.ERROR_UNDEFINED);
        }
        log.info("【文件上传至本地】绝对路径：{}", localFilePath);
        return hadoopUtils.upload(currentPath+"/" + fileName, localFilePath)? ResultDTO.success():ResultDTO.error(ErrorCode.FAILED_UPLOAD);

    }

    /**
     * 创建新文件夹
     * @param name
     * @return
     */
    @PostMapping("/local/mkdir")
    public String mkdir(@RequestParam(value = "addNew")String name){
        name=name.trim();
        System.out.println("新增路径："+currentPath);
        System.out.println("新增文件夹路径"+currentPath+"/"+name);
        hadoopUtils.createDir(currentPath,name);
        return "redirect:/local?hdfs="+currentPath;
    }

    /**
     * 预览文件转pdf
     * @param response
     * @param path
     * @return
     */
    @ResponseBody
    @RequestMapping("local/toPdfFile")
    public String toPdfFile(HttpServletResponse response, @RequestParam(value = "path", required = false) String path) {
        if(path==null)
            path=DEFAULT;
        System.out.println("预览文件路径"+path);
        String p = hadoopUtils.copyFromHDFS(path);
        System.out.println("获取到文件路径"+p);
        String fileName = p.substring(p.lastIndexOf("/" )+ 1, p.length());
        File file = new File(p);//需要转换的文件
        /*对filename进行判断*/
        try {
            File newFile = new File(localPath);//转换之后文件生成的地址
            if (!newFile.exists()) {
                newFile.mkdirs();
            }
            //文件转化
            converter.convert(file).to(new File(localPath+"/"+fileName+".pdf")).execute();
            //使用response,将pdf文件以流的方式发送的前段
            response.reset();
            ServletOutputStream outputStream = response.getOutputStream();
            InputStream in = new FileInputStream(new File(localPath+"/"+fileName+".pdf"));// 读取文件
            // copy文件
            int i = IOUtils.copy(in, outputStream);
            System.out.println(i);
            in.close();
            outputStream.close();

        } catch (Exception e) {
            log.info("客户端端开连接");
        }

        return "local";
    }


}
