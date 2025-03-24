package gitlet;

import java.io.File;
import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Gitlet 项目中的暂存区 */
public class StagingArea implements Serializable, Dumpable {
    @Serial
    private static final long serialVersionUID = -8161863113995390299L;
    /** 记录文件名以及对应 blob ID 的对应表 */
    private Map<String, String> stagedForAddition;
    /** 记录待删除的文件名 */
    private List<String> stagedForRemoval;


    public StagingArea() {
        if (Repository.GITLET_DIR.exists()) {
            File stagingAreaFile = Utils.join(Repository.GITLET_DIR, "stagingArea");
            // 根据 stagingArea 是否存在，加载或创建 stagingArea
            // 实现持久化运行，不会因程序重启而丢失数据
            if (stagingAreaFile.exists()) {
                StagingArea stagingArea = Utils.readObject(stagingAreaFile, StagingArea.class);
                this.stagedForAddition = stagingArea.stagedForAddition;
                this.stagedForRemoval = stagingArea.stagedForRemoval;
            } else {
                this.stagedForAddition = new HashMap<>();
                this.stagedForRemoval = new ArrayList<>();
                saveStagingArea();
            }
        }
    }

    /** 查看被添加的文件 */
    public Map<String, String> getStagedForAddition() {
        return stagedForAddition;
    }

    /** 查看被删除的文件 */
    public List<String> getStagedForRemoval() {
        return stagedForRemoval;
    }

    /** 保存 StagingArea */
    public void saveStagingArea() {
        // 将暂存区内容写入文件 "stagingArea"
        File saveFile = Utils.join(Repository.GITLET_DIR, "stagingArea");
        Utils.writeObject(saveFile, this);
    }

    /** 添加文件到暂存区 */
    public void addFile(String fileName, String currentCommitID) {
        // 如果文件之前被标记为删除，则取消删除
        if (stagedForRemoval.contains(fileName)) {
            stagedForRemoval.remove(fileName);
        }

        Commit currentCommit = Commit.fromFile(currentCommitID);
        Map<String, String> mapFileNameToBlobIDInCurrentCommit = currentCommit.mapFileNameToBlob;

        File fileToBeAdded = new File(fileName);
        String newFileContent = Utils.readContentsAsString(fileToBeAdded);


        if (mapFileNameToBlobIDInCurrentCommit.containsKey(fileName)) {
            String oldBlobID = mapFileNameToBlobIDInCurrentCommit.get(fileName);
            String fileContentInOldBlob = Blob.contentFromFile(oldBlobID);
            // 如果文件和当前 commit 中的版本相同，则不需要添加，直接返回
            if (newFileContent.equals(fileContentInOldBlob)) {
                return;
            }
            // 如果内容不同，则创建新的 blob 并存入 stagedForAddition
            else {
                Blob newBlob = new Blob(newFileContent);
                stagedForAddition.put(fileName, newBlob.getBlobID());
                newBlob.saveBlob();
            }
        }
        // 如果文件未被当前 commit 追踪，则同样创建新的 blob 并存入 stagedForAddition
        else {
            Blob newBlob = new Blob(newFileContent);
            stagedForAddition.put(fileName, newBlob.getBlobID());
            newBlob.saveBlob();
        }
    }

    /** 移除文件 */
    public void removeFile(String fileName, String currentCommitID) {
        // 如果文件在 stagedForAddition 中，就从暂存区中移除
        if (stagedForAddition.containsKey(fileName)) {
            stagedForAddition.remove(fileName);
        }
        // 如果文件被当前 commit 追踪, 则把它加入 stagedForRemoval
        Commit currentCommit = Commit.fromFile(currentCommitID);
        Map<String, String> mapFileNameToBlobIDInCurrentCommit = currentCommit.mapFileNameToBlob;
        if (mapFileNameToBlobIDInCurrentCommit.containsKey(fileName)) {
            stagedForRemoval.add(fileName);
            boolean successfullyRemoved = Utils.restrictedDelete(fileName);
        }
    }

    /** 清空暂存区 */
    public void clear() {
        // 清空待添加和待删除区
        stagedForAddition.clear();
        stagedForRemoval.clear();
    }

    // 打印暂存区内容
    @Override
    public void dump() {
        System.out.println("stagedForAddition: ");
        for (String fileName : stagedForAddition.keySet()) {
            System.out.printf("file name: %s to blob ID: %s%n",
                    fileName, stagedForAddition.get(fileName));
        }
        System.out.println("stagedForRemoval: ");
        for (String fileName : stagedForRemoval) {
            System.out.print(fileName + " ");
        }
        System.out.println();
    }
}