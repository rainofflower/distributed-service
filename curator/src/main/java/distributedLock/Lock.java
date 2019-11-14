package distributedLock;

public interface Lock {

    void acquire() throws Exception;

    void release() throws Exception;
}
