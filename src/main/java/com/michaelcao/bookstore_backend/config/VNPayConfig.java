package com.michaelcao.bookstore_backend.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
@Data
public class VNPayConfig {
    
    @Value("${VNPAY_TMN_CODE:FAKY2Z2T}")
    private String tmnCode;
    
    @Value("${VNPAY_SECRET_KEY:CQ8TLPGRAXSV3EQN2I1PFBTGFNRFWG16}")
    private String secretKey;
    
    @Value("${VNPAY_PAY_URL:https://sandbox.vnpayment.vn/paymentv2/vpcpay.html}")
    private String payUrl;
    
    @Value("${VNPAY_RETURN_URL:http://localhost:3000/payment/result}")
    private String returnUrl;
    
    private String version = "2.1.0";
} 