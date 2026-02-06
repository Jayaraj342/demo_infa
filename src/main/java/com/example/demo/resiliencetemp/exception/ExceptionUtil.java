package com.example.demo.resiliencetemp.exception;

public class ExceptionUtil {

    public static RuntimeException throwOriginal(Throwable t) {
        // 1. Unwrap the wrapping layers (CompletionException/ExecutionException)
//        if (t instanceof java.util.concurrent.CompletionException || t instanceof java.util.concurrent.ExecutionException) {
//            return throwOriginal(t.getCause());
//        }

        // 2. "Sneakily" throw the original exception (Checked or Unchecked)
        sneakyThrow(t);
        return new RuntimeException(t); // unreachable
    }

    @SuppressWarnings("unchecked")
    private static <T extends Throwable> void sneakyThrow(Throwable t) throws T {
        throw (T) t; // The compiler erases <T> to Throwable, but treats it as T locally
    }
}
