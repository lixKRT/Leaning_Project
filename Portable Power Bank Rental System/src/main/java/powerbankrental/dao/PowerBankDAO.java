package powerbankrental.dao;

import powerbankrental.model.PowerBank;
import powerbankrental.model.enums.PowerBankStatus;

import java.sql.Connection;
import java.util.List;

//充电宝数据访问接口-定义充电宝相关的数据库操作方法
public interface PowerBankDAO {
    boolean addPowerBank(PowerBank powerBank);
    boolean deletePowerBank(PowerBank powerBank);

    PowerBank getPowerBankById(String id);
    List<PowerBank> getAllPowerBanks();
    List<PowerBank> getAllPowerBanksByStatus(PowerBankStatus status);
    List<PowerBank> getAllAvailablePowerBanksByStatus(PowerBankStatus status);

    boolean updatePowerBank(PowerBank powerBank);//更新充电宝信息
    boolean updatePowerBankStatus(String id, PowerBankStatus powerBankStatus);
    boolean updatePowerBankCharge(String id, int charge);
    //在事务中更新充电宝状态和电量
    boolean updatePowerBankRentalInTransaction(String id, PowerBank powerBank, int charge, Connection connection);
}
