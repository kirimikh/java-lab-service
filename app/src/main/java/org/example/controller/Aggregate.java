package org.example.controller;

import org.example.client.Limits;
import org.example.client.UserInfo;

record Aggregate(UserInfo user, Limits limits) {
}
