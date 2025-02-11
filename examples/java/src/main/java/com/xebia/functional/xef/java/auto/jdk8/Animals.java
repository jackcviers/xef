package com.xebia.functional.xef.java.auto.jdk8;

import com.xebia.functional.xef.auto.PlatformConversation;
import com.xebia.functional.xef.auto.llm.openai.OpenAI;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class Animals {

    private final PlatformConversation scope;

    public Animals(PlatformConversation scope) {
        this.scope = scope;
    }

    public CompletableFuture<Animal> uniqueAnimal() {
        return scope.prompt(OpenAI.FromEnvironment.DEFAULT_SERIALIZATION, "A unique animal species.", Animal.class);
    }

    public CompletableFuture<Invention> groundbreakingInvention() {
        return scope.prompt(OpenAI.FromEnvironment.DEFAULT_SERIALIZATION, "A groundbreaking invention from the 20th century.", Invention.class);
    }

    public CompletableFuture<Story> story(Animal animal, Invention invention) {
        String storyPrompt =
                "Write a short story of 500 words that involves the following elements:" +
                        "1. A unique animal species called ${animal.name} that lives in " + animal.habitat + " and has a diet of " + animal.diet + "." +
                        "2. A groundbreaking invention from the 20th century called " + invention.name + " , invented by " + invention.inventor + " in " + invention.year + ", which serves the purpose of " + invention.purpose + ".";
        return scope.prompt(OpenAI.FromEnvironment.DEFAULT_SERIALIZATION, storyPrompt, Story.class);
    }

    public static class Animal {
        public String name;
        public String habitat;
        public String diet;
    }

    public static class Invention {
        public String name;
        public String inventor;
        public int year;
        public String purpose;
    }

    public static class Story {
        public Animal animal;
        public Invention invention;
        public String text;

        public void tell() {
            System.out.println("Story about " + animal.name + " and " + invention.name + ": " + text);
        }
    }

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        try (PlatformConversation scope = OpenAI.conversation()) {
            Animals animals = new Animals(scope);
            animals.uniqueAnimal()
                    .thenCompose(animal -> animals.groundbreakingInvention()
                            .thenCompose(invention -> animals.story(animal, invention)
                                    .thenAccept(Story::tell)
                            )).get();
        }
    }
}
