package com.nowcoder.controller;

import com.nowcoder.dao.LoginTicketDAO;
import com.nowcoder.service.UserService;
import com.sun.deploy.net.HttpResponse;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

/**
 * Created by 张汉江 on 2017/8/17
 */
@Controller
public class LoginController {

    private final  static Logger logger= LoggerFactory.getLogger(LoginController.class);
    @Autowired
    UserService userService;
    @RequestMapping(path ={"/reg/"},method = {RequestMethod.POST})
    public String register(Model model, @RequestParam("username") String username,
                           @RequestParam("password") String password,
                           @RequestParam(value = "next" , required = false) String  next ,
                           @RequestParam(value = "rememberme" ,defaultValue = "false") boolean  rememberme ,
                           HttpServletResponse response){
        try {
            Map<String,String>map= userService.register(password, username);
            if(map.containsKey("ticket")){       //success
                Cookie cookie=new Cookie("ticket",map.get("ticket"));
                cookie.setPath("/");
                if(rememberme) {
                    cookie.setMaxAge(3600 * 24 * 5);
                }
                response.addCookie(cookie);
                if(StringUtils.isNotBlank(next)){
                    return "redirect:"+next;
                }
                return "redirect:/";
            }else{
                model.addAttribute("msg",map.get("msg"));
                return  "login";
            }
        }catch (Exception e){
            logger.error("注册异常!"+e.getMessage());
            model.addAttribute("msg","服务器错误!");
            return "login";
        }

    }

    @RequestMapping(path ={"/reglogin"},method = {RequestMethod.GET})
    public String reg(Model model, @RequestParam(value = "next" , required = false) String  next ) {
        model.addAttribute("next",next);
      return  "login";

    }

    @RequestMapping(path ={"/login/"},method = {RequestMethod.POST})
    public String login(Model model, @RequestParam("username") String username,
                        @RequestParam("password") String password,
                        @RequestParam(value = "next" , required = false) String  next ,
                        @RequestParam(value = "rememberme" ,defaultValue = "false") boolean  rememberme ,
                        HttpServletResponse response){
        try {
            Map<String,String>map= userService.login(password, username);
            if(map.containsKey("ticket")){//success
                Cookie cookie=new Cookie("ticket",map.get("ticket"));
                cookie.setPath("/");
                if(rememberme) {
                    cookie.setMaxAge(3600 * 24 * 5);
                }
                response.addCookie(cookie);
                if(StringUtils.isNotBlank(next)){
                    return "redirect:"+next;
                }
                return "redirect:/";
            }else{
                model.addAttribute("msg",map.get("msg"));
                return  "login";
            }

        }catch (Exception e){
            logger.error("注册异常!"+e.getMessage());
            return "login";
        }

    }
    @RequestMapping(path = {"/logout"},method = RequestMethod.GET)
    public String logout(@CookieValue("ticket") String ticket){
        userService.logout(ticket);
        return "redirect:/";
    }

}
