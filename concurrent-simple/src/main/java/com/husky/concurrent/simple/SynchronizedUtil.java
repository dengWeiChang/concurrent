package com.husky.concurrent.simple;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import sun.font.CFontManager;

import java.util.concurrent.*;

/**
 * 同步工具
 * @author dengweichang
 */
@Slf4j
public class SynchronizedUtil {


	public static void main(String[] args) {
//		countDownLatch();
//		cyclicBarrier();
//		simpleBarrier();
//		childSynchronized();
		System.out.println(Integer.toBinaryString(-1 << 29));
		System.out.println(Integer.toBinaryString((-1<<29)+1));
	}
	/**
	 * 倒计时门闩
	 * @see java.util.concurrent.CountDownLatch
	 * 模拟并发操作，线程中使用开始限制条件，主线程中开始运行并且阻塞结束条件
	 */
	private static void countDownLatch() {
		//工作线程数
		final int num = 3;
		final CountDownLatch start = new CountDownLatch(1);
		final CountDownLatch done = new CountDownLatch(num);
		Runnable runnable = new Runnable() {
			@Override
			public void run() {
				try {
					report("run()");
					start.await();
					report("working");
					Thread.sleep((int)(Math.random() * 1000));
				} catch (InterruptedException e) {
					log.error("await被中断", e);
				} finally {
					done.countDown();
				}
			}

			void report(String s) {
				log.info(System.currentTimeMillis() + ":" + Thread.currentThread().getId() + ":" + s);
			}
		};
		ExecutorService executorService = Executors.newFixedThreadPool(num);
		for (int i = 0; i < num; i++) {
			executorService.execute(runnable);
		}
		log.info("main is running");
		try {
			Thread.sleep(1000);
			start.countDown();
			log.info("start is open");
			done.await();
			log.info("执行完毕");
			executorService.shutdown();
		} catch (InterruptedException e) {
			log.error("线程被中断", e);
		}
	}

	/**
	 * 循环使用的线程屏障
	 * @see CyclicBarrier
	 * @see CyclicBarrier#barrierCommand TODO: 并非主线程，但是为什么synchornied失效有待验证
	 */
	private static void cyclicBarrier() {
		//3*3的矩阵
		float[][] matrix = new float[3][3];
		int counter = 0;
		for (int row = 0; row < matrix.length; row++) {
			for (int col = 0; col < matrix[0].length; col++) {
				matrix[row][col] = counter++;
			}
		}
		dump(matrix);

		class Solver {
			final int N;
			final float[][] data;
			final CyclicBarrier cyclicBarrier;

			public Solver(float[][] data) {
				String lock = "abc";
				this.data = data;
				N = data.length;
				cyclicBarrier = new CyclicBarrier(N, () ->{
					log.info("merging");
					synchronized (lock) {
						log.info("进入barrier线程同步块,线程id -{}", Thread.currentThread().getId());
						"abc".notifyAll();
						log.info("退出barrier线程同步块");
					}
				});
				for (int i = 0; i < N; i++) {
					new Thread(new Worker(i)).start();
				}
				synchronized (lock) {
					log.info("进入main同步块线程id -{}", Thread.currentThread().getId());
					try {
						log.info("main is waiting...");
						"abc".wait();
						log.info("main is notified");
					} catch (InterruptedException e) {
						log.error("main is interrupted");
					} finally {
						log.info("退出main同步块");
					}
				}
			}

			class Worker implements Runnable {
				int myRow;
				boolean done = false;

				public Worker(int myRow) {
					this.myRow = myRow;
				}

				boolean done() {
					return done;
				}

				void processRow(int myRow) {
					log.info("solver processing row: " + myRow);
					try {
						//此处睡眠1秒，防止执行过快在主线程"abc".await之前到达屏障
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						return;
					}
					for (int i = 0; i < N; i++) {
						data[myRow][i] *= 10;
					}
					done = true;
				}

				@Override
				public void run() {
					while (!done()) {
						processRow(myRow);
						try {
							cyclicBarrier.await();
						} catch (InterruptedException |BrokenBarrierException e) {
							log.error("cyclic barrier error", e);
							return;
						}
					}
				}
			}
		}

		new Solver(matrix);

		dump(matrix);

	}

	private static void simpleBarrier() {
		final int threadNum = 3;
		Runnable activeRecommend = () -> {
			log.info("barrier is open -{}", Thread.currentThread().getId());
		};
		CyclicBarrier barrier = new CyclicBarrier(threadNum, activeRecommend);
		log.info("main thread -{}", Thread.currentThread().getId());
		ExecutorService executorService = Executors.newFixedThreadPool(threadNum);
		for (int i = 0; i < threadNum; i++) {
			executorService.execute(() -> {
				try {
					log.info(Thread.currentThread().getId() + "等大家一起上班");
					barrier.await();
					log.info(Thread.currentThread().getId() + "干活了...");
					Thread.sleep(1000);
				} catch (InterruptedException | BrokenBarrierException e) {
					e.printStackTrace();
				}
			});
		}
		executorService.shutdown();
	}

	/**
	 * 信号量许可证
	 */
	private static void semaphore() {

	}

	/**
	 * 输出矩阵
	 * @param matrix	二维矩阵
	 */
	static void dump(float[][] matrix) {
		for (int i = 0; i < matrix.length; i++) {
			for (int col = 0; col < matrix[0].length; col++) {
				log.info(String.valueOf(matrix[i][col]));
			}
		}
	}

	/**
	 * synchronized对子线程并非可重入
	 */
	static void childSynchronized() {
		String lock = "abc";
		new Thread(() -> {
			synchronized (lock) {
				log.info("进入主线程");
				new Thread(() -> {
					synchronized (lock) {
						log.info("进入子线程");
						try {
							Thread.sleep(1000);
						} catch (InterruptedException e) {
							e.printStackTrace();
						} finally {
							log.info("退出子线程");
						}
					}
				}).start();
				try {
					Thread.sleep(2000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				log.info("退出主线程");
			}
		}).start();
	}
}
