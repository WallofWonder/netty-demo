package org.example.c1;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.concurrent.atomic.AtomicInteger;

public class TestFilesWalkFileTree {
    public static void main(String[] args) throws IOException {
        showJars();
    }

    private static void showJars() throws IOException {
        final AtomicInteger jarCnt = new AtomicInteger();
        Files.walkFileTree(Paths.get("D:\\Java\\jdk1.8.0_333"),new SimpleFileVisitor<Path>(){
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                if (file.toString().endsWith(".jar")) {
                    System.out.println(file);
                    jarCnt.incrementAndGet();
                }
                return super.visitFile(file, attrs);
            }
        });
        System.out.println("jar cnt: " + jarCnt);
    }

    private static void showAll() throws IOException {
        final AtomicInteger dirCnt = new AtomicInteger();
        final AtomicInteger fileCnt = new AtomicInteger();

        Files.walkFileTree(Paths.get("D:\\Java\\jdk1.8.0_333"),new SimpleFileVisitor<Path>(){
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                System.out.println("======>" + dir);
                dirCnt.incrementAndGet();
                return super.preVisitDirectory(dir, attrs);
            }

            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                System.out.println(file);
                fileCnt.incrementAndGet();
                return super.visitFile(file, attrs);
            }
        });

        System.out.println("dir cnt: " + dirCnt);
        System.out.println("file cnt: " + fileCnt);
    }
}
