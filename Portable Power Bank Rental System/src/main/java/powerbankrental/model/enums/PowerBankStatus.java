package powerbankrental.model.enums;

public enum PowerBankStatus {
    //用于枚举电源状态。
    // 部分用不到，但还是写下，模拟真实环境
    AVAILABLE,
    RENTED,
    LOW_BATTERY,//低电量
    MAINTENANCE,//维护中
    CHARGING,//充电中
    UNAVAILABLE,//不可用
    DISCARDED;//报废，丢弃
}
