package com.cloud.demo.controller;

import com.cloud.demo.entity.User;
import com.cloud.demo.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;

@Controller
public class IndexController {
    @Autowired
    private UserService userService;

    @GetMapping("/")
    public String index() {
        return "login";
    }

    @PostMapping("/login")
    public String login(HttpServletRequest request,
                        HttpServletResponse response,
                        @RequestParam(value = "u") String name,
                        @RequestParam(value = "p") String password) {
        User user = userService.checkUser(name, password);
        if(user!=null){
            //更新时间
            user.setLastLoginTime(new Date());
            userService.updateUser(user);
            //将token标识放入cookie里面
            response.addCookie(new Cookie("token","123456"));
            //登陆成功,写session
            request.getSession().setAttribute("user",user);
            return "redirect:/local";
        } else
            return "redirect:/";
    }
    //退出登录
    @GetMapping("/logout")
    public String logout(HttpServletRequest request,
                         HttpServletResponse response){
        //移除session
        request.getSession().removeAttribute("user");
        Cookie cookie = new Cookie("token",null);
        cookie.setMaxAge(0);
        response.addCookie(cookie);

        return "redirect:/";
    }
}
