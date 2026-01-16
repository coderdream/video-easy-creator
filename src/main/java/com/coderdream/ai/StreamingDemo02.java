//package com.coderdream.ai;
//
//import dev.langchain4j.data.message.AiMessage;
//import dev.langchain4j.model.StreamingResponseHandler;
//import dev.langchain4j.model.chat.StreamingChatLanguageModel;
//import dev.langchain4j.model.openai.OpenAiStreamingChatModel;
//
//import java.util.concurrent.TimeUnit;
//
//public class StreamingDemo02 {
//
//    public static void main(String[] args) {
//        // 设置HTTP/HTTPS代理
//        System.setProperty("http.proxyHost", "127.0.0.1");
//        System.setProperty("http.proxyPort", "7890");
//        System.setProperty("https.proxyHost", "127.0.0.1");
//        System.setProperty("https.proxyPort", "7890");
//
////langchain4j的测试服务-不稳定
//        StreamingChatLanguageModel model = OpenAiStreamingChatModel.builder()
//                .baseUrl("http://langchain4j.dev/demo/openai/v1")
//                .modelName("gpt-4o-mini")
//                .apiKey("demo")
//                .build();
//        model.generate("你好，你是谁?", new StreamingResponseHandler<AiMessage>() {
//            @Override
//            public void onNext(String token) {
//                System.out.println(token);
//                try {
//                    TimeUnit.SECONDS.sleep(1);
//                } catch (InterruptedException e) {
//                    throw new RuntimeException(e);
//                }
//            }
//            @Override
//            public void onError(Throwable error) {
//                System.out.println(error);
//            }
//        });
//    }
//}
