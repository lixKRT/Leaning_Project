package powerbankrental.dao;

import powerbankrental.model.PowerBank;
import powerbankrental.model.enums.PowerBankStatus;

import java.sql.Connection;
import java.util.List;

//��籦���ݷ��ʽӿ�-�����籦��ص����ݿ��������
public interface PowerBankDAO {
    boolean addPowerBank(PowerBank powerBank);
    boolean deletePowerBank(PowerBank powerBank);

    PowerBank getPowerBankById(String id);
    List<PowerBank> getAllPowerBanks();
    List<PowerBank> getAllPowerBanksByStatus(PowerBankStatus status);
    List<PowerBank> getAllAvailablePowerBanksByStatus(PowerBankStatus status);

    boolean updatePowerBank(PowerBank powerBank);//���³�籦��Ϣ
    boolean updatePowerBankStatus(String id, PowerBankStatus powerBankStatus);
    boolean updatePowerBankCharge(String id, int charge);
    //�������и��³�籦״̬�͵���
    boolean updatePowerBankRentalInTransaction(String id, PowerBank powerBank, int charge, Connection connection);
}
