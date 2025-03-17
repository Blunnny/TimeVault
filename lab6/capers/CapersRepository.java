package capers;

import java.io.File;
import static capers.Utils.*;

/** A repository for Capers 
 * @author TODO
 * The structure of a Capers Repository is as follows:
 *
 * .capers/ -- top level folder for all persistent data in your lab12 folder
 *    - dogs/ -- folder containing all of the persistent data for dogs
 *    - story -- file containing the current story
 *
 * TODO: change the above structure if you do something different.
 */
public class CapersRepository {
    /** Current Working Directory. */
    static final File CWD = new File(System.getProperty("user.dir"));

    /** Main metadata folder. */
    /** 将当前工作目录 CWD 与 .capers 拼接， 形成 .capers目录路径 CAPERS_FOLDER */
    static final File CAPERS_FOLDER = Utils.join(CWD, ".capers");

    /**
     * 确保 .capers 和 .capers/dogs 目录存在
     * 如果目录不存在，则创建它们
     * .capers/ -- top level folder for all persistent data in your lab12 folder
     *    - dogs/ -- folder containing all of the persistent data for dogs
     *    - story -- file containing the current story
     */
    public static void setupPersistence() {
        if (!CAPERS_FOLDER.exists()) {
            CAPERS_FOLDER.mkdir();
        }
        File dogFolder = Utils.join(CAPERS_FOLDER, "dogs");
        if (!dogFolder.exists()) {
            dogFolder.mkdir();
        }

    }

    /**
     * Appends the first non-command argument in args
     * to a file called `story` in the .capers directory.
     * @param text String of the text to be appended to the story
     */
    public static void writeStory(String text) {
        // 定义文件路径
        File storyFile = join(CAPERS_FOLDER, "story");

        // 如果文件不存在，则将text作为文件内容，否则将新内容追加在原内容之后
        String newStoryFile;
        if (!storyFile.exists()) {
            newStoryFile = text;
        } else {
            String originStory = readContentsAsString(storyFile);
            newStoryFile = originStory + "\n" + text;
        }

        // 写入新内容
        writeContents(storyFile, newStoryFile);

        // 打印文件内容
        System.out.println(newStoryFile);
    }

    /**
     * Creates and persistently saves a dog using the first
     * three non-command arguments of args (name, breed, age).
     * Also prints out the dog's information using toString().
     */
    public static void makeDog(String name, String breed, int age) {
        // 创建一个新的 Dog 对象，传入名称、品种和年龄
        Dog dog = new Dog(name, breed, age);
        // 将狗对象保存到 .capers/dogs/ 目录下的文件中
        dog.saveDog();
        System.out.println(dog.toString());
    }

    /**
     * Advances a dog's age persistently and prints out a celebratory message.
     * Also prints out the dog's information using toString().
     * Chooses dog to advance based on the first non-command argument of args.
     * @param name String name of the Dog whose birthday we're celebrating.
     */
    public static void celebrateBirthday(String name) {
        // 根据狗的名字加载狗对象
        Dog dog = Dog.fromFile(name);
        if (dog == null) {
            System.out.println("Error: No such dog found.");
            return;
        }
        dog.haveBirthday();
        dog.saveDog();
    }
}
