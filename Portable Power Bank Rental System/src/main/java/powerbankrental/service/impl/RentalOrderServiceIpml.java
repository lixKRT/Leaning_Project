package powerbankrental.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import powerbankrental.dao.RentalOrderDAO;
import powerbankrental.dao.UserDAO;
import powerbankrental.dao.impl.RentalOrderDAOImpl;
import powerbankrental.dao.impl.UserDAOImpl;
import powerbankrental.model.PowerBank;
import powerbankrental.model.RentalOrder;
import powerbankrental.model.User;
import powerbankrental.model.enums.OrderStatus;
import powerbankrental.model.enums.PowerBankStatus;
import powerbankrental.service.PowerBankService;
import powerbankrental.service.RentalOrderService;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

public class RentalOrderServiceIpml implements RentalOrderService {
    private static final Logger logger = LoggerFactory.getLogger(RentalOrderServiceIpml.class);

    private static final double HOURLY_RATE = 1.5 ;//每小时费用
    private static final double DAILY_RATE = 30;  //每天最高费用
    private static final double MINIMUM_FEE = 1.5;//最低收费
    private static final double FREE_MINUTES = 2;//免费分钟数

    //数据访问对象
    private final RentalOrderDAO rentalOrderDAO;
    private final UserDAO userDAO;
    private final PowerBankService  powerBankService;

    public RentalOrderServiceIpml() {
        this.rentalOrderDAO = new RentalOrderDAOImpl();
        this.userDAO = new UserDAOImpl();
        this.powerBankService = new PowerBankServiceImpl();
    }
    public RentalOrderServiceIpml(RentalOrderDAO rentalOrderDAO, UserDAO userDAO, PowerBankService powerBankService) {
        this.rentalOrderDAO = rentalOrderDAO;
        this.userDAO = userDAO;
        this.powerBankService = powerBankService;
    }

    @Override
    public RentalOrder rentPowerBank(long userId, String powerBankId) {
        try {
            //对用户各种各种验证
            User user = userDAO.getUserById(userId);
            if (user == null) {
                logger.warn("User with id " + userId + " not found");
                return null;
            }
            if (!user.isActive()) {
                logger.warn("User with id " + userId + " is not active");
                return null;
            }
            if (hasActiveOrder(userId)) {
                logger.warn("User with id " + userId + " is already active.");
                return null;
            }

            //验证充电宝
            PowerBank powerBank = powerBankService.getPowerBankById(powerBankId);
            if (powerBank == null) {
                logger.warn("PowerBank with id " + powerBankId + " not found");
                return null;
            }
            if (!powerBankService.isPowerBankAvailable(powerBankId)) {
                logger.warn("PowerBank with id " + powerBankId + " is not available");
                return null;
            }

            //终于开始创造订单了
            RentalOrder rentalOrder = new RentalOrder();
            rentalOrder.setUserId(userId);
            rentalOrder.setPowerBankId(powerBankId);
            rentalOrder.setRentalStartTime(LocalDateTime.now());
            rentalOrder.setOrderStatus(OrderStatus.ACTIVE);
            rentalOrder.setRentalStartChargePercentage(powerBank.getCurrentCapacityPercentage());

            //保存订单
            long orderId = rentalOrderDAO.addRentalOrder(rentalOrder);
            if (Math.toIntExact(orderId) <= 0){
                logger.warn("Order with id " + orderId + " not found");
                return null;
            }

            //更新充电宝状态为已租出
            boolean updateSuccess = powerBankService.updatePowerBankStatus(powerBankId, PowerBankStatus.RENTED);
            if (! updateSuccess) {
                logger.warn("Failed to update power bank status for order with id " + orderId + "try to delete order");
                rentalOrderDAO.deleteOrder(orderId);
                return null;
            }

            //设置订单ID并返回
            rentalOrder.setOrderId(orderId);
            logger.info("Order with id " + orderId + " updated successfully");
            return rentalOrder;
        }catch (Exception e){
            logger.error("Exception in RentalOrderServiceIpml.rentPowerBank", e);
            return null;
        }
    }

    @Override
    public RentalOrder returnPowerBank(long orderId) {
        try {
            RentalOrder rentalOrder = rentalOrderDAO.getRentalOrderById(orderId);
            if (rentalOrder == null) {
                logger.warn("RentalOrder with id " + orderId + " not found");
                return null;
            }
            //验证订单状态
            if (rentalOrder.getOrderStatus() != OrderStatus.ACTIVE){
                logger.warn("RentalOrder with id " + orderId + " is not active.");
                return null;
            }
            PowerBank powerBank = powerBankService.getPowerBankById(rentalOrder.getPowerBankId());
            if (powerBank == null) {
                logger.warn("PowerBank with id " + rentalOrder.getPowerBankId() + " not found");
                return null;
            }

            rentalOrder.setRentalEndTime(LocalDateTime.now());
            rentalOrder.setOrderStatus(OrderStatus.COMPLETED);
            rentalOrder.setRentalEndTime(LocalDateTime.now());

            BigDecimal fee = calculateRentalFee(rentalOrder);
            rentalOrder.setFee(fee);

            boolean updateSuccess = rentalOrderDAO.updateRentalOrder(rentalOrder);
            if (! updateSuccess) {
                logger.error("rentalOrder with id " + rentalOrder.getOrderId() + " is filled");
                return null;
            }

            boolean powerBankUpdateSuccess =  powerBankService.updatePowerBankStatus(powerBank.getPowerBankId(), PowerBankStatus.AVAILABLE);
            if (! powerBankUpdateSuccess) {
                logger.warn("PowerBank with id " + powerBank.getPowerBankId() + " can not set available");
            }

            logger.info("RentalOrder with id " + orderId + " updated successfully");
            return rentalOrder;
        }catch (Exception e){
            logger.error("Exception in RentalOrderServiceIpml.returnPowerBank", e);
            return null;
        }
    }

    @Override
    public boolean cancelOrder(long orderId) {
        try{
            RentalOrder rentalOrder = rentalOrderDAO.getRentalOrderById(orderId);
            if (rentalOrder == null) {
                logger.warn("RentalOrder with id " + orderId + " not found");
                return false;
            }
            if (rentalOrder.getOrderStatus() != OrderStatus.ACTIVE){
                logger.warn("RentalOrder with id " + orderId + " is not active.");
                return false;
            }
            LocalDateTime startDateTime = rentalOrder.getRentalStartTime();
            LocalDateTime endDateTime = rentalOrder.getRentalEndTime();

            long minutes = Duration.between(startDateTime, endDateTime).toMinutes();

            if (minutes <= 2) {
                logger.warn("RentalOrder with id " + orderId + " is filled");
                return false;
            }

            rentalOrder.setOrderStatus(OrderStatus.COMPLETED);
            rentalOrder.setRentalEndTime(LocalDateTime.now());

            boolean updateSuccess = rentalOrderDAO.updateRentalOrder(rentalOrder);
            if (! updateSuccess) {
                logger.warn("rentalOrder with id " + orderId + " is filled");
                return false;
            }

            boolean powerBankUpdateSuccess = powerBankService.updatePowerBankStatus(rentalOrder.getPowerBankId(), PowerBankStatus.AVAILABLE);
            if (! powerBankUpdateSuccess) {
                logger.warn("RentalOrder with id " + orderId + " updated successfully");
            }

            logger.info("RentalOrder with id " + orderId + " updated successfully");
            return true;
        }catch (Exception e){
            logger.error("Exception in RentalOrderServiceIpml.cancelOrder", e);
            return false;
        }
    }

    @Override
    public BigDecimal calculateRentalFee(RentalOrder rentalOrder) {
        if (rentalOrder.getRentalStartTime() == null) {
            return BigDecimal.ZERO;
        }

        LocalDateTime startDateTime = rentalOrder.getRentalStartTime();
        LocalDateTime endDateTime = rentalOrder.getRentalEndTime();

        double rentalDuration = calculateRentalDuration(startDateTime, endDateTime);
        long rentalMinutes = Duration.between(startDateTime, endDateTime).toMinutes();
        if (rentalDuration <= FREE_MINUTES){
            return BigDecimal.ZERO;
        }

        BigDecimal rentalFee = new BigDecimal(rentalDuration * HOURLY_RATE);
        //TODO-应用最低收费
        //TODO-应用日封费用

        return  rentalFee;
    }

    @Override
    public double calculateRentalDuration(LocalDateTime startDateTime, LocalDateTime endDateTime) {
        if (startDateTime == null || endDateTime == null) {
            return Double.MAX_VALUE;
        }

        Duration duration = Duration.between(startDateTime, endDateTime);
        return duration.toMinutes()/60.0;
    }

    @Override
    public RentalOrder getOrderById(long orderId) {
        try{
            return rentalOrderDAO.getRentalOrderById(orderId);
        }catch (Exception e){
            logger.error("Exception in RentalOrderServiceIpml.getOrderById", e);
            return null;
        }
    }

    @Override
    public List<RentalOrder> getOrdersByUserId(long userId) {
        try{
            return rentalOrderDAO.getAllRentalOrdersByUserId(userId);
        }catch (Exception e){
            logger.error("Exception in RentalOrderServiceIpml.getOrdersByUserId", e);
            return null;
        }
    }

    @Override
    public List<RentalOrder> getOrdersByPowerBankId(String powerBankId) {
        try{
            return rentalOrderDAO.getRentalOrdersByPowerBank(powerBankId);
        }catch (Exception e){
            logger.error("Exception in RentalOrderServiceIpml.getOrdersByPowerBankId", e);
            return null;
        }
    }

    @Override
    public List<RentalOrder> getOrdersByStatus(OrderStatus orderStatus) {
        try {
            return rentalOrderDAO.getRentalOrdersByStatus(orderStatus);
        }catch (Exception e){
            logger.error("Exception in RentalOrderServiceIpml.getOrdersByStatus", e);
            return null;
        }
    }

    @Override
    public List<RentalOrder> getAllOrders() {
        try{
            return rentalOrderDAO.getAllRentalOrders();
        }catch (Exception e){
            logger.error("Exception in RentalOrderServiceIpml.getAllOrders", e);
            return null;
        }
    }

    @Override
    public boolean hasActiveOrder(long userId) {
        try {
            List<RentalOrder> activeOrders = rentalOrderDAO.getActiveRentalOrdersByUserId(userId);
            return (!activeOrders.isEmpty());
        }catch (Exception e){
            logger.error("Exception in RentalOrderServiceIpml.hasActiveOrder", e);
            return false;
        }
    }

    @Override
    public boolean isPowerBankRented(String powerBankId) {
        try{
            if (powerBankId == null ||  powerBankId.isEmpty()) {
                logger.warn("powerBankId is null or powerBankId is empty");
                return false;
            }

            PowerBank powerBank = powerBankService.getPowerBankById(powerBankId);
            if (powerBank == null) {
                logger.warn("powerBankId is null or powerBankId is empty");
                return false;
            }

            return powerBank.getStatus() == PowerBankStatus.AVAILABLE;
        }catch (Exception e){
            logger.error("Exception in RentalOrderServiceIpml.isPowerBankRented", e);
            return false;
        }
    }
}
