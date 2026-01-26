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
package com.reandroid.apkeditor.decompile;

import com.reandroid.apk.*;
import com.reandroid.apkeditor.CommandExecutor;
import com.reandroid.apkeditor.Util;
import com.reandroid.apkeditor.smali.SmaliDecompiler;
import com.reandroid.archive.ArchiveFile;
import com.reandroid.archive.block.ApkSignatureBlock;
import com.reandroid.arsc.chunk.TableBlock;
import com.reandroid.arsc.coder.xml.XmlCoder;

import java.io.File;
import java.io.IOException;

/**
 * APK反编译器类
 * 负责将APK文件反编译为XML、JSON或原始格式
 */
public class Decompiler extends CommandExecutor<DecompileOptions> {
    
    /**
     * 构造函数
     * @param options 反编译选项
     */
    public Decompiler(DecompileOptions options){
        super(options, "[DECOMPILE] ");
    }
    
    /**
     * 执行反编译命令
     * @throws IOException IO异常
     */
    @Override
    public void runCommand() throws IOException {
        DecompileOptions options = getOptions();
        // 删除输出目录
        delete(options.outputFile);
        
        // 加载APK文件
        logMessage("Loading ...");
        ApkModule apkModule=ApkModule.loadApkFile(this,
                options.inputFile, options.getFrameworks());
        apkModule.setPreferredFramework(options.frameworkVersion);
        
        // 如果指定了签名目录，则导出签名块
        if(options.signaturesDirectory != null){
            dumpSignatureBlock();
            return;
        }
        
        // 检查APK是否受保护
        String protect = Util.isProtected(apkModule);
        if(protect!=null){
            logMessage(options.inputFile.getAbsolutePath());
            logMessage(protect);
            return;
        }
        
        // 设置资源目录名称
        if(options.resDirName!=null){
            logMessage("Renaming resources root dir: "+options.resDirName);
            apkModule.setResourcesRootDir(options.resDirName);
        }
        
        // 验证资源目录
        if(options.validateResDir){
            logMessage("Validating resources dir ...");
            apkModule.validateResourcesDir();
        }
        
        // 开始反编译
        logMessage("Decompiling to " + options.type + " ...");

        ApkModuleDecoder decoder = getApkModuleDecoder(apkModule);
        decoder.decode(options.outputFile);
        logMessage("Saved to: "+options.outputFile);
    }
    
    /**
     * 获取APK模块解码器
     * 根据反编译类型创建相应的解码器
     * @param apkModule APK模块对象
     * @return APK模块解码器
     * @throws IOException IO异常
     */
    private ApkModuleDecoder getApkModuleDecoder(ApkModule apkModule) throws IOException {
        DecompileOptions options = getOptions();
        ApkModuleDecoder decoder;
        if (DecompileOptions.TYPE_JSON.equals(options.type)) {
            // JSON格式解码器
            decoder = new ApkModuleJsonDecoder(apkModule, options.splitJson);
        } else if (DecompileOptions.TYPE_RAW.equals(options.type)){
            // 原始格式解码器
            decoder = new ApkModuleRawDecoder(apkModule);
        } else {
            // XML格式解码器
            ApkModuleXmlDecoder xmlDecoder = new ApkModuleXmlDecoder(apkModule);
            xmlDecoder.setKeepResPath(options.keepResPath);
            decoder = xmlDecoder;
            XmlCoder.getInstance().getSetting().setLogger(this);
        }
        // 清理文件路径
        decoder.sanitizeFilePaths();
        // 设置DEX解码器
        decoder.setDexDecoder(getSmaliDecompiler(apkModule));
        // 设置DEX配置文件解码器
        DexProfileDecoderImpl dexProfileDecoder = new DexProfileDecoderImpl(options);
        dexProfileDecoder.setApkLogger(this);
        decoder.setDexProfileDecoder(dexProfileDecoder);
        return decoder;
    }
    
    /**
     * 获取Smali反编译器
     * @param apkModule APK模块对象
     * @return Smali反编译器
     * @throws IOException IO异常
     */
    private SmaliDecompiler getSmaliDecompiler(ApkModule apkModule) throws IOException {
        // 如果选项指定保持原始DEX格式，则返回null
        if (getOptions().dex) {
            return null;
        }
        // 获取用于DEX注释的表块
        TableBlock tableBlock = getTableBlockForDexComment(apkModule);
        SmaliDecompiler smaliDecompiler = new SmaliDecompiler(tableBlock, getOptions());
        smaliDecompiler.setApkLogger(this);
        return smaliDecompiler;
    }
    
    /**
     * 获取用于DEX注释的表块
     * @param apkModule APK模块对象
     * @return 表块对象
     * @throws IOException IO异常
     */
    private TableBlock getTableBlockForDexComment(ApkModule apkModule) throws IOException {
        // 如果没有DEX文件，返回null
        if (apkModule.listDexFiles().isEmpty()) {
            return null;
        }
        // 如果APK模块有表块，直接返回
        if (apkModule.hasTableBlock()) {
            return apkModule.getTableBlock();
        }
        // 尝试获取用户框架的表块
        TableBlock tableBlock = getUserFrameworkForDexComment();
        if (tableBlock == null) {
            // 如果用户框架不存在，获取内置框架的表块
            tableBlock = getInternalFrameworkForDexComment();
        }
        return tableBlock;
    }
    
    /**
     * 获取用户框架的表块
     * @return 表块对象
     * @throws IOException IO异常
     */
    private TableBlock getUserFrameworkForDexComment() throws IOException {
        DecompileOptions options = getOptions();

        File[] files = options.getFrameworks();
        // 如果只有一个框架文件且没有指定框架版本，直接加载
        if (files.length == 1 && options.frameworkVersion == null) {
            logMessage("Loading framework: " + files[0]);
            return ApkModule.loadApkFile(files[0]).getTableBlock();
        }
        // 合并多个框架文件
        TableBlock tableBlock = null;
        if (files.length != 0) {
            tableBlock = TableBlock.createEmpty();
            for (File file : files) {
                logMessage("Loading framework: " + file);
                tableBlock.addFramework(ApkModule.loadApkFile(file)
                        .getTableBlock());
            }
        }
        // 如果指定了框架版本，添加最佳匹配的框架
        if (tableBlock != null) {
            if (options.frameworkVersion != null) {
                FrameworkApk frameworkApk = AndroidFrameworks.getBestMatch(options.frameworkVersion);
                if (frameworkApk != null) {
                    tableBlock.addFramework(frameworkApk.getTableBlock());
                }
            }
            return tableBlock;
        }
        return null;
    }
    
    /**
     * 获取内置框架的表块
     * @return 表块对象
     */
    private TableBlock getInternalFrameworkForDexComment() {
        DecompileOptions options = getOptions();
        FrameworkApk frameworkApk = null;
        // 根据指定的框架版本获取最佳匹配
        if (options.frameworkVersion != null) {
            frameworkApk = AndroidFrameworks.getBestMatch(options.frameworkVersion);
        }
        // 如果没有找到，获取当前框架
        if (frameworkApk == null) {
            frameworkApk = AndroidFrameworks.getCurrent();
        }
        // 如果还是没有，获取最新框架
        if (frameworkApk == null) {
            frameworkApk = AndroidFrameworks.getLatest();
        }
        // 如果找到框架，返回其表块
        if (frameworkApk != null) {
            logMessage("Using internal framework: " + frameworkApk.getName());
            return frameworkApk.getTableBlock();
        }
        return null;
    }
    
    /**
     * 导出签名块
     * 将APK的签名块导出到指定目录
     * @throws IOException IO异常
     */
    private void dumpSignatureBlock() throws IOException {
        logMessage("Dumping signature blocks ...");
        DecompileOptions options = getOptions();
        ArchiveFile archive = new ArchiveFile(options.inputFile);
        ApkSignatureBlock apkSignatureBlock = archive.getApkSignatureBlock();
        // 如果没有签名块，返回
        if(apkSignatureBlock == null){
            logMessage("Don't have signature block");
            return;
        }
        // 将签名块写入目录
        apkSignatureBlock.writeSplitRawToDirectory(options.signaturesDirectory);
        logMessage("Signatures dumped to: " + options.signaturesDirectory);
    }
}
