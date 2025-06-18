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

    //取消订单
    public void cancelOrder() {
        if (this.orderStatus != OrderStatus.ACTIVE){
            throw new IllegalArgumentException("Only active orders can be cancelled");
        }

        //-6.18
        //应该再加一个判定条件，只有前5分钟才可以取消订单
        if(this.getRentalDurationMin() >= 5){
            throw new IllegalArgumentException("Only  rental time less than 2 mins can be cancelled");
        }

        this.orderStatus = OrderStatus.CANCELLED;
        this.rentalEndTime = LocalDateTime.now();
        this.billedHours = 0;
        this.fee = BigDecimal.ZERO;
    }

    //计算租凭时长（分钟）
    public Long getRentalDurationMin(){
        if (rentalStartTime == null || rentalEndTime == null){
            throw new IllegalArgumentException("Rental start and end time connot be null");
        }

        return Duration.between(rentalStartTime, rentalEndTime).toMinutes();
    }

    //计算租凭时长（小时）
    public static int calculateBilledHours(Long min){
        if (min <= 0){
            return 1;
        }

        return (int) Math.ceil(min/60);
    }

    //计算电量消耗量
    public int calculateChargeLoss(){
        if (rentalStartTime == null || rentalEndTime == null){
            throw new IllegalArgumentException("Rental start and end time connot be null");
        }

        long min = getRentalDurationMin();
        return (int)Math.min(min, 100);
    }

    //计算订单归还时电量百分比
    public int getReturnChargePercentage(){
        int chargeLoss = calculateChargeLoss();
        int returnCharge = this.rentalStartChargePercentage -  chargeLoss;
        return Math.max(0, returnCharge);
    }

    //计算费用
    public BigDecimal calculateFee(Integer billedHours){
        return  new BigDecimal(billedHours).multiply(HOURLY_RATE);
    }

    //完成订单，根据租凭时间计算费用
    public BigDecimal completeOrder (LocalDateTime rentalEndTime){
        //在结算时，获取rentalEndTime

        if (this.orderStatus != OrderStatus.ACTIVE){
            throw new IllegalArgumentException("Only active orders can be completed");
        }

        this.rentalEndTime = rentalEndTime;
        long rentalDurationMin = getRentalDurationMin();
        this.billedHours = calculateBilledHours(rentalDurationMin);
        this.fee = calculateFee(billedHours);
        this.orderStatus = OrderStatus.COMPLETED;

        return this.fee;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null && getClass() != obj.getClass()) return false;
        RentalOrder other = (RentalOrder) obj;
        return Objects.equals(orderId, other.orderId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(orderId);
    }
}
