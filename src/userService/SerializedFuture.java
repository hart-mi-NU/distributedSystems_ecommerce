package userService;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;


// Custom Future object that is serializable. Used for returning a CompletableFuture<Request> over RMI
public class SerializedFuture<T> extends CompletableFuture<T> implements Serializable {
    private static final long serialVersionUID = 1L;

    private void writeObject(ObjectOutputStream out) throws IOException {
        T result = join();
        out.writeObject(result);
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        @SuppressWarnings("unchecked")
        T result = (T) in.readObject();
        complete(result);
    }

    @Override
    public T get(long timeout, TimeUnit unit)
            throws InterruptedException, ExecutionException, TimeoutException {
        return super.get(timeout, unit);
    }

    @Override
    public T join() {
        return super.join();
    }
}
