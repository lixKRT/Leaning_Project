package powerbankrental.model;

import powerbankrental.model.enums.OrderStatus;

import java.time.Duration;
import java.time.LocalDateTime;
import java.math.BigDecimal;
import java.util.Objects;

//租凭订单实体类
public class RentalOrder {
    private String orderId;
    private Long userId;
    private String powerBankId;
    private LocalDateTime rentalStartTime;
    private int rentalStartChargePercentage;
    private LocalDateTime rentalEndTime;
    private  Integer billedHours;
    private BigDecimal fee;
    private OrderStatus orderStatus;

    public static final BigDecimal HOURLY_RATE = BigDecimal.valueOf(1.5);

    public RentalOrder() {
        this.rentalStartTime = LocalDateTime.now();
        this.orderStatus = OrderStatus.ACTIVE;
    }
    public RentalOrder(Long userId, String powerBankId, int rentalStartChargePercentage) {
        this();
        this.userId = userId;
        this.powerBankId = powerBankId;
        this.rentalStartChargePercentage = rentalStartChargePercentage;
    }
    //完整构造函数，用于数据库加载数据
    public  RentalOrder(String orderId, Long userId, String powerBankId, LocalDateTime rentalStartTime,
                        int rentalStartChargePercentage, LocalDateTime rentalEndTime,
                        Integer billedHours, BigDecimal fee, OrderStatus orderStatus) {
        this.orderId = orderId;
        this.userId = userId;
        this.powerBankId = powerBankId;
        this.rentalStartTime = rentalStartTime;
        this.rentalStartChargePercentage = rentalStartChargePercentage;
        this.rentalEndTime = rentalEndTime;
        this.billedHours = billedHours;
        this.fee = fee;
        this.orderStatus = orderStatus;
    }

    public  String getOrderId() {
        return this.orderId;
    }
    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public Long getUserId() {
        return this.userId;
    }
    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getPowerBankId() {
        return this.powerBankId;
    }
    public void setPowerBankId(String powerBankId) {
        this.powerBankId = powerBankId;
    }

    public LocalDateTime getRentalStartTime() {
        return this.rentalStartTime;
    }
    public void setRentalStartTime(LocalDateTime rentalStartTime) {
        this.rentalStartTime = rentalStartTime;
    }

    public int getRentalStartChargePercentage() {
        return this.rentalStartChargePercentage;
    }
    public void setRentalStartChargePercentage(int rentalStartChargePercentage) {
        this.rentalStartChargePercentage = rentalStartChargePercentage;
    }

    public LocalDateTime getRentalEndTime() {
        return this.rentalEndTime;
    }
    public void setRentalEndTime(LocalDateTime rentalEndTime) {
        this.rentalEndTime = rentalEndTime;
    }

    public Integer getBilledHours() {
        return this.billedHours;
    }
    public void setBilledHours(Integer billedHours) {
        this.billedHours = billedHours;
    }

    public BigDecimal getFee() {
        return this.fee;
    }
    public void setFee(BigDecimal fee) {
        this.fee = fee;
    }

    public OrderStatus getOrderStatus() {
        return this.orderStatus;
    }
    public void setOrderStatus(OrderStatus orderStatus) {
        this.orderStatus = orderStatus;
    }

    //完成订单，根据租凭时间计算费用
    public BigDecimal completeOrder (LocalDateTime rentalEndTime){
        //在结算时，获取rentalEndTime

        if (this.orderStatus != OrderStatus.ACTIVE){
            throw new IllegalArgumentException("Only active orders can be completed");
        }

        this.rentalEndTime = rentalEndTime;

        return BigDecimal.ZERO;
    }

    public Long getRentalDurationMin(){
        if (rentalStartTime == null || rentalEndTime == null){
            throw new IllegalArgumentException("Rental start and end time connot be null");
//            return 0;
        }

        return Duration.between(rentalStartTime, rentalEndTime).toMinutes();
    }
}
