package powerbankrental.model;

import  powerbankrental.model.enums.PowerBankStatus;
import java.time.LocalDateTime;
import java.util.Objects;

//充电宝实体类
public class PowerBank {
    private String powerBankId;
    private String model;
    private int initialCapacityMah;//设计容量
    private int currentCapacityPercentage;//当前电量百分比
    private PowerBankStatus status;
    private LocalDateTime addedDate;
    private String locationDescription;//估计也用不上
    private int totalChargingDurationMinutes;//累计充电时间（分钟），预计用不着
    private int totalRentalCount;

    public  PowerBank() {
        this.addedDate = LocalDateTime.now();
        this.totalChargingDurationMinutes = 0;
        this.totalRentalCount = 0;
        this.status = PowerBankStatus.AVAILABLE;
        this.currentCapacityPercentage = 100;
        this.model = "lolicami";
        this.locationDescription = "Warehouse";
    }
    public PowerBank(String powerBankId, String model, int initialCapacityMah) {
        this();
        this.powerBankId = powerBankId;
        this.model = model;
        this.initialCapacityMah = initialCapacityMah;
    }
    //用于数据库加载数据
    public PowerBank(String powerBankId, String model, int initialCapacityMah,
                     int currentCapacityPercentage, PowerBankStatus status, LocalDateTime addedDate,
                     String locationDescription, int totalChargingDurationMinutes, int totalRentalCount) {
        this.powerBankId = powerBankId;
        this.model = model;
        this.initialCapacityMah = initialCapacityMah;
        this.currentCapacityPercentage = currentCapacityPercentage;
        this.status = status;
        this.addedDate = addedDate;
        this.locationDescription = locationDescription;
        this.totalChargingDurationMinutes = totalChargingDurationMinutes;
        this.totalRentalCount = totalRentalCount;
    }

    public  String getPowerBankId() {
        return this.powerBankId;
    }
    public void setPowerBankId(String powerBankId) {
        this.powerBankId = powerBankId;
    }

    public  String getModel() {
        return this.model;
    }
    public void setModel(String model) {
        this.model = model;
    }

    public  int getInitialCapacityMah() {
        return this.initialCapacityMah;
    }
    public void setInitialCapacityMah(int initialCapacityMah) {
        this.initialCapacityMah = initialCapacityMah;
    }

    public  int getCurrentCapacityPercentage() {
        return this.currentCapacityPercentage;
    }
    public void setCurrentCapacityPercentage(int currentCapacityPercentage) {
        if(currentCapacityPercentage < 0 || currentCapacityPercentage > 100) {
            throw new IllegalArgumentException("Current capacity percentage must be between 0 and 100");
        }
        this.currentCapacityPercentage = currentCapacityPercentage;

/*        if(currentCapacityPercentage < 0 ) {
            this.currentCapacityPercentage = 0;
        }else if(currentCapacityPercentage > 100) {
            this.currentCapacityPercentage = 100;
        }else  {
            this.currentCapacityPercentage = currentCapacityPercentage;
        }*/
    }

    public  PowerBankStatus getStatus() {
        return this.status;
    }
    public void setStatus(PowerBankStatus status) {
        this.status = status;
    }

    public  LocalDateTime getAddedDate() {
        return this.addedDate;
    }
    public void setAddedDate(LocalDateTime addedDate) {
        this.addedDate = addedDate;
    }

    public  String getLocationDescription() {
        return this.locationDescription;
    }
    public void setLocationDescription(String locationDescription) {
        this.locationDescription = locationDescription;
    }

    public  int getTotalChargingDurationMinutes() {
        return this.totalChargingDurationMinutes;
    }
    public void setTotalChargingDurationMinutes(int totalChargingDurationMinutes) {
        this.totalChargingDurationMinutes = totalChargingDurationMinutes;
    }

    public  int getTotalRentalCount() {
        return this.totalRentalCount;
    }
    public void setTotalRentalCount(int totalRentalCount) {
        this.totalRentalCount = totalRentalCount;
    }

    //增加租凭次数
    public void increaseTotalRentalCount() {
        this.totalRentalCount++;
    }
    //增加累计充电时长
    public void increaseTotalChargingDurationMinutes(int minutes) {
        if(minutes < 0) {
            throw new IllegalArgumentException("Minimum amount of minutes must be greater than zero");
        }

        if(minutes > 0) {
            this.totalChargingDurationMinutes += minutes;
        }
    }
    //更新电量并根据电量自动更新状态
    public void updateChargeAndStatus(int newChargePercentage) {
/*
        修改set抛出异常的方法（不抛出异常）||  使用集合对电量百分比做限制  ||  完善下列捕获异常方法
        选哪个？-6.17
        决定了，改set，让前面处理-6.18
        还是决定用throw了-6.18*/
/*        try{
            setCurrentCapacityPercentage(newChargePercentage);
        }catch(Exception e) {
            e.getMessage();
        }*/

        if (status != PowerBankStatus.UNAVAILABLE && status != PowerBankStatus.MAINTENANCE
                && status != PowerBankStatus.CHARGING && status != PowerBankStatus.DISCARDED) {
            if (newChargePercentage > 50) {
                this.status = PowerBankStatus.AVAILABLE;
            } else if (newChargePercentage < 50 && newChargePercentage > 20) {
                this.status = PowerBankStatus.LOW_BATTERY;
            } else {
                this.status = PowerBankStatus.UNAVAILABLE;
            }
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {return true;}
        if (obj == null || getClass() != obj.getClass()) {return false;}
        PowerBank powerBank = (PowerBank) obj;
        return Objects.equals(powerBankId, powerBank.powerBankId);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(powerBankId);
    }
}
