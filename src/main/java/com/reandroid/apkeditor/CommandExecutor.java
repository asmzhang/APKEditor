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
package com.reandroid.apkeditor;

import com.reandroid.apk.APKLogger;
import com.reandroid.archive.ZipEntryMap;
import com.reandroid.apk.ApkModule;
import com.reandroid.arsc.ARSCLib;
import com.reandroid.arsc.coder.xml.XmlCoderLogger;
import com.reandroid.commons.utils.log.Logger;

import java.io.File;
import java.io.IOException;
import java.util.regex.Pattern;

/**
 * 命令执行器抽象基类
 * 实现了APK日志记录和XML编码日志记录接口，提供通用的命令执行功能
 * 
 * @param <T> 选项类型，必须继承自Options类
 */
public class CommandExecutor<T extends Options> implements APKLogger, XmlCoderLogger {

    /** 命令选项对象 */
    private final T options;
    /** 日志标签 */
    private String mLogTag;
    /** 是否启用日志 */
    private boolean mEnableLog;

    /**
     * 构造函数
     * @param options 命令选项对象
     * @param logTag 日志标签
     */
    public CommandExecutor(T options, String logTag){
        this.options = options;
        this.mLogTag = logTag;
        this.mEnableLog = true;
    }
    
    /**
     * 执行命令（已废弃，请使用runCommand()）
     * @throws IOException IO异常
     */
    @Deprecated
    public void run() throws IOException {
        runCommand();
    }
    
    /**
     * 执行命令（抽象方法，由子类实现）
     * @throws IOException IO异常
     */
    public void runCommand() throws IOException {
        throw new RuntimeException("Method not implemented");
    }
    
    /**
     * 删除文件或目录
     * @param file 要删除的文件或目录
     */
    protected void delete(File file) {
        if(file == null || !file.exists()) {
            return;
        }
        logMessage("Delete: " + file);
        if(file.isFile()) {
            file.delete();
        } else if(file.isDirectory()) {
            Util.deleteDir(file);
        }
    }
    
    /**
     * 获取命令选项对象
     * @return 命令选项对象
     */
    protected T getOptions() {
        return options;
    }

    /**
     * 应用extractNativeLibs属性
     * 控制APK中的native库是否被提取到文件系统
     * 
     * @param apkModule APK模块对象
     * @param extractNativeLibs extractNativeLibs属性值
     *         可选值：
     *         - "manifest": 使用AndroidManifest.xml中的值
     *         - "true": 强制设置为true
     *         - "false": 强制设置为false
     *         - 其他值: 不设置
     */
    protected void applyExtractNativeLibs(ApkModule apkModule, String extractNativeLibs) {
        if (extractNativeLibs != null) {
            Boolean value;
            if ("manifest".equalsIgnoreCase(extractNativeLibs)) {
                // 从AndroidManifest.xml读取值
                if (apkModule.hasAndroidManifest()) {
                    value = apkModule.getAndroidManifest().isExtractNativeLibs();
                } else {
                    value = null;
                }
            } else if ("true".equalsIgnoreCase(extractNativeLibs)) {
                value = Boolean.TRUE;
            } else if ("false".equalsIgnoreCase(extractNativeLibs)) {
                value = Boolean.FALSE;
            } else {
                value = null;
            }
            logMessage("Applying: extractNativeLibs=" + value);
            apkModule.setExtractNativeLibs(value);
        }
    }

    /**
     * 设置日志标签
     * @param tag 日志标签
     */
    protected void setLogTag(String tag) {
        if(tag == null){
            tag = "";
        }
        this.mLogTag = tag;
    }
    
    /**
     * 设置是否启用日志
     * @param enableLog 是否启用日志
     */
    public void setEnableLog(boolean enableLog) {
        this.mEnableLog = enableLog;
    }
    
    /**
     * 记录日志消息
     * @param msg 日志消息
     */
    @Override
    public void logMessage(String msg) {
        if(!mEnableLog){
            return;
        }
        Logger.i(mLogTag + msg);
    }
    
    /**
     * 记录错误日志
     * @param msg 错误消息
     * @param tr 异常对象
     */
    @Override
    public void logError(String msg, Throwable tr) {
        if(!mEnableLog){
            return;
        }
        Logger.e(mLogTag + msg, tr);
    }
    
    /**
     * 记录详细日志（在同一行输出）
     * @param msg 日志消息
     */
    @Override
    public void logVerbose(String msg) {
        if(!mEnableLog){
            return;
        }
        Logger.sameLine(mLogTag + msg);
    }
    
    /**
     * 记录带标签的日志消息
     * @param tag 日志标签
     * @param msg 日志消息
     */
    @Override
    public void logMessage(String tag, String msg) {
        if(!mEnableLog){
            return;
        }
        Logger.sameLine(mLogTag + msg);
    }

    /**
     * 记录带标签的详细日志
     * @param tag 日志标签
     * @param msg 日志消息
     */
    @Override
    public void logVerbose(String tag, String msg) {
        if(!mEnableLog){
            return;
        }
        Logger.sameLine(mLogTag + msg);
    }
    
    /**
     * 记录警告日志
     * @param msg 警告消息
     */
    public void logWarn(String msg) {
        Logger.e(mLogTag + msg);
    }

    /**
     * 记录版本信息
     */
    public void logVersion() {
        logMessage("Using: " + APKEditor.getName() + " version " + APKEditor.getVersion() + ", " + ARSCLib.getName() + " version " + ARSCLib.getVersion());
    }

    /**
     * 清除APK元数据
     * 包括签名和APK签名块
     * @param module APK模块对象
     */
    protected static void clearMeta(ApkModule module){
        removeSignature(module);
        module.setApkSignatureBlock(null);
    }
    
    /**
     * 移除APK签名
     * 删除META-INF目录下的签名文件
     * @param module APK模块对象
     */
    protected static void removeSignature(ApkModule module){
        ZipEntryMap archive = module.getZipEntryMap();
        // 删除META-INF目录下的所有签名文件（.MF, .SF, .RSA等）
        archive.removeIf(Pattern.compile("^META-INF/.+\\.(([MS]F)|(RSA))"));
        // 删除stamp-cert-sha256文件
        archive.remove("stamp-cert-sha256");
    }
}
