//package com.coderdream.ai;
//
//
//import dev.langchain4j.data.message.AiMessage;
//import dev.langchain4j.data.message.UserMessage;
//import dev.langchain4j.model.chat.ChatLanguageModel;
//import dev.langchain4j.model.openai.OpenAiChatModel;
//import dev.langchain4j.model.output.Response;
//
//public class HelloWorld02 {
//
//    public static void main(String[] args) {
//        ChatLanguageModel model = OpenAiChatModel.builder().apiKey("demo").modelName("gpt-4o-mini").build();
//        UserMessage userMessage1 = UserMessage.userMessage("你好，你是谁？");
//        Response<AiMessage> response1 = model.generate(userMessage1);
//        AiMessage aiMessage1 = response1.content();
//        System.out.println(aiMessage1.text());
//        System.out.println("-------");
//        UserMessage userMessage2 = UserMessage.userMessage("请再重复⼀次");
//        Response<AiMessage> response2 = model.generate(userMessage1, aiMessage1,
//                userMessage2);
//        System.out.println(response2.content().text());
//    }
//}
