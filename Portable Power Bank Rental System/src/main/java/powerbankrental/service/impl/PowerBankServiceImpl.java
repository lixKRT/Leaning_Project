package powerbankrental.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import powerbankrental.service.PowerBankService;
import powerbankrental.model.PowerBank;
import powerbankrental.model.enums.PowerBankStatus;
import powerbankrental.dao.PowerBankDAO;
import powerbankrental.dao.impl.PowerBankDAOImpl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

public class PowerBankServiceImpl implements PowerBankService {
    private static final Logger logger = LoggerFactory.getLogger(PowerBankServiceImpl.class);
    private final PowerBankDAO powerBankDAO;

    public PowerBankServiceImpl() {
        this.powerBankDAO = new PowerBankDAOImpl();
    }
    public PowerBankServiceImpl(PowerBankDAO powerBankDAO){
        this.powerBankDAO = powerBankDAO;
    }

    @Override
    public boolean addPowerBank(PowerBank powerBank) {
        try{
            if (powerBank == null){
                logger.error("Power bank is null");
                return false;
            }
            if (powerBank.getPowerBankId() == null || powerBank.getPowerBankId().isEmpty()){
                powerBank.setPowerBankId(generatePowerBankId());
            }
            if (powerBank.getModel() == null || powerBank.getModel().isEmpty()){
                powerBank.setModel("lolicami");
            }
            if (powerBank.getInitialCapacityMah() <= 0){
                logger.error("Initial Capacity Mah is less than 0");
                return false;
            }
            if (powerBank.getCurrentCapacityPercentage() <= 0 ||  powerBank.getCurrentCapacityPercentage() >= 100){
                logger.error("Current Capacity Percentage is bigger than 100 or less than 0");
                return false;
            }
            PowerBank existingPowerBankId = powerBankDAO.getPowerBankById(powerBank.getPowerBankId());
            if (existingPowerBankId != null){
                logger.error("Power bank already exists");
                return false;
            }

            //����Ĭ��ֵ
            if(powerBank.getStatus() == null){
                powerBank.setStatus(PowerBankStatus.AVAILABLE);
            }
            if (powerBank.getAddedDate() == null){
                powerBank.setAddedDate(LocalDateTime.now());
            }

            boolean success = this.powerBankDAO.addPowerBank(powerBank);
            if (success){
                logger.info("Power bank added successfully,ID is {}, model is {}", powerBank.getPowerBankId(),  powerBank.getModel());
            }else{
                logger.error("Power bank add failed");
            }
            return success;
        }catch (Exception e){
            logger.error("Error adding power bank",e);
            return false;
        }
    }

    @Override
    public PowerBank getPowerBankById(String powerBankId){
        try{
            if (powerBankId == null || powerBankId.isEmpty()){
                logger.error("Power bank id is null");
                return null;
            }

            PowerBank powerBank = powerBankDAO.getPowerBankById(powerBankId);
            if (powerBank == null){
                logger.error("Power bank not found");
            }
            return powerBank;
        }catch (Exception e){
            logger.error("Error getting power bank",e);
            return null;
        }
    }

    @Override
    public List<PowerBank> getAllPowerBanks() {
        try{
            List<PowerBank> powerBanks = powerBankDAO.getAllPowerBanks();
            logger.info("All power banks found,the number of powerbanks is {}",powerBanks.size());
            return powerBanks;
        }catch (Exception e){
            logger.error("Error getting all power banks",e);
            return null;
        }
    }

    @Override
    public List<PowerBank> getPowerBankByStatus(PowerBankStatus powerBankStatus) {
        try{
            if (powerBankStatus == null){
                logger.error("Power bank status is null");
                return null;
            }

            List<PowerBank> powerBanks = powerBankDAO.getAllPowerBanksByStatus(powerBankStatus);
            logger.info("All {} power banks found,the number of powerbanks is {}", powerBankStatus,powerBanks.size());
            return powerBanks;
        }catch (Exception e){
            logger.error("Error getting all power banks",e);
            return null;
        }
    }

    @Override
    public List<PowerBank> getAvailablePowerBanks() {
        try{
            List<PowerBank> availablePowerBanks = powerBankDAO.getAllPowerBanksByStatus(PowerBankStatus.AVAILABLE);

            //���˵����е�������ĳ�籦
            List<PowerBank> result = availablePowerBanks.stream()
                    .filter(pb -> pb.getCurrentCapacityPercentage() >= 50)
                    .collect(Collectors.toList());

            logger.info("Available power banks found,the number of powerbanks is {}", result.size());
            return result;
        }catch (Exception e){
            logger.error("Error getting available power banks",e);
            return null;
        }
    }

    @Override
    public boolean updatePowerBank(PowerBank powerBank) {
        try {
            if (powerBank == null){
                logger.error("Power bank is null");
                return false;
            }

            if (powerBank.getPowerBankId() == null || powerBank.getPowerBankId().isEmpty()){
                powerBank.setPowerBankId(generatePowerBankId());
            }
            if (powerBank.getModel() == null || powerBank.getModel().isEmpty()){
                powerBank.setModel("lolicami");
            }

            boolean success = this.powerBankDAO.updatePowerBank(powerBank);
            if (success){
                logger.info("Power bank updated successfully,ID is {}", powerBank.getPowerBankId());
            }else {
                logger.error("Power bank updated failed");
            }
            return success;
        }catch (Exception e){
            logger.error("Error updating power bank",e);
            return false;
        }
    }

    @Override
    public boolean updatePowerBankStatus(String powerBankId, PowerBankStatus powerBankStatus) {
        try{
            if (powerBankId == null || powerBankId.isEmpty()){
                logger.error("Power bank id is null");
                return false;
            }
            if (powerBankStatus == null){
                logger.error("Power bank status is null");
                return false;
            }

            PowerBank existingPowerBank = powerBankDAO.getPowerBankById(powerBankId);
            if (existingPowerBank == null){
                logger.error("Power bank not found");
                return false;
            }

            //ʹ�ø���������֤���״̬�ĺϷ���
            if (! isValidStatusTransition(existingPowerBank.getStatus(), powerBankStatus)){
                logger.error("Power bank status is invalid");
                return false;
            }

            //����״̬
            existingPowerBank.setStatus(powerBankStatus);
            boolean success = this.powerBankDAO.updatePowerBank(existingPowerBank);
            if (success){
                logger.info("Power bank updated successfully,ID is {}", powerBankId);
            }else  {
                logger.error("Power bank updated failed");
            }
            return success;
        }catch (Exception e){
            logger.error("Error updating power bank",e);
            return false;
        }
    }

    @Override
    public boolean updatePowerBankChargePercentage(String powerBankId, int chargePercentage) {
        try{
            if (powerBankId == null || powerBankId.isEmpty()){
                logger.error("Power bank id is null");
                return false;
            }

            if (chargePercentage < 0 || chargePercentage > 100){
                logger.error("Invalid charge percentage");
                return false;
            }

            PowerBank powerBank = powerBankDAO.getPowerBankById(powerBankId);
            if (powerBank == null){
                logger.error("Power bank not found");
                return false;
            }

            powerBank.setCurrentCapacityPercentage(chargePercentage);
            boolean success = this.powerBankDAO.updatePowerBank(powerBank);
            if (success){
                logger.info("Power bank updated successfully,ID is {}", powerBankId);
            }else {
                logger.error("Power bank updated failed");
            }
            return success;
        }catch (Exception e){
            logger.error("Error updating power bank",e);
            return false;
        }
    }

    @Override
    public boolean deletePowerBank(String powerBankId) {
        try {
            if (powerBankId == null || powerBankId.isEmpty()){
                logger.error("Power bank id is null");
                return false;
            }

            PowerBank powerBank = powerBankDAO.getPowerBankById(powerBankId);
            if (powerBank == null){
                logger.error("Power bank not found");
                return false;
            }

            if (powerBank.getStatus() == PowerBankStatus.RENTED){
                logger.info("Rented Power bank can not be delete.");
                return false;
            }

            boolean success = this.powerBankDAO.deletePowerBank(powerBank);
            if (success){
                logger.info("Power bank deleted successfully,ID is {}", powerBankId);
            }else  {
                logger.error("Power bank delete failed");
            }
            return success;
        }catch (Exception e){
            logger.error("Error deleting power bank",e);
            return false;
        }
    }

    @Override
    public boolean isPowerBankAvailable(String powerBankId) {
        try{
            if (powerBankId == null || powerBankId.isEmpty()){
                logger.error("Power bank id is null");
                return false;
            }

            PowerBank powerBank = powerBankDAO.getPowerBankById(powerBankId);
            if (powerBank == null){
                logger.error("Power bank not found");
                return false;
            }

            if (powerBank.getStatus() != PowerBankStatus.AVAILABLE){
                logger.error("Power bank status is not AVAILABLE");
                return false;
            }

            if (powerBank.getCurrentCapacityPercentage() < 20){
                logger.info("Current capacity percentage is less than 20");
                return false;
            }

            logger.info("Power bank can be rented.ID is {} , Status is {}, Current capacity is {}"
                    ,powerBankId,powerBank.getStatus(),powerBank.getCurrentCapacityPercentage());
            return true;
        }catch (Exception e){
            logger.error("Error checking power bank availability",e);
            return false;
        }
    }

    @Override
    public String generatePowerBankId() {
        String timestamp = String.valueOf(LocalDateTime.now());
        Random random = new Random();
        int randomInt = random.nextInt(99999);
        return timestamp + String.valueOf(randomInt);
    }

    //��������-��֤��籦״̬����ĺϷ���
    private boolean isValidStatusTransition(PowerBankStatus currentStatus, PowerBankStatus newStatus){
        if (currentStatus == newStatus){
            return true;
        }

        //����ҵ������������״̬���
        switch (currentStatus){
            case AVAILABLE:
                // ����״̬���Ա�Ϊ�������С�ά���С����𻵡��ѱ���
                return newStatus == PowerBankStatus.RENTED ||
                        newStatus == PowerBankStatus.MAINTENANCE ||
                        newStatus == PowerBankStatus.UNAVAILABLE ||
                        newStatus == PowerBankStatus.DISCARDED;

            case RENTED:
                // ������ֻ�ܱ�Ϊ�����á�ά���С����𻵡��ѱ���
                return newStatus == PowerBankStatus.AVAILABLE ||
                        newStatus == PowerBankStatus.MAINTENANCE ||
                        newStatus == PowerBankStatus.UNAVAILABLE ||
                        newStatus == PowerBankStatus.DISCARDED;

            case MAINTENANCE:
                // ά���п��Ա�Ϊ�����á����𻵡��ѱ���
                return newStatus == PowerBankStatus.AVAILABLE ||
                        newStatus == PowerBankStatus.UNAVAILABLE ||
                        newStatus == PowerBankStatus.DISCARDED;

            case UNAVAILABLE:
                // ���𻵿��Ա�Ϊ��ά���С��ѱ���
                return newStatus == PowerBankStatus.MAINTENANCE ||
                        newStatus == PowerBankStatus.DISCARDED;

            case DISCARDED:
                // �ѱ��ϲ��ܱ��Ϊ����״̬
                return false;

            default:
                return false;
        }
    }
}
