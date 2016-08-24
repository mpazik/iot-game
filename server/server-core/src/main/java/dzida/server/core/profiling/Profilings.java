package dzida.server.core.profiling;

import java.util.function.Supplier;

public class Profilings {

    public static <T> T printTime(String title, Supplier<T> method) {
        long start = System.nanoTime();
        T value = method.get();
        long elapsedTime = System.nanoTime() - start;
        System.out.println(title + " in: " + ((double)(elapsedTime / 1000) / 1000) + "ms");
        return value;
    }

    public static void printTime(String title, Runnable runnable) {
        long start = System.nanoTime();
        runnable.run();
        long elapsedTime = System.nanoTime() - start;
        System.out.println(title + " in: " + ((double) (elapsedTime / 1000) / 1000) + "ms");
    }
}
