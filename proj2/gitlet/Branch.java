package gitlet;

import java.io.File;
import java.io.Serial;
import java.io.Serializable;

/** 用于表示分支的类，管理不同的提交路径，并指向最新的 commit*/
public class Branch implements Serializable, Dumpable {
    @Serial
    private static final long serialVersionUID = -5094230405275657934L;
    /** 分支的名称 */
    private String name;
    /** 分支指向的 commit 的 SHA1 ID */
    private String commitID;

    /** 创建一个新的分支对象 */
    public Branch(String name, String commitID) {
        this.name = name;
        this.commitID = commitID;
    }

    /** 获取分支的名称 */
    public String getName() {
        return name;
    }

    /** 返回分支指向的 commit 的 SHA1 ID. */
    public String getCommitID() {
        return commitID;
    }

    /** 从文件中加载分支信息 */
    public static Branch fromFile(String branchName) {
        File branchFile = Utils.join(Repository.BRANCH_DIR, branchName);
        if (!branchFile.exists()) {
            return null;
        }
        return Utils.readObject(branchFile, Branch.class);
    }

    /** 保存分支 */
    public void saveBranch() {
        if (!Repository.BRANCH_DIR.exists()) {
            Repository.BRANCH_DIR.mkdir();
        }
        File saveFile = Utils.join(Repository.BRANCH_DIR, name);
        Utils.writeObject(saveFile, this);
    }

    /** 更新分支（当分支有新的 commit 时） */
    public void update(String newCommitID) {
        this.commitID = newCommitID;
        saveBranch();
    }

    /** 删除分支 */
    public static void removeBranch(String branchName) {
        File branchToRemove = Utils.join(Repository.BRANCH_DIR, branchName);
        if (!branchToRemove.exists()) {
            return;
        }
        branchToRemove.delete();
    }

    // 打印当前分支的名称和 commit ID
    @Override
    public void dump() {
        System.out.printf("name: %s%ncommitID: %s%n", name, commitID);
    }
}