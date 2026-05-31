package com.forest.payment.entity;

import com.forest.starter.jpa.ForestAuditablePO;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.time.LocalDateTime;

/**
 * 表示支付执行单。
 *
 * <p>支付单是业务订单和第三方支付渠道之间的执行记录，只沉淀支付事实，不承载充值到账、
 * 积分入账等业务侧结果。</p>
 */
@Entity
@Table(
    name = "payment_order",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_payment_order_payment_no", columnNames = {"payment_no"}),
        @UniqueConstraint(name = "uk_payment_order_out_trade_no", columnNames = {"out_trade_no"})
    }
)
public class PaymentOrderPO extends ForestAuditablePO {
    /**
     * 支付单生命周期状态。
     */
    public enum Status {
        /**
         * 本地支付单已创建，尚未成功向渠道创建预支付会话。
         */
        CREATED,

        /**
         * 渠道预支付会话已创建，可返回给小程序调起 wx.requestPayment。
         */
        PREPAY_CREATED,

        /**
         * 支付渠道已确认收款成功，并已通过回调完成本地入账事件发布。
         */
        SUCCESS,

        /**
         * 支付尝试失败的终态，预留给渠道明确失败或人工失败标记场景。
         */
        FAILED,

        /**
         * 支付单已关闭，不再允许继续支付，通常发生在同一业务单重新发起支付前。
         */
        CLOSED
    }

    /**
     * 支付渠道及支付场景。
     */
    public enum Channel {
        /**
         * 微信小程序支付场景；底层仍调用微信支付 API v3 的 /v3/pay/transactions/jsapi 下单接口。
         */
        WECHAT_MINIAPP_PAYMENT("WECHAT_MINIAPP_PAYMENT");

        /**
         * 数据库存储值，显式维护是为了兼容历史渠道值迁移和后续渠道扩展。
         */
        private final String persistedValue;

        Channel(String persistedValue) {
            this.persistedValue = persistedValue;
        }

        public String getPersistedValue() {
            return persistedValue;
        }

        public static Channel fromPersistedValue(String persistedValue) {
            if (persistedValue == null || persistedValue.isBlank()) {
                return null;
            }
            if ("WECHAT_MINIAPP_PAYMENT".equals(persistedValue) || "WECHAT_JSAPI".equals(persistedValue)) {
                return WECHAT_MINIAPP_PAYMENT;
            }
            throw new IllegalArgumentException("未知支付渠道: " + persistedValue);
        }
    }

    /**
     * 支付单主键，本系统内部定位一笔支付执行记录的唯一 ID。
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 本系统生成的支付单号，用于后台运营、日志排查和对外展示，不直接传给微信支付。
     */
    @Column(name = "payment_no", nullable = false, length = 64)
    private String paymentNo;

    /**
     * 业务单类型，用于路由回原业务域；当前充值场景使用 RECHARGE。
     */
    @Column(name = "biz_type", nullable = false, length = 32)
    private String bizType;

    /**
     * 业务主单 ID，例如充值订单 ID；支付成功后事件会携带该值回写业务域。
     */
    @Column(name = "biz_order_id", nullable = false)
    private Long bizOrderId;

    /**
     * 实际使用的支付渠道和场景，目前固定为微信小程序支付。
     */
    @Convert(converter = PaymentOrderChannelConverter.class)
    @Column(nullable = false, length = 32)
    private Channel channel = Channel.WECHAT_MINIAPP_PAYMENT;

    /**
     * 本次应支付金额，单位为分；创建时来自业务快照，回调时用于校验微信通知金额。
     */
    @Column(name = "amount_cents", nullable = false)
    private Integer amountCents;

    /**
     * 支付单状态，描述本地支付执行记录从创建、预下单到成功或关闭的流转结果。
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private Status status = Status.CREATED;

    /**
     * 商户订单号，传给微信支付的 out_trade_no；微信回调会用它反查本地支付单。
     */
    @Column(name = "out_trade_no", nullable = false, length = 64)
    private String outTradeNo;

    /**
     * 微信预支付交易会话标识，来自预下单响应，用于生成小程序调起支付参数和排查渠道链路。
     */
    @Column(name = "prepay_id", length = 128)
    private String prepayId;

    /**
     * 微信支付交易号，支付成功回调中返回，是微信侧确认交易的唯一凭证。
     */
    @Column(name = "transaction_id", length = 64)
    private String transactionId;

    /**
     * 创建人 ID，预留给后台人工建单或审计场景；小程序用户自助支付链路目前不强依赖。
     */

    /**
     * 最后修改人 ID，预留给后台调整、人工关单等审计场景。
     */

    /**
     * 软删除标记，0 表示有效记录；支付链路默认只产生有效支付单。
     */

    /**
     * 支付单创建时间，由实体持久化前自动写入。
     */

    /**
     * 本系统收到并处理微信支付回调的时间，用于定位回调延迟和幂等处理。
     */
    @Column(name = "notify_time")
    private LocalDateTime notifyTime;

    /**
     * 微信侧确认支付成功的时间，来自回调通知；业务到账事件以该时间作为支付完成时间。
     */
    @Column(name = "paid_time")
    private LocalDateTime paidTime;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getPaymentNo() {
        return paymentNo;
    }

    public void setPaymentNo(String paymentNo) {
        this.paymentNo = paymentNo;
    }

    public String getBizType() {
        return bizType;
    }

    public void setBizType(String bizType) {
        this.bizType = bizType;
    }

    public Long getBizOrderId() {
        return bizOrderId;
    }

    public void setBizOrderId(Long bizOrderId) {
        this.bizOrderId = bizOrderId;
    }

    public Channel getChannel() {
        return channel;
    }

    public void setChannel(Channel channel) {
        this.channel = channel;
    }

    public Integer getAmountCents() {
        return amountCents;
    }

    public void setAmountCents(Integer amountCents) {
        this.amountCents = amountCents;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public String getOutTradeNo() {
        return outTradeNo;
    }

    public void setOutTradeNo(String outTradeNo) {
        this.outTradeNo = outTradeNo;
    }

    public String getPrepayId() {
        return prepayId;
    }

    public void setPrepayId(String prepayId) {
        this.prepayId = prepayId;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public LocalDateTime getNotifyTime() {
        return notifyTime;
    }

    public void setNotifyTime(LocalDateTime notifyTime) {
        this.notifyTime = notifyTime;
    }

    public LocalDateTime getPaidTime() {
        return paidTime;
    }

    public void setPaidTime(LocalDateTime paidTime) {
        this.paidTime = paidTime;
    }
}
