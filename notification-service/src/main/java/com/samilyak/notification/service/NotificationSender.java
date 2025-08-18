package com.samilyak.notification.service;

import com.samilyak.notification.dto.NotificationDto;

public interface NotificationSender {

    String getChannelType();

    void send(NotificationDto dto);

}
