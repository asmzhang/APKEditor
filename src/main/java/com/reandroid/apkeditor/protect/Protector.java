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

		// 签名
		signAPK(options.outputFile);
    }

    /**
     * 签名APK文件
     * @param apkFile 要签名的APK文件
     */
    private void signAPK(java.io.File apkFile) {

        // 检查apksigner.jar是否存在
        java.io.File apksignerJar = new java.io.File("libs/apksigner.jar");
        if (!apksignerJar.exists()) {
            logMessage("Warning: apksigner.jar not found, skipping signature");
            return;
        }

        // 获取签名选项
        boolean signature = getOptions().signature;
        String signatureFile=getOptions().signatureFile;

        if (signatureFile != null) {
            // 使用指定文件
        } else if (signature) {
            // 使用默认签名
            try {
                // 构建签名命令
                java.util.List<String> command = new java.util.ArrayList<>();
                command.add("java");
                command.add("-jar");
                command.add(apksignerJar.getAbsolutePath());
                command.add("sign");
                command.add("--ks");
                command.add("src/main/resources/testKey.jks");
                command.add("--ks-pass");
                command.add("pass:testtest");
                command.add("--key-pass");
                command.add("pass:admin@test");
                command.add("--ks-key-alias");
                command.add("test");
                command.add("--out");
                String path = apkFile.getAbsolutePath();
                String signedApk = path.replaceFirst("\\.apk$", "_sign.apk");
                command.add(signedApk);
                command.add(apkFile.getAbsolutePath());

                // 执行签名命令
                logMessage("Signing APK...");
                ProcessBuilder processBuilder = new ProcessBuilder(command);
                processBuilder.redirectErrorStream(true);
                Process process = processBuilder.start();

                // 读取输出
                try (java.io.BufferedReader reader = new java.io.BufferedReader(
                        new java.io.InputStreamReader(process.getInputStream()))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        logMessage(line);
                    }
                }

                // 等待命令执行完成
                int exitCode = process.waitFor();
                if (exitCode == 0) {
                    java.io.File idsig = new java.io.File(signedApk + ".idsig");
                    if (idsig.exists()) {
                        idsig.delete();
                    }
                    logMessage("APK signed successfully");
                } else {
                    logMessage("Error signing APK: exit code " + exitCode);
                }
            } catch (Exception e) {
                logMessage("Error signing APK: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
}
