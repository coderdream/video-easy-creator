//package com.coderdream.ai;
//
//import dev.langchain4j.data.message.AiMessage;
//import dev.langchain4j.model.StreamingResponseHandler;
////import dev.langchain4j.model.chat.StreamingChatLanguageModel;
//import dev.langchain4j.model.openai.OpenAiStreamingChatModel;
//import okhttp3.OkHttpClient;
//
//import java.net.InetSocketAddress;
//import java.net.Proxy;
//import java.time.Duration;
//import java.util.concurrent.TimeUnit;
//
//public class StreamingDemoWithProxy {
//
//    public static void main(String[] args) {
//        // 方法二：使用OkHttpClient配置代理（更灵活）
//        Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress("127.0.0.1", 7890));
//
//        OkHttpClient okHttpClient = new OkHttpClient.Builder()
//                .proxy(proxy)
//                .connectTimeout(Duration.ofSeconds(30))
//                .readTimeout(Duration.ofSeconds(60))
//                .build();
//
//        // langchain4j的测试服务-不稳定
//        StreamingChatLanguageModel model = OpenAiStreamingChatModel.builder()
//                .baseUrl("http://langchain4j.dev/demo/openai/v1")
//                .modelName("gpt-4o-mini")
//                .apiKey("demo")
//                .client(okHttpClient)  // 设置自定义的OkHttpClient
//                .build();
//
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
