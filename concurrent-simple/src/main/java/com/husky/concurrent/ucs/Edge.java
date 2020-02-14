package com.husky.concurrent.ucs;

/**
 * 邻接矩阵之边
 * Vertex relations
 * E ={ (x, y) | x, y ∈V }
 * @author dengweichang
 */
public class Edge {

	/**
	 * 起始顶点，目标顶点，边权重（代价）
	 */
	public int start, dest, weight;

	public Edge(int start, int dest, int weight) {
		this.start = start;
		this.dest = dest;
		this.weight = weight;
	}

	public static void main(String[] args) throws InterruptedException {
		Thread thread = new Thread(() -> System.out.println(1));
		Thread.sleep(1000);
		thread.join();
		System.out.println("end");
	}
}
