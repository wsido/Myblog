package top.wsido.exception;

/**
 * @Description: 持久化异常
 * @Author: wsido
 * @Date: 2020-08-14
 */

public class PersistenceException extends RuntimeException {
	public PersistenceException() {
	}

	public PersistenceException(String message) {
		super(message);
	}

	public PersistenceException(String message, Throwable cause) {
		super(message, cause);
	}
}
