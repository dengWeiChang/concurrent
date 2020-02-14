package com.husky.concurrent.ucs;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;
import java.util.stream.IntStream;

public class BFS {

	/**
	 * 已探索路径
	 */
	private PriorityQueue<DefaultPath> pathQueue = new PriorityQueue<>(50, (o1, o2) -> {
		if ((o1.fictitiousCost() - o2.fictitiousCost()) > 0) {
			return 1;
		} else if ((o1.fictitiousCost() - o2.fictitiousCost()) < 0) {
			return -1;
		}
		return 0;
	});

	/**
	 * 已经搜索的顶点
	 */
	private HashSet<Vertex> existList = new HashSet<>();

	private VertexFactory vertexFactory;

	public BFS(VertexFactory vertexFactory) {
		this.vertexFactory = vertexFactory;
	}

	public void search(Vertex start, Vertex target, String method) {
		boolean useEstimate = "A*".equals(method);
		Vertex currentVertex = start;
		if (currentVertex.equals(target)) {
			System.out.println("起始顶点即为目标顶点");
			return;
		}
		DefaultPath currentPath = new DefaultPath();
		currentPath.getPathList().add(new Tuple<>(currentVertex, 0));
		pathQueue.add(currentPath);

		int i = 0;
		for (;;) {
			i++;
			currentPath = pathQueue.poll();
			if (currentPath == null) {
				System.out.println("没有结果！！");
				return;
			}
			Tuple<Vertex, Integer> last = currentPath.getPathList().getLast();
			currentVertex = last.getVertex();
//			System.out.println(currentVertex.time +" "+ currentVertex.x +" "+ currentVertex.y +" "+ currentPath.cost +" "+ (currentPath.fictitiousCost() - currentPath.cost) );
			if (currentVertex.equals(target)) {
				System.out.println("path -> ");
				currentPath.getPathList().forEach(item -> System.out.println(item.getVertex().x + " " + item.getVertex().y + " " + item.getVertex().time + " " + item.getCost() ));
				System.out.println("total cost:" + currentPath.getCost());
				System.out.println("total vertex :" + i);
				return;
			}
			List<Tuple<Vertex, Integer>> tuples = vertexFactory.vertexCost(currentVertex);
			DefaultPath finalCurrentPath = currentPath;
			tuples.forEach(tuple -> {
				//若该节点已搜索过则放弃，之前的节点为最佳路径
				if (existList.contains(tuple.getVertex())) {
					return;
				}
				existList.add(tuple.getVertex());
				DefaultPath explore = finalCurrentPath.explore(tuple.getVertex(), tuple.getCost());
				if (useEstimate) {
					int abs = Math.abs(target.time - tuple.getVertex().time);
//					System.out.println(abs + "--total path cost--" + finalCurrentPath.cost);
					explore.setEstimated(Math.min(abs, finalCurrentPath.cost));
				}
				pathQueue.add(explore);
			});
		}
	}

	public static void main(String[] args) throws IOException {
		long startTime = System.currentTimeMillis();
		BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream("/Users/dengweichang/Documents/input0.txt")));
		String method = bufferedReader.readLine();
		System.out.println(method);

		String[] max = bufferedReader.readLine().split(" ");
		int maxX = Integer.parseInt(max[0]);
		int maxY = Integer.parseInt(max[1]);

		String[] startStr = bufferedReader.readLine().split(" ");
		Vertex start = new Vertex(Integer.parseInt(startStr[1]), Integer.parseInt(startStr[2]), Integer.parseInt(startStr[0]));
		String[] targetStr = bufferedReader.readLine().split(" ");
		Vertex target = new Vertex(Integer.parseInt(targetStr[1]), Integer.parseInt(targetStr[2]), Integer.parseInt(targetStr[0]));

		int channelNum = Integer.parseInt(bufferedReader.readLine());
		List<Channel> channelList = new ArrayList<>();
		for (int i = 0; i < channelNum; i++) {
			String[] channelStr = bufferedReader.readLine().split(" ");
			int time1 = Integer.parseInt(channelStr[0]);
			int x = Integer.parseInt(channelStr[1]);
			int y = Integer.parseInt(channelStr[2]);
			int time2 = Integer.parseInt(channelStr[3]);

			channelList.add(new Channel(time1, time2,x,y, Math.abs(time2 - time1)));
		}

		BFS bfs = new BFS(new VertexFactory(maxX, maxY, channelList));

		bfs.search(start, target, method);
		System.out.println("运行时间，ms-" + (System.currentTimeMillis() - startTime));
	}
}

/**
 * 顶点生成器
 */
class VertexFactory {

	public VertexFactory(int maxX, int maxY, List<Channel> channelList) {
		this.maxX = maxX;
		this.maxY = maxY;
		this.channelMap = new HashMap<>();
		if (channelList == null || channelList.isEmpty()) {
			return;
		}
		channelList.forEach(channel -> {
			Vertex vertex1 = new Vertex(channel.getX(), channel.getY(), channel.getTime1());
			Vertex vertex2 = new Vertex(channel.getX(), channel.getY(), channel.getTime2());
			if (channelMap.get(vertex1) == null) {
				ArrayList<Tuple<Vertex, Integer>> list = new ArrayList<>();
				list.add(new Tuple<>(vertex2, channel.getCost()));
				channelMap.put(vertex1, list);
			} else {
				channelMap.get(vertex1).add(new Tuple<>(vertex2, channel.getCost()));
			}
			if (channelMap.get(vertex2) == null) {
				ArrayList<Tuple<Vertex, Integer>> list = new ArrayList<>();
				list.add(new Tuple<>(vertex1, channel.getCost()));
				channelMap.put(vertex2, list);
			} else {
				channelMap.get(vertex2).add(new Tuple<>(vertex1, channel.getCost()));
			}
		});
	}

	private int maxX, maxY;
	private HashMap<Vertex, List<Tuple<Vertex, Integer>>> channelMap;

	/**
	 * 获取某顶点的子节点
	 * @param vertex	当前起始节点
	 * @return	当前起始节点的可达节点及消耗代价
	 */
	public List<Tuple<Vertex, Integer>> vertexCost(Vertex vertex) {
		List<Tuple<Vertex, Integer>> result = new ArrayList<>(9);
		int x = vertex.x;
		int y = vertex.y;
		int time = vertex.time;
		if (x > maxX || y > maxY) {
			return result;
		}
		int[][] straight = {{-1,0},{1,0},{0,-1},{0,1}};
		int[][] diagonal = {{-1,-1},{-1,1},{1,-1},{1,1}};
		IntStream.range(0, straight.length).forEach(i -> {
			int varX = straight[i][0];
			int varY = straight[i][1];
			if (x + varX >= 0 && y + varY >= 0 && x + varX <= maxX && y + varY < maxY) {
				result.add(new Tuple<>(new Vertex(x + varX, y + varY, time), 10));
			}
		});
		IntStream.range(0, diagonal.length).forEach(i -> {
			int varX = diagonal[i][0];
			int varY = diagonal[i][1];
			if (x + varX >= 0 && y + varY >= 0 && x + varX <= maxX && y + varY < maxY) {
				result.add(new Tuple<>(new Vertex(x + varX, y + varY, time), 14));
			}
		});
		List<Tuple<Vertex, Integer>> tuples = channelMap.get(vertex);
		if (null != tuples) {
			result.addAll(tuples);
			//通道只能用一次
			channelMap.remove(vertex);
		}
		return result;
	}

}

class Channel {
	private int time1;
	private int time2;
	private int x;
	private int y;
	private int cost;

	public int getTime1() {
		return time1;
	}

	public void setTime1(int time1) {
		this.time1 = time1;
	}

	public int getTime2() {
		return time2;
	}

	public void setTime2(int time2) {
		this.time2 = time2;
	}

	public int getX() {
		return x;
	}

	public void setX(int x) {
		this.x = x;
	}

	public int getY() {
		return y;
	}

	public void setY(int y) {
		this.y = y;
	}

	public int getCost() {
		return cost;
	}

	public void setCost(int cost) {
		this.cost = cost;
	}

	public Channel(int time1, int time2, int x, int y, int cost) {
		this.time1 = time1;
		this.time2 = time2;
		this.x = x;
		this.y = y;
		this.cost = cost;
	}
}

/**
 * 用于存放顶点以及到达该顶点的代价
 */
class Tuple<Vertex, Integer> {
	private Vertex vertex;
	private int cost;

	public Tuple(Vertex vertex, int cost) {
		this.vertex = vertex;
		this.cost = cost;
	}

	public Vertex getVertex() {
		return vertex;
	}

	public int getCost() {
		return cost;
	}
}

/**
 * 搜索顶点
 */
class Vertex {
	int x,y,time;

	public Vertex(int x, int y, int time) {
		this.x = x;
		this.y = y;
		this.time = time;
	}

	public String key() {
		return time + "-" + x + "-" + y;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		Vertex vertex = (Vertex) o;
		return x == vertex.x &&
				y == vertex.y &&
				time == vertex.time;
	}

	@Override
	public int hashCode() {
		return Objects.hash(x, y, time);
	}
}

/**
 * 搜索路径
 * 若两条路径经过同一个顶点，则取其最短路径
 */
class DefaultPath {
	/**
	 * 整条路径的代价
	 */
	int cost;

	private int estimated;

	public int getEstimated() {
		return estimated;
	}

	public void setEstimated(int estimated) {
		this.estimated = estimated;
	}

	/**
	 * 这里不考虑pathList中是否存在改顶点，调用方自行处理
	 * 若需要其他权重信息，重写之
	 */
	public int fictitiousCost() {
		return cost + estimated;
	}

	/**
	 * 路径上的节点以及到达该节点所需的代价
	 */
	private LinkedList<Tuple<Vertex, Integer>> pathList = new LinkedList<>();

	public int getCost() {
		return cost;
	}

	public void setCost(int cost) {
		this.cost = cost;
	}

	public LinkedList<Tuple<Vertex, Integer>> getPathList() {
		return pathList;
	}

	public void setPathList(LinkedList<Tuple<Vertex, Integer>> pathList) {
		this.pathList = pathList;
	}

	/**
	 * 探索下一个节点
	 * @param vertex	当前顶点
	 * @param currentCost	到下一个节点的path cost
	 * @return	路径
	 */
	public DefaultPath explore(Vertex vertex, int currentCost) {
		DefaultPath path = new DefaultPath();
		LinkedList<Tuple<Vertex, Integer>> vertices = new LinkedList<>(pathList);
		vertices.add(new Tuple<>(vertex, currentCost));
		path.setPathList(vertices);
		path.setCost(cost + currentCost);
		return path;
	}

}
