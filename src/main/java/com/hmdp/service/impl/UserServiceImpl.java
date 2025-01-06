package com.hmdp.service.impl;

import cn.hutool.core.util.RandomUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hmdp.dto.LoginFormDTO;
import com.hmdp.dto.Result;
import com.hmdp.dto.UserDTO;
import com.hmdp.entity.User;
import com.hmdp.mapper.UserMapper;
import com.hmdp.service.IUserService;
import com.hmdp.utils.RegexUtils;
import lombok.extern.slf4j.Slf4j;
import cn.hutool.core.bean.BeanUtil;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpSession;

import static com.hmdp.utils.SystemConstants.USER_NICK_NAME_PREFIX;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author 虎哥
 * @since 2021-12-22
 */
@Slf4j
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements IUserService {

    @Override
    public Result sendCode(String phone, HttpSession session) {
        //校验手机号
        if (RegexUtils.isPhoneInvalid(phone)) {
            //如果不符合，返回错误信息
            return Result.fail("手机号格式错误！");
        }
//        如果不符合返回错误信息
//        如果符合，则生成验证码
        String code = RandomUtil.randomNumbers(6);
//        将验证码保存在session中
        session.setAttribute("code",code);
//        发闪送验证码
        log.debug("发送验证码=>{}",code);
//        返回ok
        return Result.ok();
    }

    @Override
    public Result login(LoginFormDTO loginForm, HttpSession session) {
        String phone = loginForm.getPhone();
//        1.校验验证码
        if (RegexUtils.isPhoneInvalid(phone)) {
            //如果不符合，返回错误信息
            return Result.fail("手机号格式错误！");
        }
//        2.校验验证码
        String formCode = loginForm.getCode();
        if (formCode == null || !formCode.equals(session.getAttribute("code"))){
            //        3.验证码不一致报错
            return Result.fail("验证码错误！");
        }
//        4.根据用户号查询用户手机号是否存在
        User user = query().eq("phone", phone).one();
        if (user == null){
            //        5.不存在 创建新用户并保存在session中
             user = carterUserWithPhone(phone, session);
        }

//        3.用户存在直接保存在session中
        session.setAttribute("user", BeanUtil.copyProperties(user, UserDTO.class));
        return Result.ok();
    }

    private User carterUserWithPhone(String phone, HttpSession session) {
        User user = new User();
        user.setPhone(phone);
        user.setNickName(USER_NICK_NAME_PREFIX+RandomUtil.randomString(10));
        //保存用户
        save(user);
        return user;
    }
}
