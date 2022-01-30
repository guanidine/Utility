package pers.guanidine.utility.impls;

/**
 * 回调接口。
 *
 * @author Guanidine Beryllium
 */
public interface CallBack<T> {
    /**
     * 回调结果传递方法。
     *
     * @param result 回调结果
     */
    void onCall(T result);
}
