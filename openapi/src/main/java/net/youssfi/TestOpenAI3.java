package net.youssfi;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.knuddels.jtokkit.Encodings;
import com.knuddels.jtokkit.api.Encoding;
import com.knuddels.jtokkit.api.EncodingRegistry;
import com.knuddels.jtokkit.api.ModelType;
import com.theokanning.openai.completion.chat.ChatCompletionRequest;
import com.theokanning.openai.completion.chat.ChatCompletionResult;
import com.theokanning.openai.completion.chat.ChatMessage;
import com.theokanning.openai.completion.chat.ChatMessageRole;
import com.theokanning.openai.service.OpenAiService;

import java.util.Arrays;
import java.util.List;

public class TestOpenAI3 {
    public static void main(String[] args) {
        String promptText = """
                [
                  {"role":"system", "content": "You are a helpful assistant." },
                  {"role":"user", "content": "What's the weather like?" },
                  {"role":"assistant", "content":"It's raining." },
                  {"role":"user", "content":"What should I take with me?" }
                ]
                """;
        GsonBuilder builder = new GsonBuilder().setPrettyPrinting();
        Gson gson = builder.create();
        ChatMessage[] chatMessagesArray = gson.fromJson(promptText, ChatMessage[].class);
        List<ChatMessage> prompt = Arrays.asList(chatMessagesArray);
        System.out.println(numberOfToken(prompt));
        OpenAiService service = new OpenAiService("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");
        ChatCompletionRequest completionRequest = ChatCompletionRequest.builder()
                .messages(prompt)
                .temperature(0.8)
                .model("gpt-4")
                .n(2)
                .build();
        ChatCompletionResult response = service.createChatCompletion(completionRequest);
        System.out.println(response.getChoices().get(0).getMessage().getContent());
        System.out.println(response.getChoices().get(1).getMessage().getContent());
    }
    public static int numberOfToken(List<ChatMessage> messages){
        EncodingRegistry registry = Encodings.newDefaultEncodingRegistry();
        Encoding enc = registry.getEncodingForModel(ModelType.GPT_3_5_TURBO);
        // Each message is sandwiched with <|start|>role and <|end|>
        // Hence, messages look like: <|start|>system or user or assistant{message}<|end|>
        int tokensPerMessage = 3; // token1:<|start|>, token2:system(or user or assistant), token3:<|end|>
        int numberOfTokens = 0;
        for (ChatMessage message : messages){
            numberOfTokens += tokensPerMessage;
            int messageNumTokens = enc.encode(message.getContent()).size() + enc.encode(message.getRole()).size();
            numberOfTokens += messageNumTokens;
        }
        numberOfTokens += 3;
       return numberOfTokens ;
    }
}
