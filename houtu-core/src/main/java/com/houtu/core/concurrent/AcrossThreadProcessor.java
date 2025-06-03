package com.houtu.core.concurrent;

/**
 * @author Jon
 * @email lujiafayx@163.com
 * @date 2021年5月19日
 * @Description 线程数据跨越传输接口。通过父线程中执行get、子线程中执行set来传递参数
 *
 */
public interface AcrossThreadProcessor<T> {

    /**
     * 从父线程中获取数据
     * 注：在父线程中执行
     */
    T parentGet();

    /**
     * 子线程正式执行前
     * 注：在子线程中执行
     * @param parentThread 父线程
     * @param t 数据对象
     */
    void childExecuteBefore(Thread parentThread, T t);

    /**
     * 子线程正式执行后
     * 注：在子线程中执行
     * @param parentThread 父线程
     * @param t 数据对象
     */
    void childExecuteAfter(Thread parentThread, T t);

}
