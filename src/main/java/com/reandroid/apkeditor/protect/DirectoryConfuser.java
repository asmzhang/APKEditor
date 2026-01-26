///*
// *  Copyright (C) 2022 github.com/REAndroid
// *
// *  Licensed under the Apache License, Version 2.0 (the "License");
// *  you may not use this file except in compliance with the License.
// *  You may obtain a copy of the License at
// *
// *      http://www.apache.org/licenses/LICENSE-2.0
//  *
//  * Unless required by applicable law or agreed to in writing, software
//  * distributed under the License is distributed on an "AS IS" BASIS,
//  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//  * See the License for the specific language governing permissions and
//  * limitations under the License.
//  */
//package com.reandroid.apkeditor.protect;
//
//import com.reandroid.apk.ApkModule;
//import com.reandroid.apk.ResFile;
//import com.reandroid.apk.UncompressedFiles;
//import com.reandroid.apkeditor.utils.CyclicIterator;
//import com.reandroid.archive.Archive;
//
///**
// * 目录名称混淆器类
// * 负责混淆APK中的目录名称
// */
//public class DirectoryConfuser extends Confuser {
//
//    /** 目录名称迭代器 */
//    private final CyclicIterator<String> namesIterator;
//
//    /**
//     * 构造函数
//     * @param protector 保护器对象
//     */
//    public DirectoryConfuser(Protector protector) {
//        super(protector, "DirectoryConfuser: ");
//        // 从选项中加载目录名称字典
//        this.namesIterator = new CyclicIterator<>(
//                protector.getOptions().loadDirectoryNameDictionary());
//    }
//
//    /**
//     * 执行混淆操作
//     */
//    @Override
//    public void confuse() {
//        logMessage("Confusing ...");
//
//        ApkModule apkModule = getApkModule();
//        UncompressedFiles uf = apkModule.getUncompressedFiles();
//
//        // 遍历所有资源文件
//        for(ResFile resFile : getApkModule().listResFiles()){
//            // 获取输入源方法
//            int method = resFile.getInputSource().getMethod();
//
//            // 生成新的路径
//            String pathNew = generateNewPath(resFile);
//            if(pathNew != null) {
//                String path = resFile.getFilePath();
//                // 如果是存储方法，替换路径
//                if(method == Archive.STORED) {
//                    uf.replacePath(path, pathNew);
//                }
//                // 设置新的文件路径
//                resFile.setFilePath(pathNew);
//                onPathChanged(path, pathNew);
//            }
//        }
//    }
//
//    /**
//     * 生成新的文件路径
//     * @param resFile 资源文件
//     * @return 新的文件路径，如果不需要修改则返回null
//     */
//    private String generateNewPath(ResFile resFile) {
//        // 检查是否需要保留该类型
//        if (isKeepType(resFile.pickOne().getTypeName())) {
//            return null;
//        }
//        return generateNewPath(resFile.getFilePath());
//    }
//
//    /**
//     * 生成新的路径
//     * @param path 原始路径
//     * @return 新的路径
//     */
//    private String generateNewPath(String path) {
//        CyclicIterator<String> iterator = this.namesIterator;
//        iterator.resetCycleCount();
//
//        // 循环尝试生成新路径，直到找到不冲突的路径
//        while (iterator.getCycleCount() == 0) {
//            String newPath = replaceDirectory(path, iterator.next());
//            if (!containsFilePath(newPath)) {
//                return newPath;
//            }
//        }
//        return null;
//    }
//
//    /**
//     * 替换目录名称
//     * @param path 原始路径
//     * @param dirName 新的目录名称
//     * @return 替换后的路径
//     */
//    private static String replaceDirectory(String path, String dirName) {
//        // 查找最后一个斜杠位置
//        int i = path.lastIndexOf('/');
//        if (i < 0) {
//            i = 0;
//        } else {
//            i = i + 1;
//            if (i == path.length()) {
//                i = i - 1;
//            }
//        }
//
//        // 获取简单名称
//        String simpleName = path.substring(i);
//
//        // 如果目录名称不为空，添加斜杠
//        if (dirName.length() != 0) {
//            dirName = dirName + "/";
//        }
//        return dirName + simpleName;
//    }
//}










package com.reandroid.apkeditor.protect;

import com.reandroid.apk.ApkModule;
import com.reandroid.apk.ResFile;
import com.reandroid.apk.UncompressedFiles;
import com.reandroid.apkeditor.utils.CyclicIterator;
import com.reandroid.archive.Archive;

/**
 * 目录名称混淆器类
 * 负责混淆APK中的目录名称
 */
public class DirectoryConfuser extends Confuser {

    /** 目录名称迭代器 */
    private final CyclicIterator<String> namesIterator;

    /**
     * 构造函数
     * @param protector 保护器对象
     */
    public DirectoryConfuser(Protector protector) {
        super(protector, "DirectoryConfuser: ");
        // 从选项中加载目录名称字典
        this.namesIterator = new CyclicIterator<>(
                protector.getOptions().loadDirectoryNameDictionary());
    }

    /**
     * 执行混淆操作
     */
    @Override
    public void confuse() {
        logMessage("Confusing ...");

        ApkModule apkModule = getApkModule();
        UncompressedFiles uf = apkModule.getUncompressedFiles();

        // 遍历所有资源文件
        for (ResFile resFile : apkModule.listResFiles()) {
            // 获取输入源方法
            int method = resFile.getInputSource().getMethod();

            // 生成新的路径
            String pathNew = generateNewPath(resFile);
            if (pathNew != null) {
                String path = resFile.getFilePath();
                // 如果是存储方法，替换路径
                if (method == Archive.STORED) {
                    uf.replacePath(path, pathNew);
                }
                // 设置新的文件路径
                resFile.setFilePath(pathNew);
                onPathChanged(path, pathNew);
            }
        }
    }

    /**
     * 生成新的文件路径
     * @param resFile 资源文件
     * @return 新的文件路径，如果不需要修改则返回null
     */
    private String generateNewPath(ResFile resFile) {
        // 检查是否需要保留该类型
        if (isKeepType(resFile.pickOne().getTypeName())) {
            return null;
        }
        return generateNewPath(resFile.getFilePath());
    }

    /**
     * 生成新的路径
     * @param path 原始路径
     * @return 新的路径
     */
    private String generateNewPath(String path) {
        CyclicIterator<String> iterator = this.namesIterator;
        iterator.resetCycleCount();

        // 先尝试一次，避免死循环
        String newPath = replaceDirectory(path, iterator.next());
        if (!containsFilePath(newPath)) {
            return newPath;
        }

        // 循环尝试生成新路径，直到找到不冲突的路径
        while (iterator.getCycleCount() == 0) {
            newPath = replaceDirectory(path, iterator.next());
            if (!containsFilePath(newPath)) {
                return newPath;
            }
        }
        return null;
    }

    /**
     * 替换目录名称（只替换最后一级目录）
     * 例如: res/drawable/icon.png -> res/abc/icon.png
     *
     * @param path 原始路径
     * @param dirName 新的目录名称
     * @return 替换后的路径
     */
    private static String replaceDirectory(String path, String dirName) {
        int i = path.lastIndexOf('/');
        if (i < 0) {
            // 没有目录结构，直接拼接
            return dirName + "/" + path;
        }

        String parent = path.substring(0, i + 1);
        String simpleName = path.substring(i + 1);

        // 如果目录名称为空，不加斜杠
        if (dirName.isEmpty()) {
            return parent + simpleName;
        }
        return parent + dirName + "/" + simpleName;
    }
}
