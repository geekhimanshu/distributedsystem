import org.apache.zookeeper.*;
import org.apache.zookeeper.data.Stat;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

public class LeaderElection implements Watcher {

    private static final String ZOOKEPER_ADDRESS = "localhost:2181";
    private static final int SESSION_TIMEOUT = 3000;
    private static final String ELECTION_NAMESPACE = "/election";
    private static final String TARGET_ZNODE = "/target_znode";
    private ZooKeeper zooKeeper;
    private String currentZnodeName;

//    public static void main(String [] args) throws IOException, InterruptedException, KeeperException {
//        LeaderElection leaderElection = new LeaderElection();
//        leaderElection.connectToZooKeeper();
//        leaderElection.watchTargetZnode();
//        leaderElection.run();
//        leaderElection.close();
//    }

    public static void main(String [] args) throws IOException, InterruptedException, KeeperException {
        LeaderElection leaderElection = new LeaderElection();
        leaderElection.connectToZooKeeper();
        leaderElection.volunteerForLeadership();
        leaderElection.reElectLeader();
        leaderElection.run();
        leaderElection.close();
        System.out.println("Disconnected from zookeeper. Existing application");
    }

    public void volunteerForLeadership() throws InterruptedException, KeeperException {
        String znodePrefix = ELECTION_NAMESPACE + "/c_";
        String znodeFullPath = zooKeeper.create(znodePrefix, new byte[]{}, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL_SEQUENTIAL);

        System.out.println(znodeFullPath);
        this.currentZnodeName = znodeFullPath.replace(ELECTION_NAMESPACE + "/", "");
    }

    public void reElectLeader() throws InterruptedException, KeeperException {
        Stat predecessorStat = null;
        String predecessorZnodeName = "";
        while(predecessorStat == null) {

            List<String> children = this.zooKeeper.getChildren(ELECTION_NAMESPACE, false);
            Collections.sort(children);
            String smallestChild = children.get(0);

            if(smallestChild.equals(currentZnodeName)) {
                System.out.println("I am the leader");
                return;
            } else {
                System.out.println("I am not the leader, " + smallestChild + " is the leader");
                int predecessorIndex = Collections.binarySearch(children, currentZnodeName) - 1;
                predecessorZnodeName = children.get(predecessorIndex);
                predecessorStat = zooKeeper.exists(ELECTION_NAMESPACE + "/" + predecessorZnodeName, this);
            }
        }

        System.out.println("Watching znode: " + predecessorZnodeName);
    }

    public void connectToZooKeeper() throws IOException {
        this.zooKeeper = new ZooKeeper(ZOOKEPER_ADDRESS, SESSION_TIMEOUT, this);
    }

    public void run() throws InterruptedException {
        synchronized (zooKeeper) {
            this.zooKeeper.wait();
        }
    }

    private void close() throws InterruptedException {
        this.zooKeeper.close();
    }

    public void watchTargetZnode() throws InterruptedException, KeeperException {
        Stat stat = zooKeeper.exists(TARGET_ZNODE, this);
        if(stat == null) return;
        byte[] data = zooKeeper.getData(TARGET_ZNODE, this, stat);
        List<String> children = zooKeeper.getChildren(TARGET_ZNODE, this);

        System.out.println("Data: " + new String(data) + " children: " + children);
    }

    @Override
    public void process(WatchedEvent watchedEvent) {
        switch (watchedEvent.getType()) {
            case None:
                if(watchedEvent.getState() == Event.KeeperState.SyncConnected) {
                    System.out.println("Sucessfully connected to zookeeper");
                } else {
                    synchronized (zooKeeper) {
                        System.out.println("Disconnected from zookeeper");
                        zooKeeper.notifyAll();
                    }
                }
                break;
            case NodeDeleted:
                System.out.println(TARGET_ZNODE + " was deleted");
                try {
                    reElectLeader();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                } catch (KeeperException e) {
                    throw new RuntimeException(e);
                }
                break;
            case NodeCreated:
                System.out.println(TARGET_ZNODE + " was created");
                break;
            case NodeChildrenChanged:
                System.out.println(TARGET_ZNODE + " children changed");
                break;
            case NodeDataChanged:
                System.out.println(TARGET_ZNODE + " data changed");
                break;
        }

        try {
            watchTargetZnode();
        } catch (KeeperException e) {

        } catch (InterruptedException e) {

        }
    }
}
