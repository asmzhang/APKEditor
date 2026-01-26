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
import com.reandroid.apk.DexFileInputSource;
import com.reandroid.archive.ByteInputSource;
import com.reandroid.archive.InputSource;
import com.reandroid.dex.model.DexClass;
import com.reandroid.dex.model.DexFile;
import com.reandroid.utils.collection.ArrayCollection;
import com.reandroid.utils.collection.CollectionUtil;
import com.reandroid.utils.collection.FilterIterator;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;

/**
 * DEX文件混淆器类
 * 负责对APK中的DEX文件进行混淆处理
 */
public class DexConfuser extends Confuser {

    /** DEX混淆任务列表 */
    private final ArrayCollection<DexConfuseTask> mTaskList;

    /**
     * 构造函数（带任务列表）
     * @param protector 保护器对象
     * @param tasks DEX混淆任务数组
     */
    public DexConfuser(Protector protector, DexConfuseTask ... tasks) {
        super(protector, "DexConfuser: ");
        this.mTaskList = new ArrayCollection<>(tasks);
    }
    
    /**
     * 构造函数（使用默认任务）
     * @param protector 保护器对象
     */
    public DexConfuser(Protector protector) {
        this(protector,
                new DexArrayPayloadConfuser().setLogger(protector),
                new DexStringFogger().setLogger(protector)
        );
    }

    /**
     * 执行混淆操作
     */
    @Override
    public void confuse() {
        // 如果未启用，则跳过
        if (!isEnabled()) {
            logMessage("Skip");
            return;
        }
        
        ApkModule apkModule = getApkModule();
        List<DexFileInputSource> dexFiles = getApkModule().listDexFiles();
        
        // 如果没有DEX文件，返回
        if (dexFiles.isEmpty()) {
            logMessage("Dex files not found");
            return;
        }
        
        logMessage("Confusing " + dexFiles.size() + " dex files ...");
        
        // 遍历所有DEX文件
        for (DexFileInputSource inputSource : dexFiles) {
            InputSource modified = confuse(inputSource);
            if (modified != null) {
                // 添加修改后的DEX文件到模块
                apkModule.add(modified);
            }
        }
        
        // 记录任务摘要
        List<DexConfuseTask> taskList = getTasks();
        for (DexConfuseTask task : taskList) {
            task.logSummary();
        }
    }
    
    /**
     * 混淆输入源
     * @param inputSource DEX文件输入源
     * @return 修改后的输入源
     */
    private InputSource confuse(InputSource inputSource) {
        logMessage(inputSource.getAlias());
        DexFile dexFile = load(inputSource);
        InputSource result = null;
        
        // 如果混淆成功，创建新的输入源
        if (confuse(dexFile)) {
            result = new ByteInputSource(save(dexFile), inputSource.getAlias());
            result.copyAttributes(inputSource);
        }
        dexFile.close();
        return result;
    }
    
    /**
     * 保存DEX文件
     * @param dexFile DEX文件对象
     * @return DEX文件字节数组
     */
    private byte[] save(DexFile dexFile) {
        // 刷新DEX文件
        dexFile.refresh();
        // 收缩DEX文件
        dexFile.shrink();
        // 完全刷新DEX文件
        dexFile.refreshFull();
        return dexFile.getBytes();
    }
    
    /**
     * 混淆DEX文件
     * @param dexFile DEX文件对象
     * @return 是否进行了混淆
     */
    private boolean confuse(DexFile dexFile) {
        List<DexConfuseTask> taskList = getTasks();
        boolean result = false;
        
        // 遍历所有DEX类
        Iterator<DexClass> iterator = dexFile.getDexClasses();
        while (iterator.hasNext()) {
            DexClass dexClass = iterator.next();
            
            // 对每个类应用所有混淆任务
            for (DexConfuseTask task : taskList) {
                if (task.apply(dexClass)) {
                    result = true;
                }
            }
        }
        return result;
    }
    
    /**
     * 加载DEX文件
     * @param inputSource 输入源
     * @return DEX文件对象
     */
    private DexFile load(InputSource inputSource) {
        try {
            DexFile dexFile = DexFile.read(inputSource.openStream());
            dexFile.setSimpleName(inputSource.getSimpleName());
            return dexFile;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    
    /**
     * 获取混淆级别
     * @return 混淆级别
     */
    private int getLevel() {
        return getProtector().getOptions().dexLevel;
    }

    /**
     * 获取所有任务
     * @return 任务迭代器
     */
    public Iterator<DexConfuseTask> getAllTasks() {
        return mTaskList.iterator();
    }
    
    /**
     * 检查是否启用
     * @return 如果启用则返回true
     */
    public boolean isEnabled() {
        if (getLevel() <= 0) {
            return false;
        }
        return !getTasks().isEmpty();
    }
    
    /**
     * 获取启用的任务
     * @return 启用的任务列表
     */
    public List<DexConfuseTask> getTasks() {
        int level = getLevel();
        return CollectionUtil.toList(FilterIterator.of(
                getAllTasks(), task -> task.isEnabled(level)));
    }
}
