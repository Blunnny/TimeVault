package gitlet;


import java.io.File;
import java.io.Serial;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 *  @author JIN
 */
public class Commit implements Serializable, Dumpable {

    @Serial
    private static final long serialVersionUID = 1323192079276861884L;

    private String message; // 描述信息
    private Date timeStamp; // 时间戳
    private String parent1; // 当前提交的第一父提交
    private String parent2; // 当前提交的第二父提交
    Map<String, String> mapFileNameToBlob = new HashMap<>(); // 保存文件名及文件内容哈希映射
    private String commitID; // 该提交的哈希值

    // 初始提交
    public Commit() {
        this.message = "initial commit";
        this.parent1 = null;
        this.parent2 = null;
        this.timeStamp = new Date(0);
        this.commitID = Utils.sha1(Utils.serialize(this));
    }

    // 普通提交
    public Commit(String message, String parent1, String parent2,
                  Map<String, String> mapFileNameToBlob) {
        this.message = message;
        this.parent1 = parent1;
        this.parent2 = parent2;
        this.timeStamp = new Date();
        this.mapFileNameToBlob = mapFileNameToBlob;
        this.commitID = Utils.sha1(Utils.serialize(this));
    }

    // 获取各类信息的方法
    public String getMessage() {
        return message;
    }
    public String getFirstParent() {
        return parent1;
    }
    public String getSecondParent() {
        return parent2;
    }
    public String getCommitID() {
        return commitID;
    }


    // 从文件中读取提交
    // 从完整的 commitID 中读取
    public static Commit fromFile(String requiredCommitID) {
        // 完整的 commitID 为 40 位，若长度小于 40 位，则使用缩写方法读取
        final int standardCommitIDLength = 40;
        int requiredCommitIDLength = requiredCommitID.length();
        if (requiredCommitIDLength < standardCommitIDLength) {
            return shortFromFile(requiredCommitID);
        }
        // 查找 commitID 对应的 commit
        File commitFile = Utils.join(Repository.COMMITS_DIR, requiredCommitID);
        if (!commitFile.exists()) {
            return null;
        }
        return Utils.readObject(commitFile, Commit.class);
    }
    // 从缩写的 commitID 中读取
    public static Commit shortFromFile(String shortCommitID) {
        int shortLength = shortCommitID.length();
        String matchedCommitID = "";
        // 遍历 COMMIT_DIR 目录，寻找以 shortCommitID 开头的完整 commitID
        for (String commitID : Utils.plainFilenamesIn(Repository.COMMITS_DIR)) {
            if (commitID.substring(0, shortLength).equals(shortCommitID)) {
                matchedCommitID = commitID;
            }
        }
        File commitFile = Utils.join(Repository.COMMITS_DIR, matchedCommitID);
        if (!commitFile.exists()) {
            return null;
        }
        return Utils.readObject(commitFile, Commit.class);
    }

    // 保存提交，文件名为 commitID
    public void saveCommit() {
        if (!Repository.COMMITS_DIR.exists()) {
            Repository.COMMITS_DIR.mkdir();
        }
        File saveFile = Utils.join(Repository.COMMITS_DIR, commitID);
        Utils.writeObject(saveFile, this);
    }

    // 获取文件内容
    public String getFileContentInCommit(String fileName) {
        if (!mapFileNameToBlob.containsKey(fileName)) {
            return null;
        }
        String fileBlobID = mapFileNameToBlob.get(fileName);
        String fileContent = Blob.contentFromFile(fileBlobID);
        return fileContent;
    }

    // 格式化输出 commit 详情
    @Override
    public String toString() {
        String s = "===" + "\n";
        s += "commit" + commitID + "\n";

        if (parent2 != null) {
            int shortParentIDLength = 7;
            String parent1Short = parent1.substring(0, shortParentIDLength);
            String parent2Short = parent2.substring(0, shortParentIDLength);
            s += "Merge: " + parent1Short + " " + parent2Short + "\n";
        }

        SimpleDateFormat sdf = new SimpleDateFormat(
                "E MMM dd HH:mm:ss yyyy Z", Locale.ENGLISH);
        s += "Date: " + sdf.format(timeStamp) + "\n";
        s += message + "\n";
        return s;
    }

    // 获取当前分支的 commit 历史
    public static String getCurrentCommitLog(String currentCommitID) {
        StringBuilder log = new StringBuilder();
        String commitID = currentCommitID;
        while (commitID != null) {
            Commit commitToBePresented = fromFile(commitID);
            log.append(commitToBePresented.toString()).append("\n");
            commitID = commitToBePresented.getFirstParent();
        }
        return log.substring(0, log.length() - 1);
    }

    // 获取所有的 commit 历史
    public static String getGlobalCommitLog() {
        StringBuilder globalLog = new StringBuilder();
        List<String> allCommitsID = Utils.plainFilenamesIn(Repository.COMMITS_DIR);
        assert allCommitsID != null;
        for (String commitID : allCommitsID) {
            Commit commit = fromFile(commitID);
            globalLog.append(commit.toString()).append("\n");
        }
        return globalLog.substring(0, globalLog.length() - 1);
    }

    // 查找所有 message 相同的 commit
    public static List<String> findCommitLogWithMessage(String message) {
        List<String> commitIDsToReturn = new ArrayList<>();
        List<String> allCommitsID = Utils.plainFilenamesIn(Repository.COMMITS_DIR);
        assert allCommitsID != null;
        for (String commitID : allCommitsID) {
            Commit commit = fromFile(commitID);
            if (commit.getMessage().equals(message)) {
                commitIDsToReturn.add(commit.getCommitID());
            }
        }
        return commitIDsToReturn;
    }

    @Override
    public void dump() {
        System.out.println("gitlet log of this commit:");
        System.out.println(this);
        System.out.println("map file name to blob:");
        for (String fileName : mapFileNameToBlob.keySet()) {
            System.out.printf("file name: %s to blob ID: %s%n",
                    fileName, mapFileNameToBlob.get(fileName));
        }
        System.out.println("parent1: " + parent1);
        System.out.println("parent2: " + parent2);
        }
    }
}







