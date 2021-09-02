package com.github.miho73.ipu.library;

import com.github.miho73.ipu.repositories.MemberRepository;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;

@Component
@PropertySource("classpath:/properties/secret.properties")
public class Captcha {
    @Value("${captcha.v3.secret}") private String CAPTCHA_V3_SECRET;
    @Value("${captcha.v2.secret}") private String CAPTCHA_V2_SECRET;

    private final String VERIFY_URL = "https://www.google.com/recaptcha/api/siteverify";
    private final String USER_AGENT = "Mozilla/5.0";

    private final Logger LOGGER = LoggerFactory.getLogger(MemberRepository.class);

    public boolean getV3Result(String token) throws IOException {
        return getResult(CAPTCHA_V3_SECRET, token);
    }
    public boolean getV2Result(String token) throws IOException {
        return getResult(CAPTCHA_V2_SECRET, token);
    }

    private boolean getResult(String secret, String token) throws IOException  {
        URL obj = new URL(VERIFY_URL);
        HttpsURLConnection con = (HttpsURLConnection) obj.openConnection();

        con.setRequestMethod("POST");
        con.setRequestProperty("User-Agent", USER_AGENT);
        con.setRequestProperty("Accept-Language", "en-US,en;q=0.5");

        String postParams = "secret=" + secret + "&response=" + token;

        con.setDoOutput(true);
        DataOutputStream wr = new DataOutputStream(con.getOutputStream());
        wr.writeBytes(postParams);
        wr.flush();
        wr.close();

        int responseCode = con.getResponseCode();

        BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuilder response = new StringBuilder();

        LOGGER.debug("reCAPTCHA verification: "+ response);

        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();

        JSONObject resp = new JSONObject(response.toString());
        return resp.getBoolean("success");
    }
}
