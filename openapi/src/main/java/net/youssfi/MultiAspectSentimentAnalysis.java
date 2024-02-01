package net.youssfi;


import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.theokanning.openai.completion.chat.ChatCompletionChoice;
import com.theokanning.openai.completion.chat.ChatCompletionRequest;
import com.theokanning.openai.completion.chat.ChatCompletionResult;
import com.theokanning.openai.completion.chat.ChatMessage;
import com.theokanning.openai.service.OpenAiService;
import tech.tablesaw.api.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MultiAspectSentimentAnalysis {
    public static void main(String[] args) throws IOException {
        GsonBuilder builder = new GsonBuilder();
        builder.setPrettyPrinting();
        Gson gson = builder.create();
        String templatePrompt = """
                [
                        {
                          "role": "system",
                          "content": "Perform aspect based sentiment analysis on laptop reviews presented in the input delimited by triple backticks, that is, ```.\\nIn each review there might be one or more of the following aspects: screen, keyboard, and mousepad.\\nFor each review presented as input:\\n- Identify if there are any of the 3 aspects (screen, keyboard, mousepad) present in the review.\\n- Assign a sentiment polarity (positive, negative or neutral) for each aspect\\nArrange your response a JSON object with the following headers:\\n- category:[list of aspects]\\n- polarity:[list of corresponding polarities for each aspect]}\\n"
                        },
                        {
                          "role": "user",
                          "content": "```The RAM is good. The design is decent.```\\n"
                        },
                        {
                          "role": "assistant",
                          "content": {"category":["ram","design"],"polarity":["positive","neutral"]}
                        },
                        {
                          "role": "user",
                          "content": "```The screen is amazing. The design is impressive. The mousepad is bad.```\\n"
                        },
                        {
                          "role": "assistant",
                          "content": {"category":["screen","design","mousepad"],"polarity":["positive","positive","negative"]}
                        },
                        {
                          "role": "user",
                          "content": "```The GPU is adequate. The camera is average. The software is poor. The keyboard is great.```\\n"
                        },
                        {
                          "role": "assistant",
                          "content": {"category":["gpu","camera","software","keyboard"],"polarity":["neutral","neutral","negative","positive"]}
                        },
                        {
                          "role": "user",
                          "content": "```The RAM is terrible. The battery is fair. The design is disappointing.```\\n"
                        },
                        {
                          "role": "assistant",
                          "content": {"category":["ram","battery","design"],"polarity":["negative","neutral","negative"]}
                        },
                        {
                          "role": "user",
                          "content": "```The GPU is terrible. The keyboard is poor. The mousepad is decent.```\\n"
                        },
                        {
                          "role": "assistant",
                          "content": {"category":["gpu","keyboard","mousepad"],"polarity":["negative","negative","neutral"]}
                        },
                        {
                          "role": "user",
                          "content": "```The battery is poor. The camera is average. The GPU is fair. The screen is terrible.```\\n"
                        },
                        {
                          "role": "assistant",
                          "content": {"category":["battery","camera","gpu","screen"],"polarity":["negative","neutral","neutral","negative"]}
                        },
                        {
                          "role": "user",
                          "content": "```The software is average. The RAM is disappointing.```\\n"
                        },
                        {
                          "role": "assistant",
                          "content": {"category":["software","ram"],"polarity":["neutral","negative"]}
                        },
                        {
                          "role": "user",
                          "content": "```The battery is great. The RAM is good. The software is excellent.```\\n"
                        },
                        {
                          "role": "assistant",
                          "content": {"category":["battery","ram","software"],"polarity":["positive","positive","positive"]}
                        },
                        {
                          "role": "user",
                          "content": "```The hardware is average. The mousepad is excellent.```\\n"
                        },
                        {
                          "role": "assistant",
                          "content": {"category":["hardware","mousepad"],"polarity":["neutral","positive"]}
                        },
                        {
                          "role": "user",
                          "content": "```The camera is unpleasant. The hardware is bad. The battery is amazing. The GPU is fair.```\\n"
                        },
                        {
                          "role": "assistant",
                          "content": {"category":["camera","hardware","battery","gpu"],"polarity":["negative","negative","positive","neutral"]}
                        },
                        {
                          "role": "user",
                          "content": "```%s```\\n"
                        }
                      ]
                """;

        String userMessage = "The cpu is good, the screen is bad but the mousepad is fine";
        String userPrompt = String.format(templatePrompt, userMessage);
        OpenAiService service = new OpenAiService("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");
        System.out.println(userPrompt);
        String data = Files.readString(new File("prompt.json").toPath());
        ChatMessage[] chatMessages = gson.fromJson(data, ChatMessage[].class);
        ChatCompletionRequest completionRequest = ChatCompletionRequest.builder()
                .messages(List.of(chatMessages))
                .temperature(0.5)
                .model("gpt-3.5-turbo")
                .build();
        System.out.println("============== REQUEST ==============");
        System.out.println(gson.toJson(completionRequest));
        ChatCompletionResult chatCompletion = service.createChatCompletion(completionRequest);
        System.out.println("============== RESPONSE ==============");
        List<ChatCompletionChoice> choices = chatCompletion.getChoices();
        String s = gson.toJson(chatCompletion);
        System.out.println(s);
        System.out.println("===============");
    }
}
