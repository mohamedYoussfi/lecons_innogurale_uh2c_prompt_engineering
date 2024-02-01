package net.youssfi;

import com.theokanning.openai.completion.chat.ChatCompletionRequest;
import com.theokanning.openai.completion.chat.ChatCompletionResult;
import com.theokanning.openai.completion.chat.ChatMessage;
import com.theokanning.openai.completion.chat.ChatMessageRole;
import com.theokanning.openai.service.OpenAiService;

import java.util.List;

public class TestOpenAI {
    public static void main(String[] args) {
        OpenAiService service = new OpenAiService("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");
        List<ChatMessage> chatMessages = List.of(
                new ChatMessage(ChatMessageRole.SYSTEM.value(),"You are a helpful assistant."),
                new ChatMessage(ChatMessageRole.USER.value(),"Does Azure OpenAI work with multi-model inputs?"),
                new ChatMessage(ChatMessageRole.ASSISTANT.value(),"Yes, at the moment there is restricted access to multi-model inputs (e.g., text + images)."),
                new ChatMessage(ChatMessageRole.USER.value(),"What else does the Azure Open AI service support?")
        );
        ChatCompletionRequest completionRequest = ChatCompletionRequest.builder()
                .messages(chatMessages)
                .temperature(0.5)
                .model("gpt-4")
                .build();
        ChatCompletionResult response = service.createChatCompletion(completionRequest);
        System.out.println(response.getChoices().get(0).getMessage().getContent());
    }
}
