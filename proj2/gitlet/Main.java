package gitlet;

/** 解析命令行输入并调用 Repository 类执行相应的操作 */
public class Main {

    /** Usage: java gitlet.Main ARGS, where ARGS contains
     *  <COMMAND> <OPERAND1> <OPERAND2> ...
     *
     *  Commands contain {init, add, commit, rm, log, global-log,
     *           find, status, checkout, branch, rm-branch, reset, merge} etc.
     *  Details can be obtained from the switch-case in {@code gitlet.Main.main} method
     *  and <a href="https://sp21.datastructur.es/materials/proj/proj2/proj2#the-commands">
     *      proj2#the-commands</a>
     *
     * All persistent data should be stored in a ".gitlet"
     * directory in the current working directory.
     *
     * .gitlet/
     *      - commits/ -- 存储所有 commit 对象，每个 commit 以其 SHA1 哈希值命名
     *          - commit1_SHA1 -- commit1 file (named as its SHA1 ID, containing metadata)
     *          - commit2_SHA1 -- commit2 file (similar to commit1_SHA1)
     *          - ...
     *      - branches/ -- 存储所有分支，每个分支以分支名命名，内容是指向的 commit ID。
     *          - master -- named as "master", the commit SHA1 ID string as its contents
     *          - branch1 -- similar to "master"
     *          - branch2 -- similar to "master"
     *          - ...
     *      - blobs/ -- 存储所有文件快照
     *          - blob1_SHA1 -- named as blob1's SHA1_ID, containing file contents of blob1
     *          - blob2_SHA1 -- similar to blob1_SHA1
     *          - ...
     *      - repository -- 存储当前 HEAD 及 active branch 指针
     *      - stagingArea -- 存储暂存区状态，包括新增和删除的文件
     */
    public static void main(String[] args) {
        // 若没有提供命令行参数，输出提示信息并退出
        if (args.length == 0) { // what if args is empty?
            System.out.println("Please enter a command.");
            System.exit(0);
        }
        // 实例化 repository 对象
        Repository repository = new Repository();
        // 获取用户的第一个命令
        String firstArg = args[0];

        // 使用 switch-case 来解析命令并调用 Repository 类中的相应方法
        switch (firstArg) {
            case "init":
                // 检查参数长度是否正确
                validateNumAndFormatArgs(args, 1);
                // 调用方法
                repository.init();
                break;
            case "add":
                validateNumAndFormatArgs(args, 2);
                repository.add(args[1]);
                break;
            case "commit":
                validateNumAndFormatArgs(args, 2);
                String message = args[1];
                repository.commit(message);
                break;
            case "rm":
                validateNumAndFormatArgs(args, 2);
                String fileNameToBeRemoved = args[1];
                repository.remove(fileNameToBeRemoved);
                break;
            case "log":
                validateNumAndFormatArgs(args, 1);
                repository.printLog();
                break;
            case "global-log":
                validateNumAndFormatArgs(args, 1);
                repository.printGlobalLog();
                break;
            case "find":
                validateNumAndFormatArgs(args, 2);
                String messageToBeFound = args[1];
                repository.printFoundIDs(messageToBeFound);
                break;
            case "status":
                validateNumAndFormatArgs(args, 1);
                repository.printStatus();
                break;
            case "checkout":
                handleCheckoutArg(args, repository);
                break;
            case "branch":
                validateNumAndFormatArgs(args, 2);
                String branchNameToCreate = args[1];
                repository.createBranch(branchNameToCreate);
                break;
            case "rm-branch":
                validateNumAndFormatArgs(args, 2);
                String branchNameToRemove = args[1];
                repository.removeBranch(branchNameToRemove);
                break;
            case "reset":
                validateNumAndFormatArgs(args, 2);
                String commitID = args[1];
                repository.reset(commitID);
                break;
            case "merge":
                validateNumAndFormatArgs(args, 2);
                String branchNameToMerge = args[1];
                repository.merge(branchNameToMerge);
                break;
            default:
                // 若命令无效，则打印错误消息并退出
                System.out.println("No command with that name exists.");
                System.exit(0);
        }
        return;
    }

    /**
     * checkout 有以下三种方法，根据输入自动选择匹配的方法：
     * 1. {@code java gitlet.Main checkout -- [file name]}
     * 2. {@code java gitlet.Main checkout [commit id] -- [file name]}
     * 3. {@code java gitlet.Main checkout [branch name]}
     */
    private static void handleCheckoutArg(String[] args, Repository repository) {
        if (args.length == 3) {
            // java gitlet.Main checkout -- [file name]
            validateNumAndFormatArgs(args, 3);
            String fileName = args[2];
            repository.checkoutWithFileName(fileName);
        } else if (args.length == 4) {
            // java gitlet.Main checkout [commit id] -- [file name]
            validateNumAndFormatArgs(args, 4);
            String commitID = args[1];
            String fileName = args[3];
            repository.checkoutWithCommitIDAndFileName(commitID, fileName);
        } else if (args.length == 2) {
            // java gitlet.Main checkout [branch name]
            validateNumAndFormatArgs(args, 2);
            String branchName = args[1];
            repository.checkoutWithBranchName(branchName);
        }
    }


    /** 辅助方法：验证输入是否合法 */
    public static void validateNumAndFormatArgs(String[] args, int n) {
        // 确保命令参数个数正确
        if (args.length != n) {
            throw new RuntimeException("Incorrect operands.");
        }

        // 针对 checkout 进行格式检查
        String firstArg = args[0];
        if (firstArg.equals("checkout")) {
            if (args.length == 3 && !args[1].equals("--")) {
                // java gitlet.Main checkout -- [file name]
                System.out.println("Incorrect operands.");
                System.exit(0);
            }
            if (args.length == 4 && !args[2].equals("--")) {
                // java gitlet.Main checkout [commit id] -- [file name]
                System.out.println("Incorrect operands.");
                System.exit(0);
            }
        }

        /* 检查是否处于 .gitlet 目录下 */
        if (!args[0].equals("init") && !Repository.GITLET_DIR.exists()) {
            System.out.println("Not in an initialized Gitlet directory.");
            System.exit(0);
        }
    }
}