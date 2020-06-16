package com.cloud.demo.controller;

import com.cloud.demo.entity.HdfsPath;
import com.cloud.demo.entity.User;
import com.cloud.demo.service.UserService;
import com.cloud.demo.service.utils.HadoopUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@Controller
@Slf4j
public class CloudController {
    public static String DEFAULT = "/cloud/demo";
    @Autowired
    private HadoopUtils hadoopUtils;
    @Autowired
    private UserService userService;

    @GetMapping("/cloudStatus")
    public String cloud(Model model, HttpServletRequest request) {
        User user1 =(User) request.getSession().getAttribute("user");
        User user = userService.getUser(user1.getId());
        List<Object> size = hadoopUtils.getAllSize(hadoopUtils.getFilesData(DEFAULT), user.getMaxSize());
        model.addAttribute("user",user);
        model.addAttribute("size",size);
        return "/cloudStatus";
    }

    @ResponseBody
    @RequestMapping("/cloudStatus/{action}")
    public Object getBar(@PathVariable(name = "action") String action) {
        List<HdfsPath> filesData = hadoopUtils.getFilesData(DEFAULT);
        if (action.equals("bar"))
            return hadoopUtils.getDataBar(filesData);
        else if (action.equals("pie"))
            return hadoopUtils.getDataPie(filesData);
        else
            return null;
    }


}
