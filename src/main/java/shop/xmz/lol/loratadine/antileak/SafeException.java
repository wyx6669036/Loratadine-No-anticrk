package shop.xmz.lol.loratadine.antileak;

import java.io.PrintStream;
import java.io.PrintWriter;


public class SafeException extends RuntimeException {
    public SafeException() {
        setStackTrace(new StackTraceElement[0]);
    }

    @Override
    public void printStackTrace(PrintStream s) {

    }

    @Override
    public void printStackTrace(PrintWriter s) {

    }

    @Override
    public void printStackTrace() {

    }
}
