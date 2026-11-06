package org.example.controller;

import org.example.client.Limits;
import org.example.client.UserInfo;

import java.math.BigDecimal;

record Aggregate(UserInfo user, Limits limits) {
    static Aggregate fallback(String id) {
        return new Aggregate(new UserInfo(id, "unknown"), new Limits(id, BigDecimal.ZERO));
    }
}
