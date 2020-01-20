package com.husky.concurrent.simple;

import java.util.*;
import java.util.concurrent.*;

/**
 * 并发工具
 * @author dengweichang
 * @see java.util.concurrent.Executor
 * <p>
 *     只关注Runable
 *     无法追踪可运行任务的运行过程
 *     无法执行一组可运行任务
 *     ******* 无法正确的关闭executor ********
 * </p>
 * 以上问题由{@link java.util.concurrent.ExecutorService}解决
 *
 * <p>
 *     {@link  ExecutorService#awaitTermination(long, TimeUnit)}
 *     {@link  ExecutorService#shutdown()}
 *     {@link  ExecutorService#shutdownNow()}
 *
 *     {@link  ExecutorService#invokeAll(Collection)}
 *     {@link  ExecutorService#invokeAny(Collection)}
 *
 *     {@link #callAble()}			Executor.submit Callable，获取线程执行状态以及返回结果
 *     {@link #runnable()}
 *     {@link ExecutorService#submit(Runnable, Object)}
 * </p>
 *
 * @see java.util.concurrent.Future	异步计算结果
 * <p>
 *     {@link Future#cancel(boolean)}
 *		{@link Future#get()}
 * </p>
 */
public class ExecutorDemo {

	private static final ThreadPoolExecutor EXECUTOR = new ThreadPoolExecutor(20, 100, 10, TimeUnit.SECONDS,
			new ArrayBlockingQueue<>(512), r -> new Thread(r, "executor-demo" + "-" + System.currentTimeMillis()));

	public static void main(String[] args) throws ExecutionException, InterruptedException {
		try {
//			callAble();
//			runnable();
//			awaitTermination();
//			executors();
			List<Integer> list = new ArrayList<>();
			list.add(1);
			list.add(2);
			list.add(3);
			Collections.shuffle(list);
			list.forEach(System.out::println);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			EXECUTOR.shutdown();
		}
	}


	/**
	 * @see ExecutorService#submit(Callable)	任务执行过程中产生的异常会被吃掉，在调用Future.get时抛出
	 * @see Future#isDone() 此任务是否完成，非阻塞
	 * @see Future#get() 	获取任务执行结果，阻塞
	 */
	private static void callAble() throws ExecutionException, InterruptedException {
		Future<String[]> future = EXECUTOR.submit(() -> {
			System.out.println("耗时操作");
			Thread.sleep(10);
			System.out.println(4/0);
			return new String[]{"fuck", "you"};
		});
		while (!future.isDone()) {
			System.out.println("waiting future...");
		}
		System.out.println(Arrays.toString(future.get()));
	}

	/**
	 * @see ExecutorService#submit(Runnable)	若无异常，则Future.get得到null
	 * @see ExecutorService#execute(Runnable)	任务执行过程中产生的异常会直接抛出
	 */
	private static void runnable() throws ExecutionException, InterruptedException {
		Runnable runnable = () -> {
			System.out.println("耗时操作--" + Thread.currentThread().getName());
			try {
				Thread.sleep(100);
				System.out.println(4/0);
				System.out.println("任务完成--" + Thread.currentThread().getName());
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		};
		Future<?> submit = EXECUTOR.submit(runnable);
		System.out.println(submit.get());
		EXECUTOR.execute(runnable);
	}

	/**
	 * TODO:暂时没什么用处
	 * @throws InterruptedException
	 */
	private static void awaitTermination() throws InterruptedException {
		EXECUTOR.submit(() -> {
			try {
				Thread.sleep(1000);
				System.out.println("task A is completed");
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		});
		EXECUTOR.submit(() -> {
			try {
				Thread.sleep(1000);
				System.out.println("task B is completed");
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		});
		System.out.println("task count:" + EXECUTOR.getTaskCount());
		boolean b = EXECUTOR.awaitTermination(5000, TimeUnit.MILLISECONDS);
		System.out.println("active task count:" + EXECUTOR.getActiveCount());
		System.out.println(b);
	}

	/**
	 * @see Executors
	 */
	private static void executors() throws ExecutionException, InterruptedException {
		ExecutorService executorService = Executors.newCachedThreadPool(Executors.defaultThreadFactory());
		Runnable runnable = () -> {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			System.out.println("耗时操作");
			throw new RuntimeException("fuck");
		};
		executorService.execute(runnable);
		executorService.shutdown();
	}

}
