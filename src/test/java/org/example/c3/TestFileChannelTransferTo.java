package org.example.c3;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;

public class TestFileChannelTransferTo {
    public static void main(String[] args) {
        try (FileChannel from = new FileInputStream("data.txt").getChannel();
             FileChannel to = new FileOutputStream("to.txt").getChannel()
        ) {
            long size = from.size();
            // left: 剩余待传输的数据量
            // transferTo一次最多传输2g,如果数据量大于2g，则这个循环体就会执行不止一次
            for (long left = size; left > 0; ) {
                System.out.println("position: " + (size - left) + " left: " + left);
                // 效率高，会利用OS的零拷贝进行优化
                left -= from.transferTo(size - left, left, to);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
