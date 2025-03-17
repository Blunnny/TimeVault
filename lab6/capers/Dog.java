package capers;

import java.io.File;
import java.io.Serializable;
import static capers.Utils.*;

/** Represents a dog that can be serialized.
 * @author TODO
*/
public class Dog implements Serializable { // TODO

    /** Folder that dogs live in. */
    /** 将 .capers 和 dogs 拼接，形成 .capers/dogs 的路径 .capers/dogs */
    static final File DOG_FOLDER = Utils.join(CapersRepository.CAPERS_FOLDER, "dogs"); //


    /** Age of dog. */
    private int age;
    /** Breed of dog. */
    private String breed;
    /** Name of dog. */
    private String name;

    /**
     * Creates a dog object with the specified parameters.
     * @param name Name of dog
     * @param breed Breed of dog
     * @param age Age of dog
     */
    public Dog(String name, String breed, int age) {
        this.age = age;
        this.breed = breed;
        this.name = name;
    }

    /**
     * Reads in and deserializes a dog from a file with name NAME in DOG_FOLDER.
     *
     * @param name Name of dog to load
     * @return Dog read from file
     */
    public static Dog fromFile(String name) {
        // 生成狗文件路径
        File dogFile = join(DOG_FOLDER, name);
        // 如果存在，使用 readObject 反序列化文件内容为 Dog 对象
        if (dogFile.exists()) {
            return readObject(dogFile, Dog.class);
        }
        // 如果不存在，返回 null
        return null;
    }

    /**
     * Increases a dog's age and celebrates!
     */
    public void haveBirthday() {
        age += 1;
        System.out.println(toString());
        System.out.println("Happy birthday! Woof! Woof!");
    }

    /**
     * Saves a dog to a file for future use.
     */
    public void saveDog() {
        // 生成狗文件的路径
        File dogFile = join(DOG_FOLDER, name);
        // 将当前狗对象序列化并写入文件
        writeObject(dogFile, this);
    }

    @Override
    public String toString() {
        return String.format(
            "Woof! My name is %s and I am a %s! I am %d years old! Woof!",
            name, breed, age);
    }

}
