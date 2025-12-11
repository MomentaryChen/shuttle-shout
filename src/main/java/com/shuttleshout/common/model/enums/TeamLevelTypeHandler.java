package com.shuttleshout.common.model.enums;

import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedJdbcTypes;
import org.apache.ibatis.type.MappedTypes;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * TeamLevel 枚舉類型處理器
 * 用於 MyBatis-Flex 中枚舉與數據庫字符串之間的轉換
 * 數據庫存儲枚舉的顯示名稱（中文），如 "新手"、"初階" 等
 * 
 * @author ShuttleShout Team
 */
@MappedTypes(TeamLevel.class)
@MappedJdbcTypes(JdbcType.VARCHAR)
public class TeamLevelTypeHandler extends BaseTypeHandler<TeamLevel> {

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, TeamLevel parameter, JdbcType jdbcType) throws SQLException {
        // 將枚舉的顯示名稱存儲到數據庫
        ps.setString(i, parameter.getDisplayName());
    }

    @Override
    public TeamLevel getNullableResult(ResultSet rs, String columnName) throws SQLException {
        String value = rs.getString(columnName);
        return convertToEnum(value);
    }

    @Override
    public TeamLevel getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        String value = rs.getString(columnIndex);
        return convertToEnum(value);
    }

    @Override
    public TeamLevel getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        String value = cs.getString(columnIndex);
        return convertToEnum(value);
    }

    /**
     * 將數據庫字符串轉換為枚舉值
     * 支援顯示名稱和枚舉名稱兩種格式
     * 
     * @param value 數據庫中的值
     * @return 對應的枚舉值，如果未找到則返回 null
     */
    private TeamLevel convertToEnum(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        
        // 首先嘗試通過顯示名稱匹配
        TeamLevel level = TeamLevel.fromDisplayName(value);
        if (level != null) {
            return level;
        }
        
        // 如果顯示名稱匹配失敗，嘗試通過枚舉名稱匹配（兼容舊數據）
        level = TeamLevel.fromName(value);
        if (level != null) {
            return level;
        }
        
        // 如果都匹配失敗，返回 null
        return null;
    }
}

