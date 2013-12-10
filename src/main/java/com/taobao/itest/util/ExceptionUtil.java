package com.taobao.itest.util;

/**
 * 封装一些跟异常相关的工具方法
 * 
 * @author yufan.yq
 * 
 */
public class ExceptionUtil {
	private static final String CAUSE_BY = "Cause By：\r\n";
	private static final String SEPRETOR = "\r\n";

	public static void appendStackTrace(StringBuilder content,
			Throwable throwable) {
		if (throwable == null || content == null) {
			return;
		}
		content.append(SEPRETOR).append(CAUSE_BY);
		content.append(throwable.toString()).append(SEPRETOR);
		content.append(SEPRETOR);

		StackTraceElement[] stackTrace = throwable.getStackTrace();
		if (stackTrace != null) {
			for (StackTraceElement stack : stackTrace) {
				if (stack != null) {
					content.append(stack.toString());
					content.append(SEPRETOR);
				}
			}
			appendStackTrace(content, throwable.getCause());
		}
	}
}
