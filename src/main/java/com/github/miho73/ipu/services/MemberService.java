package com.github.miho73.ipu.services;

import com.github.miho73.ipu.domain.LoginForm;
import com.github.miho73.ipu.domain.Member;
import com.github.miho73.ipu.exceptions.InvalidInputException;
import com.github.miho73.ipu.library.Captcha;
import com.github.miho73.ipu.library.SHA;
import com.github.miho73.ipu.repositories.MemberRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;

@Service("MemberService")
public class MemberService {
    private final MemberRepository memberRepository;
    private final SHA sha;
    private final Captcha captcha;

    private final Logger LOGGER = LoggerFactory.getLogger(MemberRepository.class);

    @Autowired
    public MemberService(MemberRepository memberRepository, SHA sha, Captcha captcha) {
        this.memberRepository = memberRepository;
        this.sha = sha;
        this.captcha = captcha;
    }

    public Member getUserByCode(long code) throws SQLException {
        memberRepository.open();
        Member member = memberRepository.getUserByCode(code);
        memberRepository.close();
        return member;
    }

    public Member getUserById(String id) throws SQLException {
        memberRepository.open();
        Member member = memberRepository.getUserById(id);
        memberRepository.close();
        return member;
    }

    public enum LOGIN_RESULT {
        OK,
        BAD_PASSWORD,
        ID_NOT_FOUND,
        INVALID_INPUT,
        CAPTCHA_FAILED,
    }

    public LOGIN_RESULT checkLogin(LoginForm form) throws SQLException, NoSuchAlgorithmException, InvalidInputException, IOException {
        boolean captchaFlag = false;
        if(form.getgVers().equals("v3") && captcha.getV3Result(form.getgToken())) captchaFlag = true;
        else if(form.getgToken().equals("v2") && captcha.getV2Result(form.getgToken())) captchaFlag = true;

        if (captchaFlag) {
            memberRepository.open();
            Member member = memberRepository.getUserForAuthentication(form.getId());
            memberRepository.close();
            if (member == null) {
                LOGGER.debug("Login attempt: id=" + form.getId() + ", result=id not found");
                return LOGIN_RESULT.ID_NOT_FOUND;
            }
            String hash = sha.SHA512(form.getPassword(), member.getSalt());
            if (member.getPwd().equals(hash)) {
                LOGGER.debug("Login attempt: id=" + form.getId() + ", result=ok");
                return LOGIN_RESULT.OK;
            }
            LOGGER.debug("Login attempt: id=" + form.getId() + ", result=wrong password");
            return LOGIN_RESULT.BAD_PASSWORD;
        } else {
            LOGGER.debug("Login attempt: id=" + form.getId() + ", result=CAPTCHA failed");
            return LOGIN_RESULT.CAPTCHA_FAILED;
        }
    }
}

