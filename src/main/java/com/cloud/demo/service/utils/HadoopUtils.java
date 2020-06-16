package com.cloud.demo.service.utils;

import com.cloud.demo.dto.BarDTO;
import com.cloud.demo.dto.PieDTO;
import com.cloud.demo.entity.HdfsPath;
import lombok.extern.slf4j.Slf4j;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class HadoopUtils {

    @Value("${hdfs.core-site}")
    private String core;
    @Value("${hdfs.hdfs-site}")
    private String hdfs;
    @Value("${localFile-home}")
    private String localPath;

    private Configuration configuration = new Configuration();
    private FileSystem fileSystem;

    public void init() throws IOException {
        configuration.addResource(core);
        configuration.addResource(hdfs);
        fileSystem = FileSystem.get(configuration);
    }


    /**
     * 上传文件
     *
     * @param path
     * @param local
     * @return
     */
    public boolean upload(String path, String local) {
        File file = new File(local);
        try {
            fileSystem.copyFromLocalFile(new Path(local), new Path(path));
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        } finally {
            log.info("【文件删除{}】，绝对路径：{}", file.delete() ? "成功" : "失败", local);
        }
    }

    /**
     * 获取文件类型
     *
     * @param name
     * @return
     */
    public String getFileType(String name) {
        String type = "";
        if (name.lastIndexOf(".") > 0) {
            type = name.substring(name.lastIndexOf("."), name.length());
        } else
            return "other";
        String out = "";
        if (type.indexOf("doc") > 0 || type.indexOf("docx") > 0)
            out = "word";
        else if (type.indexOf("xls") > 0 || type.indexOf("xlsx") > 0)
            out = "excel";
        else if (type.indexOf("ppt") > 0 || type.indexOf("pptx") > 0)
            out = "ppt";
        else if (type.indexOf("txt") > 0 || type.indexOf("css") > 0 || type.indexOf("html") > 0 || type.indexOf("js") > 0)
            out = "txt";
        else if (type.indexOf("jpg") > 0 || type.indexOf("jpeg") > 0 || type.indexOf("png") > 0 || type.indexOf("bmp") > 0)
            out = "pic";
        else if (type.indexOf("zip") > 0 || type.indexOf("rar") > 0 || type.indexOf("jar") > 0)
            out = "zip";
        else if (type.indexOf("pdf") > 0)
            out = "pdf";
        else
            out = "other";
        return out;
    }

    /**
     * 获取当前文件列表
     *
     * @param path
     * @return
     */
    public List<HdfsPath> getFilesList(String path) {
        List<HdfsPath> list = new ArrayList<>();
        path += "/";
        System.out.println("PATH:" + path);
        try {
            FileStatus[] fileStatuses = fileSystem.listStatus(new Path(path));
            for (int i = 0; i < fileStatuses.length; i++) {
                Path p = fileStatuses[i].getPath();
                HdfsPath hdfsPath = new HdfsPath();
                hdfsPath.setName(p.getName());
                hdfsPath.setPathName(path + p.getName());
                hdfsPath.setSize(fileStatuses[i].getLen());
                if (fileStatuses[i].isFile()) {
                    hdfsPath.setTypes(getFileType(p.getName()));
                    hdfsPath.setCreateTime(fileStatuses[i].getAccessTime());
                } else {
                    hdfsPath.setTypes("dir");
                    hdfsPath.setCreateTime(fileStatuses[i].getModificationTime());
                }

                list.add(hdfsPath);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return list;
    }

    /**
     * 获取路径列表
     *
     * @param path
     * @return
     */
    public List<HdfsPath> getPaths(String path) {

        String[] split = path.split("/");
        List<HdfsPath> hdfsPaths = new ArrayList<>();
        List<String> paths = Arrays.stream(split).collect(Collectors.toList());
        paths.forEach((v) -> {
            if (!v.equals("")) {
                String substring = path.substring(0, path.indexOf(v)) + v;
                hdfsPaths.add(new HdfsPath(v, substring));
            }
        });
        return hdfsPaths.subList(2, hdfsPaths.size());
    }

    /**
     * 创建文件夹
     *
     * @param path
     * @param name
     * @return
     */
    public boolean createDir(String path, String name) {
        try {
            fileSystem.mkdirs(new Path(path + "/" + name));
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    /**
     * 删除文件
     *
     * @param path
     * @return
     */
    public boolean delete(String path) {
        try {
            fileSystem.delete(new Path(path), true);
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    /**
     * 修改文件名
     *
     * @param oldName
     * @param newName
     * @return
     */
    public boolean changeName(String oldName, String newName) {
        try {
            fileSystem.rename(new Path(oldName), new Path(newName));
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 从hdfs上复制文件到本地
     *
     * @param path
     * @return
     */
    public String copyFromHDFS(String path) {
        String name = path.substring(path.lastIndexOf("/") + 1, path.length());
        try {
            fileSystem.copyToLocalFile(new Path(path), new Path(localPath + "/" + name));
            return localPath + "/" + name;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 清空缓存
     */
    public void clearTmp() {
        String path = localPath;
        File file = new File(path);
        String[] tempList = file.list();
        File temp = null;
        for (int i = 0; i < tempList.length; i++) {
            if (path.endsWith(File.separator)) {
                temp = new File(path + tempList[i]);
            } else {
                temp = new File(path + File.separator + tempList[i]);
            }
            if (temp.isFile()) {
                temp.delete();
            }
        }
    }

    /**
     * 获取所有文件信息
     *
     * @param path
     * @return
     */
    public List<HdfsPath> getFilesData(String path) {
        List<HdfsPath> list = new ArrayList<>();
        try {
            FileStatus[] fileStatuses = fileSystem.listStatus(new Path(path));
            for (int i = 0; i < fileStatuses.length; i++) {
                Path p = fileStatuses[i].getPath();
                HdfsPath hdfsPath = new HdfsPath();
                hdfsPath.setName(p.getName());
                hdfsPath.setPathName(path + p.getName());
                hdfsPath.setSize(fileStatuses[i].getLen());
                if (fileStatuses[i].isFile()) {
                    hdfsPath.setTypes(getFileType(p.getName()));
                    hdfsPath.setCreateTime(fileStatuses[i].getAccessTime());
                    list.add(hdfsPath);
                } else {
                    List<HdfsPath> filesData = getFilesData(p.toString());
                    list.addAll(filesData);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return list;
    }

    /**
     * 获取饼状图
     *
     * @param filesData
     * @return
     */
    public List<PieDTO> getDataPie(List<HdfsPath> filesData) {
        List<PieDTO> list = new ArrayList<>();
        Map<String, Integer> map = new HashMap<>();
        filesData.stream().map(v -> getFileType(v.getName())).forEach((fileType) -> {
            Integer integer = map.get(fileType);
            if (integer != null)
                map.put(fileType, integer + 1);
            else
                map.put(fileType, 1);
        });
        map.forEach((k, v) -> {
            list.add(new PieDTO(k, v));
        });

        return list;
    }

    /**
     * 获取条形图数据
     * @param filesData
     * @return
     */
    public BarDTO getDataBar(List<HdfsPath> filesData) {
        List<PieDTO> dataPie = getDataPie(filesData);
        List<String> name = new ArrayList<>();
        List<Integer> number = new ArrayList<>();
        dataPie.forEach((v)->{
            name.add(v.getName());
            number.add(v.getValue());
        });

        return new BarDTO(name,number);
    }

    /**
     * 获取用户云盘信息
     * @param filesData
     * @param maxSize
     * @return
     */
    public List<Object> getAllSize(List<HdfsPath> filesData,Integer maxSize){
        List<Object>out=new ArrayList<>();
        long size= 0;
        for(int i=0;i<filesData.size();i++)
            size+=filesData.get(i).getSize();
        if(size<1024)
            out.add (size+'B');
        else if(size>=1024||size<=1048576)
            out.add(size/1024+"KB");
        else
            out.add(size/1024560+"MB");
        out.add(size/1048576/maxSize);
        return out;
    }
}
