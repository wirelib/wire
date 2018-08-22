package com.github.wire;

import com.github.wire.enums.MessageEntityType;
import com.github.wire.enums.UpdateType;
import com.github.wire.internal.ApiPoller;
import com.github.wire.internal.InMemoryDispatcher;
import com.github.wire.internal.UpdateDispatcher;
import com.github.wire.telegram.Telegram;
import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.reactivex.Observable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import okhttp3.OkHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.Update;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

public class Wire {

    private final static Logger logger = LoggerFactory.getLogger(Wire.class);

    private final Telegram telegram;
    private final UpdateDispatcher dispatcher;
    private final List<Middleware> middlewares = new ArrayList<>();

    public Wire(String token, String apiUrl) {
        final OkHttpClient client = new OkHttpClient();
        final Gson gson = gson();
        this.telegram = new Telegram(client, gson, apiUrl, new ApiPoller(client, gson, apiUrl));
        this.dispatcher = new InMemoryDispatcher(telegram);
    }

    public Wire(String token) {
        this(token, Defaults.TELEGRAM_API_URL + token);
    }

    private Gson gson() {
        return new GsonBuilder()
                .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
                .create();
    }

    public void use(Middleware... m) {
        middlewares.addAll(Arrays.asList(m));
    }

    public void use(Middleware m) {
        middlewares.add(m);
    }

    public Wire onCommand(String command, ContextCallback callback) {
        register("/" + command, callback);
        return this;
    }

    public Wire onStartCommand(ContextCallback callback) {
        return onCommand("start", callback);
    }

    public Wire on(UpdateType updateType, ContextCallback callback) {
        register(updateType, callback);
        return this;
    }

    public Wire onText(Pattern regex, ContextCallback callback) {
        register(regex, callback);
        return this;
    }

    public Wire onText(String regex, ContextCallback callback) {
        onText(Pattern.compile(regex), callback);
        return this;
    }

    public Wire onMessageEntity(MessageEntityType type, ContextCallback callback) {
        register(type, callback);
        return this;
    }

    public Wire onMention(ContextCallback callback) {
        register(MessageEntityType.MENTION, callback);
        return this;
    }

    public Wire onAction(String data, ContextCallback callback) {
        register(data, callback);
        return this;
    }

    public Wire onCallbackQuery(ContextCallback callback) {
        register(UpdateType.CALLBACK_QUERY, callback);
        return this;
    }

    public void start() {
        start(true);
    }

    public void start(boolean block) {
        final Observable<Update> updates = telegram.start();
        logger.info("Started polling");

        if (block) {
            updates.blockingSubscribe(this::dispatch, this::onError);
        } else {
            updates.subscribeOn(Schedulers.single())
                    .subscribe(this::dispatch, this::onError);
        }
    }


    public Wire catchError(Consumer<Throwable> handler) {
        telegram.catchError(handler);
        return this;
    }

    private void dispatch(Update update) {
        dispatcher.dispatch(update, middlewares);
    }

    private void onError(Throwable t) {
        logger.error(t.getMessage(), t);
    }

    private void register(Object key, ContextCallback callback) {
        dispatcher.register(key, callback);
    }
}
