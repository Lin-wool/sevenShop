package com.sevenshop.service;

import com.sevenshop.entity.Order;
import com.sevenshop.entity.Product;
import com.sevenshop.entity.User;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class MailService {

    private static final Logger logger = LoggerFactory.getLogger(MailService.class);

    @Autowired
    private JavaMailSender mailSender;

    @Value("${app.admin-email:}")
    private String adminEmail;

    @Value("${spring.mail.username:}")
    private String mailUsername;

    @Async
    public void sendNewOrderNotification(User user, Order order, Product product) {
        logger.info("========== 邮件服务开始处理 ==========");
        logger.info("adminEmail: {}", adminEmail);
        logger.info("mailUsername: {}", mailUsername);

        if (adminEmail == null || adminEmail.isEmpty()) {
            logger.warn("邮件未发送: adminEmail 未配置!");
            return;
        }

        if (mailUsername == null || mailUsername.isEmpty()) {
            logger.warn("邮件未发送: mailUsername (MAIL_USERNAME) 未配置!");
            return;
        }

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(mailUsername);
            helper.setTo(adminEmail);
            helper.setSubject("新订单提醒 - " + user.getNickname() + " 下单了！");

            int quantity = order.getQuantity() != null ? order.getQuantity() : 1;
            String htmlContent = String.format("""
                <div style="font-family: Arial, sans-serif; padding: 20px; max-width: 600px; margin: 0 auto;">
                    <h2 style="color: #ff6b6b;">💕 新订单提醒</h2>
                    <div style="background: #f8f9fa; padding: 20px; border-radius: 8px; margin: 20px 0;">
                        <p><strong>用户：</strong>%s (%s)</p>
                        <p><strong>商品：</strong>%s</p>
                        <p><strong>数量：</strong>%d 件</p>
                        <p><strong>总价：</strong>¥%.2f</p>
                        <p><strong>规格：</strong>%s</p>
                        <p><strong>地址：</strong>%s</p>
                        <p><strong>备注：</strong>%s</p>
                        <p><strong>下单时间：</strong>%s</p>
                    </div>
                    <p style="color: #666; font-size: 14px;">请尽快处理订单～</p>
                </div>
                """,
                user.getNickname(),
                user.getUsername(),
                product.getName(),
                quantity,
                order.getPrice(),
                order.getSpecs(),
                order.getAddress(),
                order.getRemark() != null ? order.getRemark() : "无",
                order.getCreatedAt()
            );

            helper.setText(htmlContent, true);
            mailSender.send(message);
            logger.info("邮件发送成功! 发送给: {}", adminEmail);
        } catch (Exception e) {
            logger.error("邮件发送失败: ", e);
        }
    }
}
