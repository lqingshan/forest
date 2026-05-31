package com.forest.recharge.entity;

import com.forest.starter.jpa.ForestAuditablePO;

import jakarta.persistence.Column;
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
 * 表示充值业务主单。
 *
 * <p>充值单负责锁定用户选择的充值套餐、应付金额和到账积分；支付执行过程交给
 * payment_order 记录，支付成功后再回写本单的到账状态。</p>
 */
@Entity
@Table(
    name = "recharge_order",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_recharge_order_recharge_no", columnNames = {"recharge_no"})
    }
)
public class RechargeOrderPO extends ForestAuditablePO {
    /**
     * 充值业务单生命周期状态。
     */
    public enum Status {
        /**
         * 充值单已创建，尚未被支付成功回调确认。
         */
        CREATED,

        /**
         * 充值单已到账，积分入账事件已由充值域发布。
         */
        PAID,

        /**
         * 充值单已关闭，不再允许继续支付或入账。
         */
        CLOSED
    }

    /**
     * 充值单主键，本系统内部定位一笔充值业务的唯一 ID。
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 发起充值的用户 ID，用于订单归属校验和充值成功后的积分入账。
     */
    @Column(name = "user_id", nullable = false)
    private Long userId;

    /**
     * 充值套餐编码，创建订单时从套餐目录选择，并用于支付描述和运营排查。
     */
    @Column(name = "package_code", nullable = false, length = 40)
    private String packageCode;

    /**
     * 本次充值应支付金额，单位为分；创建后作为支付单金额快照来源。
     */
    @Column(name = "amount_cents", nullable = false)
    private Integer amountCents;

    /**
     * 支付成功后应给用户增加的积分数量，创建充值单时由套餐配置锁定。
     */
    @Column(name = "credited_points", nullable = false)
    private Integer creditedPoints;

    /**
     * 充值业务状态，描述本单是否仍待支付、已到账或已关闭。
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Status status = Status.CREATED;

    /**
     * 本系统生成的充值单号，用于前端展示、后台运营和日志排查。
     */
    @Column(name = "recharge_no", nullable = false, length = 64)
    private String rechargeNo;

    /**
     * 成功完成本充值单的支付单 ID；只有支付成功并回写到账后才会填充。
     */
    @Column(name = "paid_payment_order_id")
    private Long paidPaymentOrderId;

    /**
     * 创建人 ID，预留给后台代客建单或审计场景；用户自助充值链路目前不强依赖。
     */

    /**
     * 最后修改人 ID，预留给后台关单、修正或审计场景。
     */

    /**
     * 软删除标记，0 表示有效记录；充值链路默认只产生有效充值单。
     */

    /**
     * 充值单创建时间，由实体持久化前自动写入。
     */

    /**
     * 充值到账时间，通常来自支付成功事件携带的微信支付成功时间。
     */
    @Column(name = "paid_time")
    private LocalDateTime paidTime;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getPackageCode() {
        return packageCode;
    }

    public void setPackageCode(String packageCode) {
        this.packageCode = packageCode;
    }

    public Integer getAmountCents() {
        return amountCents;
    }

    public void setAmountCents(Integer amountCents) {
        this.amountCents = amountCents;
    }

    public Integer getCreditedPoints() {
        return creditedPoints;
    }

    public void setCreditedPoints(Integer creditedPoints) {
        this.creditedPoints = creditedPoints;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public String getRechargeNo() {
        return rechargeNo;
    }

    public void setRechargeNo(String rechargeNo) {
        this.rechargeNo = rechargeNo;
    }

    public Long getPaidPaymentOrderId() {
        return paidPaymentOrderId;
    }

    public void setPaidPaymentOrderId(Long paidPaymentOrderId) {
        this.paidPaymentOrderId = paidPaymentOrderId;
    }

    public LocalDateTime getPaidTime() {
        return paidTime;
    }

    public void setPaidTime(LocalDateTime paidTime) {
        this.paidTime = paidTime;
    }
}
