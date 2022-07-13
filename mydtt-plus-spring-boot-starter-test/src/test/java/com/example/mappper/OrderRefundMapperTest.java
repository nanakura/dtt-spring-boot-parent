package com.example.mappper;

import cn.alphahub.dtt.plus.util.JacksonUtil;
import com.example.domain.dtt.DttMember;
import com.example.domain.order.OrderRefund;
import com.example.enums.MemberType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.util.List;

@SpringBootTest
class OrderRefundMapperTest {

    @Autowired
    OrderRefundMapper orderRefundMapper;

    @BeforeEach
    void setUp() {
    }

    @AfterEach
    void tearDown() {
    }


    @Test
    void selectList() {
        List<OrderRefund> refunds = orderRefundMapper.selectList(null);
        refunds.forEach( c -> System.out.println(JacksonUtil.toJson(c)));
    }

    @Test
    void insert() {

    }

}