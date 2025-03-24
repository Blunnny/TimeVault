package gitlet;

import java.io.File;
import java.io.Serial;
import java.io.Serializable;

/** 表示 blob 对象 */
public class Blob implements Serializable, Dumpable {
    @Serial
    private static final long serialVersionUID = -5528835260037188238L;
    /** blob 对象的唯一 SHA1 ID */
    private String blobID;
    /** 存储这个 blob 关联的文件内容 */
    private String fileContent;

    /** 创建 blob 实例 */
    public Blob(String content) {
        this.fileContent = content;
        this.blobID = Utils.sha1(Utils.serialize(content)); // 内容相同的文件共享一个 blob
    }

    /** 访问 blobID */
    public String getBlobID() {
        return blobID;
    }

    /** 访问 fileContent */
    public String getFileContent() {
        return fileContent;
    }

    /** 读取 blob 对应的文件（通过 blob 对象反序列化文件内容） */
    public static Blob fromFile(String requiredBlobID) {
        File blobFile = Utils.join(Repository.BLOB_DIR, requiredBlobID);
        if (!blobFile.exists()) {
            return null;
        }
        return Utils.readObject(blobFile, Blob.class);
    }

    /** 读取 blob 对应的文件内容 */
    public static String contentFromFile(String requiredBlobID) {
        Blob requiredBlob = fromFile(requiredBlobID);
        if (requiredBlob == null) {
            return null;
        }
        return requiredBlob.fileContent;
    }

    /** 保存 blob */
    public void saveBlob() {
        if (!Repository.BLOB_DIR.exists()) {
            Repository.BLOB_DIR.mkdir();
        }
        File saveFile = Utils.join(Repository.BLOB_DIR, blobID);
        Utils.writeObject(saveFile, this);
    }

    /** 打印 blob 对应的 SHA1 值以及存储的文件内容 */
    @Override
    public void dump() {
        System.out.printf("blob ID: %s%nfile content: %s%n", blobID, fileContent);
    }
}