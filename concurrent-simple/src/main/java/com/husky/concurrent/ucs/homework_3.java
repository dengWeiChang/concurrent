import java.util.*;
import java.io.File;
import java.io.FileWriter;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;

public class homework_3 {
    public static void main(String[] args) throws IOException {

        long startTime = System.currentTimeMillis();

        String path = "/Users/dengweichang/Documents/testcase/input4.txt";
        Problem p = Read.ReadInput(path);
        Method m = new Method();
        if (p.getAlgorithm().equals("BFS")){
            System.out.println("Do BFS...");
            m.Search(p);
        }else if (p.getAlgorithm().equals("UCS")){
            System.out.println("Do UCS...");
            m.Search(p);
        }else if (p.getAlgorithm().equals("A*")) {
            System.out.println("Do A*...");
            m.Search(p);
        }else{
            System.out.println("Wrong Algorithm!");
        }

        long endTime = System.currentTimeMillis();
        System.out.println("程序运行时间：" + (endTime - startTime) + "ms");


    }
}


class Method {


    public void Search(Problem p) throws IOException {

        //不同算法给openList排序方法不同
        PriorityQueue<Node> openList = new PriorityQueue<>();
        switch (p.getAlgorithm()) {
            case "BFS":
                Comparator<Node> comparator_BFS = new Comparator<Node>() {
                    @Override
                    public int compare(Node s1, Node s2) {
                        return 0;
                    }
                };
                openList = new PriorityQueue<>(comparator_BFS);
                break;
            case "UCS":
                Comparator<Node> comparator_UCS = new Comparator<Node>() {
                    @Override
                    public int compare(Node s1, Node s2) {
                        if (s1.getPathCost() > s2.getPathCost()) {
                            return 1;
                        } else {
                            return -1;
                        }
                    }
                };
                openList = new PriorityQueue<>(comparator_UCS);
                break;
            case "A*":
                Comparator<Node> comparator_AStar = new Comparator<Node>() {
                    @Override
                    public int compare(Node s1, Node s2) {
                        if((s1.getPathCost()+s1.getEstimateCost())==(s2.getPathCost()+s2.getEstimateCost())){
                            return 0;
                        } else if ((s1.getPathCost() + s1.getEstimateCost()) > (s2.getPathCost() + s2.getEstimateCost())) {
                            return 1;
                        } else {
                            return -1;
                        }
                    }
                };
                openList = new PriorityQueue<>(comparator_AStar);
                break;
            default:
                System.out.println("Wrong Algorithm");
                break;
        }


        /*
         
        流程：

        0.weight[走直线cost，走对角cost，走通道cost]
          给不同的算法的操作赋值：bfs[1,1,1]；A*&ucs[10,14,time差]；其中time差在class Node.findChildren出算好了
          若起点=终点，直接打印
        1.取openList的第一个节点
        2.判断，若是goal，打印path

        
        */

        Node root = new Node();
        root.setState(p.getInitial());
        openList.offer(root);
        HashSet<Node> closedList = new HashSet<>();
        int weight[] = new int[3];
        if (p.getAlgorithm() == "BFS") {
            weight[0] = 1;
            weight[1] = 1;
            weight[2] = 1;
        } else {
            weight[0] = 10;
            weight[1] = 14;
        }

        if (p.isStartEqualEnd()) {
            int[] t = p.getInitial();
            String c = "0" + "\n" + "1" + "\n" + t[0] + " " + t[1] + " " + t[2] + 0;
            writeOutput(c);
        } else {
            while (true) {
                if (openList.isEmpty()) {
                    String c = "FAIL";
                    writeOutput(c);
                    return;
                } else {
                    Node n = openList.poll();
                    if (n.goalTest(p)) {
                        Path(n);
                        break;
                    } else {
                        closedList.add(n);
                        n.findChildren(p, weight);
                        ArrayList<Node> kids = n.getChildren();
                        for (int i = 0; i < kids.size(); i++) {
                                if (closedList.contains(kids.get(i))) {
                                continue;
                            }
                            openList.offer(kids.get(i));
                        }
                    }
                }
            }

        }
    }




    // 找路径：从这个node开始向上找parent，知道partner为null
    public void Path(Node node) throws IOException {
        ArrayList<int[]> path = new ArrayList<>();
        int[] s = node.getState();
        path.add(new int[]{s[0], s[1], s[2], node.getStepCost()});
        while (node.getParent() != null) {
            node = node.getParent();
            int[] p = node.getState();
            path.add(0, new int[]{p[0], p[1], p[2], node.getStepCost()});
        }

        String p = "\n";
        int totalCost = 0;
        for (int j = 0; j < path.size(); j++) {
            String c = path.get(j)[0] + " " + path.get(j)[1] + " " + path.get(j)[2] + " " + path.get(j)[3] + "\n";
            p = p + c;
            totalCost = totalCost + path.get(j)[3];
        }
        String f = Integer.toString(totalCost) + "\n" + path.size() + p;
        writeOutput(f);

    }

    //打印path到文件
    public void writeOutput(String s) throws IOException {
        File file = new File("./output.txt");
        if (!file.exists()) {
            file.createNewFile();
        } else {
            file.delete();
            FileWriter fw = new FileWriter("./output.txt", true);
            fw.write(s);
            fw.flush();
            fw.close();
        }

    }
}


/*
    三个有用的：
        1.重写了equals方法，state一样即一样
        2.findChildren：找到这个点可能产生的child：
                                    channel point九个：8+channel另一端
                                    普通点：八个 上 下 左 右 左上 左下 右上 右下
        3.goaltest：判断这个node是不是终点
*/
class Node {
    private int index;
    private Node parent;
    private ArrayList<Node> children;
    private int[] state;
    private int depth;
    private int estimateCost;
    private int pathCost;
    private int stepCost;

    //Getter and Setter


    public int getStepCost() {
        return stepCost;
    }

    public void setStepCost(int stepCost) {
        this.stepCost = stepCost;
    }

    public int getPathCost() {
        return pathCost;
    }

    public void setPathCost(int pathCost) {
        this.pathCost = pathCost;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public Node getParent() {
        return parent;
    }

    public void setParent(Node parent) {
        this.parent = parent;
    }

    public ArrayList<Node> getChildren() {
        return children;
    }


    public int[] getState() {
        return state;
    }

    public void setState(int[] state) {
        this.state = state;
    }

    public int getDepth() {
        return depth;
    }

    public void setDepth(int depth) {
        this.depth = depth;
    }

    public int getEstimateCost() {
        return estimateCost;
    }

    public void setEstimateCost(int estimateCost) {
        this.estimateCost = estimateCost;
    }

    //Constructor
    public Node() {
    }

    public Node(int index) {
        this.index = index;
    }

    public Node(int index, Node parent, ArrayList<Node> children, int[] state, int depth, int estimateCost, int pathCost) {
        this.index = index;
        this.parent = parent;
        this.children = children;
        this.state = state;
        this.depth = depth;
        this.estimateCost = estimateCost;
        this.pathCost = pathCost;
    }

    //===============================================================
    //expand node: apply all actions to get kids

    public ArrayList<Node> findChildren(Problem p, int[] weight) {
        ArrayList<Node> children = new ArrayList<>();
        int normalCost = weight[0], diagonalCost = weight[1], jauntCost = weight[2];
        int index = this.getIndex(), level = this.getDepth(), cost = this.getPathCost();
        int[] current = this.getState(), size = p.getGrid();
        int year = current[0];
        //8 directions:Northwest,West,Southwest,North,South,Northeast,East,Southeast
        int dx[] = {-1, -1, -1, 0, 0, 1, 1, 1}, dy[] = {1, 0, -1, 1, -1, 1, 0, -1};
        for (int i = 0; i < 8; i++) {
            int x = current[1] + dx[i];
            int y = current[2] + dy[i];
            if (x >= 0 && x <= size[0] - 1 && y >= 0 && y <= size[1] - 1) {
                Node n1 = new Node(index + 1 + i);
                n1.setDepth(level + 1);
                n1.setParent(this);
                n1.setState(new int[]{year, x, y});
                int dis = Math.abs(x - current[1]) + Math.abs(y - current[2]);
                if (dis == 2) {
                    n1.setPathCost(cost + diagonalCost);
                    n1.setStepCost(diagonalCost);
                } else if (dis == 1) {
                    n1.setPathCost(cost + normalCost);
                    n1.setStepCost(normalCost);
                }
                n1.setEstimateCost(0);
                children.add(n1);
            }
        }
        for (int[] ints : p.getChannels()) {
            boolean hasChannel = (year == ints[0] || year == ints[3]) && current[1] == ints[1] && current[2] == ints[2];
            if (hasChannel) {
                Node n2 = new Node(index + children.size() + 1);
                n2.setDepth(level + 1);
                n2.setParent(this);
                if (year == ints[0]) {
                    n2.setState(new int[]{ints[3], current[1], current[2]});
                } else {
                    n2.setState(new int[]{ints[0], current[1], current[2]});
                }
                if (p.getAlgorithm() == "BFS"){
                    jauntCost = 1;
                }else{
                    jauntCost = Math.abs(ints[3]-ints[0]);
                }
                n2.setPathCost(cost + jauntCost);
                n2.setStepCost(jauntCost);
                int h = Math.abs(n2.getState()[0] - p.getTarget()[0]);
                n2.setEstimateCost(h);
                children.add(n2);
            }
        }
        this.children = children;
        return this.children;
    }

    //=================================================================

    public boolean goalTest(Problem p) {
        int[] s = this.getState();
        int[] t = p.getTarget();
        System.out.println("current node --" + s[0] + ":" + s[1] + ":" + s[0]);
        if (s[0]==t[0] && s[1]==t[1] && s[2]==t[2]) {
            return true;
        } else {
            return false;
        }
    }

    //override toString
    public String printNode() {
        String s = Arrays.toString(state);

        ArrayList<String> c = new ArrayList<>();
        if (this.children != null) {
            for (int i = 0; i < this.children.size(); i++) {
                //Node t = this.children.get(i);
                c.add("Node" + this.children.get(i).getIndex());
            }
        } else
            c = null;

        String p;
        if (this.parent != null) {
            p = "Node" + this.parent.getIndex();
        } else
            p = null;

        StringBuilder sb = new StringBuilder();
        sb.append("==================" + "\n").
                append("The information of Node" + index + " :" + "\n").append("parent is: " + p + "\n").
                append("children are: " + c + "\n").append("current position is: " + s + "\n").
                append("at level " + depth + "\n").append("estimate future cost is: " + estimateCost + "\n").
                append("cost from root is: " + pathCost + "\n").
                append("==================" + "\n");
        //System.out.println(sb);
        return sb.toString();
    }

    @Override
    public String toString() {
        return printNode();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Node)) return false;
        Node node = (Node) o;
        return Arrays.equals(getState(), node.getState());
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(getState());
    }
}


/*
  记录input.txt的信息
*/
class Problem {
    private String algorithm;
    private int[] grid = new int[2];
    private int[] initial = new int[3];
    private int[] target = new int[3];
    private int channelCount;
    private ArrayList<int[]> channels;


    //getter and setter
    public String getAlgorithm() {
        return algorithm;
    }

    public void setAlgorithm(String algorithm) {
        this.algorithm = algorithm;
    }

    public int[] getGrid() {
        return grid;
    }

    public void setGrid(int[] grid) {
        this.grid = grid;
    }

    public int[] getInitial() {
        return initial;
    }

    public void setInitial(int[] initial) {
        this.initial = initial;
    }

    public int[] getTarget() {
        return target;
    }

    public void setTarget(int[] target) {
        this.target = target;
    }

    public int getChannelCount() {
        return channelCount;
    }

    public void setChannelCount(int channelCount) {
        this.channelCount = channelCount;
    }

    public ArrayList<int[]> getChannels() {
        return channels;
    }

    public void setChannels(ArrayList<int[]> channels) {
        this.channels = channels;
    }

    //constructor

    public Problem() {
    }

    public Problem(String algorithm, int[] grid, int[] initial, int[] target, int channelCount, ArrayList<int[]> channels) {
        this.algorithm = algorithm;
        this.grid = grid;
        this.initial = initial;
        this.target = target;
        this.channelCount = channelCount;
        this.channels = channels;

    }


    //override toString
    @Override
    public String toString() {
        return "Problem{" +
                "algorithm='" + algorithm + '\'' +
                ", grid size=" + Arrays.toString(grid) +
                ", initial position=" + Arrays.toString(initial) +
                ", target position=" + Arrays.toString(target) +
                ", channel number=" + channelCount +
                ", each channel:" + Arrays.deepToString(channels.toArray()) +
                "}";
    }

    public boolean isStartEqualEnd(){
        int[] i = this.getInitial();
        int[] t = this.getTarget();
        if ((i[0] == t[0]) && (i[1] == t[1]) && (i[2]==t[2])) {
            return true;
        } else{
            return false;
        }
    }
}



//读input.txt
class Read {

    public static Problem ReadInput(String path) throws IOException {

        FileInputStream fstream = new FileInputStream(path);
        BufferedReader input = new BufferedReader(new InputStreamReader(fstream));

        ArrayList<String> arr = new ArrayList<>();
        String line;
        Problem p = new Problem();
        while ((line = input.readLine()) != null) {
            arr.add(line);
        }

        p.setAlgorithm(arr.get(0));

        //change string to int[]
        String[] arr_int1 = arr.get(1).split("\\s+");
        int[] gridSize = new int[arr_int1.length];
        gridSize[0] = Integer.parseInt(arr_int1[0]);
        gridSize[1] = Integer.parseInt(arr_int1[1]);
        p.setGrid(gridSize);

        String[] arr_int2 = arr.get(2).split("\\s+");
        int[] initial = new int[arr_int2.length];
        for (int i = 0; i < initial.length; i++) {
            initial[i] = Integer.parseInt(arr_int2[i]);
        }
        p.setInitial(initial);

        String[] arr_int3 = arr.get(3).split("\\s+");
        int[] target = new int[arr_int3.length];
        for (int i = 0; i < target.length; i++) {
            target[i] = Integer.parseInt(arr_int3[i]);
        }
        p.setTarget(target);

        p.setChannelCount(Integer.parseInt(arr.get(4)));

        ArrayList<int[]> channels = new ArrayList();
        for (int i = 5; i < arr.size(); i++) {
            String[] arr_int = arr.get(i).split("\\s+");
            int[] channel = new int[arr_int.length];
            for (int j = 0; j < channel.length; j++) {
                channel[j] = Integer.parseInt(arr_int[j]);
            }
            channels.add(channel);
        }
        p.setChannels(channels);

        //System.out.println(p);
        input.close();
        return p;
    }
}







