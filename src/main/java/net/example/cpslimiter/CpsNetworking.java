package net.example.cpslimiter;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.*;

public class CpsNetworking {
    private static final String SERVER_URL = "http://185.152.93.222:8081";
    public static final Map<UUID, Integer> MOD_USERS_CPS = new ConcurrentHashMap<>();
    // Добавь это поле в начало класса CpsNetworking
public static boolean showCpsLocally = true;
    private static final HttpClient client = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(1))
            .build();
    
    private static final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    
    // Используем ConcurrentLinkedQueue - она быстрее для частого добавления/удаления
    private static final ConcurrentLinkedQueue<Long> clickTimeline = new ConcurrentLinkedQueue<>();
    
    // Храним ссылку на текущую задачу пинга, чтобы можно было её остановить
    private static ScheduledFuture<?> pingTask;

    public static void init() {
        // Обновляем список CPS всех игроков раз в секунду
        scheduler.scheduleAtFixedRate(CpsNetworking::updateUsersFromServer, 0, 1, TimeUnit.SECONDS);
    }

    // Внутри класса CpsNetworking замени эти два метода:

public static void addClick() {
    // Используем наносекунды для записи момента клика
    clickTimeline.add(System.nanoTime());
}

public static int getCurrentCps() {
    long now = System.nanoTime();
    // Очищаем клики старее 1 секунды
    clickTimeline.removeIf(t -> now - t > 1_000_000_000L);
    
    int realCps = clickTimeline.size();
    
    // Возвращаем либо реальный CPS, либо 20, если реальный выше.
    // Это ограничит и то, что видишь ты, и то, что летит на сервер.
    return Math.min(20, realCps);
}

    public static void startPinging(UUID uuid) {
        // Если уже пингуем - останавливаем старую задачу перед запуском новой
        stopPinging();

        pingTask = scheduler.scheduleAtFixedRate(() -> {
            try {
                int myCps = getCurrentCps();
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(SERVER_URL + "/ping/" + uuid.toString() + "/" + myCps))
                        .timeout(Duration.ofSeconds(1))
                        .GET().build();
                
                // Используем discardEntity, чтобы не плодить объекты ответов
                client.sendAsync(request, HttpResponse.BodyHandlers.discarding());
            } catch (Exception ignored) {}
        }, 0, 1, TimeUnit.SECONDS);
    }

    // Метод для остановки отправки данных (вызывать при DISCONNECT)
    public static void stopPinging() {
        if (pingTask != null && !pingTask.isCancelled()) {
            pingTask.cancel(false);
        }
    }

    private static void updateUsersFromServer() {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(SERVER_URL + "/get_users"))
                    .timeout(Duration.ofSeconds(1))
                    .GET().build();

            client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(HttpResponse::body)
                .thenAccept(body -> {
                    try {
                        JsonElement element = JsonParser.parseString(body);
                        if (element.isJsonObject()) {
                            JsonObject json = element.getAsJsonObject();
                            
                            // Вместо clear() + putAll создаем временную мапу, чтобы избежать мерцания
                            Map<UUID, Integer> newCpsData = new HashMap<>();
                            for (Map.Entry<String, JsonElement> entry : json.entrySet()) {
                                try {
                                    newCpsData.put(UUID.fromString(entry.getKey()), entry.getValue().getAsInt());
                                } catch (Exception ignored) {}
                            }
                            
                            // Обновляем основную мапу за одно действие (или через replace)
                            MOD_USERS_CPS.keySet().retainAll(newCpsData.keySet()); // Удаляем тех, кого нет в ответе
                            MOD_USERS_CPS.putAll(newCpsData); // Обновляем значения
                        }
                    } catch (Exception ignored) {}
                });
    
            } catch (Exception ignored) {}
            
    }

    
}