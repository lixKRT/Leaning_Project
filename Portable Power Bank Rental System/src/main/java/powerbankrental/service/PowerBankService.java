package powerbankrental.service;

import powerbankrental.model.PowerBank;
import powerbankrental.model.enums.PowerBankStatus;

import java.util.List;

public interface PowerBankService {
    boolean addPowerBank(PowerBank powerBank);
    PowerBank getPowerBankById(String powerBankId);
    List<PowerBank> getAllPowerBanks();
    List<PowerBank> getPowerBankByStatus(PowerBankStatus powerBankStatus);
    List<PowerBank> getAvailablePowerBanks();
    boolean updatePowerBank(PowerBank powerBank);
    boolean updatePowerBankStatus(String powerBankId, PowerBankStatus powerBankStatus);
    boolean updatePowerBankChargePercentage(String powerBankId, int chargePercentage);
    boolean deletePowerBank(String powerBankId);
    boolean isPowerBankAvailable(String powerBankId);
    String generatePowerBankId();
}
