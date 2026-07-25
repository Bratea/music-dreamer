package com.musicdreamer.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.musicdreamer.entity.Notification;

import java.util.List;

public interface NotificationService extends IService<Notification> {
    /**
     * 发送通知
     */
    void sendNotification(Long userId, String title, String content, Integer type);

    /**
     * 批量发送通知
     */
    void sendBatch(List<Long> userIds, String title, String content, Integer type);

    /**
     * 标记已读
     */
    void markAsRead(Long notificationId, Long userId);

    /**
     * 获取用户未读通知
     */
    List<Notification> getUnread(Long userId);
}
