package gitlet;

import edu.princeton.cs.algs4.In;

import java.io.File;
import java.io.Serializable;
import java.sql.Blob;
import java.util.*;


import static gitlet.Utils.*;


/** 实现主要功能
 *  @author Jin
 */
public class Repository {
    /** 当前工作目录. */
    public static final File CWD = new File(System.getProperty("user.dir"));
    /** .gitlet 目录(位于当前工作目录下 )，储存所有仓库数据. */
    public static final File GITLET_DIR = join(CWD, ".gitlet");
    /** .commit 目录 (位于 .gitlet 目录下 )，储存所有 commit 数据. */
    public static final File COMMITS_DIR = join(GITLET_DIR, ".commit");
    /** 储存所有分支信息数据的文件 */
    public static final File BRANCH_DIR = join(GITLET_DIR, ".branches");
    /** 储存文件快照 blob 的文件. */
    public static final File BLOB_DIR = join(GITLET_DIR, "blobs");
    /** head 指针，指向当前 commit 的唯一 ID. */
    private String head;
    /** 当前激活的分支名称，默认是 master. */
    private String activatedBranchName;

    /** 加载已存在的仓库数据 */
    public Repository() {
        // 如果 GITLET_DIR 存在（已初始化）
        if (GITLET_DIR.exists()) {
            // 定位到 repo 文件，该文件中只有 head 和 activatedBranchName两个指针数据
            File repository = join(GITLET_DIR, "repo");
            if (repository.exists()) {
                // 读取 repo 文件，将其反序列化成一个 Repository 对象
                Repository repo = readObject(repository, Repository.class);
                this.head = repo.head;
                this.activatedBranchName = repo.activatedBranchName;
            }
        }
    }

    /** 保存当前仓库数据 */
    public void saveRepository() {
        File saveFile = join(GITLET_DIR, "repo"); // 目标存储文件 .gitlet/repo
        writeObject(saveFile, (Serializable) this); // 将当前 Repository 实例序列化并存储到 repo 文件
    }

    /** 初始化一个 Gitlet 版本控制系统 */
    public void init() {
        // 如果 GITLET_DIR 存在，则打印错误消息并退出
        if (GITLET_DIR.exists()) {
            System.out.println("A Gitlet version-control system already"
            + "exists in the current directory");
            System.exit(0);
        }
        // 创建 gitlet 目录，作为所有版本控制数据的根目录
        GITLET_DIR.mkdir();
        // 初始化首个提交
        COMMITS_DIR.mkdir(); // 创建 commits 目录用于存放所有 commit 对象
        Commit initCommit = new Commit(); // 创建初始提交
        initCommit.saveCommit(); // 将初始提交储存进 commits 目录
        // 创建 master 分支
        BRANCH_DIR.mkdir(); // 创建 branches 目录用于存放所有分支信息
        Branch branch = new Branch("master", initCommit.getCommitID()); // master 分支指向初始提交
        branch.saveBranch(); // 将 master 分支保存进 branch 目录中
        // 设置 head 与 activatedBranchName
        head = initCommit.getCommitID(); // head 指向初始提交的 ID
        activatedBranchName = "master"; // 将当前活跃的分支摄制为 master
        this.saveRepository(); // 将当前仓库对象的指针信息保存进 repo 文件中
        // 初始化暂存区
        StagingArea stagingArea = new StagingArea(); // 创建暂存区对象 stagingArea
        stagingArea.saveStagingArea(); // 保存暂存区对象
        // 创建 blobs 目录, 用于存储文件快照
        BLOB_DIR.mkdir();
    }

    /** 将文件添加到暂存区 */
    public void add(String fileName) {
        File fileToBeAdded = new File(fileName);
        // 如果文件不存在，则报错并退出
        if(!fileToBeAdded.exists()) {
            System.out.println("File does not exist!");
            System.exit(0);
        }
        // 检查是否已有储存的暂存区，有则读取，没有则创建新的暂存区
        StagingArea stagingArea = new StagingArea();
        // 添加文件到暂存区
        stagingArea.addFile(fileName, head);
        // 保存更新后的暂存区
        stagingArea.saveStagingArea();
    }

    /** 创建一个新的提交 */
    public void commit(String message) {
        commitWithMerge(message, null); // *
    }

    private void commitWithMerge(String message, String secondParent) {
        // 加载之前的暂存区，使得本次提交基于已有的暂存变更
        StagingArea stagingArea = new StagingArea();
        // 如果没有文件被添加或删除，说明没有变更，打印错误消息并退出
        if (stagingArea.getStagedForAddition().isEmpty()
                && stagingArea.getStagedForRemoval().isEmpty()) {
            System.out.println("No changes added to the commit!");
            System.exit(0);
        }
        // 检查打印消息是否为空（git 中打印消息不能为空）
        if (message.isEmpty()) {
            System.out.println("Please enter a commit message!");
        }

        // 读取当前 commit 文件（head 为当前 commit 的 id）
        Commit currentCommit = Commit.fromFile(head);
        // 新 commit 继承记录当前 commit 跟踪的所有文件的 map，在此基础上修改
        // 文件快照的格式是 Map<String, String>，其中包括 Key（文件名） 和 Value （Blob ID （文件内容的哈希值））
        Map<String, String> newCommitMapFileNameToBlob = currentCommit.mapFileNameToBlob;
        // 从当前 commit 继承的文件快照中，删除所有被标记为删除的文件
        for (String fileToBeRemoved : stagingArea.getStagedForRemoval()) {
            newCommitMapFileNameToBlob.remove(fileToBeRemoved);
        }
        // 对修改的文件进行更新
        for (String fileToBeAddedOrModified : stagingArea.getStagedForAddition().keySet()) {
            newCommitMapFileNameToBlob.put(fileToBeAddedOrModified,
                    stagingArea.getStagedForAddition().get(fileToBeAddedOrModified));
        }

        // 创建新的 commit 对象
        Commit newCommit = new Commit(message, head, secondParent, newCommitMapFileNameToBlob);
        // 使 head 指向新的 commit
        head = newCommit.getCommitID();
        // 将新的 commit 保存在 commit 目录中
        newCommit.saveCommit();

        // 读取当前分支记录
        Branch branch = Branch.fromFile(activatedBranchName);
        // 将当前分支指向最新的 commit
        branch.update(newCommit.getCommitID());

        // 清空暂存区（已提交）
        stagingArea.clear();
        stagingArea.saveStagingArea();
        // 保存仓库内容
        saveRepository();
    }

    /** 删除文件 */
    public void remove(String fileName) {
        // 读取当前 head 指向的 commit
        Commit currentCommit = Commit.fromFile(head);
        // 读取暂存区
        StagingArea stagingArea = new StagingArea();
        // 处理无法删除的情况，打印错误消息并退出
        // 文件不在当前 commit 中 、不在暂存区、没有被标记删除
        if (!currentCommit.mapFileNameToBlob.containsKey(fileName)
                && !stagingArea.getStagedForAddition().containsKey(fileName)
                && !stagingArea.getStagedForRemoval().contains(fileName)) {
            System.out.println("No reason to remove the file!");
            System.exit(0);
        }
        // 若文件在 commit 中，则标记为 staged for removal
        // 若文件在暂存区， 则直接移除
        stagingArea.removeFile(fileName, currentCommit.getCommitID());
        // 保存修改后的暂存区
        stagingArea.saveStagingArea();
    }

    /** 从当前 head 打印的 commit 开始，沿着父 commit 逆向遍历，打印 commit 历史 */
    public void printLog() {
        String commitLog = Commit.getCurrentCommitLog(head);
        System.out.println(commitLog);
    }

    /** 打印 gitlet 中全部 commit 历史 */
    public void printGlobalLog() {
        String globalCommitLog = Commit.getGlobalCommitLog();
        System.out.println(globalCommitLog);
    }

    /** 查找具有特定提交消息的 commit，并打印其提交 ID */
    public void printFoundIDs(String message) {
        // 返回一个包含所有匹配 message 的 ID 列表
        List<String> foundCommitIDs = Commit.findCommitLogWithMessage(message);
        // 若没有匹配 message 的提交， 则打印错误消息并退出
        if (foundCommitIDs.isEmpty()) {
            System.out.println("Found no commit with that messages!");
            System.exit(0);
        }
        // 逐行打印匹配项的提交 ID
        for (String commitId : foundCommitIDs) {
            System.out.println(commitId);
        }
    }

    /** 打印 Gitlet 版本控制系统的当前状态 */
    public void printStatus() {
        // 加载当前暂存区的信息
        StagingArea stagingArea = new StagingArea();

        // 打印分支信息
        System.out.println("=== Branches ===");
        // 加载 BRANCH_DIR 目录下的所有分支名并打印，当前激活的分支前额外添加星号
        for (String branchName : Utils.plainFilenamesIn(BRANCH_DIR)) {
            if (branchName.equals(activatedBranchName)) {
                System.out.print("*");
            }
            System.out.println(branchName);
        }
        System.out.println();

        // 打印所有已暂存的文件
        System.out.println("=== Staged Files ===");
        printStagedFilesWithOrder(stagingArea);
        System.out.println();

        // 打印所有标记为删除的文件
        System.out.println("=== Removed Files ===");
        printRemovedFilesWithOrder(stagingArea);
        System.out.println();

        // 打印所有被修改但未被暂存的文件
        System.out.println("=== Modifications Not Staged For Commit ===");
        printModifiedNotStagedFilesWithOrder(stagingArea);
        System.out.println();

        // 打印所有未被 gitlet 追踪的文件
        System.out.println("=== Untracked Files ===");
        printUntrackedFilesWithOrder(stagingArea);
        System.out.println();
    }

    /**  辅助打印方法一 ：打印所有已暂存的文件名，并按字母排序 */
    private void printStagedFilesWithOrder(StagingArea stagingArea) {
        // 筛选出符合要求的文件名（key），并将其转化为数组
        String[] stagedFiles = stagingArea.getStagedForAddition().keySet().toArray(new String([0]));
        // 对文件名按字母顺序投递
        Arrays.sort(stagedFiles);
        // 遍历打印
        for (String stagedFile : stagedFiles) {
            System.out.println(stagedFile);
        }
    }

    /**  辅助打印方法二 ：打印所有标记为删除的文件名，并按字母排序 */
    private void printRemovedFilesWithOrder(StagingArea stagingArea) {
        // 原理同打印方法一
        String[] removeFiles = stagingArea.getStagedForRemoval().toArray(new String[0]);
        Arrays.sort(removeFiles);
        for (String removeFile : removeFiles) {
            System.out.println(removeFile);
        }
    }

    /**  辅助打印方法三 ：打印修改但未暂存的文件（附带打印状态），并按字母排序 */
    private void printModifiedNotStagedFilesWithOrder(StagingArea stagingArea) {
        // 定义空列表，储存最终要打印的文件列表
        List<String> modifiedNotStagedFilesWithDescription = new ArrayList<>();
        // 获取当前目录下的所有文件
        List<String> allFiles = Utils.plainFilenamesIn(CWD);
        // 获取当前 commit 追踪的文件
        Commit currentCommit = Commit.fromFile(head);

        // *遍历所有文件
        for (String fileName : allFiles) {
            File file = new File(fileName);
            // 计算文件的 blobID 用以检查文件是否被修改
            Blob currentblob = new Blob(Utils.readContentsAsString(file));
            // 修改情况一：文件被当前 commit 追踪，但内容发生变化且未被添加到暂存区
            if (currentCommit.mapFileNameToBlob.containsKey(fileName)
                && !currentCommit.mapFileNameToBlob.get(fileName).equals(currentblob.getBlobID())
                && !stagingArea.getStagedForAddition().containsKey(fileName)) {
                modifiedNotStagedFilesWithDescription.add(fileName + "(modified)");
            }
            // 修改情况二：文件已被添加到暂存区，但内容发生了修改
            else if (stagingArea.getStagedForAddition().containsKey(fileName)
                && !currentblob.getBlobID().equals(stagingArea.getStagedForAddition().get(fileName))) {
                modifiedNotStagedFilesWithDescription.add((fileName + "(modified)"));
            }
        }
        // 删除情况一：文件已被添加到暂存区，但随后被删除（文件不在当前目录下）
        for (String fileName : stagingArea.getStagedForAddition().keySet()) {
            if (!allFiles.contains(fileName)) {
                modifiedNotStagedFilesWithDescription.add((fileName + "(deleted)"))
            }
        }
        // 删除情况二：文件已被 commit 追踪，但未被标记删除且已被删除（文件不在当前目录下）
        for (String fileName : currentCommit.mapFileNameToBlob.keySet()) {
            if (!stagingArea.getStagedForRemoval().contains(fileName)
                && !allFiles.contains(fileName)) {
                modifiedNotStagedFilesWithDescription.add((fileName + "(deleted)"))
            }
        }
        // 输出结果
        String[] modifiedNotStagedFilesWithDescriptionArray = modifiedNotStagedFilesWithDescription.toArray(new String[]);
        Arrays.sort(modifiedNotStagedFilesWithDescriptionArray);
        for (String modifiedFile : modifiedNotStagedFilesWithDescriptionArray) {
            System.out.println(modifiedFile);
        }
    }

    /**  辅助打印方法四 ：打印未跟踪文件（存在于当前目录下，但 gitlet 中没有），并按字母排序 */
    private void printUntrackedFilesWithOrder(StagingArea stagingArea) {
        // 同辅助方法三
        List<String> untrackedFiles = new ArrayList<>();
        List<String> allFiles = Utils.plainFilenamesIn(CWD);
        Commit currentCommit = Commit.fromFile(head);
        // 情况一：未被 commit 追踪也未被暂存的文件
        for (String fileName : allFiles) {
            if (!currentCommit.mapFileNameToBlob.containKey(fileName)
                && !stagingArea.getStagedForAddition().containsKey(fileName)) {
                untrackedFiles.add(fileName);
            }
            // 情况二：曾被暂存删除但又出现在目录中
            else if (stagingArea.getStagedForRemoval().contain(fileName)) {
                untrackedFiles.add(fileName);
            }
        }
        // 输出结果
        String[] untrackedFilesArray = untrackedFiles.toArray(new String[0]);
        Arrays.sort(untrackedFilesArray);
        for (String modifiedFile : untrackedFilesArray) {
            System.out.println(modifiedFile);
        }
    }


    /** 恢复文件或切换分支  */
    public void checkoutWithFileName(String fileName) {
        // 默认使用 head 指向的 commit 来恢复文件（恢复到最新版本）
        checkoutWithCommitIDAndFileName(head, fileName);
    }

    /** 恢复指定 commit 中的文件（恢复到某个历史版本）  */
    public void checkoutWithCommitIDAndFileName(String commitID, String fileName) {
        // 读取 commitID 对应的 commit 对象
        Commit commitToCheckout = Commit.fromFile(commitID);
        File fileToCheckout = new File(fileName);
        // 若 commitID 不存在，则打印错误消息并退出
        if (commitToCheckout == null) {
            System.out.println("No commit with that id exists!");
            System.exit(0);
        }
        // 若 commitID 对应的文件中没有 fileName，则打印错误消息并退出
        if (!commitToCheckout.mapFileNameToBlob.containsKey(fileName)) {
            System.out.println("File does not exists in that commit!");
            System.exit(0);
        }
        // 执行恢复
        // *获取该文件在 commitID 版本中的 blobID（文件内容的哈希值）
        String blobIDOfFilePrevVersion = commitToCheckout.mapFileNameToBlob.get(fileName);
        // 获取对应版本文件内容（逆处理 blobID）
        String fileContentPrevVersion = Blob.contentFromFile(blobIDOfFilePrevVersion);
        // 将内容写入，覆盖当前文件
        Utils.writeContents(fileToCheckout, fileContentPrevVersion);
    }

    /** 切换分支  */
    public void checkoutWithBranchName(String branchName) {
        // 目标分支的名字不存在于 BRANCH_DIR 中，则报错并退出
        if (!Utils.plainFilenamesIn(BRANCH_DIR).contains(branchName)) {
            System.out.println("No such branch exist!");
            System.exit(0);
        }
        // 目标分支为当前分支，则报错并退出
        if (branchName.equals(activatedBranchName)) {
            System.out.println("No need to checkout the current branch!");
            System.exit(0);
        }

        List<String> allFiles = Utils.plainFilenamesIn(CWD);
        // 读取 branchName 分支的 Branch 对象
        Branch branchToCheckout = Branch.fromFile(branchName);
        // 获取该 Branch 对象的 commitID
        String headCommitIDOfBranch = branchToCheckout.getCommitID();
        // 加载该 commit
        Commit headCommitOfBranch = Commit.fromFile(headCommitIDOfBranch);
        // 获取当前 commit
        Commit currentCommit = Commit.fromFile(head);

        // 若当前分支中未跟踪的文件在目标分支的 HEAD commit 中存在，则退出并报错
        // 确保用户先手动删除或提交文件，防止数据丢失
        for (String fileName : allFiles) {
            if (headCommitOfBranch.mapFileNameToBlob.containsKey(fileName)
                && !currentCommit.mapFileNameToBlob.containsKey(fileName)) {
                System.out.println("These is untracked file in the way;"
                    + " delete it, or add and commit it first!");
                System.exit(0);
            }
        }

        // *恢复目标分支的 HEAD commit 文件
        for (String commitFileName : headCommitOfBranch.mapFileNameToBlob.keySet()) {
            // 获取 commitID 下所有有文件的 blobID
            String blobIDPrevVersion = headCommitIDOfBranch.mapFileNameToBlob.get(commitFileName);
            // 获取对应版本文件内容（逆处理 blobID）
            String fileContentPrevVersion = Blob.contentFromFile(blobIDPrevVersion);
            // 创建并将内容写入当前工作目录
            File fileToBePutInCWD = new File(commitFileName);
            Utils.writeContents(fileToBePutInCWD, fileContentPrevVersion);
        }

        // 删除当前分支有单目标分支没有的文件
        for (String fileInCWD : allFiles) {
            if (!headCommitOfBranch.mapFileNameToBlob.comtainsKey(fileInCWD)
                && currentCommit.mapFileNameToBlob.containsKey(fileInCWD)) {
                boolean isFileInCWDDeled = Utils.restrictedDelete(fileInCWD);
            }
        }

        // 切换分支
        // 加载当前暂存区的信息
        StagingArea stagingArea = new StagingArea();
        // 切换分支
        activatedBranchName = branchName;
        // 更新 head
        head = headCommitIDOfBranch;
        // 清空暂存区并保存
        stagingArea.clear();
        stagingArea.saveStagingArea();
        saveRepository();
    }

    /** 创建新分支，使其指向当前 commit  */
    public void createBranch(String branchName) {
        // 若分支已存在，则打印错误消息并退出
        if (Utils.plainFilenamesIn(BRANCH_DIR).contains(branchName)) {
            System.out.println("A branch with that name already exists!");
            System.exit(0);
        }
        // 创建前 commit 的新分支
        Branch newBranch = new Branch(branchName, head);
        // 将分支信息 commitID 存入 branchName 文件
        newBranch.saveBranch();
    }

    /** 删除指定分支（不包括分支上的 commit）  */
    public void removeBranch(String branchName) {
        // 若分支不存在，则打印错误消息并退出
        if (!Utils.plainFilenamesIn(BRANCH_DIR).contains(branchName)) {
            System.out.println("A branch with that name does not exist!");
            System.exit(0);
        }
        // 若分支为当前分支，则打印错误消息并退出
        if (branchName.equals(activatedBranchName)) {
            System.out.println("Cannot remove the current branch!");
            System.exit(0);
        }
        Branch.removeBranch(branchName);
    }

    /** 回滚到之前的某个 commit 状态  */
    public void reset(String commitID) {
        // 若 commitID 不在 COMMITS_DIR 目录中，则报错并退出
        if (!Utils.plainFilenamesIn(COMMITS_DIR).contains(commitID)) {
            System.out.println("No commit with that id exists!");
            System.exit(0);
        }

        List<String> allFiles = Utils.plainFilenamesIn(CWD); // 获取当前文件夹中所有文件的文件名
        Commit commitToReset = commit.fromFile(commitID); // 获取 commitID 对应的目标 commit
        // 获取目标 commit对应的完整 commit ID（commitID是缩写）
        String standardCommitID = commitToReset.getCommitID();
        Commit currentCommit = Commit.fromFile(head); // 获取当前 commit

        // 若存在未被当前 commit 追踪，但会被目标 commit 覆盖的文件，则返回错误信息并退出
        // 防止文件丢失
        for (String fileName: allFiles) {
            if (commitToReset.mapFileNameToBlob.containKey(fileName)
                && !currentCommit.mapFileNameToBlob.containKey(fileName)) {
                System.out.println("There is an untracked file in the way, " +
                        "delete it, or add and commit it first!");
                System.exit(0);
            }
        }
        // 恢复指定 commit 追踪的文件
        for (String fileNameInCommitToReset : commitToReset.mapFileNameToBlob.keySet()) {
            checkoutWithCommitIDAndFileName(commitID, fileNameInCommitToReset);
        }
        // 删除当前 commit 追踪但目标 commit 不追踪的文件
        for (String fileName : allFiles) {
            if (commitToReset.mapFileNameToBlob.containKey(fileName)
                    && !commitToReset.mapFileNameToBlob.containKey(fileName)) {
                boolean isFileInCWDDeleted = Utils.restrictedDelete(fileName);
            }
        }

        head = standardCommitID; // 将 head 指向新的 commit
        Branch branch = Branch.fromFile(activatedBranchName); // 读取新的分支对象
        branch.update(standardCommitID); // 让当前分支指向新的 commit
        branch.saveBranch(); // 保存新的分支信息
        // 清空暂存区并保存
        StagingArea stagingArea = new StagingArea();
        stagingArea.clear();
        stagingArea.saveStagingArea();
        saveRepository();
    }

    /** 合并指定分支到当前分支  */
    public void merge(String branchNameToMerge) {
        // 合法性检查
        validateMerge(branchNameToMerge);
        // 获取最新的 commit
        Branch branchToMerge = Branch.fromFile(branchNameToMerge);
        String otherBranchCommitID = branchToMerge.getCommitID();
        Commit otherBranchCommit = Commit.fromFile(otherBranchCommitID);
        // 获取当前 commit
        Commit currentHeadCommit = Commit.fromFile(head);
        // 找到最近的共同祖先
        String splitPointCommitID = getSplitPointCommitID(head, otherBranchCommitID, branchToMerge);
        Commit splitPointCommit = Commit.fromFile(splitPointCommitID);

        // 创建 set, 储存所有受影响的文件名
        Set<String> filesToConsider = new HashSet<>();
        filesToConsider.addAll(currentHeadCommit.mapFileNameToBlob.keySet()); // 当前分支
        filesToConsider.addAll(otherBranchCommit.mapFileNameToBlob.keySet()); // 目标分支
        filesToConsider.addAll(splitPointCommit.mapFileNameToBlob.keySet()); // 最近的共同祖先分支

        // 将 set 中的文件逐个合并，检查是否存在冲突
        boolean isConfilcted = false;
        for (String file : filesToConsider) {
            isConfilcted = mergeFile(file, currentHeadCommit, otherBranchCommit, splitPointCommit);
        }

        // 合并成功后，创建合并 commit
        String commitWithMergeMessage = "Merged" + branchToMerge + "into" + activatedBranchName + "!";
        commitWithMerge(commitWithMergeMessage, otherBranchCommitID);

        // 若发生冲突，则打印提示用户
        if (isConfilcted) {
            System.out.println("Encountered a merge conflict!");
        }
    }

    /** 辅助方法：有效性检查 */
    private void validateMerge(String branchNameToMerge) {
        // 加载暂存区
        StagingArea stagingArea = new StagingArea();

        // 情况一：暂存区非空，则打印错误并退出
        if (!stagingArea.getStagedForAddition().isEmpty()
            && stagingArea.getStagedForRemoval().isEmpty()) {
            System.out.println("You have uncommitted changes!");
            System.exit(0);
        }
        // 情况二：如果 branchNameToMerge 不存在，则打印错误并退出
        if (!Utils.plainFilenamesIn(BRANCH_DIR).contains(branchNameToMerge)) {
            System.out.println("A branch with that name does not exist!");
            System.exit(0);
        }
        // 情况三：merge 对象为自身，则打印错误并退出
        if (branchNameToMerge.equals(activatedBranchName)) {
            System.out.println("Cannot merge a branch with itself!");
            System.exit(0);
        }

        // 情况四：若存在未被当前分支追踪但被目标分支追踪的文件，则打印错误并退出（防止误覆盖）
        // 获取目标分支和当前分支的 commit 信息
        Branch branchToMerge = Branch.fromFile(branchNameToMerge);
        String otherBranchCommitID = branchToMerge.getCommitID();
        Commit otherBranchCommit = Commit.fromFile(otherBranchCommitID);
        Commit currentHeadCommit = Commit.fromFile(head);
        // 检查是否存在该情况
        List<String> allFiles = Utils.plainFilenamesIn(CWD);
        for (String fileName : allFiles) {
            if (!currentHeadCommit.mapFileNameToBlob.containsKey(fileName)
                && otherBranchCommit.mapFileNameToBlob.comtainsKey(fileName)) {
                System.out.println("There is an untracked file in the way;" +
                        "delete it, or add and commit it first!");
                System.exit(0);
            }
        }
    }

    /** 辅助方法：获取当前分支和目标分支最近的公共祖先 */
    private String getSplitPointCommitID(String currentBranchCommitID,
                                         String otherBranchCommitID, String otherBranchName) {
        String splitPointCommitID = "";
        // 创建当前分支和目标分支的图
        // 其中 key 为 commitID， value 为 commit 深度
        Map<String, Integer> currentCommitsGraph = buildCommitsGraph(currentBranchCommitID);
        Map<String, Integer> otherCommitsGraph = buildCommitsGraph(otherBranchCommitID);

        // 寻找同时在当前分支和目标分支存在，并且深度最小的 commit
        int minDepth = Integer.MAX_VALUE;
        for (String currentCommitID : currentCommitsGraph.keySet()) {
            int depth = currentCommitsGraph.get(currentCommitID);
            if (otherCommitsGraph.containsKey(currentCommitID) && depth < minDepth) {
                splitPointCommitID = currentCommitID;
                minDepth = depth;
            }
        }

        // *特殊情况一：如果找到公共祖先就是目标分支，说明目标分支是当前分支的祖先，则不需要合并
        if (splitPointCommitID.equals(otherCommitsGraph)) {
            System.out.println("Given branch is an ancestor of the current branch!");
            System.exit(0);
        }
        // 特殊情况二：如果找到公共祖先就是当前分支，说明当前分支是目标分支的祖先，则只需要快进到最新的目标分支
        if (splitPointCommitID.equals(currentBranchCommitID)) {
            checkoutWithBranchName(otherBranchName);
            System.out.println("Current branch fast_forwarded!");
            System.exit(0);
        }

        return splitPointCommitID;
    }

    /** 辅助方法：构建 commit 深度图 */
    private Map<String, Integer> buildCommitsGraph(String commitID) {
        Map<String, Integer> commitsGraph = new HashMap<>();
        buildCommitsGraphHelper(commitID, 0, commitsGraph);
        return commitsGraph;
    }
    // 递归遍历，将每个 commit 及其深度存入 commitsGraph
    private void buildCommitsGraphHelper(String commitID, int depth, Map<String, Integer> commitsGraph) {
        if (commitID == null) {
            return;
        }
        commitsGraph.put(commitID, depth);
        Commit commit = Commit.fromFile(commitID);
        // commit 至多有两个父 commit
        buildCommitsGraphHelper(commit.getFirstParent(), depth + 1, commitsGraph);
        buildCommitsGraphHelper(commit.getSecondParent(), depth + 1, commitsGraph);
    }

    /** 辅助方法：合并单个文件 */
    private boolean mergeFile(String file, Commit currenrtHeadCommit,
                              Commit otherBranchCommit, Commit splitPointCommit) {
        boolean isConflicted = false;
        // 确定文件是否在当前分支、目标分支和共同祖先的提交中存在
        boolean isFileInCurrent = currenrtHeadCommit.mapFileNameToBlob.containsKey(file);
        boolean isFileInOther = otherBranchCommit.mapFileNameToBlob.containsKey(file);
        boolean isFileInSplit = splitPointCommit.mapFileNameToBlob.containsKey(file);
        // 获取各个提交中的文件内容
        String fileContentInCurrent = currenrtHeadCommit.getFileContentInCommit(file);
        String fileContentInOrder = otherBranchCommit.getFileContentInContent(file);
        String fileContentInSplit = splitPointCommit.getFileContentInCommit(file);

        // 分情况处理，共计十一种情况
        // 当前分支、目标分支和共同祖先中均存在该文件
        if (isFileInCurrent && isFileInOther && isFileInSplit) {
            // 当前分支的文件是否被修改
            boolean isFileModifiedInCurrent = !fileContentInCurrent.equals(fileContentInSplit);
            // 目标分支的文件是否被修改
            boolean isFileModifiedInOther = !fileContentInOrder.equals(fileContentInSplit);
            // 情况一：当前分支的文件未被修改，目标分支的文件被修改，则用目标分支的文件覆盖，并添加到暂存区
            if (isFileModifiedInOther && !isFileModifiedInCurrent) {
                StagingArea stagingArea = new StagingArea();
                checkoutWithCommitIDAndFileName(otherBranchCommit.getCommitID(), file);
                stagingArea.addFile(file, head);
                stagingArea.saveStagingArea();
            }
            // 情况二：当前分支的文件被修改，目标分支的文件未被修改，无需处理
            else if (!isFileModifiedInOther && isFileModifiedInCurrent) {
                isConflicted = false;
            }
            // 当前分支的文件和目标分支的文件都被修改
            else if (isFileModifiedInOther && isFileModifiedInCurrent) {
                // 情况三：当前分支的文件和目标分支的文件被以同样的方式修改，无需处理
                if (fileContentInCurrent.equals(fileContentInOrder)) {
                    isConflicted = false;
                }
                // 情况四：当前分支的文件和目标分支的文件均被修改，且修改方式不一致，处理并标记冲突
                else {
                    mergeFileWithConflicts(file, fileContentInCurrent, fileContentInOrder);
                    isConflicted = true;
                }
            }
        }
        // 情况五：当前分支和目标分支中存在该文件，而共同祖先不存在该文件
        // 若当前分支和目标分支中的文件不同，则无法自动合并，需要处理并标记冲突
        else if (!isFileInSplit && isFileInCurrent && isFileInOther
                && !fileContentInCurrent.equals(fileContentInOrder)) {
            mergeFileWithConflicts(file, fileContentInCurrent, fileContentInOrder);
            isConflicted = true;
        }
        // 情况六：文件只在当前分支中存在，无需处理
        else if (!isFileInSplit && !isFileInOther && isFileInCurrent) {
            isConflicted = false;
        }
        // 情况七：文件只在目标分支中存在，获取该文件并保存
        else if (!isFileInSplit && !isFileInCurrent && isFileInOther) {
            StagingArea stagingArea = new StagingArea();
            checkoutWithCommitIDAndFileName(otherBranchCommit.getCommitID(), file);
            stagingArea.addFile(file, head);
            stagingArea.saveStagingArea();
        }
        // 当前分支和共同祖先中存在该文件，而目标分支不存在该文件
        else if (!isFileInOther && isFileInSplit && isFileInCurrent) {
            // 情况八：若该文件未在当前分支中被修改过，则删除该文件
            if (fileContentInCurrent.equals(fileContentInSplit)) {
                remove(file);
            }
            // 情况九：若该文件在当前分支中被修改过，则需要处理并标记冲突
            else {
                mergeFileWithConflicts(file, fileContentInCurrent, fileContentInOrder);
                isConflicted = true;
            }
        }
        // 目标分支和共同祖先中存在该文件，而当前分支不存在该文件
        else if (!isFileInCurrent && isFileInSplit && isFileInOther) {
            // 情况十：目标分支中的文件未被修改过，则无需处理
            if (fileContentInOrder.equals(fileContentInSplit)) {
                isConflicted = false;
            }
            // 情况十一：目标分支中的文件被修改过，则需要处理并标记冲突
            else {
                mergeFileWithConflicts(file, fileContentInCurrent, fileContentInOrder);
                isConflicted = true;
            }
        }
        return isConflicted;
    }

    /** 辅助方法：处理合并冲突 */
    /*
    冲突输出格式：
    <<<<<<< HEAD
    (当前分支的文件内容)
    =======
    (要合并分支的文件内容)
    >>>>>>>
     */
    private void mergeFileWithConflicts(String file, String fileContentInCurrnet,
                                        String fileContentInOther) {
        String fileContentWithConflicts = "<<<<<<< HEAD" + "\n";
        // 若当前分支存在文件，则加入输出内容，否则留空
        if (fileContentInCurrnet != null) {
            fileContentWithConflicts += fileContentInCurrnet;
        }
        fileContentWithConflicts += "======" + "\n";
        // 若目标分支存在文件，则加入输出内容，否则留空
        if (fileContentInOther != null) {
            fileContentWithConflicts += fileContentInOther;
        }
        fileContentWithConflicts += ">>>>>>>" + "\n";

        // 创建文件并将冲突信息写入
        File fileWithConflicts = new File(file);
        Utils.writeContents(fileWithConflicts, fileContentWithConflicts);
        // 将冲突信息作为 Blob 存储
        Blob blob = new Blob(fileContentWithConflicts);
        blob.saveBlob();
        // 将冲突文件添加到暂存区
        StagingArea stagingArea = new StagingArea();
        stagingArea.addFile(file, head);
        stagingArea.saveStagingArea();
    }

    // 打印当前分支信息，包括 head 和当前激活分支的名称
    @Override
    public void dump() {
        System.out.printf("head: %s%nbranch: %s%n", head, activatedBranchName);
    }
}























