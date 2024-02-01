package net.youssfi;

import com.knuddels.jtokkit.Encodings;
import com.knuddels.jtokkit.api.Encoding;
import com.knuddels.jtokkit.api.EncodingRegistry;
import com.knuddels.jtokkit.api.EncodingType;
import com.knuddels.jtokkit.api.ModelType;

import java.util.List;

public class Tokenizer {
    public static void main(String[] args) {
        EncodingRegistry registry = Encodings.newDefaultEncodingRegistry();
        Encoding enc = registry.getEncodingForModel(ModelType.GPT_3_5_TURBO);
        List<Integer> encoded = enc.encode("This is a sample sentence.");
        // encoded = [2028, 374, 264, 6205, 11914, 13]
        System.out.println(encoded);
        byte[] decoded = enc.decodeBytes(encoded);
       // decoded = "This is a sample sentence."

        System.out.println(new String(decoded));
// Or get the tokenizer based on the model type
        Encoding secondEnc = registry.getEncodingForModel(ModelType.GPT_3_5_TURBO);
// enc == secondEnc
    }
}
