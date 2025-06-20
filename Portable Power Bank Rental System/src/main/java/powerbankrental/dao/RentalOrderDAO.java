package powerbankrental.dao;

import powerbankrental.model.RentalOrder;
import powerbankrental.model.enums.OrderStatus;

import java.math.BigDecimal;
import java.sql.Connection;
import java.time.LocalDateTime;
import java.util.List;

//��Ƹ�������ݷ��ʽӿ�-������Ƹ������ص����ݿ��������
public interface RentalOrderDAO {
    int addRentalOrder(RentalOrder rentalOrder);
    int addRentalOrderInTransaction(RentalOrder rentalOrder, Connection connection);

    RentalOrder getRentalOrderById(String orderId);
    List<RentalOrder> getAllRentalOrders();
    List<RentalOrder> getActiveRentalOrdersByUserId(Long userId);
    List<RentalOrder> getAllRentalOrdersByUserId(Long userId);
    List<RentalOrder> getRentalOrdersByPowerBank(String powerBank);
    List<RentalOrder> getRentalOrdersByStatus(OrderStatus orderStatus);

    boolean updateRentalOrder(RentalOrder rentalOrder);
    boolean updateRentalOrderStatus(String orderId, OrderStatus orderStatus);
    boolean updateRentalOrderReturn(String orderId, LocalDateTime endTime, int billedHours,
                                    BigDecimal fee, OrderStatus status);
    boolean updateOrderReturnInTransaction(String orderId, LocalDateTime emdTime
            , int billedHours, BigDecimal fee,OrderStatus status, Connection connection);
}
