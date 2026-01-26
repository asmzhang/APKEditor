/*
 *  Copyright (C) 2022 github.com/REAndroid
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
  */
package com.reandroid.apkeditor.protect;

import com.reandroid.apk.ApkModule;
import com.reandroid.apk.ResFile;
import com.reandroid.apk.UncompressedFiles;
import com.reandroid.apkeditor.utils.CyclicIterator;
import com.reandroid.archive.Archive;

/**
 * 文件名称混淆器类
 * 负责混淆APK中的文件名称
 */
public class FileNameConfuser extends Confuser {

    /** 文件名称迭代器 */
    private final CyclicIterator<String> namesIterator;

    /**
     * 构造函数
     * @param protector 保护器对象
     */
    public FileNameConfuser(Protector protector) {
        super(protector, "FileNameConfuser: ");
        // 从选项中加载文件名称字典
        this.namesIterator = new CyclicIterator<>(
                protector.getOptions().loadFileNameDictionary());
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
        for(ResFile resFile : getApkModule().listResFiles()){
            // 获取输入源方法
            int method = resFile.getInputSource().getMethod();
            
            // 生成新的路径
            String pathNew = generateNewPath(resFile);
            if(pathNew != null) {
                String path = resFile.getFilePath();
                // 如果是存储方法，替换路径
                if(method == Archive.STORED) {
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
        
        // 循环尝试生成新路径，直到找到不冲突的路径
        while (iterator.getCycleCount() == 0) {
            String newPath = replaceSimpleName(path, iterator.next());
            if (!containsFilePath(newPath)) {
                return newPath;
            }
        }
        return null;
    }

    /**
     * 替换简单名称
     * @param path 原始路径
     * @param symbol 新的名称
     * @return 替换后的路径
     */
    private static String replaceSimpleName(String path, String symbol) {
        // 查找最后一个斜杠位置
        int i = path.lastIndexOf('/');
        String dirName;
        String simpleName;
        
        if (i < 0) {
            dirName = "";
            simpleName = path;
        } else {
            i = i + 1;
            dirName = path.substring(0, i);
            simpleName = path.substring(i);
        }
        
        // 查找最后一个点位置
        i = simpleName.lastIndexOf('.');
        String ext;
        
        if (i < 0) {
            ext = ".";
        } else {
            // 特殊处理.9.png文件
            if (simpleName.endsWith(".9.png")) {
                ext = ".9.png";
            } else {
                ext = simpleName.substring(i);
            }
        }
        
        return dirName + symbol + ext;
    }
}
