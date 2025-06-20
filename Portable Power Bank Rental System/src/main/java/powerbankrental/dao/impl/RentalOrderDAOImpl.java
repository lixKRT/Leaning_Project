package powerbankrental.dao.impl;

import powerbankrental.dao.RentalOrderDAO;
import powerbankrental.model.RentalOrder;
import powerbankrental.model.enums.OrderStatus;
import powerbankrental.util.DatabaseUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

//租聘订单数据访问接口实现类-实现所有与租聘订单相关的数据库操作
public class RentalOrderDAOImpl implements RentalOrderDAO {
    private static final Logger logger = LoggerFactory.getLogger(RentalOrderDAOImpl.class);
    //创造新的租聘订单
    @Override
    public int addRentalOrder(RentalOrder rentalOrder) {
        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;

        try {
            connection = DatabaseUtil.getConnection();

            String sql = "INSERT INTO rental_orders (user_id, powerbank_id, rental_start_time, " +
                    "rental_start_charge_percentage, order_status) VALUES (?, ?, ?, ?, ?)";
            statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            statement.setLong(1, rentalOrder.getUserId());
            statement.setString(2, rentalOrder.getPowerBankId());
            statement.setTimestamp(3, Timestamp.valueOf(rentalOrder.getRentalStartTime()));
            statement.setInt(4, rentalOrder.getReturnChargePercentage());
            statement.setString(5, rentalOrder.getOrderStatus().name());

            int affectedRows = statement.executeUpdate();

            if (affectedRows > 0) {
                logger.info("RentalOrder added successfully");
            }else  {
                logger.error("Failed to add RentalOrder");
                return -1;
            }

            //获取自动生成的订单ID
            resultSet = statement.getGeneratedKeys();
            if (resultSet.next()){
                int orderId = resultSet.getInt(1);
                logger.info("RentalOrder added successfully, orderId: " + orderId);
                return orderId;
            }else  {
                logger.error("Failed to add RentalOrder, orderId cannot be created");
                return -1;
            }
        }catch (SQLException e){
            logger.warn("Failed to add RentalOrder", e);
            return -1;
        }finally {
            DatabaseUtil.closeConnection(connection, statement, resultSet);
        }
    }

    //在事务中创建订单
    @Override
    public int addRentalOrderInTransaction(RentalOrder rentalOrder, Connection connection) {
        PreparedStatement statement = null;
        ResultSet resultSet = null;

        try{
            String sql = "INSERT INTO rental_orders (user_id, powerbank_id, rental_start_time, " +
                    "rental_start_charge_percentage, order_status) VALUES (?, ?, ?, ?, ?)";
            statement = connection.prepareStatement(sql, statement.RETURN_GENERATED_KEYS);
            statement.setLong(1, rentalOrder.getUserId());
            statement.setTimestamp(3, Timestamp.valueOf(rentalOrder.getRentalStartTime()));
            statement.setInt(4, rentalOrder.getRentalStartChargePercentage());
            statement.setString(5, rentalOrder.getOrderStatus().name());

            int affectedRows = statement.executeUpdate();

            if (affectedRows > 0) {
                logger.info("RentalOrder added successfully");
            }else {
                logger.error("Failed to add RentalOrder");
                return -1;
            }

            resultSet = statement.getGeneratedKeys();

            if (resultSet.next()){
                int orderId = resultSet.getInt(1);
                logger.info("RentalOrder added successfully, orderId: " + orderId);
                return orderId;
            }else {
                logger.error("Failed to add RentalOrder, orderId cannot be created");
                return -1;
            }
        }catch (SQLException e){
            logger.warn("Failed to add RentalOrder", e);
            return -1;
        }finally {
            DatabaseUtil.closeConnection(statement, resultSet);
        }
    }

    @Override
    public RentalOrder getRentalOrderById(String orderId) {
        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;

        try{
            connection = DatabaseUtil.getConnection();

            String sql = "SELECT * FROM rental_orders WHERE order_id = ?";
            statement = connection.prepareStatement(sql);
            statement.setString(1, orderId);

            resultSet = statement.executeQuery();

            if (resultSet.next()){
                return extractRentalOrderFromResultSet(resultSet);
            }else {
                logger.error("Failed to get RentalOrder with orderId: " + orderId);
                return null;
            }
        }catch (SQLException e){
            logger.warn("Failed to get RentalOrder with orderId: " + orderId, e);
            return null;
        }finally {
            DatabaseUtil.closeConnection(connection, statement, resultSet);
        }
    }

    public List<RentalOrder> getAllRentalOrders() {
        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        List<RentalOrder> rentalOrders = new ArrayList<>();

        try{
            connection = DatabaseUtil.getConnection();

            String sql = "SELECT * FROM rental_orders";
            statement = connection.prepareStatement(sql);

            resultSet = statement.executeQuery();

            while (resultSet.next()){
                RentalOrder rentalOrder = extractRentalOrderFromResultSet(resultSet);
                rentalOrders.add(rentalOrder);
            }
            logger.info("getAllRentalOrders successfully,nember is " + rentalOrders.size());
            return rentalOrders;
        }catch (SQLException e){
            logger.warn("Failed to get RentalOrders", e);
            return null;
        }finally {
            DatabaseUtil.closeConnection(connection, statement, resultSet);
        }
    }

    @Override
    public List<RentalOrder> getAllRentalOrdersByUserId(Long userId) {
        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        List<RentalOrder> rentalOrders = new ArrayList<>();

        try{
            connection = DatabaseUtil.getConnection();

            String sql = "SELECT * FROM rental_orders WHERE user_id = ?";
            statement = connection.prepareStatement(sql);
            statement.setLong(1, userId);

            resultSet = statement.executeQuery();

            while (resultSet.next()){
                RentalOrder order = extractRentalOrderFromResultSet(resultSet);
                rentalOrders.add(order);
            }
            logger.info("getAllRentalOrdersByUserId successfully,nember is " + rentalOrders.size());
            return rentalOrders;
        }catch (SQLException e){
            logger.warn("Failed to get RentalOrders", e);
            return null;
        }finally {
            DatabaseUtil.closeConnection(connection, statement, resultSet);
        }
    }

    @Override
    public List<RentalOrder> getActiveRentalOrdersByUserId(Long userId) {
        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        List<RentalOrder> rentalOrders = new ArrayList<>();

        try{
            connection = DatabaseUtil.getConnection();

            String sql = "SELECT * FROM rental_orders WHERE user_id = ? AND order_status = ? " +
                    "ORDER BY rental_start_time DESC";
            statement = connection.prepareStatement(sql);
            statement.setLong(1, userId);
            statement.setString(2, OrderStatus.ACTIVE.name());

            resultSet = statement.executeQuery();

            while (resultSet.next()){
                RentalOrder order = extractRentalOrderFromResultSet(resultSet);
                rentalOrders.add(order);
            }
            logger.info("getActiveRentalOrdersByUserId successfully,nember is " + rentalOrders.size());
            return rentalOrders;
        }catch (SQLException e){
            logger.warn("getActiveRentalOrdersByUserId failed");
            return null;
        }finally {
            DatabaseUtil.closeConnection(connection, statement, resultSet);
        }
    }

    @Override
    public List<RentalOrder> getRentalOrdersByPowerBank(String powerBank) {
        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        List<RentalOrder> rentalOrders = new ArrayList<>();

        try{
            connection = DatabaseUtil.getConnection();

            String sql = "SELECT * FROM rental_orders WHERE powerbank_id = ?";
            statement = connection.prepareStatement(sql);
            statement.setString(1, powerBank);

            resultSet = statement.executeQuery();

            while (resultSet.next()){
                RentalOrder order = extractRentalOrderFromResultSet(resultSet);
                rentalOrders.add(order);
            }
            logger.info("getRentalOrdersByPowerBank successfully,nember is " + rentalOrders.size());
            return rentalOrders;
        }catch (SQLException e){
            logger.warn("Failed to get RentalOrders", e);
            return null;
        }finally {
            DatabaseUtil.closeConnection(connection, statement, resultSet);
        }
    }

    @Override
    public List<RentalOrder> getRentalOrdersByStatus(OrderStatus orderStatus) {
        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        List<RentalOrder> rentalOrders = new ArrayList<>();

        try{
            connection = DatabaseUtil.getConnection();

            String sql = "SELECT * FROM rental_orders WHERE order_status = ?";
            statement = connection.prepareStatement(sql);
            statement.setString(1, orderStatus.name());

            resultSet = statement.executeQuery();

            while (resultSet.next()){
                RentalOrder order  = extractRentalOrderFromResultSet(resultSet);
                rentalOrders.add(order);
            }
            logger.info("getRentalOrdersByStatus successfully,nember is " + rentalOrders.size());
            return rentalOrders;
        }catch (SQLException e){
            logger.warn("Failed to get RentalOrders", e);
            return null;
        }finally {
            DatabaseUtil.closeConnection(connection, statement, resultSet);
        }
    }

    public boolean updateRentalOrder(RentalOrder rentalOrder) {
        Connection connection = null;
        PreparedStatement statement = null;

        try{
            connection = DatabaseUtil.getConnection();

            String sql = "UPDATE rental_orders SET user_id = ?, powerbank_id = ?, " +
                    "rental_start_time = ?, rental_start_charge_percentage = ?, " +
                    "rental_end_time = ?, billed_hours = ?, fee = ?, order_status = ? " +
                    "WHERE order_id = ?";
            statement = connection.prepareStatement(sql);
            statement.setLong(1, rentalOrder.getUserId());
            statement.setString(2, rentalOrder.getPowerBankId());
            statement.setTimestamp(3, Timestamp.valueOf(rentalOrder.getRentalStartTime()));
            statement.setInt(4, rentalOrder.getRentalStartChargePercentage());

            if (rentalOrder.getRentalEndTime() != null) {
                statement.setTimestamp(5, Timestamp.valueOf(rentalOrder.getRentalEndTime()));
            }else {
                statement.setNull(5, Types.TIMESTAMP);
            }

            if (rentalOrder.getBilledHours() > 0) {
                statement.setInt(6, rentalOrder.getBilledHours());
            }else {
                statement.setNull(6, Types.INTEGER);
            }

            if (rentalOrder.getFee().intValue() > 0) {
                statement.setBigDecimal(7, rentalOrder.getFee());
            }else {
                statement.setNull(7, Types.DECIMAL);
            }

            statement.setString(8, rentalOrder.getOrderStatus().name());
            statement.setString(9, rentalOrder.getOrderId());

            //执行更新
            int affectedRows = statement.executeUpdate();

            if (affectedRows > 0) {
                logger.info("updateRentalOrder successfully,ID is " + rentalOrder.getOrderId());
                return true;
            }else {
                logger.warn("updateRentalOrder failed,ID is " + rentalOrder.getOrderId());
                return false;
            }
        }catch (SQLException e){
            logger.warn("Failed to update RentalOrder", e);
            return false;
        }finally {
            DatabaseUtil.closeConnection(connection, statement);
        }
    }

    @Override
    public boolean updateRentalOrderStatus(String orderId, OrderStatus orderStatus) {
        Connection connection = null;
        PreparedStatement statement = null;

        try{
            connection = DatabaseUtil.getConnection();

            String sql  = "UPDATE rental_orders SET order_status = ? WHERE order_id = ?";
            statement = connection.prepareStatement(sql);
            statement.setString(1, orderId);
            statement.setString(2, orderStatus.name());

            int affectedRows = statement.executeUpdate();

            if (affectedRows > 0) {
                logger.info("updateRentalOrderStatus successfully,ID is " + orderId);
                return true;
            }else {
                logger.warn("updateRentalOrderStatus failed,ID is " + orderId);
                return false;
            }
        }catch (SQLException e){
            logger.warn("Failed to update RentalOrder", e);
            return false;
        }finally {
            DatabaseUtil.closeConnection(connection, statement);
        }
    }

    @Override
    public boolean updateRentalOrderReturn(String orderId, LocalDateTime endTime, int billedHours, BigDecimal fee, OrderStatus status) {
        Connection connection = null;
        PreparedStatement statement = null;

        try{
            connection = DatabaseUtil.getConnection();

            String sql = "UPDATE rental_orders SET rental_end_time = ?, billed_hours = ?, " +
                    "fee = ?, order_status = ? WHERE order_id = ?";
            statement = connection.prepareStatement(sql);
            statement.setTimestamp(1, Timestamp.valueOf(endTime));
            statement.setInt(2, billedHours);
            statement.setBigDecimal(3, fee);
            statement.setString(4, status.name());
            statement.setString(5, orderId);

            int affectedRows = statement.executeUpdate();

            if (affectedRows > 0) {
                logger.info("updateRentalOrderReturn successfully,ID is " + orderId);
                return true;
            }else  {
                logger.warn("updateRentalOrderReturn failed,ID is " + orderId);
                return false;
            }
        }catch (SQLException e){
            logger.warn("Failed to update RentalOrder", e);
            return false;
        }finally {
            DatabaseUtil.closeConnection(connection, statement);
        }
    }

    @Override
    public boolean updateOrderReturnInTransaction(String orderId, LocalDateTime emdTime
            , int billedHours, BigDecimal fee, OrderStatus status, Connection connection) {
        PreparedStatement statement = null;

        try {
            connection = DatabaseUtil.getConnection();

            String sql = "UPDATE rental_orders SET rental_end_time = ?, billed_hours = ?, " +
                    "fee = ?, order_status = ? WHERE order_id = ?";
            statement = connection.prepareStatement(sql);
            statement.setTimestamp(1, Timestamp.valueOf(emdTime));
            statement.setInt(2, billedHours);
            statement.setBigDecimal(3, fee);
            statement.setString(4, status.name());
            statement.setString(5, orderId);

            int affectedRows = statement.executeUpdate();
            if (affectedRows > 0) {
                logger.info("updateOrderReturnInTransaction successfully,ID is " + orderId);
                return true;
            }else{
                logger.warn("updateOrderReturnInTransaction failed,ID is " + orderId);
                return false;
            }
        }catch (SQLException e){
            logger.warn("Failed to update RentalOrder", e);
            return false;
        }finally {
            DatabaseUtil.closeConnection(connection, statement);
        }
    }

    //辅助方法
    public RentalOrder extractRentalOrderFromResultSet (ResultSet resultSet) throws SQLException {
        RentalOrder order = new RentalOrder();

        order.setOrderId(resultSet.getString("order_id"));
        order.setUserId(resultSet.getLong("user_id"));
        order.setPowerBankId(resultSet.getString("powerbank_id"));

        Timestamp startTime = resultSet.getTimestamp("rental_start_time");
        if (startTime != null ) {
            order.setRentalStartTime(startTime.toLocalDateTime());
        }

        order.setRentalStartChargePercentage(resultSet.getInt("rental_start_charge_percentage"));

        Timestamp endTime = resultSet.getTimestamp("rental_start_time");
        if (endTime != null ) {
            order.setRentalEndTime(endTime.toLocalDateTime());
        }

        order.setBilledHours(resultSet.getInt("billed_hours"));
        order.setFee(resultSet.getBigDecimal("fee"));
        order.setOrderStatus(OrderStatus.valueOf(resultSet.getString("order_status")));

        return order;
    }
}
