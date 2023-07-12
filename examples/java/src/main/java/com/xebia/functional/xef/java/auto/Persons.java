package com.xebia.functional.xef.java.auto;

import java.util.concurrent.ExecutionException;

public class Persons {

    public static class Person {
        public String name;
        public int age;

        @Override
        public String toString() {
            return "Hello " + name + ", you are " + age + " years old.";
        }
    }

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        try (AIScope scope = new AIScope()) {
            scope.prompt("What is your name and age?", Person.class)
                    .thenAccept(person -> System.out.println(person))
                    .get();
        }
    }
}
