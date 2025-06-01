package gleam.exception;

/**
 * buffer读取数据失败错误
 * 
 * @author hdh
 *
 */
public class BufferReadDataException extends RuntimeException {

    /**
     * 
     */
    private static final long serialVersionUID = 6712488298293704972L;

    public BufferReadDataException() {
    }

    public BufferReadDataException(String msg) {
        super(msg);
    }

    public BufferReadDataException(String msg, Throwable cause) {
        super(msg, cause);
    }

    public BufferReadDataException(Throwable cause) {
        super(cause);
    }

}
