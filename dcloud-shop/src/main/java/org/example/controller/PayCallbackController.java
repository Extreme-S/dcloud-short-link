package org.example.controller;

import com.alibaba.fastjson.JSONObject;
import com.wechat.pay.contrib.apache.httpclient.auth.ScheduledUpdateCertificatesVerifier;
import com.wechat.pay.contrib.apache.httpclient.util.AesUtil;
import lombok.extern.slf4j.Slf4j;
import org.example.config.WechatPayConfig;
import org.example.service.ProductOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;


@Controller
@RequestMapping("/api/callback/order/v1/")
@Slf4j
public class PayCallbackController {

    @Autowired
    private WechatPayConfig wechatPayConfig;

    @Autowired
    private ProductOrderService productOrderService;

    @Autowired
    private ScheduledUpdateCertificatesVerifier verifier;


    /**
     * * 获取报文
     * * 验证签名（确保是微信传输过来的）
     * * 解密（AES对称解密出原始数据）
     * * 处理业务逻辑
     * * 响应请求
     */
    @RequestMapping("wechat")
    @ResponseBody
    public Map<String, String> wechatPayCallback(HttpServletRequest request, HttpServletResponse response) {
        String body = getRequestBody(request);                                  //获取报文
        String nonceStr = request.getHeader("Wechatpay-Nonce");           //随机串
        String signature = request.getHeader("Wechatpay-Signature");      //微信传递过来的签名
        String serialNo = request.getHeader("Wechatpay-Serial");          //证书序列号（微信平台）
        String timestamp = request.getHeader("Wechatpay-Timestamp");      //时间戳
        //构造签名串——》 应答时间戳\n应答随机串\n应答报文主体\n
        String signStr = Stream.of(timestamp, nonceStr, body).collect(Collectors.joining("\n", "", "\n"));
        Map<String, String> map = new HashMap<>(2);
        try {
            //验证签名是否通过
            boolean result = verifiedSign(serialNo, signStr, signature);
            if (result) {
                //解密数据
                String plainBody = decryptBody(body);
                log.info("解密后的明文:{}", plainBody);
                Map<String, String> paramsMap = convertWechatPayMsgToMap(plainBody);
                //处理业务逻辑 TODO
                //响应微信
                map.put("code", "SUCCESS");
                map.put("message", "成功");
            }
        } catch (Exception e) {
            log.error("微信支付回调异常:{}", e);
        }
        return map;
    }


    /**
     * 转换body为map
     */
    private Map<String, String> convertWechatPayMsgToMap(String plainBody) {
        Map<String, String> paramsMap = new HashMap<>(2);
        JSONObject jsonObject = JSONObject.parseObject(plainBody);
        paramsMap.put("out_trade_no", jsonObject.getString("out_trade_no"));                        //商户订单号
        paramsMap.put("trade_state", jsonObject.getString("trade_state"));                          //交易状态
        paramsMap.put("account_no", jsonObject.getJSONObject("attach").getString("accountNo"));     //附加数据
        return paramsMap;
    }


    /**
     * 解密body的密文
     * <p>
     * "resource": {
     * "original_type": "transaction",
     * "algorithm": "AEAD_AES_256_GCM",
     * "ciphertext": "",
     * "associated_data": "",
     * "nonce": ""
     * }
     */
    private String decryptBody(String body) throws GeneralSecurityException {
        AesUtil aesUtil = new AesUtil(wechatPayConfig.getApiV3Key().getBytes(StandardCharsets.UTF_8));
        JSONObject object = JSONObject.parseObject(body);
        JSONObject resource = object.getJSONObject("resource");
        String ciphertext = resource.getString("ciphertext");
        String associatedData = resource.getString("associated_data");
        String nonce = resource.getString("nonce");
        return aesUtil.decryptToString(
                associatedData.getBytes(StandardCharsets.UTF_8), nonce.getBytes(StandardCharsets.UTF_8), ciphertext);
    }


    /**
     * 验证签名
     *
     * @param serialNo  微信平台-证书序列号
     * @param signStr   自己组装的签名串
     * @param signature 微信返回的签名
     */
    private boolean verifiedSign(String serialNo, String signStr, String signature) {
        return verifier.verify(serialNo, signStr.getBytes(StandardCharsets.UTF_8), signature);
    }


    /**
     * 读取请求数据流
     */
    private String getRequestBody(HttpServletRequest request) {
        StringBuilder sb = new StringBuilder();
        try (ServletInputStream inputStream = request.getInputStream();
             BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));) {
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
        } catch (IOException e) {
            log.error("读取数据流异常:{}", e);
        }
        return sb.toString();
    }


}
