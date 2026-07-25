package com.musicdreamer.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.musicdreamer.entity.Notification;
import com.musicdreamer.mapper.NotificationMapper;
import com.musicdreamer.service.NotificationService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class NotificationServiceImpl
        extends ServiceImpl<NotificationMapper, Notification>
        implements NotificationService {

    @Override
    public void sendNotification(Long userId, String title, String content, Integer type) {
        Notification n = new Notification();
        n.setUserId(userId);
        n.setTitle(title);
        n.setContent(content);
        n.setType(type == null ? 1 : type);
        n.setIsRead(0);
        n.setCreateTime(LocalDateTime.now());
        save(n);
    }

    @Override
    public void sendBatch(List<Long> userIds, String title, String content, Integer type) {
        for (Long uid : userIds) {
            sendNotification(uid, title, content, type);
        }
    }

    @Override
    public void markAsRead(Long notificationId, Long userId) {
        Notification n = getById(notificationId);
        if (n != null && n.getUserId().equals(userId)) {
            n.setIsRead(1);
            updateById(n);
        }
    }

    @Override
    public List<Notification> getUnread(Long userId) {
        return lambdaQuery()
                .eq(Notification::getUserId, userId)
                .orderByDesc(Notification::getCreateTime)
                .list();
    }
}
