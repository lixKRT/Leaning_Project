package powerbankrental.dao;

import powerbankrental.model.RentalOrder;
import powerbankrental.model.User;
import powerbankrental.model.enums.OrderStatus;

import java.sql.Connection;
import java.time.LocalDateTime;
import java.util.List;

//��Ƹ�������ݷ��ʽӿ�-������Ƹ������ص����ݿ��������
public interface RentalOrderDAO {
    int addRentalOrder(RentalOrder rentalOrder);
    int addRentalOrderInTransaction(RentalOrder rentalOrder, Connection connection);

    RentalOrder getRentalOrderById(String id);
    List<RentalOrder> getAllRentalOrders();
    List<RentalOrder> getActiveRentalOrdersByUserId(int userId);
    List<RentalOrder> getAllRentalOrdersByUserId(int userId);
    List<RentalOrder> getRentalOrdersByPowerBank(String powerBank);
    List<RentalOrder> getRentalOrdersByStatus(OrderStatus orderStatus);

    boolean updateRentalOrder(RentalOrder rentalOrder);
    boolean updateRentalOrderStatus(String id, OrderStatus orderStatus);
    boolean updateRentalOrderReturn(int id, LocalDateTime emdTime, int billedHours,
                                    double fee,OrderStatus status);
    boolean updateOrderReturnInTransaction(int orderId, LocalDateTime emdTime
            , int billedHours, double fee,OrderStatus status, Connection connection);
}
