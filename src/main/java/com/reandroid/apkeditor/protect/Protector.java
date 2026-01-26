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

import java.io.IOException;

import com.reandroid.apk.ApkModule;
import com.reandroid.apkeditor.CommandExecutor;
import com.reandroid.apkeditor.Util;

/**
 * APK保护器类
 * 负责对APK文件进行混淆和保护处理
 */
public class Protector extends CommandExecutor<ProtectorOptions> {

    /** APK模块对象 */
    private ApkModule mApkModule;

    /**
     * 构造函数
     * @param options 保护选项
     */
    public Protector(ProtectorOptions options) {
        super(options, "[PROTECT] ");
    }

    /**
     * 获取APK模块对象
     * @return APK模块对象
     */
    public ApkModule getApkModule() {
        return this.mApkModule;
    }

    /**
     * 设置APK模块对象
     * @param apkModule APK模块对象
     */
    public void setApkModule(ApkModule apkModule) {
        this.mApkModule = apkModule;
    }

    /**
     * 获取保护选项
     * @return 保护选项对象
     */
    @Override
    public ProtectorOptions getOptions() {
        return super.getOptions();
    }

    /**
     * 执行保护命令
     * @throws IOException IO异常
     */
    @Override
    public void runCommand() throws IOException {
        ProtectorOptions options = getOptions();
        // 删除输出文件
        delete(options.outputFile);
        
        // 加载APK文件
        ApkModule module = ApkModule.loadApkFile(this, options.inputFile);
        module.setLoadDefaultFramework(false);
        
        // 检查APK是否受保护
        String protect = Util.isProtected(module);
        if(protect != null){
            logMessage(options.inputFile.getAbsolutePath());
            logMessage(protect);
            return;
        }
        
        // 设置APK模块
        setApkModule(module);
        
        // 执行各种混淆操作
        // 1. 混淆AndroidManifest.xml
        new ManifestConfuser(this).confuse();
        // 2. 混淆目录名称
        new DirectoryConfuser(this).confuse();
        // 3. 混淆文件名称
        new FileNameConfuser(this).confuse();
        // 4. 混淆资源表
        new TableConfuser(this).confuse();
        // 5. 混淆DEX文件
        new DexConfuser(this).confuse();
        
        // 刷新资源表
        module.getTableBlock().refresh();
        
        // 写入APK文件
        logMessage("Writing apk ...");
        if (options.confuse_zip) {
            // 如果启用ZIP结构混淆，使用保护文件写入器
            logMessage("Confusing zip structure ...");
            new ProtectedFileWriter(module, options.outputFile).write();
        } else {
            // 否则使用标准APK写入器
            module.writeApk(options.outputFile);
        }
        module.close();
        logMessage("Saved to: " + options.outputFile);
    }
}
