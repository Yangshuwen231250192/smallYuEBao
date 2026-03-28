package com.example.yuebao.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 账户数据模型
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AccountVO {
    private BigDecimal balance;      // 余额账户
    private BigDecimal yuebao;       // 余额宝资产
}
