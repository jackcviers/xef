package com.xebia.functional.xef.java.auto.jdk8;

import com.xebia.functional.xef.auto.PlatformConversation;
import com.xebia.functional.xef.auto.llm.openai.OpenAI;

import java.util.concurrent.ExecutionException;

public class TouristAttractions {

    static class TouristAttraction {
        public String name;
        public String location;
        public String history;
    }

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        try (PlatformConversation scope = OpenAI.conversation()) {
            scope.prompt(OpenAI.FromEnvironment.DEFAULT_SERIALIZATION, "Statue of Liberty location and history.", TouristAttraction.class)
                    .thenAccept(statueOfLiberty -> System.out.println(
                            statueOfLiberty.name + "is located in " + statueOfLiberty.location +
                                    " and has the following history: " + statueOfLiberty.history
                            )
                    ).get();
        }
    }
}
