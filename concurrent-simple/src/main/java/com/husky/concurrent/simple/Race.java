package com.husky.concurrent.simple;

import com.husky.concurrent.simple.model.IDClass;

/**
 * 线程竞态
 * 条件 + 数据
 * @author dengweichang
 */
@SuppressWarnings("all")
public class Race {

	/**
	 * 通过观测状态决定下一步动作
	 * 典型的案例:单例模式需要双重锁校验（if xx == null）
	 * 此处理解为调度器控制的多线程交叉，并发访问但不注重写操作
	 */
	private static final int CONDITION_CHECK_THAN_ACT = 0;

	/**
	 * 读取旧状态->更改->更新
	 * 典型的案例:次数累加 int++
	 */
	private static final int CONDITION_READ_MODIFY_WRITE = 1;

	/**
	 * 数据竞争
	 * 多线程并发访问同一块内存区域，至少一条为了写
	 * 典型的案例:单例模式初始化对象操作
	 */
	private static final int DATA_HAPPENS_BEFORE_ORDERING = 2;


	private static int a = 10;

	private void add () {
		a ++;
	}

	public static void main(String[] args) throws InterruptedException {
//		synchronizeObjectMethod();
//		synchronizeClassMethod();
		Race race = new Race();
		new Thread(() -> {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			race.add();
			System.out.println("线程1---"+a);
		}).start();
		new Thread(()->{
			System.out.println("线程2---"+a);
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			if (a == 10) {
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				System.out.println("线程2---"+a);
			}

		}).start();
	}

	/**
	 * 成员方法同步
	 */
	private static void synchronizeObjectMethod() {

		class ID {
			private int counter;
			public synchronized int getID() {
				return counter++;
			}
		}
		ID id = new ID();
		for (int i = 0; i < 3; i++) {
			new Thread(()-> System.out.println(id.getID())).start();
		}


	}

	/**
	 * 类方法同步
	 * <p>
	 *     static
	 */
	private static void synchronizeClassMethod() {

		for (int i = 0; i < 3; i++) {
			new Thread(()->{
				System.out.println(IDClass.getID());
			}).start();
		}
	}


}