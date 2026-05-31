package com.forest.notification.sms.entity;

import com.forest.starter.jpa.ForestAuditablePO;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

/**
 * Records every SMS send attempt for audit and provider troubleshooting.
 */
@Entity
@Table(name = "sms_send_log")
public class SmsSendLogPO extends ForestAuditablePO {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "sms_no", nullable = false, length = 40, unique = true)
    private String smsNo;

    @Column(name = "business_app_code", nullable = false, length = 80)
    private String businessAppCode;

    @Column(name = "client_app_code", length = 80)
    private String clientAppCode;

    @Column(name = "client_type", length = 30)
    private String clientType;

    @Column(nullable = false, length = 40)
    private String scene;

    @Column(nullable = false, length = 30)
    private String phone;

    @Column(name = "template_code", length = 80)
    private String templateCode;

    @Column(name = "sign_name", length = 80)
    private String signName;

    @Column(name = "content_snapshot", length = 500)
    private String contentSnapshot;

    @Column(nullable = false, length = 30)
    private String provider;

    @Column(name = "provider_request_id", length = 120)
    private String providerRequestId;

    @Column(name = "provider_biz_id", length = 120)
    private String providerBizId;

    @Column(name = "provider_response_code", length = 80)
    private String providerResponseCode;

    @Column(name = "provider_response_message", length = 255)
    private String providerResponseMessage;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private SmsSendStatus status = SmsSendStatus.PENDING;

    @Column(name = "send_ip", length = 64)
    private String sendIp;

    @Column(name = "sent_at")
    private LocalDateTime sentAt;

    @Column(name = "failed_at")
    private LocalDateTime failedAt;

    public Long getId() {
        return id;
    }

    public String getSmsNo() {
        return smsNo;
    }

    public void setSmsNo(String smsNo) {
        this.smsNo = smsNo;
    }

    public String getBusinessAppCode() {
        return businessAppCode;
    }

    public void setBusinessAppCode(String businessAppCode) {
        this.businessAppCode = businessAppCode;
    }

    public String getClientAppCode() {
        return clientAppCode;
    }

    public void setClientAppCode(String clientAppCode) {
        this.clientAppCode = clientAppCode;
    }

    public String getClientType() {
        return clientType;
    }

    public void setClientType(String clientType) {
        this.clientType = clientType;
    }

    public String getScene() {
        return scene;
    }

    public void setScene(String scene) {
        this.scene = scene;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getTemplateCode() {
        return templateCode;
    }

    public void setTemplateCode(String templateCode) {
        this.templateCode = templateCode;
    }

    public String getSignName() {
        return signName;
    }

    public void setSignName(String signName) {
        this.signName = signName;
    }

    public String getContentSnapshot() {
        return contentSnapshot;
    }

    public void setContentSnapshot(String contentSnapshot) {
        this.contentSnapshot = contentSnapshot;
    }

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public String getProviderRequestId() {
        return providerRequestId;
    }

    public void setProviderRequestId(String providerRequestId) {
        this.providerRequestId = providerRequestId;
    }

    public String getProviderBizId() {
        return providerBizId;
    }

    public void setProviderBizId(String providerBizId) {
        this.providerBizId = providerBizId;
    }

    public String getProviderResponseCode() {
        return providerResponseCode;
    }

    public void setProviderResponseCode(String providerResponseCode) {
        this.providerResponseCode = providerResponseCode;
    }

    public String getProviderResponseMessage() {
        return providerResponseMessage;
    }

    public void setProviderResponseMessage(String providerResponseMessage) {
        this.providerResponseMessage = providerResponseMessage;
    }

    public SmsSendStatus getStatus() {
        return status;
    }

    public void setStatus(SmsSendStatus status) {
        this.status = status;
    }

    public String getSendIp() {
        return sendIp;
    }

    public void setSendIp(String sendIp) {
        this.sendIp = sendIp;
    }

    public LocalDateTime getSentAt() {
        return sentAt;
    }

    public void setSentAt(LocalDateTime sentAt) {
        this.sentAt = sentAt;
    }

    public LocalDateTime getFailedAt() {
        return failedAt;
    }

    public void setFailedAt(LocalDateTime failedAt) {
        this.failedAt = failedAt;
    }
}
