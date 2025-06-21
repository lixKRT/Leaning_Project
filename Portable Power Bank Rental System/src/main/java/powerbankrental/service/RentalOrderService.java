package powerbankrental.service;

import powerbankrental.model.RentalOrder;
import  powerbankrental.model.enums.OrderStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public interface RentalOrderService {
    RentalOrder  rentPowerBank(long userId, String powerBankId);
    RentalOrder returnPowerBank( long userId);
    boolean cancelOrder(long orderId);
    BigDecimal calculateRentalFee(RentalOrder rentalOrder);
    double calculateRentalDuration(LocalDateTime startDateTime, LocalDateTime endDateTime);
    RentalOrder getOrderById(long orderId);
    List<RentalOrder> getOrdersByUserId(long userId);
    List<RentalOrder> getOrdersByPowerBankId(String powerBankId);
    List<RentalOrder> getOrdersByStatus(OrderStatus orderStatus);
    List<RentalOrder> getAllOrders();
    boolean hasActiveOrder(long userId);
    boolean isPowerBankRented(String powerBankId);
}
