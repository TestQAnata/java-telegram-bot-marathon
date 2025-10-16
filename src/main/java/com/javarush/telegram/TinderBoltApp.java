package com.javarush.telegram;

import com.javarush.telegram.ChatGPTService;
import com.javarush.telegram.DialogMode;
import com.javarush.telegram.MultiSessionTelegramBot;
import com.javarush.telegram.UserInfo;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.objects.*;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

import java.util.ArrayList;

public class TinderBoltApp extends MultiSessionTelegramBot {
    public static final String TELEGRAM_BOT_NAME = "JR_tinder_nata_AI_bot"; //TODO: добавь имя бота в кавычках
    public static final String TELEGRAM_BOT_TOKEN = "7974665868:AAGhFpG7I2m243U8Wkyq98e8MDe-AfQzkwU"; //TODO: добавь токен бота в кавычках
    public static final String OPEN_AI_TOKEN = "javcgkI69gxC3ckPzxOUHNCBh/y1o00JRjNFssihxHBWKdOdr2ibmIufgWV4ld3dZrWBlRbJVJze58CHbsTrtvI7+rVKG3vCOAEKpulzC91bOlKxude9NK/N2V764mqKnhbguJ1U0EV1Wnpbit4qDoqKCfmHBB7tNmpRuM+d37TBvYwmwM8l2+jIiwPqaYxviAt2tL8UggF9VxuubtldnUGkR9/ckEPhSzHgRzaPD9fNhVhLE="; //TODO: добавь токен ChatGPT в кавычках

    private ChatGPTService chatGPT = new ChatGPTService(OPEN_AI_TOKEN);

    private DialogMode currentMode = null;

    private ArrayList<String> list = new ArrayList<>();

    public TinderBoltApp() {
        super(TELEGRAM_BOT_NAME, TELEGRAM_BOT_TOKEN);
    }

    private UserInfo me;

    private UserInfo he;

    private int questionCount;

    @Override
    public void onUpdateEventReceived(Update update) {
        //TODO: основной функционал бота будем писать здесь
        String message = getMessageText();

        if (message.equals("/start")) {
            currentMode = DialogMode.MAIN;
            sendPhotoMessage("main");
            String text = loadMessage("main");
            sendTextMessage(text);

            showMainMenu("главное меню бота", "/start",
                         "генерация Tinder-профиля \uD83D\uDE0E", "/profile",
                         "сообщение для знакомства \uD83E\uDD70", "/opener",
                         "переписка от вашего имени \uD83D\uDE08", "/message",
                         "переписка со звездами \uD83D\uDD25", "/date",
                         "задать вопрос чату GPT \uD83E\uDDE0", "/gpt");
            return;
        }
         //command GPT
        if (message.equals("/gpt")) {
            currentMode = DialogMode.GPT;
            sendPhotoMessage("gpt");
            String text = loadMessage("gpt");
            sendTextMessage(text);
            return;
        }

        if (currentMode == DialogMode.GPT && !isMessageCommand()) {
            String prompt = loadPrompt("gpt");
            String answer = chatGPT.sendMessage(prompt, message);
            sendTextMessage(answer);
            return;
        }
        //command DATE
        if(message.equals("/date")){
            currentMode = DialogMode.DATE;
            sendPhotoMessage("date");
            String text = loadMessage("");
            sendTextButtonsMessage(text,
                    "Ариана Гранде", "date_grande",
                    "Марго Робби", "date_robbie",
                    "Зендея", "date_zendaya",
                    "Райн Гослинг", "date_gosling",
                    "Том Харди", "date_hardy");
            return;
        }

        if (currentMode == DialogMode.DATE && !isMessageCommand()){
            String query = getCallbackQueryButtonKey();
            if (query.startsWith("date_")) {
                sendPhotoMessage(query);
                sendTextMessage(" Отличный выбор! \nТвоя задача пригласить девушку на свидание ❤\uFE0F за 5 сообщений.");

                String prompt = loadPrompt(query);
                chatGPT.setPrompt("Диалог с девушкой");
                return;
            }

            String answer = chatGPT.addMessage(message);
            sendTextMessage(answer);
            return;
        }

        //command MESSAGE
        if (message.equals("/message")) {
            currentMode = DialogMode.MESSAGE;
            sendPhotoMessage("message");
            sendTextButtonsMessage("Пришлите в чат вашу переписку",
                    "Следующее сообщение","message_next",
                    "Пригласить на свидание", "message_date");
            return;
        }

        if(currentMode == DialogMode.MESSAGE && !isMessageCommand()){
            String query = getCallbackQueryButtonKey();
            if (query.startsWith("message_")){
                String prompt = loadPrompt(query);
                String userChatHistory = String.join("\n\n", list);

                Message nsg = sendTextMessage("Подождите пару секунд - ChatGPT думает...");
                String answer = chatGPT.sendMessage(prompt, userChatHistory);//10 sec
                updateTextMessage(nsg,answer);
                return;
            }

            list.add(message);
            return;
        }

        //command PROFILE
        if (message.equals("/profile")){
            currentMode = DialogMode.PROFILE;
            sendPhotoMessage("profile");

            me = new UserInfo();
            questionCount = 1;
            sendTextMessage("Сколько вам лет?");
            return;
        }

        if(currentMode == DialogMode.PROFILE && !isMessageCommand()) {
            switch (questionCount){
                case 1:
                    me.age = message;
                    questionCount=2;
                    sendTextMessage("Кем вы работаете?");
                    return;
                case 2:
                    me.occupation = message;
                    questionCount=3;
                    sendTextMessage("Есть ли у вас хобби?");
                    return;
                case 3:
                    me.hobby = message;
                    questionCount=4;
                    sendTextMessage("Что вам не нравится в людях?");
                    return;
                case 4:
                    me.annoys = message;
                    questionCount=5;
                    sendTextMessage("Цели знакомства?");
                    return;
                case 5:
                    me.goals = message;

                    String aboutMyself = me.toString();
                    String prompt = loadPrompt("profile");
                    Message nsg = sendTextMessage("Подождите пару секунд - ChatGPT \uD83E\uDDE0 думает...");
                    String answer = chatGPT.sendMessage(prompt, aboutMyself);
                    updateTextMessage(nsg, answer);
                    return;

            }

            return;
        }

        //command OPENER
        if (message.equals("/opener")){
            currentMode = DialogMode.OPENER;
            sendPhotoMessage("opener");

            he = new UserInfo();
            questionCount = 1;
            sendTextMessage("Имя мужчины?");
            return;
        }

        if (currentMode == DialogMode.OPENER && !isMessageCommand()){
            switch (questionCount){
                case 1:
                    he.name = message;
                    questionCount = 2;
                    sendTextMessage("Сколько ему лет?");
                    return;
                case 2:
                    he.age = message;
                    questionCount = 3;
                    sendTextMessage("Есть ли у него хобби и какие?");
                    return;
                case 3:
                    he.hobby = message;
                    questionCount = 4;
                    sendTextMessage("Кем он работает?");
                    return;
                case 4:
                    he.occupation = message;
                    questionCount = 5;
                    sendTextMessage("Цель знакомства?");
                    return;
                case 5:
                    he.goals = message;

                    String aboutFriend = he.toString();
                    String prompt = loadPrompt("opener");
                    Message nsg = sendTextMessage("Подождите пару секунд - ChatGPT \uD83E\uDDE0 думает...");
                    String answer = chatGPT.sendMessage(prompt, aboutFriend);
                    updateTextMessage(nsg, answer);
                    return;
            }
            String aboutFriend = message;
            String prompt = loadPrompt("opener");
            Message nsg = sendTextMessage("Подождите пару секунд - ChatGPT \uD83E\uDDE0 думает...");
            String answer = chatGPT.sendMessage(prompt, aboutFriend);
            updateTextMessage(nsg, answer);
            return;
        }

        sendTextMessage("*Привет!*");
        sendTextMessage("_Привет!_");

        sendTextMessage("Вы написали " + message);

        sendTextButtonsMessage("Выберите режим работы",
                "Старт", "start",
                "Стоп", "stop");
    }

    public static void main (String[] args) throws TelegramApiException {
        TelegramBotsApi telegramBotsApi = new TelegramBotsApi(DefaultBotSession.class);
        telegramBotsApi.registerBot(new TinderBoltApp());
    }
}
