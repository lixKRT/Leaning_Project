package powerbankrental.model;

import powerbankrental.model.enums.OrderStatus;

import java.time.Duration;
import java.time.LocalDateTime;
import java.math.BigDecimal;
import java.util.Objects;

//��ƾ����ʵ����
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
    //�������캯�����������ݿ��������
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

    //ȡ������
    public void cancelOrder() {
        if (this.orderStatus != OrderStatus.ACTIVE){
            throw new IllegalArgumentException("Only active orders can be cancelled");
        }

        //-6.18
        //Ӧ���ټ�һ���ж�������ֻ��ǰ5���Ӳſ���ȡ������
        if(this.getRentalDurationMin() >= 5){
            throw new IllegalArgumentException("Only  rental time less than 2 mins can be cancelled");
        }

        this.orderStatus = OrderStatus.CANCELLED;
        this.rentalEndTime = LocalDateTime.now();
        this.billedHours = 0;
        this.fee = BigDecimal.ZERO;
    }

    //������ƾʱ�������ӣ�
    public Long getRentalDurationMin(){
        if (rentalStartTime == null || rentalEndTime == null){
            throw new IllegalArgumentException("Rental start and end time connot be null");
        }

        return Duration.between(rentalStartTime, rentalEndTime).toMinutes();
    }

    //������ƾʱ����Сʱ��
    public static int calculateBilledHours(Long min){
        if (min <= 0){
            return 1;
        }

        return (int) Math.ceil(min/60);
    }

    //�������������
    public int calculateChargeLoss(){
        if (rentalStartTime == null || rentalEndTime == null){
            throw new IllegalArgumentException("Rental start and end time connot be null");
        }

        long min = getRentalDurationMin();
        return (int)Math.min(min, 100);
    }

    //���㶩���黹ʱ�����ٷֱ�
    public int getReturnChargePercentage(){
        int chargeLoss = calculateChargeLoss();
        int returnCharge = this.rentalStartChargePercentage -  chargeLoss;
        return Math.max(0, returnCharge);
    }

    //�������
    public BigDecimal calculateFee(Integer billedHours){
        return  new BigDecimal(billedHours).multiply(HOURLY_RATE);
    }

    //��ɶ�����������ƾʱ��������
    public BigDecimal completeOrder (LocalDateTime rentalEndTime){
        //�ڽ���ʱ����ȡrentalEndTime

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
