package powerbankrental.dao.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import powerbankrental.dao.PowerBankDAO;
import powerbankrental.model.PowerBank;
import powerbankrental.model.enums.PowerBankStatus;
import powerbankrental.util.DatabaseUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

//充电宝数据访问接口实现类-实现所有与充电宝相关的数据库操作
public class PowerBankDAOImpl implements PowerBankDAO {
    private static final Logger logger = LoggerFactory.getLogger(RentalOrderDAOImpl.class);

    //添加充电宝
    @Override
    public boolean addPowerBank(PowerBank powerBank) {
        Connection connection = null;
        PreparedStatement statement = null;

        try{
            //设立sql语句，设定参数，建立连接，最后执行，也可以一开始建立连接
            connection = DatabaseUtil.getConnection();

            String sql = "INSERT INTO power_banks (powerbank_id, model, initial_capacity_mah, current_charge_percentage, " +
                    "status, location_description, total_charging_duration_minutes, total_rental_count, added_date) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
            statement = connection.prepareStatement(sql);
            statement.setString(1, powerBank.getPowerBankId());
            statement.setString(2, powerBank.getModel());
            statement.setInt(3, powerBank.getInitialCapacityMah());
            statement.setInt(4, powerBank.getCurrentCapacityPercentage());
            statement.setString(5, powerBank.getStatus().name());
            statement.setString(6, powerBank.getLocationDescription());
            statement.setInt(7, powerBank.getTotalChargingDurationMinutes());
            statement.setInt(8, powerBank.getTotalRentalCount());
            statement.setTimestamp(9, Timestamp.valueOf(powerBank.getAddedDate()));

            int affectedRows = statement.executeUpdate();
            if (affectedRows > 0){
                logger.info("Successfully added power bank,ID:" + powerBank.getPowerBankId());
                return true;
            }else {
                logger.warn("Failed to add power bank,ID:" + powerBank.getPowerBankId());
                return false;
            }
        }catch (SQLException e){
            logger.error("Failed to add power bank,ID:" + powerBank.getPowerBankId(), e);
            return false;
        }finally {
            DatabaseUtil.closeConnection(connection, statement);
        }
    }

    //删除充电宝
    @Override
    public boolean deletePowerBank(PowerBank powerBank) {
        Connection connection = null;
        PreparedStatement statement = null;

        try {
            connection = DatabaseUtil.getConnection();

            String sql = "DELETE FROM power_banks WHERE powerbank_id = ?";

            statement = connection.prepareStatement(sql);
            statement.setString(1, powerBank.getPowerBankId());

            int affectedRows = statement.executeUpdate();

            if (affectedRows > 0){
                logger.info("Successfully deleted power bank,ID:" + powerBank.getPowerBankId());
                return true;
            }else{
                logger.warn("Failed to delete power bank,ID:" + powerBank.getPowerBankId());
                return false;
            }
        }catch (SQLException e){
            logger.error("Failed to delete power bank,ID:" + powerBank.getPowerBankId(), e);
            return false;
        }finally {
            DatabaseUtil.closeConnection(connection, statement);
        }
    }

    //通过ID获取充电宝信息
    @Override
    public PowerBank getPowerBankById(String powerBankId){
        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;

        try{
            connection = DatabaseUtil.getConnection();

            String sql = "SELECT * FROM power_banks WHERE powerbank_id = ?";

            statement = connection.prepareStatement(sql);
            statement.setString(1, powerBankId);

            resultSet = statement.executeQuery();

            if (resultSet.next()) {
                return extractPowerBankFromResultSet(resultSet);
            }else  {
                logger.warn("Failed to get power bank,ID:" + powerBankId);
                return null;
            }
        }catch (SQLException e){
            logger.error("Failed to get power bank,ID:" + powerBankId, e);
            return null;
        }finally {
            DatabaseUtil.closeConnection(connection, statement, resultSet);
        }
    }

    //获取所有的充电宝信息
    @Override
    public List<PowerBank> getAllPowerBanks(){
        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        List<PowerBank> powerBanks = new ArrayList<>();

        try{
            connection = DatabaseUtil.getConnection();

            String sql = "SELECT * FROM power_banks ORDER BY powerbank_id";

            statement = connection.prepareStatement(sql);

            resultSet = statement.executeQuery();

            while (resultSet.next()){
                PowerBank powerBank = extractPowerBankFromResultSet(resultSet);
                powerBanks.add(powerBank);
            }
            logger.info("Successfully retrieved {} power banks", powerBanks.size());
            return powerBanks;
        }catch (SQLException e){
            logger.error("Failed to get power banks",e);
            return null;
        }finally {
            DatabaseUtil.closeConnection(connection, statement, resultSet);
        }
    }

    //通过状态查询充电宝
    @Override
    public List<PowerBank> getAllPowerBanksByStatus(PowerBankStatus powerBankStatus){
        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        List<PowerBank> powerBanks = new ArrayList<>();

        try {
            connection = DatabaseUtil.getConnection();
            String sql = "SELECT * FROM power_banks WHERE status = ?";
            statement.setString(1, powerBankStatus.name());
            statement = connection.prepareStatement(sql);
            resultSet = statement.executeQuery();
            while (resultSet.next()){
                PowerBank powerBank = extractPowerBankFromResultSet(resultSet);
                powerBanks.add(powerBank);
            }
            logger.info("Successfully retrieved {} power banks", powerBanks.size());
            return powerBanks;
        }catch (SQLException e){
            logger.error("Failed to get power banks",e);
            return null;
        }finally {
            DatabaseUtil.closeConnection(connection, statement, resultSet);
        }
    }

    //获取所有可用充电宝
    @Override
    public List<PowerBank> getAllAvailablePowerBanks(){
        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        List<PowerBank> powerBanks = new ArrayList<>();

        try{
            connection = DatabaseUtil.getConnection();

            String sql = "SELECT * FROM power_banks WHERE status = ? ORDER BY powerbank_id";            //ORDER BY powerbank_id - 根据充电宝ID字段对结果进行排序
            statement = connection.prepareStatement(sql);
            statement.setString(1, PowerBankStatus.AVAILABLE.name());

            resultSet = statement.executeQuery();

            while (resultSet.next()){
                PowerBank powerBank = extractPowerBankFromResultSet(resultSet);
                powerBanks.add(powerBank);
            }
            logger.info("Successfully retrieved {} power banks", powerBanks.size());
            return powerBanks;
        }catch (SQLException e){
            logger.error("Failed to get power banks",e);
            return null;
        }finally {
            DatabaseUtil.closeConnection(connection, statement, resultSet);
        }
    }

    //更新充电宝信息
    public boolean updatePowerBank(PowerBank powerBank){
        Connection connection = null;
        PreparedStatement statement = null;

        try{
            connection = DatabaseUtil.getConnection();

            String sql = "UPDATE power_banks SET model = ?, initial_capacity_mah = ?, " +
                    "current_charge_percentage = ?, status = ?, location_description = ?, " +
                    "total_charging_duration_minutes = ?, total_rental_count = ? " +
                    "WHERE powerbank_id = ?";
            statement = connection.prepareStatement(sql);
            statement.setString(1, powerBank.getModel());
            statement.setInt(2, powerBank.getInitialCapacityMah());
            statement.setInt(3, powerBank.getCurrentCapacityPercentage());
            statement.setString(4, powerBank.getStatus().name());
            statement.setString(5, powerBank.getLocationDescription());
            statement.setInt(6, powerBank.getTotalChargingDurationMinutes());
            statement.setInt(7, powerBank.getTotalRentalCount());
            statement.setString(8, powerBank.getPowerBankId());

            int affectedRows = statement.executeUpdate();

            if (affectedRows > 0){
                logger.info("Successfully updated power bank,ID:" + powerBank.getPowerBankId());
                return true;
            }else{
                logger.warn("Failed to update power bank,ID:" + powerBank.getPowerBankId());
                return false;
            }
        }catch (SQLException e){
            logger.error("Failed to update power bank,ID:" + powerBank.getPowerBankId(), e);
            return false;
        }finally {
            DatabaseUtil.closeConnection(connection, statement);
        }
    }

    //更新充电宝状态
    public boolean updatePowerBankStatus(String powerBankId, PowerBankStatus powerBankStatus){
        Connection connection = null;
        PreparedStatement statement = null;

        try{
            connection = DatabaseUtil.getConnection();

            String sql = "UPDATE power_banks SET status = ? WHERE powerbank_id = ?";
            statement = connection.prepareStatement(sql);
            statement.setString(1, String.valueOf(powerBankStatus));
            statement.setString(2, powerBankId);

            int affectedRows = statement.executeUpdate();

            if (affectedRows > 0){
                logger.info("Successfully updated power bank,ID:" + powerBankId);
                return true;
            }else {
                logger.warn("Failed to update power bank,ID:" + powerBankId);
                return false;
            }
        }catch (SQLException e){
            logger.error("Failed to update power bank,ID:" + powerBankId, e);
            return false;
        }finally {
            DatabaseUtil.closeConnection(connection, statement);
        }
    }

    //更新充电宝电量
    public boolean updatePowerBankCharge(String powerBankId, int charge){
        Connection connection = null;
        PreparedStatement statement = null;

        try{
            connection = DatabaseUtil.getConnection();

            String sql = "UPDATE power_banks SET current_charge_percentage = ? WHERE powerbank_id = ?";
            statement = connection.prepareStatement(sql);
            statement.setInt(1, charge);
            statement.setString(2, powerBankId);

            int affectedRows = statement.executeUpdate();

            if (affectedRows > 0){
                logger.info("Successfully updated power bank,ID:" + powerBankId);
                return true;
            }else  {
                logger.warn("Failed to update power bank,ID:" + powerBankId);
                return false;
            }
        }catch (SQLException e){
            logger.error("Failed to update power bank,ID:" + powerBankId, e);
            return false;
        }finally {
            DatabaseUtil.closeConnection(connection, statement);
        }
    }

    //在事务中更新充电宝状态和电量
    public boolean updatePowerBankRentalInTransaction(PowerBank powerBank, int charge
            , PowerBankStatus powerBankStatus, Connection connection){
        PreparedStatement statement = null;

        try{//同另一个类，使用提供的链接，节省资源
            String sql = "UPDATE power_banks SET status = ?, current_charge_percentage = ? WHERE powerbank_id = ?";
            statement = connection.prepareStatement(sql);
            statement.setString(1, powerBankStatus.name());
            statement.setInt(2, charge);
            statement.setString(3, powerBank.getPowerBankId());

            int affectedRows = statement.executeUpdate();

            if (affectedRows > 0){
                logger.info("Successfully updated power bank,ID:" + powerBank.getPowerBankId());
                return true;
            }else {
                logger.warn("Failed to update power bank,ID:" + powerBank.getPowerBankId());
                return false;
            }
        }catch (SQLException e){
            logger.error("Failed to update power bank,ID:" + powerBank.getPowerBankId(), e);
            return false;
        }finally {
            DatabaseUtil.closeConnection(connection, statement);
        }
    }

    //辅助方法-从ResultSet中提取数据并创建一个PowerBank对象
    public PowerBank extractPowerBankFromResultSet(ResultSet resultSet)throws SQLException {
        PowerBank powerBank = new PowerBank();

        powerBank.setPowerBankId(resultSet.getString("powerBank_id"));
        powerBank.setModel(resultSet.getString("model"));
        powerBank.setInitialCapacityMah(resultSet.getInt("initial_capacity_mah"));
        powerBank.setCurrentCapacityPercentage(resultSet.getInt("current_capacity_percentage"));
        powerBank.setStatus(PowerBankStatus.valueOf(resultSet.getString("status")));
        powerBank.setLocationDescription(resultSet.getString("location_description"));
        powerBank.setTotalChargingDurationMinutes(resultSet.getInt("total_charging_duration_minutes"));
        powerBank.setTotalRentalCount(resultSet.getInt("total_rental_count"));
        Timestamp addedDate = resultSet.getTimestamp("added_date");
        if (addedDate == null) {
            powerBank.setAddedDate(addedDate.toLocalDateTime());
        }
        return powerBank;
    }

}
