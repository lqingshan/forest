package com.forest.payment.notify;

import com.forest.starter.web.ForestApiPaths;
import com.forest.payment.service.PaymentOrderService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 接收微信支付异步通知。
 *
 * <p>这是微信支付服务器调用的 open API，不依赖小程序用户 JWT；最终到账以后端验签后的通知为准。</p>
 */
@RestController
@RequestMapping(ForestApiPaths.OPEN + "/wechat/pay")
public class WechatPayNotifyController {
    private final PaymentOrderService paymentOrderService;

    public WechatPayNotifyController(PaymentOrderService paymentOrderService) {
        this.paymentOrderService = paymentOrderService;
    }

    @PostMapping("/notify")
    public String notify(@RequestBody String requestBody, @RequestHeader Map<String, String> headers) {
        // 微信支付要求成功处理后返回 success；重复通知由支付服务做幂等。
        paymentOrderService.handleWechatPayNotify(requestBody, headers);
        return "success";
    }
}
