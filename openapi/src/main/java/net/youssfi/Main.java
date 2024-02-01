package net.youssfi;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.knuddels.jtokkit.Encodings;
import com.knuddels.jtokkit.api.Encoding;
import com.knuddels.jtokkit.api.EncodingRegistry;
import com.knuddels.jtokkit.api.ModelType;
import com.theokanning.openai.completion.chat.*;
import com.theokanning.openai.service.OpenAiService;
import tech.tablesaw.api.DoubleColumn;
import tech.tablesaw.api.Row;
import tech.tablesaw.api.StringColumn;
import tech.tablesaw.api.Table;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.fasterxml.jackson.databind.deser.std.UntypedObjectDeserializer.Vanilla.std;

public class Main {
    public static void main(String[] args) throws IOException {

        GsonBuilder builder = new GsonBuilder();
        builder.setPrettyPrinting();
        Gson gson = builder.create();
        String user_message_template = """
                ```%s```
                """;
        String userQuery = String.format(user_message_template,"The screen is amazing. The design is impressive. The mousepad is bad.");
        String systemMessage = """
Perform aspect based sentiment analysis on laptop reviews presented in the input delimited by triple backticks, that is, ```.\nIn each review there might be one or more of the following aspects: screen, keyboard, and mousepad.\nFor each review presented as input:\n- Identify if there are any of the 3 aspects (screen, keyboard, mousepad) present in the review.\n- Assign a sentiment polarity (positive, negative or neutral) for each aspect\nArrange your response a JSON object with the following headers:\n- category:[list of aspects]\n- polarity:[list of corresponding polarities for each aspect]}
""";

        Table table = Table.read().csv("laptop_reviews.csv");
        table = cleanDataSet(table);

        Table[] tables = table.sampleSplit(0.8);
        Table trainTable = tables[0];

        Table goldExamplesTable = tables[1];
        System.out.println(goldExamplesTable.first(5));
        List<Row> examples = createExamples(trainTable, 4, gson);

        List<ChatMessage> zeroShotPrompt = new ArrayList<>();
        zeroShotPrompt.add(new ChatMessage(ChatMessageRole.SYSTEM.value(), systemMessage));

        List<ChatMessage> fewShoPrompt = createPrompt(systemMessage, examples, user_message_template);

        EncodingRegistry registry = Encodings.newDefaultEncodingRegistry();
        Encoding enc = registry.getEncodingForModel(ModelType.GPT_3_5_TURBO);
        List<Integer> encoded = enc.encode(gson.toJson(fewShoPrompt));
        System.out.println("Tokens count = "+encoded.size());
        //double scoreZeroShotPrompt=evaluatePrompt(zeroShotPrompt,goldExamplesTable,user_message_template, gson);
        //System.out.println("Accuracy zero shot prompt = "+scoreZeroShotPrompt);
        //System.out.println("-------------");
        //double scoreFewShotPrompt=evaluatePrompt(fewShoPrompt,goldExamplesTable,user_message_template, gson);
        //System.out.println("Accuracy few shot prompt = "+scoreFewShotPrompt);

        //Files.write(new File("zeroShotPrompt.json").toPath(), gson.toJson(zeroShotPrompt).getBytes());
        //Files.write(new File("fewShotPrompt.json").toPath(), gson.toJson(fewShoPrompt).getBytes());

        int numberOfRuns=10;
        DoubleColumn fewShotPerformance = DoubleColumn.create("accuracies");
        Table t = Table.create(fewShotPerformance);
        for (int i = 0; i <numberOfRuns ; i++) {
            System.out.println("Run => "+i);
            List<Row> iterationExamples = createExamples(trainTable, 4, gson);
            List<ChatMessage> prompt = createPrompt(systemMessage, iterationExamples, user_message_template);
            double accuracy = evaluatePrompt(prompt, goldExamplesTable, user_message_template, gson);
            fewShotPerformance.append(accuracy);
            Files.write(new File("fewShotPrompt_"+accuracy+".json").toPath(), gson.toJson(prompt).getBytes());
        }

        Table summary = t.summary();
        System.out.println(summary);

    }

    public static  Table cleanDataSet(Table table){
        for(int i=0; i<table.rowCount();i++){
            String text = table.getString(i,"text");
            String aspectValue = table.getString(i,"aspects");
            String categoryValue = table.getString(i,"category");
            String newAspectValue = aspectValue.replace("array(", "").replace(",dtype=object)", "")
                    .replace(",dtype=int16)", "").replace(",dtype=int32)", "").replace("'","\"")
                    .toLowerCase();
            String newCategoryValue = categoryValue.replace("array(", "").replace(",dtype=object)", "")
                    .replace(",dtype=int16)", "").replace(",dtype=int32)", "").replace("'","\"")
                    .toLowerCase();
            StringColumn aspects = table.stringColumn("aspects");
            StringColumn categories = table.stringColumn("category");
            aspects.set(i,newAspectValue);
            categories.set(i, newCategoryValue);
        }
        return table;
    }

    public static List<Row> createExamples(Table table, int n, Gson gson){
        List<Row> examples = new ArrayList<>();
        List<String> columnsToSelect=List.of("id","aspects","category");
        List<Long> exampleIds = new ArrayList();
        Map<String,ArrayList> aspectIndex=
                Map.of("screen",new ArrayList(),"keyboard",new ArrayList(),"mousepad", new ArrayList());

        for (int i=0;i<table.rowCount();i++){
            String id = table.getString(i,"id");
            String category = table.getString(i,"category");
            Map<String, ArrayList> cat = gson.fromJson(table.getString(i,"category"), HashMap.class);
            List<String> aspects = cat.get("category");
            for (String key : aspectIndex.keySet()){
                if(aspects.contains(key)){
                    aspectIndex.get(key).add(Long.valueOf(id));
                }
            }

        }
        for (String key : aspectIndex.keySet()){
            Collections.shuffle(aspectIndex.get(key));
            exampleIds.addAll(aspectIndex.get(key).subList(0,4));
        }
        for (int i=0;i<table.rowCount();i++){
            Row row = table.row(i);
            if(exampleIds.contains(Long.valueOf(row.getInt("id")))){
                examples.add(row);
            }
        }
        return examples;
    }

    public static List<ChatMessage> createPrompt(String systemMessage, List<Row> examples, String useMessageTemplate){

        List<ChatMessage> fewShotPrompt=new ArrayList<>();
        fewShotPrompt.add(new ChatMessage(ChatMessageRole.SYSTEM.value(), systemMessage));
        for(Row row : examples){
            String exampleInput = row.getString("text");
            String exampleAbsa = row.getString("category");
            fewShotPrompt.add(new ChatMessage(ChatMessageRole.USER.value(), String.format(useMessageTemplate,exampleInput)));
            fewShotPrompt.add(new ChatMessage(ChatMessageRole.ASSISTANT.value(), exampleAbsa));
        }
        return fewShotPrompt;
    }

    public static double evaluatePrompt(List<ChatMessage> prompt, Table goldExamples, String userMessageTemplate, Gson gson) {

        List<Map<String, ArrayList<String>>> predictions= new ArrayList<>();
        List<Map<String, ArrayList<String>>> groundTruth= new ArrayList<>();
        for (int i=0;i<goldExamples.rowCount();i++) {
            //System.out.println("Iteration = >"+i);
            List<ChatMessage> rowPrompt = new ArrayList<>();
            Row row = goldExamples.row(i);
            List<ChatMessage> userInput = List.of(
                    new ChatMessage(ChatMessageRole.USER.value(), String.format(userMessageTemplate, row.getString("text")))
            );
            rowPrompt.addAll(prompt);
            rowPrompt.addAll(userInput);

            ChatCompletionRequest completionRequest = ChatCompletionRequest.builder()
                    .messages(rowPrompt)
                    .temperature(0.5)
                    .model("gpt-3.5-turbo")
                    .build();
            OpenAiService service = new OpenAiService("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");
            ChatCompletionResult response = service.createChatCompletion(completionRequest);
            List<ChatCompletionChoice> choices = response.getChoices();
            String responseContent = choices.get(0).getMessage().getContent();
            predictions.add(gson.fromJson(responseContent, HashMap.class));
            groundTruth.add(gson.fromJson(row.getString("category"),HashMap.class));
        }
        double correctPredictions=0;
        double totalPredictions = goldExamples.rowCount();
        for (int i=0;i<goldExamples.rowCount();i++){
            Map<String, ArrayList<String>> prediction = predictions.get(i);
            Map<String, ArrayList<String>> truth = groundTruth.get(i);
            if(truth.equals(prediction)){
                ++correctPredictions;
            }
        }
        //System.out.println(predictions);
        //System.out.println(groundTruth);
        return (correctPredictions/ totalPredictions);
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