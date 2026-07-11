package com.example.shelfy.service;

import com.example.shelfy.model.PushSubscription;
import com.example.shelfy.model.ShelfyItem;
import com.example.shelfy.repository.PushSubscriptionRepository;
import com.example.shelfy.repository.ShelfyItemRepository;
import com.example.shelfy.repository.UserRepository;
import nl.martijndwars.webpush.Notification;
import nl.martijndwars.webpush.PushService;
import nl.martijndwars.webpush.Subscription;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.security.Security;
import java.time.LocalDate;
import java.util.List;

@Service
public class WebPushService {

    private static final Logger log = LoggerFactory.getLogger(WebPushService.class);

    private final PushSubscriptionRepository subscriptionRepository;
    private final ShelfyItemRepository itemRepository;
    private final UserRepository userRepository;

    @Value("${vapid.public.key:}")
    private String vapidPublicKey;

    @Value("${vapid.private.key:}")
    private String vapidPrivateKey;

    @Value("${vapid.subject:mailto:admin@shelfy.app}")
    private String vapidSubject;

    public WebPushService(PushSubscriptionRepository subscriptionRepository,
                          ShelfyItemRepository itemRepository,
                          UserRepository userRepository) {
        this.subscriptionRepository = subscriptionRepository;
        this.itemRepository = itemRepository;
        this.userRepository = userRepository;
        Security.addProvider(new BouncyCastleProvider());
    }

    // =============================================
    // 毎朝8時に期限アラートを送信
    // =============================================
    @Scheduled(cron = "0 0 8 * * *", zone = "Asia/Tokyo")
    public void sendExpiryAlerts() {
        if (vapidPublicKey == null || vapidPublicKey.isBlank()) return;

        LocalDate today = LocalDate.now();
        LocalDate in3Days = today.plusDays(3);
        LocalDate in7Days = today.plusDays(7);

        List<ShelfyItem> allItems = itemRepository.findAll();

        for (ShelfyItem item : allItems) {
            if (item.getExpiryDate() == null) continue;

            String message = null;

            if (item.getExpiryDate().isBefore(today)) {
                message = "【期限切れ】" + item.getName() + "の期限が切れています";
            } else if (!item.getExpiryDate().isAfter(in3Days)) {
                message = "【期限間近】" + item.getName() + "の期限まであと" + item.getDaysUntilExpiry() + "日です";
            } else if (!item.getExpiryDate().isAfter(in7Days)) {
                message = "【期限注意】" + item.getName() + "の期限まであと" + item.getDaysUntilExpiry() + "日です";
            }

            if (message != null) {
                sendToGroup(item.getGroupId(), "Shelfy アラート", message);
            }
        }
    }

    // =============================================
    // グループの全ユーザーに通知
    // =============================================
    public void sendToGroup(Long groupId, String title, String body) {
        List<PushSubscription> subs = subscriptionRepository.findAll().stream()
            .filter(s -> {
                try {
                    Long uid = s.getUserId();
                    return userRepository.findById(uid)
                        .map(u -> true).orElse(false);
                } catch (Exception e) {
                    return false;
                }
            }).toList();

        for (PushSubscription sub : subs) {
            sendNotification(sub, title, body);
        }
    }

    // =============================================
    // 単一デバイスに通知送信
    // =============================================
    public void sendNotification(PushSubscription sub, String title, String body) {
        try {
            PushService pushService = new PushService(vapidPublicKey, vapidPrivateKey, vapidSubject);

            String payload = "{\"title\":\"" + title + "\",\"body\":\"" + body + "\",\"icon\":\"/icons/icon-192.png\"}";

            Subscription subscription = new Subscription(
                sub.getEndpoint(),
                new Subscription.Keys(sub.getP256dh(), sub.getAuth())
            );

            pushService.send(new Notification(subscription, payload));
            log.info("プッシュ通知送信成功: {}", sub.getEndpoint().substring(0, 30));

        } catch (Exception e) {
            log.error("プッシュ通知送信失敗: {}", e.getMessage());
            if (e.getMessage() != null && e.getMessage().contains("410")) {
                subscriptionRepository.deleteByEndpoint(sub.getEndpoint());
                log.info("無効なサブスクリプションを削除: {}", sub.getEndpoint().substring(0, 30));
            }
        }
    }
}
